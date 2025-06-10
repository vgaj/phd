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

package com.github.vgaj.phd.server.monitor.pcap;

import com.github.vgaj.phd.server.address.SourceAndDestinationAddress;

import org.pcap4j.packet.Packet;
import org.springframework.stereotype.Component;

/**
 * Functionality to parse pcap data
 */
@Component
public class PcapPacketHelper {
    public boolean isIpv4(Packet pcapPacket) {
        return (pcapPacket.getRawData()[14] >> 4 == 4);
    }

    public SourceAndDestinationAddress getSourceHost(Packet pcapPacket) {
        return getHostAtOffset(pcapPacket, 14 + 12);
    }

    public SourceAndDestinationAddress getDestHost(Packet pcapPacket) {
        return getHostAtOffset(pcapPacket, 14 + 16);
    }

    private SourceAndDestinationAddress getHostAtOffset(Packet pcapPacket, int offset) {
        // Want data to be stored on the stack, hence not using an array
        byte octet1, octet2, octet3, octet4;
        octet1 = pcapPacket.getRawData()[offset++];
        octet2 = pcapPacket.getRawData()[offset++];
        octet3 = pcapPacket.getRawData()[offset++];
        octet4 = pcapPacket.getRawData()[offset];
        return new SourceAndDestinationAddress(octet1, octet2, octet3, octet4);
    }

    public int getLength(Packet pcapPacket) {
        return pcapPacket.length();
    }

}
