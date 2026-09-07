# Hibernate ORM Architecture Taxonomy & Interactive Mind Map

Hibernate ORM is an enterprise object-relational mapping framework for Java. It maps idiomatic domain model object graphs to relational database tables, managing SQL generation, state persistence, caching, and transaction boundaries.

---

## 🧠 Interactive Hibernate Architecture Mind Map

```mermaid
mindmap
  root(("Hibernate ORM"))
    "01 Architecture & Bootstrap"
      "ServiceRegistry Bootstrap Chain"
      "SessionFactory - Immutable Thread Safe Cluster Scope"
      "Session - Unit of Work Thread Unsafe"
      "StatelessSession - High Speed Bulk Engine"
    "02 Entity States & Persistence"
      "Transient - New Unmanaged Entity"
      "Persistent - Managed L1 Cache State"
      "Detached - Closed Session Entity"
      "Removed - Scheduled for SQL Delete"
      "ActionQueue & Automatic Dirty Checking"
    "03 Mapping & Inheritance"
      "Key Generators - SEQUENCE IDENTITY TABLE"
      "Embeddables & ElementCollections"
      "Inheritance - SINGLE TABLE"
      "Inheritance - JOINED"
      "Inheritance - TABLE PER CLASS"
    "04 Caching Architecture"
      "First Level L1 Cache - Session Scope"
      "Second Level L2 Cache - SessionFactory Scope"
      "L2 Concurrency - READ WRITE TRANSACTIONAL"
      "Query Cache Hydration"
    "05 Fetching & N Plus 1"
      "LAZY vs EAGER Default Fetch Types"
      "N Plus 1 Select Problem Mechanics"
      "Resolutions - JOIN FETCH"
      "Resolutions - EntityGraph & BatchSize"
    "06 Concurrency & Locking"
      "Version Field - Optimistic Locking"
      "StaleObjectStateException Handling"
      "Pessimistic Locking - FOR UPDATE"
    "07 Query Engines & Diagnostics"
      "HQL JPQL AST Compiler Engine"
      "CriteriaBuilder Type Safe API"
      "Hibernate Statistics & SQL Logging"
      "LazyInitializationException Fixes"
```

---

## 📊 Core Component Matrix

| Subsystem | Core Interface / Class | Primary Responsibility | Key Mechanism |
| :--- | :--- | :--- | :--- |
| **Bootstrap Engine** | `StandardServiceRegistryBuilder`, `MetadataSources` | Builds service registry, parses mappings, produces `SessionFactory` | XML / Annotation parsing, Dialect resolution |
| **Session Manager** | `SessionFactory`, `Session` | Manages database connections, entity lifecycle, and unit of work | JDBC Connection Pool wrapper, L1 Cache |
| **Persistence Context** | `StatefulPersistenceContext` | Enforces entity identity (`a == b`), dirty checking, ActionQueue ordering | Identity Map, Snapshot array comparisons |
| **Action Queue** | `ActionQueue` | Queues and orders SQL DML operations prior to transaction commit | Inserts $\to$ Updates $\to$ Collection Deletes $\to$ Deletes |
| **L2 Cache Engine** | `RegionFactory`, `DomainDataRegion` | Shared cross-session caching for entity state and collection data | Ehcache / Infinispan / Hazelcast integration |
| **Query Engine** | `HqlParser`, `CriteriaBuilderImpl` | Translates HQL, JPQL, or Criteria queries into native SQL dialects | ANTLR AST parser, Dialect SQL translator |
