package com.teender.crush.data;

import java.util.Map;

public class ChatSession {
    Long timestamp;
    Map<String, Boolean> users;

    public Long getTimestamp() {
        return timestamp;
    }

    public Map<String, Boolean> getUsers() {
        return users;
    }
}
