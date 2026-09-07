# Chapter 1: Hibernate Architecture & Bootstrap Engine

Hibernate ORM operates as an abstraction layer between object-oriented Java domain models and relational database tables, managing JDBC connections, SQL dialect translation, and transaction boundaries.

---

## 1. Bootstrap Registry Sequence

The Hibernate bootstrap sequence constructs the runtime metadata model and initializes services before creating the `SessionFactory`.

```
[ Configuration Properties / Annotations ]
                    |
                    v
[ BootstrapServiceRegistry ]  ---> Loads ClassLoaderService & IntegratorService
                    |
                    v
[ StandardServiceRegistry ]   ---> Configures ConnectionProvider (HikariCP), Dialect,
                    |              and TransactionCoordinatorServices
                    v
[ MetadataSources ]           ---> Scans @Entity classes & ORM XML mappings
                    |
                    v
[ Metadata ]                  ---> Validated in-memory Object-Relational Metadata
                    |
                    v
[ SessionFactory ]            ---> Heavyweight, thread-safe runtime engine
```

---

## 2. Component Scopes: `SessionFactory`, `Session` & `StatelessSession`

```
+-------------------------------------------------------------------+
| SessionFactory (Thread-Safe, Cluster-Scoped Singleton)            |
| - Caches compiled HQL/JPQL ASTs, Metadata, and L2 Cache Regions   |
|                                                                   |
|   ├── Session 1 (Thread-Unsafe, Unit-of-Work, L1 Cache)           |
|   ├── Session 2 (Thread-Unsafe, Unit-of-Work, L1 Cache)           |
|   └── StatelessSession (Bulk Ingestion Engine - No L1 Cache)      |
+-------------------------------------------------------------------+
```

### 2.1 Mechanical Comparison

| Dimension | `SessionFactory` | `Session` (Stateful) | `StatelessSession` |
| :--- | :--- | :--- | :--- |
| **Thread Safety** | **Thread-Safe**. Shared across application threads. | **Thread-Unsafe**. Must be confined to a single thread/transaction. | **Thread-Unsafe**. Single-thread bulk processor. |
| **Lifecycle Scope** | Application / Cluster lifetime. | Short-lived (Single HTTP request / transaction). | Short-lived (Bulk job execution). |
| **L1 Cache Support**| N/A | **Yes**. Maintains L1 Cache & Identity Map. | **No**. Bypasses L1 Cache completely. |
| **Dirty Checking** | N/A | **Yes**. Automatic snapshot comparison on flush. | **No**. Must explicitly call `update()` / `insert()`. |
| **Primary Use Case**| Building Sessions & caching L2 data. | Standard OLTP business transactions. | High-volume batch ETL data processing ($100,000+$ rows). |

---

## 3. The Dialect Engine & Connection Provider

### 3.1 Dialect Abstraction
The `Dialect` class encapsulates vendor-specific SQL syntax differences across database engines (PostgreSQL, MySQL, Oracle, MS SQL Server).
* **Responsibilities:** Formats paging queries (`LIMIT/OFFSET` vs `FETCH FIRST`), maps Java types to SQL types, specifies sequence syntax, handles locking syntax (`FOR UPDATE`).

### 3.2 Connection Provider (HikariCP Integration)
Hibernate delegates physical JDBC connection creation to a `ConnectionProvider`:

```properties
# hibernate.properties configuration
hibernate.connection.provider_class=org.hibernate.hikaricp.internal.HikariCPConnectionProvider
hibernate.hikari.maximumPoolSize=30
hibernate.hikari.minimumIdle=10
hibernate.hikari.idleTimeout=600000
```

---

## 4. Staff Engineer Architecture Rules
1. **Never Create Multiple `SessionFactory` Instances:** Creating multiple `SessionFactory` instances duplicates metadata storage and destroys connection pool efficiency. Maintain exactly one `SessionFactory` per database.
2. **Confine `Session` to Unit-of-Work:** Never store a `Session` in an instance field or share it across concurrent threads (`Session is not thread-safe`).
3. **Use `StatelessSession` for Batch Operations:** When updating or inserting $> 10,000$ rows in a batch job, use `StatelessSession` to avoid `OutOfMemoryError` caused by L1 Cache accumulation.
