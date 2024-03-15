package com.github.vgaj.phd.server.monitor.pcap;

import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageData;
import org.pcap4j.core.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static org.pcap4j.core.PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;


/**
 * Retrieves captured data from Pcap4j
 */
@Component
public class PcapMonitorTask implements Runnable, MonitorTaskFilterUpdateInterface
{
    @Autowired
    private MessageData messageData;

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
            messageData.addError("Error while stopping", e);
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
                messageData.addMessage("Filter update took " + durationMs + " ms");
            }

        }
        catch (PcapNativeException | NotOpenException e)
        {
            messageData.addError("Failed to update libpcap filter", e);
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
                    messageData.addMessage("Could not find NIC");
                    return;
                }
                nif = optInt.get();
                messageData.addMessage("Using " + nif.getName());
            }
            catch (PcapNativeException e)
            {
                messageData.addError("Error looking for NIC", e);
                return;
            }

            // NB: Packets are getting captured from this point
            handle = nif.openLive(65536, PROMISCUOUS, 100);

            messageData.addMessage("Using filter: " + filter);
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
            messageData.addError("Exiting monitor thread due to an error ", e);
        }
    }
}
