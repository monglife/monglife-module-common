package com.monglife.module.common.kafka.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.Collection;

@Slf4j
public class KafkaModuleErrorHandler implements CommonErrorHandler {

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
        log.error(thrownException.getMessage(), thrownException);
        Collection<TopicPartition> assignedPartitions = container.getAssignedPartitions();
        consumer.seekToEnd(assignedPartitions);
        consumer.assignment();
    }
}