package com.task05.pojos;

public class Response {
    private final int statusCode;
    private final Event event;

    public Response(int statusCode, Event event) {
        this.statusCode = statusCode;
        this.event = event;
    }

    public int  getStatusCode() {
        return statusCode;
    }


}
