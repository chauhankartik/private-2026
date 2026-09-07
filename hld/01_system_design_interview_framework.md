# Chapter 1: The 4-Step System Design Interview Framework & Back-of-the-Envelope Estimation

## 1. The 4-Step System Design Interview Framework

A system design interview is an open-ended, collaborative architectural discussion. Following a structured framework prevents diving into implementation details prematurely.

```
 +-----------------------------------------------------------------------------------+
 | 4-Step System Design Interview Framework                                          |
 +--------+------------------------------------+-------------------------------------+
 | Step   | Phase                              | Recommended Allocation              |
 +--------+------------------------------------+-------------------------------------+
 | Step 1 | Requirements Clarification & Scope | 3 to 5 Minutes                      |
 | Step 2 | Back-of-the-Envelope Estimation    | 5 to 7 Minutes                      |
 | Step 3 | High-Level Architecture Blueprint  | 10 to 15 Minutes                    |
 | Step 4 | Deep Dive & Bottleneck Resolution  | 15 to 20 Minutes                    |
 +--------+------------------------------------+-------------------------------------+
```

---

## 2. Step 1: Requirements Clarification & Scope

Never start drawing architecture diagrams without explicit requirements clarification. Divide requirements into **Functional** and **Non-Functional**:

### Functional Requirements (What the system DOES):
* *"Can a user upload a video or only watch videos?"*
* *"Is 1-on-1 chat required, or group chat up to 500 members?"*
* *"Do short URLs expire, or are they permanent?"*

### Non-Functional Requirements (System Quality Attributes):
* **Availability SLA:** e.g., 99.99% Availability ("four nines").
* **Latency SLA:** e.g., $P_{99} \text{ Read Latency} < 100\text{ms}$.
* **Consistency Model:** Strong Read-After-Write vs Eventual Consistency.
* **Scale Target:** e.g., 100 Million Daily Active Users (DAU).

```
 Availability SLA Math Quick Reference:
 +--------------------+-----------------------+
 | Availability SLA   | Allowed Downtime/Year |
 +--------------------+-----------------------+
 | 99% (Two Nines)    | 3.65 Days             |
 | 99.9% (Three Nines)| 8.76 Hours            |
 | 99.99% (Four Nines)| 52.6 Minutes          |
 | 99.999% (Five Nines| 5.26 Minutes          |
 +--------------------+-----------------------+
```

---

## 3. Step 2: Back-of-the-Envelope Capacity Estimation

Back-of-the-envelope calculations validate architectural choices (e.g., deciding whether a single PostgreSQL database suffices or if a distributed Cassandra cluster is required).

### Canonical Estimation Parameters (Base Assumptions):
* $1 \text{ Day} = 86,400 \text{ Seconds} \approx 10^5 \text{ Seconds}$.
* $100 \text{ Million DAU}$.
* $80/20 \text{ Pareto Rule}$: $20\%$ of hot objects generate $80\%$ of read traffic.

### 1. Queries Per Second (QPS) Math
Assume 100M DAU, and each user performs 10 read requests per day:

$$\text{Total Daily Reads} = 100 \times 10^6 \times 10 = 1 \text{ Billion Reads/Day}$$

$$\text{Average Read QPS} = \frac{10^9 \text{ Reads}}{86,400 \text{ Seconds}} \approx 12,000 \text{ QPS}$$

$$\text{Peak Read QPS} = \text{Average QPS} \times 2 = 24,000 \text{ QPS}$$

---

### 2. Storage Capacity Math (5-Year Horizon)
Assume 100M DAU, $10\%$ of users post 1 photo/day ($10 \text{ Million Writes/Day}$). Each photo metadata record is $500 \text{ Bytes}$:

$$\text{Daily Metadata Storage} = 10^7 \text{ Writes} \times 500 \text{ Bytes} = 5 \text{ GB / Day}$$

$$\text{5-Year Storage} = 5 \text{ GB/Day} \times 365 \text{ Days} \times 5 \text{ Years} \approx 9.1 \text{ Terabytes}$$

---

### 3. RAM Cache Capacity Math (80/20 Rule)
To cache $20\%$ of daily read traffic in RAM (e.g., Redis):
If daily read volume is $1 \text{ TB/day}$:

$$\text{RAM Cache Required} = 0.20 \times 1 \text{ TB} = 200 \text{ GB RAM}$$

---

## 4. Step 3 & 4: Blueprint & Deep Dive

```
                             High-Level Blueprint
                                      |
                                      v
  [Client App] ----> [DNS / CDN] ----> [Load Balancer] ----> [API Gateway]
                                                                  |
           +------------------------------------------------------+------------------------------------------------------+
           |                                                      |                                                      |
           v                                                      v                                                      v
  [Stateless Service A]                                 [Stateless Service B]                                 [Stateless Service C]
           |                                                      |                                                      |
           v                                                      v                                                      v
  [Redis Cache Cluster]                                [Database (Master/Slave)]                             [Kafka Message Queue]
```

### Deep-Dive Focus Areas:
1. **Single Point of Failure (SPOF):** Eliminate single instances by introducing redundant load balancers and active-active database replicas.
2. **Database Bottlenecks:** Apply read-replicas, index optimization, and horizontal sharding.
3. **Cache Invalidation:** Implement Cache-Aside or Write-Through policies with explicit TTLs.
