# Chapter 4: ResultSet Streaming, Fetch Size & Cursor Management

## 1. ResultSet Cursor Types & Concurrency Modes

When creating statements, developers can specify `ResultSet` cursor navigation properties and concurrency rules:

```java
Statement stmt = connection.createStatement(
    ResultSet.TYPE_FORWARD_ONLY,
    ResultSet.CONCUR_READ_ONLY
);
```

### Cursor Type Matrix:

| ResultSet Type | Navigation Ability | Visibility of Concurrent DB Mutations | Memory Overhead |
| :--- | :--- | :--- | :--- |
| `TYPE_FORWARD_ONLY` (Default) | Sequential forward iteration (`next()`) | None | **Lowest** (Streams rows) |
| `TYPE_SCROLL_INSENSITIVE` | Bidirectional (`previous()`, `absolute(5)`) | Insensitive to external updates | **High** (Buffers result snapshot in JVM/Driver memory) |
| `TYPE_SCROLL_SENSITIVE` | Bidirectional | Reflects live database updates while cursor is open | **Highest** (Maintains active server cursor handles) |

---

## 2. Memory Management: Default Fetch Size vs `setFetchSize(int)`

By default, many JDBC drivers (e.g., PostgreSQL `pgjdbc`) fetch **all matching rows** from the database server in a single response network payload, storing every row in JVM Heap RAM.

```
 Query Matching 10,000,000 Rows (Default Fetch Size = 0 / All)
 Database Server -----(Sends 10M Rows over Socket)-----> JVM Heap RAM
                                                                |
                                                                v
                                              OutOfMemoryError (OOM Crash!)
```

### Fixing OOM Crashes with `setFetchSize()`:
Passing `setFetchSize(int rows)` instructs the driver to fetch rows in bounded chunks over an open database cursor:

```java
connection.setAutoCommit(false); // Required by PostgreSQL for cursor streaming
PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM massive_log_table");
pstmt.setFetchSize(1000); // Streams 1,000 rows at a time over network socket

ResultSet rs = pstmt.executeQuery();
while (rs.next()) {
    // Process single row
}
```

```
 Database Server  ==[Chunk 1: 1,000 Rows]==> Driver Buffer  ==> Processed
                  ==[Chunk 2: 1,000 Rows]==> Driver Buffer  ==> Processed
                  (JVM Heap memory consumption remains strictly bounded!)
```

---

## 3. Driver-Specific Cursor Streaming Modes

### MySQL Driver (`mysql-connector-j`) Streaming Mode
MySQL's driver ignores standard `setFetchSize(1000)` unless specific streaming flags are set:

```java
// Enabling true row-by-row streaming in MySQL Connector/J
Statement stmt = connection.createStatement(
    ResultSet.TYPE_FORWARD_ONLY, 
    ResultSet.CONCUR_READ_ONLY
);
stmt.setFetchSize(Integer.MIN_VALUE); // MySQL magic constant for row-by-row streaming!

ResultSet rs = stmt.executeQuery("SELECT * FROM huge_table");
```
* **Caution in MySQL:** While `Integer.MIN_VALUE` streaming mode prevents JVM OOM, no other queries can be executed on that physical `Connection` until the full `ResultSet` has been iterated and closed.
