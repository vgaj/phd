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

package com.github.vgaj.phd.server.result;

import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.common.util.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Comparator;
import java.util.Optional;

public class ResultCategorisationImpl implements ResultCategorisation
{
    private AnalysisResult result;
    public ResultCategorisationImpl(AnalysisResult result)
    {
        this.result = result;
    }

    @Override
    public boolean areAllIntervalsTheSame_c11()
    {
        return result.getRepeatedIntervals().size() == 1;
    }

    @Override
    public boolean areSomeIntervalsTheSame_c12()
    {
        return result.getRepeatedIntervals().size() > 0;
    }

    @Override
    public boolean areAllTransfersTheSameSize_c21()
    {
        return result.getRepeatedTransferSizes().size() == 1;
    }
    @Override
    public boolean areSomeTransfersTheSameSize_c22()
    {
        return result.getRepeatedTransferSizes().size() > 0;
    }

    private Optional<Integer> getMostCommonInterval()
    {
        return result.getRepeatedIntervals().stream()
                .sorted((interval1,interval2) -> {
                    if (interval1.getValue().getCount() == interval2.getValue().getCount())
                    {
                        // If a number of frequencies have the same count then return the longest
                        return interval2.getKey().getInterval() - interval1.getKey().getInterval();
                    }
                    else
                    {
                        return interval2.getValue().getCount() - interval1.getValue().getCount();
                    }
                        } )
                .map(x -> x.getKey().getInterval())
                .findFirst();
    }

    /**
     * Determines whether the application has been running for long enough that the
     * address for the result should have been seen
     */
    @Override
    public boolean isRuntimeLongEnoughToDecideIfResultIsCurrent()
    {
        Optional<Integer> mostCommonInterval = getMostCommonInterval();

        long uptimeMinutes = ManagementFactory.getRuntimeMXBean().getUptime() / 60000;

        // After starting we wait for the interval and a buffer to allow for processing
        // delays before we check if a previously identified pattern is current
        // The buffer includes the delay for information from the BPF program to be read,
        // to be processed, and the analysis to be performed
        return  (mostCommonInterval.isEmpty() || uptimeMinutes > (mostCommonInterval.get() + 2));
    }

    /**
     * Determines whether the result is current by looking at when it was last seen
     * and the interval it is usually seen at
     */
    @Override
    public boolean isResultCurrent()
    {
        Optional<Integer> mostCommonInterval = getMostCommonInterval();

        long minutesSinceLastSeen = EpochMinuteUtil.now() - result.getLastSeenEpochMinute();

        return mostCommonInterval.isPresent() && (minutesSinceLastSeen < (mostCommonInterval.get() + 2));
    }

}
