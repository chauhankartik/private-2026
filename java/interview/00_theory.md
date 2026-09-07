# Java Interview Questions & Edge Cases Exhaustive Theory Guide

---

## 1. Syntax & Primitive Types Edge Cases

### Q1: What is the Integer Cache, what is its range, and why does `Integer a = 100, b = 100; a == b` evaluate to `true` while `Integer x = 200, y = 200; x == y` evaluates to `false`?
- **Answer:** Auto-boxing integers via `Integer.valueOf(int)` utilizes an internal private static cache (`Integer.IntegerCache`).
- By default, the JVM caches `Integer` objects for values between **`-128` and `127`** (inclusive).
- For `100`, `a` and `b` receive references to the *same* pre-allocated heap object in `IntegerCache`, so reference equality `a == b` evaluates to `true`.
- For `200` (outside `-128..127`), `Integer.valueOf(200)` allocates a new `Integer` object instance on the heap for each invocation. Thus, `x` and `y` hold references to two distinct heap memory addresses, making `x == y` `false`.
- *JVM Flag:* The upper limit can be adjusted using `-XX:AutoBoxCacheMax=<size>`.

---

### Q2: Why does `0.1 + 0.2 != 0.3` in Java, and how should financial values be represented?
- **Answer:** Java represents `double` and `float` using **IEEE 754 floating-point standard** (binary fractions).
- Decimal values like `0.1` (`1/10`) cannot be represented exactly as finite binary fractions. `0.1 + 0.2` evaluates to `0.30000000000000004` due to rounding error.
- **Solution:** For monetary/financial calculations, always use `BigDecimal` initialized via **String constructor** (`new BigDecimal("0.1")`), or operate using integer cents/smallest units (`long`).

---

### Q3: What is the Ternary Operator Type Promotion rule, and why does `Object obj = true ? 1 : 2.0;` set `obj` to a `Double` instance `1.0`?
- **Answer:** The JLS (Section 15.25) specifies that if the second and third operands of a ternary operator have different numeric primitive types, the expression is evaluated to a **common promoted numeric type** before assignment.
- For `int` (`1`) and `double` (`2.0`), the common type is `double`. Thus, `1` is widened to `double` `1.0`, and auto-boxed to `Double`.

---

### Q4: How does `String.intern()` work, and how does string literal pooling differ from `new String("abc")`?
- **Answer:**
  - `String s1 = "abc";` checks the JVM **String Pool** (stored in Heap memory). If present, it returns the pooled reference. If absent, it creates it in the pool.
  - `String s2 = new String("abc");` *always* allocates a new `String` object instance on the JVM Heap, even if `"abc"` exists in the pool. Thus `s1 == s2` is `false`.
  - `s2.intern()` searches the String Pool for `"abc"`. It returns the canonical reference from the pool. Therefore, `s1 == s2.intern()` is `true`.

---

## 2. OOP, Inheritance & Polymorphism Traps

### Q5: Why is calling an overridable method inside a parent constructor a severe anti-pattern in Java?
- **Answer:** In Java constructor chaining, when instantiating a child class (`new Child()`), the superclass constructor `Parent()` executes **before** `Child` instance fields are initialized!
- If `Parent()` invokes an overridable method `print()`, dynamic dispatch routes execution to `Child.print()`.
- However, `Child`'s instance fields have not been initialized yet (they contain default values: `0`, `false`, or `null`), leading to unexpected `NullPointerException` or corrupt state.

```java
class Parent {
    Parent() { print(); } // Polymorphic call during construction!
    void print() { System.out.println("Parent"); }
}
class Child extends Parent {
    String name = "Initialized";
    @Override void print() { System.out.println("Child name: " + name.length()); } // Throws NPE!
}
```

---

### Q6: How does Java's Overload Resolution Order work when choosing between exact match, widening, boxing, and varargs?
- **Answer:** The compiler resolves overloaded method calls in 3 distinct phases (JLS 15.12.2):
  1. **Phase 1: Subtyping / Widening without Boxing** (e.g. `int` \(\to\) `long` \(\to\) `double`).
  2. **Phase 2: Auto-Boxing / Unboxing** (e.g. `int` \(\to\) `Integer`).
  3. **Phase 3: Varargs** (e.g. `int...`).

