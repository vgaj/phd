package com.github.vgaj.phd.server.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
public class TransferTimestamp
{
    @Getter
    private Long timestamp;

    @Override
    public String toString() { return String.format("%d",timestamp); }

    public int compareTo(TransferTimestamp other)
    {
        return timestamp.compareTo(other.timestamp);
    }

    public TransferIntervalMinutes subtract(TransferTimestamp other)
    {
        return TransferIntervalMinutes.of((int) (timestamp - other.timestamp));
    }
}
