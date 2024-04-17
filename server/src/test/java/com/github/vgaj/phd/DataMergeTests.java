/*
MIT License

Copyright (c) 2022-2024 Viru Gajanayake

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

import com.github.vgaj.phd.server.analysis.AnalysisCache;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.result.*;

import com.github.vgaj.phd.server.util.Pair;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.List;

public class DataMergeTests
{
    @Test
    void mergeAnalysisResult() throws UnknownHostException
    {
        // Arrange
        RemoteAddress address1 = new RemoteAddress((byte) 8, (byte) 8, (byte) 8,(byte)  8);
        RemoteAddress address2 = new RemoteAddress((byte) 8, (byte) 8, (byte) 8,(byte)  8);
        address1.lookupHost();
        address2.lookupHost();
        AnalysisResultImpl result1 = ResultXmlSerialisationTest.makeAnalysisResult(1, 11, 2, 22, 5, 55, 6, 666, 888);
        AnalysisResultImpl result2 = ResultXmlSerialisationTest.makeAnalysisResult(1, 10, 3, 10, 5, 11, 7, 777, 999);

        // Act
        AnalysisResult combined = result1.merge(result2);

        // Assert
        assert combined.getLastSeenEpochMinute() == 999;
        List<Pair<TransferIntervalMinutes, TransferCount>> intervals = combined.getRepeatedIntervals();
        List<Pair<TransferSizeBytes, TransferCount>> sizes = combined.getRepeatedTransferSizes();
        assert intervals.get(0).getKey().getInterval() == 1  && intervals.get(0).getValue().getCount() == 21; // 11 + 10
        assert intervals.get(1).getKey().getInterval() == 2  && intervals.get(1).getValue().getCount() == 22;
        assert intervals.get(2).getKey().getInterval() == 3  && intervals.get(2).getValue().getCount() == 10;
        assert sizes.get(0).getKey().getSize() == 5 && sizes.get(0).getValue().getCount() == 66; // 55 + 11
        assert sizes.get(1).getKey().getSize() == 6 && sizes.get(1).getValue().getCount() == 666;
        assert sizes.get(2).getKey().getSize() == 7 && sizes.get(2).getValue().getCount() == 777;
    }


    @Test
    void mergeAddresses()
    {
        // Arrange
        AnalysisCache cache = new AnalysisCache();

        // Previous
        cache.putPreviousResult(new RemoteAddress((byte) 1, (byte) 1, (byte) 1, (byte) 1), new AnalysisResultImpl()); // unique
        cache.putPreviousResult(new RemoteAddress((byte) 8, (byte) 8, (byte) 8, (byte) 8), new AnalysisResultImpl()); // shared
        cache.putPreviousResult(new RemoteAddress((byte) 1, (byte) 0, (byte) 0, (byte) 1), new AnalysisResultImpl()); // unique
        cache.putPreviousResult(new RemoteAddress((byte) 8, (byte) 8, (byte) 4, (byte) 4), new AnalysisResultImpl()); // shared

        // Current
        cache.putCurrentResult(new RemoteAddress((byte) 8, (byte) 8, (byte) 8, (byte) 8), new AnalysisResultImpl()); // shared
        cache.putCurrentResult(new RemoteAddress((byte) 9, (byte) 9, (byte) 9, (byte) 9), new AnalysisResultImpl()); // unique
        cache.putCurrentResult(new RemoteAddress((byte) 8, (byte) 8, (byte) 4, (byte) 4), new AnalysisResultImpl()); // shared

        // Act
        List<RemoteAddress> mergeAddresses = cache.getAddresses();

        // Assert
        assert mergeAddresses.size() == 5;
        assert mergeAddresses.contains(new RemoteAddress((byte) 1, (byte) 1, (byte) 1, (byte) 1));
        assert mergeAddresses.contains(new RemoteAddress((byte) 1, (byte) 0, (byte) 0, (byte) 1));
        assert mergeAddresses.contains(new RemoteAddress((byte) 9, (byte) 9, (byte) 9, (byte) 9));
        assert mergeAddresses.contains(new RemoteAddress((byte) 8, (byte) 8, (byte) 8, (byte) 8));
        assert mergeAddresses.contains(new RemoteAddress((byte) 8, (byte) 8, (byte) 4, (byte) 4));
    }

}
