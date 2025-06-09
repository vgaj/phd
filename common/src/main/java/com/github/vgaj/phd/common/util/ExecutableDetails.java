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

package com.github.vgaj.phd.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Logic to store the details of a command and arguments in a string
 */
public class ExecutableDetails {
    public static String COMMAND_SEPARATOR = " #";

    public static String lookup(int pid) {
        String command;
        try {
            String comm = new String(Files.readAllBytes(Paths.get("/proc/", String.valueOf(pid), "/comm"))).replaceAll("\\r|\\n", "").replaceAll("\\p{C}", " ");
            String cmdLine = new String(Files.readAllBytes(Paths.get("/proc/", String.valueOf(pid), "/cmdline"))).replaceAll("\\r|\\n", "").replaceAll("\\p{C}", " ");
            command = comm + COMMAND_SEPARATOR + cmdLine;
        } catch (IOException e) {
            // If the process no longer exists then use the PID
            command = "pid=" + pid;
        }
        return command;
    }

    public static String getCommand(String commAndCmdline) {
        if (commAndCmdline != null && commAndCmdline.contains(COMMAND_SEPARATOR)) {
            return commAndCmdline.substring(0, commAndCmdline.indexOf(COMMAND_SEPARATOR));
        }
        return commAndCmdline;
    }

    public static String getCmdline(String commAndCmdline) {
        if (commAndCmdline != null && commAndCmdline.contains(COMMAND_SEPARATOR)) {
            int startIndex = commAndCmdline.indexOf(COMMAND_SEPARATOR) + COMMAND_SEPARATOR.length();
            if (startIndex < commAndCmdline.length()) {
                return commAndCmdline.substring(startIndex);
            }
        }
        return "";
    }

    public static String getCommandWithArguments(String commAndCmdline) {
        return getCommand(commAndCmdline) + " " + getCmdline(commAndCmdline);
    }
}
