package com.monglife.module.common.logging.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class TransactionLogDto extends LogDto {

    private Map<String, Object> args;

    private Object returnValue;

    private String transaction;

    @Builder
    public TransactionLogDto(String traceId, String method, Map<String, Object> args, Object returnValue, String transaction) {
        super(traceId, method);
        this.args = args;
        this.returnValue = returnValue;
        this.transaction = transaction;
    }
}
