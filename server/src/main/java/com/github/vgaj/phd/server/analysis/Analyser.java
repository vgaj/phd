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

package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.result.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class Analyser implements AnalyserInterface
{
    // TODO: Analyser unit tests

    @Autowired
    private MonitorData monitorData;

    /**
     * The minimum interval between data that is of interest
     */
    @Value("${phd.minimum.interval.minutes}")
    private Integer minIntervalMinutes;

    /**
     * The minimum number of pairs of transmissions at an interval that is of interest
     */
    @Value("${phd.minimum.count.at.interval}")
    private Integer minCountAtInterval;

    /**
     * The minimum number of transmissions of the same size that are considered interesting
     */
    @Value("${phd.minimum.count.of.size}")
    private Integer minCountOfSameSize;

    private AnalyserUtil analyserUtil = new AnalyserUtil();

    /**
     * This is the logic which analyses the data for a given host
     * @param address The address to do the analysis for
     * @return Structure containing the results of the analysis if the minimal criteria is met
     */
    public Optional<AnalysisResult> analyse(RemoteAddress address)
    {
        AnalysisResultImpl result = new AnalysisResultImpl();

        // List of:
        //  - time
        //  - length of data at that time
        // Note that both calls below will sort this list
        List<Map.Entry<TransferTimestamp, TransferSizeBytes>> dataForAddress = monitorData.getCopyOfPerMinuteData(address);

        // Map of:
        // interval (minutes) -> list to lengths of data at this interval
        Map<TransferIntervalMinutes,List<TransferSizeBytes>> intervalsBetweenData = analyserUtil.getIntervalsBetweenData(dataForAddress);

        //=============
        // Pre-criteria: We are only interested in looking at hosts where every interval between
        // data is greater than the configured minimum.
        // This should reduce web browsing traffic getting captured.
        if (intervalsBetweenData.size() > 0 &&
                intervalsBetweenData.entrySet().stream().allMatch(entryForFrequency -> entryForFrequency.getKey().getInterval() >= minIntervalMinutes))
        {
            //=============
            // Repeated transfers at the same interval
            intervalsBetweenData.entrySet().stream()
                    .filter(e -> e.getValue().size() >= minCountAtInterval)
                    .sorted((entry1, entry2) ->
                    {
                        Integer size1 = entry1.getValue().size();
                        Integer size2 = entry2.getValue().size();
                        return size1.compareTo(size2);
                    })
                    .forEach(entry -> result.addRepeatedInterval(entry.getKey(), TransferCount.of(entry.getValue().size())));

            // TODO: Check if most are at same interval
            // TODO: Check if average interval is roughly (total run time / number of times)
            // TODO: Check if last reading is less than 2 x Average interval ago
            // TODO: Check if all sizes are similar - look at Std Dev

            //=============
            // Repeated transfers of the same size
            // Map of transfer size in bytes -> number of transfers
            Map<TransferSizeBytes, TransferCount> dataFrequencies = analyserUtil.getDataSizeFrequenciesFromRaw(dataForAddress);
            dataFrequencies.entrySet().stream().filter(e -> e.getValue().getCount() >= minCountOfSameSize)
                    .forEach(e -> result.addRepeatedTransferSize(e.getKey(),e.getValue()));

            // Set the last set time
            result.setLastSeenEpochMinute(monitorData.getDataForAddress(address).getLatestEpochMinute());

            return Optional.of(result);
        }
        else
        {
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

    public Set<RemoteAddress> getAddressesToIgnore()
    {
        HashSet<RemoteAddress> addressesToIgnore = new HashSet<>();

        long now = Instant.now().getEpochSecond() / 60;
        monitorData.getAddresses().forEach(address ->
        {
            boolean receivedDataInLastInterval = false;
            for (long minute = now; minute > now - minIntervalMinutes; minute--)
            {
                if (monitorData.getDataForAddress(address).getByteCountPerMinute().getOrDefault(minute, 0) > 0)
                {
                    if (receivedDataInLastInterval)
                    {
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
