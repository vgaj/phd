package com.github.vgaj.phd.server.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TransferSizeBytes
{
    public static TransferSizeBytes of(int size)
    {
        TransferSizeBytes t = new TransferSizeBytes();
        t.size = size;
        return t;
    }
    @Getter
    private int size;

    @Override
    public String toString() { return String.format("%d",size);}
}
