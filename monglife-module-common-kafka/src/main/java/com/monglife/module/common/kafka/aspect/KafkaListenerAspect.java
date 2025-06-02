package com.monglife.module.common.kafka.aspect;

import com.monglife.module.common.kafka.event.TransactionEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("!test")
public class KafkaListenerAspect {

    @Order(Integer.MIN_VALUE)
    @Around("@annotation(kafkaListener)")
    public Object around(ProceedingJoinPoint joinPoint, KafkaListener kafkaListener) throws Throwable {

        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof TransactionEvent<?> event) {
                MDC.put("traceId", event.getTransactionId());
            }
        }

        return joinPoint.proceed();
    }
}
