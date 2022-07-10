# 5. 동적 프록시 기술

## 5-1. 리플렉션

**리플렉션**이란? 

> 구체적인 클래스 타입을 알지 못해도, 그 클래스의 메소드, 타입, 변수들에 접근할 수 있도록 해주는 자바API

-  리플렉션 기술은 **런타임에 동작**하기 때문에, **컴파일 시점에 오류를 잡을 수 없다.**

```java
@Test
void reflection2() throws Exception {
  Hello target = new Hello();
  
  // 1. 클래스의 메타정보 획득
  Class classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");
  
  // 2. 메서드 메타정보 획득
  Method methodCallA = classHello.getMethod("callA");
  dynamicCall(methodCallA, target);
  
  Method methodCallB = classHello.getMethod("callB");
  dynamicCall(methodCallB, target);
}

// 리플렉션
// 구체적인 타입을 알지 못해도 해당 메서드에 접근할 수 있도록 함
private void dynamicCall(Method method, Object target) throws Exception {
  log.info("start");
  Object result = method.invoke(target);
  log.info("result={}", result);
}
```



## 5-2, 3. JDK 동적 프록시 - 예제코드

```java
public interface AInterface {
  public String call();
}

@Slf4j
public class AImpl implements AInterface {
  @Override
  public String call() {
    log.info("A 호출");
    return "a";
  }
}
```



```java
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

    Object result = method.invoke(target, args); // target 실행
    long emdTime = System.currentTimeMillis();

    long resultTime = startTime - emdTime;

    log.info("TimeProxy 종료 resultTime={}", resultTime);

    return result;
  }
}
```

- 동적 프록시를 사용하고 싶은 공통 로직 클래스는 

  - **InvocationHandler** 를 상속받는다.

  - @Override invoke 메소드를 오버라이드한다.

  - 프록시 이후 객체로 모든 다 되도록 Object target를 선언하고<br>생성자를 통해 주입받는다

    ```java
    private final Object target;
    public TimeInvocationHandler(Object target) { this.target = target; }
    ```



```java
@Slf4j
public class JdkDynamicProxyTest {
  @Test
  void dynamicA(){
    AImpl target = new AImpl();
    TimeInvocationHandler handler = new TimeInvocationHandler(target); // target을 주입한다. InvocationHandler를 상속받았다.
		
    // 동적 프록시 생성
    AInterface proxy = (AInterface) Proxy.newProxyInstance(
      AInterface.class.getClassLoader() // 어떤 클래스로더에 할지 지정
      , new Class[]{AInterface.class}   // 어떤 Interface 기반으로 프록시를 만들지
      , handler);                       // 프록시가 사용해야할 공통로직

    proxy.call();
    log.info("targetClass={}", target.getClass()); //class hello.proxy.jdkdynamic.code.AImpl
    log.info("proxyClass={}", proxy.getClass()); // class com.sun.proxy.$Proxy12
  }
}
```

위 코드를 실행하면 결과는 아래와 같다.

```
INFO hello.proxy.jdkdynamic.code.TimeInvocationHandler - TimeProxy 실행
INFO hello.proxy.jdkdynamic.code.AImpl - A 호출
INFO hello.proxy.jdkdynamic.code.TimeInvocationHandler - TimeProxy 종료 resultTime=-1
INFO hello.proxy.jdkdynamic.JdkDynamicProxyTest - targetClass=class hello.proxy.jdkdynamic.code.AImpl
INFO hello.proxy.jdkdynamic.JdkDynamicProxyTest - proxyClass=class com.sun.proxy.$Proxy12
```

**실행 순서**

1. 클라이언트는 JDK 동적 프록시의 call() 을 실행한다.

   ```java
   proxy.call();
   ```

2. JDK 동적 프록시는 **`InvocationHandler.invoke()`** 를 호출한다. <br>`TimeInvocationHandler` 가 구현체로 있으로 `TimeInvocationHandler.invoke()` 가 호출된다.

3. TimeInvocationHandler 가 내부 로직을 수행하고, <br>**method.invoke(target, args)** 를 호출해서 target 인 실제 객체( AImpl )를 호출한다.

4. AImpl 인스턴스의 call() 이 실행된다.

5. AImpl 인스턴스의 call() 의 실행이 끝나면 TimeInvocationHandler 로 응답이 돌아온다. 시간 로그를 출력하고 결과를 반환한다.

<img width="741" alt="image" src="https://user-images.githubusercontent.com/58017318/178129039-c0405fa9-b754-4e41-84e1-f4144363e7c9.png">


> $proxy1은 사용자가 만든 클래스가 아닌 아래 소스를 통해 만들었다.
>
> > ```java
> > // 동적 프록시 생성
> > AInterface proxy = (AInterface) Proxy.newProxyInstance(
> > AInterface.class.getClassLoader()
> > , new Class[]{AInterface.class}  
> > , new TimeInvocationHandler(new AImpl()));                      
> > ```



