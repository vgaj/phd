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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
@ConditionalOnProperty(name = "phd.use.bpf", havingValue = "true", matchIfMissing = true)
public class BpfMonitorTask implements MonitorTaskFilterUpdateInterface
{
    private MessageInterface messages = Messages.getLogger(this.getClass());

    @Autowired
    private MonitorData monitorData;

    @Autowired
    private LibBpfWrapper libBpfWrapper;

    @Value("${phd.bpf.map.name}")
    private String bpf_map_name;

    private Set<RemoteAddress> addressesToIgnore = new ConcurrentSkipListSet<RemoteAddress>();

    int mapFd;

    // Occurs after @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void load()
    {
        mapFd = libBpfWrapper.getMapFdByName(bpf_map_name);
        if (mapFd == -1)
        {
            messages.addError("Map " + bpf_map_name + " was not loaded");
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
        Long epochMinute = EpochMinuteUtil.now();
        if (mapFd != -1)
        {
            List<Pair<RemoteAddress,Integer>> dataForLastMinute =  libBpfWrapper.getData(mapFd);
            dataForLastMinute.forEach(entry->
            {
                if (!addressesToIgnore.contains(entry.getKey()))
                {
                    monitorData.addData(entry.getKey(), entry.getValue(), epochMinute);
                }
            });
        }
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
