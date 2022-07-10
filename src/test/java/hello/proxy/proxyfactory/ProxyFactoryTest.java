package hello.proxy.proxyfactory;

import hello.proxy.common.advice.TimeAdvice;
import hello.proxy.common.service.ConcreteService;
import hello.proxy.common.service.ServiceImpl;
import hello.proxy.common.service.ServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ProxyFactoryTest{
    @Test
    @DisplayName("인터페이스가 있으면 JDK 동적 프록시 사용")
    void interfaceFactory(){
        ServiceInterface target = new ServiceImpl(); // target

        // 프록시 만들어 주는 객체 [ProxyFactory]
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice(new TimeAdvice()); // 공통로직 Advice
        ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
        proxy.save();

        //class hello.proxy.common.service.ServiceImpl
        log.info("targetClass={}", target.getClass());
        //proxyClass=class com.sun.proxy.$Proxy13
        log.info("proxyClass={}", proxy.getClass());
        assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(proxy)).isTrue(); // 인터페이스가 있는 클래스의 프록시인지 JDK프록시 인지
        assertThat(AopUtils.isCglibProxy(proxy)).isFalse(); // 클래스만으로 만든 프록시인지
    }

    @Test
    @DisplayName("구체클래스만 있으면 CGLIB 프록시 사용")
    void concreteFactory(){
        ConcreteService target = new ConcreteService();

        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice(new TimeAdvice());
        ConcreteService proxy = (ConcreteService) proxyFactory.getProxy();
        proxy.call();

        //class hello.proxy.common.service.ConcreteService
        log.info("targetClass={}", target.getClass());
        //class hello.proxy.common.service.ConcreteService$$EnhancerBySpringCGLIB$$c58f4a7
        log.info("proxyClass={}", proxy.getClass());
        assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse(); // 인터페이스가 있는 클래스의 프록시인지 JDK프록시 인지
        assertThat(AopUtils.isCglibProxy(proxy)).isTrue(); // 클래스만으로 만든 프록시인지
    }


    @Test
    @DisplayName("proxyTargetClass 옵션을 사용하면 " +
            "인터페이스가 있어도 CGLIB를 사용하여, 클래스 기반 프록시 사용")
    void proxyTargetClass(){
        ServiceInterface target = new ServiceImpl();

        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);//인터페이스가 있어도 CGLIB를 사용하여, 클래스 기반 프록시 사용
        proxyFactory.addAdvice(new TimeAdvice());
        ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
        proxy.save();

        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());
        assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
        assertThat(AopUtils.isCglibProxy(proxy)).isTrue();
    }
}
