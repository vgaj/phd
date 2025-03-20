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
import com.github.vgaj.phd.server.result.TransferSizeBytes;
import com.github.vgaj.phd.server.result.TransferTimestamp;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Store of traffic data for each host
 */
@Component
public class TrafficDataStore
{
    private MessageInterface messages = Messages.getLogger(this.getClass());

    // Stats for each host
    private final ConcurrentMap<SourceAndDestinationAddress, DataForAddress> data = new ConcurrentHashMap<>();

    public void addData(@NonNull SourceAndDestinationAddress host, int length, long epochMinute)
    {
        if (!data.containsKey(host))
        {
            try
            {
                host.lookupDestinationHost();
                host.lookupSourceMacAddress();
                messages.addDebug("Adding destination: " + host.getDesinationHostString());

                data.put(host, new DataForAddress());
            }
            catch (UnknownHostException e)
            {
                messages.addError("Failed to lookup address", e);
            }
        }
        //messages.addMessage("Received " + length + " bytes for " + host.getAddressString());
        data.get(host).addBytes(length, epochMinute);
    }

    public ArrayList<Map.Entry<TransferTimestamp, TransferSizeBytes>> getCopyOfPerMinuteData(SourceAndDestinationAddress address)
    {
        ArrayList<Map.Entry<TransferTimestamp, TransferSizeBytes>> entries = new ArrayList<>();
        data.get(address).getPerMinuteData().forEach(e -> entries.add(
                Map.entry( new TransferTimestamp(e.getKey()), new TransferSizeBytes(e.getValue()))));
        return entries;
    }

    public DataForAddress getDataForAddress(SourceAndDestinationAddress address)
    {
        return data.get(address);
    }

    public List<SourceAndDestinationAddress> getAddresses()
    {
        List<SourceAndDestinationAddress> addresses = new LinkedList<>();
        data.keySet().forEach(a -> addresses.add(a));
        return addresses;
    }

    public List<SourceAndDestinationAddress> getAddressesWithDataSince(long epochMinute)
    {
        List<SourceAndDestinationAddress> addresses = new LinkedList<>();
        data.keySet().forEach(address ->
        {
            DataForAddress addressData = data.get(address);
            if (addressData != null && addressData.getLatestEpochMinute() >= epochMinute)
            {
                addresses.add(address);
            }
        });
        return addresses;
    }

    public void cleanupIgnoredAddresses(Set<SourceAndDestinationAddress> addressesToExclude)
    {
        addressesToExclude.forEach(address -> data.remove(address));
        messages.addDebug("Size of Monitor Data is now " + data.size());
    }
}
