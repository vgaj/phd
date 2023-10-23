package com.github.vgaj.phd.server.result;

import java.util.List;
import java.util.Map;

public interface AnalysisResult
{
    // TODO: Needed?
    boolean isMinimalCriteriaMatch();

    List<Pair<TransferIntervalMinutes, TransferCount>> getRepeatedIntervals();

    List<Pair<TransferSizeBytes, TransferCount>> getRepeatedTransferSizes();
}
