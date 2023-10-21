package com.github.vgaj.phd.server.result;

import java.util.List;
import java.util.Map;

public interface AnalysisResult
{
    // TODO: Needed?
    boolean isMinimalCriteriaMatch();

    List<Map.Entry<TransferIntervalMinutes, TransferCount>> getRepeatedIntervals();

    List<Map.Entry<TransferSizeBytes, TransferCount>> getRepeatedTransferSizes();
}
