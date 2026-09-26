# The Builder Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Constructing complex objects with many optional parameters using constructors causes **Telescoping Constructor Anti-Pattern**:

```java
// Anti-Pattern: Telescoping Constructors
public HttpRequest(String url) { ... }
public HttpRequest(String url, String method) { ... }
public HttpRequest(String url, String method, Map<String, String> headers) { ... }
public HttpRequest(String url, String method, Map<String, String> headers, String body) { ... }
public HttpRequest(String url, String method, Map<String, String> headers, String body, int timeout) { ... }
```

### Problems with JavaBeans Setters:
Using `setMethod("POST")`, `setBody("...")` allows objects to exist in **partially constructed or mutable states**, breaking thread safety and immutability.

---

## 🏗️ The Builder Solution

The **Builder Pattern** separates the construction of a complex object from its representation, allowing step-by-step construction via a fluent API while producing an **immutable target object**.

### Key Advantages:
1. **Readable Fluent API:** `new HttpRequest.Builder("https://api.com").method("POST").header("Auth", "Token").build()`
2. **Immutability:** Target class fields are `private final` with no setter methods.
3. **Validation Invariants:** Invariants can be validated in the `build()` method before creating the target object.
