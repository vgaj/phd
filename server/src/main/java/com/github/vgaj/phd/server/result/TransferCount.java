package com.github.vgaj.phd.server.result;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class TransferCount
{
    public static TransferCount of(int count)
    {
        TransferCount t = new TransferCount();
        t.count = count;
        return t;
    }

    @Getter
    private int count;

    public TransferCount merge(TransferCount other)
    {
        return TransferCount.of(count + other.count);
    }

    @Override
    public String toString() { return String.format("%d",count);}
}
