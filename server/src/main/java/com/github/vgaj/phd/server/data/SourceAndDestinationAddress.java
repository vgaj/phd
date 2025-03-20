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

package com.github.vgaj.phd.server.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@NoArgsConstructor
public class SourceAndDestinationAddress implements Comparable<SourceAndDestinationAddress> {
    /**
     * The source address. If all octets are 0 that means it is local.
     * It is done this way to enable easy deserialisation
     */
    private final byte[] srcOctets = new byte[4];

    /**
     * The destination address
     */
    private final byte[] dstOctets = new byte[4];

    private String destinationHostname = null;

    private boolean destinationLookupAttempted = false;

    @Getter
    private String reverseDesinationHostname = null;

    @Getter
    private String sourceMacAddressAndDetails = null;

    public SourceAndDestinationAddress(byte srcOctet1, byte srcOctet2, byte srcOctet3, byte srcOctet4, byte dstOctet1, byte dstOctet2, byte dstOctet3, byte dstOctet4)  {
        srcOctets[0] = srcOctet1;
        srcOctets[1] = srcOctet2;
        srcOctets[2] = srcOctet3;
        srcOctets[3] = srcOctet4;
        dstOctets[0] = dstOctet1;
        dstOctets[1] = dstOctet2;
        dstOctets[2] = dstOctet3;
        dstOctets[3] = dstOctet4;
    }
    public SourceAndDestinationAddress(byte dstOctet1, byte dstOctet2, byte dstOctet3, byte dstOctet4) {
        dstOctets[0] = dstOctet1;
        dstOctets[1] = dstOctet2;
        dstOctets[2] = dstOctet3;
        dstOctets[3] = dstOctet4;
    }

    public SourceAndDestinationAddress(InetAddress srcAddress, InetAddress dstAddress) {
        assert srcAddress != null && srcAddress.getAddress().length == 4 && dstAddress != null && dstAddress.getAddress().length == 4;
        srcOctets[0] = srcAddress.getAddress()[0];
        srcOctets[1] = srcAddress.getAddress()[1];
        srcOctets[2] = srcAddress.getAddress()[2];
        srcOctets[3] = srcAddress.getAddress()[3];
        dstOctets[0] = dstAddress.getAddress()[0];
        dstOctets[1] = dstAddress.getAddress()[1];
        dstOctets[2] = dstAddress.getAddress()[2];
        dstOctets[3] = dstAddress.getAddress()[3];
    }

    public SourceAndDestinationAddress(InetAddress dstAddress) {
        assert dstAddress != null && dstAddress.getAddress().length == 4;
        dstOctets[0] = dstAddress.getAddress()[0];
        dstOctets[1] = dstAddress.getAddress()[1];
        dstOctets[2] = dstAddress.getAddress()[2];
        dstOctets[3] = dstAddress.getAddress()[3];
    }

    public boolean doesSourceMatch(InetAddress address) {
        return Arrays.equals(srcOctets, address.getAddress());
    }

    public void clearSourceAddress() {
        Arrays.fill(srcOctets, (byte)0);
    }

    @JsonIgnore
    public boolean isSourceAddressClear() {
        return IntStream.range(0, srcOctets.length).allMatch(i -> srcOctets[i] == 0);
    }

    @JsonIgnore
    public String getDesinationAddressString() {
        return getAddressString(dstOctets);
    }

    @JsonIgnore
    public String getSourceAddressString() {
        return getAddressString(srcOctets);
    }

    private static String getAddressString(byte[] octetsToPrint) {
        StringBuilder ip = new StringBuilder();
        for (int i = 0; i < octetsToPrint.length; i++) {
            ip.append(Byte.toUnsignedInt(octetsToPrint[i]));
            if (i != octetsToPrint.length - 1) {
                ip.append(".");
            }
        }
        return ip.toString();
    }

    @JsonIgnore
    public String getDesinationHostString() {
        return (destinationHostname != null) ? destinationHostname : getDesinationAddressString();
    }

    /**
     * If the IP address has not previously been looked up then it is looked up.
     */
    public void lookupDestinationHost() throws UnknownHostException {
        if (!destinationLookupAttempted) {
            destinationLookupAttempted = true;
            InetAddress addr = InetAddress.getByAddress(dstOctets);
            destinationHostname = addr.getHostName();
            if (destinationHostname != null) {
                List<String> parts = Arrays.asList(destinationHostname.split("\\."));
                Collections.reverse(parts);
                reverseDesinationHostname = String.join(".", parts);
            }
        }
    }

    /**
     * Use nmap to get the MAC address and type for the source address
     */
    public void lookupSourceMacAddress() {
        /*
        if (!isSourceAddressClear())  {
            sourceMacAddressAndDetails = SourceIpToMacAddressLookup.lookup(getSourceAddressString());
        }
        */
        sourceMacAddressAndDetails = SourceIpToMacAddressLookup.lookup("192.168.1.1");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceAndDestinationAddress that = (SourceAndDestinationAddress) o;
        return Arrays.equals(srcOctets, that.srcOctets) && Arrays.equals(dstOctets, that.dstOctets);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {
                Arrays.hashCode(srcOctets),
                Arrays.hashCode(dstOctets)
        });
    }

    @Override
    public int compareTo(SourceAndDestinationAddress other) {
        int compareDst = Arrays.compare(this.dstOctets, other.dstOctets);
        if (compareDst != 0) {
            return compareDst;
        }
        return Arrays.compare(this.srcOctets, other.srcOctets);
    }

}
