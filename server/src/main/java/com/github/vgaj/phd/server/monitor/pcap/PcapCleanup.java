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

package com.github.vgaj.phd.server.monitor.pcap;

import com.github.vgaj.phd.server.address.SourceAndDestinationAddress;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;

import com.github.vgaj.phd.server.monitor.MonitorTaskFilterUpdateInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "phd.use.bpf", havingValue = "false", matchIfMissing = false)
public class PcapCleanup implements MonitorTaskFilterUpdateInterface {
    @Autowired
    private PcapMonitorTaskFilterUpdateInterface monitor;

    private MessageInterface messages = Messages.getLogger(this.getClass());

    private Map<SourceAndDestinationAddress, Long> currentlyIgnoredAddresses = new HashMap<>();

    /**
     * The maximum number of addresses to put in the ignore list
     */
    @Value("${phd.maximum.addresses.to.ignore}")
    private int maxAddressesToIgnore = 250;

    @Override
    public void updateFilter(Set<SourceAndDestinationAddress> addressesToExclude) {
        // Get the new addresses that should be ignored
        List<SourceAndDestinationAddress> newAddressesToIgnore = addressesToExclude.stream().filter(a -> !currentlyIgnoredAddresses.containsKey(a)).collect(Collectors.toList());

        // There is a maximum number of addresses that will be ignored
        if (currentlyIgnoredAddresses.size() + newAddressesToIgnore.size() > maxAddressesToIgnore) {
            int numberToRemove = currentlyIgnoredAddresses.size() + newAddressesToIgnore.size() - maxAddressesToIgnore;
            List<SourceAndDestinationAddress> addressesToRemove = currentlyIgnoredAddresses.entrySet().stream()
                    .filter(entry -> !addressesToExclude.contains(entry.getKey()))
                    .sorted(Map.Entry.comparingByValue())
                    .limit(numberToRemove)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            addressesToRemove.forEach(currentlyIgnoredAddresses::remove);
        }

        newAddressesToIgnore.forEach(a ->
        {
            messages.addDebug("Not monitoring " + a.getDesinationAddressString());
            currentlyIgnoredAddresses.put(a, System.currentTimeMillis());
        });

        monitor.updateFilter(currentlyIgnoredAddresses.keySet());

    }
}
