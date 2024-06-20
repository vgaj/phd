/*
MIT License

Copyright (c) 2022-2024 Viru Gajanayake

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.vgaj.phd.server.monitor.bpf;

import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import com.github.vgaj.phd.common.util.Pair;
import com.github.vgaj.phd.server.monitor.MonitorTaskFilterUpdateInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "phd.use.bpf", havingValue = "true", matchIfMissing = true)
public class BpfMonitorTask implements MonitorTaskFilterUpdateInterface
{
    private MessageInterface messages = Messages.getLogger(this.getClass());

    @Autowired
    private MonitorData monitorData;

    @Autowired
    private LibBpfWrapper libBpfWrapper;

    @Value("${phd.bpf.map.ip.bytes}")
    private String bpf_map_ip_bytes;

    @Value("${phd.bpf.map.ip.time}")
    private String bpf_map_ip_time;

    @Value("${phd.bpf.map.pid.time}")
    private String bpf_map_pid_time;

    private Set<RemoteAddress> addressesToIgnore = new ConcurrentSkipListSet<RemoteAddress>();

    int mapFdIpBytes;
    int mapFdIpTime;
    int mapFdPidTime;

    // Occurs after @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void load()
    {
        mapFdIpBytes = libBpfWrapper.getMapFdByName(bpf_map_ip_bytes);
        if (mapFdIpBytes == -1)
        {
            messages.addError("Map " + bpf_map_ip_bytes + " was not loaded");
        }
        mapFdIpTime = libBpfWrapper.getMapFdByName(bpf_map_ip_time);
        if (mapFdIpTime == -1)
        {
            messages.addError("Map " + bpf_map_ip_time + " was not loaded");
        }
        mapFdPidTime = libBpfWrapper.getMapFdByName(bpf_map_pid_time);
        if (mapFdPidTime == -1)
        {
            messages.addError("Map " + bpf_map_pid_time + " was not loaded");
        }
    }

    // Occurs before @PreDestroy
    @EventListener(ContextClosedEvent.class)
    public void unload()
    {
    }

    @Scheduled(cron = "59 * * * * *")
    public void collectData()
    {
        long start = System.currentTimeMillis();
        Long epochMinute = EpochMinuteUtil.now();

        List<Pair<RemoteAddress,Integer>> ipToBytesForLastMinute =  libBpfWrapper.getAddressToCountData(mapFdIpBytes);
        List<Pair<RemoteAddress,Long>> ipToTimeForLastMinute =  libBpfWrapper.getAddressToTimeData(mapFdIpTime);
        List<Pair<Integer,Long>> pidToTimeForLastMinute =  libBpfWrapper.getPidToTimeData(mapFdPidTime);
        messages.addMessage("Total time (ms) to get data: " + (System.currentTimeMillis() - start));

        // Process count data
        ipToBytesForLastMinute.forEach(entry->
        {
            if (!addressesToIgnore.contains(entry.getKey()))
            {
                monitorData.addData(entry.getKey(), entry.getValue(), epochMinute);
            }
        });

        ipToTimeForLastMinute.forEach(entry ->
        {
            messages.addMessage("ADDR->TIME " + entry.getKey().getAddressString() + "   " + entry.getValue());
        });
        pidToTimeForLastMinute.forEach( entry ->
        {
            messages.addMessage("PID->TIME " + entry.getKey() + "   " + entry.getValue());
        });

        RemoteAddress ignore1 = new RemoteAddress((byte)192,(byte)168,(byte)1,(byte)1);
        RemoteAddress ignore2 = new RemoteAddress((byte)255,(byte)255,(byte)255,(byte)255);

        // Work out PID for address
        pidToTimeForLastMinute.forEach( pidToTime ->
        {
            long rangeStart = pidToTime.getValue();
            long rangeEnd = pidToTime.getValue() + 500_000_000;
            List<Pair<RemoteAddress,Long>> candidates = ipToTimeForLastMinute.stream()
                    .filter(ipToTime -> !ipToTime.getKey().equals(ignore1) && !ipToTime.getKey().equals(ignore2))
                    .filter(ipToTime -> ipToTime.getValue() > rangeStart && ipToTime.getValue() < rangeEnd)
                    .collect(Collectors.toList());

            candidates.forEach( x -> messages.addMessage("PID " + pidToTime.getKey() + " time ns " + pidToTime.getValue() + " delay ns " + (x.getValue() - rangeStart) + " address: " + x.getKey().getAddressString()));

        });

        messages.addMessage("Total time (ms) to process: " + (System.currentTimeMillis() - start));
    }

    @Override
    public void updateFilter(Set<RemoteAddress> addressesToExclude)
    {
        int sizeBefore = addressesToIgnore.size();
        addressesToExclude.forEach(a ->
        {
            try
            {
                if (addressesToIgnore.add(a))
                {
                    messages.addDebug("Not monitoring " + a.getAddressString());
                }
            }
            catch (Throwable t)
            {
                messages.addError("Failed to add address to ignore", t);
            }
        });
        int sizeAfter = addressesToIgnore.size();

        if (sizeAfter > sizeBefore)
        {
            messages.addDebug("Total ignored addresses now is " + sizeAfter);
        }
    }
}
