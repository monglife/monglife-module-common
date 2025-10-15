package com.monglife.module.common.security.aspect;

import com.monglife.core.enums.role.RoleCode;
import com.monglife.module.common.security.annotation.AuthCheck;
import com.monglife.module.common.security.principal.Passport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Profile("!test")
public class AuthCheckAspect {

    @Around("@within(com.monglife.module.common.security.annotation.AuthCheck) || @annotation(com.monglife.module.common.security.annotation.AuthCheck)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Passport passport = (Passport) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuthCheck authCheck = method.getAnnotation(AuthCheck.class);

        for (RoleCode roleCode : authCheck.value()) {
            if (roleCode.getRole().equals(passport.getRole())) {
                return joinPoint.proceed();
            }
        }

        throw new AccessDeniedException("메서드 접근 권한이 없습니다.");
    }
}
