/*
MIT License

Copyright (c) 2022-2025 Viru Gajanayake

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

package com.github.vgaj.phd.common.ipc;

import com.github.vgaj.phd.common.query.IpcMessage;

import java.io.*;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.ArrayList;

public class DomainSocketComms implements AutoCloseable {
    public static final Path SOCKET_PATH = Path.of("/tmp", "phone_home_detector_ipc");

    private final SocketChannel channel;

    public DomainSocketComms(SocketChannel channel) {
        this.channel = channel;
    }

    public void writeSocketMessage(IpcMessage message) throws IOException {
        // Serialize to Buffer
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(message);
        oos.flush();
        byte[] byteArray = os.toByteArray();
        oos.close();
        os.close();

        int size = byteArray.length;
        ByteBuffer buffer = ByteBuffer.allocate(4 + size);
        buffer.putInt(size);
        buffer.put(byteArray);
        buffer.flip();


        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public <T extends IpcMessage> T readSocketMessage(
            Class<T> READ_TYPE) throws IOException {
        // Read the size
        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        int bytesRead = channel.read(sizeBuffer);
        sizeBuffer.flip();
        if (bytesRead < 0) {
            return null;
        }
        int messageSize = sizeBuffer.getInt();

        // Read from domain socket
        ByteBuffer messageBuffer = ByteBuffer.allocate(messageSize);
        bytesRead = channel.read(messageBuffer);
        if (bytesRead < 0) {
            return null;
        }

        byte[] bytes = new byte[bytesRead];
        messageBuffer.flip();
        messageBuffer.get(bytes);

        InputStream is = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(is);
        ObjectInputFilter filter = ObjectInputFilter.allowFilter(
                cl ->
                {
                    // We will allow the read message to be built up of
                    // - anything from the same package
                    // - An ArrayList and underlying array
                    // - String
                    return cl.getPackage() == READ_TYPE.getPackage()
                            || cl.isArray()
                            || cl == InetAddress.class
                            || cl == Inet4Address.class
                            || cl == Inet6Address.class
                            || cl == ArrayList.class
                            || cl == String.class;
                },
                ObjectInputFilter.Status.REJECTED);
        ois.setObjectInputFilter(filter);

        // Deserialize
        Object message = null;
        try {
            message = ois.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        }

        ois.close();
        is.close();

        if (READ_TYPE.isInstance(message)) {
            return READ_TYPE.cast(message);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        if (channel != null) {
            channel.close();
        }
    }
}
