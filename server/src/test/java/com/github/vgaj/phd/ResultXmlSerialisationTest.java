package com.github.vgaj.phd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.vgaj.phd.server.analysis.ResultsSaveItem;
import com.github.vgaj.phd.server.analysis.ResultsSaveList;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.result.*;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

public class ResultXmlSerialisationTest
{

    private static XmlMapper getXmlMapper()
    {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false); // Prevent Jackson from using a wrapper for empty lists
        XmlMapper xmlMapper = new XmlMapper(xmlModule);
        return xmlMapper;
    }

    private static AnalysisResultImpl makeAnalysisResult(int interval1, int intervalCount1, int interval2, int intervalCount2, int size1, int sizeCount1, int size2, int sizeCount2)
    {
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.setMinimalCriteriaMatch(true);
        result.addRepeatedInterval( TransferIntervalMinutes.of(interval1), TransferCount.of(intervalCount1));
        result.addRepeatedInterval( TransferIntervalMinutes.of(interval2), TransferCount.of(intervalCount2));
        result.addRepeatedTransferSize( TransferSizeBytes.of(size1), TransferCount.of(sizeCount1));
        result.addRepeatedTransferSize( TransferSizeBytes.of(size2), TransferCount.of(sizeCount2));
        return result;
    }

    @Test
    void roundTripAnalysisResult() throws JsonProcessingException
    {
        int interval1 = 1;
        int intervalCount1 = 2;
        int interval2 = 3;
        int intervalCount2 = 4;
        int size1 = 5;
        int sizeCount1 = 6;
        int size2 = 7;
        int sizeCount2 = 8;
        AnalysisResultImpl result = makeAnalysisResult(interval1, intervalCount1, interval2, intervalCount2, size1, sizeCount1, size2, sizeCount2);

        String xml = getXmlMapper().writeValueAsString(result);
        AnalysisResultImpl fromXml = getXmlMapper().readValue(xml, AnalysisResultImpl.class);

        assert fromXml.isMinimalCriteriaMatch() == true;
        assert fromXml.getRepeatedIntervals().size() == 2;
        assert fromXml.getRepeatedTransferSizes().size() == 2;
        assert fromXml.getRepeatedIntervals().get(0).getKey().getInterval() == interval1;
        assert fromXml.getRepeatedIntervals().get(0).getValue().getCount() == intervalCount1;
        assert fromXml.getRepeatedIntervals().get(1).getKey().getInterval() == interval2;
        assert fromXml.getRepeatedIntervals().get(1).getValue().getCount() == intervalCount2;
        assert fromXml.getRepeatedTransferSizes().get(0).getKey().getSize() == size1;
        assert fromXml.getRepeatedTransferSizes().get(0).getValue().getCount() == sizeCount1;
        assert fromXml.getRepeatedTransferSizes().get(1).getKey().getSize() == size2;
        assert fromXml.getRepeatedTransferSizes().get(1).getValue().getCount() == sizeCount2;
    }

    @Test
    void roundTripEmptyAnalysisResult() throws JsonProcessingException
    {
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addRepeatedInterval( TransferIntervalMinutes.of(1), TransferCount.of(2));

        String xml = getXmlMapper().writeValueAsString(result);
        AnalysisResultImpl fromXml = getXmlMapper().readValue(xml, AnalysisResultImpl.class);

        assert fromXml.isMinimalCriteriaMatch() == false;
        assert fromXml.getRepeatedIntervals().size() == 1;
        assert fromXml.getRepeatedIntervals().get(0).getKey().getInterval() == 1;
        assert fromXml.getRepeatedIntervals().get(0).getValue().getCount() == 2;
        assert fromXml.getRepeatedTransferSizes().size() == 0;
    }

    @Test
    void roundTripRemoteAddress() throws UnknownHostException, JsonProcessingException
    {
        RemoteAddress address = new RemoteAddress((byte) 8, (byte) 8, (byte) 8,(byte)  8);
        address.lookupHost();

        String xml = getXmlMapper().writeValueAsString(address);
        RemoteAddress fromXml = getXmlMapper().readValue(xml, RemoteAddress.class);

        assert fromXml.getReverseHostname().equals(address.getReverseHostname());
    }

    @Test
    void roundTripResultsPersistence() throws UnknownHostException, JsonProcessingException
    {
        RemoteAddress address = new RemoteAddress((byte) 8, (byte) 8, (byte) 8,(byte)  8);
        address.lookupHost();
        int interval1 = 1;
        int intervalCount1 = 2;
        int interval2 = 3;
        int intervalCount2 = 4;
        int size1 = 5;
        int sizeCount1 = 6;
        int size2 = 7;
        int sizeCount2 = 8;
        AnalysisResultImpl result = makeAnalysisResult(interval1, intervalCount1, interval2, intervalCount2, size1, sizeCount1, size2, sizeCount2);

        ResultsSaveList results = new ResultsSaveList();
        results.getResultsForSaving().add(ResultsSaveItem.of(address,result,7));

        String xml = getXmlMapper().writeValueAsString(results);
        ResultsSaveList fromXml = getXmlMapper().readValue(xml, ResultsSaveList.class);

        assert fromXml.getResultsForSaving().get(0).getAddress().getReverseHostname().equals(address.getReverseHostname());
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(0).getKey().getInterval() == 1;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(0).getValue().getCount() == 2;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(1).getKey().getInterval() == 3;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(1).getValue().getCount() == 4;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedTransferSizes().get(0).getKey().getSize() == 5;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedTransferSizes().get(0).getValue().getCount() == 6;
        assert fromXml.getResultsForSaving().get(0).getLastSeenEpochMinute() == 7;
    }

    @Test
    void mergeAnalysisResult() throws UnknownHostException
    {
        RemoteAddress address1 = new RemoteAddress((byte) 8, (byte) 8, (byte) 8,(byte)  8);
        RemoteAddress address2 = new RemoteAddress((byte) 8, (byte) 8, (byte) 8,(byte)  8);
        address1.lookupHost();
        address2.lookupHost();

        AnalysisResultImpl result1 = makeAnalysisResult(1, 11, 2, 22, 5, 55, 6, 666);
        AnalysisResultImpl result2 = makeAnalysisResult(1, 10, 3, 10, 5, 11, 7, 777);
        AnalysisResultImpl combined = result1.merge(result2);

        List<Pair<TransferIntervalMinutes, TransferCount>> intervals = combined.getRepeatedIntervals();
        List<Pair<TransferSizeBytes, TransferCount>> sizes = combined.getRepeatedTransferSizes();
        assert intervals.get(0).getKey().getInterval() == 1  && intervals.get(0).getValue().getCount() == 21; // 11 + 10
        assert intervals.get(1).getKey().getInterval() == 2  && intervals.get(1).getValue().getCount() == 22;
        assert intervals.get(2).getKey().getInterval() == 3  && intervals.get(2).getValue().getCount() == 10;
        assert sizes.get(0).getKey().getSize() == 5 && sizes.get(0).getValue().getCount() == 66; // 55 + 11
        assert sizes.get(1).getKey().getSize() == 6 && sizes.get(1).getValue().getCount() == 666;
        assert sizes.get(2).getKey().getSize() == 7 && sizes.get(2).getValue().getCount() == 777;
    }
    
}
