# Chapter 4: Data Access, Spring Data JPA & Transaction Management

Declarative transaction management and object-relational mapping (ORM) are critical components of enterprise Spring applications.

---

## 1. Declarative Transaction Architecture (`@Transactional`)

Spring manages database transactions using AOP proxying and the `PlatformTransactionManager` abstraction.

```
[ Client Request ]
       |
       v
[ TransactionInterceptor (AOP Proxy) ]
       |
       ├── 1. Obtains Connection from PlatformTransactionManager
       ├── 2. Disables Auto-Commit (connection.setAutoCommit(false))
       ├── 3. Binds Connection to ThreadLocal via TransactionSynchronizationManager
       |
       v
[ Target Service Method Executed ]
       |
       ├── Success?  ===> PlatformTransactionManager.commit()
       └── Exception? ===> Checks Rollback Rules -> PlatformTransactionManager.rollback()
```

---

## 2. Transaction Propagation Behaviors

When a transactional method calls another transactional method, **Propagation Behaviors** define how transaction boundaries are managed.

```
OuterMethod() [@Transactional]
     |
     +---> InnerMethod() [@Transactional(propagation = Propagation.xxx)]
```

| Propagation Behavior | Mechanism / Isolation Effect |
| :--- | :--- |
| **`REQUIRED`** (Default) | Joins outer transaction if present; creates a new transaction if none exists. Exceptions in inner method mark **entire transaction rollback-only**. |
| **`REQUIRES_NEW`** | **Suspends outer transaction**, creates a brand new independent transaction. Inner commit/rollback operates independently of outer transaction. |
| **`NESTED`** | Executes within a nested transaction using **JDBC Savepoints**. Inner rollback reverts to Savepoint without forcing outer transaction rollback. |
| **`SUPPORTS`** | Executes within transaction if present; executes non-transactionally if none exists. |
| **`MANDATORY`** | Requires existing outer transaction; throws `IllegalTransactionStateException` if none exists. |
| **`NOT_SUPPORTED`** | Suspends outer transaction and executes non-transactionally. |
| **`NEVER`** | Throws exception if an outer transaction exists. |

---

## 3. The Spring Transaction Rollback Trap

By default, Spring `@Transactional` **only rolls back on Unchecked Exceptions** (`RuntimeException` and `Error`).

```java
// DANGER: Checked Exception (IOException) will NOT trigger transaction rollback!
@Transactional
public void processOrder() throws IOException {
    orderRepository.save(order);
    throw new IOException("Disk Full"); // Transaction COMMITS!
}

// FIX: Explicitly specify rollbackFor
@Transactional(rollbackFor = Exception.class)
public void processOrderFixed() throws IOException {
    orderRepository.save(order);
    throw new IOException("Disk Full"); // Transaction ROLLS BACK!
}
```

---

## 4. Spring Data JPA & Hibernate Mechanics

### 4.1 Persistence Context (L1 Cache) & Dirty Checking
* **Persistence Context (L1 Cache):** Bound to the active `EntityManager` / `@Transactional` session. Guarantees entity identity (`entityA == entityB`) within the same transaction.
* **Automatic Dirty Checking:** Hibernate snapshots entity state when loaded into the L1 cache. Upon transaction commit (`flush()`), Hibernate compares the current state against the snapshot and generates SQL `UPDATE` statements automatically without explicit `repository.save()` calls.

### 4.2 The N+1 Query Problem & Resolution
Occurs when fetching a list of $N$ parent entities with `@OneToMany` lazy relationships, causing $1$ query for parents plus $N$ additional queries for children.

```sql
-- Query 1 (Fetch 100 Orders)
SELECT * FROM orders;

-- Queries 2 to 101 (Fired in a loop for each Order's Items!)
SELECT * FROM order_items WHERE order_id = ?;
```

#### Resolutions:
1. **`JOIN FETCH` in JPQL:**
   ```java
   @Query("SELECT o FROM Order o JOIN FETCH o.items")
   List<Order> findAllWithItems();
   ```
2. **`@EntityGraph` Annotation:**
   ```java
   @EntityGraph(attributePaths = {"items"})
   List<Order> findAll();
   ```

---

## 5. Staff Engineer Data Access Checklist
1. **Always Set `rollbackFor = Exception.class`:** Ensure `@Transactional(rollbackFor = Exception.class)` is used on business services to prevent partial commits on checked exceptions.
2. **Beware `REQUIRES_NEW` Connection Exhaustion:** Long-running operations inside `@Transactional(propagation = Propagation.REQUIRES_NEW)` hold two physical database connections concurrently (the suspended outer connection + active inner connection), risking HikariCP pool exhaustion.
3. **Disable OSIV (`spring.jpa.open-in-view=false`):** Open-Session-In-View keeps database connections open throughout the entire HTTP request rendering phase. Disable OSIV in production to prevent connection starvation.
