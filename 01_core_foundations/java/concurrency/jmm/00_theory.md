# Java Memory Model (JMM) — Complete Theory Guide
> **Study goal:** Understand WHY concurrency bugs happen at the hardware/JVM level,
> not just HOW to fix them. This is what separates senior engineers from juniors.

---

## 1. The Problem: Modern Hardware is NOT Sequential

Your Java code looks sequential. Your CPU is not.

```java
// You write this:
a = 1;
b = 2;

// The CPU (and JVM) may execute it as:
b = 2;
a = 1;   // reordered — if no dependency, it's equivalent for a SINGLE thread
```

This is **instruction reordering** — done by:
- The **compiler** (javac, JIT): for optimization
- The **CPU**: out-of-order execution
- The **memory subsystem**: store buffers, CPU caches

Reordering is **invisible to a single thread** (the thread always sees a consistent view of its own operations).  
It is **catastrophically visible to other threads** without synchronization.

---

## 2. The JMM Is a Specification, Not a Description

The JMM (defined in JSR-133, Java 5+) does NOT describe what hardware does.  
It defines a **contract**: if you follow certain rules (use `synchronized`, `volatile`, etc.),  
the JVM **guarantees** certain behaviors regardless of hardware.

Think of it like: "I don't care how you implement it — just promise me X."

The JMM answers: **"Under what conditions is a write by Thread A visible to Thread B?"**

---

## 3. The Two Core Problems

### Problem 1: Visibility
A write by Thread A may not be immediately visible to Thread B.

```
Thread A: value = 42;   (written to CPU cache / store buffer)
Thread B: print(value); (may still read 0 from its own cache)
```

### Problem 2: Reordering
Instructions may execute in a different order than written.

```java
// Thread A (initializes an object):
obj = new Singleton();   // LOOKS like one instruction
// Actually:
// 1. Allocate memory for Singleton
// 2. Write reference to 'obj'
// 3. Initialize fields
// Steps 2 and 3 can be REORDERED! Other threads may see a half-initialized object.
```

---

## 4. Happens-Before (HB) — The Fundamental Guarantee

The JMM uses **happens-before** to define visibility.

**Definition:** Action A happens-before action B if:
- A's effects (writes) are **guaranteed to be visible** to B.
- A's instructions cannot be reordered to appear **after** B.

HB is **transitive**: if A HB→ B and B HB→ C, then A HB→ C.

### The Happens-Before Rules

```
1. PROGRAM ORDER RULE
   Within a single thread, each action happens-before the next.
   a = 1; b = 2; → a=1 HB b=2 (within the same thread)

2. MONITOR LOCK RULE (synchronized)
   Unlocking a monitor happens-before every subsequent lock on the SAME monitor.
   Thread A: synchronized { x = 5; }  // unlock
   Thread B: synchronized { print(x); } // lock → sees x=5 ✓

3. VOLATILE VARIABLE RULE
   A write to a volatile field happens-before every subsequent read of that field.
   Thread A: volatile flag = true;
   Thread B: if (flag) print(data);  // sees flag=true AND all writes before flag=true ✓

4. THREAD START RULE
   Thread.start() happens-before every action in the started thread.
   main: x = 42; t.start(); → t will see x=42

5. THREAD JOIN RULE
   All actions in a thread happen-before Thread.join() returns in another thread.
   t.join(); → guaranteed to see all of t's writes ✓

6. THREAD INTERRUPTION RULE
   Thread.interrupt() happens-before the detection of the interrupt.

7. FINALIZER RULE
   End of constructor happens-before the start of finalize().

8. TRANSITIVITY
   If A HB→ B and B HB→ C, then A HB→ C.
```

---

## 5. What volatile Actually Guarantees

`volatile` provides **two** guarantees:

### Guarantee 1: Visibility
Every write to a `volatile` variable is immediately flushed to main memory.  
Every read fetches from main memory (skips CPU cache).

### Guarantee 2: Happens-Before
A write to `volatile x` happens-before any subsequent read of `volatile x`.  
**AND all writes before the volatile write are also visible.**

```java
// Thread A:
data = 42;           // normal write
flag = true;         // volatile write  ← happens-before

// Thread B:
if (flag) {          // volatile read   ← happens-after
    use(data);       // sees 42 ✓ (because of the HB chain)
}
```

This "piggybacking" is why a single `volatile` flag can synchronize a whole block of data.

### What volatile Does NOT Guarantee: Atomicity

```java
volatile int counter = 0;
counter++;   // This is: READ counter → ADD 1 → WRITE counter (3 operations)
// Two threads doing counter++ concurrently → race condition!
// Use AtomicInteger instead.
```

---

## 6. Safe Publication

**Safe publication** means making an object reference visible to other threads  
such that those threads see a fully initialized object (not a partially constructed one).

