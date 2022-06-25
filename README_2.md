# 프록시패턴과 데코레이터 패턴

프록시 객체가 중간에 있어서 **접근 제어**와 **부가 기능 추가**를 수행할 수 있다.

## 첫번째 프록시 패턴을 통해 접근제어 기능을 확인해보자



일단, 프록시 적용 전 아래와 같이 만든다.

<img width="646" alt="image-20220620233755735" src="https://user-images.githubusercontent.com/58017318/175777194-5a8cc241-bfcc-41bc-ae60-e4201cf12914.png">


```java
public interface Subject {
  String operation();
}
```

```java
@Slf4j
public class RealSubject implements Subject {
  @Override
  public String operation() {
    log.info("실제 객체 호출"); sleep(1000);
    return "data";
  }
  
  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
}
```

```java
public class ProxyPatternClient {
  private Subject subject;
  
  public ProxyPatternClient(Subject subject) {
    this.subject = subject;
  }
  
  public void execute() {
    subject.operation();
  }
}
```



여기서 테스트 코드를 작성해보면 총 데이터를 3번 불러오기에 **3초가 걸린다.**

```java
@Test
void noProxyTest() {
  RealSubject realSubject = new RealSubject();
  ProxyPatternClient client = new ProxyPatternClient(realSubject);
  client.execute(); //1초
  client.execute(); //1초
  client.execute(); //1초
}
```



여기서 **캐시**기능을 할 수 있는 **접근제어 프록시**가 존재하게 된다면<br>즉, **프록시 패턴을 적용**하면 아래와 같다.

<img width="647" alt="image-20220620234120358" src="https://user-images.githubusercontent.com/58017318/175777200-10033a2e-d484-42a0-ac07-04b3e1058cef.png">


즉, 기존 RealSubject과 Client코드를 건들이지 않고 아래와 같이 추가한다.

```java
@Slf4j
public class CacheProxy implements Subject {
  private Subject target;
  private String cacheValue;
  
  public CacheProxy(Subject target) {
    this.target = target;
  }
  
  @Override
  public String operation() {
    log.info("프록시 호출");
    if (cacheValue == null) {
      cacheValue = target.operation();
    }
    return cacheValue;
  }
}
```

> operation() : 구현한 코드를 보면 cacheValue 에 값이 없으면 실제 객체( target )를 호출해서 값을 구한다. <br>그리고 구한 값을 cacheValue 에 저장하고 반환한다.<br>따라서 **처음 조회 이후** 캐시에서 매우 **빠르게 데이터를 조회**할 수 있다.



```java
@Test
void cacheProxyTest() {
  Subject realSubject = new RealSubject();
  Subject cacheProxy = new CacheProxy(realSubject);
  ProxyPatternClient client = new ProxyPatternClient(cacheProxy);
  client.execute(); // 1초
  client.execute(); // 프록시객체로 인해 1초도 안걸림
  client.execute(); // 프록시객체로 인해 1초도 안걸림
}
```



> **정리**<br>프록시 패턴의 핵심은 **RealSubject 코드와 클라이언트 코드를 전혀 변경하지 않고**, 프록시를 도입해서 **접근 제어**를 했다는 점이다.
>  그리고 클라이언트 코드의 변경 없이 **자유롭게 프록시를 넣고 뺄 수 있다**. 실제 클라이언트 입장에서는 프록시 객체가 주입되었는지, 실제 객체가 주입되었는지 알지 못한다.
