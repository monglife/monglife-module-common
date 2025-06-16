package com.monglife.module.common.logging.utils;

import com.monglife.module.common.logging.dto.LogDto;
import com.monglife.module.common.logging.enums.LoggerType;

import java.lang.reflect.Method;

public interface LoggingUtil {

    /**
     * INFO 로그 출력
     * @param logDto 로그 Dto
     * @param loggerType 로거 타입
     */
    void printInfoLog(LogDto logDto, LoggerType loggerType);

    /**
     * DEBUG 로그 출력
     * @param logDto 로그 Dto
     * @param loggerType 로거 타입
     */
    void printDebugLog(LogDto logDto, LoggerType loggerType);

    /**
     * ERROR 로그 출력
     * @param logDto 로그 Dto
     * @param loggerType 로거 타입
     */
    void printErrorLog(LogDto logDto, LoggerType loggerType);

    /**
     * 로깅 필요 메서드 여부
     */
    boolean isLoggingMethod(Method method);

    /**
     * 로그 추적 ID 초기화
     */
    String setTraceId();

    /**
     * 로그 추적 ID 조회
     * @return 로그 추적 ID
     */
    String getTraceId();

    /**
     * 로그 추적 오프셋 조회 (없는 경우 초기화)
     */
    int setTraceOffset();

    /**
     * 로그 추적 오프셋 증가
     */
    void increaseTraceOffset();

    /**
     * 로그 추적 오프셋 조회
     * @return 로그 추적 오프셋
     */
    int getTraceOffset();

    /**
     * 엔터리 메서드 이름
     */
    String setEntryMethod(Method method);

    /**
     * 엔터리 메서드 조회
     * @return 엔터리 메서드
     */
    String getEntryMethod();

    /**
     * 스레드 캐시 삭제
     */
    void clear();
}
