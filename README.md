

# 템플릿 메서드 패턴, 전략 패턴, 콜백 패턴

## 1. 템플릿 메서드 패턴

**템플릿메서드 패턴**이란? <br>**부모 클래스**에 **변하지 않는 템플릿 코드**를 둔다. 그리고 **변하는 부분은 자식 클래스**에 두고 **상속과 오버라이딩을 사용**해서 처리한다.

<img width="643" alt="image-20220618224834084" src="https://user-images.githubusercontent.com/58017318/174489477-9edb17bd-f318-401d-b4ab-4ee91106a4a6.png">



예제1)

**부모클래스 AbstractTemplate**

```java
@Slf4j
public abstract class AbstractTemplate {

    public void exec(){
        long startTime = System.currentTimeMillis();
        
        // 변하는 부분 자식클래스로 만들어서 상속
        call();
        
        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime = {}", resultTime);
    }

    protected abstract void call();
}
```



**자식클래스 SubClassLogin1, SubClassLogin2**

```java
@Slf4j
public class SubClassLogin1 extends AbstractTemplate{
    @Override
    protected void call() {
        log.info("비즈니스 로직 실행1");
    }
}
```

```java
@Slf4j
public class SubClassLogin2 extends AbstractTemplate{
    @Override
    protected void call() {
        log.info("비즈니스 로직 실행1");
    }
}
```



**테스트 코드**

```java
/**
	템프릿 메서드 패턴 적용
*/
@Test
void templateMethodV1(){
  AbstractTemplate template1 = new SubClassLogin1();
  template1.exec();
  AbstractTemplate template2 = new SubClassLogin2();
  template2.exec();
}
```

<br><Br>

템플릿 메서드 패턴의 단점은<br>SubClassLogic1, SubClassLogic2처럼 상속받을 자식 클래스를 계속 만들어야한다.<br>이는 **익명내부 클래스**를 사용해서 해결할 수 있다

**익명 내부 클래스**란?<br>클래스를 따로 만들지 않고, <br>**객체 인스턴스를 생성**하면서 **동시에 상속**받아야할 **메소드를 정의**할 수 있다.

 * 익명클래스는 직접 지정하는 이름이 없고 내부에 선언되어있기에
   getClass메소드를 찍어보면
   **현재클래스$1** 이렇게 표시된다.
   ==> class hello.advanced.trace.template.**TemplateMethodTest$2**



**익명 클래스를 활용한 템플릿 메서드 패턴** 예제

```java
/**
* 익명 내부 클래스 활용
*/
@Test
void templateMethodV2(){
  AbstractTemplate template1 = new AbstractTemplate() {
    @Override
    protected void call() {
      log.info("비즈니스로직1 실행");
    }
  };
  template1.exec();

  AbstractTemplate template2 = new AbstractTemplate() {
    @Override
    protected void call() {
      log.info("비즈니스로직2 실행");
    }
  };
  template2.exec();

  /** class hello.advanced.trace.template.TemplateMethodTest$2  */
  log.info("익명내부클래스 template2 이름 => {}", template2.getClass());
}
```



### 템플릿 메서드의 정의

>  템플릿 메서드 디자인 패턴의 목적은 다음과 같습니다.
>  "작업에서 알고리즘의 골격을 정의하고 일부 단계를 하위 클래스로 연기합니다. 템플릿 메서드를 사용하면<br>하위 클래스가 알고리즘의 구조를 변경하지 않고도 알고리즘의 특정 단계를 재정의할 수 있습니다." [GOF]

풀어서 설명하면 다음과 같다.
 부모 클래스에 알고리즘의 **골격인 템플릿을 정의**하고, 일부 **변경되는 로직**은 **자식 클래스에 정의**하는 것이다. <br>이렇게 하면 **자식 클래스가 알고리즘의 전체 구조를 변경**하지 않고, **특정 부분만 재정의**할 수 있다. 결국 **상속과 오버라이딩**을 통한 다형성으로 **문제를 해결**하는 것이다.

**하지만** 부모 클래스의 기능을 사용하든 사용하지 않든 간에 부모 클래스를 강하게 의존하게 된다. 

이를 위해 만들어진 **전략패턴**을 알아보자

------



# 전략패턴

전략 패턴은 **변하지 않는 부분을 Context Class** 라는 곳에 두고<br>**변하는 부분**을 **Strategy 라는 인터페이스**를 만들고<br>상속이 아니라 **위임으로 문제를 해결**

