package com.monglife.module.common.logging.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
