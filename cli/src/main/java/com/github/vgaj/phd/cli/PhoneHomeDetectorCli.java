package com.github.vgaj.phd.cli;

import com.github.vgaj.phd.common.ipc.DomainSocketComms;
import com.github.vgaj.phd.common.query.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PhoneHomeDetectorCli
{
    public static void main(String[] args)
    {
        System.out.println("Phone Home Detector Results (use -? for options)");

        boolean fullResults = true;
        InetAddress inetAddress = null;
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
        else if (args.length != 0)
        {
            System.out.println("No options      Shows overall results");
            System.out.println("-h <IP address> Shows history for an address");
            System.out.println("-?              Shows help");
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
            sockComms.writeSocketMessage(fullResults ? new SummaryResultsQuery() : new DetailedResultsQuery(inetAddress));
        }
        catch (IOException e)
        {
            System.out.println("Failed to make request. Error: " + e);
            return;
        }

        Object response = null;
        try
        {
            response = sockComms.readSocketMessage(fullResults ? SummaryResultsResponse.class : DetailedResultsResponse.class);
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
            if (fullResults)
            {
                SummaryResultsResponse summaryResponse = (SummaryResultsResponse) response;
                ArrayList<DisplayResult> results = summaryResponse.getData().results;

                Collections.sort(results, new Comparator<DisplayResult>() {
                    @Override
                    public int compare(DisplayResult e1, DisplayResult e2)
                    {
                        // Want reverse order which highest score on top
                        return e2.score - e1.score;
                    }
                });
                StringBuilder sb = new StringBuilder();
                summaryResponse.getData().results.forEach(r ->
                {
                    sb.append(r.ipAddress);
                    if (!r.ipAddress.equals(r.hostName))
                    {
                        sb.append(" (").append(r.hostName).append(")");
                    }
                    sb.append(System.lineSeparator());
                    sb.append("  Score: ").append(r.score).append(System.lineSeparator());
                    r.resultLines.forEach(line ->
                    {
                        sb.append("    ").append(line.message).append(System.lineSeparator());
                        line.subMessages.forEach(subline ->
                        {
                            sb.append("      ").append(subline).append((System.lineSeparator()));
                        });
                    });
                });
                System.out.println(sb);
            }
            else
            {
                DetailedResultsResponse detailedResponse = (DetailedResultsResponse) response;
                StringBuilder sb = new StringBuilder();
                detailedResponse.results.forEach(r ->
                {
                    sb.append(r).append(System.lineSeparator());
                });
                System.out.println(sb);
            }
        }
  }
}
