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

import com.github.vgaj.phd.common.query.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CliArgumentParser
{
    public static RequestResponseDetails parse(String[] args)
    {
        System.out.println("Phone Home Detector Results (use -? for options)");

        if (args.length == 0)
        {
            return new RequestResponseDetails( new SummaryResultsQuery(), SummaryResultsResponse.class, false, false);
        }
        else if (args.length == 1 && args[0].equals("-c"))
        {
            return new RequestResponseDetails( new SummaryResultsQuery(), SummaryResultsResponse.class, false, true);
        }
        else if (args.length == 1 && args[0].equals("-x"))
        {
            return new RequestResponseDetails( new SummaryResultsQuery(), SummaryResultsResponse.class, true, false);
        }
        else if (args.length == 2 && (args[0].equals("-c") && args[1].equals("-x") || args[0].equals("-x") && args[1].equals("-c")))
        {
            return new RequestResponseDetails( new SummaryResultsQuery(), SummaryResultsResponse.class, true, true);
        }
        else if (args.length == 2 && args[0].equals("-h"))
        {
            InetAddress inetAddress;
            try
            {
                inetAddress = InetAddress.getByName(args[1]);
            }
            catch (UnknownHostException e)
            {
                System.out.println(args[1] + " is not a valid IP address");;
                return null;
            }
            return new RequestResponseDetails( new HostHistoryQuery(inetAddress), HostHistoryResponse.class, false,false);
        }
        else if (args.length == 1 && args[0].equals("-d"))
        {
            return new RequestResponseDetails( new DebugLogQuery(), DebugLogResponse.class, false, false);
        }
        System.out.println("No options      Overall results");
        System.out.println("-c              Only show current results (exclude past patterns that are no longer seen)");
        System.out.println("-x              Results with extra information");
        System.out.println("-h <IP address> History for an address");
        System.out.println("-d              View tail of debug log");
        System.out.println("-?              View this help");
        return null;
    }
}
