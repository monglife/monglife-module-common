package com.monglife.module.common.kafka.config;

public class TestPayload {

    private Long id;

    private String message;

    public TestPayload() {
    }

    public TestPayload(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TestPayload{" +
                "id=" + id +
                ", message='" + message + '\'' +
                '}';
    }
}
