package com.monglife.module.common.logging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ArgsUtil {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 로깅을 위한 메서드 파라미터 문자열 생성
     * @param method 메서드
     * @param args 메서드 파라미터
     * @return 메서드 파라미터 문자열
     */
    public static String generateArgs(Method method, Object[] args) {

        StringBuilder argsBuilder = new StringBuilder();
        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            if (args[index] == null) {
                argsBuilder.append("null");
            } else if (args[index].getClass().isPrimitive()) {
                argsBuilder
                        .append("\n")
                        .append(" - ")
                        .append("[")
                        .append(index)
                        .append("] ")
                        .append(parameters[index].getName())
                        .append("<")
                        .append(args[index].getClass().getTypeName())
                        .append("> : ")
                        .append(args[index]);
            } else {
                try {
                    String argJson = objectMapper.writeValueAsString(args[index]);
                    argsBuilder
                            .append("\n")
                            .append(" - ")
                            .append("[")
                            .append(index)
                            .append("] ")
                            .append(parameters[index].getName())
                            .append("<")
                            .append(args[index].getClass().getTypeName())
                            .append("> : ")
                            .append(argJson);
                } catch (JsonProcessingException ignored) {
                    argsBuilder
                            .append("\n")
                            .append(" - ")
                            .append("[")
                            .append(index)
                            .append("] ")
                            .append(parameters[index].getName())
                            .append("<")
                            .append(args[index].getClass().getTypeName())
                            .append("> : ")
                            .append(args[index].toString());
                }
            }

            if (index != parameters.length - 1) argsBuilder.append(", ");
        }

        return argsBuilder.toString();
    }
}
