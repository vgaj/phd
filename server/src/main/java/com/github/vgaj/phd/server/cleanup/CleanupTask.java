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

package com.github.vgaj.phd.server.cleanup;

import com.github.vgaj.phd.server.analysis.RawDataProcessorInterface;
import com.github.vgaj.phd.server.store.TrafficDataStore;
import com.github.vgaj.phd.server.address.SourceAndDestinationAddress;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import com.github.vgaj.phd.server.monitor.MonitorTaskFilterUpdateInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CleanupTask {
    @Autowired
    private RawDataProcessorInterface rawDataProcessor;

    @Autowired
    private MonitorTaskFilterUpdateInterface monitor;

    @Autowired
    private TrafficDataStore data;

    private MessageInterface messages = Messages.getLogger(this.getClass());

    @Scheduled(cron = "30 * * * * *")
    public void removeFrequentAddresses() {
        // Get addresses to ignore based on currently receiving data
        Set<SourceAndDestinationAddress> addressesToIgnore = rawDataProcessor.getAddressesToIgnore();

        // Add to list to ignore when monitoring
        monitor.updateFilter(addressesToIgnore);

        // Remove addressees from stored data
        data.cleanupIgnoredAddresses(addressesToIgnore);
    }
}
