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

package com.github.vgaj.phd.common.query;

import com.github.vgaj.phd.common.properties.HotSpotModeChecker;
import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.common.util.ExecutableDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The result for use by Thymeleaf rendering
 */
public class DisplayResultModel
{
    public String source;
    public String destination;
    public String sourceIp;
    public String destinationIp;
    public String lastSeen;
    public String isCurrent;
    public String score;
    public List<String> details = new ArrayList<>();
    public DisplayResultModel(DisplayResult result) {
        if (new HotSpotModeChecker().isHotSpot()){
            source = result.sourceIpAddress();
        } else {
            source = !result.probableExecutableDetails().isBlank() ? ExecutableDetails.getCommand(result.probableExecutableDetails()) : "Unknown Source";
        }
        destination = result.destinationIpAddress();
        if (!result.destinationIpAddress().equals(result.destinationHostName())) {
            destination += " (" + result.destinationHostName() + ")";
        }
        sourceIp = result.sourceIpAddress();
        destinationIp = result.destinationIpAddress();
        lastSeen = EpochMinuteUtil.toString(result.lastSeenEpochMinute());
        isCurrent = result.isCurrent() ? "Yes" : "No";
        score = String.valueOf(result.score());
        Arrays.stream(result.resultLines()).forEach(line -> {
            details.add(line.message());
            Arrays.stream(line.subMessages()).forEach(subMessage -> {
                details.add("- "+subMessage);
            });
        });
    }
}
