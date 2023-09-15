package com.github.vgaj.phd.util;

import com.github.vgaj.phd.data.RemoteAddress;
import org.pcap4j.packet.Packet;
import org.springframework.stereotype.Component;

/**
 * Functionality to parse pcap data
 */
@Component
public class PcapPacketHelper
{
     public boolean isIpv4(Packet pcapPacket)
    {
        return (pcapPacket.getRawData()[14]>>4 == 4);
    }

    public RemoteAddress getSourceHost(Packet pcapPacket)
    {
        return getHostAtOffset(pcapPacket,14+12);
    }
    public RemoteAddress getDestHost(Packet pcapPacket)
    {
        return getHostAtOffset(pcapPacket,14+16);
    }
    private RemoteAddress getHostAtOffset(Packet pcapPacket, int offset)
    {
        // Want data to be stored on the stack, hence not using an array
        byte octet1,octet2,octet3,octet4;
        octet1 = pcapPacket.getRawData()[offset++];
        octet2 = pcapPacket.getRawData()[offset++];
        octet3 = pcapPacket.getRawData()[offset++];
        octet4 = pcapPacket.getRawData()[offset++];
        return new RemoteAddress(octet1,octet2,octet3,octet4);
    }
    public int getLength(Packet pcapPacket)
    {
        return pcapPacket.length();
    }


}
