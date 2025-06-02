package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.module.common.logging.utils.ArgsUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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

    @Pointcut("consumerPointcut() || controllerPointcut() || listenerPointcut() || workerPointcut()")
    private void endPointPointcut() {}

    @Pointcut("consumerPointcut() || controllerPointcut() || listenerPointcut() || workerPointcut() || useCasePointcut() || servicePointcut() || portPointcut() || repositoryPointcut()")
    private void targetPointcut() {}

    @Around("endPointPointcut() || !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)")
    public Object aroundEndPoint(ProceedingJoinPoint joinPoint) throws Throwable {

        if (MDC.get("traceId") == null || MDC.get("traceId").isBlank()) {
            MDC.put("traceId", UUID.randomUUID().toString());
        }

        try {
            return joinPoint.proceed();
        } catch (Exception exception) {
            String traceId = MDC.get("traceId");

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            String clazzName = method.getDeclaringClass().getName();
            String methodName = method.getName();

            String message = exception.getMessage();

            if (exception instanceof ErrorException errorException) {
                message = errorException.getErrorCode() == null ? "" : errorException.getErrorCode().getMessage();
            }

            String error = "\n" +
                    String.format("%-15s : %s", "TRACE ID", traceId) +
                    String.format("%-15s : %s#%s", "METHOD", clazzName, methodName) +
                    String.format("%-15s : %s", "MESSAGE", message) +
                    String.format("%-15s : %s", "STACK TRACE", ArgsUtil.generateExceptionTrace(exception));

            log.error(error);

            throw exception;
        } finally {
            MDC.clear();
        }
    }

    /**
     * None Transactional 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Around("targetPointcut() && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging) && !@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundNoTransactional(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = MDC.get("traceId");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        StringBuilder before = new StringBuilder();
        before.append("\n")
                .append(String.format("%-15s : %s", "TRACE ID", traceId))
                .append(String.format("%-15s : %s", "TRANSACTION", "X"))
                .append(String.format("%-15s : %s#%s", "METHOD", clazzName, methodName))
                .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateArgs(method, joinPoint.getArgs())));

        if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
            log.info(before.toString());
        } else {
            log.debug(before.toString());
        }

        Object returnValue = joinPoint.proceed();

        StringBuilder after = new StringBuilder();
        after.append("\n")
                .append(String.format("%-15s : %s", "TRACE ID", traceId))
                .append(String.format("%-15s : %s", "TRANSACTION", "X"))
                .append(String.format("%-15s : %s#%s", "METHOD", clazzName, methodName))
                .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateReturnObject(returnValue)));


        if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
            log.info(after.toString());
        } else {
            log.debug(after.toString());
        }

        return returnValue;
    }

    /**
     * Transactional 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Around("targetPointcut() && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging) && @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundTransactional(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = MDC.get("traceId");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        StringBuilder before = new StringBuilder();
        before.append("\n")
                .append(String.format("%-15s : %s", "TRACE ID", traceId))
                .append(String.format("%-15s : %s", "TRANSACTION", TransactionSynchronizationManager.getCurrentTransactionName()))
                .append(String.format("%-15s : %s#%s", "METHOD", clazzName, methodName))
                .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateArgs(method, joinPoint.getArgs())));

        if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
            log.info(before.toString());
        } else {
            log.debug(before.toString());
        }

        Object returnValue = joinPoint.proceed();

        StringBuilder after = new StringBuilder();
        after.append("\n")
                .append(String.format("%-15s : %s", "TRACE ID", traceId))
                .append(String.format("%-15s : %s", "TRANSACTION", TransactionSynchronizationManager.getCurrentTransactionName()))
                .append(String.format("%-15s : %s#%s", "METHOD", clazzName, methodName))
                .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateReturnObject(returnValue)));


        if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
            log.info(after.toString());
        } else {
            log.debug(after.toString());
        }

        return returnValue;
    }
}
