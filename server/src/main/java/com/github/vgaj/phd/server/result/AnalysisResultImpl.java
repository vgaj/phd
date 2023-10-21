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
    // TODO why not a map
    private List<Map.Entry<TransferIntervalMinutes, TransferCount>> intervals = new ArrayList<>();
    public void addRepeatedInterval(TransferIntervalMinutes intervalMinutes, TransferCount numberOfTimes)
    {
        intervals.add(Map.entry(intervalMinutes, numberOfTimes));
    }
    @Override
    public List<Map.Entry<TransferIntervalMinutes, TransferCount>> getRepeatedIntervals()
    {
        return intervals;
    }

    /**
     * TRANSFER SIZE - List of: sizes of transfers in bytes -> number to times
     */
    private List<Map.Entry<TransferSizeBytes, TransferCount>> dataSizes = new ArrayList<>();
    public void addRepeatedTransferSize(TransferSizeBytes transferSizeBytes, TransferCount numberOfTimes)
    {
        dataSizes.add(Map.entry(transferSizeBytes, numberOfTimes));
    }
    @Override
    public List<Map.Entry<TransferSizeBytes, TransferCount>> getRepeatedTransferSizes()
    {
        return dataSizes;
    }
}