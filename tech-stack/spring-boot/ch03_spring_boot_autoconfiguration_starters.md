# Chapter 3: Spring Boot Auto-Configuration & Starter Mechanics

Spring Boot auto-configuration automatically configures Spring application beans based on classpath dependencies, property files, and existing bean definitions, eliminating boilerplate XML/Java configurations.

---

## 1. Anatomy of `@SpringBootApplication`

The `@SpringBootApplication` annotation placed on the main entry point is a composite annotation combining three core annotations:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootConfiguration // 1. Marks class as @Configuration
@EnableAutoConfiguration // 2. Triggers Auto-Configuration discovery
@ComponentScan           // 3. Scans package & sub-packages for @Component
public @interface SpringBootApplication { ... }
```

---

## 2. Auto-Configuration Discovery Mechanism

How does Spring Boot discover auto-configuration classes provided by starter libraries on the classpath?

```
Spring Boot Startup
        |
        v
@EnableAutoConfiguration
        |
        v
AutoConfigurationImportSelector
        |
        v Reads Config File
[ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports ]
(Spring Boot 3.x Format)
        |
        v
Loads List of Candidate Auto-Configuration Classes (e.g. DataSourceAutoConfiguration)
        |
        v
Evaluates @Conditional Annotations on Each Class
        |
        +---> Conditions Met?   ===> Register Beans in ApplicationContext
        +---> Conditions Failed? ===> Skip Configuration
```

### 2.1 Spring Boot 2.x vs 3.x Discovery Files
* **Spring Boot 2.x:** Discovered via `META-INF/spring.factories` key-value pairs:
  ```properties
  org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.example.MyAutoConfiguration
  ```
* **Spring Boot 3.x:** Discovered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` containing plain class names line-by-line:
  ```text
  com.example.MyAutoConfiguration
  ```

---

## 3. Conditional Annotations Deep-Dive

Auto-configuration relies on `@Conditional` annotations to safely instantiate beans only when specific conditions are satisfied.

```java
@AutoConfiguration
@ConditionalOnClass(DataSource.class) // 1. Classpath check
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean // 2. User bean check (App code takes precedence)
    @ConditionalOnProperty(name = "spring.datasource.type") // 3. Property check
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
```

| Annotation | Evaluation Trigger |
| :--- | :--- |
| **`@ConditionalOnClass`** | Passes if specified class is present on the JVM classpath. |
| **`@ConditionalOnMissingBean`** | Passes only if no bean of the specified type already exists in the `ApplicationContext` (allows user overrides!). |
| **`@ConditionalOnProperty`** | Passes if specified environment property matches `havingValue` or is present (`matchIfMissing`). |
| **`@ConditionalOnWebApplication`**| Passes only if application is running in a web context (Servlet or Reactive). |

---

## 4. Building a Custom Spring Boot Starter

Creating enterprise custom starters standardizes cross-cutting libraries (e.g., custom security SDKs, audit loggers, metrics collectors) across microservices.

### 4.1 Recommended Module Structure
```
my-custom-spring-boot-starter/
├── my-custom-spring-boot-autoconfigure/   # Contains autoconfiguration logic & @Beans
│   ├── src/main/java/com/example/config/
│   │   ├── MyFeatureAutoConfiguration.java
│   │   └── MyFeatureProperties.java
│   └── src/main/resources/
│       └── META-INF/spring/
│           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── my-custom-spring-boot-starter/           # Empty POM aggregator for dependencies
    └── pom.xml
```

---

## 5. Staff Engineer Auto-Configuration Rules
1. **Always Use `@ConditionalOnMissingBean` on Library Beans:** When publishing internal company starter libraries, wrap `@Bean` methods with `@ConditionalOnMissingBean` to allow downstream service teams to override default behavior seamlessly.
2. **Debug Auto-Configuration with Condition Evaluation Report:** Run Spring Boot with `--debug` or inspect the `/actuator/conditions` endpoint to troubleshoot why an expected auto-configuration bean was or was not instantiated.
3. **Use Spring Boot 3.x `.imports` Files:** Ensure new custom starters use the modern `AutoConfiguration.imports` location for compatibility with Spring Boot 3.x and GraalVM AOT hints.
