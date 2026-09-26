# Spring Framework & Spring Boot Architecture & Production Internals — Deep Dive Study Guide

> **Goal:** Master Spring IoC container mechanics, 3-level cache circular dependency resolution, AOP dynamic proxies (JDK vs CGLIB), Spring Boot auto-configuration & custom starters, `@Transactional` propagation behaviors, Spring MVC vs Reactive WebFlux, Spring Security filter chains, Actuator observability, GraalVM AOT native compilation, and production troubleshooting for Staff Software Engineering.

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

👉 **Full Mind Map & Taxonomy:** [`00_SpringBoot_MindMap.md`](00_SpringBoot_MindMap.md)  
📚 **Recommended Books & References:** [`00_Recommended_Books_and_Resources.md`](00_Recommended_Books_and_Resources.md)

---

## 📖 Chapter Index

1. **[Ch 1: Spring IoC Container & Bean Lifecycle](ch01_ioc_container_bean_lifecycle.md)** — `ApplicationContext` vs `BeanFactory`, `BeanDefinition`, full lifecycle phases, `BeanPostProcessor`, 3-Level Cache circular dependency resolution.
2. **[Ch 2: Aspect-Oriented Programming (AOP) & Proxy Architecture](ch02_aop_proxies_aspects.md)** — JDK Dynamic Proxies vs CGLIB, `@Aspect`, `@Around`, `JoinPoint`, `AopProxyFactory`, self-invocation trap & fixes.
3. **[Ch 3: Spring Boot Autoconfiguration & Starter Mechanics](ch03_spring_boot_autoconfiguration_starters.md)** — `@SpringBootApplication`, `AutoConfiguration.imports`, `@ConditionalOnProperty`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`, building custom starters.
4. **[Ch 4: Data Access, Spring Data JPA & Transaction Management](ch04_spring_data_jpa_transactions.md)** — `@Transactional`, `PlatformTransactionManager`, propagation (`REQUIRED`, `REQUIRES_NEW`, `NESTED`), Hibernate L1/L2 cache, N+1 query resolution (`JOIN FETCH`, `EntityGraph`).
5. **[Ch 5: Web Layer: Spring MVC vs Spring WebFlux](ch05_spring_mvc_webflux_reactive.md)** — `DispatcherServlet` pipeline, Tomcat thread-per-request vs Project Reactor (`Mono`, `Flux`) Netty event-loop non-blocking I/O.
6. **[Ch 6: Spring Security Architecture & Filter Chain Mechanics](ch06_spring_security_architecture.md)** — `DelegatingFilterProxy`, `FilterChainProxy`, `SecurityFilterChain`, `AuthenticationManager`, `SecurityContextHolder` (`ThreadLocal`), JWT & OAuth2.
7. **[Ch 7: Spring Boot Actuator, Observability, GraalVM Native & Diagnostics](ch07_actuator_metrics_graalvm_troubleshooting.md)** — Actuator endpoints, Micrometer/Prometheus, GraalVM AOT native compilation, thread dump starvation analysis, and `BeanCreationException` debugging.