<img width="643" alt="image-20220619004857331" src="https://user-images.githubusercontent.com/58017318/174489484-041c6eb2-761e-490a-a76c-43fb234b866a.png">

> GOF 디자인 패턴에서 정의한 전략 패턴의 의도는 다음과 같다.
> **알고리즘 제품군을 정의**하고 각각을 **캡슐화**하여 상호 교환 가능하게 만들자. <br>전략을 사용하면 알고리즘을 사용하는 클라이언트와 독립적으로 알고리즘을 변경할 수 있다.



**예제1.**변하지 않는 부분을 Context Class

```java
/**
* 필드에 전략을 보관하는 방식
*/
@Slf4j
public class ContextV1 {
  private Strategy strategy;
  public ContextV1(Strategy strategy) {
    this.strategy = strategy;
  }
  
  public void execute() {
    long startTime = System.currentTimeMillis(); //비즈니스 로직 실행
    strategy.call(); //위임
    //비즈니스 로직 종료
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime={}", resultTime);
  }
}
```



**예제1.** 변하는 부분을 Strategy Interface

```java
public interface Strategy {
      void call();
}
```



**예제1.** 변하는 부분 Interface(Strategy) implements 구현체

```java
@Slf4j
public class StrategyLogic1 implements Strategy {
  @Override
  public void call() {
    log.info("비즈니스 로직1 실행"); 
  }
}

@Slf4j
public class StrategyLogic2 implements Strategy {
  @Override
  public void call() {
    log.info("비즈니스 로직2 실행"); 
  }
}
```



위 내용으로 Test코드를 만들면 아래와 같다

##### 1. StrategyLogic1, StrategyLogic2 직접 호출하기

```java
@Test
void strategyV1(){
  StrategyLogic1 strategyLogic1 = new StrategyLogic1();
  ContextV1 contextV1 = new ContextV1(strategyLogic1);
  contextV1.execute();

  StrategyLogic2 strategyLogic2 = new StrategyLogic2();
  ContextV1 contextV2 = new ContextV1(strategyLogic2);
  contextV2.execute();
}
```



##### 2. StrategyLogic1, StrategyLogic2 익명클래스 만들기

```java
@Test
void strategy2(){
  Strategy strategyLogic1 = new Strategy() {
    @Override
    public void call() {
      log.info("비즈니스 로직 실행1");
    }
  };
  ContextV1 contextV1 = new ContextV1(strategyLogic1);
  contextV1.execute();


  Strategy strategyLogic2 = new Strategy() {
    @Override
    public void call() {
      log.info("비즈니스 로직 실행2");
    }
  };
  ContextV1 contextV2 = new ContextV1(strategyLogic2);
  contextV2.execute();
}
```



##### 3. StrategyLogic1, StrategyLogic2 익명클래스 만들기 <br>ContextV1만들면서 Strategy(interface) 구현체 만들기

```java
@Test
void strategy3(){
  // ContextV1생성하면서 구현채를 바로 넣어버리기
  ContextV1 contextV1 = new ContextV1(new Strategy() {
    @Override
    public void call() {
      log.info("비즈니스 로직 실행1");
    }
  });
  contextV1.execute();

  ContextV1 contextV2 = new ContextV1(new Strategy() {
    @Override
    public void call() {
      log.info("비즈니스 로직 실행2");
    }
  });
  contextV2.execute();
}
```



##### 4. StrategyLogic1, StrategyLogic2 익명클래스 만들기 - 람다로 넣기

- 자바8부터 익명내부클래스를 람다로 변경할 수 있다.
- 단, 인터페이스(Strategy)는 메서드가 1개만 있으야 가능하다.

```java
@Test
void strategy4(){
  // ContextV1생성하면서 구현채를 바로 넣어버리는데 [람다]로 바꾸기
  ContextV1 contextV1 = new ContextV1(() -> log.info("비즈니스 로직 실행1"));
  contextV1.execute();

  ContextV1 contextV2 = new ContextV1(() -> log.info("비즈니스 로직 실행2"));
  contextV2.execute();
}
```



## 전략패턴 - 파라미터로 넘기기

```java
/**
 * 파라미터로 전략을 보관하는 방식
 */
@Slf4j
public class ContextV2 {
  public void execute(Strategy strategy){
    long startTime = System.currentTimeMillis();
    //비즈니스 로직 실행
    strategy.call();
    //비즈니스 로직 종료
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime = {}", resultTime);
  }
}
```



