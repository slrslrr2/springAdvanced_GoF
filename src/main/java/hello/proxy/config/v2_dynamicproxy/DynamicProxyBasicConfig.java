package hello.proxy.config.v2_dynamicproxy;

import hello.proxy.app.v1.*;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

@Configuration
public class DynamicProxyBasicConfig {
    @Bean
    public OrderControllerV1 orderController(LogTrace logTrace){
        OrderControllerV1 controllerImpl = new OrderControllerV1Impl(orderService(logTrace));
//        return new OrderControllerInterfaceProxy(controllerImpl, logTrace);
        OrderControllerV1 proxy = (OrderControllerV1) Proxy.newProxyInstance(
                OrderControllerV1.class.getClassLoader()
                , new Class[]{OrderControllerV1.class}
                , new LogTraceBasicHandler(controllerImpl, logTrace));
        return proxy;
    }

    @Bean
    public OrderServiceV1 orderService(LogTrace logTrace) {
        OrderServiceV1 orderServiceImpl = new OrderServiceV1Impl(orderRepository(logTrace));

//        return new OrderServiceInterfaceProxy(orderServiceImpl, logTrace);
        OrderServiceV1 proxy = (OrderServiceV1) Proxy.newProxyInstance(
                OrderServiceV1.class.getClassLoader()
                , new Class[]{OrderServiceV1.class}
                , new LogTraceBasicHandler(orderServiceImpl, logTrace));
        return proxy;
    }

    @Bean
    public OrderRepositoryV1 orderRepository(LogTrace logTrace) {
        OrderRepositoryV1 orderRepository = new OrderRepositoryV1Impl();

        // return new OrderRepositoryConcreteProxy(repositoryImpl, logtrace);
        // OrderRepositoryConcreteProxy 라는 프록시 구현체를 만드는것이 아닌
        // Proxy.newProxyInstance를 통하여 동적 프록시를 생성한다.
        OrderRepositoryV1 proxy = (OrderRepositoryV1) Proxy.newProxyInstance(
                OrderRepositoryV1.class.getClassLoader()
                , new Class[]{OrderRepositoryV1.class}
                , new LogTraceBasicHandler(orderRepository, logTrace));

        return proxy;
    }
}
