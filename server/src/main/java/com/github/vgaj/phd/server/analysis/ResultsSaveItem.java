package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.result.AnalysisResultImpl;
import lombok.Getter;

public class ResultsSaveItem
{
    public static ResultsSaveItem of(RemoteAddress address, AnalysisResultImpl result, long lastSeenEpochMinute)
    {
        ResultsSaveItem item = new ResultsSaveItem();
        item.address = address;
        item.result = result;
        item.lastSeenEpochMinute = lastSeenEpochMinute;
        return item;
    }
    @Getter
    private RemoteAddress address;

    @Getter
    private AnalysisResultImpl result;

    @Getter
    private long lastSeenEpochMinute;
}
