# Chapter 11: Performance and Scalability

Amdahl's Law, lock contention reduction, lock splitting, and lock striping.

---

## 📌 Amdahl's Law & Theoretical Speedup

Amdahl's Law describes the maximum speedup achievable by parallelizing a program:

$$\text{Speedup} \le \frac{1}{(1 - P) + \frac{P}{N}}$$

Where $P$ is the parallelizable fraction of the work, and $N$ is the number of processing cores.

If 10% of a program is strictly sequential ($1 - P = 0.10$), maximum speedup on **infinite cores** is at most $10\times$!

---

## 📌 Lock Contention Reduction Techniques

### 1. Narrowing Lock Scope
Hold locks for the shortest time possible. Move non-thread-critical tasks (string formatting, I/O) out of synchronized blocks.

### 2. Lock Splitting
Replace a single global lock guarding multiple independent state variables with separate locks for each variable.

### 3. Lock Striping
Divide a collection into multiple independent lock stripes.

```mermaid
flowchart TD
    subgraph Lock Striping ConcurrentHashMap 16 Locks
        L0["Lock Stripe 0 (Buckets 0, 16, 32...)"]
        L1["Lock Stripe 1 (Buckets 1, 17, 33...)"]
        L15["Lock Stripe 15 (Buckets 15, 31...)"]
    end
    
    T1["Thread 1 Accessing Key Hashing to Stripe 0"] --> L0
    T2["Thread 2 Accessing Key Hashing to Stripe 1"] --> L1
    Note over T1,T2: Executes concurrently without blocking each other!
```
