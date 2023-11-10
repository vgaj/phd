package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.result.AnalysisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
            Optional<AnalysisResult> result =analyser.analyse(address);
            if (result.isPresent())
            {
                analysisCache.putCurrentResult(address, result.get());
            }
            else
            {
                analysisCache.removeCurrentResult(address);
            }
        });
    }

}
