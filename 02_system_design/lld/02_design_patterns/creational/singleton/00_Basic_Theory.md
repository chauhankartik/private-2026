# The Singleton Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Ensure a class has **only one instance** in the entire application lifecycle and provide a **global point of access** to that instance.

### Common Real-World Use Cases:
- Database Connection Pools (`DataSource`).
- Thread Pools / Executor Services.
- Configuration Managers.
- In-Memory Caches / Logging Frameworks (`Logger`).

---

## 🔒 Four Implementation Variants & Multi-Threading

### 1. Lazy Initialization with Double-Checked Locking (Recommended for standard classes)
- Uses `volatile` keyword to prevent instruction reordering (ensures object is fully constructed before pointer is visible to other threads).
- Synchronizes ONLY on the first initialization call to eliminate lock acquisition overhead on subsequent reads.

```java
public class DoubleCheckedSingleton {
    private static volatile DoubleCheckedSingleton instance;

    private DoubleCheckedSingleton() {}

    public static DoubleCheckedSingleton getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckedSingleton.class) {
                if (instance == null) {
                    instance = new DoubleCheckedSingleton();
                }
            }
        }
        return instance;
    }
}
```

### 2. Bill Pugh Holder Class (Best Lazy Thread-Safe Pattern without Locks)
- Relies on Java's ClassLoader guarantees. The inner static class `SingletonHolder` is NOT loaded into RAM until `getInstance()` is explicitly invoked!

```java
public class BillPughSingleton {
    private BillPughSingleton() {}

    private static class SingletonHolder {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }

    public static BillPughSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

### 3. Enum Singleton (Effective Java Recommendation by Joshua Bloch)
- **Bulletproof against Reflection & Serialization Attacks!**
- Java enums natively guarantee single instance creation and handle serialization safely.

```java
public enum EnumSingleton {
    INSTANCE;
    public void doSomething() { ... }
}
```

---

## 🛡️ Breaking Singleton & Protection Strategies

1. **Reflection Attack:** Reflection can invoke private constructor (`setAccessible(true)`).
   - *Fix:* Throw exception in constructor if instance already exists or use `EnumSingleton`.
2. **Serialization Attack:** Deserializing a serialized singleton creates a new instance.
   - *Fix:* Implement `readResolve()` method returning existing instance or use `EnumSingleton`.
