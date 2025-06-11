package com.monglife.module.common.logging.enums;

import lombok.Getter;

@Getter
public enum BasicLogType implements LogType {

    INITIALIZE,
    EXCEPTION,
    METHOD_CALL,
    METHOD_RETURN
    ;

    @Override
    public String getName() {
        return this.name();
    }
}
