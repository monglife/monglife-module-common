package com.monglife.module.common.security.annotation;

import com.monglife.core.enums.role.RoleCode;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface AuthCheck {

    RoleCode[] value() default {};
}
