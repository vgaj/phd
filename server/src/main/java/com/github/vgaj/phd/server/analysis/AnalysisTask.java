package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.messages.MessageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnalysisTask
{
    @Autowired
    AnalysisCache analysisCache;

    @Autowired
    private MonitorData monitorData;

    @Autowired
    private MessageData messageData;

    @Autowired
    private Analyser analyser;

    @Scheduled(fixedRateString = "${phd.analysis.interval.ms}", initialDelayString = "${phd.analysis.interval.ms}")
    public void analyse()
    {
        monitorData.getAddresses().forEach(address ->
        {
            analysisCache.putCurrentResult(address, analyser.analyse(address));
        });
    }

}
