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

package com.github.vgaj.phd.cli.response;

import com.github.vgaj.phd.cli.RequestResponseDetails;
import com.github.vgaj.phd.common.query.DisplayResult;
import com.github.vgaj.phd.common.query.DisplayResultLine;
import com.github.vgaj.phd.common.query.ResponseInterface;
import com.github.vgaj.phd.common.query.SummaryResultsResponse;
import com.github.vgaj.phd.common.util.EpochMinuteUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SummaryResultsResponsePrinter implements ResponsePrinter
{
    private RequestResponseDetails queryDetails;
    private ResponseInterface response;
    public SummaryResultsResponsePrinter(RequestResponseDetails queryDetails, ResponseInterface response)
    {
        this.queryDetails = queryDetails;
        this.response = response;
    }
    @Override
    public void print()
    {
        SummaryResultsResponse summaryResponse = (SummaryResultsResponse) response;
        List<DisplayResult> results = Arrays.asList((summaryResponse.data().results()));

        Collections.sort(results, new Comparator<DisplayResult>() {
            @Override
            public int compare(DisplayResult e1, DisplayResult e2)
            {
                // Group all for executable together
                if (e1.probableExecutable() != null
                        && e2.probableExecutable() != null
                        && !e1.probableExecutable().equals(e2.probableExecutable()))
                {
                    return e1.probableExecutable().compareTo(e2.probableExecutable());
                }

                // Want reverse order which highest score on top
                return e2.score() - e1.score();
            }
        });
        StringBuilder sb = new StringBuilder();
        boolean showExtraInfo = queryDetails.showExtraDetail();
        boolean onlyShowCurrent = queryDetails.onlyShowCurrent();

        AtomicReference<String> lastExe = new AtomicReference<>();
        AtomicBoolean first = new AtomicBoolean(true);
        results.forEach(r ->
        {
            if (showExtraInfo || first.get() || r.probableExecutable() != null && !r.probableExecutable().equals(lastExe.get()))
            {
                sb.append(r.probableExecutable() != null && !r.probableExecutable().isBlank() ? r.probableExecutable() : "Unknown Source");
                sb.append(System.lineSeparator());
            }
            lastExe.set(r.probableExecutable());
            first.set(false);
            if (!onlyShowCurrent || r.isCurrent())
            {
                sb.append("  ");
                sb.append(r.ipAddress());
                if (!r.ipAddress().equals(r.hostName()))
                {
                    sb.append(" (").append(r.hostName()).append(")");
                }
                sb.append(System.lineSeparator());
                if (showExtraInfo)
                {
                    sb.append("    Last Seen: ")
                            .append(EpochMinuteUtil.toString(r.lastSeenEpochMinute()))
                            .append(System.lineSeparator());
                    sb.append("    Score: ")
                            .append(r.score())
                            .append(System.lineSeparator());
                    if (!r.isCurrent())
                    {
                        sb.append("    Result is not current")
                                .append(System.lineSeparator());
                    }
                }
                for (DisplayResultLine line : r.resultLines())
                {
                    String space = showExtraInfo ? "      " : "    ";
                    sb.append(space).append(line.message()).append(System.lineSeparator());
                    if (showExtraInfo)
                    {
                        for (String subline : line.subMessages())
                        {
                            sb.append("        ").append(subline).append((System.lineSeparator()));
                        }
                    }
                }
            }
        });
        System.out.println(sb);
    }
}
