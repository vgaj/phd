package com.github.vgaj.phd.server.result;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class AnalysisResultImpl implements AnalysisResult
{
    /**
     * MINIMAL CRITERIA
     */
    @Getter
    @Setter
    private boolean minimalCriteriaMatch;

    /**
     * TRANSFER INTERVAL - List of: interval in minutes -> number of times
     */
    private List<Pair<TransferIntervalMinutes, TransferCount>> intervals = new ArrayList<>();

    public void addRepeatedInterval(TransferIntervalMinutes intervalMinutes, TransferCount numberOfTimes)
    {
        intervals.add( Pair.of(intervalMinutes, numberOfTimes));
    }

    @Override
    public List<Pair<TransferIntervalMinutes, TransferCount>> getRepeatedIntervals()
    {
        return intervals;
    }

    /**
     * TRANSFER SIZE - List of: sizes of transfers in bytes -> number to times
     */
    private List<Pair<TransferSizeBytes, TransferCount>> dataSizes = new ArrayList<>();

    public void addRepeatedTransferSize(TransferSizeBytes transferSizeBytes, TransferCount numberOfTimes)
    {
        dataSizes.add( Pair.of(transferSizeBytes, numberOfTimes));
    }

    @Override
    public List<Pair<TransferSizeBytes, TransferCount>> getRepeatedTransferSizes()
    {
        return dataSizes;
    }

    public AnalysisResultImpl merge (AnalysisResultImpl other)
    {
        AnalysisResultImpl combinedResult = new AnalysisResultImpl();

        HashMap<TransferIntervalMinutes, TransferCount> combinedIntervals = new HashMap<>();
        this.intervals.forEach(interval -> combinedIntervals.put(interval.getKey(), interval.getValue()));
        other.intervals.forEach(interval -> combinedIntervals.merge(interval.getKey(), interval.getValue(), TransferCount::merge));
        combinedIntervals.forEach((interval,count) -> combinedResult.addRepeatedInterval(interval, count));

        HashMap<TransferSizeBytes, TransferCount> combinedSizes = new HashMap<>();
        this.dataSizes.forEach(size -> combinedSizes.put(size.getKey(), size.getValue()));
        other.dataSizes.forEach(size -> combinedSizes.merge(size.getKey(), size.getValue(), TransferCount::merge));
        combinedSizes.forEach((size,count) -> combinedResult.addRepeatedTransferSize(size, count));

        combinedResult.minimalCriteriaMatch = this.minimalCriteriaMatch || other.minimalCriteriaMatch;

        return combinedResult;
    }

}