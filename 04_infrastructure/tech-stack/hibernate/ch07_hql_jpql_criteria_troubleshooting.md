# Chapter 7: HQL / JPQL, Criteria API & Performance Diagnostics

Mastering object-oriented query languages and diagnosing production Hibernate exceptions are essential skills for senior backend engineers.

---

## 1. HQL / JPQL AST Query Compilation Engine

HQL and JPQL operate directly on entity classes and object properties rather than relational database tables and columns.

```
HQL Query String ("SELECT o FROM Order o WHERE o.status = :status")
                          |
                          v ANTLR Lexer / Parser
[ Abstract Syntax Tree (AST) ]
                          |
                          v Target Dialect Translation (e.g. PostgreSQLDialect)
Native SQL String ("SELECT o.id AS id1_0_, o.status AS status2_0_ FROM orders o WHERE o.status = ?")
```

### 1.1 Constructor Projections (DTO Queries)
Avoid fetching full entity graphs when creating read-only API responses:

```java
// Bypasses Persistence Context tracking for maximum read performance!
TypedQuery<OrderDTO> query = entityManager.createQuery(
    "SELECT new com.example.dto.OrderDTO(o.id, o.orderDate, o.totalAmount) " +
    "FROM Order o WHERE o.status = :status", OrderDTO.class
);
```

---

## 2. Type-Safe Criteria API

The Criteria API provides a programmatically constructed, type-safe alternative to HQL string queries.

```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Order> cq = cb.createQuery(Order.class);
Root<Order> order = cq.from(Order.class);
Join<Order, Item> items = order.join("items", JoinType.LEFT);

Predicate statusPredicate = cb.equal(order.get("status"), OrderStatus.COMPLETED);
Predicate amountPredicate = cb.greaterThan(order.get("totalAmount"), new BigDecimal("100"));

cq.select(order).where(cb.and(statusPredicate, amountPredicate)).distinct(true);
List<Order> results = entityManager.createQuery(cq).getResultList();
```

---

## 3. Production Troubleshooting Playbook

### 3.1 Resolving `LazyInitializationException`

```text
org.hibernate.LazyInitializationException: could not initialize proxy [com.example.User#1] - no Session
```

* **Cause:** Application code attempted to access a `LAZY` association or collection *after* the owning `Session` / `@Transactional` boundary was closed.
* **Fixes:**
  1. Add `JOIN FETCH` to the original query loading the entity.
  2. Use `@EntityGraph` to dynamically load the required association eagerly.
* **ANTI-PATTERN WARNING:** Never set `hibernate.enable_lazy_load_no_trans = true`! This setting opens a new temporary database connection for *every* lazy property access, causing severe connection pool exhaustion in production.

### 3.2 Hibernate Statistics & Performance Telemetry
Enable Hibernate Statistics in application configuration to monitor runtime performance metrics:

```properties
hibernate.generate_statistics=true
```

```java
// Inspecting Statistics MBean programmatically
Statistics stats = sessionFactory.getStatistics();
long entityLoadCount = stats.getEntityLoadCount();
long l2CacheHitCount = stats.getSecondLevelCacheHitCount();
double cacheHitRatio = stats.getSecondLevelCacheHitCount() / (double) (stats.getSecondLevelCacheHitCount() + stats.getSecondLevelCacheMissCount());
```

---

## 4. Staff Engineer Production SLA Framework
1. **Never Enable `enable_lazy_load_no_trans` in Production:** Treat `LazyInitializationException` as a signal to fix your data fetching boundaries with `JOIN FETCH` or DTO projections.
2. **Use SQL Parameter Logging in Staging:** Enable `org.hibernate.type.descriptor.sql` logging in test environments to verify actual SQL parameter bindings.
3. **Monitor `hibernate.generate_statistics`:** Alert if `QueryExecutionMaxTime` exceeds SLA targets (e.g. $> 500\text{ ms}$).
