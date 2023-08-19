package com.github.vgaj.phd.result;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class AnalysisResultImpl implements AnalysisResult
{

    @Getter
    @Setter
    private boolean minimalCriteriaMatch;

    /**
     * c11 - If all intervals are the same then this will be set
     */
    private Optional<TransferIntervalMinutes> c11AllTransfersAtSameInterval = Optional.empty();
    public void setAllTransfersAtSameInterval_c11(TransferIntervalMinutes interval)
    {
        c11AllTransfersAtSameInterval = Optional.of(interval);
    }
    @Override
    public TransferIntervalMinutes getIntervalOfAllTransfers_c11()
    {
        return c11AllTransfersAtSameInterval.get();
    }
    @Override
    public boolean areAllIntervalsTheSame_c11()
    {
        return c11AllTransfersAtSameInterval.isPresent();
    }

    /**
     * c21 - If all transfers are the same size then this will be set
     */
    private Optional<TransferSizeBytes> c21AllDataIsSameSize = Optional.empty();
    public void setAllDataIsSameSize_c21(TransferSizeBytes sizeInBytes)
    {
        c21AllDataIsSameSize = Optional.of(sizeInBytes);
    }
    @Override
    public TransferSizeBytes getSizeOfAllTransfers_c21()
    {
        return c21AllDataIsSameSize.get();
    }
    @Override
    public boolean areAllTransfersTheSameSize_c21()
    {
        return c21AllDataIsSameSize.isPresent();
    }

    /**
     * c12 - List of: interval in minutes -> number of times
     */
    private List<Map.Entry<TransferIntervalMinutes, TransferCount>> c12IntervalsBetweenData = new ArrayList<>();
    public void addIntervalFrequency_c12(TransferIntervalMinutes intervalMinutes, TransferCount numberOfTimes)
    {
        c12IntervalsBetweenData.add(Map.entry(intervalMinutes, numberOfTimes));
    }
    @Override
    public List<Map.Entry<TransferIntervalMinutes, TransferCount>> getRepeatedIntervals_c12()
    {
        return c12IntervalsBetweenData;
    }
    @Override
    public boolean areSomeIntervalsTheSame_c12()
    {
        return c12IntervalsBetweenData.size() > 0;
    }

    /**
     * c22 - List of: sizes of transfers in bytes -> number to times
     */
    private List<Map.Entry<TransferSizeBytes, TransferCount>> c22SomeDataIsSameSizeMessages = new ArrayList<>();
    public void addTransferSizeFrequency_c22(TransferSizeBytes transferSizeBytes, TransferCount numberOfTimes)
    {
        c22SomeDataIsSameSizeMessages.add(Map.entry(transferSizeBytes, numberOfTimes));
    }
    @Override
    public List<Map.Entry<TransferSizeBytes, TransferCount>> getRepeatedTransferSizes_c22()
    {
        return c22SomeDataIsSameSizeMessages;
    }
    @Override
    public boolean areSomeTransfersTheSameSize_c22()
    {
        return c22SomeDataIsSameSizeMessages.size() > 0;
    }

}