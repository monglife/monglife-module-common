package com.monglife.module.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.core.enums.role.RoleCode;
import com.monglife.module.common.security.exception.ForbiddenHandler;
import com.monglife.module.common.security.exception.UnAuthorizationHandler;
import com.monglife.module.common.security.filter.GlobalExceptionFilter;
import com.monglife.module.common.security.filter.PassportFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@EnableWebSecurity
@EnableMethodSecurity
@AutoConfiguration
public class SecurityAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain filterChain(
            @Autowired UnAuthorizationHandler unAuthorizationHandler,
            @Autowired ForbiddenHandler forbiddenHandler,
            @Autowired PassportFilter passportFilter,
            @Autowired GlobalExceptionFilter globalExceptionFilter,
            HttpSecurity http
    ) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(passportFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(globalExceptionFilter, PassportFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/admin/**").hasAuthority(RoleCode.ADMIN.getRole())
                    .requestMatchers("/**").hasAnyAuthority(Arrays.stream(RoleCode.values()).map(RoleCode::getRole).toArray(String[]::new))
                    .anyRequest().authenticated()
            )
            .exceptionHandling(configurer -> {
                configurer.authenticationEntryPoint(unAuthorizationHandler);
                configurer.accessDeniedHandler(forbiddenHandler);
            })
            .build();
    }

    @Bean
    public UnAuthorizationHandler unAuthorizationHandler() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new UnAuthorizationHandler(objectMapper);
    }

    @Bean
    public ForbiddenHandler forbiddenHandler() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new ForbiddenHandler(objectMapper);
    }

    @Bean
    public GlobalExceptionFilter securityExceptionHandler() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new GlobalExceptionFilter(objectMapper);
    }

    @Bean
    public PassportFilter passportFilter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new PassportFilter(objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
