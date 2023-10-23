package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.result.AnalysisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class AnalysisTask
{
    private final ConcurrentMap<RemoteAddress, AnalysisResult> resultsCache = new ConcurrentHashMap<>();

    @Autowired
    private MonitorData monitorData;

    @Autowired
    private MessageData messageData;

    @Autowired
    private Analyser analyser;

    @Scheduled(fixedRateString = "${phd.analysis.interval.ms}")
    public void analyse()
    {
        monitorData.getAddresses().forEach(address ->
        {
            resultsCache.put(address, analyser.analyse(address));
        });
    }

    public Optional<AnalysisResult> getResult( RemoteAddress address)
    {
        return Optional.ofNullable(resultsCache.get(address));
    }
}
