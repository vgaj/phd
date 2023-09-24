package com.github.vgaj.phd.server.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
public class TransferIntervalMinutes
{
    @Getter
    private int interval;

    @Override
    public String toString() { return String.format("%d",interval);}
}
