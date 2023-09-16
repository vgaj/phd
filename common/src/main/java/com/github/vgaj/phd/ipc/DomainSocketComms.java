package com.github.vgaj.phd.ipc;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public class DomainSocketComms <TREAD extends Serializable, TWRITE extends Serializable>
{
    private final SocketChannel channel;
    private final int MAX_MESSAGE_SIZE = 64*1024;
    public DomainSocketComms(SocketChannel channel)
    {
        this.channel = channel;
    }
    public void writeSocketMessage(TWRITE message) throws IOException
    {
        // Serialize to Buffer
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(message);
        oos.flush();
        ByteBuffer buffer = ByteBuffer.wrap(os.toByteArray());
        oos.close();
        os.close();

        while (buffer.hasRemaining())
        {
            channel.write(buffer);
        }
    }

    public Optional<TREAD> readSocketMessage() throws IOException
    {
        // Read from domain socket
        ByteBuffer buffer = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0)
        {
            return Optional.empty();
        }

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);

        InputStream is = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(is);

        // TODO filter

        // Deserialize
        Object message = null;
        try
        {
            message = ois.readObject();
        }
        catch (ClassNotFoundException e)
        {
            // TODO
            e.printStackTrace();
            return Optional.empty();
        }

        ois.close();
        is.close();

        // TODO instanceof
        return Optional.of((TREAD) message);
    }
}
