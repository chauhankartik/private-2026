# Hibernate ORM Recommended Reading List & Technical References

A curated list of authoritative books, official user guides, and open-source codebase pointers for mastering Hibernate ORM, JPA, persistence performance tuning, and SQL execution mechanics.

---

## 📚 Recommended Books

1. **_High-Performance Java Persistence_** — Vlad Mihalcea (Independently Published)
   * **Why Read It:** The absolute gold standard book on Java data access and Hibernate performance. Covers JDBC batching, connection pooling, identifier generators, inheritance trade-offs, L1/L2 caching, dirty checking algorithms, locking, and resolving N+1 query overhead.
   * **Key Focus:** Hibernate ORM performance tuning and low-level SQL optimization.

2. **_Java Persistence with Hibernate (2nd Edition)_** — Christian Bauer & Gavin King (Manning Publications)
   * **Why Read It:** Written by Gavin King (the creator of Hibernate). The comprehensive architectural bible covering entity mappings, composite keys, inheritance strategies, HQL/JPQL, criteria queries, and transaction management.

3. **_Pro JPA 2: Mastering the Java Persistence API_** — Mike Keith & Merrick Schincariol (Apress)
   * **Why Read It:** In-depth guide to standard Jakarta Persistence (JPA) annotations, EntityManager lifecycle, criteria queries, entity graphs, and object-relational mapping patterns.

---

## 📄 Official Documentation & Reference Manuals

1. **[Hibernate ORM User Guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html)**
   * **Topics:** Domain model mapping, basic types, embeddables, entity associations, inheritance, HQL/JPQL parser, Criteria API, L2 caching configuration.
2. **[Jakarta Persistence Specification (JPA 3.1 Spec)](https://jakarta.ee/specifications/persistence/)**
   * **Topics:** Standard API contracts (`EntityManager`, `EntityManagerFactory`, `EntityTransaction`), query language specification, locking modes (`PESSIMISTIC_WRITE`, `OPTIMISTIC`).

---

## 💻 Source Code References (Java Repository)

Explore core engine components in the [Hibernate ORM GitHub Repository](https://github.com/hibernate/hibernate-orm):

* **`hibernate-core/src/main/java/org/hibernate/internal/SessionFactoryImpl.java`:** Heavyweight, thread-safe `SessionFactory` implementation.
* **`hibernate-core/src/main/java/org/hibernate/internal/SessionImpl.java`:** Unit-of-work `Session` managing entity persistence context.
* **`hibernate-core/src/main/java/org/hibernate/engine/spi/ActionQueue.java`:** DML execution queue maintaining deterministic SQL operation ordering (`Executable` objects).
* **`hibernate-core/src/main/java/org/hibernate/engine/internal/StatefulPersistenceContext.java`:** First-level (L1) cache implementation enforcing entity identity maps and dirty checking snapshots.
