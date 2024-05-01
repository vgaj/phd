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

package com.github.vgaj.phd.server.query;

import com.github.vgaj.phd.common.ipc.DomainSocketComms;
import com.github.vgaj.phd.common.query.*;

import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Component
public class ExternalQueryTask  implements Runnable
{
    private MessageInterface messages = Messages.getLogger(this.getClass());

    private Thread queryThread;
    private boolean isShuttingDown = false;

    @Autowired
    private QueryLogic query;

    // Occurs after @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void start()
    {
        queryThread = new Thread(this);
        queryThread.start();
    }

    // Occurs before @PreDestroy
    @EventListener(ContextClosedEvent.class)
    public void stop()
    {
        try
        {
            isShuttingDown = true;
            queryThread.interrupt();
            queryThread.join(1);
        }
        catch (InterruptedException ignored)
        {
        }
    }

    @Override
    public void run()
    {
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(DomainSocketComms.SOCKET_PATH);

        try
        {
            Files.deleteIfExists(DomainSocketComms.SOCKET_PATH);
        }
        catch (IOException e)
        {
            messages.addError("Failed to delete " + DomainSocketComms.SOCKET_PATH, e);
            return;
        }

        try
        {
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX))
            {
                try
                {
                    serverChannel.bind(socketAddress);
                }
                catch (IOException e)
                {
                    if (!isShuttingDown)
                    {
                        messages.addError("Failed to bind to " + DomainSocketComms.SOCKET_PATH, e);
                    }
                    return;
                }

                // Make to world read/writable
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(PosixFilePermission.OWNER_READ);
                permissions.add(PosixFilePermission.OWNER_WRITE);
                permissions.add(PosixFilePermission.GROUP_READ);
                permissions.add(PosixFilePermission.GROUP_WRITE);
                permissions.add(PosixFilePermission.OTHERS_READ);
                permissions.add(PosixFilePermission.OTHERS_WRITE);
                try
                {
                    Files.setPosixFilePermissions(DomainSocketComms.SOCKET_PATH, permissions);
                }
                catch (IOException e)
                {
                    messages.addError("Failed to change permissions for " + DomainSocketComms.SOCKET_PATH, e);
                    return;
                }

                while (true)
                {
                    SocketChannel channel = null;
                    try
                    {
                        channel = serverChannel.accept();
                        messages.addDebug("New client connected.");
                    }
                    catch (IOException e)
                    {
                        if (!isShuttingDown)
                        {
                            messages.addError("Failed accept connection on " + DomainSocketComms.SOCKET_PATH, e);
                        }
                        return;
                    }
                    DomainSocketComms sockComms = new DomainSocketComms(channel);
                    new Thread(() -> {
                        while (true)
                        {
                            try
                            {
                                RequestInterface request = sockComms.readSocketMessage(RequestInterface.class);
                                if (request != null)
                                {
                                    if (request instanceof SummaryResultsQuery)
                                    {
                                        messages.addMessage("Received a summary request.");
                                        SummaryResultsResponse response = new SummaryResultsResponse(query.getDisplayContent());
                                        sockComms.writeSocketMessage(response);
                                    }
                                    else if (request instanceof HostHistoryQuery)
                                    {
                                        messages.addMessage("Received a detailed request.");
                                        HostHistoryResponse response = new HostHistoryResponse(query.getData(((HostHistoryQuery)request).address()).toArray(new String[0]));
                                        sockComms.writeSocketMessage(response);
                                    }
                                    else if (request instanceof DebugLogQuery)
                                    {
                                        messages.addMessage("Received a debug log request.");
                                        DebugLogResponse response = new DebugLogResponse(Messages.getMessages().toArray(new String[0]));
                                        sockComms.writeSocketMessage(response);
                                    }
                                    else
                                    {
                                        messages.addError( "Received unexpected request type " + request.getClass().getCanonicalName());
                                    }
                                    messages.addDebug("Sent response.");
                                }
                                else
                                {
                                    // Connection was closed
                                    messages.addDebug("Client disconnected.");
                                    break;
                                }
                            }
                            catch (IOException connectionException)
                            {
                                messages.addError("Error communicating with client", connectionException);
                                break;
                            }
                        }
                    }).start();
                }
            }
            catch (IOException e)
            {
                messages.addError("Failed to open " + DomainSocketComms.SOCKET_PATH, e);
            }
        }
        finally
        {
            try
            {
                Files.deleteIfExists(DomainSocketComms.SOCKET_PATH);
            }
            catch (IOException e)
            {
                messages.addError("Failed to delete " + DomainSocketComms.SOCKET_PATH, e);
            }
        }
    }
}
