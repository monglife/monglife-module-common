package com.monglife.module.common.logging.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogDto {

    private String traceId;

    private Integer traceOffset;

    private String className;

    private String method;

    public LogDto(String traceId, Integer traceOffset, String className, String method) {
        this.traceId = traceId;
        this.traceOffset = traceOffset;
        this.className = className;
        this.method = method;
    }
}
