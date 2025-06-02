package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.module.common.logging.utils.ArgsUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Value("${spring.config.activate.on-profile}")
    private String profile;

    @Pointcut("execution(* com.monglife..*Consumer.*(..))")
    private void consumerPointcut() {}

    @Pointcut("execution(* com.monglife..*Controller.*(..))")
    private void controllerPointcut() {}

    @Pointcut("execution(* com.monglife..*Worker.*(..))")
    private void workerPointcut() {}

    @Pointcut("execution(* com.monglife..*Listener.*(..))")
    private void listenerPointcut() {}

    @Pointcut("execution(* com.monglife..*UseCase.*(..))")
    private void useCasePointcut() {}

    @Pointcut("execution(* com.monglife..*Service.*(..))")
    private void servicePointcut() {}

    @Pointcut("execution(* com.monglife..*Port.*(..))")
    private void portPointcut() {}

    @Pointcut("execution(* com.monglife..*Repository.*(..))")
    private void repositoryPointcut() {}

    @Pointcut("consumerPointcut() || controllerPointcut() || listenerPointcut()")
    private void endPointPointcut() {}

    @Pointcut("consumerPointcut() || controllerPointcut() || listenerPointcut() || useCasePointcut() || servicePointcut() || portPointcut() || repositoryPointcut()")
    private void targetPointcut() {}

    @Around("endPointPointcut()")
    public Object aroundEndPoint(ProceedingJoinPoint joinPoint) throws Throwable {

        if (MDC.get("traceId") == null || MDC.get("traceId").isBlank()) {
            MDC.put("traceId", UUID.randomUUID().toString());
        }

        try {
            return joinPoint.proceed();
        } finally {
            MDC.clear();
        }
    }

    /**
     * None Transactional 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Before("targetPointcut() && !@annotation(org.springframework.transaction.annotation.Transactional) && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)")
    public void beforeNoTransactional(JoinPoint joinPoint) {

        String traceId = MDC.get("traceId");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
            log.info("[{}] <TRANSACTION: X> {}#{} {}", traceId, clazzName, methodName, ArgsUtil.generateArgs(method, joinPoint.getArgs()));
        } else {
            log.debug("[{}] <TRANSACTION: X> {}#{} {}", traceId, clazzName, methodName, ArgsUtil.generateArgs(method, joinPoint.getArgs()));
        }
    }

    /**
     * Transactional 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Before("targetPointcut() && @annotation(org.springframework.transaction.annotation.Transactional) && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)")
    public void beforeTransactional(JoinPoint joinPoint) {

        String traceId = MDC.get("traceId");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
            log.info("[{}] <TRANSACTION: {}> {}#{} {}", traceId, TransactionSynchronizationManager.getCurrentTransactionName(), clazzName, methodName, ArgsUtil.generateArgs(method, joinPoint.getArgs()));
        } else {
            log.debug("[{}] <TRANSACTION: {}> {}#{} {}", traceId, TransactionSynchronizationManager.getCurrentTransactionName(), clazzName, methodName, ArgsUtil.generateArgs(method, joinPoint.getArgs()));
        }
    }

    /**
     * 예외 발생 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     * @param exception 발생 예외
     */
    @AfterThrowing(value = "targetPointcut()", throwing = "exception")
    public void afterThrowingException(JoinPoint joinPoint, Exception exception) {

        String traceId = MDC.get("traceId");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        String message = exception.getMessage();

        if (exception instanceof ErrorException errorException) {
            message = errorException.getErrorCode() == null ? "" : errorException.getErrorCode().getMessage();
        }

        log.error("[{}] <THROW> {}#{} {} => {}\n{}", traceId, clazzName, methodName, exception, message, exception.getStackTrace());
    }
}
