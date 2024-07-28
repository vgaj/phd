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
        String programName = "phone-home-detector";

        Options options = new Options();
        Option optionCurrent = new Option("c", "current", false, "Only show current results (exclude past patterns that are no longer seen)");
        Option optionVerbose = new Option("v", null, false, "Show more verbose information");
        Option optionDebug = new Option("d", null, false, "Show debug info");
        Option optionHelp = new Option("?", "help", false, "View this help");

        Option optionAddress = new Option("a", "address", true, "Show data from the specified IP address");
        // TODO: Option to filter by program
//        Option optionProgram = new Option("p", "program", true, "Only show the specified program");
//        OptionGroup hostOrProgramGroup = new OptionGroup();
//        hostOrProgramGroup.addOption(optionAddress);
//        hostOrProgramGroup.addOption(optionProgram);
//        hostOrProgramGroup.setRequired(false);
//        options.addOptionGroup(hostOrProgramGroup);
        options.addOption(optionAddress);

        options.addOption(optionCurrent);
        options.addOption(optionVerbose);
        options.addOption(optionDebug);
        options.addOption(optionHelp);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
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
            formatter.printHelp(programName, null, options, "use no options to see overall details", true);
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
