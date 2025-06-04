package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.module.common.logging.dto.ExceptionLogDto;
import com.monglife.module.common.logging.dto.LogDto;
import com.monglife.module.common.logging.dto.NormalLogDto;
import com.monglife.module.common.logging.service.LoggingService;
import com.monglife.module.common.logging.utils.ArgsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Stack;

@Order(Integer.MAX_VALUE)
@Slf4j
@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class TargetLoggingAspect {

    private final LoggingService loggingService;

    /**
     * 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Around("com.monglife.module.common.logging.pointcut.LoggingPointcut.allPointcut() && !com.monglife.module.common.logging.pointcut.LoggingPointcut.loggingModulePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = loggingService.getTraceId();
        int traceOffset = loggingService.getTraceOffset();

        // traceOffset 증가
        loggingService.increaseTraceOffset();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Map<String, Object> args = ArgsUtil.generateArgs(method, joinPoint.getArgs());

        LogDto logDto = null;

        try {
            Object returnValue = joinPoint.proceed();

            if (loggingService.isLoggingMethod(traceId, method)) {
                logDto = NormalLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .className(clazzName)
                        .method(methodName)
                        .args(args)
                        .returnValue(returnValue)
                        .transaction(TransactionSynchronizationManager.getCurrentTransactionName())
                        .build();
            }

            return returnValue;

        } catch (Exception exception) {

            if (loggingService.isLoggingMethod(traceId, method)) {
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
            if (loggingService.isLoggingMethod(traceId, method)) {
                Stack<LogDto> logStack = loggingService.getLogStack(traceId);

                if (logStack != null && logDto != null) {
                    logStack.add(logDto);
                }
            }
        }
    }
}
