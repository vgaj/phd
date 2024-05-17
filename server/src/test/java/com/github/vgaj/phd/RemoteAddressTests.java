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

package com.github.vgaj.phd;

import com.github.vgaj.phd.server.data.RemoteAddress;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RemoteAddressTests
{
    @Test
    public void addressStringFromBytesTest()
    {
        RemoteAddress address = new RemoteAddress((byte) 192, (byte) 168, (byte) 111, (byte) 222);
        assert address.getAddressString().equals("192.168.111.222");
    }

    @Test
    public void addressStringFromInetAddressTest() throws UnknownHostException
    {
        String addressString = "192.168.111.222";
        InetAddress inetAddress = InetAddress.getByName(addressString);
        RemoteAddress remoteAddress = new RemoteAddress(inetAddress);
        assert remoteAddress.getAddressString().equals(addressString);
    }

    @Test
    public void nonResolvableIpAddressStringTest() throws UnknownHostException
    {
        RemoteAddress address = new RemoteAddress((byte) 192, (byte) 168, (byte) 111, (byte) 222);
        address.lookupHost();
        assert address.getHostString().equals("192.168.111.222");
    }

    @Test
    public void resolvableIpAddressStringPriorToLookupTest() throws UnknownHostException
    {
        RemoteAddress address = new RemoteAddress((byte) 8, (byte) 8, (byte) 8, (byte) 8);
        assert address.getHostString().equals("8.8.8.8");
    }

    @Test
    public void resolvableIpAddressStringAfterLookupTest() throws UnknownHostException
    {
        RemoteAddress address = new RemoteAddress((byte) 8, (byte) 8, (byte) 8, (byte) 8);
        address.lookupHost();
        assert !address.getHostString().equals("8.8.8.8");
    }
}
