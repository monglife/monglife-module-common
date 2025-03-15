package com.monglife.module.common.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monglife.module.common.security.response.SecurityResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;


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
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // "application/json; charset=UTF-8"
        response.setStatus(SecurityResponse.SECURITY_FORBIDDEN.getHttpStatus());
        response.getWriter().write(objectMapper.writeValueAsString(SecurityResponse.SECURITY_FORBIDDEN.toResponseDto()));
    }
}
