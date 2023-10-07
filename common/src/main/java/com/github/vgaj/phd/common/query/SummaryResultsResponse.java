package com.github.vgaj.phd.common.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class SummaryResultsResponse implements Serializable
{
    public DisplayContent data;
}
