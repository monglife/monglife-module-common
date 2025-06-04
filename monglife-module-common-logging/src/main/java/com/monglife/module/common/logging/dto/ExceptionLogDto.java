package com.monglife.module.common.logging.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.monglife.module.common.logging.enums.LogType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ExceptionLogDto extends LogDto {

    private String message;

    private String stackTrace;

    @Builder()
    public ExceptionLogDto(String traceId, Integer traceOffset, String entryMethod, String className, String method, String message, String stackTrace) {
        super(traceId, traceOffset, entryMethod, className, method, LogType.EXCEPTION);
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
