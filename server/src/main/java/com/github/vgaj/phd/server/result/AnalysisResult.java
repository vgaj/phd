package com.github.vgaj.phd.server.result;

import java.util.List;
import java.util.Map;

public interface AnalysisResult
{
    boolean isMinimalCriteriaMatch();
    boolean areAllIntervalsTheSame_c11();
    boolean areSomeIntervalsTheSame_c12();
    boolean areAllTransfersTheSameSize_c21();
    boolean areSomeTransfersTheSameSize_c22();


    TransferIntervalMinutes getIntervalOfAllTransfers_c11();
    TransferSizeBytes getSizeOfAllTransfers_c21();

    List<Map.Entry<TransferIntervalMinutes, TransferCount>> getRepeatedIntervals_c12();

    List<Map.Entry<TransferSizeBytes, TransferCount>> getRepeatedTransferSizes_c22();
}
