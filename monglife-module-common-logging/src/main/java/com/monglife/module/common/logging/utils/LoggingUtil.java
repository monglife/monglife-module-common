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
     * 로그 추적 ID 조회
     * @return 로그 추적 ID
     */
    public String getTraceId() {
        return MDC.get("traceId");
    }

    /**
     * 로그 추적 ID 조회 (없는 경우 초기화)
     * @return 로그 추적 ID
     */
    public String getTraceIdOrReset() {

        String traceId = MDC.get("traceId");

        if (traceId == null || traceId.isBlank()) {
            MDC.put("traceId", CommonUtil.randomId());
            traceId = MDC.get("traceId");
        }

        return traceId;
    }

    /**
     * 로그 추적 오프셋 조회
     * @return 로그 추적 오프셋
     */
    public int getTraceOffset() {
        return this.convertTraceOffset(MDC.get("traceOffset"));
    }

    /**
     * 로그 추적 오프셋 조회 (없는 경우 초기화)
     * @return 로그 추적 오프셋
     */
    public int getTraceOffsetOrReset() {
        int traceOffset = this.convertTraceOffset(MDC.get("traceOffset"));

        if (traceOffset == Integer.MIN_VALUE) {
            MDC.put("traceOffset", "0");
            traceOffset = this.convertTraceOffset(MDC.get("traceOffset"));
        }

        return traceOffset;
    }

    /**
     * 로그 추적 오프셋 증가
     * @return 로그 추적 오프셋 증가 후 값
     */
    public int increaseTraceOffset() {
        int traceOffset = this.convertTraceOffset(MDC.get("traceOffset"));

        if (traceOffset != Integer.MIN_VALUE) {
            traceOffset += 1;
            MDC.put("traceOffset", String.valueOf(traceOffset));
        }

        return traceOffset;
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
     * 로깅 필요 메서드 여부
     */
    public boolean isLoggingMethod(String traceId, Method method) {
        return traceId != null && !method.isAnnotationPresent(DisableLogging.class);
    }

    /**
     * traceOffset 정수 변환
     */
    private int convertTraceOffset(String traceOffset) {
        if (traceOffset != null && !traceOffset.isBlank() && traceOffset.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(traceOffset);
        }

        return Integer.MIN_VALUE;
    }
}
