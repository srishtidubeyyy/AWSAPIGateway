package com.task05.pojos;

import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value;

import java.util.Map;

public class Request {
    private  int principalId;
    private  Map<String, String> content;
    public Request(){

    }

    public Request(int principalId, Map<String, String> content) {
        this.principalId = principalId;
        this.content = content;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public Map<String, String> getContent() {
        return content;
    }
}
