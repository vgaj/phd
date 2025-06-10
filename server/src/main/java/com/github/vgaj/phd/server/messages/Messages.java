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

package com.github.vgaj.phd.server.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Messages implements MessageInterface {
    private Logger logger;

    public static Messages getLogger(Class<?> clazz) {
        return new Messages(clazz);
    }

    private Messages(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    // Maximum number of messages to store
    private static int maxMessagesToShow = 100;

    // Where the next message will go
    private static int msgIndex = 0;

    // The ring buffer of messages
    private static String[] messages = new String[maxMessagesToShow];

    public void addError(String msg, Throwable t) {
        logger.error(msg, t);
        add(msg);
    }

    public void addError(String msg) {
        logger.error(msg);
        add(msg);
    }

    public void addMessage(String msg) {
        logger.info(msg);
        add(msg);
    }

    @Override
    public void addDebug(String msg) {
        logger.debug(msg);
        add(msg);
    }

    private static void add(String msg) {
        synchronized (messages) {
            messages[msgIndex] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss ")) + msg;
            msgIndex = getNext(msgIndex);
        }
    }

    private static int getNext(int i) {
        return (i == (maxMessagesToShow - 1) ? 0 : i + 1);
    }

    public static List<String> getMessages() {
        ArrayList<String> results = new ArrayList<>(maxMessagesToShow);
        int i = msgIndex;
        for (int x = 0; x < maxMessagesToShow; x++) {
            if (messages[i] != null) {
                results.add(messages[i]);
            }
            i = getNext(i);
        }
        return results;
    }
}
