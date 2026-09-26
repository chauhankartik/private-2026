# Spring Framework & Spring Boot Architecture Taxonomy & Mind Map

Spring Framework and Spring Boot form the enterprise backbone of modern Java backend development, providing dependency injection, aspect-oriented programming, declarative transactions, auto-configuration, reactive web stacks, and enterprise security.

---

## 🧠 Interactive Spring Boot Architecture Mind Map

```mermaid
mindmap
  root(("Spring Boot Architecture"))
    "01 IoC & Bean Lifecycle"
      "ApplicationContext & BeanFactory"
      "BeanDefinition & Registry"
      "Full Lifecycle - PostProcessors Init Destroy"
      "3-Level Cache Circular Dependency Resolution"
    "02 AOP & Dynamic Proxies"
      "JDK Dynamic Proxy - Interface Reflection"
      "CGLIB Proxy - Bytecode Subclassing"
      "Aspects Pointcuts & JoinPoints"
      "AopProxyFactory & Self-Invocation Trap"
    "03 Auto-Configuration & Starters"
      "SpringBootApplication Annotation Trio"
      "AutoConfiguration.imports Discovery"
      "Conditional Annotations - OnProperty OnClass OnBean"
      "Custom Starter Modules"
    "04 Data Access & Transactions"
      "Transactional Interceptor & PlatformTransactionManager"
      "Propagation Behaviors - REQUIRED REQUIRES_NEW NESTED"
      "Hibernate Persistence Context - L1 Cache & Dirty Checking"
      "N Plus 1 Query Resolution - Join Fetch EntityGraph"
    "05 Web Layer - MVC vs WebFlux"
      "DispatcherServlet Execution Pipeline"
      "Tomcat Thread-Per-Request Model"
      "Spring WebFlux - Project Reactor Mono Flux"
      "Netty Event Loop Non-Blocking I/O"
    "06 Spring Security Architecture"
      "DelegatingFilterProxy & FilterChainProxy"
      "SecurityFilterChain Filter Sequence"
      "AuthenticationManager & SecurityContextHolder ThreadLocal"
      "Stateless JWT & OAuth2 Resource Server"
    "07 Actuator & Observability"
      "Actuator Endpoints & Custom HealthIndicators"
      "Micrometer Metrics & Prometheus Exporting"
      "GraalVM Native Images & Spring AOT"
      "Troubleshooting - Thread Dumps & BeanCreationException"
```

---

## 📊 Core Component Matrix

| Subsystem | Core Interfaces / Annotations | Primary Responsibility | Key Mechanism |
| :--- | :--- | :--- | :--- |
| **IoC Container** | `ApplicationContext`, `BeanPostProcessor`, `@Autowired` | Manages object instantiation, dependency injection, and scope lifecycles | Reflection, `BeanDefinitionRegistry`, 3-Level Cache |
| **AOP Engine** | `@Aspect`, `@Around`, `AopProxyFactory` | Decouples cross-cutting concerns (logging, security, metrics, transactions) | Dynamic Proxying (JDK Dynamic vs CGLIB) |
| **Auto-Configuration**| `@EnableAutoConfiguration`, `@ConditionalOnClass` | Automatically configures Spring beans based on classpath dependencies | `AutoConfiguration.imports` discovery |
| **Transaction Manager**| `@Transactional`, `PlatformTransactionManager` | Manages database transaction boundaries, commits, and rollbacks | AOP proxy wrapping, `TransactionSynchronizationManager` |
| **Web MVC** | `DispatcherServlet`, `@RestController` | Handles HTTP request routing, parameter binding, and response serialization | Servlet API, `HandlerMapping`, `HandlerAdapter` |
| **WebFlux** | `WebHandler`, `Mono<T>`, `Flux<T>` | Non-blocking reactive HTTP request handling | Reactive Streams, Project Reactor, Netty event loop |
| **Spring Security** | `SecurityFilterChain`, `AuthenticationManager` | Authenticates requests and authorizes access to endpoints and methods | Servlet Filter chain, `SecurityContextHolder` (`ThreadLocal`) |
