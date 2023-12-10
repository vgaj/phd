package com.github.vgaj.phd.server.monitor;

import com.github.vgaj.phd.server.data.RemoteAddress;

import java.util.Set;

public interface MonitorTaskFilterUpdateInterface
{
    void updateFilter(Set<RemoteAddress> addressesToExclude);
}
