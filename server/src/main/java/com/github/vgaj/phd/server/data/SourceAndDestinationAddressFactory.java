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

import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constructs instances of SourceAndDestinationAddress
 * If the source is local then it the bytes will be set to 0s.
 * This is needed because we compare addresses related to PID which do not have
 * a source address recorded with source/destination pairs.
 * This class is responsible for enforcing that.
 */
@Component
public class SourceAndDestinationAddressFactory
{
    private MessageInterface messages = Messages.getLogger(this.getClass());
    private List<InetAddress> localAddresses = null;
    private Stream<InetAddress> getLocalAddressesStream() {
        if (localAddresses == null) {
            try {
                localAddresses = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                        .filter(nic -> {
                            try {
                                return nic.isUp() && !nic.isLoopback();
                            } catch (SocketException e) {
                                messages.addError("Unable to check if NIC is up or loopback: " + nic.getDisplayName(), e);
                                return false;
                            }
                        })
                        .flatMap(nic -> Collections.list(nic.getInetAddresses()).stream())
                        .filter(inetAddress -> !inetAddress.isLoopbackAddress()
                                && !inetAddress.isLinkLocalAddress()
                                && !inetAddress.isMulticastAddress())
                        .collect(Collectors.toList());
            } catch (SocketException e) {
                messages.addError("Unable to get local addresses", e);
                localAddresses = new ArrayList<>();
            }
        }
        return localAddresses.stream();
    }

    public SourceAndDestinationAddress createForDestinationAddress(Pointer address)
    {
        return new SourceAndDestinationAddress(address.getByte(0), address.getByte(1), address.getByte(2), address.getByte(3));
    }

    public SourceAndDestinationAddress createForSourceAndDestinationAddress(Pointer address)
    {
        // Note that the bytes are written in little endian in BPF
        SourceAndDestinationAddress sourceAndDestinationAddress = new SourceAndDestinationAddress(
                address.getByte(4), address.getByte(5), address.getByte(6), address.getByte(7),
                address.getByte(0), address.getByte(1), address.getByte(2), address.getByte(3)
        );
        getLocalAddressesStream().forEach(localAddress -> {
            if (sourceAndDestinationAddress.doesSourceMatch(localAddress)) {
                sourceAndDestinationAddress.clearSourceAddress();
            }
        });
        return sourceAndDestinationAddress;
    }
}
