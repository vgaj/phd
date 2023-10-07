package com.github.vgaj.phd.common.query;

import java.io.Serializable;
import java.util.ArrayList;

public class DisplayContent implements Serializable
{
    // TODO consider using records
    public ArrayList<DisplayResult> results;
    public ArrayList<String> messages;
}
