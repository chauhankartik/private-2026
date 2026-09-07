# Chapter 7: Spring Boot Actuator, Observability, GraalVM Native & Diagnostics

Production readiness requires comprehensive observability, efficient runtime compilation, and structured troubleshooting playbooks for JVM and Spring framework failures.

---

## 1. Spring Boot Actuator & Micrometer Observability

Actuator exposes production-ready operational endpoints for monitoring application health, metrics, and runtime state.

```
Spring Boot Application
       |
       +---> Micrometer Registry ---> Prometheus / Grafana (/actuator/prometheus)
       |
       +---> Actuator Endpoints
               ├── /actuator/health    -> Health Indicators (DB, Disk, Redis)
               ├── /actuator/metrics   -> JVM Memory, GC, HTTP Latency Timers
               ├── /actuator/env       -> Active Spring Profiles & Config Properties
               └── /actuator/heapdump  -> Triggers HPROF Heap Dump file download
```

### 1.1 Custom HealthIndicator
```java
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean isGatewayUp = checkThirdPartyGateway();
        if (isGatewayUp) {
            return Health.up().withDetail("gateway", "Operational").build();
        }
        return Health.down().withDetail("error", "Gateway Connection Refused").build();
    }
}
```

---

## 2. GraalVM Ahead-of-Time (AOT) & Native Images

Spring Boot 3 introduces native compilation via GraalVM and the **Spring AOT Engine**.

```
[ Spring Boot Source Code ]
            |
            v
[ Spring AOT Compiler ]  ---> Evaluates Beans & Auto-Configurations at Build Time
            |                 Generates C++ Native Hints (RuntimeHints)
            v
[ GraalVM native-image ] ---> Compiles Java Bytecode directly to Standalone OS Binary
            |
            v
[ Executable Binary ]    ---> Instant Startup (< 50ms), Low RSS Memory (~50MB)
```

### 2.1 Trade-Offs: JVM vs GraalVM Native

| Metric | Standard JVM Execution | GraalVM Native Image |
| :--- | :--- | :--- |
| **Startup Time** | Slow (2 - 10 seconds JVM warm-up) | **Instant (< 50 milliseconds)** |
| **Memory Footprint (RSS)** | High (~300MB - 1GB initial heap) | **Ultra Low (~40MB - 100MB)** |
| **Peak Throughput** | Higher (JIT C2 compiler optimizes at runtime) | Slightly lower (No runtime JIT profiling) |
| **Reflection / Dynamic Proxies**| Supported dynamically at runtime | Requires explicit `RuntimeHints` build metadata |

---

## 3. Master Diagnostic & Troubleshooting Playbook

### 3.1 Common Spring Boot Exceptions & Root Causes

```
+-----------------------------------------------------------------------------------------+
| Exception Diagnostic Guide                                                              |
|                                                                                         |
| NoSuchBeanDefinitionException: No qualifying bean of type 'com.example.Service'        |
|   ├── Cause: Package outside @ComponentScan path, or @Conditional annotation evaluated false.|
|   └── Fix: Check main application package location or verify property values.           |
|                                                                                         |
| UnsatisfiedDependencyException: Error creating bean with name 'controller':           |
|   ├── Cause: Multiple candidate beans found for interface without @Qualifier.           |
|   └── Fix: Add @Primary to target bean or specify @Qualifier("beanName").               |
|                                                                                         |
| BeanCurrentlyInCreationException: Requested bean is currently in creation:             |
|   ├── Cause: Unresolvable constructor-based circular dependency (A <-> B).               |
|   └── Fix: Refactor to setter injection or annotate parameter with @Lazy.               |
+-----------------------------------------------------------------------------------------+
```

### 3.2 Thread Dump Analysis (Tomcat Starvation)
When HTTP responses stall, fetch a thread dump via `/actuator/threaddump` or `jstack <pid>`:

```text
"http-nio-8080-exec-45" #67 daemon prio=5 os_prio=0 tid=0x00007f WAITING
   java.lang.Thread.State: WAITING (on object monitor)
   at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:180)
   at org.springframework.jdbc.datasource.DataSourceUtils.getConnection(DataSourceUtils.java:80)
```
* **Diagnostic:** Tomcat worker threads are blocked waiting for database connections from HikariCP (`HikariPool.getConnection`).
* **Fix:** Increase `spring.datasource.hikari.maximum-pool-size` or optimize slow database queries holding connections.

---

## 4. Staff Engineer Production SLA Framework
1. **Secure Actuator Endpoints:** Never expose sensitive endpoints like `/actuator/env`, `/actuator/heapdump`, or `/actuator/beans` publicly. Restrict access using Spring Security rules.
2. **Configure HikariCP Pool Sizes:** Align HikariCP pool size with database core capacity ($PoolSize = 2 \times CPU\_Cores + Effective\_Spindle\_Count$) to avoid connection pool thrashing.
3. **Set Micrometer Timed Metrics:** Annotate critical business methods with `@Timed` to automatically generate percentiles ($p50, p95, p99$) in Prometheus.
