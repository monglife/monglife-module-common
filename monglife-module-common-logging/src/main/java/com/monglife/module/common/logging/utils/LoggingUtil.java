package com.monglife.module.common.logging.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.core.utils.CommonUtil;
import com.monglife.module.common.logging.annotation.DisableLogging;
import com.monglife.module.common.logging.dto.LogDto;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class LoggingUtil {

    private final ObjectMapper objectMapper;

    public LoggingUtil() {
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
     * 로깅 필요 메서드 여부
     */
    public boolean isLoggingMethod(String traceId, Method method) {
        return traceId != null && !method.isAnnotationPresent(DisableLogging.class);
    }

    /**
     * 로그 추적 ID 초기화
     */
    public void setTraceId() {

        String traceId = MDC.get("traceId");

        if (traceId == null || traceId.isBlank()) {
            MDC.put("traceId", CommonUtil.randomId());
        }
    }

    /**
     * 로그 추적 ID 조회
     * @return 로그 추적 ID
     */
    public String getTraceId() {
        return MDC.get("traceId");
    }

    /**
     * 로그 추적 오프셋 조회 (없는 경우 초기화)
     */
    public void setTraceOffset() {
        int traceOffset = this.convertTraceOffset(MDC.get("traceOffset"));

        if (traceOffset == Integer.MIN_VALUE) {
            MDC.put("traceOffset", "-1");
        }
    }

    /**
     * 로그 추적 오프셋 증가
     */
    public void increaseTraceOffset() {
        int traceOffset = this.convertTraceOffset(MDC.get("traceOffset"));

        if (traceOffset != Integer.MIN_VALUE) {
            traceOffset += 1;
            MDC.put("traceOffset", String.valueOf(traceOffset));
        }
    }

    /**
     * 로그 추적 오프셋 조회
     * @return 로그 추적 오프셋
     */
    public int getTraceOffset() {
        return this.convertTraceOffset(MDC.get("traceOffset"));
    }

    /**
     * 엔터리 메서드 이름
     */
    public void setEntryMethod(Method method) {
        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        MDC.put("entryMethod", String.format("%s#%s", clazzName, methodName));
    }

    public String getEntryMethod() {
        return MDC.get("entryMethod") == null ? "" : MDC.get("entryMethod");
    }

    /**
     * 스레드 캐시 삭제
     */
    public void clear() {
        MDC.clear();
    }

    public String parseJson(LogDto logDto) {
        try {
            return objectMapper.writeValueAsString(logDto);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * traceOffset 정수 변환
     */
    private int convertTraceOffset(String traceOffset) {
        if (traceOffset != null && !traceOffset.isBlank() && traceOffset.matches("-?\\d+")) {
            return Integer.parseInt(traceOffset);
        }

        return Integer.MIN_VALUE;
    }
}
