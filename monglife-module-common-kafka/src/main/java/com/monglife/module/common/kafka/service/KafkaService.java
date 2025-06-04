package com.monglife.module.common.kafka.service;

import com.monglife.core.utils.CommonUtil;
import com.monglife.module.common.kafka.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.config.activate.on-profile}")
    private String profile;

    /**
     * 이벤트 발생
     * @param topic 이벤트 토픽 (주제)
     * @param data 이벤트 본문
     */
    public <T> void generateEvent(String topic, T data) {

        String traceId = MDC.get("traceId");
        int traceOffset = convertTraceOffset(MDC.get("traceOffset"));

        if (traceId == null || traceId.isBlank()) {
            MDC.put("traceId", CommonUtil.randomId());
            traceId = MDC.get("traceId");
        }

        if (traceOffset < 0) {
            MDC.put("traceOffset", "0");
            traceOffset = Integer.parseInt(MDC.get("traceOffset"));
        }

        kafkaTemplate.send(topic, TransactionEvent.builder()
                .transactionId(traceId)
                .traceOffset(traceOffset)
                .topic(topic)
                .data(data)
                .build());
    }

    /**
     * 이벤트 발생
     * @param topic 이벤트 토픽 (주제)
     * @param data 이벤트 본문
     */
    public <T> void generateEventWithProfile(String topic, T data) {

        String topicWithProfile = (profile != null && !profile.isBlank() ? profile + "." : "unknown.") + topic;

        String traceId = MDC.get("traceId");
        int traceOffset = convertTraceOffset(MDC.get("traceOffset"));

        if (traceId == null || traceId.isBlank()) {
            MDC.put("traceId", CommonUtil.randomId());
            traceId = MDC.get("traceId");
        }

        if (traceOffset < 0) {
            MDC.put("traceOffset", "0");
            traceOffset = Integer.parseInt(MDC.get("traceOffset"));
        }

        kafkaTemplate.send(topicWithProfile, TransactionEvent.builder()
                .transactionId(traceId)
                .traceOffset(traceOffset)
                .topic(topic)
                .data(data)
                .build());
    }

    /**
     * traceOffset 정수 변환
     */
    private int convertTraceOffset(String traceOffset) {
        if (traceOffset.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(traceOffset);
        }

        return -1;
    }
}
