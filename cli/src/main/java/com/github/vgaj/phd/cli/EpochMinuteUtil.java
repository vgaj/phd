package com.github.vgaj.phd.cli;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EpochMinuteUtil
{
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.systemDefault());
    public static String toString(long epochMinute)
    {
        return formatter.format(Instant.ofEpochSecond(epochMinute*60));
    }
}
