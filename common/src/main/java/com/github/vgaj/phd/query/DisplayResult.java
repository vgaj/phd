package com.github.vgaj.phd.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DisplayResult implements Serializable
{
    public String hostName;
    public String ipAddress;
    public int totalBytes;
    public int totalTimes;
    public int score;
    public ArrayList<DisplayResultLine> resultLines = new ArrayList<>();
}