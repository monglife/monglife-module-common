package com.monglife.module.common.logging.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggingAspectTest {

    private final ObjectMapper objectMapper;

    public LoggingAspectTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void objectMapperTest() throws JsonProcessingException {
        objectMapper.writeValueAsString(null);
    }
}