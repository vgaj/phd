package com.github.vgaj.phd.server.data;

import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.result.TransferSizeBytes;
import com.github.vgaj.phd.server.result.TransferTimestamp;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class MonitorData
{
    // TODO: Periodic cleanup of uninteresting data
    // TODO: Save data and reload on restart

    @Autowired
    MessageData messageData;

    // Stats for each host
    private final ConcurrentMap<RemoteAddress, DataForAddress> data = new ConcurrentHashMap<>();

    public void addData(@NonNull RemoteAddress host, int length, long epochMinute)
    {
        if (!data.containsKey(host))
        {
            String hostname = host.lookupHost();
            messageData.addMessage("New host: " + hostname);
            data.put(host, new DataForAddress());
        }
        data.get(host).addBytes(length, epochMinute);
    }

    public ArrayList<Map.Entry<TransferTimestamp, TransferSizeBytes>> getCopyOfPerMinuteData(RemoteAddress address)
    {
        ArrayList<Map.Entry<TransferTimestamp, TransferSizeBytes>> entries = new ArrayList<>();
        data.get(address).getPerMinuteData().forEach(e -> entries.add(
                Map.entry( new TransferTimestamp(e.getKey()), new TransferSizeBytes(e.getValue()))));
        return entries;
    }

    public DataForAddress getDataForAddress(RemoteAddress address)
    {
        return data.get(address);
    }

    public List<RemoteAddress> getAddresses()
    {
        List<RemoteAddress> addresses = new LinkedList<>();
        data.keySet().forEach(a -> addresses.add(a));
        return addresses;
    }

}
