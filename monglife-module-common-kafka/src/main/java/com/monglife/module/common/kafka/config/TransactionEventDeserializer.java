package com.monglife.module.common.kafka.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monglife.module.common.kafka.event.TransactionEvent;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class TransactionEventDeserializer<T> implements Deserializer<TransactionEvent<T>> {

    private final ObjectMapper objectMapper;

    public TransactionEventDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public TransactionEvent<T> deserialize(String topic, byte[] data) {
        try {
            Map<String, Object> map = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
            String dataClassName = (String) map.get("dataClassName");

            if (dataClassName == null) {
                throw new RuntimeException("kafka transactionEvent deserialization failed. not contain dataClassName.");
            }

            return objectMapper.readValue(data, objectMapper.getTypeFactory().constructParametricType(TransactionEvent.class, Class.forName(dataClassName)));

        } catch (Exception e) {
            throw new RuntimeException("kafka transactionEvent deserialization failed. json parse failed.");
        }
    }

    @Override
    public void close() {}
}