package com.github.vgaj.phd.display;

import java.util.LinkedList;
import java.util.List;

public class DisplayResultLine
{
    public DisplayResultLine(String message)
    {
        this.message = message;
    }
    public String message;
    public List<String> subMessages = new LinkedList<>();
}

