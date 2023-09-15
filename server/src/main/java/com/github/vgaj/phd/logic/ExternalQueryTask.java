package com.github.vgaj.phd.logic;

import com.github.vgaj.phd.ipc.DomainSocketComms;
import org.pcap4j.core.NotOpenException;
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
import java.nio.file.Path;
import java.util.Optional;

@Component
public class ExternalQueryTask  implements Runnable
{
    private Thread queryThread;




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
            //handle.breakLoop();
            queryThread.join(1);
        }
        catch (InterruptedException e)
        {
            // TODO
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
                    e.printStackTrace();
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
                        e.printStackTrace();
                        return;
                    }
                    DomainSocketComms<String> sockComms = new DomainSocketComms<String>(channel);
                    new Thread(() -> {
                        while (true)
                        {
                            System.out.println("top of loop");
                            try
                            {
                                System.out.println("reading message");
                                Optional<String> request = sockComms.readSocketMessage();
                                System.out.println("got message");
                                if (request.isPresent())
                                {
                                    System.out.println(request.get());
                                    sockComms.writeSocketMessage("Got " + request.get());
                                }
                                else
                                {
                                    // Connection was closed
                                    break;
                                }
                            }
                            catch (IOException e1)
                            {
                                e1.printStackTrace();
                                System.out.println("exiting due to error " + e1.toString());
                            }

                        }
                    }).start();
                }
            }
            catch (IOException e)
            {
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
                e.printStackTrace();
            }
        }
    }
}
