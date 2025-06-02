package com.monglife.module.common.logging.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class NotTransactionLogDto extends LogDto {

    private Map<String, Object> args;

    private Object returnValue;

    @Builder
    public NotTransactionLogDto(String traceId, String method, Map<String, Object> args, Object returnValue) {
        super(traceId, method);
        this.args = args;
        this.returnValue = returnValue;
    }
}
