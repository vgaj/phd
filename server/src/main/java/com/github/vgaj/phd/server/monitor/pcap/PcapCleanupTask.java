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

import com.github.vgaj.phd.server.analysis.AnalyserInterface;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.Messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "phd.use.bpf", havingValue = "false", matchIfMissing = false)
public class PcapCleanupTask
{
    @Autowired
    private AnalyserInterface analyser;

    @Autowired
    private MonitorTaskFilterUpdateInterface monitor;

    @Autowired
    private Messages messages;

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
