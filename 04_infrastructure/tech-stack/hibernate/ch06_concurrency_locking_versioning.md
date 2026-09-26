# Chapter 6: Concurrency Control & Locking Mechanisms

Concurrent database access requires locking mechanisms to prevent **Lost Updates** and maintain data consistency under multi-threaded execution.

---

## 1. Optimistic Locking (`@Version`)

Optimistic locking assumes data conflicts are rare. Instead of holding database locks during user think-time, it uses a version field to detect concurrent modifications at commit time.

```java
@Entity
public class Account {
    @Id @GeneratedValue
    private Long id;

    private BigDecimal balance;

    @Version
    private Long version; // Integer, Long, or Timestamp
}
```

### 1.1 Optimistic Lock Execution Mechanics

```
Transaction A (Reads Account v1) ──┐
                                  ├──> Updates Name, increments version to 2
Transaction B (Reads Account v1) ──┘
                                         |
                                         v
Transaction A Commits FIRST:
  UPDATE accounts SET balance = 500, version = 2 WHERE id = 1 AND version = 1;
  ===> 1 Row Updated (SUCCESS!)
                                         |
                                         v
Transaction B Commits SECOND:
  UPDATE accounts SET balance = 300, version = 2 WHERE id = 1 AND version = 1;
  ===> 0 Rows Updated! (CONFLICT DETECTED!)
  ===> Throws OptimisticLockException / StaleObjectStateException!
```

---

## 2. Pessimistic Locking (`SELECT ... FOR UPDATE`)

Pessimistic locking acquires explicit database-level row locks during query execution, blocking concurrent transactions until the locking transaction completes.

```java
// Acquires Exclusive Row Lock (SELECT ... FOR UPDATE)
Account account = entityManager.find(
    Account.class, 
    1L, 
    LockModeType.PESSIMISTIC_WRITE,
    Map.of("jakarta.persistence.lock.timeout", 3000) // 3-second timeout
);
```

### 2.1 Pessimistic Lock Modes

| Lock Mode | SQL Generated | Behavior / Scope |
| :--- | :--- | :--- |
| **`PESSIMISTIC_READ`** | `SELECT ... FOR SHARE` | Shared Lock. Prevents concurrent transactions from acquiring exclusive locks. |
| **`PESSIMISTIC_WRITE`**| `SELECT ... FOR UPDATE` | **Exclusive Lock**. Blocks all concurrent reads and writes on the locked rows. |
| **`PESSIMISTIC_FORCE_INCREMENT`** | `SELECT ... FOR UPDATE` | Exclusive Lock + automatically increments the `@Version` attribute. |

---

## 3. Optimistic vs Pessimistic Locking Decision Matrix

| Metric | Optimistic Locking (`@Version`) | Pessimistic Locking (`FOR UPDATE`) |
| :--- | :--- | :--- |
| **Database Lock Overhead** | **Zero**. No DB rows locked. | High. Holds active row locks in DB memory. |
| **Concurrency Scaling** | **High**. Ideal for web APIs with low collision. | Low. High contention causes blocked threads. |
| **User Think-Time Support**| **Supported** across HTTP request cycles. | **Forbidden**. Holding DB locks across HTTP calls exhausts connections. |
| **Failure Resolution** | Retries application logic on exception. | Queues execution behind DB lock. |

---

## 4. Staff Engineer Concurrency SLA Framework
1. **Annotate All Business Entities with `@Version`:** Default to optimistic locking on all core entities to detect lost updates across detached web sessions.
2. **Never Hold Pessimistic Locks Across Network Calls:** Always acquire pessimistic locks (`PESSIMISTIC_WRITE`) inside a short, dedicated transaction. Releasing locks quickly prevents connection pool starvation.
3. **Always Set Lock Timeouts:** Pass `jakarta.persistence.lock.timeout` hints (e.g. 2000ms) on pessimistic queries to prevent threads from blocking indefinitely during database deadlocks.
