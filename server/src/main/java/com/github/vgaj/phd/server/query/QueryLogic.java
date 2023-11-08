package com.github.vgaj.phd.server.query;

import com.github.vgaj.phd.server.analysis.AnalysisCache;
import com.github.vgaj.phd.server.data.DataForAddress;
import com.github.vgaj.phd.server.messages.MessageData;
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
    @Autowired
    private MonitorData monitorData;

    @Autowired
    private MessageData messageData;

    @Autowired
    private AnalysisCache analyserCache;

    @Value("${phd.display.maximum.data}")
    private Integer maxDataToShow;

    /**
    Generate the content for the main page
     */
    public DisplayContent getDisplayContent()
    {
        DisplayContent content = new DisplayContent();
        content.results = new ArrayList<>();

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
            if (resultFromCache.isPresent() && resultFromCache.get().isMinimalCriteriaMatch())
            {
                AnalysisResult result = resultFromCache.get();
                ResultCategorisation resultCategorisation = new ResultCategorisationImpl(result);

                DisplayResult displayResult = new DisplayResult();
                content.results.add(displayResult);
                displayResult.hostName = address.getHostString();
                displayResult.ipAddress = address.getAddressString();
                DataForAddress currentDataForAddress = monitorData.getDataForAddress(address);
                if (currentDataForAddress != null)
                {
                    displayResult.totalBytes = currentDataForAddress.getTotalBytes();
                    displayResult.totalTimes = currentDataForAddress.getMinuteBlockCount();
                }

                // TODO: sort by score
                displayResult.score = (new AnalysisScore(resultCategorisation)).getScore();

                if (resultCategorisation.areAllIntervalsTheSame_c11())
                {
                    displayResult.resultLines.add( new DisplayResultLine("all intervals are " + result.getRepeatedIntervals().get(0).getKey() + " minutes"));
                }

                if (resultCategorisation.areSomeIntervalsTheSame_c12())
                {
                    DisplayResultLine resultLine = new DisplayResultLine("intervals between data:");
                    result.getRepeatedIntervals().forEach(r ->
                            resultLine.subMessages.add(r.getKey() + " min, " + r.getValue() + " times"));
                    displayResult.resultLines.add(resultLine);
                }
                if (resultCategorisation.areAllTransfersTheSameSize_c21())
                {
                    displayResult.resultLines.add( new DisplayResultLine("all transfers are " + result.getRepeatedTransferSizes().get(0).getKey() + " bytes"));
                }
                if (resultCategorisation.areSomeTransfersTheSameSize_c22())
                {
                    DisplayResultLine resultLine = new DisplayResultLine("repeated data sizes:");
                    result.getRepeatedTransferSizes().forEach(r ->
                            resultLine.subMessages.add(r.getKey() + " bytes, " + r.getValue() + " times"));
                    displayResult.resultLines.add( resultLine);
                }
                if (maxDataToShow > 0)
                {
                    //sb.append("- last ").append(maxDataToShow).append(" data points: ").append("<br/>");
                    //sb.append(entryForAddress.getValue().getPerMinuteDataForDisplay(maxDataToShow));
                }
            }
        });

        // The messages
        content.messages = new ArrayList<>();
        content.messages.addAll(messageData.getMessages());

        return content;
    }

    /**
     * Generate the data for a given address
     */
    public ArrayList<String> getData(InetAddress address)
    {
        ArrayList<String> results = new ArrayList<>();
        results.addAll(monitorData.getDataForAddress(new RemoteAddress(address)).getPerMinuteDataForDisplay(maxDataToShow));
        return results;
    }
}
