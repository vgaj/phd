/*
MIT License

Copyright (c) 2025 Viru Gajanayake

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

package com.github.vgaj.phd.server.data;

import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Uses nmap to lookup the MAC address and NIC details
 */
public class SourceIpToMacAddressLookup
{
    private static final MessageInterface messages = Messages.getLogger(SourceIpToMacAddressLookup.class);

    private static final ConcurrentMap<String, String > data = new ConcurrentHashMap<>();

    public static String lookup(String ipAddress)
    {
        if (!data.containsKey(ipAddress))
        {
            // We only attempt the lookup once and if it fails store this value
            String macAddressString = null;

            try
            {
                String[] command = {"/usr/bin/nmap", "-sn", ipAddress};
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                StringBuilder sb = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    // Example: MAC Address: A1:B2:C3:D4:E5:F6 (Tp-link Technologies)
                    if (line.startsWith("MAC Address:"))
                    {
                        int startIndex = line.indexOf(':') + 2;
                        if (line.length() > startIndex)
                        {
                            macAddressString = line.substring(startIndex);
                        }
                    }
                    sb.append(line);
                }

                // Capture errors
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null)
                {
                    sb.append(line);
                }

                // Wait for process to finish
                int exitCode = process.waitFor();
                if (macAddressString == null)
                {
                    messages.addError("nmap exited with code: " + exitCode);
                    messages.addError(sb.toString());
                }
            }
            catch (IOException | InterruptedException e)
            {
                messages.addError("nmap failed",e);
            }
            data.put(ipAddress, macAddressString);
        }

        return data.get(ipAddress);
    }


}
