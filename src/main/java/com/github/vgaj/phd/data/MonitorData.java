package com.github.vgaj.phd.data;

import com.github.vgaj.phd.result.TransferSizeBytes;
import com.github.vgaj.phd.result.TransferTimestamp;
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

    public ArrayList<Map.Entry<RemoteAddress, DataForAddress>> getCopyOfRawData()
    {
        ArrayList<Map.Entry<RemoteAddress, DataForAddress>> entries = new ArrayList<>();
        data.entrySet().forEach(e -> entries.add(e));
        return entries;
    }

    public DataForAddress getDataForAddress(RemoteAddress address)
    {
        return data.get(address);
    }
}
