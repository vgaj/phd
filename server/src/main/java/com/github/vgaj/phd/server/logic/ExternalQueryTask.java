package com.github.vgaj.phd.server.logic;

import com.github.vgaj.phd.ipc.DomainSocketComms;
import com.github.vgaj.phd.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private Thread queryThread;

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
            logger.error("Failed to delete " + DomainSocketComms.SOCKET_PATH, e);
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
                    logger.error("Failed to bind to " + DomainSocketComms.SOCKET_PATH, e);
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
                    logger.error("Failed to change permissions for " + DomainSocketComms.SOCKET_PATH, e);
                    return;
                }

                while (true)
                {
                    SocketChannel channel = null;
                    try
                    {
                        channel = serverChannel.accept();
                        logger.info("New client connected.");
                    }
                    catch (IOException e)
                    {
                        logger.error("Failed accept connection on " + DomainSocketComms.SOCKET_PATH, e);
                        return;
                    }
                    DomainSocketComms sockComms = new DomainSocketComms(channel);
                    new Thread(() -> {
                        while (true)
                        {
                            try
                            {
                                ResultsQueryBase request = (ResultsQueryBase) sockComms.readSocketMessage(ResultsQueryBase.class);
                                if (request != null)
                                {
                                    if (request instanceof SummaryResultsQuery)
                                    {
                                        logger.info("Received a summary request.");
                                        SummaryResultsResponse response = new SummaryResultsResponse();
                                        response.setData(query.getDisplayContent());
                                        sockComms.writeSocketMessage(response);
                                    }
                                    else if (request instanceof DetailedResultsQuery)
                                    {
                                        logger.info("Received a detailed request.");
                                        DetailedResultsResponse response = new DetailedResultsResponse();
                                        response.setResults(query.getData(((DetailedResultsQuery)request).address));
                                        sockComms.writeSocketMessage(response);
                                    }
                                    else
                                    {
                                        logger.error( "Received unexpected request type " + request.getClass().getCanonicalName());
                                    }
                                    logger.info("Sent response.");
                                }
                                else
                                {
                                    // Connection was closed
                                    logger.info("Client disconnected.");
                                    break;
                                }
                            }
                            catch (IOException connectionException)
                            {
                                logger.error("Error communicating with client", connectionException);
                                break;
                            }
                        }
                    }).start();
                }
            }
            catch (IOException e)
            {
                logger.error("Failed to open " + DomainSocketComms.SOCKET_PATH, e);
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
                logger.error("Failed to delete " + DomainSocketComms.SOCKET_PATH, e);
            }
        }
    }
}
