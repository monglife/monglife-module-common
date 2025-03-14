package com.monglife.module.common.jpa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@AutoConfiguration
public class MultiTransactionAutoConfig {

    private final List<PlatformTransactionManager> transactionManagers;

    @Autowired
    public MultiTransactionAutoConfig(List<PlatformTransactionManager> transactionManagers) {
        this.transactionManagers = transactionManagers;
    }

    @Primary
    @Bean(name = "transactionManager")
    @ConditionalOnMissingBean(ChainedTransactionManager.class)
    public ChainedTransactionManager transactionManager() {
        return new ChainedTransactionManager(transactionManagers.toArray(new PlatformTransactionManager[0]));
    }
}
