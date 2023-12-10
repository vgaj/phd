package com.github.vgaj.phd.server.messages;

import java.util.ArrayList;
import java.util.List;

public interface MessageDataInterface
{
    void addError(String msg, Throwable t);

    void addMessage(String msg);

    List<String> getMessages();
}
