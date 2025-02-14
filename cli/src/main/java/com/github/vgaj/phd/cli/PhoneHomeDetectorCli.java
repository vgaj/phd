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

import com.github.vgaj.phd.cli.response.ResponsePrinterFactory;
import com.github.vgaj.phd.common.ipc.DomainSocketComms;
import com.github.vgaj.phd.common.query.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Properties;

public class PhoneHomeDetectorCli
{
    public static void main(String[] args)
    {
        RequestResponsePair queryDetails = CliArgumentParser.parse(args);
        if (queryDetails == null)
        {
            return;
        }

        System.out.print("Phone Home Detector Results - version ");
        System.out.print(CliProperties.getVersion());
        System.out.println(" (use -? for options)");

        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(DomainSocketComms.SOCKET_PATH);
        SocketChannel channel = null;
        try
        {
            channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            channel.connect(socketAddress);
        }
        catch (IOException e)
        {
            System.err.println("Failed to connect to service. Is it running? Error: " + e);
            return;
        }

        DomainSocketComms sockComms = new DomainSocketComms(channel);
        try
        {
            sockComms.writeSocketMessage(queryDetails.request());
        }
        catch (IOException e)
        {
            System.err.println("Failed to make request. Error: " + e);
            return;
        }

        ResponseInterface response;
        try
        {
            response = sockComms.readSocketMessage(queryDetails.responseType());
        }
        catch (IOException e)
        {
            System.err.println("Failed to read response. Error: " + e);
            return;
        }
        if (response == null)
        {
            System.err.println("Did not receive a response");
        }
        else
        {
            ResponsePrinterFactory.get(queryDetails, response).print();
        }
  }
}
