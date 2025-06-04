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
public class MethodCallDto extends LogDto {

    private String transaction;

    @Builder
    public MethodCallDto(String traceId, Integer traceOffset, String className, String method, Map<String, Object> args, String transaction) {
        super(traceId, traceOffset, className, method, args, LogType.METHOD_CALL);
        this.transaction = transaction;
    }
}
