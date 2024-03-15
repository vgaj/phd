package com.github.vgaj.phd.server.monitor.pcap;

import com.github.vgaj.phd.server.analysis.AnalyserInterface;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PcapCleanupTask
{
    @Autowired
    private AnalyserInterface analyser;

    @Autowired
    private MonitorTaskFilterUpdateInterface monitor;

    @Autowired
    private MessageData messages;

    private Map<RemoteAddress,Long> currentlyIgnoredAddresses = new HashMap<>();

    /**
     * The maximum number of addresses to put in the ignore list
     */
    @Value("${phd.maximum.addresses.to.ignore}")
    private int maxAddressesToIgnore = 250;

    @Scheduled(fixedRateString = "${phd.cleanup.interval.ms}", initialDelayString = "${phd.cleanup.interval.ms}")
    public void removeFrequentAddresses()
    {
        // Get addresses to ignore based on currently receiving data
        Set<RemoteAddress> addressesToIgnore = analyser.getAddressesToIgnore();

        // Get the new addresses that should be ignored
        List<RemoteAddress> newAddressesToIgnore = addressesToIgnore.stream().filter(a -> !currentlyIgnoredAddresses.containsKey(a)).collect(Collectors.toList());

        // There is a maximum number of addresses that will be ignored
        if (currentlyIgnoredAddresses.size() + newAddressesToIgnore.size() > maxAddressesToIgnore)
        {
            int numberToRemove = currentlyIgnoredAddresses.size() + newAddressesToIgnore.size() - maxAddressesToIgnore;
            List<RemoteAddress> addressesToRemove = currentlyIgnoredAddresses.entrySet().stream()
                    .filter(entry -> !addressesToIgnore.contains(entry.getKey()))
                    .sorted(Map.Entry.comparingByValue())
                    .limit(numberToRemove)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            addressesToRemove.forEach(currentlyIgnoredAddresses::remove);
        }

        newAddressesToIgnore.forEach( a ->
        {
            messages.addMessage("Not monitoring " + a.getAddressString());
            currentlyIgnoredAddresses.put(a, System.currentTimeMillis());
        });

        monitor.updateFilter(currentlyIgnoredAddresses.keySet());
    }
}
