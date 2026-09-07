# Chapter 1: Reliability, Scalability, and Maintainability — Deep Dive Notes

> **Core Theme:** The three foundational requirements of modern software systems. How to quantify load, measure tail latencies ($p99, p999$), handle hardware/software faults, and reduce accidental complexity.

---

## 1. Reliability (Continuing to Work Correctly)

A system is **reliable** if it continues to work correctly (performing the expected function at the desired performance level) even in the face of adversity (**faults**).

### Faults vs. Failures:
- **Fault:** A specific component deviating from its spec (e.g. 1 disk crashes, 1 network link drops).
- **Failure:** The entire system as a whole stops providing the required service to the user.
- **Goal:** Design fault-tolerant systems where component faults do not cascade into system failures.

### Three Types of Faults:
1. **Hardware Faults:** Hard drives crash, RAM corrupts, power outages occur.
   - *Mitigation:* Redundancy (RAID arrays, dual power supplies, multi-datacenter replication).
2. **Software Errors:** Systemic bugs (e.g., runaway process consuming CPU, unhandled edge cases, cascading timeouts).
   - *Mitigation:* Monitoring, process isolation, canary deployments, graceful degradation.
3. **Human Errors:** Misconfigurations, bad deployments, operational mistakes (Cause of >75% of outages).
   - *Mitigation:* Decouple test/sandbox from production, automated rollbacks, telemetry, thorough code reviews.

---

## 2. Scalability (Coping with Growth)

Scalability is a system's ability to cope with increased load without degrading performance.

### Describing Load (Load Parameters):
- **Twitter Timeline Example (Classic Load Parameter Case Study):**
  - **Operation 1 (Post Tweet):** User posts a new message (Average 4,600 rps, Peak 12,000 rps).
  - **Operation 2 (Home Timeline):** User views recent tweets from followed users (300,000 rps).
  
#### Two Architectural Approaches to Twitter Timeline:
- **Approach 1 (Relational Join on Read):** Posting a tweet inserts row into `tweets` table. Reading timeline joins `followers` $\to$ `tweets`. (Heavy CPU on read at 300k rps!).
- **Approach 2 (Fan-Out on Write / Pre-computed Caches):** Maintain a Redis cache for each user's home timeline. When User $X$ tweets, fan out and insert tweet ID into home timeline caches of all followers.
- **The Hybrid Challenge:** For celebrities with 50M+ followers (e.g. Obama, Taylor Swift), fan-out on write causes 50M cache writes for a single tweet!
- **Twitter Hybrid Solution:** Standard users use Approach 2 (Fan-out on write); High-follower users (>10k followers) are excluded from fan-out and merged on read (Approach 1).

---

## 3. Describing Performance & Tail Latencies

In batch systems, we measure **throughput**. In interactive online systems, we measure **response time**.

```
                           RESPONSE TIME DISTRIBUTION
                 Frequency
                     ▲
                     │      Median (p50)
                     │        │      p95    p99    p99.9 (Tail Latency)
                     │        │       │      │       │
                     │      ┌─┴─┐   ┌─┴─┐  ┌─┴─┐   ┌─┴─┐
                     └──────┴───┴───┴───┴──┴───┴───┴───┴─────► Time (ms)
```

### Why Percentiles Matter More Than Averages:
- **Average (Arithmetic Mean):** Highly misleading! Does not show how many users experienced slowness.
- **Median ($p50$):** Half of requests take less time, half take more.
- **Tail Latencies ($p95, p99, p999$):** The 95th, 99th, and 99.9th percentiles measure the worst-case user experiences.

### Why $p999$ (99.9th Percentile) is Critical for Business:
1. **High-value Customers:** Customers with the largest datasets/most activity make the most API requests and experience tail latencies most frequently.
2. **Tail Latency Amplification:** If a single web request requires 100 backend service calls in parallel, the overall request speed is bounded by the *slowest* of those 100 calls ($p99$ tail latency dominates!).

---

## 4. Maintainability (Minimizing Operational Pain)

1. **Operability:** Make it easy for operations teams to keep the system running smoothly (good telemetry, self-healing, clear metrics).
2. **Simplicity:** Remove **Accidental Complexity** (complexity arising purely from implementation details rather than problem domain). Use abstractions!
3. **Evolvability:** Make it easy to modify the system in the future when requirements change (Agility / Extensibility).
