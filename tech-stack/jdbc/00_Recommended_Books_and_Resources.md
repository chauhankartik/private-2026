# Recommended Books & Resources: JDBC & Database Connectivity

A curated collection of authoritative books, specification documents, architectural guides, and open-source repositories for mastering JDBC.

---

## 1. Essential Books & Specifications

1. **Java Database Programming with JDBC (2nd Edition)**  
   *Author:* George Reese  
   *Focus:* Complete guide to low-level JDBC API, driver architectures, transaction isolation, connection pooling, and batch processing.

2. **High-Performance Java Persistence**  
   *Author:* Vlad Mihalcea  
   *Focus:* The definitive practical guide on JDBC performance tuning, connection pooling (HikariCP), statement batching, fetch size optimization, locking, and transaction isolation levels.

3. **Oracle JDBC 4.3 Specification (JSR 221)**  
   *Publisher:* Oracle / Java Community Process  
   *Focus:* Official API specification detailing standard JDBC interfaces, `DriverManager`, `DataSource`, rowsets, and `XADataSource` distributed transactions.

---

## 2. Technical Documentation & Manuals

* **[HikariCP Architecture Wiki](https://github.com/brettwooldridge/HikariCP/wiki)** — In-depth technical articles on HikariCP lock-free `ConcurrentBag`, array optimizations (`FastList`), and connection pool sizing math.
* **[PostgreSQL JDBC Driver Documentation (`pgjdbc`)](https://jdbc.postgresql.org/documentation/)** — Server-side prepared statements, binary transfer protocol, and connection tuning for PostgreSQL.
* **[MySQL Connector/J Developer Guide](https://dev.mysql.com/doc/connector-j/en/)** — Configuration options (`useServerPrepStmts`, `rewriteBatchedStatements`), cursor streaming modes, and connection failover.

---

## 3. Open-Source Codebases to Explore

* **[HikariCP Repository (GitHub)](https://github.com/brettwooldridge/HikariCP)** — Java source code for HikariCP (`HikariPool`, `ConcurrentBag`, `FastList`).
* **[PostgreSQL JDBC Driver Repository (`pgjdbc`)](https://github.com/pgjdbc/pgjdbc)** — Open-source Java driver implementing PostgreSQL wire protocol v3.
* **[MySQL Connector/J Repository (GitHub)](https://github.com/mysql/mysql-connector-j)** — Official Pure Java Type 4 driver implementation for MySQL.
