package com.github.vgaj.phd.server.data;

import lombok.Data;

/**
 * Some new data that was captured that needed to be queued for processing
 */
@Data
public class NewDataEvent
{
    // TODO: Can the data be stored here?
    private RemoteAddress host;
    private int length;
    private long epochMinute;

}
