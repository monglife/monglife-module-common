package com.monglife.module.common.logging.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoggerType {

    CONSOLE_LOGGER("ConsoleLogger"),
    LOGSTASH_LOGGER("LogstashLogger"),
    ;

    private final String loggerName;
}
