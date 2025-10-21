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

package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.server.data.TransferTimestamp;
import com.github.vgaj.phd.server.lookup.HostToExecutableLookup;
import com.github.vgaj.phd.server.store.TrafficDataStore;
import com.github.vgaj.phd.server.address.SourceAndDestinationAddress;

import com.github.vgaj.phd.server.result.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RawDataProcessor implements RawDataProcessorInterface {
    @Autowired
    private TrafficDataStore trafficDataStore;

    @Autowired
    private HostToExecutableLookup hostToExecutableLookup;

    /**
     * The minimum interval between data that is of interest
     */
    @Value("${phd.minimum.interval.minutes}")
    private Integer minIntervalMinutes;

    private final RawDataProcessorUtil rawDataProcessorUtil = new RawDataProcessorUtil();

    /**
     * This is the logic which process the data for a given host
     *
     * @param address The address to do the processing for
     * @return Structure containing the results of the analysis if the minimal criteria is met
     */
    public Optional<AnalysisResult> processRawData(SourceAndDestinationAddress address) {
        AnalysisResultImpl result = new AnalysisResultImpl();

        // List of:
        //  - time
        //  - length of data at that time
        // Note that both calls below will sort this list
        List<Map.Entry<TransferTimestamp, TransferSizeBytes>> dataForAddress = trafficDataStore.getCopyOfPerMinuteData(address);

        // Map of:
        // interval (minutes) -> list to lengths of data at this interval
        Map<TransferIntervalMinutes, List<TransferSizeBytes>> intervalsBetweenData = rawDataProcessorUtil.getIntervalsBetweenData(dataForAddress);

        //=============
        // Pre-criteria: We are only interested in looking at hosts where every interval between
        // data is greater than the configured minimum.
        // This should reduce web browsing traffic getting captured.
        if (!intervalsBetweenData.isEmpty() &&
                intervalsBetweenData.entrySet().stream().allMatch(entryForFrequency -> entryForFrequency.getKey().getInterval() >= minIntervalMinutes)) {
            //=============
            // Repeated transfers at the same interval
            intervalsBetweenData.entrySet().stream()
                    .sorted((entry1, entry2) ->
                    {
                        Integer size1 = entry1.getValue().size();
                        Integer size2 = entry2.getValue().size();
                        return size1.compareTo(size2);
                    })
                    .forEach(entry -> result.addIntervalCount(entry.getKey(), TransferCount.of(entry.getValue().size())));

            //=============
            // Repeated transfers of the same size
            // Map of transfer size in bytes -> number of transfers
            Map<TransferSizeBytes, TransferCount> dataFrequencies = rawDataProcessorUtil.getDataSizeFrequenciesFromRaw(dataForAddress);
            dataFrequencies.forEach(result::addTransferSizeCount);

            // Set the last set time
            result.setLastSeenEpochMinute(trafficDataStore.getDataForAddress(address).getLatestEpochMinute());

            // Set the probable last executable
            // Definitely not guaranteed to be correct, it's just the last exe connecting to that host
            String executable = hostToExecutableLookup.getProcessForAddress(address);
            result.setProbableExecutable(executable != null ? executable : " ");

            return Optional.of(result);
        } else {
            /* For testing
            if (true)
            {
                result.addRepeatedInterval(TransferIntervalMinutes.of(13), TransferCount.of(42));
                result.setLastSeenEpochMinute(monitorData.getDataForAddress(address).getLatestEpochMinute());
                return Optional.of(result);
            }
            */

            return Optional.empty();
        }
    }

    /**
     * Identifies addresses which can be ignored.
     * If data was sent to the address in more than once over the previous
     * minimum duration then it will be ignored
     *
     * @return addresses to ignore
     */
    public Set<SourceAndDestinationAddress> getAddressesToIgnore() {
        HashSet<SourceAndDestinationAddress> addressesToIgnore = new HashSet<>();

        long now = EpochMinuteUtil.now();
        trafficDataStore.getAddresses().forEach(address ->
        {
            boolean receivedDataInLastInterval = false;

            // We might not have received data for the current minute yet,
            // so we look back from the previous minute to the minute that
            // was the minimum duration ago.
            // For example if this is minute 100 and the minimum interval is 2
            // then we look at minute 99 and 98
            for (long minute = now - 1; minute >= now - minIntervalMinutes; minute--) {
                if (trafficDataStore.getDataForAddress(address).getByteCountPerMinute().getOrDefault(minute, 0) > 0) {
                    if (receivedDataInLastInterval) {
                        addressesToIgnore.add(address);
                        break;
                    }
                    receivedDataInLastInterval = true;
                }
            }
        });
        return addressesToIgnore;
    }

}
