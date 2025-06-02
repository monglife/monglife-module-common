package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.core.utils.CommonUtil;
import com.monglife.module.common.logging.annotation.DisableLogging;
import com.monglife.module.common.logging.utils.ArgsUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Order(Integer.MAX_VALUE)
@Slf4j
@Aspect
@Component
@Profile("!test")
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

    private static final Queue<String> DISABLE_LOGGING_TRAICE_ID_QUEUE = new ConcurrentLinkedDeque<>();

    @Around("endPointPointcut()")
    public Object aroundEndPoint(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = MDC.get("traceId");

        if (traceId == null || traceId.isBlank()) {
            MDC.put("traceId", CommonUtil.randomId());
            traceId = MDC.get("traceId");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (method.isAnnotationPresent(DisableLogging.class)) {
            DISABLE_LOGGING_TRAICE_ID_QUEUE.offer(traceId);
        }

        try {
            return joinPoint.proceed();
        } finally {
            DISABLE_LOGGING_TRAICE_ID_QUEUE.remove(traceId);
            MDC.clear();
        }
    }

    @AfterThrowing(value = "endPointPointcut() && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) throws Throwable {

        String traceId = MDC.get("traceId");

        if (!DISABLE_LOGGING_TRAICE_ID_QUEUE.contains(traceId)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            String clazzName = method.getDeclaringClass().getName();
            String methodName = method.getName();

            String message = exception.getMessage();

            if (exception instanceof ErrorException errorException) {
                message = errorException.getErrorCode() == null ? "" : errorException.getErrorCode().getMessage();
            }

            String error = "\n" +
                    String.format("%-15s : %s\n", "TRACE ID", traceId) +
                    String.format("%-15s : %s#%s\n", "METHOD", clazzName, methodName) +
                    String.format("%-15s : %s\n", "MESSAGE", message) +
                    String.format("%-15s : %s", "STACK TRACE", ArgsUtil.generateExceptionTrace(exception));

            log.error(error);
        }

        throw exception;
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

        if (!DISABLE_LOGGING_TRAICE_ID_QUEUE.contains(traceId)) {
            StringBuilder before = new StringBuilder();
            before.append("\n")
                    .append(String.format("%-15s : %s\n", "TRACE ID", traceId))
                    .append(String.format("%-15s : %s\n", "TRANSACTION", "X"))
                    .append(String.format("%-15s : %s#%s\n", "METHOD", clazzName, methodName))
                    .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateArgs(method, joinPoint.getArgs())));

            if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
                log.info(before.toString());
            } else {
                log.debug(before.toString());
            }
        }

        Object returnValue = joinPoint.proceed();

        if (!DISABLE_LOGGING_TRAICE_ID_QUEUE.contains(traceId)) {
            StringBuilder after = new StringBuilder();
            after.append("\n")
                    .append(String.format("%-15s : %s\n", "TRACE ID", traceId))
                    .append(String.format("%-15s : %s\n", "TRANSACTION", "X"))
                    .append(String.format("%-15s : %s#%s\n", "METHOD", clazzName, methodName))
                    .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateReturnObject(returnValue)));


            if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
                log.info(after.toString());
            } else {
                log.debug(after.toString());
            }
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

        if (!DISABLE_LOGGING_TRAICE_ID_QUEUE.contains(traceId)) {
            StringBuilder before = new StringBuilder();
            before.append("\n")
                    .append(String.format("%-15s : %s\n", "TRACE ID", traceId))
                    .append(String.format("%-15s : %s\n", "TRANSACTION", TransactionSynchronizationManager.getCurrentTransactionName()))
                    .append(String.format("%-15s : %s#%s\n", "METHOD", clazzName, methodName))
                    .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateArgs(method, joinPoint.getArgs())));

            if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
                log.info(before.toString());
            } else {
                log.debug(before.toString());
            }
        }

        Object returnValue = joinPoint.proceed();

        if (!DISABLE_LOGGING_TRAICE_ID_QUEUE.contains(traceId)) {
            StringBuilder after = new StringBuilder();
            after.append("\n")
                    .append(String.format("%-15s : %s\n", "TRACE ID", traceId))
                    .append(String.format("%-15s : %s\n", "TRANSACTION", TransactionSynchronizationManager.getCurrentTransactionName()))
                    .append(String.format("%-15s : %s#%s\n", "METHOD", clazzName, methodName))
                    .append(String.format("%-15s : %s", "ARGS", ArgsUtil.generateReturnObject(returnValue)));


            if (profile != null && !profile.isBlank() && ("dev".equals(profile) || "stg".equals(profile))) {
                log.info(after.toString());
            } else {
                log.debug(after.toString());
            }
        }

        return returnValue;
    }
}
