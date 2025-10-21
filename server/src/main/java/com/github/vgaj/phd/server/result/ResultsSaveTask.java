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

package com.github.vgaj.phd.server.result;

import com.github.vgaj.phd.server.analysis.AnalysisCache;
import com.github.vgaj.phd.server.store.TrafficDataStore;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ResultsSaveTask {
    private MessageInterface messages = Messages.getLogger(this.getClass());

    @Autowired
    AnalysisCache analysisCache;

    @Autowired
    private TrafficDataStore trafficDataStore;


    @Value("${phd.results.xml.path}")
    private String xmlFilePath;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        Path xmlFile = Path.of(xmlFilePath);
        if (Files.exists(xmlFile)) {
            try {
                messages.addMessage("Loading results from XML...");
                String xml = new String(Files.readAllBytes(xmlFile));
                ResultsSaveList fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, ResultsSaveList.class);
                fromXml.getResultsForSaving().forEach(result -> {
                    analysisCache.putPreviousResult(result.getAddress(), result.getResult());
                });
                messages.addMessage("Loaded " + fromXml.getResultsForSaving().size() + " results from XML");
            } catch (Exception e) {
                messages.addError("Error reading xml results file: " + xmlFilePath, e);
            }
        }
    }

    @EventListener(ContextClosedEvent.class) // Occurs before @PreDestroy
    @Scheduled(fixedDelayString = "${phd.save.interval.ms}", initialDelayString = "${phd.save.interval.ms}")
    public void save() {
        messages.addMessage("Saving results to XML...");
        Path xmlFile = Path.of(xmlFilePath);
        ResultsSaveList toXml = new ResultsSaveList();
        analysisCache.getAddresses().forEach(address -> {
            toXml.getResultsForSaving().add(ResultsSaveItem.of(address, (AnalysisResultImpl) analysisCache.getResult(address).get()));
        });
        try {
            String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(toXml);
            Files.writeString(xmlFile, xml);
            messages.addMessage("Saved " + toXml.getResultsForSaving().size() + " results to XML");
        } catch (IOException e) {
            messages.addError("Error writing xml results file: " + xmlFilePath, e);
        }
    }
}