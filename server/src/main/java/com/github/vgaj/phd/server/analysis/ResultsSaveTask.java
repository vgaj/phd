package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.result.AnalysisResultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.nio.file.Paths;

@Component
public class ResultsSaveTask
{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AnalysisCache analysisCache;

    @Autowired
    private MonitorData monitorData;


    @Value("${phd.results.xml.path}")
    private String xmlFilePath;

    @EventListener(ApplicationReadyEvent.class)
    public void load()
    {
        Path xmlFile = Path.of(xmlFilePath);
        if (Files.exists(xmlFile))
        {
            try
            {
                logger.info("Loading results from XML...");
                String xml = new String(Files.readAllBytes(xmlFile));
                ResultsSaveList fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, ResultsSaveList.class);
                fromXml.getResultsForSaving().forEach(result -> {
                    analysisCache.putPreviousResult(result.getAddress(), result.getResult());
                });
                logger.info("Loaded {} results from XML", fromXml.getResultsForSaving().size());
            }
            catch (Exception e)
            {
                logger.error("Error reading xml results file: " + xmlFilePath, e);
            }
        }
    }

    @EventListener(ContextClosedEvent.class) // Occurs before @PreDestroy
    @Scheduled(fixedDelayString = "${phd.save.interval.ms}", initialDelayString = "${phd.save.interval.ms}")
    public void save()
    {
        logger.info("Saving results to XML...");
        Path xmlFile = Path.of(xmlFilePath);
        ResultsSaveList toXml = new ResultsSaveList();
        analysisCache.getAddresses().forEach(address -> {
            toXml.getResultsForSaving().add(ResultsSaveItem.of(address, (AnalysisResultImpl) analysisCache.getResult(address).get()));
        });
        try
        {
            String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(toXml);
            Files.writeString(xmlFile, xml);
            logger.info("Saved {} results to XML", toXml.getResultsForSaving().size());
        }
        catch (IOException e)
        {
            logger.error("Error writing xml results file: " + xmlFilePath, e);
        }
    }
}