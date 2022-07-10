package hello.proxy.jdkdynamic.code;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class TimeInvocationHandler implements InvocationHandler {
    private final Object target; // 프록시 이후 객체로 모든 다 되도록 Object

    public TimeInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("TimeProxy 실행");
        long startTime = System.currentTimeMillis();

        Object result = method.invoke(target, args);
        long emdTime = System.currentTimeMillis();
        long resultTime = startTime - emdTime;
        log.info("TimeProxy 종료 resultTime={}", resultTime);

        return result;
    }
}