package com.github.vgaj.phd.cli;

import com.github.vgaj.phd.ipc.DomainSocketComms;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Optional;

public class PhoneHomeDetectorCli
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        System.out.println("Phone Home Detector Results");
        Path socketPath = Path.of("/tmp", "phone_home_detector_ipc");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);
        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        channel.connect(socketAddress);

        DomainSocketComms<String> sockComms = new DomainSocketComms<String>(channel);

        String message = "abc123";
        System.out.println("Sending: " + message);
        sockComms.writeSocketMessage(message);
        Optional<String> response = sockComms.readSocketMessage();
        System.out.println("Received: " + response.get());

    }
}
