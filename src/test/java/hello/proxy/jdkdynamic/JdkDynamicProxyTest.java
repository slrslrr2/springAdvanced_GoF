package hello.proxy.jdkdynamic;

import hello.proxy.jdkdynamic.code.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

@Slf4j
public class JdkDynamicProxyTest {
    @Test
    void dynamicA(){
        AImpl target = new AImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target);

        AInterface proxy = (AInterface) Proxy.newProxyInstance(
                AInterface.class.getClassLoader() // 어떤 클래스로더에 할지 지정
                , new Class[]{AInterface.class}   // 어떤 Interface 기반으로 프록시를 만들지
                , handler);                       // 프록시가 사용해야할 공통로직

        proxy.call();
        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());
    }

    @Test
    void dynamicB(){
        BImpl target = new BImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target);

        BInterface proxy = (BInterface) Proxy.newProxyInstance(
                BInterface.class.getClassLoader() // 어떤 클래스로더에 할지 지정
                , new Class[]{BInterface.class}   // 어떤 Interface 기반으로 프록시를 만들지
                , handler);                       // 프록시가 사용해야할 공통로직

        proxy.call();
        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());
    }
}