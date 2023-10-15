package com.github.vgaj.phd.server.housekeeping;

import com.github.vgaj.phd.server.analysis.Analyser;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.monitor.MonitorTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CleanupTask
{
    @Autowired
    private Analyser analyser;

    @Autowired
    private MonitorTask monitor;

    @Autowired
    private MessageData messages;

    private Set<RemoteAddress> lastAddressesToIgnore = null;

    @Scheduled(fixedRateString = "${phd.cleanup.interval.ms}")
    public void removeFrequentAddresses()
    {
        // TODO Enforce a maximum to ignore
        // TODO When reaching the max remove the oldest
        // TODO If it comes back add it to a permanent exclude list
        Set<RemoteAddress> addressesToIgnore = analyser.getAddressesToIgnore();

        if (addressesToIgnore.size() > 0 )
        {
            monitor.updateFilter(addressesToIgnore);

            if (lastAddressesToIgnore != null)
            {
                addressesToIgnore.stream().filter(a -> !lastAddressesToIgnore.contains(a)).forEach(a ->
                        messages.addMessage("Not monitoring " + a.getAddressString()));
            }
            lastAddressesToIgnore = addressesToIgnore;
        }
    }
}
