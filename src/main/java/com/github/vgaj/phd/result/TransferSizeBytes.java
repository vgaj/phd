package com.github.vgaj.phd.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
public class TransferSizeBytes
{
    @Getter
    private int size;

    @Override
    public String toString() { return String.format("%d",size);}
}
