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

package com.github.vgaj.phd.server.data;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataForAddress {
    private int totalBytes = 0;

    /**
     * Number of bytes in minute blocks
     * key -> epoch minute
     * value -> byte count
     */
    @Getter
    private final ConcurrentMap<Long, Integer> byteCountPerMinute = new ConcurrentHashMap<>();

    public void addBytes(int count, long epochMinute) {
        totalBytes += count;
        byteCountPerMinute.put(epochMinute,
                count + (byteCountPerMinute.containsKey(epochMinute) ? byteCountPerMinute.get(epochMinute) : 0));
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public int getMinuteBlockCount() {
        return byteCountPerMinute.keySet().size();
    }

    public long getLatestEpochMinute() {
        return byteCountPerMinute.keySet().stream().max(Long::compareTo).orElse(0L);
    }

    public List<Map.Entry<Long, Integer>> getPerMinuteData() {
        return new ArrayList<>(byteCountPerMinute.entrySet());
    }
}
