package com.monglife.module.common.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.module.common.kafka.event.TransactionEvent;
import com.monglife.module.common.kafka.exception.KafkaModuleErrorHandler;
import com.monglife.module.common.kafka.property.KafkaModuleProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@AutoConfiguration
@ConditionalOnClass({ KafkaTemplate.class })
@AutoConfigureBefore({ KafkaAutoConfiguration.class })
@EnableConfigurationProperties({ KafkaModuleProperties.class })
public class KafkaAutoConfig {

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnMissingBean(CommonErrorHandler.class)
    public CommonErrorHandler kafkaModuleErrorHandler() {
        return new KafkaModuleErrorHandler();
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, TransactionEvent<Object>> kafkaConsumerFactory(KafkaModuleProperties kafkaModuleProperties) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaModuleProperties.getUrl());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaModuleProperties.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, TransactionEventDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaModuleProperties.getAutoOffsetResetConfig());
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, kafkaModuleProperties.getIsolationLevelConfig());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.monglife.module.common.kafka.event");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new TransactionEventDeserializer<>(objectMapper));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionEvent<Object>> kafkaListenerContainerFactory(
            @Qualifier("kafkaConsumerFactory") DefaultKafkaConsumerFactory<String, TransactionEvent<Object>> consumerFactory,
            @Autowired CommonErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TransactionEvent<Object>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactory(KafkaModuleProperties kafkaModuleProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaModuleProperties.getUrl());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
