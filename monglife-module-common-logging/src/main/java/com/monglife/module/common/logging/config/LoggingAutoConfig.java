package com.monglife.module.common.logging.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({ ObjectMapper.class })
public class LoggingAutoConfig {

    /**
     * 로깅용 objectMapper
     */
    @Bean("LoggingObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        try {
            Class<?> clazz = Class.forName("com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module");
            Object hibernate6Module = clazz.getDeclaredConstructor().newInstance();
            clazz.getMethod("disable", Enum.class).invoke(hibernate6Module, Enum.valueOf((Class<Enum>) Class.forName("com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module$Feature"),"FORCE_LAZY_LOADING"));
            objectMapper.registerModule((Module) hibernate6Module);
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);

        return objectMapper;
    }
}
