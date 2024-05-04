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

import com.github.vgaj.phd.server.result.AnalysisResultImpl;
import com.github.vgaj.phd.server.result.ResultCategorisationImpl;
import com.github.vgaj.phd.server.result.TransferCount;
import com.github.vgaj.phd.server.result.TransferIntervalMinutes;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class ResultCategorisationTest
{
    @Test
    public void testGetMostCommonInterval() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addRepeatedInterval(TransferIntervalMinutes.of(1), TransferCount.of(1));
        result.addRepeatedInterval(TransferIntervalMinutes.of(2), TransferCount.of(9));
        result.addRepeatedInterval(TransferIntervalMinutes.of(3), TransferCount.of(5));
        result.addRepeatedInterval(TransferIntervalMinutes.of(4), TransferCount.of(8));
        result.addRepeatedInterval(TransferIntervalMinutes.of(5), TransferCount.of(9));
        result.addRepeatedInterval(TransferIntervalMinutes.of(6), TransferCount.of(1));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Act
        Method method = ResultCategorisationImpl.class.getDeclaredMethod("getMostCommonInterval");
        method.setAccessible(true);
        Optional<Integer> mostCommonInterval = (Optional<Integer>) method.invoke(categorisation);


        // Assert
        assert mostCommonInterval.isPresent();
        assert mostCommonInterval.get() == 5;

    }
}
