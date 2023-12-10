package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.result.AnalysisResult;

import java.util.Optional;
import java.util.Set;

public interface AnalyserInterface
{
    Optional<AnalysisResult> analyse(RemoteAddress address);
    Set<RemoteAddress> getAddressesToIgnore();
}
