package com.monglife.module.common.feign.exception;

import com.monglife.core.dto.response.ResponseDto;
import lombok.Getter;

import java.util.Map;

@Getter
public class FeignClientException extends RuntimeException {

    private final String code;

    private final String message;

    private final Integer httpStatus;

    private final Map<String, Object> result;

    public FeignClientException(ResponseDto<Map<String, Object>> responseDto) {
        this.code = responseDto.getCode();
        this.message = responseDto.getMessage();
        this.httpStatus = responseDto.getHttpStatus();
        this.result = responseDto.getResult();
    }
}
