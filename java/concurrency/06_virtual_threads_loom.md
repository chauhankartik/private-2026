# Chapter 6: Java 21+ Virtual Threads (Project Loom) & Structured Concurrency

## 1. Platform Threads vs Virtual Threads

Prior to Java 21, every `java.lang.Thread` was a **Platform Thread** mapped 1:1 to an Operating System (OS) kernel thread.

```
 Platform Threads (Legacy 1:1 Model):
 Java Thread 1  =========> OS Kernel Thread 1 (1MB Stack Memory, Context Switch Overhead)
 Java Thread 2  =========> OS Kernel Thread 2 (1MB Stack Memory, Context Switch Overhead)

 Virtual Threads (Java 21+ M:N Model):
 Virtual Thread 1 ----+
 Virtual Thread 2 ----+--> Carrier Thread (OS Thread 1) ---> CPU Core
 Virtual Thread 3 ----+
 (Millions of Virtual Threads, ~1KB Memory per thread, Managed by JVM in RAM!)
```

### Architectural Comparison:

| Attribute | Platform Thread (OS 1:1) | Virtual Thread (JVM M:N - JEP 444) |
| :--- | :--- | :--- |
| **Creation Overhead** | High (~1MB OS stack memory per thread) | Ultra-Low (~1KB heap memory per thread) |
| **Max Capacity** | Restricted (~2,000–5,000 threads per server) | Millions of active threads ($100\text{K}+$) |
| **Context Switch** | OS Kernel context switch (Expensive) | JVM Heap Continuation un-mounting (Cheap) |
| **Primary Use Case** | Heavy CPU-bound computation | High-concurrency I/O-bound throughput (HTTP, DB queries) |

---

## 2. Continuation Un-Mounting & Carrier Threads

When a Virtual Thread executes an I/O blocking operation (such as `Thread.sleep()`, socket `read()`, or database network fetch):

```
 1. Virtual Thread encounters Blocking I/O
                        |
                        v
 2. JVM Un-mounts Virtual Thread Continuation Stack to JVM Heap Memory
                        |
                        v
 3. Carrier Thread (OS ForkJoin Worker Thread) is instantly FREED to execute another Virtual Thread!
                        |
                        v
 4. When I/O completes, OS notifies JVM, and Virtual Thread is re-mounted onto an available Carrier Thread!
```

---

## 3. Pinning Hazards (`synchronized` vs `ReentrantLock`)

A Virtual Thread is **Pinned** to its Carrier Thread when it cannot be un-mounted during a blocking I/O operation.

### Pinning Triggers:
1. Executing blocking I/O inside a **`synchronized` block or method**.
2. Executing blocking I/O inside a Native Method or Foreign Function Call (JNI).

```java
// PINNING HAZARD: Blocks the underlying OS Carrier Thread!
public synchronized void processPayment() {
    // Blocking socket network call inside synchronized block pins the thread!
    restClient.post("/pay"); 
}

// SOLUTION: Replace synchronized with ReentrantLock!
private final ReentrantLock lock = new ReentrantLock();

public void processPaymentSafely() {
    lock.lock();
    try {
        restClient.post("/pay"); // Virtual thread un-mounts cleanly without pinning!
    } finally {
        lock.unlock();
    }
}
```

### Detecting Pinning at Runtime:
Pass VM flag `-Djdk.tracePinnedThreads=full` to print stack traces whenever a Virtual Thread pins its Carrier Thread.

---

## 4. Structured Concurrency (`StructuredTaskScope`)

**Structured Concurrency** (JEP 453) treats a group of concurrent sub-tasks running in different threads as a single unit of work, simplifying error handling and cancellation.

```java
// Short-Circuit on First Failure using ShutdownOnFailure
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<String> userTask  = scope.fork(() -> fetchUser(userId));
    Subtask<Order> orderTask = scope.fork(() -> fetchOrder(orderId));

    scope.join();           // Join both concurrent sub-tasks
    scope.throwIfFailed();  // Propagate exception if ANY sub-task failed!

    // If both succeeded, process combined result
    return new UserOrderResponse(userTask.get(), orderTask.get());
}
```
