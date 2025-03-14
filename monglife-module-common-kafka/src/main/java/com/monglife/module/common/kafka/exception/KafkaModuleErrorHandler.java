package com.monglife.module.common.kafka.exception;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.Collection;

public class KafkaModuleErrorHandler implements CommonErrorHandler {

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
        Collection<TopicPartition> assignedPartitions = container.getAssignedPartitions();
        consumer.seekToEnd(assignedPartitions);
        consumer.assignment();
    }
}