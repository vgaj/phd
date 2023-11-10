package com.github.vgaj.phd.server.result;

import java.util.List;
import java.util.Map;

public interface AnalysisResult
{
    long getLastSeenEpochMinute();

    List<Pair<TransferIntervalMinutes, TransferCount>> getRepeatedIntervals();

    List<Pair<TransferSizeBytes, TransferCount>> getRepeatedTransferSizes();

    AnalysisResult merge (AnalysisResult other);
}
