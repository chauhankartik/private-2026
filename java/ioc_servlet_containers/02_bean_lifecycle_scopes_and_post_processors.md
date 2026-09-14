# Chapter 2: Bean Lifecycle, Scopes & Post-Processors

Detailed mechanics of the Bean execution pipeline, lifecycle callbacks, scopes, and `BeanPostProcessor` proxy generation.

---

## 📌 1. Complete Bean Lifecycle Pipeline (8 Steps)

```mermaid
flowchart TD
    Step1["1. Parse BeanDefinition (Class, Scope, Props)"] --> Step2["2. Instantiate Raw Object (Constructor Reflection)"]
    Step2 --> Step3["3. Populate Properties & Inject Dependencies (@Inject)"]
    Step3 --> Step4["4. Invoke Aware Callbacks (BeanNameAware, ApplicationContextAware)"]
    Step4 --> Step5["5. BeanPostProcessor.postProcessBeforeInitialization()"]
    Step5 --> Step6["6. Initialization Callback (@PostConstruct / afterPropertiesSet)"]
    Step6 --> Step7["7. BeanPostProcessor.postProcessAfterInitialization() (AOP Proxy Wrap!)"]
    Step7 --> Step8["8. Ready for Production Usage"]
    Step8 -- Container Shutdown --> Step9["9. Destruction Callback (@PreDestroy / destroy)"]
```

---

## 📌 2. Bean Scopes Comparison

| Scope | Instance Lifetime & Allocation Strategy | Best Use Case |
|---|---|---|
| **Singleton** *(Default)* | Exactly 1 shared instance per IoC container instance. Cached in `singletonObjects`. | Stateless services, Repositories, Controllers. |
| **Prototype** | Creates a new instance every time `getBean()` or injection occurs. Never cached. | Stateful objects, Builders, Command objects. |
| **Request** | 1 instance per HTTP Request lifecycle. Bound to `HttpServletRequest` attributes. | Web user search parameters, request tracing. |
| **Session** | 1 instance per HTTP Session. Bound to `HttpSession` attributes. | Shopping Cart, User Authentication state. |
| **Application** | 1 instance per `ServletContext` web application scope. | Web app global settings. |

---

## 📌 3. Custom `BeanPostProcessor` for AOP Proxy Wrapping

`BeanPostProcessor` allows intercepting bean creation to wrap raw instances in Dynamic Proxies (used by `@Transactional`, `@Async`, and Security Filters).

```mermaid
sequenceDiagram
    participant IoC as IoC Container Engine
    participant Raw as Raw OrderServiceImpl Instance
    participant BPP as Custom AOP BeanPostProcessor
    participant Proxy as JDK Dynamic Proxy / CGLIB Proxy

    IoC->>Raw: Instantiate Raw OrderServiceImpl
    IoC->>Raw: Inject Dependencies
    IoC->>BPP: postProcessAfterInitialization(Raw Instance, "orderService")
    BPP->>Proxy: Proxy.newProxyInstance(orderService, LogMethodInterceptor)
    BPP-->>IoC: Returns Proxy Instance (Replaces Raw Instance in IoC Cache!)
    IoC->>IoC: Put Proxy into singletonObjects Map!
```

### Java Code: Custom Logging `BeanPostProcessor`
```java
package com.custom.ioc;

import java.lang.reflect.Proxy;

public class LoggingBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // Run prior to @PostConstruct
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        
        // Wrap with JDK Dynamic Proxy if class has @LogExecution annotation
        if (beanClass.isAnnotationPresent(LogExecution.class)) {
            return Proxy.newProxyInstance(
                beanClass.getClassLoader(),
                beanClass.getInterfaces(),
                (proxy, method, args) -> {
                    System.out.println("[LOG START] Executing method: " + method.getName());
                    long start = System.currentTimeMillis();
                    
                    Object result = method.invoke(bean, args);
                    
                    long elapsed = System.currentTimeMillis() - start;
                    System.out.println("[LOG END] Completed " + method.getName() + " in " + elapsed + " ms");
                    return result;
                }
            );
        }
        return bean;
    }
}
```
