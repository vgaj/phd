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

package com.github.vgaj.phd.server.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.vgaj.phd.common.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Store the results from analysing the data
 */
public class AnalysisResultImpl implements AnalysisResult {
    @Setter
    @Getter
    long lastSeenEpochMinute;

    @Setter
    @Getter
    String probableExecutable;

    /**
     * TRANSFER INTERVAL - List of: interval in minutes -> number of times
     */
    private List<Pair<TransferIntervalMinutes, TransferCount>> intervals = new ArrayList<>();

    public void addIntervalCount(TransferIntervalMinutes intervalMinutes, TransferCount numberOfTimes) {
        intervals.add(Pair.of(intervalMinutes, numberOfTimes));
    }

    @JsonIgnore
    @Override
    public List<Pair<TransferIntervalMinutes, TransferCount>> getIntervalCount() {
        return intervals;
    }

    /**
     * TRANSFER SIZE - List of: sizes of transfers in bytes -> number to times
     */
    private List<Pair<TransferSizeBytes, TransferCount>> dataSizes = new ArrayList<>();

    public void addTransferSizeCount(TransferSizeBytes transferSizeBytes, TransferCount numberOfTimes) {
        dataSizes.add(Pair.of(transferSizeBytes, numberOfTimes));
    }

    @JsonIgnore
    @Override
    public List<Pair<TransferSizeBytes, TransferCount>> getTransferSizeCount() {
        return dataSizes;
    }

    @Override
    public AnalysisResult merge(AnalysisResult other) {
        AnalysisResultImpl combinedResult = new AnalysisResultImpl();

        HashMap<TransferIntervalMinutes, TransferCount> combinedIntervals = new HashMap<>();
        this.intervals.forEach(interval -> combinedIntervals.put(interval.getKey(), interval.getValue()));
        other.getIntervalCount().forEach(interval -> combinedIntervals.merge(interval.getKey(), interval.getValue(), TransferCount::merge));
        combinedIntervals.forEach((interval, count) -> combinedResult.addIntervalCount(interval, count));

        HashMap<TransferSizeBytes, TransferCount> combinedSizes = new HashMap<>();
        this.dataSizes.forEach(size -> combinedSizes.put(size.getKey(), size.getValue()));
        other.getTransferSizeCount().forEach(size -> combinedSizes.merge(size.getKey(), size.getValue(), TransferCount::merge));
        combinedSizes.forEach((size, count) -> combinedResult.addTransferSizeCount(size, count));

        combinedResult.setLastSeenEpochMinute(
                this.getLastSeenEpochMinute() > other.getLastSeenEpochMinute() ? this.getLastSeenEpochMinute() : other.getLastSeenEpochMinute());

        // Use the executable from the result that was seen later
        if (this.getLastSeenEpochMinute() > other.getLastSeenEpochMinute() && this.getProbableExecutable() != null && !this.getProbableExecutable().isBlank()) {
            combinedResult.setProbableExecutable(this.getProbableExecutable());
        } else {
            combinedResult.setProbableExecutable(other.getProbableExecutable());
        }

        return combinedResult;
    }

}