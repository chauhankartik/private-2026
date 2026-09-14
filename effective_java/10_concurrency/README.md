# Chapter 11: Concurrency (Items 78 – 84)

Writing clean, safe, high-throughput concurrent Java applications.

---

## 📌 Item 78: Synchronize access to shared mutable data

Synchronization is required for both **mutual exclusion** (preventing concurrent state updates) and **visibility** (ensuring memory writes by one thread are visible to another thread).

```java
// ❌ Bug: Thread may loop forever due to missing visibility!
private static boolean stopRequested;

// ✅ Fix 1: Use volatile for visibility:
private static volatile boolean stopRequested;

// ✅ Fix 2: Use AtomicBoolean for atomic operations:
private static final AtomicBoolean stopRequested = new AtomicBoolean(false);
```

---

## 📌 Item 79: Avoid excessive synchronization

Never call alien methods (overridable methods or client callbacks) inside a synchronized block. It can cause liveness failures (deadlocks) or silent data corruption.

Keep synchronized blocks as small as possible (**open calls**).

---

## 📌 Item 80: Prefer executors, tasks, and streams to threads

Manual thread management (`new Thread(r).start()`) is inefficient and unsafe. Use `ExecutorService`.

### Modern Java 21+ Virtual Threads
```java
// Java 21+ Virtual Thread Executor for massive concurrency (IO-bound workloads):
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i -> {
        executor.submit(() -> {
            // High concurrency blocking I/O operation
        });
    });
}
```

---

## 📌 Item 81: Prefer concurrency utilities to wait and notify

Higher-level concurrency utilities (`ConcurrentHashMap`, `BlockingQueue`, `CountDownLatch`, `Semaphore`, `CompletableFuture`) supersede low-level `wait` and `notify`.

---

## 📌 Item 82: Document thread safety

Document a class's thread safety level explicitly:
- **Immutable**: Constant instances, no synchronization needed (`String`, `Integer`).
- **Unconditionally thread-safe**: Internal synchronization (`ConcurrentHashMap`, `AtomicInteger`).
- **Conditionally thread-safe**: Requires external synchronization for certain operations (`Collections.synchronizedList`).
- **Not thread-safe**: Must be synchronized externally (`ArrayList`, `HashMap`).

---

## 📌 Item 83: Use lazy initialization judiciously

Don't use lazy initialization unless needed.

### Static Field Lazy Holder Idiom (Thread-Safe, No Locks)
```java
private static class FieldHolder {
    static final FieldType field = computeFieldValue();
}
public static FieldType getField() {
    return FieldHolder.field;
}
```

### Instance Field Double-Checked Locking Idiom
```java
private volatile FieldType field;

public FieldType getField() {
    FieldType result = field;
    if (result == null) { // First check (no lock)
        synchronized(this) {
            result = field;
            if (result == null) { // Second check (with lock)
                field = result = computeFieldValue();
            }
        }
    }
    return result;
}
```

---

## 📌 Item 84: Don't depend on the thread scheduler

Do not rely on `Thread.yield()` or thread priorities for program correctness. Design thread logic so the number of runnable threads does not greatly exceed the number of CPU cores.
