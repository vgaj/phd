package com.github.vgaj.phd.query;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.io.Serializable;

@Data
public class SummaryResultsResponse implements Serializable
{
    public DisplayContent data;
}
