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

import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Data about processes.  The last process associated with an address.
 */
@Component
public class HostToExecutableLookup
{
    private MessageInterface messages = Messages.getLogger(this.getClass());

    @Autowired
    private PidToCommandLookup pidToCommandLookup;

    // Last PID or command for each host
    private final ConcurrentMap<RemoteAddress, String > data = new ConcurrentHashMap<>();

    public void addData(@NonNull RemoteAddress host, int pid)
    {
        String command = pidToCommandLookup.get(pid);
        String previousCommand = data.put(host, command);

        //if (previousCommand != null && !command.equals(previousCommand)) messages.addMessage("Different command: " + command + " (previously " + previousCommand + ") for host " + host.getAddressString());
    }

    public String getProcessForAddress(RemoteAddress address)
    {
        return data.get(address);
    }
}
