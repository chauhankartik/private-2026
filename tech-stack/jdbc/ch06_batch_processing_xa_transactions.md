# Chapter 6: High-Throughput Batch Processing & Distributed XA Transactions

## 1. High-Throughput JDBC Batch Execution

Executing `INSERT` statements in a standard loop generates heavy network round-trip overhead:

```java
// SLOW: 10,000 Network Round-Trips!
for (User user : users) {
    pstmt.setString(1, user.getName());
    pstmt.executeUpdate();
}
```

### Batch Execution API:
Grouping queries into a batch reduces network round-trips to a single network payload:

```java
connection.setAutoCommit(false);
PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)");

int count = 0;
for (User user : users) {
    pstmt.setString(1, user.getName());
    pstmt.setString(2, user.getEmail());
    pstmt.addBatch(); // Adds parameter set to batch buffer
    
    if (++count % 1000 == 0) {
        pstmt.executeBatch(); // Executes 1,000 statements in 1 network payload
    }
}
pstmt.executeBatch(); // Execute remaining
connection.commit();
```

### MySQL Batch Rewriting (`rewriteBatchedStatements=true`)
By default, MySQL executes batch statements as multiple individual SQL commands separated by semicolons (`INSERT ...; INSERT ...;`). Adding `rewriteBatchedStatements=true` rewrites the batch into a single bulk insert query (`INSERT INTO users VALUES (?), (?), (?)`), improving write throughput by **300% to 500%**.

---

## 2. Distributed Transactions & XA Two-Phase Commit (2PC)

When a single business transaction spans multiple independent database instances (e.g., PostgreSQL Order DB and MySQL Payment DB), standard `java.sql.Connection` commits cannot guarantee distributed ACID consistency.

JDBC supports distributed transactions via the **Java Transaction API (JTA)** and **`javax.sql.XADataSource`**.

```
 Transaction Manager (JTA / Atomikos / Narayana)
        |
        +-----------------------------------+
        |                                   |
        v Phase 1: PREPARE                  v Phase 1: PREPARE
 +--------------------+              +--------------------+
 | Resource Manager A |              | Resource Manager B |
 | (XADataSource DB1) |              | (XADataSource DB2) |
 +--------------------+              +--------------------+
        |                                   |
        | Returns Prepared Vote OK          | Returns Prepared Vote OK
        +-----------------------------------+
        |
        v Phase 2: COMMIT
        +-----------------------------------+
        |                                   |
        v Commit                            v Commit
 +--------------------+              +--------------------+
 | DB 1 Committed     |              | DB 2 Committed     |
 +--------------------+              +--------------------+
```

### XA Execution Protocol Steps:
1. **`XAConnection` Registration:** The transaction manager acquires `XAConnection` instances wrapping underlying physical database connections.
2. **Phase 1 (Prepare):** The transaction manager issues `XAResource.prepare(xid)` to all databases. Databases write mutations to persistent WAL disk logs, acquire row locks, and return `XA_OK`.
3. **Phase 2 (Commit):** If all databases vote `XA_OK`, the transaction manager issues `XAResource.commit(xid)`. If any database votes abort, all databases execute `XAResource.rollback(xid)`.
