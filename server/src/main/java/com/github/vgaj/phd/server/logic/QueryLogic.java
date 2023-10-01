package com.github.vgaj.phd.server.logic;

import com.github.vgaj.phd.server.data.DataForAddress;
import com.github.vgaj.phd.server.data.MessageData;
import com.github.vgaj.phd.server.data.MonitorData;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.query.DisplayContent;
import com.github.vgaj.phd.query.DisplayResult;
import com.github.vgaj.phd.query.DisplayResultLine;
import com.github.vgaj.phd.server.result.AnalysisResult;
import com.github.vgaj.phd.server.result.AnalysisScore;
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
    private Analyser analyser;

    @Value("${phm.display.maximum.data}")
    private Integer maxDataToShow;

    /**
    Generate the content for the main page
     */
    public DisplayContent getDisplayContent()
    {
        // TODO: run the logic periodically and just return the last result

        DisplayContent content = new DisplayContent();
        content.results = new ArrayList<>();

        // The data
        ArrayList<Map.Entry<RemoteAddress, DataForAddress>> entries = monitorData.getCopyOfRawData();
        Collections.sort(entries, new Comparator<Map.Entry<RemoteAddress, DataForAddress>>() {
            @Override
            public int compare(Map.Entry<RemoteAddress, DataForAddress> e1, Map.Entry<RemoteAddress, DataForAddress> e2)
            {
                if (e1.getKey() == null || e1.getKey().getReverseHostname() == null || e2.getKey() == null || e2.getKey().getReverseHostname() == null)
                {
                    return 0;
                }
                else
                {
                    return e1.getKey().getReverseHostname().compareTo(e2.getKey().getReverseHostname());
                }
            }
        });

        entries.forEach( entryForAddress ->
        {
            AnalysisResult result = analyser.analyse(entryForAddress.getKey());
            if (result.isMinimalCriteriaMatch())
            {
                DisplayResult displayResult = new DisplayResult();
                content.results.add(displayResult);
                displayResult.hostName = entryForAddress.getKey().getHostString();
                displayResult.ipAddress = entryForAddress.getKey().getAddressString();
                displayResult.totalBytes = entryForAddress.getValue().getTotalBytes();
                displayResult.totalTimes = entryForAddress.getValue().getMinuteBlockCount();

                // TODO: sort by score
                displayResult.score = (new AnalysisScore(result)).getScore();

                if (result.areAllIntervalsTheSame_c11())
                {
                    displayResult.resultLines.add( new DisplayResultLine("all intervals are " + result.getIntervalOfAllTransfers_c11() + " minutes"));
                }

                if (result.areSomeIntervalsTheSame_c12())
                {
                    DisplayResultLine resultLine = new DisplayResultLine("intervals between data:");
                    result.getRepeatedIntervals_c12().forEach(r ->
                            resultLine.subMessages.add(r.getKey() + " min, " + r.getValue() + " times"));
                    displayResult.resultLines.add(resultLine);
                }
                if (result.areAllTransfersTheSameSize_c21())
                {
                    displayResult.resultLines.add( new DisplayResultLine("all transfers are " + result.getSizeOfAllTransfers_c21() + " bytes"));
                }
                if (result.areSomeTransfersTheSameSize_c22())
                {
                    DisplayResultLine resultLine = new DisplayResultLine("repeated data sizes:");
                    result.getRepeatedTransferSizes_c22().forEach(r ->
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
