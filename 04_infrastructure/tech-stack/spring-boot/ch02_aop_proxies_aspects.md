# Chapter 2: Aspect-Oriented Programming (AOP) & Proxy Architecture

Aspect-Oriented Programming (AOP) complements Object-Oriented Programming (OOP) by enabling modularization of cross-cutting concerns (logging, security, transaction management, caching, metrics) without cluttering business logic.

---

## 1. Spring AOP Core Concepts

```
[ Client Call ] ---> [ AOP Proxy ] ---> Interceptor Chain (Advice) ---> [ Target Instance ]
```

* **Aspect:** A module encapsulating a cross-cutting concern (annotated with `@Aspect`).
* **JoinPoint:** A specific execution point in the application (in Spring AOP, always a method execution).
* **Pointcut:** A predicate matching JoinPoints (e.g., `execution(* com.example.service.*.*(..))`).
* **Advice:** Action taken at a JoinPoint:
  * `@Before`: Executes prior to method invocation.
  * `@AfterReturning`: Executes after successful method completion.
  * `@AfterThrowing`: Executes if method throws an exception.
  * `@After`: Executes regardless of method outcome (finally block).
  * `@Around`: Wraps method execution (`ProceedingJoinPoint.proceed()`), allowing modification of arguments, return values, or short-circuiting.

---

## 2. Dynamic Proxies: JDK Dynamic vs CGLIB

Spring AOP implements proxy-based AOP. At runtime, Spring wraps target beans inside a proxy object.

```
                  DefaultAopProxyFactory
                             |
         +-------------------+-------------------+
         |                                       |
  Target implements Interface?            proxyTargetClass = true OR no Interfaces
         |                                       |
         v                                       v
[ JDK Dynamic Proxy ]                   [ CGLIB Subclass Proxy ]
(java.lang.reflect.Proxy)              (Enhancer Bytecode Subclassing)
```

### 2.1 Mechanical Comparison

| Dimension | JDK Dynamic Proxy | CGLIB Proxy |
| :--- | :--- | :--- |
| **Requirement** | Target bean **must implement an interface**. | Target class can be a **concrete class** (no interfaces needed). |
| **Mechanism** | Generates dynamic proxy implementing target interface via `java.lang.reflect.Proxy` & `InvocationHandler`. | Generates dynamic subclass of target class using bytecode manipulation (`Enhancer` & `MethodInterceptor`). |
| **Limitations** | Cannot proxy methods not declared in interface. | Cannot proxy `final` classes or `final` methods (subclassing blocked). |
| **Spring Boot Default**| Default in Spring Framework legacy. | **Default in Spring Boot 2.x / 3.x** (`spring.aop.proxy-target-class=true`). |

---

## 3. The Self-Invocation Trap & Resolution

Because Spring AOP is proxy-based, advice is executed **only when calls enter through the external Proxy object**.

```
[ External Client ] ---> [ AOP Proxy ] ---> methodA() (Aspect Executes!)
                                                 |
                                                 | Internal call: this.methodB()
                                                 v
                                           methodB() (ASPECT BYPASSED!)
```

### 3.1 The Bug
If `methodA()` invokes `this.methodB()` internally within the same class, `methodB()` executes directly on the target instance (`this`). Any `@Transactional`, `@Async`, or `@Cacheable` annotations on `methodB()` are **completely ignored**!

### 3.2 Resolution Strategies

#### Strategy 1: Architectural Refactoring (Recommended)
Extract `methodB()` into a separate Spring Bean component.

```java
@Service
public class OrderService {
    @Autowired
    private PaymentProcessor paymentProcessor;

    public void processOrder() {
        paymentProcessor.executePayment(); // Enters via Proxy!
    }
}
```

#### Strategy 2: Self-Injection with `@Lazy`
Inject the proxy of the service into itself.

```java
@Service
public class OrderService {
    @Autowired @Lazy
    private OrderService self;

    public void methodA() {
        self.methodB(); // Enters via self Proxy!
    }
}
```

#### Strategy 3: `AopContext.currentProxy()`
Expose the proxy explicitly (requires `@EnableAspectJAutoProxy(exposeProxy = true)`).

```java
public void methodA() {
    ((OrderService) AopContext.currentProxy()).methodB();
}
```

---

## 4. Staff Engineer AOP Best Practices
1. **Prefer Composition Over `AopContext` Hacks:** Refactor self-invoking methods into dedicated helper beans rather than coupling code to `AopContext`.
2. **Beware Final Methods with CGLIB:** In Spring Boot, methods marked `final` will silently bypass AOP interceptors because CGLIB cannot override final methods in generated subclasses.
3. **Use Specific Pointcuts:** Avoid broad pointcut expressions like `execution(* *(..))` which force Spring to evaluate proxies across all container beans, increasing startup overhead.
