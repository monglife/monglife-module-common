package com.monglife.module.common.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.core.exception.ErrorException;
import com.monglife.core.utils.CommonUtil;
import com.monglife.module.common.logging.annotation.DisableLogging;
import com.monglife.module.common.logging.annotation.DisableLoggingCascade;
import com.monglife.module.common.logging.dto.ExceptionLogDto;
import com.monglife.module.common.logging.dto.LogDto;
import com.monglife.module.common.logging.dto.NotTransactionLogDto;
import com.monglife.module.common.logging.dto.TransactionLogDto;
import com.monglife.module.common.logging.utils.ArgsUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    @Pointcut("execution(public * com.monglife..model.*.*(..))")
    private void domainPointcut() {}

    @Pointcut("execution(* com.monglife..*Port.*(..))")
    private void portPointcut() {}

    @Pointcut("execution(* com.monglife..*Repository.*(..))")
    private void repositoryPointcut() {}

    @Pointcut("consumerPointcut() || controllerPointcut() || listenerPointcut() || workerPointcut()")
    private void entryPointcut() {}

    @Pointcut("useCasePointcut() || servicePointcut() || domainPointcut() || portPointcut() || repositoryPointcut()")
    private void businessPointcut() {}

    @Pointcut("entryPointcut() || businessPointcut()")
    private void targetPointcut() {}

    @Value("${spring.config.activate.on-profile}")
    private String profile;

    private static final Map<String, Stack<LogDto>> LOG_QUEUE_MAP = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public LoggingAspect() {
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
    @Around("entryPointcut()")
    public Object aroundEntry(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = MDC.get("traceId");

        if (traceId == null || traceId.isBlank()) {
            MDC.put("traceId", CommonUtil.randomId());
            traceId = MDC.get("traceId");
        }

        if (convertTraceOffset(MDC.get("traceOffset")) == Integer.MIN_VALUE) {
            MDC.put("traceOffset", "0");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 로깅 제외 Entry layer 메서드
        if (!method.isAnnotationPresent(DisableLoggingCascade.class)) {
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

    /**
     * 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Around("targetPointcut() && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = MDC.get("traceId");
        int traceOffset = convertTraceOffset(MDC.get("traceOffset"));

        // traceOffset 증가
        if (traceOffset != Integer.MIN_VALUE) {
            MDC.put("traceOffset", String.valueOf(traceOffset + 1));
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Map<String, Object> args = ArgsUtil.generateArgs(method, joinPoint.getArgs());

        LogDto logDto = null;

        try {
            Object returnValue = joinPoint.proceed();

            if (isLoggingMethod(traceId, method)) {
                if (!method.isAnnotationPresent(Transactional.class)) {
                    // 논 트랜 잭션 메서드
                    logDto = NotTransactionLogDto.builder()
                            .traceId(traceId)
                            .traceOffset(traceOffset)
                            .className(clazzName)
                            .method(methodName)
                            .args(args)
                            .returnValue(returnValue)
                            .build();
                } else {
                    // 트랜 잭션 메서드
                    logDto = TransactionLogDto.builder()
                            .traceId(traceId)
                            .traceOffset(traceOffset)
                            .className(clazzName)
                            .method(methodName)
                            .args(args)
                            .returnValue(returnValue)
                            .transaction(TransactionSynchronizationManager.getCurrentTransactionName())
                            .build();
                }
            }

            return returnValue;

        } catch (Exception exception) {

            if (isLoggingMethod(traceId, method)) {
                String message = exception.getMessage();

                if (exception instanceof ErrorException errorException) {
                    message = errorException.getErrorCode() == null ? "" : errorException.getErrorCode().getMessage();
                }

                logDto = ExceptionLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .className(clazzName)
                        .method(methodName)
                        .args(args)
                        .message(message)
                        .stackTrace(ArgsUtil.generateExceptionTrace(exception))
                        .build();
            }

            throw exception;

        } finally {
            if (LOG_QUEUE_MAP.containsKey(traceId) && !method.isAnnotationPresent(DisableLogging.class)) {
                Stack<LogDto> logStack = LOG_QUEUE_MAP.get(traceId);

                if (logStack != null && logDto != null) {
                    logStack.add(logDto);
                }
            }
        }
    }

    /**
     * 로깅 필요 메서드 여부
     */
    private boolean isLoggingMethod(String traceId, Method method) {
        return LOG_QUEUE_MAP.containsKey(traceId) && !method.isAnnotationPresent(DisableLogging.class);
    }

    /**
     * traceOffset 정수 변환
     */
    private int convertTraceOffset(String traceOffset) {
        if (traceOffset != null && !traceOffset.isBlank() && traceOffset.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(traceOffset);
        }

        return Integer.MIN_VALUE;
    }
}
