package com.monglife.module.common.logging.pointcut;

import org.aspectj.lang.annotation.Pointcut;

public class LoggingPointcut {
    
    @Pointcut("execution(* com.monglife..*Consumer.*(..))")
    private void consumerPointcut() {}

    @Pointcut("execution(* com.monglife..*Controller.*(..))")
    private void controllerPointcut() {}

    @Pointcut("execution(* com.monglife..*Worker.*(..))")
    private void workerPointcut() {}

    @Pointcut("execution(* com.monglife..*Initializer.*(..))")
    private void initializerPointcut() {}

    @Pointcut("execution(* com.monglife..*UseCase.*(..))")
    private void useCasePointcut() {}

    @Pointcut("execution(* com.monglife..*Service.*(..))")
    private void servicePointcut() {}

    @Pointcut("execution(public * com.monglife..model.*.*(..))")
    private void domainPointcut() {}

    @Pointcut("execution(* com.monglife..*Port.*(..))")
    private void portPointcut() {}

    @Pointcut("execution(* com.monglife..*Listener.*(..))")
    private void listenerPointcut() {}

    @Pointcut("execution(* com.monglife..*Repository.*(..))")
    private void repositoryPointcut() {}

    // group point cut
    @Pointcut("consumerPointcut() || controllerPointcut() || workerPointcut() || initializerPointcut()")
    public void entryPointcut() {}

    @Pointcut("useCasePointcut() || servicePointcut() || domainPointcut() || listenerPointcut() || portPointcut() || repositoryPointcut()")
    public void businessPointcut() {}

    // union all point cut
    @Pointcut("entryPointcut() || businessPointcut()")
    public void allPointcut() {}
}
