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

package com.github.vgaj.phd.server.score;

import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.common.util.Pair;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import com.github.vgaj.phd.server.result.AnalysisResult;
import com.github.vgaj.phd.server.result.TransferCount;
import com.github.vgaj.phd.server.result.TransferIntervalMinutes;
import com.github.vgaj.phd.server.result.TransferSizeBytes;
import org.springframework.beans.factory.annotation.Value;

import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.stream.Stream;

public class ResultCategorisationImpl implements ResultCategorisation {
    private final MessageInterface messages = Messages.getLogger(this.getClass());

    /**
     * The minimum number of pairs of transmissions at an interval that is of interest
     */
    @Value("${phd.minimum.count.at.interval}")
    private final Integer minCountAtInterval = 2;

    /**
     * The minimum number of transmissions of the same size that are considered interesting
     */
    @Value("${phd.minimum.count.of.size}")
    private final Integer minCountOfSameSize = 2;

    /**
     * If this percentage of intervals are the same then most are considered to be the same
     */
    @Value("${phd.percent.interval.same.for.most}")
    private final Integer percentageOfIntervalsNeededForMostToBeSame = 80;

    /**
     * If this percentage of sizes are the same then most are considered to be the same
     */
    @Value("${phd.percent.size.same.for.most}")
    private final Integer percentageOfSizesNeededForMostToBeSame = 80;

    private final AnalysisResult result;

    public ResultCategorisationImpl(AnalysisResult result) {
        this.result = result;
    }

    ////////////////////
    /// Interval Rules
    ////////////////////

    @Override
    public boolean areAllIntervalsTheSame_c11() {
        return result.getIntervalCount().size() == 1 &&
                result.getIntervalCount().get(0).getValue().getCount() >= minCountAtInterval;
    }

    @Override
    public boolean areMostIntervalsTheSame_c12() {
        int totalCount = result.getIntervalCount().stream().mapToInt(pair -> pair.getValue().getCount()).sum();
        Optional<Integer> countForMostCommonInterval = getCountForMostCommonInterval();

        if (totalCount > 0 && countForMostCommonInterval.isPresent()) {
            // Check if 80% are the same interval - note 80% is based on observations
            return ((double) countForMostCommonInterval.get() / totalCount) > ((double) percentageOfIntervalsNeededForMostToBeSame / 100);
        }
        return false;
    }

    @Override
    public boolean areSomeIntervalsTheSame_c13() {
        return getRepeatedTransferIntervalsStream().findAny().isPresent();
    }

    private Stream<Pair<TransferIntervalMinutes, TransferCount>> getRepeatedTransferIntervalsStream() {
        return result.getIntervalCount().stream().filter(pair -> pair.getValue().getCount() >= minCountAtInterval);
    }

    /////////////////
    /// Size Rules
    /////////////////

    @Override
    public boolean areAllTransfersTheSameSize_c21() {
        return result.getTransferSizeCount().size() == 1 &&
                result.getTransferSizeCount().get(0).getValue().getCount() >= minCountOfSameSize;
    }

    @Override
    public boolean areMostTransfersTheSameSize_c22() {
        int totalCount = result.getTransferSizeCount().stream().mapToInt(pair -> pair.getValue().getCount()).sum();
        Optional<Integer> countForMostCommonSize = getCountForMostCommonSize();
        if (totalCount > 0 && countForMostCommonSize.isPresent()) {
            // Check if 80% are the same size - note 80% is based on observations
            return ((double) countForMostCommonSize.get() / totalCount) > ((double) percentageOfSizesNeededForMostToBeSame / 100);
        }
        return false;
    }

    @Override
    public boolean areSomeTransfersTheSameSize_c23() {
        return getRepeatedTransferSizeStream().count() > 0;
    }

    private Stream<Pair<TransferSizeBytes, TransferCount>> getRepeatedTransferSizeStream() {
        return result.getTransferSizeCount().stream().filter(pair -> pair.getValue().getCount() >= minCountOfSameSize);
    }

    private Stream<Pair<TransferIntervalMinutes, TransferCount>> getIntervalsSorted() {
        return getRepeatedTransferIntervalsStream()
                .sorted((interval1, interval2) -> {
                    if (interval1.getValue().getCount() == interval2.getValue().getCount()) {
                        // If two frequencies have the same count then return the longest
                        return interval2.getKey().getInterval() - interval1.getKey().getInterval();
                    } else {
                        return interval2.getValue().getCount() - interval1.getValue().getCount();
                    }
                });
    }

    private Stream<Pair<TransferSizeBytes, TransferCount>> getSizesSorted() {
        return getRepeatedTransferSizeStream()
                .sorted((size1, size2) -> {
                    if (size1.getValue().getCount() == size2.getValue().getCount()) {
                        // If two sizes have the same count then return the largest
                        return size2.getKey().getSize() - size1.getKey().getSize();
                    } else {
                        return size2.getValue().getCount() - size1.getValue().getCount();
                    }
                });
    }

    @Override
    public Optional<Integer> getMostCommonInterval() {
        return getIntervalsSorted()
                .map(x -> x.getKey().getInterval())
                .findFirst();
    }

    @Override
    public Optional<Integer> getCountForMostCommonInterval() {
        return getIntervalsSorted()
                .map(x -> x.getValue().getCount())
                .findFirst();
    }

    @Override
    public Optional<Integer> getMostCommonSize() {
        return getSizesSorted()
                .map(x -> x.getKey().getSize())
                .findFirst();
    }

    @Override
    public Optional<Integer> getCountForMostCommonSize() {
        return getSizesSorted()
                .map(x -> x.getValue().getCount())
                .findFirst();
    }

    /**
     * Determines whether the application has been running for long enough that the
     * address for the result should have been seen
     */
    @Override
    public boolean isRuntimeLongEnoughToDecideIfResultIsCurrent() {
        Optional<Integer> mostCommonInterval = getMostCommonInterval();

        long uptimeMinutes = ManagementFactory.getRuntimeMXBean().getUptime() / 60000;

        // After starting we wait for the interval and a buffer to allow for processing
        // delays before we check if a previously identified pattern is current
        // The buffer includes the delay for information from the BPF program to be read,
        // to be processed, and the analysis to be performed
        return (mostCommonInterval.isEmpty() || uptimeMinutes > (mostCommonInterval.get() + 2));
    }

    /**
     * Determines whether the result is current by looking at when it was last seen
     * and the interval it is usually seen at
     */
    @Override
    public boolean isResultCurrent() {
        Optional<Integer> mostCommonInterval = getMostCommonInterval();

        long minutesSinceLastSeen = EpochMinuteUtil.now() - result.getLastSeenEpochMinute();

        return mostCommonInterval.isPresent() && (minutesSinceLastSeen < (mostCommonInterval.get() + 2));
    }

}
