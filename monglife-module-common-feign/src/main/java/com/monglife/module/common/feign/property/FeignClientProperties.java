package com.monglife.module.common.feign.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "module.feign")
public class FeignClientProperties {

    private Long connectTimeout = 2000L;

    private Long readTimeout = 4000L;

    private Boolean followRedirects = false;
}
