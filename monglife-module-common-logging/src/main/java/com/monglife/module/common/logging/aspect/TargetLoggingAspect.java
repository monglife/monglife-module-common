package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.module.common.logging.dto.ExceptionLogDto;
import com.monglife.module.common.logging.dto.MethodCallDto;
import com.monglife.module.common.logging.dto.MethodReturnLogDto;
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

@Slf4j
@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class TargetLoggingAspect {

    private final ArgsUtil argsUtil;

    private final LoggingUtil loggingUtil;

    /**
     * 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Around("com.monglife.module.common.logging.pointcut.LoggingPointcut.allPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // traceOffset 증가
        loggingUtil.increaseTraceOffset();

        String traceId = loggingUtil.getTraceId();
        int traceOffset = loggingUtil.getTraceOffset();
        String entryMethod = loggingUtil.getEntryMethod();

        try {
            if (loggingUtil.isLoggingMethod(method)) {

                String clazzName = method.getDeclaringClass().getName();
                String methodName = method.getName();

                MethodCallDto methodCallDto = MethodCallDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .entryMethod(entryMethod)
                        .className(clazzName)
                        .method(methodName)
                        .args(argsUtil.generateArgs(method, joinPoint.getArgs()))
                        .transaction(TransactionSynchronizationManager.getCurrentTransactionName())
                        .build();

                log.info(loggingUtil.parseJson(methodCallDto));

                Object returnValue = joinPoint.proceed();

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

                return returnValue;

            } else {
                return joinPoint.proceed();
            }

        } catch (ErrorException exception) {
            if (loggingUtil.isLoggingMethod(method)) {
                String message = exception.getErrorCode() == null ? "" : exception.getErrorCode().getMessage();

                String clazzName = "";
                String methodName = "";

                StackTraceElement[] stackTraceElements = exception.getStackTrace();
                if (stackTraceElements.length > 0) {
                    clazzName = stackTraceElements[0].getClassName();
                    methodName = stackTraceElements[0].getMethodName();
                }

                ExceptionLogDto exceptionLogDto = ExceptionLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .entryMethod(entryMethod)
                        .className(clazzName)
                        .method(methodName)
                        .message(message)
                        .stackTrace(argsUtil.generateExceptionTrace(exception, 1))
                        .build();

                log.info(loggingUtil.parseJson(exceptionLogDto));
            }

            throw exception;

        } catch (Exception exception) {
            if (loggingUtil.isLoggingMethod(method)) {
                String message = exception.getMessage();

                String clazzName = "";
                String methodName = "";

                StackTraceElement[] stackTraceElements = exception.getStackTrace();
                if (stackTraceElements.length > 0) {
                    clazzName = stackTraceElements[0].getClassName();
                    methodName = stackTraceElements[0].getMethodName();
                }

                ExceptionLogDto exceptionLogDto = ExceptionLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .entryMethod(entryMethod)
                        .className(clazzName)
                        .method(methodName)
                        .message(message)
                        .stackTrace(argsUtil.generateExceptionTrace(exception))
                        .build();

                log.error(loggingUtil.parseJson(exceptionLogDto));
            }

            throw exception;
        }
    }
}
