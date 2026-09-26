# Chapter 5: Transaction Control, Isolation Levels & Savepoints

## 1. JDBC Transaction Control (`autoCommit`)

By default, every newly opened JDBC connection runs in **Auto-Commit Mode** (`autoCommit = true`). Every individual DML statement (`INSERT`, `UPDATE`, `DELETE`) is immediately committed as an independent transaction upon completion.

### Explicit Transaction Boundaries:
To group multiple SQL statements into a single atomic transaction, disable `autoCommit`:

```java
try {
    connection.setAutoCommit(false); // Begin explicit transaction
    
    updateAccountBalance(connection, fromAccount, -500);
    updateAccountBalance(connection, toAccount, +500);
    
    connection.commit(); // Commit all operations atomically
} catch (SQLException e) {
    connection.rollback(); // Roll back all operations on failure
    throw e;
} finally {
    connection.setAutoCommit(true); // Reset connection state before returning to pool
}
```

---

## 2. ANSI SQL Isolation Levels in JDBC

Developers configure transaction isolation levels per connection to balance concurrency against read phenomena (Dirty Reads, Non-Repeatable Reads, Phantom Reads):

```java
connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
```

| JDBC Isolation Level | Dirty Reads | Non-Repeatable Reads | Phantom Reads | Database Lock / MVCC Overhead |
| :--- | :--- | :--- | :--- | :--- |
| `TRANSACTION_READ_UNCOMMITTED` | Allowed | Allowed | Allowed | Lowest |
| `TRANSACTION_READ_COMMITTED` (Default) | Prevented | Allowed | Allowed | Low (Read snapshot per query) |
| `TRANSACTION_REPEATABLE_READ` | Prevented | Prevented | Allowed | Medium (Read snapshot per txn) |
| `TRANSACTION_SERIALIZABLE` | Prevented | Prevented | Prevented | Highest (Pessimistic predicate locks) |

---

## 3. Savepoint Mechanics & Partial Rollbacks

A **`Savepoint`** creates an intermediate undo checkpoint within an active transaction, allowing partial rollbacks without aborting the entire transaction.

```java
connection.setAutoCommit(false);

// Step 1: Insert Parent Order
insertOrder(connection, orderId);
Savepoint orderSavepoint = connection.setSavepoint("OrderCreated");

try {
    // Step 2: Attempt Non-Critical Inventory Allocation
    allocateInventory(connection, orderId);
} catch (SQLException e) {
    // Partial Rollback: Undo inventory attempt, keep Parent Order intact!
    connection.rollback(orderSavepoint);
    flagOrderForManualReview(connection, orderId);
}

// Step 3: Commit Parent Order
connection.commit();
```

```
 Transaction Start -> Insert Order -> [ Savepoint: OrderCreated ] -> Allocate Inventory (FAILS!)
                                                                                |
                                                                                v Partial Rollback
 Transaction Commit <- Insert Order <- [ Savepoint: OrderCreated ] <------------+
```
