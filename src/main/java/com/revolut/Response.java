package com.revolut;

import com.google.gson.JsonElement;

public class Response {
    private StatusResponse status;
    private String message;
    private JsonElement data;

    public Response(StatusResponse status) {
        this.status = status;
    }

    public Response(StatusResponse status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(StatusResponse status, JsonElement data) {
        this.status = status;
        this.data = data;
    }

    public StatusResponse getStatus() {
        return status;
    }

    public void setStatus(StatusResponse status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Response{");
        sb.append("status=").append(status);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}