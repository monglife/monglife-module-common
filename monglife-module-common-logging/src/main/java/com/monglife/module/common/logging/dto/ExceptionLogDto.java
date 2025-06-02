package com.monglife.module.common.logging.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExceptionLogDto extends LogDto {

    private String message;

    private String stackTrace;

    @Builder()
    public ExceptionLogDto(String traceId, String method, String message, String stackTrace) {
        super(traceId, method);
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
