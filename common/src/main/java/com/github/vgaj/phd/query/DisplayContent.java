package com.github.vgaj.phd.query;

import java.io.Serializable;
import java.util.List;

public class DisplayContent implements Serializable
{
    public List<DisplayResult> results;
    public List<String> messages;
}
