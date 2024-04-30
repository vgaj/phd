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

package com.github.vgaj.phd.cli;

import com.github.vgaj.phd.common.ipc.DomainSocketComms;
import com.github.vgaj.phd.common.query.*;
import com.github.vgaj.phd.common.util.EpochMinuteUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.*;

public class PhoneHomeDetectorCli
{
    public static void main(String[] args)
    {
        System.out.println("Phone Home Detector Results (use -? for options)");

        boolean extraInfo = false;
        boolean fullResults = true;
        InetAddress inetAddress = null;
        boolean debugLog = false;
        if (args.length == 2 && args[0].equals("-h"))
        {
            fullResults = false;
            try
            {
                inetAddress = InetAddress.getByName(args[1]);
            }
            catch (UnknownHostException e)
            {
                System.out.println(args[1] + " is not a valid IP address");;
                return;
            }
        }
        else if (args.length == 1 && args[0].equals("-x"))
        {
            extraInfo = true;
        }
        else if (args.length == 1 && args[0].equals("-d"))
        {
            debugLog = true;
        }
        else if (args.length != 0)
        {
            System.out.println("No options      Overall results");
            System.out.println("-x              Results with extra information");
            System.out.println("-h <IP address> History for an address");
            System.out.println("-d              View rolling debug log");
            System.out.println("-?              View this help");
            return;
        }

        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(DomainSocketComms.SOCKET_PATH);
        SocketChannel channel = null;
        try
        {
            channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            channel.connect(socketAddress);
        }
        catch (IOException e)
        {
            System.out.println("Failed to connect to service. Is it running? Error: " + e);
            return;
        }

        DomainSocketComms sockComms = new DomainSocketComms(channel);
        try
        {
            if (debugLog)
            {
                sockComms.writeSocketMessage(new DebugLogQuery());
            }
            else
            {
                sockComms.writeSocketMessage(fullResults ? new SummaryResultsQuery() : new DetailedResultsQuery(inetAddress));
            }
        }
        catch (IOException e)
        {
            System.out.println("Failed to make request. Error: " + e);
            return;
        }

        Object response = null;
        try
        {
            if (debugLog)
            {
                response = sockComms.readSocketMessage(DebugLogResponse.class);
            }
            else
            {
                response = sockComms.readSocketMessage(fullResults ? SummaryResultsResponse.class : DetailedResultsResponse.class);
            }
        }
        catch (IOException e)
        {
            System.out.println("Failed to read response. Error: " + e);
            return;
        }
        if (response == null)
        {
            System.out.println("Did not receive a response");
        }
        else
        {
            if (debugLog)
            {
                DebugLogResponse logResponse = (DebugLogResponse)  response;
                StringBuilder sb = new StringBuilder();
                Arrays.asList(logResponse.log()).forEach(entry -> {
                    sb.append(entry).append(System.lineSeparator());
                });
                System.out.println(sb);
            }
            else if (fullResults)
            {
                SummaryResultsResponse summaryResponse = (SummaryResultsResponse) response;
                List<DisplayResult> results = Arrays.asList((summaryResponse.data().results()));

                Collections.sort(results, new Comparator<DisplayResult>() {
                    @Override
                    public int compare(DisplayResult e1, DisplayResult e2)
                    {
                        // Want reverse order which highest score on top
                        return e2.score() - e1.score();
                    }
                });
                StringBuilder sb = new StringBuilder();
                boolean showExtraInfo = extraInfo;
                results.forEach(r ->
                {
                    sb.append(r.ipAddress());
                    if (!r.ipAddress().equals(r.hostName()))
                    {
                        sb.append(" (").append(r.hostName()).append(")");
                    }
                    sb.append(System.lineSeparator());
                    if (showExtraInfo)
                    {
                        sb.append("  Last Seen: ");
                        sb.append(EpochMinuteUtil.toString(r.lastSeenEpochMinute()));
                        sb.append(System.lineSeparator());
                        sb.append("  Score: ").append(r.score()).append(System.lineSeparator());
                    }
                    for (DisplayResultLine line : r.resultLines())
                    {
                        String space = showExtraInfo ? "    " : "  ";
                        sb.append(space).append(line.message()).append(System.lineSeparator());
                        if (showExtraInfo)
                        {
                            for (String subline : line.subMessages())
                            {
                                sb.append("      ").append(subline).append((System.lineSeparator()));
                            };
                        }
                    };
                });
                System.out.println(sb);
            }
            else
            {
                DetailedResultsResponse detailedResponse = (DetailedResultsResponse) response;
                StringBuilder sb = new StringBuilder();
                for (String r : detailedResponse.results())
                {
                    sb.append(r).append(System.lineSeparator());
                };
                System.out.println(sb);
            }
        }
  }
}
