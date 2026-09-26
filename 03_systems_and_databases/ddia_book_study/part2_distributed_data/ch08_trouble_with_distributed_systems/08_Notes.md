# Chapter 8: The Trouble with Distributed Systems — Deep Dive Notes

> **Core Theme:** Distributed systems are fundamentally different from single-node software because of **Partial Failures**, **Unreliable Networks**, **Unreliable Clocks**, and **Process Pauses**. How to reason about truth, consensus, and system safety models.

---

## 1. Faults and Partial Failures

In a single computer, if an internal hardware fault occurs, the machine undergoes a total failure (blue screen / kernel panic). In a distributed system:
- **Partial Failure:** Some parts of the system break unpredictably while other parts continue working normally.
- **Nondeterminism:** Network delays and message delivery order are inherently non-deterministic.

---

## 2. Unreliable Networks

Distributed systems communicate via asynchronous packet-switched networks (Ethernet, IP).

### What Can Go Wrong Over the Network:
1. Request is lost in transit.
2. Request is queued in a router and delayed.
3. Target node crashed.
4. Target node temporarily paused (GC pause) and will respond later.
5. Response is lost in transit.
6. Response is delayed in transit.

```
Sender ──── Request (Lost or Delayed) ────X  Receiver
Sender ◄─── Response (Lost or Delayed) ──X  Receiver
```

- **Unbounded Delays:** Standard IP networks do not guarantee maximum packet arrival bounds.
- **Detecting Network Failures:** System must rely on **Timeouts**.
  - Short timeout $\to$ Fast failure detection, but high risk of false positives (triggering unnecessary failovers under transient network spikes).
  - Long timeout $\to$ System takes longer to recover from real node crashes.
  - **Phi Accrual Failure Detector:** Dynamically measures response latency distribution to adjust timeout threshold dynamically (Used in Cassandra/Akka).

---

## 3. Unreliable Clocks

Clocks are essential for measuring duration, timeouts, and ordering events. However, hardware quartz clocks drift, and NTP network adjustments introduce time jumps!

### Two Types of Hardware Clocks:
1. **Time-of-Day Clocks (System Clock / Wall Clock):**
   - Returns current date/time relative to UTC (e.g. `System.currentTimeMillis()`).
   - Synchronized via NTP (Network Time Protocol).
   - **Danger:** NTP can force clock to jump backwards or forwards! Never use for measuring elapsed time duration.
2. **Monotonic Clocks:**
   - Measures time moving strictly forward (e.g. `System.nanoTime()`, `clock_gettime(CLOCK_MONOTONIC)`).
   - Absolute value is meaningless; used exclusively for measuring time intervals (`end_time - start_time`).

### The Danger of Wall-Clock Timestamps (LWW Data Loss):
If Node A and Node B accept concurrent writes, relying on physical wall-clock timestamps for **Last-Write-Wins (LWW)** conflict resolution causes catastrophic silent data loss when NTP drift occurs!

```
Node A (Clock: 10:00:05) Writes: value = "X"
Node B (Clock: 10:00:03 - NTP skew!) Writes: value = "Y" (Occurred AFTER Node A!)

LWW Compares: 10:00:05 > 10:00:03 -> Node A's value ("X") WIN, overwriting newer write "Y"!
```

### Google Spanner TrueTime API:
Google Spanner solves clock uncertainty using GPS receivers and Atomic Clocks in every datacenter.
- TrueTime API returns a confidence interval: $[t_{earliest}, t_{latest}]$ where uncertainty is bounded by $\epsilon$ (typically $\le 7\text{ms}$).
- Spanner waits for $2\epsilon$ duration before committing to guarantee absolute causal order across global transactions!

---

## 4. Knowledge, Truth, and Consensus

In a distributed system, no single node has a global view of reality.

### The Truth is Defined by the Majority (Quorum)
A single node cannot determine if it is alive or dead. If a node suffers a 30-second Stop-The-World Garbage Collection (GC) pause:
- The cluster assumes Node A is dead and elects Node B as new leader.
- When Node A resumes from GC pause, it may still believe it is the valid leader (**STW GC Pause Split-Brain**).

### Fencing Tokens (Preventing Split-Brain Data Corruption):
When a lock or leadership lease is granted by a coordinator (e.g. ZooKeeper), it issues a monotonically increasing **Fencing Token** ($1, 2, 3 \dots$).

```
1. Coordinator grants Lease to Leader A (Fencing Token = 33).
2. Leader A pauses (GC pause). Lease expires.
3. Coordinator grants Lease to Leader B (Fencing Token = 34).
4. Leader B writes to Storage Server with Token 34 (ACCEPTED).
5. Leader A wakes up and sends write to Storage Server with Token 33.
6. Storage Server REJECTS write because Token 33 < Highest Seen Token (34)!
```

---

## 5. System Models

Formal frameworks used to reason about distributed algorithm correctness:

### Timing Models:
1. **Synchronous Model:** Network delay, process execution time, and clock drift are all strictly bounded. (Unrealistic in cloud networks).
2. **Partially Synchronous Model:** System behaves synchronously most of the time, but occasionally experiences unbounded network delays and clock drift. (Standard realistic model).
3. **Asynchronous Model:** Zero timing assumptions. No clocks, no timeouts.

### Node Failure Models:
1. **Crash-Stop Faults:** Node fails by stopping completely and never recovers.
2. **Crash-Recovery Faults:** Node can crash at any time and resume execution later (volatile RAM lost; persistent disk intact).
3. **Byzantine (Arbitrary) Faults:** Nodes may lie, send malicious/corrupted messages, or collude to break the system (Requires Byzantine Fault Tolerance / BFT algorithms like PBFT, Bitcoin proof-of-work).