- **동적 프록시에 적용될 공통 핸들러 로직**인 **TimeInvocationHandler**로 따로 만들어놓았기에 <br>BInterface 가 존재한다면 TimeInvocationHandler를 그대로 가져다 사용하여<br>Proxy.newProxyInstance를 이용해 프록시 객체를 만들어주면된다.

-----

- InvocationHandler
  - @Override invoke
- Proxy.newProxyInstance(<br>AInterface.class.getClassLoader()<br>, new Class[]{AInterface.class} <br>, new TimeInvocationHandler(target));

----

# 5-4. JDK 동적 프록시 - 적용1



### 1. 동적 프록시에 적용될 공통 핸들러 로직 생성

**동적 프록시 적용 전 소스**

```java
@RequiredArgsConstructor
public class OrderControllerInterfaceProxy implements OrderControllerV1 {
  private final OrderControllerV1 target;
  private final LogTrace logTrace;

  @Override
  public String request(String itemId) {
    TraceStatus status = null;

    try{
      status = logTrace.begin("OrderController.request()");
      //target 호출
      String request = target.request(itemId);
      logTrace.end(status);
      return request;
    } catch (Exception e){
      logTrace.exception(status, e);
      throw e;
    }
  }

  @Override
  public String noLog() {
    return target.noLog();
  }
}
```

> 위 프록시 객체가 Controller, Service, Repository 각각 있었다.



**동적 프록시 적용 후 소스**

```java
public class LogTraceBasicHandler implements InvocationHandler {
  private final Object target;
  private final LogTrace logTrace;

  public LogTraceBasicHandler(Object target, LogTrace logTrace) {
    this.target = target;
    this.logTrace = logTrace;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    TraceStatus status = null;
    try{
      String message = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
      status = logTrace.begin(message); // "OrderController.request()"

      Object result = method.invoke(target, args);

      logTrace.end(status);
      return result;
    } catch (Exception e){
      logTrace.exception(status, e);
      throw e;
    }
  }
}
```



### 2. 동적 프록시 생성 @Configuration

**동적 프록시 적용 전 소스**

```java
@Configuration
public class InterfaceProcyConfig {
  @Bean
  public OrderControllerV1 orderController(LogTrace logTrace){
    OrderControllerV1Impl controllerImpl = new OrderControllerV1Impl(orderService(logTrace));
    return new OrderControllerInterfaceProxy(controllerImpl, logTrace);
  }

  @Bean
  public OrderServiceV1 orderService(LogTrace logTrace) {
    OrderServiceV1Impl orderServiceImpl = new OrderServiceV1Impl(orderRepository(logTrace));
    return new OrderServiceInterfaceProxy(orderServiceImpl, logTrace);
  }

  @Bean
  public OrderRepositoryV1 orderRepository(LogTrace logTrace){
    OrderRepositoryV1Impl orderRepositoryImpl = new OrderRepositoryV1Impl();
    return new OrderRepositoryInterfaceProxy(orderRepositoryImpl, logTrace);
  }
}
```



**동적 프록시 적용 후 소스**

```java
@Configuration
public class DynamicProxyBasicConfig {
  @Bean
  public OrderControllerV1 orderController(LogTrace logTrace){
    OrderControllerV1 controllerImpl = new OrderControllerV1Impl(orderService(logTrace));
    OrderControllerV1 proxy = (OrderControllerV1) Proxy.newProxyInstance(
      OrderControllerV1.class.getClassLoader()
      , new Class[]{OrderControllerV1.class}
      , new LogTraceBasicHandler(controllerImpl, logTrace));
    return proxy;
  }

  @Bean
  public OrderServiceV1 orderService(LogTrace logTrace) {
    OrderServiceV1 orderServiceImpl = new OrderServiceV1Impl(orderRepository(logTrace));

    OrderServiceV1 proxy = (OrderServiceV1) Proxy.newProxyInstance(
      OrderServiceV1.class.getClassLoader()
      , new Class[]{OrderServiceV1.class}
      , new LogTraceBasicHandler(orderServiceImpl, logTrace));
    return proxy;
  }

  @Bean
  public OrderRepositoryV1 orderRepository(LogTrace logTrace) {
    OrderRepositoryV1 orderRepository = new OrderRepositoryV1Impl();

    OrderRepositoryV1 proxy = (OrderRepositoryV1) Proxy.newProxyInstance(
      OrderRepositoryV1.class.getClassLoader()
      , new Class[]{OrderRepositoryV1.class}
      , new LogTraceBasicHandler(orderRepository, logTrace));

    return proxy;
  }
}
```



**orderRepository**를 기준으로 동적프록시를 비교한다면

