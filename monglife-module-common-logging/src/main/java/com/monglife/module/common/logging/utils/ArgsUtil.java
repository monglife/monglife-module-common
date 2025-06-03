package com.monglife.module.common.logging.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class ArgsUtil {

    /**
     * 로깅을 위한 메서드 파라미터 맵 생성
     */
    public static Map<String, Object> generateArgs(Method method, Object[] args) {

        Map<String, Object> argsMap = new HashMap<>();
        Parameter[] parameters = method.getParameters();

        for (int index = 0; index < parameters.length; index++) {
            String parameterName = parameters[index].getName();
            Object arg = args[index];

            if (arg == null) {
                argsMap.put(parameterName, "null");
            } else {
                argsMap.put(parameterName, arg);
            }
        }

        return argsMap;
    }

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
                    .append(cause.toString());

            for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
                exceptionTraceBuilder
                        .append("\tat ")
                        .append(stackTraceElement);
            }

            cause = cause.getCause();
        }

        return exceptionTraceBuilder.toString();
    }
}
