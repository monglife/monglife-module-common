package com.monglife.module.common.security.response;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.core.enums.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum SecurityResponse implements Response {

    SECURITY_UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "000-000-100", "인증 정보가 없습니다."),
    SECURITY_FORBIDDEN(HttpStatus.FORBIDDEN.value(), "000-000-101", "접근 권한이 없습니다.");

    private final Integer httpStatus;
    private final String code;
    private final String message;

    @Override
    public ResponseDto<Map<String, Object>> toResponseDto() {
        return new ResponseDto<Map<String, Object>>(code, message, httpStatus, Collections.emptyMap());
    }

    @Override
    public <T> ResponseDto<T> toResponseDto(T result) {
        return new ResponseDto<T>(code, message, httpStatus, result);
    }
}