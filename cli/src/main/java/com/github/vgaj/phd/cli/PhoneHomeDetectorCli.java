package com.github.vgaj.phd.cli;

import com.github.vgaj.phd.ipc.DomainSocketComms;
import com.github.vgaj.phd.query.SummaryResultsQuery;
import com.github.vgaj.phd.query.SummaryResultsResponse;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Optional;

public class PhoneHomeDetectorCli
{
    public static void main(String[] args)
    {
        System.out.println("Phone Home Detector Results");
        // TODO common config
        Path socketPath = Path.of("/tmp", "phone_home_detector_ipc");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);
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

        DomainSocketComms<SummaryResultsResponse, SummaryResultsQuery> sockComms
                = new DomainSocketComms<>(channel);

        Optional<SummaryResultsResponse> response = Optional.empty();
        try
        {
            SummaryResultsQuery request = new SummaryResultsQuery();
            sockComms.writeSocketMessage(request);
            response = sockComms.readSocketMessage();
        }
        catch (IOException e)
        {
            System.out.println("Failed to make request. Error: " + e);
            return;
        }

        if (response.isEmpty())
        {
            System.out.println("Did not receive a response");
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            response.get().getData().results.forEach(r ->
            {
                sb.append(r.ipAddress);
                sb.append(System.lineSeparator());
            });
            System.out.println(sb);
        }

    }
}
