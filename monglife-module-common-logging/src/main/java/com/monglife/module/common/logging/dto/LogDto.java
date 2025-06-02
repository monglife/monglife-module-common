package com.monglife.module.common.logging.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogDto {

    private String traceId;

    private String method;

    public LogDto(String traceId, String method) {
        this.traceId = traceId;
        this.method = method;
    }
}
