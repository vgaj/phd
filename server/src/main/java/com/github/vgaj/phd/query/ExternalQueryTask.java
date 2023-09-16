package com.github.vgaj.phd.query;

import com.github.vgaj.phd.display.ModelGenerator;
import com.github.vgaj.phd.ipc.DomainSocketComms;
import com.github.vgaj.phd.query.SummaryResultsQuery;
import com.github.vgaj.phd.query.SummaryResultsResponse;
import org.pcap4j.core.NotOpenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class ExternalQueryTask  implements Runnable
{
    private Thread queryThread;

    @Autowired
    private ModelGenerator modelGenerator;

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
        catch (InterruptedException e)
        {
            // TODO use a logging framework
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {

        Path socketPath = Path.of("/tmp", "phone_home_detector_ipc");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        try {
            Files.deleteIfExists(socketPath);
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
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
                    //TODO
                    e.printStackTrace();
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
                try {
                    Files.setPosixFilePermissions(socketPath, permissions);
                } catch (IOException e) {
                    // TODO
                    System.err.println("Error changing permissions: " + e.getMessage());
                    return;
                }

                while (true)
                {
                    System.out.println("accept...");
                    SocketChannel channel = null;
                    try {
                        channel = serverChannel.accept();
                        System.out.println("accepted");
                    } catch (IOException e) {
                        //TODO
                        e.printStackTrace();
                        return;
                    }
                    DomainSocketComms<SummaryResultsQuery, SummaryResultsResponse> sockComms
                            = new DomainSocketComms<>(channel);
                    new Thread(() -> {
                        while (true)
                        {
                            System.out.println("top of loop");
                            try
                            {
                                System.out.println("reading message");
                                Optional<SummaryResultsQuery> request = sockComms.readSocketMessage();
                                System.out.println("got message");
                                if (request.isPresent())
                                {
                                    SummaryResultsResponse response = new SummaryResultsResponse();
                                    response.setData(modelGenerator.getDisplayContent());
                                    sockComms.writeSocketMessage(response);
                                }
                                else
                                {
                                    // Connection was closed
                                    break;
                                }
                            }
                            catch (IOException e1)
                            {
                                //TODO
                                e1.printStackTrace();
                                System.out.println("exiting due to error " + e1.toString());
                            }

                        }
                    }).start();
                }
            }
            catch (IOException e)
            {
                //TODO
                e.printStackTrace();
            }
        }
        finally
        {
            try
            {
                Files.deleteIfExists(socketPath);
            } catch (IOException e)
            {
                //TODO
                e.printStackTrace();
            }
        }
    }
}
