package com.monglife.module.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monglife.core.dto.response.ResponseDto;
import com.monglife.core.enums.response.GlobalResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@AllArgsConstructor
public class GlobalExceptionFilter extends GenericFilterBean {

    private final ObjectMapper objectMapper;

    /**
     * 최하위 Exception 필터
     * 각 모듈 ControllerAdvice 에서 걸러지지 않은 예외들을 처리한다.
     *
     * @param servletRequest  The request to process
     * @param servletResponse The response associated with the request
     * @param chain    Provides access to the next filter in the chain for this filter to pass the request and response
     *                     to for further processing
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            ResponseDto<Map<String, Object>> responseDto = GlobalResponse.INTERNAL_SERVER_ERROR.toResponseDto(Collections.singletonMap("error", e.getMessage()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE); // "application/json; charset=UTF-8"
            response.setStatus(GlobalResponse.INTERNAL_SERVER_ERROR.getHttpStatus());
            response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        }
    }
}
