package com.monglife.module.common.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.security.response.SecurityResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;


@RestControllerAdvice(basePackages = "com.monglife.*")
@AllArgsConstructor
public class ForbiddenHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 인가 실패 핸들러
     * Passport 의 인가 정보에 따른 접근 권한이 없어 인가가 불가한 경우에 응답
     *
     * @param request that resulted in an <code>AccessDeniedException</code>
     * @param response so that the user agent can be advised of the failure
     * @param accessDeniedException that caused the invocation
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(SecurityResponse.SECURITY_FORBIDDEN.getHttpStatus());
        response.getWriter().write(objectMapper.writeValueAsString(SecurityResponse.SECURITY_FORBIDDEN.toResponseDto()));
    }

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