```java
@Slf4j
public class ContextV2Test {
  @Test
  void strategyV1(){
    ContextV2 context = new ContextV2();
    context.execute(new StrategyLogic1());

    ContextV2 context2 = new ContextV2();
    context2.execute(new StrategyLogic2());
  }

  @Test
  void stratageV2(){
    ContextV2 context = new ContextV2();
    context.execute(new Strategy() { // 익명함수 그대로 쓰기
      @Override
      public void call() {
        log.info("비즈니스 로직 실행1");
      }
    });

    ContextV2 context2 = new ContextV2();
    context2.execute(() -> log.info("비즈니스 로직 실행2")); // 람다로 익명함수 넘기기
  }
}
```



-----

# 템플릿 콜백 패턴

**콜백**이란? <br>콜백(callback) 또는 콜애프터함수(call-after function)는 <br>**다른 코드의 인수**로써 **넘겨주는 실행 가능한 코드**를 콜백(callback)이라 한다.

쉽게 이야기해서 코드가 호출(call)은 되는데 인수로써 넘겨줬기에 뒤(back)에서 실행된다.

- Context2에서 **콜백은 Strategy**이다.

- 여기서 클라이언트에서 직접 Strategy를 실행하는것이 아니라,<br>클라이언트가 ContextV2.execute(..)를 실행할 때 Strategy를 넘겨주고,<br>ContextV2뒤에서 Strategy가 실행된다.

  - ```java
    ContextV2 context2 = new ContextV2();
    context2.execute(() -> log.info("비즈니스 로직 실행2")); // 람다로 익명함수 넘기기
    ```

- 템플릿 콜백 패턴은 GOF패턴은 아니고, 스프링 안에서만 저렇게 부른다.

- Context -> Template<br>Strategy -> Callback



#### 템플릿 콜백 패턴 예제1

<img width="1128" alt="image-20220619102309758" src="https://user-images.githubusercontent.com/58017318/174489485-6a87cccd-f1ec-48ff-a9af-69360af804ec.png">


**interface Callback.java**

```java
public interface Callback {
  void call();
}
```



**Template.java**

```java
@Slf4j
public class TimeLogTemplate {
  public void execute(Callback callback){
    long startTime = System.currentTimeMillis();
    //비즈니스 로직 실행
    callback.call(); //위임
    //비즈니스 로직 종료
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime = {}", resultTime);
  }
}
```



**실행예제 - 템플릿 콜백 패턴 - 익명 내부 클래스**

```java
@Slf4j
public class TemplateCallbackTest {
  @Test
  void callback(){
    TimeLogTemplate template = new TimeLogTemplate();
    template.execute(() -> log.info("비즈니스 로직 실행"));
    TimeLogTemplate template2 = new TimeLogTemplate();
    template2.execute(() -> log.info("비즈니스 로직2 실행"));
  }
}
```

```
hello.advanced.trace.strategy.TemplateCallbackTest - 비즈니스 로직 실행
hello.advanced.trace.strategy.code.template.TimeLogTemplate - resultTime = 14
hello.advanced.trace.strategy.TemplateCallbackTest - 비즈니스 로직2 실행
hello.advanced.trace.strategy.code.template.TimeLogTemplate - resultTime = 0
```



#### 템플릿 콜백 패턴 예제2

<img width="1517" alt="image-20220619110527019" src="https://user-images.githubusercontent.com/58017318/174489487-d07567e9-8ef8-4948-bac6-f01a8b1a6c40.png">
- Callback interface를 Template의 파라미터로 받는다.

- Callback.call()로 핵심로직을 실행한다.

- OrderContoller5에서 

  - Constructor

    - ```java
      private final OrderServiceV5 orderService;
      private final TraceTemplate template;
      
      public OrderControllerV5(OrderServiceV5 orderService, LogTrace trace) {
        this.orderService = orderService; // 실글톤 주입
        this.template = new TraceTemplate(trace); // new 생성자로 LogTrace 주입
      }
      ```

- request로 해당 로직 실행 시 아래와 같이 적용한다.

  - 

  ```java
  return template.execute("OrderController.request()", ()-> { //람다로 익명함수 정의
    orderService.orderItem(itemId);
    return "ok";
  });
  ```







