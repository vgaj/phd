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

package com.github.vgaj.phd.server.monitor.pcap;

import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;

import org.pcap4j.core.*;
import static org.pcap4j.core.PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;


/**
 * Retrieves captured data from Pcap4j
 */
@Component
@ConditionalOnProperty(name = "phd.use.bpf", havingValue = "false", matchIfMissing = false)
public class PcapMonitorTask implements Runnable, MonitorTaskFilterUpdateInterface
{
    private MessageInterface messages = Messages.getLogger(this.getClass());

    @Autowired
    private PcapNewDataProcessor newDataProcessor;

    private Thread monitorThread;

    private PcapHandle handle;

    // Occurs after @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void start()
    {
        monitorThread = new Thread(this);
        monitorThread.start();
    }

    // Occurs before @PreDestroy
    @EventListener(ContextClosedEvent.class)
    public void stop()
    {
        try
        {
            handle.breakLoop();
            monitorThread.join(5000);
        }
        catch (NotOpenException | InterruptedException e)
        {
            messages.addError("Error while stopping", e);
        }
    }

    @Value("${phd.filter}")
    private String filter;

    /**
     * Update the libpcap filter with a list of addresses to exclude
     * On low-end hardware this operation takes about:
     * - 2ms for 50 addresses
     * - 50ms for 250
     * - 6 seconds for 2500
     * - and you get a Segmentation fault with 10000
     * @param addressesToExclude Addresses to exclude
     */
    public void updateFilter(Set<RemoteAddress> addressesToExclude)
    {
        try
        {

            long start = System.nanoTime();
            StringBuilder newFilter = new StringBuilder();
            newFilter.append("(").append(filter).append(")");
            addressesToExclude.forEach( address -> newFilter.append(" and not host ").append(address.getAddressString()));
            handle.setFilter(newFilter.toString(), BpfProgram.BpfCompileMode.OPTIMIZE);
            long durationMs = (System.nanoTime() - start) / 1000000;
            if (durationMs > 0)
            {
                messages.addDebug("Filter update took " + durationMs + " ms");
            }

        }
        catch (PcapNativeException | NotOpenException e)
        {
            messages.addError("Failed to update libpcap filter", e);
        }
    }

    @Override
    public void run()
    {
        try
        {
            PcapNetworkInterface nif = null;
            try
            {
                Optional<PcapNetworkInterface> optInt = Pcaps.findAllDevs().stream()
                        .filter(i -> !i.isLoopBack()
                                && !i.getAddresses().isEmpty()
                                && !i.getName().equalsIgnoreCase("any")
                                && i.isRunning()
                                && i.isUp())
                        .findFirst();
                if (optInt.isEmpty())
                {
                    messages.addError("Could not find NIC");
                    return;
                }
                nif = optInt.get();
                messages.addMessage("Using " + nif.getName());
            }
            catch (PcapNativeException e)
            {
                messages.addError("Error looking for NIC", e);
                return;
            }

            // NB: Packets are getting captured from this point
            handle = nif.openLive(65536, PROMISCUOUS, 100);

            messages.addMessage("Using filter: " + filter);
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

            PacketListener listener = pcapPacket -> { newDataProcessor.processNewData(pcapPacket); };

            handle.loop(-1, listener);
            handle.close(); // This won't normally get called
        }
        catch (InterruptedException e)
        {
        }
        catch (Exception e)
        {
            messages.addError("Exiting monitor thread due to an error ", e);
        }
    }
}
