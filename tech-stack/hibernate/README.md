# Hibernate ORM Architecture & Performance — Deep Dive Study Guide

> **Goal:** Master Hibernate ORM bootstrap engine, `SessionFactory`/`Session` unit of work, Entity Lifecycle state transitions, Persistence Context (L1 Cache & Automatic Dirty Checking), Inheritance mapping strategies, Multi-Level Caching (L2 & Query Cache), Fetching Strategies (resolving N+1 queries), Optimistic/Pessimistic Locking, and HQL/Criteria performance diagnostics for Staff Software Engineering.

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

👉 **Full Mind Map & Taxonomy:** [`00_Hibernate_MindMap.md`](00_Hibernate_MindMap.md)  
📚 **Recommended Books & References:** [`00_Recommended_Books_and_Resources.md`](00_Recommended_Books_and_Resources.md)

---

## 📖 Chapter Index

1. **[Ch 1: Hibernate Architecture & Bootstrap Engine](ch01_architecture_session_factory.md)** — Bootstrap sequence, `ServiceRegistry`, `SessionFactory` vs `Session` vs `StatelessSession`, Dialect engine, HikariCP integration.
2. **[Ch 2: Entity Lifecycle & Persistence Context](ch02_entity_states_persistence_context.md)** — Entity states (`Transient`, `Persistent`, `Detached`, `Removed`), state transitions, Persistence Context (L1 Cache), ActionQueue execution ordering, Automatic Dirty Checking.
3. **[Ch 3: Mapping Strategies & Inheritance Models](ch03_mapping_strategies_inheritance.md)** — Primary key generation strategies (`SEQUENCE`, `IDENTITY`, `TABLE`), `@Embeddable` components, Inheritance mappings (`SINGLE_TABLE`, `JOINED`, `TABLE_PER_CLASS`).
4. **[Ch 4: Multi-Level Caching Architecture (L1, L2 & Query Cache)](ch04_caching_l1_l2_query_cache.md)** — First-Level (L1) vs Second-Level (L2) Cache (Ehcache/Infinispan), L2 concurrency strategies (`READ_WRITE`, `TRANSACTIONAL`), Query Cache hydration.
5. **[Ch 5: Fetching Strategies & Performance Optimization](ch05_fetching_strategies_n_plus_1.md)** — `LAZY` vs `EAGER` defaults, N+1 Select problem, resolutions via `JOIN FETCH`, `@EntityGraph`, `@BatchSize`, and `@Fetch(FetchMode.SUBSELECT)`.
6. **[Ch 6: Concurrency Control & Locking Mechanisms](ch06_concurrency_locking_versioning.md)** — `@Version` Optimistic locking (`StaleObjectStateException`), Pessimistic locking (`PESSIMISTIC_WRITE` / `FOR UPDATE`), deadlock prevention.
7. **[Ch 7: HQL / JPQL, Criteria API & Performance Diagnostics](ch07_hql_jpql_criteria_troubleshooting.md)** — HQL / JPQL AST engine, CriteriaBuilder type-safe queries, `hibernate.generate_statistics`, fixing `LazyInitializationException` and session leaks.
