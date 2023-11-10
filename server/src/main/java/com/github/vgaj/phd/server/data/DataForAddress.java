package com.github.vgaj.phd.server.data;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DataForAddress
{
    private int totalBytes = 0;

    /**
     * Number of bytes in minute blocks
     * key -> epoch minute
     * value -> byte count
     */
    @Getter
    private final ConcurrentMap<Long, Integer> byteCountPerMinute = new ConcurrentHashMap<>();

    public void addBytes(int count, long epochMinute)
    {
        totalBytes += count;
        byteCountPerMinute.put(epochMinute,
                count + (byteCountPerMinute.containsKey(epochMinute) ? byteCountPerMinute.get(epochMinute) : 0));
    }
    public int getTotalBytes()
    {
        return totalBytes;
    }

    public int getMinuteBlockCount()
    {
        return byteCountPerMinute.keySet().size();
    }

    public long getLatestEpochMinute()
    {
        return byteCountPerMinute.keySet().stream().max(Long::compareTo).orElse(0L);
    }

    public List<Map.Entry<Long, Integer>> getPerMinuteData()
    {
        return new ArrayList<>(byteCountPerMinute.entrySet());
    }

    // TODO Remove
    public List<String> getPerMinuteDataForDisplay(int countToShow)
    {
        var data = byteCountPerMinute.entrySet();
        int dataLength = data.size();
        return data.stream()
                .sorted(Comparator.comparing(e -> ((Long) e.getKey())))
                .skip( countToShow < dataLength ? dataLength - countToShow : 0)
                .limit( countToShow)
                .map(e -> getDisplayTime(e.getKey()) + ':' + e.getValue())
                .collect(Collectors.toList());
    }

    private String getDisplayTime(long epochMinute)
    {
        Instant instant = Instant.ofEpochSecond(epochMinute * 60);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        return ldt.toString();
    }

}
