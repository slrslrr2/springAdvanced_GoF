package hello.proxy.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class TimeAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        log.info("TimeProxy 실행");
        long startTime = System.currentTimeMillis();

//        Object result = method.invoke(target, args); // InvocationHandler
//        Object result = methodProxy.invoke(target, args); // MethodInterceptor
        Object result = invocation.proceed(); // Advice는 위 기능을 [invocation.proceed] 를 사용하여 [target을 호출]한다.

        long emdTime = System.currentTimeMillis();
        long resultTime = startTime - emdTime;
        log.info("TimeProxy 종료 resultTime={}", resultTime);

        return result;
    }
}
