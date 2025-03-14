package com.monglife.module.common.kafka.service;

import com.monglife.module.common.kafka.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 이벤트 발생
     * @param topic 이벤트 토픽 (주제)
     * @param data 이벤트 본문
     */
    public <T> void generateEvent(String topic, T data) {
        kafkaTemplate.send(topic, TransactionEvent.builder()
                .topic(topic)
                .data(data)
                .build());
    }
}
