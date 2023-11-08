package com.github.vgaj.phd.server.analysis;

import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.result.AnalysisResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class AnalysisCache
{
    private final ConcurrentMap<RemoteAddress, AnalysisResult> currentResults = new ConcurrentHashMap<>();
    private final ConcurrentMap<RemoteAddress, AnalysisResult> previousResults = new ConcurrentHashMap<>();

    public void putCurrentResult(RemoteAddress address, AnalysisResult result)
    {
        currentResults.put(address, result);
    }

    public void putPreviousResult(RemoteAddress address, AnalysisResult result)
    {
        previousResults.put(address, result);
    }

    public Optional<AnalysisResult> getResult(RemoteAddress address)
    {
        // TODO add unit test
        AnalysisResult currentResult = currentResults.get(address);
        AnalysisResult previousResult = previousResults.get(address);
        if (currentResult != null && previousResult != null)
        {
            return Optional.of(currentResult.merge(previousResult));
        }
        else
        {
            return Optional.ofNullable((currentResult != null) ? currentResult : previousResult);
        }
    }

    public List<RemoteAddress> getAddresses()
    {
        // TODO add unit test
        ArrayList<RemoteAddress> addresses = new ArrayList<>(2*(currentResults.keySet().size()+previousResults.keySet().size()));
        currentResults.keySet().forEach(address -> addresses.add(address));
        previousResults.keySet().forEach(address -> { if (!addresses.contains(address)) addresses.add(address);} );
        return addresses;
    }

}
