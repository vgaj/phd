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

import org.apache.commons.cli.*;

public class CliArgumentParser
{
    public static RequestResponseDetails parse(String[] args)
    {
        Options options = new Options();
        Option optionCurrent = new Option("c", "current", false, "Restrict to current results (exclude past patterns that are no longer seen)");
        Option optionVerbose = new Option("v", "verbose", false, "Show verbose information for results");
        Option optionAddress = new Option("a", "address", true, "Show history for the specified IP address");
        Option optionDebug = new Option("d", "debug", false, "View tail of debug log");
        Option optionHelp = new Option("?", "help", false, "View this help");

        options.addOption(optionAddress);
        options.addOption(optionCurrent);
        options.addOption(optionVerbose);
        options.addOption(optionDebug);
        options.addOption(optionHelp);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        boolean printHelpAndReturn = false;
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            printHelpAndReturn = true;
        }

        if (printHelpAndReturn || cmd.hasOption(optionHelp))
        {
            //new HelpFormatter().printHelp("phone-home-detector", options, true);
            System.out.println("Usage: phone-home-detector [-c] [-v]");
            System.out.println(" No options      Show overall results");
            System.out.println(" -c,--current    " + optionCurrent.getDescription());
            System.out.println(" -v,--verbose    " + optionVerbose.getDescription());
            // -h and -d are not advertised
            //System.out.println(" -a <IP address> " + optionAddress.getDescription());
            //System.out.println(" -d              " + optionDebug.getDescription());
            System.out.println(" -?              " + optionHelp.getDescription());
            return null;
        }

        if (cmd.hasOption(optionDebug))
        {
            return new RequestResponseDetails( new DebugLogQuery(), DebugLogResponse.class, false, false);
        }

        if (cmd.hasOption(optionAddress))
        {
            InetAddress inetAddress;
            try
            {
                inetAddress = InetAddress.getByName(cmd.getOptionValue(optionAddress));
            }
            catch (UnknownHostException e)
            {
                System.out.println(cmd.getOptionValue(optionAddress) + " is not a valid IP address");
                return null;
            }
            return new RequestResponseDetails( new HostHistoryQuery(inetAddress), HostHistoryResponse.class, false,false);
        }

        return new RequestResponseDetails( new SummaryResultsQuery(), SummaryResultsResponse.class, cmd.hasOption(optionVerbose), cmd.hasOption(optionCurrent));
    }
}
