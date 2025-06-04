package com.monglife.module.common.logging.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.monglife.module.common.logging.enums.LogType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MethodReturnLogDto extends LogDto {

    private Object returnValue;

    private String transaction;

    @Builder
    public MethodReturnLogDto(String traceId, Integer traceOffset, String entryMethod, String className, String method, Object returnValue, String transaction) {
        super(traceId, traceOffset, entryMethod, className, method, LogType.METHOD_RETURN);
        this.returnValue = returnValue;
        this.transaction = transaction;
    }
}
