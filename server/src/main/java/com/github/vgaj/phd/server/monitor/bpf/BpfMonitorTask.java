/*
MIT License

Copyright (c) 2022-2025 Viru Gajanayake

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
import com.github.vgaj.phd.common.util.Pair;
import com.github.vgaj.phd.server.address.SourceAndDestinationAddress;
import com.github.vgaj.phd.server.store.TrafficDataRecorder;
import com.github.vgaj.phd.server.lookup.HostToExecutableLookup;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import com.github.vgaj.phd.server.monitor.MonitorTaskFilterUpdateInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "phd.use.bpf", havingValue = "true", matchIfMissing = true)
public class BpfMonitorTask implements MonitorTaskFilterUpdateInterface {

    private final Set<SourceAndDestinationAddress> addressesToIgnore = ConcurrentHashMap.newKeySet();

    List<Integer> mapIpBytesList;

    int mapFdIpPid;

    private final MessageInterface messages = Messages.getLogger(this.getClass());

    @Autowired
    private List<TrafficDataRecorder> trafficDataRecorders;

    @Autowired
    private HostToExecutableLookup hostToExecutableLookup;

    @Autowired
    private LibBpfWrapper libBpfWrapper;

    @Value("${phd.bpf.map.ip.bytes}")
    private String bpf_map_ip_bytes;

    @Value("${phd.bpf.map.ip.pid}")
    private String bpf_map_ip_pid;

    // Occurs after @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        mapIpBytesList = libBpfWrapper.getAllMapFdsByName(bpf_map_ip_bytes);
        if (mapIpBytesList.isEmpty()) {
            messages.addError("Map " + bpf_map_ip_bytes + " was not loaded");
        }
        List<Integer> mapIpPidList = libBpfWrapper.getAllMapFdsByName(bpf_map_ip_pid);
        if (!mapIpPidList.isEmpty()) {
            mapFdIpPid = mapIpPidList.get(0);
        } else {
            messages.addError("Map " + bpf_map_ip_pid + " was not loaded");
            mapFdIpPid = -1;
        }
    }

    // Occurs before @PreDestroy
    @EventListener(ContextClosedEvent.class)
    public void unload() {
    }

    // 59th second of every minute
    // We assume the scheduled traffic we are interested in will occur on the minute
    @Scheduled(cron = "59 * * * * *")
    public void collectData() {
        long start = System.currentTimeMillis();
        long epochMinute = EpochMinuteUtil.now();

        List<Pair<SourceAndDestinationAddress, Integer>> ipToBytesForLastMinute = new ArrayList<>();
        mapIpBytesList.forEach(id -> ipToBytesForLastMinute.addAll(libBpfWrapper.getAddressToCountData(id)));
        List<Pair<SourceAndDestinationAddress, Integer>> ipToPidForLastMinute = libBpfWrapper.getAddressToPidData(mapFdIpPid);
        messages.addDebug("Total time (ms) to get data: " + (System.currentTimeMillis() - start));

        // Store count data
        ipToBytesForLastMinute.forEach(entry ->
        {
            if (!addressesToIgnore.contains(entry.getKey())) {
                trafficDataRecorders.forEach(recorder -> recorder.addData(entry.getKey(), entry.getValue(), epochMinute));
            }
        });

        // Store process data
        ipToPidForLastMinute.forEach(entry ->
        {
            hostToExecutableLookup.addData(entry.getKey(), entry.getValue());
        });

        messages.addDebug("Total time (ms) to get and store: " + (System.currentTimeMillis() - start));
    }

    @Override
    public void updateFilter(Set<SourceAndDestinationAddress> addressesToExclude) {
        int sizeBefore = addressesToIgnore.size();
        addressesToExclude.forEach(a ->
        {
            try {
                if (addressesToIgnore.add(a)) {
                    messages.addDebug("Not monitoring " + a.getSourceAndDestinationAddressString());
                }
            } catch (Throwable t) {
                messages.addError("Failed to add address to ignore", t);
            }
        });
        int sizeAfter = addressesToIgnore.size();

        if (sizeAfter > sizeBefore) {
            messages.addDebug("Total ignored addresses now is " + sizeAfter);
        }
    }
}
