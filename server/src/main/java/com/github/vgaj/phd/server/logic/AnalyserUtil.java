package com.github.vgaj.phd.server.logic;

import com.github.vgaj.phd.server.result.TransferCount;
import com.github.vgaj.phd.server.result.TransferIntervalMinutes;
import com.github.vgaj.phd.server.result.TransferSizeBytes;
import com.github.vgaj.phd.server.result.TransferTimestamp;

import java.util.*;
import java.util.stream.Collectors;

public class AnalyserUtil
{
    /**
     * Helper to get a map of interval (minutes) to list to lengths of data at this interval
     * @param dataForAddress Raw data for the destination
     */
    public Map<TransferIntervalMinutes, List<TransferSizeBytes>> getIntervalsBetweenData(
            List<Map.Entry<TransferTimestamp, TransferSizeBytes>> dataForAddress)
    {
        Map<TransferIntervalMinutes,List<TransferSizeBytes>> results = new HashMap<>();

        Collections.sort(dataForAddress, new Comparator<Map.Entry<TransferTimestamp, TransferSizeBytes>>()
        {
            @Override
            public int compare(Map.Entry<TransferTimestamp, TransferSizeBytes> e1, Map.Entry<TransferTimestamp, TransferSizeBytes> e2)
            {
                return e1.getKey().compareTo(e2.getKey());
            }
        });

        Optional<TransferTimestamp> lastRequest = Optional.empty();
        for (var e : dataForAddress)
        {
            if (lastRequest.isPresent())
            {
                TransferIntervalMinutes interval = (e.getKey().subtract(lastRequest.get()));
                results.putIfAbsent(interval, new ArrayList<>());
                results.get(interval).add(e.getValue());
            }
            lastRequest = Optional.of(e.getKey());
        }
        return results;
    }

    /**
     * Get a map of data length to number of transfers of that length
     * @param dataSizes Raw data - data sizes sent to the destination
     */
    public Map<TransferSizeBytes, TransferCount> getDataSizeFrequencies(List<TransferSizeBytes> dataSizes)
    {
        Map<TransferSizeBytes,TransferCount> result = new HashMap<>();
        Map<TransferSizeBytes,Long> tempResult = dataSizes.stream().map(s -> s.getSize())
                .collect(Collectors.groupingBy(s -> new TransferSizeBytes(s), Collectors.counting()));
        tempResult.entrySet().forEach(e -> result.put(e.getKey(), new TransferCount(e.getValue().intValue())));
        return result;
    }
    public Map<TransferSizeBytes,TransferCount> getDataSizeFrequenciesFromRaw(List<Map.Entry<TransferTimestamp, TransferSizeBytes>> dataForAddress)
    {
        return getDataSizeFrequencies( dataForAddress.stream().map(e -> e.getValue()).collect(Collectors.toList()));
    }

}
