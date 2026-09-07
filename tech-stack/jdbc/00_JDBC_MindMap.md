# JDBC Architecture Mind Map

This document presents a structured visual breakdown of low-level JDBC internals.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((JDBC Deep Dive))
    Driver Architecture
      Type Classification
        Type 1 JDBC ODBC Bridge
        Type 2 Native C C Plus Plus API
        Type 3 Middleware Server Proxy
        Type 4 Pure Java Socket Protocol
      Driver Registration
        DriverManager System Property
        ServiceLoader SPI META INF Services
    Connection Pooling HikariCP
      Core Data Structures
        ConcurrentBag ThreadLocal Handoff
        FastList Bounds Elimination
      Pool Operations
        Get Connection Borrow
        Return Connection Recycle
      Pool Tuning SLA
        Connection Formula CPU Cores
        Leak Detection Threshold
    Statement Execution Engines
      Statement Variants
        Statement Static SQL
        PreparedStatement Parameterized
        CallableStatement Stored Procedure
      Query Optimizations
        Server Side Compilation
        Query Execution Plan Cache
        Binary Protocol Payload
    ResultSet Streaming
      Cursor Concurrency Types
        Type Forward Only
        Type Scroll Insensitive
        Type Scroll Sensitive
      Memory Management
        Set Fetch Size Row Chunking
        Integer MIN VALUE MySQL Stream Mode
        JVM Heap OOM Defense
    Transactions and Distributed Systems
      ACID Control
        AutoCommit Boolean
        ANSI Isolation Levels
        Savepoint Rollback Marker
      Distributed Transactions
        XADataSource Specification
        Two Phase Commit 2PC Workflow
```

---

## 2. Component Reference Table

| Component | Responsibility | Performance Bottlenecks & Hazards |
| :--- | :--- | :--- |
| **`DriverManager`** | Manages loaded database driver instances | Synchronized methods causing thread contention in legacy apps |
| **`HikariCP ConcurrentBag`** | Lock-free thread-local connection allocation pool | Pool exhaustion (`SQLTransientConnectionException`) under high load |
| **`PreparedStatement`** | Parameterized SQL query compilation and execution | Driver memory leak if statements are created in loops without `.close()` |
| **`ResultSet`** | Streams database query result rows to JVM memory | Memory OOM crash when executing large queries without `setFetchSize()` |
| **`Savepoint`** | Marks transactional undo checkpoints | High database lock retention when savepoints remain open across long transactions |
| **`XAConnection`** | Manages distributed 2PC transactions | In-doubt XA transactions holding row locks on database crash |
