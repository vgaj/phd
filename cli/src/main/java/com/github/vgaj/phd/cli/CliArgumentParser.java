/*
MIT License

Copyright (c) 2022-2025 Viru Gajanayake

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
    public static RequestResponsePair parse(String[] args)
    {
        Options options = new Options();
        Option optionCurrent = new Option("c", "current", false, "Restrict to current results (exclude past patterns that are no longer seen)");
        Option optionVerbose = new Option("v", "verbose", false, "Show verbose information for results");
        Option optionAddress = new Option("h", "history", true, "Show history for the specified IP address pairs, e.g. 192.168.1.2:8.8.8.8");
        Option optionDebug = new Option("l", "log", false, "View tail of debug log");
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
            // -a and -d are not advertised
            //System.out.println(" -h <Source IP address>:<Destination IP address> " + optionAddress.getDescription());
            //System.out.println(" -l              " + optionDebug.getDescription());
            // -s is handled by the bash
            System.out.println(" -s,--setup      Setup hotspot or workstation mode");
            System.out.println(" -?              " + optionHelp.getDescription());
            return null;
        }

        if (cmd.hasOption(optionDebug))
        {
            return new RequestResponsePair( new DebugLogQuery(), DebugLogResponse.class, false, false);
        }

        if (cmd.hasOption(optionAddress))
        {
            String enteredAddress = cmd.getOptionValue(optionAddress);

            // Source and destination are expected to be entered in the following format 192.168.1.2:8.8.8.8
            if (enteredAddress.chars().filter(c -> c == ':').count() != 1) {
                System.out.println(enteredAddress + " is not a source and destination pair in the format 192.168.1.2:8.8.8.8");
                return null;
            }
            String sourceAddress = enteredAddress.substring(0,enteredAddress.indexOf(":"));
            String destinationAddress = enteredAddress.substring(enteredAddress.indexOf(":")+1);

            String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
            if (!sourceAddress.matches(ipv4Pattern) || !destinationAddress.matches(ipv4Pattern)) {
                System.out.println(enteredAddress + " contains an invalid IP address");
                return null;
            }
            InetAddress inetAddressSource;
            InetAddress inetAddressDestination;
            try {
                inetAddressSource = InetAddress.getByName(sourceAddress);
                inetAddressDestination = InetAddress.getByName(destinationAddress);
            }  catch (UnknownHostException e) {
                System.out.println(enteredAddress + " contains an invalid address");
                return null;
            }
            return new RequestResponsePair( new HostHistoryQuery(inetAddressSource, inetAddressDestination), HostHistoryResponse.class, false,false);
        }

        return new RequestResponsePair( new SummaryResultsQuery(), SummaryResultsResponse.class, cmd.hasOption(optionVerbose), cmd.hasOption(optionCurrent));
    }
}
