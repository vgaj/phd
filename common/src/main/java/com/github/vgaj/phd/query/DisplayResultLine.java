package com.github.vgaj.phd.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DisplayResultLine implements Serializable
{
    public DisplayResultLine(String message)
    {
        this.message = message;
    }
    public String message;
    public ArrayList<String> subMessages = new ArrayList<>();
}