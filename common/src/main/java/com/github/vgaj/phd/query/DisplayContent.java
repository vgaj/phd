package com.github.vgaj.phd.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DisplayContent implements Serializable
{
    // TODO consider using records
    public ArrayList<DisplayResult> results;
    public ArrayList<String> messages;
}
