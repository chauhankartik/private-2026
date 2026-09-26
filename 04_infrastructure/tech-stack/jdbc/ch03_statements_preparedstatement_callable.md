# Chapter 3: Statement Execution Engines & PreparedStatement Internals

## 1. Statement Class Hierarchy

JDBC provides three statement interfaces under `java.sql.Statement` for executing SQL commands:

```
                          java.sql.Statement
                           (Static SQL Strings)
                                    |
                                    v
                       java.sql.PreparedStatement
                      (Parameterized Compiled SQL)
                                    |
                                    v
                       java.sql.CallableStatement
                     (Stored Procedures & Out Params)
```

---

## 2. SQL Injection Defense Mechanics

Raw `Statement` usage relies on string concatenation, exposing applications to **SQL Injection (SQLi)** vulnerabilities:

```java
// DANGEROUS CODE: Vulnerable to SQL Injection
String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
Statement stmt = connection.createStatement();
ResultSet rs = stmt.executeQuery(query);
```
* If `userInput` is `' OR '1'='1`, the database evaluates `WHERE username = '' OR '1'='1'`, returning all user records.

### How `PreparedStatement` Prevents SQL Injection:
```java
// SECURE CODE: PreparedStatement Parameter Binding
String query = "SELECT * FROM users WHERE username = ?";
PreparedStatement pstmt = connection.prepareStatement(query);
pstmt.setString(1, userInput);
ResultSet rs = pstmt.executeQuery();
```
* `PreparedStatement` treats parameter placeholders (`?`) strictly as literal byte values. Even if `userInput` contains `' OR '1'='1`, the database searches for a literal username string matching `"' OR '1'='1"`.

---

## 3. Server-Side Prepared Statement Compilation & Binary Protocol

Beyond security, `PreparedStatement` significantly improves performance when executed repeatedly.

```
 Client Application                                             Database Server Engine
        |                                                                 |
 1. prepareStatement("SELECT * FROM orders WHERE status = ?")            |
        | ----------------- PREPARE SQL Request ------------------------->|
        |                                                                 | 2. Parses SQL Syntax
        |                                                                 | 3. Generates Query Execution Plan
        |                                                                 | 4. Assigns Statement Handle ID: 0x01
        |<---------------- Returns Statement Handle (0x01) ---------------|
        |                                                                 |
 5. setString(1, "PAID"); executeQuery();                                 |
        | ----------------- EXECUTE 0x01 (Param: "PAID") ---------------->|
        |                                                                 | 6. Reuses Cached Query Execution Plan!
        |<---------------- Returns Binary Protocol ResultSet ------------|
```

### JDBC Driver Configuration Flags:
To enable true server-side prepared statement compilation in MySQL and PostgreSQL drivers:

1. **MySQL (`Connector/J`):**
   ```properties
   useServerPrepStmts=true
   cachePrepStmts=true
   prepStmtCacheSize=250
   prepStmtCacheSqlLimit=2048
   ```
2. **PostgreSQL (`pgjdbc`):**
   ```properties
   prepareThreshold=5   # Switches from client-side emulation to server-side PREPARE after 5 executions
   ```
