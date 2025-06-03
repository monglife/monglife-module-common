package com.monglife.module.common.kafka.aspect;

import com.monglife.module.common.kafka.event.TransactionEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(Integer.MAX_VALUE - 1)
@Aspect
@Component
@Profile("!test")
public class KafkaListenerAspect {

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof TransactionEvent<?> event) {
                MDC.put("traceId", event.getTransactionId());
            }
        }

        return joinPoint.proceed();
    }
}
