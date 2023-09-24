package com.github.vgaj.phd.ui;

import com.github.vgaj.phd.ipc.DomainSocketComms;
import com.github.vgaj.phd.query.DetailedResultsQuery;
import com.github.vgaj.phd.query.DetailedResultsResponse;
import com.github.vgaj.phd.query.SummaryResultsQuery;
import com.github.vgaj.phd.query.SummaryResultsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.InetAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PhdController
{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private DomainSocketComms makeSocketComms() throws IOException
    {
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(DomainSocketComms.SOCKET_PATH);
        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);;
        channel.connect(socketAddress);
        return new DomainSocketComms(channel);
    }

    @GetMapping("/")
    //public String index(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
    public String index(Model model)
    {
        try (DomainSocketComms sockComms = makeSocketComms())
        {
            sockComms.writeSocketMessage(new SummaryResultsQuery());
            SummaryResultsResponse response = (SummaryResultsResponse) sockComms.readSocketMessage(SummaryResultsResponse.class);
            if (response == null)
            {
                return "No valid response";
            }
            else
            {
                model.addAttribute("content", response.getData());
                return "index";
            }
        }
        catch (Exception e)
        {
            return "Error making query, is the service running?  Error: " + e.getMessage();
        }
    }

    @GetMapping("/data")
    public String data(@RequestParam(name="address", required=false, defaultValue="") String address, Model model)
    {
        InetAddress inetAddress = null;
        try
        {
            inetAddress = InetAddress.getByName(address);
        }
        catch (UnknownHostException e)
        {
            return "Invalid IP address";
        }

        try (DomainSocketComms sockComms = makeSocketComms())
        {
            sockComms.writeSocketMessage(new DetailedResultsQuery(inetAddress));
            DetailedResultsResponse response = (DetailedResultsResponse) sockComms.readSocketMessage(DetailedResultsResponse.class);
            if (response == null)
            {
                return "No valid response";
            }
            else
            {
                model.addAttribute("address", address);
                model.addAttribute("content", response.getResults());
                return "data";
            }
        }
        catch (Exception e)
        {
            return "Error making query, is the service running?  Error: " + e.getMessage();
        }
    }

}