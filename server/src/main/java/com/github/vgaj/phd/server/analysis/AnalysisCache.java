/*
MIT License

Copyright (c) 2022-2024 Viru Gajanayake

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

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

    public void removeCurrentResult(RemoteAddress address)
    {
        currentResults.remove(address);
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
