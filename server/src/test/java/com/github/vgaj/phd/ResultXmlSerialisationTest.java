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

package com.github.vgaj.phd;

import com.github.vgaj.phd.server.result.*;
import com.github.vgaj.phd.server.address.SourceAndDestinationAddress;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.UnknownHostException;

public class ResultXmlSerialisationTest {

    public static AnalysisResultImpl makeAnalysisResult(int interval1, int intervalCount1, int interval2, int intervalCount2, int size1, int sizeCount1, int size2, int sizeCount2, long lastSeen, String executable) {
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addIntervalCount(TransferIntervalMinutes.of(interval1), TransferCount.of(intervalCount1));
        result.addIntervalCount(TransferIntervalMinutes.of(interval2), TransferCount.of(intervalCount2));
        result.addTransferSizeCount(TransferSizeBytes.of(size1), TransferCount.of(sizeCount1));
        result.addTransferSizeCount(TransferSizeBytes.of(size2), TransferCount.of(sizeCount2));
        result.setLastSeenEpochMinute(lastSeen);
        result.setProbableExecutable(executable);
        return result;
    }

    @Test
    void roundTripAnalysisResult() throws JsonProcessingException {
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
        String executable = "firefox";
        AnalysisResultImpl result = makeAnalysisResult(interval1, intervalCount1, interval2, intervalCount2, size1, sizeCount1, size2, sizeCount2, lastSeen, executable);

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(result);
        AnalysisResultImpl fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, AnalysisResultImpl.class);

        // Assert
        assert fromXml.getLastSeenEpochMinute() == lastSeen;
        assert fromXml.getProbableExecutable().equals(executable);
        assert fromXml.getIntervalCount().size() == 2;
        assert fromXml.getTransferSizeCount().size() == 2;
        assert fromXml.getIntervalCount().get(0).getKey().getInterval() == interval1;
        assert fromXml.getIntervalCount().get(0).getValue().getCount() == intervalCount1;
        assert fromXml.getIntervalCount().get(1).getKey().getInterval() == interval2;
        assert fromXml.getIntervalCount().get(1).getValue().getCount() == intervalCount2;
        assert fromXml.getTransferSizeCount().get(0).getKey().getSize() == size1;
        assert fromXml.getTransferSizeCount().get(0).getValue().getCount() == sizeCount1;
        assert fromXml.getTransferSizeCount().get(1).getKey().getSize() == size2;
        assert fromXml.getTransferSizeCount().get(1).getValue().getCount() == sizeCount2;
    }

    @Test
    void roundTripEmptyAnalysisResult() throws JsonProcessingException {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addIntervalCount(TransferIntervalMinutes.of(1), TransferCount.of(2));

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(result);
        AnalysisResultImpl fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, AnalysisResultImpl.class);

        // Assert
        assert fromXml.getIntervalCount().size() == 1;
        assert fromXml.getIntervalCount().get(0).getKey().getInterval() == 1;
        assert fromXml.getIntervalCount().get(0).getValue().getCount() == 2;
        assert fromXml.getTransferSizeCount().size() == 0;
    }

    @Test
    void roundTripRemoteAddress() throws UnknownHostException, JsonProcessingException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        SourceAndDestinationAddress address = new SourceAndDestinationAddress((byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 8, (byte) 4, (byte) 4);
        address.lookupDestinationHost();

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(address);
        SourceAndDestinationAddress fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, SourceAndDestinationAddress.class);

        // Assert
        Field dstCctetsField = SourceAndDestinationAddress.class.getDeclaredField("dstOctets");
        dstCctetsField.setAccessible(true);
        byte[] dstOctets = (byte[]) dstCctetsField.get(fromXml);
        assert dstOctets[0] == 8;
        assert dstOctets[1] == 8;
        assert dstOctets[2] == 4;
        assert dstOctets[3] == 4;

        Field srcOctetsField = SourceAndDestinationAddress.class.getDeclaredField("srcOctets");
        srcOctetsField.setAccessible(true);
        byte[] srcOctets = (byte[]) srcOctetsField.get(fromXml);
        assert srcOctets[0] == 8;
        assert srcOctets[1] == 8;
        assert srcOctets[2] == 8;
        assert srcOctets[3] == 8;

        Field hostnameField = SourceAndDestinationAddress.class.getDeclaredField("destinationHostname");
        hostnameField.setAccessible(true);
        String hostname = (String) hostnameField.get(fromXml);
        assert hostname.equals("dns.google");

        Field lookupAttemptedField = SourceAndDestinationAddress.class.getDeclaredField("destinationLookupAttempted");
        lookupAttemptedField.setAccessible(true);
        assert (boolean) lookupAttemptedField.get(fromXml);

        assert fromXml.getReverseDesinationHostname().equals("google.dns");
    }

    @Test
    void roundTripResultsSaveList() throws UnknownHostException, JsonProcessingException {
        // Arrange
        SourceAndDestinationAddress address = new SourceAndDestinationAddress((byte) 8, (byte) 8, (byte) 8, (byte) 8);
        address.lookupDestinationHost();
        int interval1 = 1;
        int intervalCount1 = 2;
        int interval2 = 3;
        int intervalCount2 = 4;
        int size1 = 5;
        int sizeCount1 = 6;
        int size2 = 7;
        int sizeCount2 = 8;
        long lastSeen = 9;
        String executable = "firefox";
        AnalysisResultImpl result = makeAnalysisResult(interval1, intervalCount1, interval2, intervalCount2, size1, sizeCount1, size2, sizeCount2, lastSeen, executable);

        ResultsSaveList results = new ResultsSaveList();
        results.getResultsForSaving().add(ResultsSaveItem.of(address, result));

        // Act
        String xml = ResultsSaveXmlMapper.getXmlMapper().writeValueAsString(results);
        ResultsSaveList fromXml = ResultsSaveXmlMapper.getXmlMapper().readValue(xml, ResultsSaveList.class);

        // Assert
        assert fromXml.getResultsForSaving().get(0).getAddress().getReverseDesinationHostname().equals(address.getReverseDesinationHostname());
        assert fromXml.getResultsForSaving().get(0).getResult().getLastSeenEpochMinute() == lastSeen;
        assert fromXml.getResultsForSaving().get(0).getResult().getIntervalCount().get(0).getKey().getInterval() == 1;
        assert fromXml.getResultsForSaving().get(0).getResult().getIntervalCount().get(0).getValue().getCount() == 2;
        assert fromXml.getResultsForSaving().get(0).getResult().getIntervalCount().get(1).getKey().getInterval() == 3;
        assert fromXml.getResultsForSaving().get(0).getResult().getIntervalCount().get(1).getValue().getCount() == 4;
        assert fromXml.getResultsForSaving().get(0).getResult().getTransferSizeCount().get(0).getKey().getSize() == 5;
        assert fromXml.getResultsForSaving().get(0).getResult().getTransferSizeCount().get(0).getValue().getCount() == 6;
        assert fromXml.getResultsForSaving().get(0).getResult().getProbableExecutable().equals(executable);
    }
}
