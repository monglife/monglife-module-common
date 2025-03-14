package com.monglife.module.common.logging.aspect;

import com.monglife.module.common.logging.utils.ArgsUtil;
import com.monglife.core.exception.ErrorException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.monglife..*Consumer.*(..))")
    private void consumerPointcut() {}

    @Pointcut("execution(* com.monglife..*Controller.*(..))")
    private void controllerPointcut() {}

    @Pointcut("execution(* com.monglife..*Service.*(..))")
    private void servicePointcut() {}

    @Pointcut("execution(* com.monglife..*Listener.*(..))")
    private void listenerPointcut() {}

    @Pointcut("execution(* com.monglife..*Repository.*(..))")
    private void repositoryPointcut() {}

    @Pointcut("consumerPointcut() || controllerPointcut() || servicePointcut() || listenerPointcut()")
    private void targetPointcut() {}

    /**
     * None Transactional 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Before("targetPointcut() && !@annotation(org.springframework.transaction.annotation.Transactional) && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)")
    public void around(JoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        log.debug("\n[METHOD INVOKE] < X > {}#{} {}", clazzName, methodName, ArgsUtil.generateArgs(method, joinPoint.getArgs()));
    }

    /**
     * Transactional 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     */
    @Before("targetPointcut() && @annotation(org.springframework.transaction.annotation.Transactional) && !@annotation(com.monglife.module.common.logging.annotation.DisableLogging)")
    public void beforeTransactional(JoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        log.debug("\n[METHOD INVOKE] <{}> {}#{} {}", TransactionSynchronizationManager.getCurrentTransactionName(), clazzName, methodName, ArgsUtil.generateArgs(method, joinPoint.getArgs()));
    }

    /**
     * 예외 발생 메서드 로깅 함수
     * @param joinPoint 조인 포인트
     * @param exception 발생 예외
     */
    @AfterThrowing(value = "controllerPointcut() || consumerPointcut() || listenerPointcut()", throwing = "exception")
    public void afterThrowingException(JoinPoint joinPoint, Exception exception) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        String message = exception.getMessage();

        if (exception instanceof ErrorException errorException) {
            message = errorException.getResponse() == null ? "" : errorException.getResponse().getMessage();
        }

        log.error("\n[THROW] {}#{}\n{} : {}", clazzName, methodName, exception, message);
    }

}
