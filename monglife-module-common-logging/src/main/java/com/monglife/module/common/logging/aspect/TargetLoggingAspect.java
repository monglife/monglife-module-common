package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.module.common.logging.dto.ExceptionLogDto;
import com.monglife.module.common.logging.dto.LogDto;
import com.monglife.module.common.logging.dto.NormalLogDto;
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
import java.util.Stack;

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

        // traceOffset 증가
        loggingUtil.increaseTraceOffset();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Map<String, Object> args = ArgsUtil.generateArgs(method, joinPoint.getArgs());

        try {
            Object returnValue = joinPoint.proceed();

            if (loggingUtil.isLoggingMethod(traceId, method)) {
                NormalLogDto normalLogDto = NormalLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .className(clazzName)
                        .method(methodName)
                        .args(args)
                        .returnValue(returnValue)
                        .transaction(TransactionSynchronizationManager.getCurrentTransactionName())
                        .build();
                Stack<LogDto> logStack = loggingUtil.getLogStack(traceId);

                if (logStack != null && normalLogDto != null) {
                    logStack.add(normalLogDto);
                }
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
                        .className(clazzName)
                        .method(methodName)
                        .args(args)
                        .message(message)
                        .stackTrace(ArgsUtil.generateExceptionTrace(exception))
                        .build();

                Stack<LogDto> logStack = loggingUtil.getLogStack(traceId);

                if (logStack != null && exceptionLogDto != null) {
                    logStack.add(exceptionLogDto);
                }
            }

            throw exception;

        }
    }
}
