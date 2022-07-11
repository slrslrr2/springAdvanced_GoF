package hello.proxy.config.v6_aop.aspect;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;

@Slf4j
@Aspect
public class LogTraceAspect {
    private final LogTrace logTrace;

    public LogTraceAspect(LogTrace logTrace) {
        this.logTrace = logTrace;
    }

    @Around("execution(* hello.proxy.app..*(..))")
    public Object excute(ProceedingJoinPoint joinPoint) throws Throwable{
        // Advice 공통 로직들
        // pointcut + advice => Advisor
        TraceStatus status = null;
        try{
            String message = joinPoint.getSignature().toShortString();
            status = logTrace.begin(message); // "OrderController.request()"

            Object result = joinPoint.proceed();// 로직호출

            logTrace.end(status);
            return result;
        } catch (Exception e){
            logTrace.exception(status, e);
            throw e;
        }
    }
}
