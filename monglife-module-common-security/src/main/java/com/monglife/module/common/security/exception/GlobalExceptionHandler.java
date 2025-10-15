package com.monglife.module.common.security.exception;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.security.response.SecurityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.monglife.*")
public class GlobalExceptionHandler {

    /**
     * 권한 없음 예외 클래스 처리 핸들러
     * @param e 예외 객체
     * @return 에러 응답 객체
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto<Map<String, Object>>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(SecurityResponse.SECURITY_FORBIDDEN.getHttpStatus())
                .body(SecurityResponse.SECURITY_FORBIDDEN.toResponseDto());
    }
}
