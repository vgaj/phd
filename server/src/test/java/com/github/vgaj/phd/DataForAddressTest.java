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

import com.github.vgaj.phd.server.data.DataForAddress;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class DataForAddressTest {
    @Test
    public void dataForAddressTest() {
        // Arrange
        DataForAddress data = new DataForAddress();
        data.addBytes(1, 1);
        data.addBytes(2, 1);
        data.addBytes(3, 5);
        data.addBytes(4, 9);

        // Act
        int totalBytes = data.getTotalBytes();
        int blockCount = data.getMinuteBlockCount();
        long latest = data.getLatestEpochMinute();
        List<Map.Entry<Long, Integer>> byteCountPerMinute = data.getPerMinuteData();

        // Assert
        assert totalBytes == 1 + 2 + 3 + 4;
        assert blockCount == 3;
        assert latest == 9;
        assert byteCountPerMinute.size() == 3;
        assert byteCountPerMinute.get(0).getKey() == 1;
        assert byteCountPerMinute.get(0).getValue() == 1 + 2;
        assert byteCountPerMinute.get(1).getKey() == 5;
        assert byteCountPerMinute.get(1).getValue() == 3;
        assert byteCountPerMinute.get(2).getKey() == 9;
        assert byteCountPerMinute.get(2).getValue() == 4;
    }
}
