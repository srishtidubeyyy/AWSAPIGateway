package com.task05.pojos;

import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value;

import java.time.LocalDateTime;
import java.util.Map;

public class Event {
    private final String id;
    private final int principalId;
    private final LocalDateTime localDateTime;
    private final Map<String, String> body;

    public String getId() {
        return id;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public Event(String id, int principalId, LocalDateTime localDateTime, Map<String, String> body) {
        this.id = id;
        this.principalId = principalId;
        this.localDateTime = localDateTime;
        this.body = body;
    }
}