*Precedence Hierarchy:* **Exact Match > Primitive Widening > Auto-Boxing > Varargs**.

---

### Q7: What are the Exception Overriding rules for subclass methods?
- **Answer:** When overriding a method:
  1. A subclass method **cannot** declare new or broader **checked exceptions** than those declared by the superclass method.
  2. A subclass method **can** declare narrower/child checked exceptions, fewer checked exceptions, or no checked exceptions.
  3. A subclass method **can** declare any **unchecked exceptions** (`RuntimeException` / `Error`), regardless of the superclass method signature.

---

## 3. Control Flow & Exception Edge Cases

### Q8: What happens when a `finally` block contains a `return` statement or throws an exception?
- **Answer:** A `return` statement inside a `finally` block **overrides and discards** any value returned or exception thrown in the `try` or `catch` block!
- If the `try` block throws `new RuntimeException("Error")`, and the `finally` block executes `return 42;`, the exception is completely swallowed and erased, and the method quietly returns `42`.

---

### Q9: Does a `finally` block ALWAYS execute? What are the edge cases where it does NOT execute?
- **Answer:** `finally` blocks execute in almost all scenarios, **except**:
  1. Invoking `System.exit(code)` or `Runtime.getRuntime().halt(code)`.
  2. JVM Crash (e.g. `SIGKILL`, OS kernel crash, native C-segmentation fault).
  3. Infinite loop / deadlock inside the `try` or `catch` block.
  4. The host thread executing the `try` block is forcibly killed via native OS thread termination.

---

### Q10: How does Java 7 Try-With-Resources handle Suppressed Exceptions (`Throwable.getSuppressed()`)?
- **Answer:** In try-with-resources, if an exception is thrown inside the `try` block AND an exception is thrown while auto-closing a resource:
  - The primary exception thrown inside the `try` block is **preserved** and rethrown.
  - The close exception is attached to the primary exception as a **Suppressed Exception**.
  - Accessible via `primaryException.getSuppressed()`.

---

## 4. Collections & Concurrency Gotchas

### Q11: What happens if a key object stored in a `HashMap` is mutated after insertion?
- **Answer:** `HashMap` computes bucket placement using `key.hashCode()`.
- If a key object is mutated such that its `hashCode()` changes, the key remains in its original bucket.
- Subsequent calls to `map.get(mutatedKey)` or `map.containsKey(mutatedKey)` recalculate the hash based on the new `hashCode()`, searching the wrong bucket and returning `null`.
- *Rule:* Always use **immutable objects** (e.g., `String`, `Integer`, immutable records) as `HashMap` keys.

---

### Q12: Why can `ThreadLocal` cause severe memory leaks in Web Application Servers (e.g., Tomcat, Jetty)?
- **Answer:** Web servers reuse worker threads across multiple HTTP requests via a fixed Thread Pool.
- `ThreadLocal` variables are stored in the thread's internal `ThreadLocalMap`, keyed by weak references to the `ThreadLocal` instance, but values are **strongly referenced**.
- If a request thread finishes without calling `threadLocal.remove()`, the value remains pinned in memory attached to the long-lived thread object.
- Over time, thread reuse causes major OutOfMemoryErrors (`OOM: Metaspace / Heap`).

---

## 5. Memory, GC & ClassLoader Edge Cases

### Q13: What is the difference between `WeakReference`, `SoftReference`, and `PhantomReference`?
- **`SoftReference`:** Cleared by GC **only when heap memory pressure is critical** (right before `OutOfMemoryError`). Ideal for memory-sensitive caches.
- **`WeakReference`:** Cleared by GC at the **very next garbage collection cycle**, regardless of available memory. Ideal for canonicalizing maps (`WeakHashMap`).
- **`PhantomReference`:** `get()` always returns `null`. Enqueued in a `ReferenceQueue` after the referent has been finalized. Used for tracking off-heap native memory cleanup without `finalize()`.
