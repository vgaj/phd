package com.github.vgaj.phd.server.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
public class TransferIntervalMinutes
{
    public static TransferIntervalMinutes of(int interval)
    {
        TransferIntervalMinutes t = new TransferIntervalMinutes();
        t.interval = interval;
        return t;
    }

    @Getter
    private int interval;

    @Override
    public String toString() { return String.format("%d",interval);}
}
