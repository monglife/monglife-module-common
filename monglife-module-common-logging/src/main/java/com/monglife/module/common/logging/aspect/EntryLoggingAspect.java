package com.monglife.module.common.logging.aspect;

import com.monglife.module.common.logging.utils.LoggingUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Order(Integer.MAX_VALUE - 1)
@Aspect
@Component
@Profile("!test")
public class EntryLoggingAspect {

    private final LoggingUtil loggingUtil;

    public EntryLoggingAspect(@Autowired LoggingUtil loggingUtil) {
        this.loggingUtil = loggingUtil;
    }

    /**
     * traceId 생성 및 traceOffset 설정
     */
    @Around("@within(com.monglife.module.common.logging.annotation.EntryLoggingPoint) || @annotation(com.monglife.module.common.logging.annotation.EntryLoggingPoint)")
    public Object aroundEntry(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        loggingUtil.setTraceId();
        loggingUtil.setTraceOffset();
        loggingUtil.setEntryMethod(method);

        try {
            return joinPoint.proceed();
        } finally {
            loggingUtil.clear();
        }
    }
}
