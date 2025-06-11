package com.monglife.module.common.logging.dto;

import com.monglife.module.common.logging.enums.LogType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogDto {

    private String traceId;

    private Integer traceOffset;

    private String entryMethod;

    private String className;

    private String method;

    private String logType;

    public LogDto(String traceId, Integer traceOffset, String entryMethod, String className, String method, LogType logType) {
        this.traceId = traceId;
        this.traceOffset = traceOffset;
        this.entryMethod = entryMethod;
        this.className = className;
        this.method = method;
        this.logType = logType.getName();
    }
}
