package com.monglife.module.common.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.core.exception.ErrorException;
import com.monglife.core.utils.CommonUtil;
import com.monglife.module.common.logging.annotation.DisableLogging;
import com.monglife.module.common.logging.dto.ExceptionLogDto;
import com.monglife.module.common.logging.dto.LogDto;
import com.monglife.module.common.logging.dto.NotTransactionLogDto;
import com.monglife.module.common.logging.dto.TransactionLogDto;
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
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@Order(Integer.MAX_VALUE)
@Slf4j
@Aspect
@Component
@Profile("!test")
public class LoggingAspect {

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

    @Value("${spring.config.activate.on-profile}")
    private String profile;

    private static final Map<String, Stack<LogDto>> LOG_QUEUE_MAP = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public LoggingAspect() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Around("endPointPointcut()")
    public Object aroundEndPoint(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = MDC.get("traceId");

        if (traceId == null || traceId.isBlank()) {
            MDC.put("traceId", CommonUtil.randomId());
            traceId = MDC.get("traceId");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // DisableLogging 어노테이션이 없는 경우 맵에 삽입
        if (!method.isAnnotationPresent(DisableLogging.class)) {
            LOG_QUEUE_MAP.put(traceId, new Stack<>());
        }

        try {
            return joinPoint.proceed();
        } finally {
            Stack<LogDto> logStack = LOG_QUEUE_MAP.get(traceId);

            if (logStack != null) {
                while (!logStack.isEmpty()) {
                    LogDto logDto = logStack.pop();
                    if (logDto instanceof ExceptionLogDto exceptionLogDto) {
                        log.error(objectMapper.writeValueAsString(exceptionLogDto));
                    } else {
                        String logDtoJson = objectMapper.writeValueAsString(logDto);

                        if (profile != null && !profile.isBlank() && "prd".equals(profile)) {
                            log.debug(logDtoJson);
                        } else {
                            log.info(logDtoJson);
                        }
                    }
                }
            }

            LOG_QUEUE_MAP.remove(traceId);
            MDC.clear();
        }
    }

    @AfterThrowing(value = "endPointPointcut() && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) throws Throwable {

        String traceId = MDC.get("traceId");

        // 로그 수집이 필요한 경우
        if (LOG_QUEUE_MAP.containsKey(traceId)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            String clazzName = method.getDeclaringClass().getName();
            String methodName = method.getName();

            String message = exception.getMessage();

            if (exception instanceof ErrorException errorException) {
                message = errorException.getErrorCode() == null ? "" : errorException.getErrorCode().getMessage();
            }

            Stack<LogDto> logStack = LOG_QUEUE_MAP.get(traceId);

            if (logStack != null) {
                logStack.add(ExceptionLogDto.builder()
                        .traceId(traceId)
                        .method(String.format("%s#%s", clazzName, methodName))
                        .message(message)
                        .stackTrace(ArgsUtil.generateExceptionTrace(exception))
                        .build());
            }
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

        Object returnValue = joinPoint.proceed();

        // 로그 수집이 필요한 경우
        if (LOG_QUEUE_MAP.containsKey(traceId)) {
            NotTransactionLogDto notTransactionLogDto = NotTransactionLogDto.builder()
                    .traceId(traceId)
                    .method(String.format("%s#%s", clazzName, methodName))
                    .args(ArgsUtil.generateArgs(method, joinPoint.getArgs()))
                    .returnValue(returnValue)
                    .build();

            Stack<LogDto> logStack = LOG_QUEUE_MAP.get(traceId);

            if (logStack != null) {
                logStack.add(notTransactionLogDto);
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

        Object returnValue = joinPoint.proceed();

        if (LOG_QUEUE_MAP.containsKey(traceId)) {
            TransactionLogDto transactionLogDto = TransactionLogDto.builder()
                    .traceId(traceId)
                    .method(String.format("%s#%s", clazzName, methodName))
                    .args(ArgsUtil.generateArgs(method, joinPoint.getArgs()))
                    .returnValue(returnValue)
                    .transaction(TransactionSynchronizationManager.getCurrentTransactionName())
                    .build();

            Stack<LogDto> logStack = LOG_QUEUE_MAP.get(traceId);

            if (logStack != null) {
                logStack.add(transactionLogDto);
            }
        }

        return returnValue;
    }
}
