package com.github.vgaj.phd.common.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record DisplayResultLine (String message,  String[] subMessages) implements Serializable
{
}