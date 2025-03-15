package com.monglife.module.common.feign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.module.common.feign.property.FeignClientProperties;
import feign.Request;
import feign.Retryer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@AutoConfigureBefore(FeignAutoConfiguration.class)
@EnableConfigurationProperties({ FeignClientProperties.class })
@EnableFeignClients(basePackages = "com.monglife")
public class FeignClientAutoConfig {

    /**
     * Feign Client 기본 (?) client 는 Patch Method 사용 불가 -> OkHttp Client 으로 변경
     */
    @Bean
    public Request.Options options(FeignClientProperties feignClientProperties) {
        return new Request.Options(
                feignClientProperties.getConnectTimeout(), TimeUnit.MILLISECONDS,
                feignClientProperties.getReadTimeout(), TimeUnit.MILLISECONDS,
                feignClientProperties.getFollowRedirects()
        );
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