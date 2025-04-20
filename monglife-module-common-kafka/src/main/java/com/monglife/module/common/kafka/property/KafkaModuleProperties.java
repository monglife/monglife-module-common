package com.monglife.module.common.kafka.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "module.kafka")
public class KafkaModuleProperties {

    private String url = "localhost:9092";

    private String groupId = "default";

    private String autoOffsetResetConfig = "earliest";

    private String isolationLevelConfig = "read_committed";
}
