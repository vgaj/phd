package com.github.vgaj.phd.data;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RemoteAddress
{
    // TODO: Log4J configuration
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final byte[] octets = new byte[4];
    private String hostname = null;
    private boolean lookupAttempted = false;

    @Getter
    private String reverseHostname = null;

    public RemoteAddress(byte octet1, byte octet2, byte octet3, byte octet4)
    {
        octets[0] = octet1;
        octets[1] = octet2;
        octets[2] = octet3;
        octets[3] = octet4;
    }

    public RemoteAddress(InetAddress address)
    {
        assert address != null && address.getAddress().length == 4;
        octets[0] = address.getAddress()[0];
        octets[1] = address.getAddress()[1];
        octets[2] = address.getAddress()[2];
        octets[3] = address.getAddress()[3];
    }

    public String getAddressString()
    {
        StringBuilder ip = new StringBuilder();
        for (int i = 0; i < octets.length; i++)
        {
            ip.append(Byte.toUnsignedInt(octets[i]));
            if (i != octets.length - 1)
            {
                ip.append(".");
            }
        }
        return ip.toString();
    }

    public String getHostString()
    {
        return (hostname != null) ? hostname : getAddressString();
    }

    /**
     * If the IP address has not previously been looked up then it is looked up.
     * @return The hostname
     */
    public String lookupHost()
    {
        if (!lookupAttempted)
        {
            lookupAttempted = true;
            try
            {
                InetAddress addr = InetAddress.getByAddress(octets);
                hostname = addr.getHostName();
                if (hostname != null)
                {
                    List<String> parts = Arrays.asList(hostname.split("\\."));
                    Collections.reverse(parts);
                    reverseHostname = String.join(".", parts);
                }

                hostname += "/";
            }
            catch (UnknownHostException e)
            {
                logger.error("Failed to lookup address", e);
            }
            hostname += getAddressString();
        }
        return hostname;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteAddress that = (RemoteAddress) o;
        return Arrays.equals(octets, that.octets);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(octets);
    }

}
