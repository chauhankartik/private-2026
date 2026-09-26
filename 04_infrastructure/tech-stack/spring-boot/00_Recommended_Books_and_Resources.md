# Spring Framework & Spring Boot Recommended Reading List & Technical References

A curated list of authoritative books, official reference documentation, and open-source codebase pointers for mastering Spring Framework, Spring Boot, Spring Security, and Reactive WebFlux internals.

---

## 📚 Recommended Books

1. **_Pro Spring 6: An In-Depth Guide to the Spring Framework (6th Edition)_** — Iuliana Cosmina, Rob Harrop, Chris Schaefer, & Clarence Ho (Apress)
   * **Why Read It:** The definitive deep-dive textbook on Spring Core. Covers `ApplicationContext` mechanics, Bean lifecycle, AOP proxies, transaction management, Spring Data, and Spring Security.
   * **Key Focus:** Comprehensive low-level Spring Framework internals.

2. **_Spring Boot in Action_** — Craig Walls (Manning Publications)
   * **Why Read It:** Excellent explanation of Spring Boot auto-configuration, starter dependencies, Actuator metrics, CLI tools, and environment configuration properties.

3. **_Cloud Native Spring in Action: With Spring Boot and Kubernetes_** — Thomas Vitale (Manning Publications)
   * **Why Read It:** Essential for modern Staff Engineers. Covers building cloud-native microservices with Spring Boot 3, GraalVM AOT native compilation, Spring Cloud Gateway, WebFlux, and Kubernetes deployment.

4. **_Spring Security in Action (2nd Edition)_** — Laurentiu Spilca (Manning Publications)
   * **Why Read It:** In-depth guide to Spring Security filters, `AuthenticationManager`, custom authentication providers, OAuth2, JWT, and CORS/CSRF defenses.

---

## 📄 Official Documentation & Reference Manuals

1. **[Spring Framework Reference Documentation](https://docs.spring.io/spring-framework/reference/)**
   * **Topics:** Core Technologies (IoC container, Resources, AOP, Null-safety), Data Access (Transactions, DAO, ORM), Web on Servlet, Web on Reactive.
2. **[Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)**
   * **Topics:** Auto-configuration mechanics, Starters, Actuator endpoints, Ahead-of-Time (AOT) engine, GraalVM Native Image support.
3. **[Project Reactor Reference Guide](https://projectreactor.io/docs/core/release/reference/)**
   * **Topics:** Reactive Streams specification, `Mono` vs `Flux`, reactive operators (`flatMap`, `concatMap`, `zip`), backpressure, and threading schedulers.

---

## 💻 Source Code References (Java Repository)

Explore core components in the [Spring Framework GitHub Repository](https://github.com/spring-projects/spring-framework) and [Spring Boot GitHub Repository](https://github.com/spring-projects/spring-boot):

* **`spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultSingletonBeanRegistry.java`:** The 3-Level Cache implementation resolving circular dependencies (`singletonObjects`, `earlySingletonObjects`, `singletonFactories`).
* **`spring-aop/src/main/java/org/springframework/aop/framework/DefaultAopProxyFactory.java`:** Factory determining whether to instantiate a `JdkDynamicAopProxy` or `CglibAopProxy`.
* **`spring-tx/src/main/java/org/springframework/transaction/interceptor/TransactionInterceptor.java`:** The AOP interceptor managing declarative `@Transactional` boundaries.
* **`spring-webmvc/src/main/java/org/springframework/web/servlet/DispatcherServlet.java`:** Central front-controller Servlet dispatching HTTP requests to handlers.
* **`spring-boot-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:** Master list of all Spring Boot auto-configurations.
