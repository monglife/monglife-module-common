package com.monglife.module.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monglife.module.common.security.principal.Passport;
import com.monglife.core.vo.passport.PassportVo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class PassportFilter extends GenericFilterBean {

    private final ObjectMapper objectMapper;

    /**
     * Header 에 담긴 Passport Json 문자열을 파싱하여 인증 객체를 생성하고, SecurityContext 에 저장한다.
     *
     * @param servletRequest  The request to process
     * @param response The response associated with the request
     * @param chain    Provides access to the next filter in the chain for this filter to pass the request and response
     *                     to for further processing
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String passportJson = request.getHeader("passport");

        if (passportJson != null) {
            PassportVo passportVo = objectMapper.readValue(URLDecoder.decode(passportJson, StandardCharsets.UTF_8), PassportVo.class);

            User passport = new Passport(passportVo);

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    = new UsernamePasswordAuthenticationToken(passport, null, passport.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }

        chain.doFilter(request, response);
    }
}
