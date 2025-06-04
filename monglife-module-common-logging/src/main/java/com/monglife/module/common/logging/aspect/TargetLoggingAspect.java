package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.module.common.logging.dto.ExceptionLogDto;
import com.monglife.module.common.logging.dto.MethodReturnLogDto;
import com.monglife.module.common.logging.dto.MethodCallDto;
import com.monglife.module.common.logging.utils.ArgsUtil;
import com.monglife.module.common.logging.utils.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class TargetLoggingAspect {

    private final LoggingUtil loggingUtil;

    /**
     * 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Around("com.monglife.module.common.logging.pointcut.LoggingPointcut.allPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = loggingUtil.getTraceId();
        int traceOffset = loggingUtil.getTraceOffset();
        String entryMethod = loggingUtil.getEntryMethod();

        // traceOffset 증가
        loggingUtil.increaseTraceOffset();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Map<String, Object> args = ArgsUtil.generateArgs(method, joinPoint.getArgs());

        try {
            if (loggingUtil.isLoggingMethod(traceId, method)) {
                MethodCallDto methodCallDto = MethodCallDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .entryMethod(entryMethod)
                        .className(clazzName)
                        .method(methodName)
                        .args(args)
                        .transaction(TransactionSynchronizationManager.getCurrentTransactionName())
                        .build();

                log.info(loggingUtil.parseJson(methodCallDto));
            }

            Object returnValue = joinPoint.proceed();

            if (loggingUtil.isLoggingMethod(traceId, method)) {
                MethodReturnLogDto methodReturnLogDto = MethodReturnLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .entryMethod(entryMethod)
                        .className(clazzName)
                        .method(methodName)
                        .returnValue(returnValue)
                        .transaction(TransactionSynchronizationManager.getCurrentTransactionName())
                        .build();

                log.info(loggingUtil.parseJson(methodReturnLogDto));
            }

            return returnValue;

        } catch (Exception exception) {

            if (loggingUtil.isLoggingMethod(traceId, method)) {
                String message = exception.getMessage();

                if (exception instanceof ErrorException errorException) {
                    message = errorException.getErrorCode() == null ? "" : errorException.getErrorCode().getMessage();
                }

                ExceptionLogDto exceptionLogDto = ExceptionLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .entryMethod(entryMethod)
                        .className(clazzName)
                        .method(methodName)
                        .message(message)
                        .stackTrace(ArgsUtil.generateExceptionTrace(exception))
                        .build();

                log.error(loggingUtil.parseJson(exceptionLogDto));
            }

            throw exception;

        }
    }
}
