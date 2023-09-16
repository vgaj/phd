package com.github.vgaj.phd.cli;

import com.github.vgaj.phd.ipc.DomainSocketComms;
import com.github.vgaj.phd.query.SummaryResultsQuery;
import com.github.vgaj.phd.query.SummaryResultsResponse;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;

public class PhoneHomeDetectorCli
{
    public static void main(String[] args)
    {
        System.out.println("Phone Home Detector Results");
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
            SummaryResultsQuery request = new SummaryResultsQuery();
            sockComms.writeSocketMessage(request);
        }
        catch (IOException e)
        {
            System.out.println("Failed to make request. Error: " + e);
            return;
        }

        SummaryResultsResponse response = null;
        try
        {
            response = (SummaryResultsResponse) sockComms.readSocketMessage(SummaryResultsResponse.class);
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
            StringBuilder sb = new StringBuilder();
            response.getData().results.forEach(r ->
            {
                // TODO more details
                // TODO add query for one address
                sb.append(r.ipAddress);
                sb.append(System.lineSeparator());
            });
            System.out.println(sb);
        }
    }
}
