package com.monglife.module.common.logging.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.monglife.module.common.logging.enums.BasicLogType;
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

    private Map<String, Object> args;

    private String transaction;

    @Builder
    public MethodCallDto(String traceId, Integer traceOffset, String entryMethod, String className, String method, Map<String, Object> args, String transaction) {
        super(traceId, traceOffset, entryMethod, className, method, BasicLogType.METHOD_CALL);
        this.args = args;
        this.transaction = transaction;
    }
}