**변경 전**

```java
@Bean
public OrderRepositoryV1 orderRepository(LogTrace logTrace){
  OrderRepositoryV1Impl orderRepositoryImpl = new OrderRepositoryV1Impl();
  return new OrderRepositoryInterfaceProxy(orderRepositoryImpl, logTrace);
}
```

> 변경 전에는 **OrderRepositoryInterfaceProxy**라는 동적인 프록시를 구체적으로 개발자가 생성해주었다.
>
> 하지만 **Proxy.newProxyInstance**함수를 통하여 프록시를 만들어주었다.

**변경 후**

```java
@Bean
public OrderRepositoryV1 orderRepository(LogTrace logTrace) {
  OrderRepositoryV1 orderRepository = new OrderRepositoryV1Impl();
	
  // 프록시 객체를 [Proxy.newProxyInstance]를 통하여 생성
  OrderRepositoryV1 proxy = (OrderRepositoryV1) Proxy.newProxyInstance(
    OrderRepositoryV1.class.getClassLoader()
    , new Class[]{OrderRepositoryV1.class}
    , new LogTraceBasicHandler(orderRepository, logTrace));

  return proxy;
}
```

<img width="744" alt="image" src="https://user-images.githubusercontent.com/58017318/178129106-e6322767-0803-4661-948d-1fa7596ed6c7.png">

- 점선은 직접만든 클래스가 아니다
- InvocationHandler를 상속받은 클래스가 공통으로 처리된다.



<img width="735" alt="image" src="https://user-images.githubusercontent.com/58017318/178129122-3190d790-93b0-4e90-9dd4-fc9238ab6b65.png">

- 클라이언트가 request 시 
  1. 프록시 객체가 실행되고
  2. 해당 프록시 객체안에 **InvocationHandler**을 **implements** 받은 **logTraceBasicHandler**가 실행된다



----

```java
private static String[] PATTERNS = {"request*", "order*", "save*"};
if(!PatternMatchUtils.simpleMatch(patterns, methodName)){
	// 매칭 안되면
	return method.invoke(target, args);
}
```

-------

# CGLIB

**CGLIB: Code Generator Library**

- CGLIB는 **바이트코드를 조작**해서 **동적으로 클래스를 생성**하는 기술을 제공하는 라이브러리이다. 
- CGLIB를 사용하면 **인터페이스가 없어도** 구체 클래스만 가지고 **동적 프록시를 **만들어낼 수 있다.
- CGLIB는 원래는 외부 라이브러리인데, 스프링 프레임워크가 스프링 내부 소스 코드에 포함했다. <br>따라서 스프링을 사용한다면 별도의 외부 라이브러리를 추가하지 않아도 사용할 수 있다.

참고로 우리가 CGLIB를 직접 사용하는 경우는 거의 없다. 이후에 설명할 스프링의 **ProxyFactory** 라는 것이 이 기술을 편리하게 사용하게 도와주기 때문에, 너무 깊이있게 파기 보다는 CGLIB가 무엇인지 대략 개념만 잡으면 된다.



## CGLIB 간단 예제 만들기

```java
public interface ServiceInterface {
  void save();
  void find();
}
```

```java
@Slf4j
public class ServiceImpl implements ServiceInterface {
  @Override
  public void save() {
    log.info("save 호출"); 
  }
  
  @Override
  public void find() {
    log.info("find 호출"); 
  }
}
```

```java
@Slf4j
public class ConcreteService {
  public void call() { 
    log.info("ConcreteService 호출");
	}
}
```



---

```java
@Slf4j
public class TimeMethodInterceptor implements MethodInterceptor {
    private final Object target;

    public TimeMethodInterceptor(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        log.info("TimeProxy 실행");
        long startTime = System.currentTimeMillis();

        Object result = methodProxy.invoke(target, args);
        long emdTime = System.currentTimeMillis();

        long resultTime = startTime - emdTime;

        log.info("TimeProxy 종료 resultTime={}", resultTime);

        return result;
    }
}

```

```java
@Slf4j
public class CglibTest {
  @Test
  void cglib(){
    ConcreteService target = new ConcreteService();
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(ConcreteService.class);
    enhancer.setCallback(new TimeMethodInterceptor(target));
    ConcreteService proxy = (ConcreteService) enhancer.create();
    proxy.call();

    log.info("targetClass={}", target.getClass()); // targetClass=class hello.proxy.common.service.ConcreteService
    log.info("proxyClass={}", proxy.getClass()); // proxyClass=class hello.proxy.common.service.ConcreteService$$EnhancerByCGLIB$$25d6b0e3
  }
}
```

<img width="1677" alt="image-20220710114854037" src="https://user-images.githubusercontent.com/58017318/178129332-ef9860ba-7f32-45a0-91e2-ea3f79768945.png">
