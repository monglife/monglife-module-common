package com.monglife.module.common.jpa.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@AutoConfiguration
public class HibernateAutoConfig {

    @Bean(name = "hibernateProperties")
    @ConditionalOnMissingBean(name = "hibernateProperties")
    public Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, "com.monglife.module.common.jpa.config.ImprovedNamingStrategy");
        return hibernateProperties;
    }
}
