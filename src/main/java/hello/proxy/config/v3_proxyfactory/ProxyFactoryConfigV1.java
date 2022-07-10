package hello.proxy.config.v3_proxyfactory;

import hello.proxy.app.v1.*;
import hello.proxy.config.v2_dynamicproxy.LogTraceBasicHandler;
import hello.proxy.config.v3_proxyfactory.advice.LogTraceAdvice;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

@Slf4j
@Configuration
public class ProxyFactoryConfigV1 {

    @Bean
    public OrderServiceV1 orderController(LogTrace logTrace) {
        OrderControllerV1 controllerImpl = new OrderControllerV1Impl(orderService(logTrace));

        ProxyFactory factory = new ProxyFactory(controllerImpl);
        factory.addAdvisor(getAdvisor(logTrace));
        OrderServiceV1 proxy = (OrderServiceV1) factory.getProxy();
        log.info("ProxyFacotry proxy={}, target={},", proxy.getClass(), controllerImpl.getClass());
        return proxy;
    }

    @Bean
    public OrderServiceV1 orderService(LogTrace logTrace) {
        OrderServiceV1 orderServiceImpl = new OrderServiceV1Impl(orderRepository(logTrace));

        ProxyFactory factory = new ProxyFactory(orderServiceImpl);
        factory.addAdvisor(getAdvisor(logTrace));
        OrderServiceV1 proxy = (OrderServiceV1) factory.getProxy();
        log.info("ProxyFacotry proxy={}, target={},", proxy.getClass(), orderServiceImpl.getClass());
        return proxy;
    }

    @Bean
    public OrderRepositoryV1 orderRepository(LogTrace logTrace){
        OrderRepositoryV1Impl orderRepository = new OrderRepositoryV1Impl();

        ProxyFactory factory = new ProxyFactory(orderRepository);
        factory.addAdvisor(getAdvisor(logTrace));
        OrderRepositoryV1 proxy = (OrderRepositoryV1) factory.getProxy();
        log.info("ProxyFacotry proxy={}, target={},", proxy.getClass(), orderRepository.getClass());
        return proxy;
    }

    private Advisor getAdvisor(LogTrace logTrace) {
        //pointcut
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedNames("request*", "order*", "save*");

        LogTraceAdvice advice = new LogTraceAdvice(logTrace);
        return new DefaultPointcutAdvisor(pointcut, advice);
    }
}
