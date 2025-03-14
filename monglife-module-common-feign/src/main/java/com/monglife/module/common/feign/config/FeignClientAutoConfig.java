package com.monglife.module.common.feign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Request;
import feign.Retryer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@EnableFeignClients
@AutoConfiguration
public class FeignClientAutoConfig {

    private static final Long CONNECT_TIMEOUT = 2000L;

    private static final Long READ_TIMEOUT = 4000L;

    /**
     * Feign Client 기본 (?) client 는 Patch Method 사용 불가 -> OkHttp Client 으로 변경
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS, READ_TIMEOUT, TimeUnit.MILLISECONDS, false);
    }

    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new FeignErrorDecoder(objectMapper);
    }

    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }
}