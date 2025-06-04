package com.monglife.module.common.logging.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class NotTransactionLogDto extends LogDto {

    private Object returnValue;

    @Builder
    public NotTransactionLogDto(String traceId, Integer traceOffset, String className, String method, Map<String, Object> args, Object returnValue) {
        super(traceId, traceOffset, className, method, args);
        this.returnValue = returnValue;
    }
}
