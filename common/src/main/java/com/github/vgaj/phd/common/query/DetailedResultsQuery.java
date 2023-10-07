package com.github.vgaj.phd.common.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

@Data
@AllArgsConstructor
public class DetailedResultsQuery extends ResultsQueryBase
{
    public InetAddress address;
}
