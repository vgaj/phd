package com.github.vgaj.phd.common.query;

import java.io.Serializable;
import java.util.ArrayList;

public class DisplayResult implements Serializable
{
    public String hostName;
    public String ipAddress;
    public int totalBytes;
    public int totalTimes;
    public int score;
    public long lastSeenEpochMinute;
    public ArrayList<DisplayResultLine> resultLines = new ArrayList<>();
}