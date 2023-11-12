package com.github.vgaj.phd.common.query;

import java.io.Serializable;
import java.util.ArrayList;

public record DisplayResult (String hostName,  String ipAddress, int totalBytes, int totalTimes, int score, long lastSeenEpochMinute, DisplayResultLine[] resultLines) implements Serializable
{
}