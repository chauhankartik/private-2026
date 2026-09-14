# Chapter 12: Testing Concurrent Programs

Testing for correctness, race conditions, safety invariants, and throughput performance.

---

## 📌 Safety & Correctness Testing Harness

Testing race conditions requires maximizing thread interleaving and ensuring all worker threads release simultaneously (**Start Gate Pattern**).

```mermaid
sequenceDiagram
    participant TestMain as Test Main Thread
    participant StartGate as CountDownLatch (1)
    participant Worker1 as Worker Thread 1
    participant Worker2 as Worker Thread 2
    participant EndGate as CountDownLatch (N)

    TestMain->>Worker1: Spawn & Start
    TestMain->>Worker2: Spawn & Start
    Worker1->>StartGate: await() -> Blocked
    Worker2->>StartGate: await() -> Blocked
    TestMain->>StartGate: countDown() -> RELEASE ALL THREADS SIMULTANEOUSLY!
    Worker1->>Worker1: Execute Concurrent Mutex Operation
    Worker2->>Worker2: Execute Concurrent Mutex Operation
    Worker1->>EndGate: countDown()
    Worker2->>EndGate: countDown()
    EndGate-->>TestMain: await() unblocks -> Assert Safety Invariants!
```

---

## 📌 Microbenchmarking with JMH (Java Microbenchmark Harness)

Never write manual `System.currentTimeMillis()` benchmark loops for Java concurrent code! The JVM JIT compiler performs dead code elimination, loop unrolling, and constant folding that corrupts custom benchmark results.

Always use **JMH**:

```java
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ConcurrentMapBenchmark {
    private ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

    @Benchmark
    public String testGet() {
        return map.get("key"); // Prevents dead code elimination by returning result!
    }
}
```
