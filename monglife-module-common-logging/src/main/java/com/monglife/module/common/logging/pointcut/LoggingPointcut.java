package com.monglife.module.common.logging.pointcut;

import org.aspectj.lang.annotation.Pointcut;

public class LoggingPointcut {
    
    @Pointcut("execution(* com.monglife..consumer.*Consumer.*(..))")
    private void consumerPointcut() {}

    @Pointcut("execution(* com.monglife..controller.*Controller.*(..))")
    private void controllerPointcut() {}

    @Pointcut("execution(* com.monglife..worker.*Worker.*(..))")
    private void workerPointcut() {}

    @Pointcut("execution(* com.monglife..initializer.*Initializer.*(..))")
    private void initializerPointcut() {}

    @Pointcut("execution(* com.monglife..service.*Service.*(..))")
    private void servicePointcut() {}

    @Pointcut("execution(* com.monglife..listener.*Listener.*(..))")
    private void listenerPointcut() {}

    @Pointcut("execution(* com.monglife..repository..*Repository*.*(..))")
    private void repositoryPointcut() {}

    @Pointcut("consumerPointcut() || controllerPointcut() || workerPointcut() || initializerPointcut() || servicePointcut() || listenerPointcut() || repositoryPointcut()")
    public void allPointcut() {}
}
