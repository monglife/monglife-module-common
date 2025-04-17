package com.monglife.module.common.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.module.common.kafka.event.TransactionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEventDeserializerTest {

    private final TransactionEventDeserializer<TestPayload> deserializer;
    private final JsonDeserializer<TransactionEvent<TestPayload>> jsonDeserializer;

    public TransactionEventDeserializerTest() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.deserializer = new TransactionEventDeserializer<>(objectMapper);
        this.jsonDeserializer = new JsonDeserializer<>(TransactionEvent.class);
    }

    @Nested
    @DisplayName("트랜잭션 이벤트 JSON 문자열 DTO 역직렬화 단위 테스트")
    class Deserialize {

        @Test
        @DisplayName("사용자 정의 역직렬화 클래스로 역직렬화를 진행 한다.")
        void transactionEventDeserializer() {
            // arrange
            String topic = "commit.test";
            byte[] data = "{\"transactionId\":\"db7543402790475d85f56affd825be21\",\"createdAt\":[2025,4,17,9,50,13,251111000],\"topic\":\"commit.exchangeStarPoint\",\"data\":{\"id\":1,\"message\":\"test message!\"},\"className\":\"com.monglife.module.common.kafka.event.TransactionEvent\",\"dataClassName\":\"com.monglife.module.common.kafka.config.TestPayload\"}".getBytes(StandardCharsets.UTF_8);

            // act
            TransactionEvent<TestPayload> expected = deserializer.deserialize(topic, data);

            // assert
            assertDoesNotThrow(() -> expected.getData().getClass());
            assertNotNull(expected.getData());
            assertEquals(expected.getData().getClass().getName(), TestPayload.class.getName());
        }

        @Test
        @DisplayName("Kafka JsonDeSerializer 로 역직렬화를 진행하는 경우 LinkedList 캐스팅 예외가 발생 한다.")
        void jsonDeSerializer() {
            // arrange
            String topic = "commit.test";
            byte[] data = "{\"transactionId\":\"db7543402790475d85f56affd825be21\",\"createdAt\":[2025,4,17,9,50,13,251111000],\"topic\":\"commit.exchangeStarPoint\",\"data\":{\"id\":1,\"message\":\"test message!\"},\"className\":\"com.monglife.module.common.kafka.event.TransactionEvent\",\"dataClassName\":\"com.monglife.module.common.kafka.config.TestPayload\"}".getBytes(StandardCharsets.UTF_8);

            // act
            TransactionEvent<TestPayload> expected = jsonDeserializer.deserialize(topic, data);

            // assert
            assertThrows(ClassCastException.class, () -> expected.getData().getClass());
        }

    }
}