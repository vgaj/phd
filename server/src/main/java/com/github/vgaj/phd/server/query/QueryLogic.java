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

package com.github.vgaj.phd.server.query;

import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.server.analysis.AnalysisCache;
import com.github.vgaj.phd.server.data.DataForAddress;
import com.github.vgaj.phd.server.messages.Messages;
import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.common.query.DisplayContent;
import com.github.vgaj.phd.common.query.DisplayResult;
import com.github.vgaj.phd.common.query.DisplayResultLine;
import com.github.vgaj.phd.server.result.AnalysisResult;
import com.github.vgaj.phd.server.result.AnalysisScore;
import com.github.vgaj.phd.server.result.ResultCategorisation;
import com.github.vgaj.phd.server.result.ResultCategorisationImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.*;

/**
 * This class is responsible for generating the model of the result of the analysis
 * which can be sent to the user interface applications (web or cli) where it
 * is formatted to display to the user
 */
@Component
public class QueryLogic
{
    // TODO: Add unit tests

    @Autowired
    private MonitorData monitorData;

    @Autowired
    private Messages messages;

    @Autowired
    private AnalysisCache analyserCache;

    @Value("${phd.display.maximum.data}")
    private Integer maxDataToShow;

    /**
    Generate the content for the main page
     */
    public DisplayContent getDisplayContent()
    {
        ArrayList<DisplayResult> results = new ArrayList<>();

        // The addresses
        List<RemoteAddress> addresses = analyserCache.getAddresses();

        Collections.sort(addresses, new Comparator<RemoteAddress>() {
            @Override
            public int compare(RemoteAddress e1, RemoteAddress e2)
            {
                if (e1 == null || e1.getReverseHostname() == null || e2 == null || e2.getReverseHostname() == null)
                {
                    return 0;
                }
                else
                {
                    return e1.getReverseHostname().compareTo(e2.getReverseHostname());
                }
            }
        });

        addresses.forEach( address ->
        {
            Optional<AnalysisResult> resultFromCache = analyserCache.getResult(address);
            if (resultFromCache.isPresent())
            {
                AnalysisResult result = resultFromCache.get();
                ResultCategorisation resultCategorisation = new ResultCategorisationImpl(result);

                int totalBytes = 0;
                int totalTimes = 0;
                DataForAddress currentDataForAddress = monitorData.getDataForAddress(address);
                if (currentDataForAddress != null)
                {
                    totalBytes = currentDataForAddress.getTotalBytes();
                    totalTimes = currentDataForAddress.getMinuteBlockCount();
                }

                int score = (new AnalysisScore(resultCategorisation)).getScore();
                ArrayList<DisplayResultLine> resultLines = new ArrayList<>();
                if (resultCategorisation.areAllIntervalsTheSame_c11())
                {
                    resultLines.add( new DisplayResultLine("all intervals are " + result.getRepeatedIntervals().get(0).getKey() + " minutes", new String[0]));
                }
                else if (resultCategorisation.areSomeIntervalsTheSame_c12())
                {
                    ArrayList<String> subMessages = new ArrayList<>();
                    result.getRepeatedIntervals().forEach(r ->
                            subMessages.add(r.getKey() + " min, " + r.getValue() + " times"));
                    DisplayResultLine resultLine = new DisplayResultLine("some intervals are the same", subMessages.toArray(new String[0]));
                    resultLines.add(resultLine);
                }
                if (resultCategorisation.areAllTransfersTheSameSize_c21())
                {
                    resultLines.add( new DisplayResultLine("all transfers are " + result.getRepeatedTransferSizes().get(0).getKey() + " bytes", new String[0]));
                }
                else if (resultCategorisation.areSomeTransfersTheSameSize_c22())
                {
                    ArrayList<String> subMessages = new ArrayList<>();
                    result.getRepeatedTransferSizes().forEach(r ->
                            subMessages.add(r.getKey() + " bytes, " + r.getValue() + " times"));
                    DisplayResultLine resultLine = new DisplayResultLine("some data sizes are repeated", subMessages.toArray(new String[0]));
                    resultLines.add( resultLine);
                }
                if (maxDataToShow > 0)
                {
                    //sb.append("- last ").append(maxDataToShow).append(" data points: ").append("<br/>");
                    //sb.append(entryForAddress.getValue().getPerMinuteDataForDisplay(maxDataToShow));
                }

                if (score > 0)
                {
                    DisplayResult displayResult = new DisplayResult(
                            address.getHostString(),
                            address.getAddressString(),
                            totalBytes,
                            totalTimes,
                            score,
                            result.getLastSeenEpochMinute(),
                            resultLines.toArray(new DisplayResultLine[0]));
                    results.add(displayResult);
                }

            }
        });

        // The messages
        ArrayList<String> messages = new ArrayList<>();
        messages.addAll(this.messages.getMessages());

        return new DisplayContent(results.toArray(new DisplayResult[0]), messages.toArray(new String[0]));
    }

    /**
     * Generate the data for a given address
     */
    public ArrayList<String> getData(InetAddress address)
    {
        ArrayList<String> results = new ArrayList<>();
        DataForAddress dataForAddress = monitorData.getDataForAddress(new RemoteAddress(address));
        if (dataForAddress != null)
        {
            var data = dataForAddress.getByteCountPerMinute().entrySet();
            int dataLength = data.size();
            data.stream()
                    .sorted(Comparator.comparing(e -> ((Long) e.getKey())))
                    .skip( maxDataToShow < dataLength ? dataLength - maxDataToShow : 0)
                    .limit( maxDataToShow)
                    .map(e -> EpochMinuteUtil.toString(e.getKey()) + " : " + e.getValue() + " bytes")
                    .forEach(results::add);
        }
        return results;
    }
}
