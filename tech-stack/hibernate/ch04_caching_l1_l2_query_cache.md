# Chapter 4: Multi-Level Caching Architecture (L1, L2 & Query Cache)

Hibernate implements a multi-tier caching hierarchy to reduce database I/O, combining the mandatory session-scoped First-Level Cache with an optional process/cluster-wide Second-Level Cache.

---

## 1. Multi-Tier Caching Hierarchy

```
[ Application Code ]
       |
       v
[ Session (L1 Cache) ]  ---> Scope: Single Thread / Session (Mandatory)
       |                     Stores: Managed Entity References (Identity Map)
       | Miss
       v
[ SessionFactory (L2 Cache) ] -> Scope: Cluster / Process Shared (Optional)
       |                     Stores: Disassembled Dehydrated State Array [Val1, Val2]
       | Miss
       v
[ Database Engine ]
```

---

## 2. Second-Level (L2) Cache Concurrency Strategies

To enable L2 caching, annotate entities with `@Cacheable` and specify a concurrency strategy using `@Cache`:

```java
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "user_cache")
public class User { ... }
```

| Concurrency Strategy | Data Update Model | Consistency SLA | Best Use Case |
| :--- | :--- | :--- | :--- |
| **`READ_ONLY`** | Throws exception if entity is updated. | Immutable | Static reference data (Countries, Currencies). |
| **`NONSTRICT_READ_WRITE`**| Asynchronous unlock on commit. | Eventual | Read-heavy data where minor stale reads are tolerable. |
| **`READ_WRITE`** | Soft-locks cache region during update. | Strong | Standard OLTP data with frequent reads and occasional writes. |
| **`TRANSACTIONAL`** | JTA 2-Phase Commit (XA). | Strict Atomicity | Enterprise financial data requiring full JTA coordination. |

---

## 3. Query Cache & The Hydration Trap

The **Query Cache** stores the results of specific HQL / JPQL queries.

```sql
-- Query Cache Key  : Hash(HQL + Parameters + Region)
-- Query Cache Value: List of Primary Keys [101, 102, 103, 104]
```

### 3.1 The Query Cache Hydration Disaster
> **CRITICAL WARNING:** The Query Cache stores **ONLY Primary Key IDs**, NOT full entity attributes!

```
1. Query Cache executes -> Returns 100 Entity IDs [1..100]
2. Hibernate attempts to hydrate entities from L2 Cache.
3. IF Target Entity is NOT configured for L2 Cache:
   ===> Hibernate fires 100 individual SQL SELECT queries to load each entity by ID! (N+1 Disaster)
```

#### Rule:
Never enable Query Cache on an entity **unless that Entity is also explicitly enabled for L2 Cache**!

---

## 4. Staff Engineer Caching Checklist
1. **L2 Cache Stores Dehydrated Tuples:** L2 cache stores primitive array tuples, not living Java objects. When an entity is fetched from L2 cache, Hibernate constructs a **new Java object instance** in the local session's L1 cache.
2. **Evict L2 Cache on Native Bulk SQL Updates:** Executing native SQL `UPDATE` or `DELETE` statements bypasses Hibernate's session event listeners. Always call `sessionFactory.getCache().evictEntityData()` after executing bulk native DML to prevent stale L2 reads.
3. **Set Eviction Expirations (TTL):** Always configure Time-To-Live (TTL) and max entries in your underlying L2 provider (Ehcache/Infinispan) to prevent unbounded JVM heap consumption.
