# Chapter 5: Fetching Strategies & Performance Optimization

Improper fetch configuration is the primary cause of latency degradation in Hibernate applications.

---

## 1. Default JPA Fetch Types

| Relationship | Default JPA Fetch Type | Recommended Override |
| :--- | :--- | :--- |
| **`@ManyToOne`** | `FetchType.EAGER` | **`FetchType.LAZY`** (Must explicitly override!) |
| **`@OneToOne`** | `FetchType.EAGER` | **`FetchType.LAZY`** |
| **`@OneToMany`** | `FetchType.LAZY` | `FetchType.LAZY` |
| **`@ManyToMany`** | `FetchType.LAZY` | `FetchType.LAZY` |

> **Rule:** Always set `@ManyToOne(fetch = FetchType.LAZY)`! EAGER fetching on to-one relationships triggers cartesian product joins or unexpected secondary select queries.

---

## 2. The N+1 Select Problem Mechanics

```
Query 1: SELECT * FROM orders; -- (Fetches 100 Orders)

For each order in 100 orders:
    order.getItems().size(); -- Triggers 100 individual SELECT queries!
    --> SELECT * FROM order_items WHERE order_id = ?;
```

Total SQL Queries Executed: **$1 + 100 = 101$ DB Roundtrips** (Causes database thread pool starvation).

---

## 3. 4 Architectural Solutions for N+1 Queries

```
+-------------------------------------------------------------------+
| 1. JPQL JOIN FETCH                                                |
| SELECT o FROM Order o JOIN FETCH o.items                          |
| => Single SQL JOIN returning parents + children in 1 query.      |
+-------------------------------------------------------------------+
| 2. JPA @EntityGraph                                               |
| @EntityGraph(attributePaths = {"items"})                         |
| => Dynamic runtime override converting LAZY paths to JOINs.       |
+-------------------------------------------------------------------+
| 3. Batch Fetching (@BatchSize)                                   |
| @BatchSize(size = 25) on Collection                               |
| => Uses IN clause: SELECT * FROM items WHERE order_id IN (?,?...)|
| => Reduces 100 queries to 4 queries (1 + 100/25).                 |
+-------------------------------------------------------------------+
| 4. Subselect Fetching (@FetchMode.SUBSELECT)                      |
| @Fetch(FetchMode.SUBSELECT) on Collection                         |
| => Uses subquery: WHERE order_id IN (SELECT id FROM orders...)    |
| => Reduces 100 queries to exactly 2 queries.                      |
+-------------------------------------------------------------------+
```

---

## 4. MultipleBagFetchException & How to Avoid It

Attempting to `JOIN FETCH` two or more `@OneToMany` collections represented as `List` (Bags) throws a `MultipleBagFetchException`.

* **Cause:** Joining multiple bags produces a **Cartesian Product** ($M \times N$ rows), leading to duplicate data and memory explosion.
* **Resolutions:**
  1. Change collection type from `List<Item>` to `Set<Item>` (Hibernate deduplicates sets).
  2. Split query into multiple queries: `JOIN FETCH` collection 1 in Query 1, then `JOIN FETCH` collection 2 in Query 2 (Persistence context automatically merges them).

---

## 5. Staff Engineer Fetching SLA Framework
1. **Default All Associations to `LAZY`:** Override every `@ManyToOne` and `@OneToOne` to `LAZY`. Fetch eagerly only at the query boundary using `JOIN FETCH` or `@EntityGraph`.
2. **Use DTO Projections for Read-Only Dashboards:** For read-only reports, bypass entities completely. Query Constructor Projections (`SELECT new com.example.OrderDTO(o.id, o.amount) FROM Order o`) to avoid Persistence Context tracking overhead.
3. **Set `@BatchSize` Globally:** Define `hibernate.default_batch_fetch_size = 25` in application properties as a safety net against accidental N+1 lazy loading loops.
