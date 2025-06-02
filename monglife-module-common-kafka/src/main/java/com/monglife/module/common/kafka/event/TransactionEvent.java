package com.monglife.module.common.kafka.event;

import com.monglife.core.utils.CommonUtil;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent<T> {

    private String transactionId;

    private LocalDateTime createdAt;

    private String topic;

    private T data;

    private String className;

    private String dataClassName;

    @Builder
    public TransactionEvent(String transactionId, String topic, T data) {
        this.transactionId = transactionId;
        this.createdAt = LocalDateTime.now();
        this.topic = topic;
        this.data = data;
        this.className = this.getClass().getName();
        this.dataClassName = data.getClass().getName();
    }
}
