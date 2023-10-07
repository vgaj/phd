package com.github.vgaj.phd.common.query;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class DetailedResultsResponse implements Serializable
{
    public ArrayList<String> results;
}