### The Problem: Unsafe Publication

```java
// Thread A:
Singleton instance = new Singleton();
// Internally: allocate memory → WRITE reference → initialize fields
//             Steps 2 and 3 can be REORDERED by CPU/JIT!
//             Thread B may see a non-null reference to an UNINITIALIZED object!

// Thread B (double-checked locking WITHOUT volatile — BROKEN!):
if (instance != null) {
    instance.doSomething();  // may call a method on uninitialized object!
}
```

### Four Safe Publication Idioms

```java
// 1. final fields — safely published via constructor
class Immutable {
    final int value;
    Immutable(int v) { this.value = v; }
    // Safe: final fields are guaranteed visible to all threads after construction
}

// 2. volatile reference
volatile Singleton instance;

// 3. Properly synchronized access
synchronized Singleton getInstance() { return instance; }

// 4. Static initializer (Initialization-on-Demand Holder)
class Singleton {
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }
    static Singleton get() { return Holder.INSTANCE; }
    // JVM guarantees class initialization is thread-safe
}
```

---

## 7. The Double-Checked Locking Problem

The classic broken pattern:

```java
// ✗ BROKEN (pre-Java 5 / without volatile)
class Singleton {
    private static Singleton instance;

    static Singleton get() {
        if (instance == null) {                  // check 1 — no lock
            synchronized (Singleton.class) {
                if (instance == null) {          // check 2 — with lock
                    instance = new Singleton();  // ← can be reordered!
                }
            }
        }
        return instance;
    }
}
// Thread B may pass check 1, see a non-null but uninitialized instance.
```

```java
// ✓ FIXED with volatile
class Singleton {
    private static volatile Singleton instance;  // ← volatile prevents reordering

    static Singleton get() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();  // volatile write → full fence
                }
            }
        }
        return instance;
    }
}
```

---

## 8. Memory Barriers (Fences)

The JVM implements happens-before using CPU **memory barriers** (fences).

| Barrier | Prevents |
|---|---|
| `LoadLoad` | Load before cannot be reordered after load after |
| `StoreStore` | Store before cannot be reordered after store after |
| `LoadStore` | Load before cannot be reordered after store after |
| `StoreLoad` | Store before cannot be reordered after load after (the most expensive) |

`volatile` write inserts a **StoreStore** + **StoreLoad** barrier.  
`volatile` read inserts a **LoadLoad** + **LoadStore** barrier.  
`synchronized` block inserts barriers on entry and exit.

You don't need to manage barriers directly in Java — they're inserted by the JVM  
when you use `synchronized`, `volatile`, `java.util.concurrent` utilities.

---

## 9. Final Fields and Immutability

**The JMM's final field guarantee:**

> After an object is "safely published" (the reference is visible to other threads),
> all `final` fields are guaranteed to be visible with their fully initialized values.

```java
class Point {
    final int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
    // Any thread that receives a reference to a properly published Point
    // will always see the correct x and y values.
}
```

**WARNING:** The guarantee applies only if the reference doesn't leak during construction:

```java
class Broken {
    final int x;
    static Broken instance;

    Broken() {
        instance = this;  // ← LEAKING 'this' before construction completes!
        x = 42;           // other threads may see x=0 via instance
    }
}
```

---

## 10. Common JMM Bugs and Their Fixes

| Bug | Symptom | Fix |
|---|---|---|
| **Stale read** | Thread reads old value of shared variable | `volatile` or `synchronized` |
| **Visibility after write** | Writer writes, reader never sees it | Add HB relationship |
| **Half-initialized object** | Object fields not fully set when reference visible | `volatile` on reference, or safe publication idiom |
| **Broken DCL Singleton** | `null` → partially constructed object | `volatile` on instance field |
| **Reordered initialization** | Constructor logic runs in unexpected order | Don't leak `this` in constructor |
| **Infinite loop** | Compiler caches non-volatile flag in register | `volatile` on the flag |
| **AtomicInteger vs volatile int** | `count++` is not atomic even with `volatile` | Use `AtomicInteger` |

---

## 11. The JMM in 3 Sentences (Interview Answer)

> "The Java Memory Model defines the rules under which writes by one thread
> become visible to other threads. It uses the 'happens-before' relationship
> to guarantee ordering: synchronized blocks, volatile variables, thread start/join,
> and concurrent utilities all establish happens-before edges. Without a
> happens-before relationship between a write and a read, the JMM makes no
> guarantee that the read will ever see the write — even if the write happened first
> in wall-clock time."

---

*Next:*
- `01_visibility.java` — Visibility bugs and volatile fixes with live demonstrations
- `02_reordering.java` — Instruction reordering, DCL singleton, final fields
- `03_happens_before.java` — HB chain construction, safe publication, memory barriers
