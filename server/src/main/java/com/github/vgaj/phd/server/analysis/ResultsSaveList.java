package com.github.vgaj.phd.server.analysis;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class ResultsSaveList
{
    @Getter
    private List<ResultsSaveItem> resultsForSaving = new LinkedList<>();
}
