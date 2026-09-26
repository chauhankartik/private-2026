# Chapter 6: Task Execution

Decoupling task submission from execution policy using the Executor Framework.

---

## 📌 Thread vs Task Decoupling

Executing tasks explicitly in threads (`new Thread(runnable).start()`) suffers from 3 major flaws:
1. Thread creation overhead (stack memory, OS thread allocation).
2. Resource exhaustion (unbounded thread creation crashes the OS with `OutOfMemoryError`).
3. Lack of management, monitoring, and cancellation.

### The `Executor` Interface
```java
public interface Executor {
    void execute(Runnable command);
}
```

---

## 📌 Executor Framework & Thread Pool Types

```mermaid
classDiagram
    class Executor {
        <<interface>>
        +execute(Runnable)
    }
    class ExecutorService {
        <<interface>>
        +submit(Callable) Future
        +shutdown()
        +shutdownNow()
    }
    class ThreadPoolExecutor {
        -corePoolSize
        -maximumPoolSize
        -workQueue
    }
    Executor <|-- ExecutorService
    ExecutorService <|-- ThreadPoolExecutor
```

### Standard Factory Methods (`Executors`)
- `Executors.newFixedThreadPool(int nThreads)`: Bound thread pool with unbounded `LinkedBlockingQueue`.
- `Executors.newCachedThreadPool()`: Unbound thread pool using `SynchronousQueue`; reuses idle threads or creates new ones.
- `Executors.newSingleThreadExecutor()`: Single worker thread executing tasks sequentially.
- `Executors.newScheduledThreadPool(int corePoolSize)`: Fixed thread pool for delayed or periodic execution.

---

## 📌 Callable and Future

While `Runnable` produces no return value and cannot throw checked exceptions, `Callable<V>` returns a value of type `V` and can throw checked exceptions.

```mermaid
sequenceDiagram
    participant C as Caller Thread
    participant ES as ExecutorService
    participant F as Future<V>
    participant W as Worker Thread

    C->>ES: submit(Callable)
    ES-->>C: Returns Future<V>
    ES->>W: Assign Task to Worker
    W->>W: Executing Computation...
    C->>F: future.get() -> Blocks if incomplete
    W->>F: Sets Result V
    F-->>C: Returns Result V!
```

```java
Callable<String> task = () -> {
    // Perform heavy processing...
    return "Result";
};

Future<String> future = executor.submit(task);
try {
    String result = future.get(5, TimeUnit.SECONDS); // Block with timeout!
} catch (TimeoutException e) {
    future.cancel(true); // Cancel if timed out!
}
```
