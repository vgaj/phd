/*
MIT License

Copyright (c) 2022-2025 Viru Gajanayake

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.server.data.TrafficDataStore;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import com.github.vgaj.phd.server.result.AnalysisResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AnalysisTask {
    @Autowired
    AnalysisCache analysisCache;

    @Autowired
    private TrafficDataStore trafficDataStore;

    private MessageInterface messages = Messages.getLogger(this.getClass());

    @Autowired
    private RawDataProcessorInterface analyser;

    @Value("${phd.analysis.interval.ms}")
    private Integer analysisIntervalMs;

    @Scheduled(fixedRateString = "${phd.analysis.interval.ms}", initialDelayString = "${phd.analysis.interval.ms}")
    public void processRawData() {
        // Only process new data
        long epochMinuteReference = EpochMinuteUtil.now() - (long) Math.ceil((double) analysisIntervalMs / 60000);

        trafficDataStore.getAddressesWithDataSince(epochMinuteReference).forEach(address ->
        {
            Optional<AnalysisResult> result = analyser.processRawData(address);
            if (result.isPresent()) {
                analysisCache.putCurrentResult(address, result.get());
            } else {
                analysisCache.removeCurrentResult(address);
            }
        });
    }

}
