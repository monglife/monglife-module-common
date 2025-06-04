package com.monglife.module.common.logging.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class ArgsUtil {

    private static final ObjectMapper objectMapper;

    static {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        hibernate6Module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(javaTimeModule);
        objectMapper.registerModule(hibernate6Module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);

    }

    /**
     * 로깅을 위한 메서드 파라미터 맵 생성
     */
    public static Map<String, Object> generateArgs(Method method, Object[] args) {

        Map<String, Object> argsMap = new HashMap<>();
        Parameter[] parameters = method.getParameters();

        for (int index = 0; index < parameters.length; index++) {
            String parameterName = parameters[index].getName();
            Object arg = args[index];

            try {
                if (arg == null || cantObjectPackage(arg)) {
                    argsMap.put(parameterName, "");
                } else {
                    objectMapper.writeValueAsString(arg);
                    argsMap.put(parameterName, arg);
                }
            } catch (Exception e) {
                argsMap.put(parameterName, "arg parse failed.");
            }
        }

        return argsMap;
    }

    /**
     * 예외 추적 문자열 생성
     * @param throwable 예외 객체
     * @return 예외 추적 문자열
     */
    public static String generateExceptionTrace(Throwable throwable) {

        StringBuilder exceptionTraceBuilder = new StringBuilder();
        exceptionTraceBuilder.append(throwable.toString());

        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            exceptionTraceBuilder
                    .append("\tat ")
                    .append(stackTraceElement);
        }

        Throwable cause = throwable.getCause();

        while(cause != null) {
            exceptionTraceBuilder
                    .append("Caused by: ")
                    .append(cause);

            for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
                exceptionTraceBuilder
                        .append("\tat ")
                        .append(stackTraceElement);
            }

            cause = cause.getCause();
        }

        return exceptionTraceBuilder.toString();
    }

    /**
     * monglife 패키지 해위 여부 확인
     * @param arg 메서드 파라 미터
     * @return monglife 패키지 하위 여부
     */
    private static boolean cantObjectPackage(Object arg) {
        return arg != null
                && arg.getClass().getPackage() != null
                && !arg.getClass().getPackage().getName().startsWith("com.monglife");
    }
}
