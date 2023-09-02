package com.github.vgaj.phd.util;

import com.github.vgaj.phd.data.RemoteAddress;
import org.pcap4j.core.PcapPacket;
import org.springframework.stereotype.Component;

/**
 * Functionality to parse pcap data
 */
@Component
public class PcapPacketHelper
{
     public boolean isIpv4(PcapPacket pcapPacket)
    {
        return (pcapPacket.getRawData()[14]>>4 == 4);
    }

    public RemoteAddress getSourceHost(PcapPacket pcapPacket)
    {
        return getHostAtOffset(pcapPacket,14+12);
    }
    public RemoteAddress getDestHost(PcapPacket pcapPacket)
    {
        return getHostAtOffset(pcapPacket,14+16);
    }
    private RemoteAddress getHostAtOffset(PcapPacket pcapPacket, int offset)
    {
        // Want data to be stored on the stack, hence not using an array
        byte octet1,octet2,octet3,octet4;
        octet1 = pcapPacket.getRawData()[offset++];
        octet2 = pcapPacket.getRawData()[offset++];
        octet3 = pcapPacket.getRawData()[offset++];
        octet4 = pcapPacket.getRawData()[offset++];
        return new RemoteAddress(octet1,octet2,octet3,octet4);
    }
    public int getLength(PcapPacket pcapPacket)
    {
        return pcapPacket.getOriginalLength();
    }

    public long getEpochMinute(PcapPacket pcapPacket)
    {
        return pcapPacket.getTimestamp().getEpochSecond() / 60;
    }
}
