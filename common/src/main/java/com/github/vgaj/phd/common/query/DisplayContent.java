package com.github.vgaj.phd.common.query;

import java.io.Serializable;
import java.util.ArrayList;

public record DisplayContent (DisplayResult[] results, String[] messages) implements Serializable
{
}
