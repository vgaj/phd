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

package com.github.vgaj.phd.server.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageData implements MessageDataInterface
{
    // TODO: Log4J configuration
    Logger logger = LoggerFactory.getLogger(this.getClass());

    // Maximum number of messages to store
    @Value("${phd.display.message.count}")
    private Integer maxMessagesToShow;

    // Where the next message will go
    private int msgIndex = 0;

    // The ring buffer of messages
    private String[] messages = null;

    public void ensureBufferInitialised()
    {
        if (messages == null)
        {
            messages = new String[maxMessagesToShow];
        }
    }
    
    public void addError(String msg, Throwable t)
    {
        logger.error(msg, t);
        add(msg);
    }

    public void addMessage(String msg)
    {
        logger.info(msg);
        add(msg);
    }

    private void add(String msg)
    {
        ensureBufferInitialised();
        messages[msgIndex] = msg;
        msgIndex = getNext(msgIndex);
    }

    private int getNext(int i)
    {
        return (i == (maxMessagesToShow - 1) ? 0 : i+1);
    }

    public List<String> getMessages()
    {
        ensureBufferInitialised();
        ArrayList<String> results = new ArrayList<>(maxMessagesToShow);
        int i = msgIndex;
        for (int x = 0; x < maxMessagesToShow; x++)
        {
            if (messages[i] != null)
            {
                results.add(messages[i]);
            }
            i = getNext(i);
        }
        return results;
    }
}
