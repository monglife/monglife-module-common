package com.monglife.module.common.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent<T> {

    private String transactionId;

    private Integer traceOffset;

    private LocalDateTime createdAt;

    private String topic;

    private T data;

    private String className;

    private String dataClassName;

    @Builder
    public TransactionEvent(String transactionId, Integer traceOffset, String topic, T data) {
        this.transactionId = transactionId;
        this.traceOffset = traceOffset;
        this.createdAt = LocalDateTime.now();
        this.topic = topic;
        this.data = data;
        this.className = this.getClass().getName();
        this.dataClassName = data.getClass().getName();
    }
}
