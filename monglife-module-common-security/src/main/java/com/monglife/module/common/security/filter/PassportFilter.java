package com.monglife.module.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monglife.core.enums.role.RoleCode;
import com.monglife.core.utils.CommonUtil;
import com.monglife.core.vo.passport.PassportDataAccountVo;
import com.monglife.core.vo.passport.PassportDataAppVersionVo;
import com.monglife.core.vo.passport.PassportDataVo;
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
import java.time.LocalDateTime;

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

        String profile = System.getProperty("spring.config.activate.on-profile");

        if ("dev".equals(profile) && passportJson == null) {
            PassportVo passportVo = PassportVo.builder()
                    .data(PassportDataVo.builder()
                            .account(PassportDataAccountVo.builder()
                                    .accountId(0L)
                                    .deviceId(CommonUtil.randomId())
                                    .email("mongs@dev.monglife.com")
                                    .name("mongs")
                                    .role(RoleCode.NORMAL.getName())
                                    .build())
                            .appVersion(PassportDataAppVersionVo.builder()
                                    .appPackageName("com.mongs.wear")
                                    .buildVersion("0.0.0")
                                    .build())
                            .build())
                    .createdAt(LocalDateTime.now())
                    .build();

            User passport = new Passport(passportVo);

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    = new UsernamePasswordAuthenticationToken(passport, null, passport.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        } else if (passportJson != null) {
            PassportVo passportVo = objectMapper.readValue(URLDecoder.decode(passportJson, StandardCharsets.UTF_8), PassportVo.class);

            User passport = new Passport(passportVo);

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    = new UsernamePasswordAuthenticationToken(passport, null, passport.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }

        chain.doFilter(request, response);
    }
}
