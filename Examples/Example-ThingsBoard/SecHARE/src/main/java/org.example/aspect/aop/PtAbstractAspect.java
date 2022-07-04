package org.example.aspect.aop;

import org.example.aspect.advise.PrivacyAroundAdvice;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public abstract class PtAbstractAspect {

    /**
     * add device, del device
     * del user
     * update info              --  after advisor
     * grant revoke
     *
     * get protected info           -- around advisor
     * verify sensitive info
     *
     */
    @Pointcut
    protected abstract void ptAroundPointcut();

    @Around("ptAroundPointcut()")
    public Object ptAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        return PrivacyAroundAdvice.execute(joinPoint);
    }

}
