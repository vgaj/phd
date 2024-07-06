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

package com.github.vgaj.phd.server.data;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@Component
public class PidToCommandLookup
{
    private HashMap<Integer,String> cache;
    long nextCacheRefresh = 0;

    String get(int pid)
    {
        // Reset the cache after 30 seconds
        if (System.currentTimeMillis() > nextCacheRefresh)
        {
            cache = new HashMap<>();
            nextCacheRefresh = System.currentTimeMillis() + 30000;
        }

        if (!cache.containsKey(pid))
        {
            String command;
            try
            {
                // TODO include cmdline

                command = new String(Files.readAllBytes(Paths.get("/proc/", String.valueOf(pid), "/comm")));
                command = command.replaceAll("\\r|\\n", "");
            } catch (IOException e)
            {
                // If the process no longer exists then use the PID
                command = "pid=" + pid;
            }
            cache.put(pid, command);
        }

        return cache.get(pid);
    }
}
