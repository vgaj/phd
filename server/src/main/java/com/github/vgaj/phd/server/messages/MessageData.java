package com.github.vgaj.phd.server.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageData
{
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
