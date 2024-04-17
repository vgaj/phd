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

import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.util.EpochMinute;
import com.github.vgaj.phd.server.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "phd.use.bpf", havingValue = "true", matchIfMissing = true)
public class BpfMonitorTask
{
    //TODO rename
    @Autowired
    private MessageData messageData;

    @Autowired
    private MonitorData monitorData;

    @Autowired
    private LibBpfWrapper libBpfWrapper;

    @Value("${phd.bpf.map.name}")
    private String bpf_map_name;

    int mapFd;

    // Occurs after @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void load()
    {
        // TODO: Load BPF Program

        mapFd = libBpfWrapper.getMapFdByName(bpf_map_name);
        if (mapFd == -1)
        {
            messageData.addMessage("Map " + bpf_map_name + " was not loaded");

            // TODO: Manually load Pcap implementation if BPF program can't be loaded
        }
    }

    // Occurs before @PreDestroy
    @EventListener(ContextClosedEvent.class)
    public void unload()
    {
        // TODO: Unload
    }

    @Scheduled(cron = "59 * * * * *")
    public void collectData()
    {
        Long epochMinute = EpochMinute.now();
        if (mapFd != -1)
        {
            messageData.addMessage("The 59th");
            // TODO: An ignore implementation

            List<Pair<RemoteAddress,Integer>> dataForLastMinute =  libBpfWrapper.getData(mapFd);
            dataForLastMinute.forEach(entry-> {
                messageData.addMessage("BPF Received " + entry.getValue() + " bytes for " + entry.getKey().getAddressString());
                monitorData.addData(entry.getKey(), entry.getValue(), epochMinute);
            });
        }
    }
}
