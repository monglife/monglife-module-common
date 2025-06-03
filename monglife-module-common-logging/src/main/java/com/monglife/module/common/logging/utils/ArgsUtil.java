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

            if (arg == null || isMonglifePackageObject(arg)) {
                argsMap.put(parameterName, "");
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

    private static boolean isMonglifePackageObject(Object arg) {
        return arg != null
                && arg.getClass().getPackage() != null
                && arg.getClass().getPackage().getName().startsWith("com.monglife")
                && !arg.getClass().getPackage().getName().contains("org.springframework.data.redis");
    }
}
