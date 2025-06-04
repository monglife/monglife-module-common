package com.monglife.module.common.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.module.common.logging.utils.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(Integer.MIN_VALUE + 1)
@Slf4j
@Aspect
@Component
@Profile("!test")
public class EntryLoggingAspect {

    @Value("${spring.config.activate.on-profile}")
    private String profile;

    private final LoggingUtil loggingUtil;

    private final ObjectMapper objectMapper;

    public EntryLoggingAspect(@Autowired LoggingUtil loggingUtil) {
        this.loggingUtil = loggingUtil;

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        hibernate6Module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(javaTimeModule);
        this.objectMapper.registerModule(hibernate6Module);
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        this.objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
    }

    /**
     * traceId 생성 및 traceOffset 설정
     */
    @Around("@within(com.monglife.module.common.logging.annotation.EntryLoggingPoint) || @annotation(com.monglife.module.common.logging.annotation.EntryLoggingPoint)")
    public Object aroundEntry(ProceedingJoinPoint joinPoint) throws Throwable {

        loggingUtil.getTraceIdOrReset();
        loggingUtil.getTraceOffsetOrReset();

        try {
            return joinPoint.proceed();
        } finally {
            loggingUtil.clear();
        }
    }
}
