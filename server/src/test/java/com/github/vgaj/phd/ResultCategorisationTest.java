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

import com.github.vgaj.phd.server.result.*;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class ResultCategorisationTest
{
    private static ResultCategorisationImpl setupForMostCommonTests()
    {
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addIntervalCount(TransferIntervalMinutes.of(1), TransferCount.of(1));
        result.addIntervalCount(TransferIntervalMinutes.of(2), TransferCount.of(9));
        result.addIntervalCount(TransferIntervalMinutes.of(3), TransferCount.of(5));
        result.addIntervalCount(TransferIntervalMinutes.of(4), TransferCount.of(8));
        result.addIntervalCount(TransferIntervalMinutes.of(5), TransferCount.of(9));
        result.addIntervalCount(TransferIntervalMinutes.of(6), TransferCount.of(1));
        result.addTransferSizeCount(TransferSizeBytes.of(10), TransferCount.of(10));
        result.addTransferSizeCount(TransferSizeBytes.of(20), TransferCount.of(90));
        result.addTransferSizeCount(TransferSizeBytes.of(30), TransferCount.of(50));
        result.addTransferSizeCount(TransferSizeBytes.of(40), TransferCount.of(80));
        result.addTransferSizeCount(TransferSizeBytes.of(50), TransferCount.of(90));
        result.addTransferSizeCount(TransferSizeBytes.of(60), TransferCount.of(10));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);
        return categorisation;
    }

    @Test
    public void testGetMostCommonInterval() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        // Arrange
        ResultCategorisationImpl categorisation = setupForMostCommonTests();

        // Act
        Method method = ResultCategorisationImpl.class.getDeclaredMethod("getMostCommonInterval");
        method.setAccessible(true);
        Optional<Integer> mostCommonInterval = (Optional<Integer>) method.invoke(categorisation);

        // Assert
        assert mostCommonInterval.isPresent();
        assert mostCommonInterval.get() == 5;
    }

    @Test
    public void testGetCountForMostCommonInterval() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        // Arrange
        ResultCategorisationImpl categorisation = setupForMostCommonTests();

        // Act
        Method method = ResultCategorisationImpl.class.getDeclaredMethod("getCountForMostCommonInterval");
        method.setAccessible(true);
        Optional<Integer> countForMostCommonInterval = (Optional<Integer>) method.invoke(categorisation);

        // Assert
        assert countForMostCommonInterval.isPresent();
        assert countForMostCommonInterval.get() == 9;
    }

    @Test
    public void testGetMostCommonSize() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        // Arrange
        ResultCategorisationImpl categorisation = setupForMostCommonTests();

        // Act
        Method method = ResultCategorisationImpl.class.getDeclaredMethod("getMostCommonSize");
        method.setAccessible(true);
        Optional<Integer> mostCommonSize = (Optional<Integer>) method.invoke(categorisation);

        // Assert
        assert mostCommonSize.isPresent();
        assert mostCommonSize.get() == 50;
    }

    @Test
    public void testGetCountForMostCommonSize() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        // Arrange
        ResultCategorisationImpl categorisation = setupForMostCommonTests();

        // Act
        Method method = ResultCategorisationImpl.class.getDeclaredMethod("getCountForMostCommonSize");
        method.setAccessible(true);
        Optional<Integer> countForMostCommonSize = (Optional<Integer>) method.invoke(categorisation);

        // Assert
        assert countForMostCommonSize.isPresent();
        assert countForMostCommonSize.get() == 90;
    }

    @Test
    public void testAllIntervalsTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addIntervalCount(TransferIntervalMinutes.of(10), TransferCount.of(2));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert categorisation.areAllIntervalsTheSame_c11();
        assert categorisation.areMostIntervalsTheSame_c12();
        assert categorisation.areSomeIntervalsTheSame_c13();
    }

    @Test
    public void testMostIntervalsTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addIntervalCount(TransferIntervalMinutes.of(3), TransferCount.of(9));
        result.addIntervalCount(TransferIntervalMinutes.of(4), TransferCount.of(10));
        result.addIntervalCount(TransferIntervalMinutes.of(5), TransferCount.of(80));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert !categorisation.areAllIntervalsTheSame_c11();
        assert categorisation.areMostIntervalsTheSame_c12();
        assert categorisation.areSomeIntervalsTheSame_c13();
    }

    @Test
    public void testSomeIntervalsTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addIntervalCount(TransferIntervalMinutes.of(1), TransferCount.of(1));
        result.addIntervalCount(TransferIntervalMinutes.of(2), TransferCount.of(2));
        result.addIntervalCount(TransferIntervalMinutes.of(3), TransferCount.of(3));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert !categorisation.areAllIntervalsTheSame_c11();
        assert !categorisation.areMostIntervalsTheSame_c12();
        assert categorisation.areSomeIntervalsTheSame_c13();
    }

    @Test
    public void testNoIntervalsTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addIntervalCount(TransferIntervalMinutes.of(10), TransferCount.of(1));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert !categorisation.areAllIntervalsTheSame_c11();
        assert !categorisation.areMostIntervalsTheSame_c12();
        assert !categorisation.areSomeIntervalsTheSame_c13();
    }

    @Test
    public void testAllSizesTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addTransferSizeCount(TransferSizeBytes.of(10), TransferCount.of(2));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert categorisation.areAllTransfersTheSameSize_c21();
        assert categorisation.areMostTransfersTheSameSize_c22();
        assert categorisation.areSomeTransfersTheSameSize_c23();
    }

    @Test
    public void testMostSizesTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addTransferSizeCount(TransferSizeBytes.of(3), TransferCount.of(9));
        result.addTransferSizeCount(TransferSizeBytes.of(4), TransferCount.of(10));
        result.addTransferSizeCount(TransferSizeBytes.of(5), TransferCount.of(80));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert !categorisation.areAllTransfersTheSameSize_c21();
        assert categorisation.areMostTransfersTheSameSize_c22();
        assert categorisation.areSomeTransfersTheSameSize_c23();
    }

    @Test
    public void testSomeSizesTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addTransferSizeCount(TransferSizeBytes.of(1), TransferCount.of(1));
        result.addTransferSizeCount(TransferSizeBytes.of(2), TransferCount.of(2));
        result.addTransferSizeCount(TransferSizeBytes.of(3), TransferCount.of(3));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert !categorisation.areAllTransfersTheSameSize_c21();
        assert !categorisation.areMostTransfersTheSameSize_c22();
        assert categorisation.areSomeTransfersTheSameSize_c23();
    }

    @Test
    public void testNoSizesTheSame()
    {
        // Arrange
        AnalysisResultImpl result = new AnalysisResultImpl();
        result.addTransferSizeCount(TransferSizeBytes.of(10), TransferCount.of(1));
        ResultCategorisationImpl categorisation = new ResultCategorisationImpl(result);

        // Assert
        assert !categorisation.areAllTransfersTheSameSize_c21();
        assert !categorisation.areMostTransfersTheSameSize_c22();
        assert !categorisation.areSomeTransfersTheSameSize_c23();
    }
}
