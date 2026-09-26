# Chapter 10: Exceptions (Items 69 – 77)

Proper exception design, handling, exception translation, and failure atomicity.

---

## 📌 Item 69: Use exceptions only for exceptional conditions

Exceptions are designed for error handling, not for normal control flow or terminating loops. Using try-catch for normal control flow is slow and hides bugs.

---

## 📌 Item 70: Use checked exceptions for recoverable conditions and runtime exceptions for programming errors

- **Checked Exceptions**: Force caller to handle or propagate error condition (use when caller can recover).
- **Runtime Exceptions**: Denote programming errors / precondition contract violations (`IndexOutOfBoundsException`, `NullPointerException`).

---

## 📌 Item 71: Avoid unnecessary use of checked exceptions

Overusing checked exceptions creates boilerplate try-catch blocks across calling layers. If a caller cannot recover, use unchecked exceptions or return an `Optional`.

---

## 📌 Item 72: Favor the use of standard exceptions

Reuse Java standard exceptions where applicable:

| Exception | Common Usage |
|---|---|
| `IllegalArgumentException` | Invalid non-null parameter value |
| `IllegalStateException` | Object state invalid for method call |
| `NullPointerException` | Null parameter where non-null required |
| `IndexOutOfBoundsException` | Index parameter out of bounds |
| `ConcurrentModificationException` | Concurrent modification detected |
| `UnsupportedOperationException` | Object does not support method |

---

## 📌 Item 73: Throw exceptions appropriate to the abstraction

Higher layers should catch lower-level exceptions and throw exceptions that fit the higher-level abstraction (**Exception Translation**).

```java
try {
    // Low-level database access...
} catch (SQLException e) {
    // Translate low-level SQL error to higher-level domain exception:
    throw new DataAccessException("Failed to load user credentials", e);
}
```

---

## 📌 Item 74: Document all exceptions thrown by each method

Document checked and unchecked exceptions with `@throws` tags in Javadoc comments.

---

## 📌 Item 75: Include failure-capture information in detail messages

Include values of key parameters causing the failure in exception messages to simplify debugging and log analysis.

---

## 📌 Item 76: Strive for failure atomicity

A failed method invocation should leave the object in the state it was in prior to the invocation.

### Strategies for Failure Atomicity
1. Use immutable objects (naturally atomic).
2. Validate parameters before modifying object state.
3. Perform operations on temporary copies before applying to actual object.

---

## 📌 Item 77: Don't ignore exceptions

An empty catch block (`catch (Exception e) {}`) masks bugs and hides failures. If an exception must be ignored, log it and document the exact reason.
