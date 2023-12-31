package com.github.vgaj.phd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.vgaj.phd.server.analysis.ResultsSaveItem;
import com.github.vgaj.phd.server.analysis.ResultsSaveList;
import com.github.vgaj.phd.server.analysis.ResultsSaveXmlMapper;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.result.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.UnknownHostException;

public class ResultXmlSerialisationTest
{

    public static AnalysisResultImpl makeAnalysisResult(int interval1, int intervalCount1, int interval2, int intervalCount2, int size1, int sizeCount1, int size2, int sizeCount2, long lastSeen)
    {
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addRepeatedInterval( TransferIntervalMinutes.of(interval1), TransferCount.of(intervalCount1));
        result.addRepeatedInterval( TransferIntervalMinutes.of(interval2), TransferCount.of(intervalCount2));
        result.addRepeatedTransferSize( TransferSizeBytes.of(size1), TransferCount.of(sizeCount1));
        result.addRepeatedTransferSize( TransferSizeBytes.of(size2), TransferCount.of(sizeCount2));
        result.setLastSeenEpochMinute(lastSeen);
        return result;
    }

    @Test
    void roundTripAnalysisResult() throws JsonProcessingException
    {
        // Arrange
        int interval1 = 1;
        int intervalCount1 = 2;
        int interval2 = 3;
        int intervalCount2 = 4;
        int size1 = 5;
        int sizeCount1 = 6;
        int size2 = 7;
        int sizeCount2 = 8;
        long lastSeen = 9;
        AnalysisResultImpl result = makeAnalysisResult(interval1, intervalCount1, interval2, intervalCount2, size1, sizeCount1, size2, sizeCount2, lastSeen);

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(result);
        AnalysisResultImpl fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, AnalysisResultImpl.class);

        // Assert
        assert fromXml.getLastSeenEpochMinute() == lastSeen;
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
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addRepeatedInterval( TransferIntervalMinutes.of(1), TransferCount.of(2));

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(result);
        AnalysisResultImpl fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, AnalysisResultImpl.class);

        // Assert
        assert fromXml.getRepeatedIntervals().size() == 1;
        assert fromXml.getRepeatedIntervals().get(0).getKey().getInterval() == 1;
        assert fromXml.getRepeatedIntervals().get(0).getValue().getCount() == 2;
        assert fromXml.getRepeatedTransferSizes().size() == 0;
    }

    @Test
    void roundTripRemoteAddress() throws UnknownHostException, JsonProcessingException, NoSuchFieldException, IllegalAccessException
    {
        // Arrange
        RemoteAddress address = new RemoteAddress((byte) 8, (byte) 8, (byte) 4,(byte)  4);
        address.lookupHost();

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(address);
        RemoteAddress fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, RemoteAddress.class);

        // Assert
        Field octetsField = RemoteAddress.class.getDeclaredField("octets");
        octetsField.setAccessible(true);
        byte[] octets =  (byte[])octetsField.get(fromXml);
        assert octets[0] == 8;
        assert octets[1] == 8;
        assert octets[2] == 4;
        assert octets[3] == 4;

        Field hostnameField = RemoteAddress.class.getDeclaredField("hostname");
        hostnameField.setAccessible(true);
        String hostname =  (String) hostnameField.get(fromXml);
        assert hostname.equals("dns.google");

        Field lookupAttemptedField = RemoteAddress.class.getDeclaredField("lookupAttempted");
        lookupAttemptedField.setAccessible(true);
        assert (boolean)lookupAttemptedField.get(fromXml);

        assert fromXml.getReverseHostname().equals("google.dns");
    }

    @Test
    void roundTripResultsSaveList() throws UnknownHostException, JsonProcessingException
    {
        // Arrange
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
        long lastSeen = 9;
        AnalysisResultImpl result = makeAnalysisResult(interval1, intervalCount1, interval2, intervalCount2, size1, sizeCount1, size2, sizeCount2, lastSeen);

        ResultsSaveList results = new ResultsSaveList();
        results.getResultsForSaving().add(ResultsSaveItem.of(address,result));

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(results);
        ResultsSaveList fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, ResultsSaveList.class);

        // Assert
        assert fromXml.getResultsForSaving().get(0).getAddress().getReverseHostname().equals(address.getReverseHostname());
        assert fromXml.getResultsForSaving().get(0).getResult().getLastSeenEpochMinute() == lastSeen;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(0).getKey().getInterval() == 1;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(0).getValue().getCount() == 2;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(1).getKey().getInterval() == 3;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedIntervals().get(1).getValue().getCount() == 4;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedTransferSizes().get(0).getKey().getSize() == 5;
        assert fromXml.getResultsForSaving().get(0).getResult().getRepeatedTransferSizes().get(0).getValue().getCount() == 6;
    }
}
