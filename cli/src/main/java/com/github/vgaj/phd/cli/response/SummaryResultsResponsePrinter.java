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

package com.github.vgaj.phd.cli.response;

import com.github.vgaj.phd.cli.RequestResponsePair;
import com.github.vgaj.phd.common.query.DisplayResult;
import com.github.vgaj.phd.common.query.DisplayResultLine;
import com.github.vgaj.phd.common.query.ResponseInterface;
import com.github.vgaj.phd.common.query.SummaryResultsResponse;
import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.common.util.ExecutableDetails;
import com.github.vgaj.phd.common.properties.HotSpotModeChecker;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SummaryResultsResponsePrinter implements ResponsePrinter {
    private final RequestResponsePair queryDetails;
    private final ResponseInterface response;

    public SummaryResultsResponsePrinter(RequestResponsePair queryDetails, ResponseInterface response) {
        this.queryDetails = queryDetails;
        this.response = response;
    }

    @Override
    public void print() {
        SummaryResultsResponse summaryResponse = (SummaryResultsResponse) response;
        List<DisplayResult> results = Arrays.asList((summaryResponse.data().results()));

        results.sort(new Comparator<DisplayResult>() {
            @Override
            public int compare(DisplayResult e1, DisplayResult e2) {
                if (HotSpotModeChecker.isHotSpot()) {
                    // If running in hotspot mode then we group by source IP
                    if (e1.sourceIpAddress() != null && !e1.sourceIpAddress().equals(e2.sourceIpAddress())) {
                        return e1.sourceIpAddress().compareTo(e2.sourceIpAddress());
                    }
                } else if (e1.probableExecutableDetails() != null
                        && e2.probableExecutableDetails() != null
                        && !e1.probableExecutableDetails().equals(e2.probableExecutableDetails())) {
                    // Otherwise we group all for executable together
                    return e1.probableExecutableDetails().compareTo(e2.probableExecutableDetails());
                }

                // Want reverse order which highest score on top
                return e2.score() - e1.score();
            }
        });
        StringBuilder sb = new StringBuilder();
        boolean showExtraInfo = queryDetails.showExtraDetail();
        boolean onlyShowCurrent = queryDetails.onlyShowCurrent();

        AtomicReference<String> lastSource = new AtomicReference<>();
        AtomicBoolean first = new AtomicBoolean(true);
        results.forEach(r ->
        {
            String sourceDisplayString = "";
            if (HotSpotModeChecker.isHotSpot()) {
                sourceDisplayString = "Source " + r.sourceIpAddress();
                if (showExtraInfo) {
                    sourceDisplayString += " - " + r.sourceAddressExtraDetails();
                }
            } else {
                if (r.probableExecutableDetails() == null || r.probableExecutableDetails().isBlank()) {
                    sourceDisplayString = "Unknown Source";
                } else if (showExtraInfo) {
                    sourceDisplayString = ExecutableDetails.getCommandWithArguments(r.probableExecutableDetails());
                } else {
                    sourceDisplayString = ExecutableDetails.getCommand(r.probableExecutableDetails());
                }
            }

            if (!onlyShowCurrent || r.isCurrent()) {
                if (first.get() || !sourceDisplayString.equals(lastSource.get())) {
                    first.set(false);
                    lastSource.set(sourceDisplayString);
                    sb.append(sourceDisplayString).append(System.lineSeparator());
                }
                sb.append("  ");
                sb.append(r.destinationIpAddress());
                if (!r.destinationIpAddress().equals(r.destinationHostName())) {
                    sb.append(" (").append(r.destinationHostName()).append(")");
                }
                sb.append(System.lineSeparator());
                if (showExtraInfo) {
                    sb.append("    Last Seen: ")
                            .append(EpochMinuteUtil.toString(r.lastSeenEpochMinute()))
                            .append(System.lineSeparator());
                    sb.append("    Score: ")
                            .append(r.score())
                            .append(System.lineSeparator());
                    if (!r.isCurrent()) {
                        sb.append("    Result is not current")
                                .append(System.lineSeparator());
                    }
                }
                for (DisplayResultLine line : r.resultLines()) {
                    String space = showExtraInfo ? "      " : "    ";
                    sb.append(space).append(line.message()).append(System.lineSeparator());
                    if (showExtraInfo) {
                        for (String subline : line.subMessages()) {
                            sb.append("        ").append(subline).append((System.lineSeparator()));
                        }
                    }
                }
            }
        });
        System.out.println(sb);
    }
}
