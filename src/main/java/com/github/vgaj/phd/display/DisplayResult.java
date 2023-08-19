package com.github.vgaj.phd.display;

import java.util.LinkedList;
import java.util.List;

public class DisplayResult
{

    public String hostName;
    public String ipAddress;
    public int totalBytes;
    public int totalTimes;
    public int score;
    public List<DisplayResultLine> resultLines = new LinkedList<>();

}
