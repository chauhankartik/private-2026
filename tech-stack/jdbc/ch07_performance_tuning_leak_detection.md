# Chapter 7: Operational Diagnostics, Connection Leak Detection & Tuning

## 1. Connection Leak Detection & HikariCP Safeguards

A **Connection Leak** occurs when application code borrows a connection from the pool but fails to close it (or return it to the pool) inside a `finally` block or `try-with-resources` statement.

```java
// LEAK HAZARD: Missing close() when an exception occurs!
Connection conn = dataSource.getConnection();
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM users");
// If an exception occurs during ResultSet parsing, conn is NEVER returned to pool!
```

### HikariCP Leak Detection Threshold
Configure `leakDetectionThreshold` (in milliseconds) to log stack trace warnings for connections held longer than expected:

```properties
# Log a leak warning with full stack trace if connection is held > 5 seconds
hikari.leakDetectionThreshold=5000
```

```
[WARN] com.zaxxer.hikari.pool.ProxyConnection - Connection leak detection triggered for connection org.postgresql.jdbc.PgConnection@4b12cf5, stack trace follows:
    at com.example.service.OrderService.processOrder(OrderService.java:42)
    at com.example.controller.OrderController.submit(OrderController.java:18)
```

---

## 2. Timeout Controls Matrix

Network failures, un-indexed slow queries, or database deadlocks can lock application worker threads indefinitely without proper timeout configurations.

```
+-----------------------------------------------------------------------------------+
| JDBC Timeout Layers                                                               |
+-----------------------+-----------------------+-----------------------------------+
| Timeout Parameter     | Config Location       | Purpose                           |
+-----------------------+-----------------------+-----------------------------------+
| `loginTimeout`        | `DriverManager`       | Max time to establish network socket|
|                       |                       | connection to DB.                 |
+-----------------------+-----------------------+-----------------------------------+
| `setQueryTimeout()`   | `Statement`           | Max execution time for a single   |
|                       |                       | SQL query before cancelling.      |
+-----------------------+-----------------------+-----------------------------------+
| `socketTimeout`       | Driver URL Property   | Operating system socket read      |
|                       | (`socketTimeout=10s`) | timeout for network drops.        |
+-----------------------+-----------------------+-----------------------------------+
| `connectionTimeout`   | HikariCP Config       | Max time client thread waits to   |
|                       | (`connectionTimeout`) | borrow connection from pool.      |
+-----------------------+-----------------------+-----------------------------------+
```

---

## 3. Profiling & Logging Proxy Drivers

To monitor raw SQL queries executed by ORM frameworks (Hibernate/JPA) and measure exact JDBC execution latencies, use JDBC proxy drivers:

### 1. `p6spy`
Intercepts JDBC driver calls and logs executed SQL queries with real parameter values filled in:
```properties
# spy.properties
driverlist=org.postgresql.Driver
appender=com.p6spy.engine.spy.appender.StdoutLogger
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(executionTime)ms | %(category) | connection %(connectionId) | %(sqlSingleLine)
```

### 2. `log4jdbc`
Logs SQL execution time, ResultSet data tables, and connection pool events:
```properties
jdbc.drivers=org.postgresql.Driver
# Change connection URL prefix to: jdbc:log4jdbc:postgresql://localhost:5432/mydb
```
