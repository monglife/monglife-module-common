package com.monglife.module.common.logging.utils;

import com.monglife.core.utils.CommonUtil;
import com.monglife.module.common.logging.annotation.DisableLogging;
import com.monglife.module.common.logging.annotation.DisableLoggingCascade;
import com.monglife.module.common.logging.dto.LogDto;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoggingUtil {

    private static final Map<String, Stack<LogDto>> LOG_QUEUE_MAP = new ConcurrentHashMap<>();

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
     * 로그 스택 초기화
     * @param traceId 로그 추적 ID
     */
    public void resetLogStack(String traceId) {
        LOG_QUEUE_MAP.put(traceId, new Stack<>());
    }

    /**
     * 로그 스택 조회
     * @param traceId 로그 추적 ID
     * @return 로그 스택
     */
    public Stack<LogDto> getLogStack(String traceId) {
        return LOG_QUEUE_MAP.get(traceId) == null ? new Stack<>() : LOG_QUEUE_MAP.get(traceId);
    }

    /**
     * 스레드 캐시 삭제
     * @param traceId 로그 추적 ID
     */
    public void clear(String traceId) {
        LOG_QUEUE_MAP.remove(traceId);
        MDC.clear();
    }

    /**
     * 로깅 필요 메서드 여부
     */
    public boolean isLoggingMethod(String traceId, Method method) {
        return LOG_QUEUE_MAP.containsKey(traceId) && !method.isAnnotationPresent(DisableLoggingCascade.class) && !method.isAnnotationPresent(DisableLogging.class);
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
