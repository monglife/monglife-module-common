package com.monglife.module.common.logging.aspect;

import com.monglife.core.exception.ErrorException;
import com.monglife.module.common.logging.dto.ExceptionLogDto;
import com.monglife.module.common.logging.utils.ArgsUtil;
import com.monglife.module.common.logging.utils.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Order(Integer.MAX_VALUE - 1)
@Aspect
@Component
@Profile("!test")
public class EntryLoggingAspect {

    private final ArgsUtil argsUtil;

    private final LoggingUtil loggingUtil;

    @Autowired
    public EntryLoggingAspect(ArgsUtil argsUtil, LoggingUtil loggingUtil) {
        this.argsUtil = argsUtil;
        this.loggingUtil = loggingUtil;
    }

    /**
     * traceId 생성 및 traceOffset 설정
     */
    @Around("@within(com.monglife.module.common.logging.annotation.EntryLoggingPoint) || @annotation(com.monglife.module.common.logging.annotation.EntryLoggingPoint)")
    public Object aroundEntry(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String traceId = loggingUtil.setTraceId();
        int traceOffset = loggingUtil.setTraceOffset();
        String entryMethod = loggingUtil.setEntryMethod(method);

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        try {
            return joinPoint.proceed();
        } catch (ErrorException exception) {
            if (loggingUtil.isLoggingMethod(traceId, method)) {
                String message = exception.getErrorCode() == null ? "" : exception.getErrorCode().getMessage();

                ExceptionLogDto exceptionLogDto = ExceptionLogDto.builder()
                        .traceId(traceId)
                        .traceOffset(traceOffset)
                        .entryMethod(entryMethod)
                        .className(clazzName)
                        .method(methodName)
                        .message(message)
                        .stackTrace(argsUtil.generateExceptionTrace(exception))
                        .build();

                log.info(loggingUtil.parseJson(exceptionLogDto));
            }

            throw exception;

        } catch (Exception exception) {
            if (loggingUtil.isLoggingMethod(traceId, method)) {
                String message = exception.getMessage();

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

        } finally {
            loggingUtil.clear();
        }
    }
}
