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

package com.github.vgaj.phd.ui;

import com.github.vgaj.phd.common.ipc.DomainSocketComms;
import com.github.vgaj.phd.common.query.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
public class PhdUiController
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
    public String index(@RequestParam(name="source", required=false, defaultValue="") String source,
                        @RequestParam(name="destination", required=false, defaultValue="") String destination,
                        Model model)
    {
        try (DomainSocketComms sockComms = makeSocketComms())
        {
            sockComms.writeSocketMessage(new SummaryResultsQuery());
            SummaryResultsResponse response = sockComms.readSocketMessage(SummaryResultsResponse.class);
            if (response == null)
            {
                return "No valid response";
            }
            else
            {
                List<DisplayResultModel> fullResults = new ArrayList<>();
                Arrays.stream(response.data().results()).forEach(result -> {
                    fullResults.add(new DisplayResultModel(result));
                });

                List<String> sourceOptions = new ArrayList<>();
                List<String> destinationOptions = new ArrayList<>();
                sourceOptions.add("");
                destinationOptions.add("");
                fullResults.forEach(result -> {
                    if (!sourceOptions.contains(result.source)) {
                        sourceOptions.add(result.source);
                    }
                    if (!destinationOptions.contains(result.destination)) {
                        destinationOptions.add(result.destination);
                    }
                });
                Collections.sort(sourceOptions);
                Collections.sort(destinationOptions);

                List<DisplayResultModel> results = new ArrayList<>();
                fullResults.forEach(result -> {
                    if ((source.isBlank() || source.equals(result.source)) && (destination.isBlank() || destination.equals(result.destination))) {
                        results.add(result);
                    }
                });

                model.addAttribute("results", results);
                model.addAttribute("sourceOptions", sourceOptions);
                model.addAttribute("destinationOptions", destinationOptions);
                model.addAttribute("selectedSource", source);
                model.addAttribute("selectedDestination", destination);


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
            sockComms.writeSocketMessage(new HostHistoryQuery(inetAddress));
            HostHistoryResponse response = sockComms.readSocketMessage(HostHistoryResponse.class);
            if (response == null)
            {
                return "No valid response";
            }
            else
            {
                model.addAttribute("address", address);
                model.addAttribute("content", response.results());
                return "data";
            }
        }
        catch (Exception e)
        {
            return "Error making query, is the service running?  Error: " + e.getMessage();
        }
    }

}