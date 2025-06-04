package com.monglife.module.common.logging.dto;

import com.monglife.module.common.logging.enums.LogType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class LogDto {

    private String traceId;

    private Integer traceOffset;

    private String className;

    private String method;

    private Map<String, Object> args;

    private LogType logType;

    public LogDto(String traceId, Integer traceOffset, String className, String method, Map<String, Object> args, LogType logType) {
        this.traceId = traceId;
        this.traceOffset = traceOffset;
        this.className = className;
        this.method = method;
        this.args = args;
        this.logType = logType;
    }
}
