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

import com.github.vgaj.phd.common.properties.ClientProperties;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Uses DHCP from hotspot to map an IP address to a name
 */
public class SourceIpToDnsNameLookup {
    private static final String LEASE_FILE_PREFIX = "/var/lib/NetworkManager/dnsmasq-";
    private static final String LEASE_FILE_POSTFIX = ".leases";
    private static final Path HOTSPOT_NIC_FILE = Path.of(ClientProperties.getHotspotNicPath());

    private static final MessageInterface messages = Messages.getLogger(SourceIpToMacAddressLookup.class);
    private static final ConcurrentMap<String, String> data = new ConcurrentHashMap<>();

    private static boolean nameInitialiseAttempted = false;
    private static String nameOfLeaseFile = null;

    private static String getNameOfLeaseFile() {
        if (!nameInitialiseAttempted) {
            nameInitialiseAttempted = true;
            if (Files.exists(HOTSPOT_NIC_FILE)) {
                try {
                    String hotspotNic = Files.readString(HOTSPOT_NIC_FILE).strip();
                    nameOfLeaseFile = LEASE_FILE_PREFIX + hotspotNic + LEASE_FILE_POSTFIX;
                } catch (IOException e) {
                    messages.addError("Fail to read hotspot NIC", e);
                }
            }
        }
        return nameOfLeaseFile;
    }

    public static String lookup(String ipAddress) {
        if (!data.containsKey(ipAddress)) {
            // We only attempt the lookup once and if it fails store this value
            String name = null;

            if (getNameOfLeaseFile() != null) {
                try (BufferedReader reader = Files.newBufferedReader(Path.of(getNameOfLeaseFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 4 && parts[2].equals(ipAddress)) {
                            name = parts[3];
                        }
                    }
                } catch (IOException e) {
                    messages.addError("Error reading file " + getNameOfLeaseFile() + ": " + e.getMessage());
                }

                data.put(ipAddress, name);
            }
        }
        return data.get(ipAddress);
    }
}

