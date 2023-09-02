package com.github.vgaj.phd.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
public class TransferCount
{
    @Getter
    private int count;

    @Override
    public String toString() { return String.format("%d",count);}
}
