# System Design 1: Distributed Unique ID Generator (Twitter Snowflake)

## 1. Problem Statement & Requirements

Design a distributed, highly available unique ID generator capable of producing 64-bit integer IDs across multiple data centers.

### Functional Requirements:
* IDs must be **unique** across the global system.
* IDs must be **64-bit numerical integers** (fits in standard 64-bit `long`).
* IDs must be **roughly sortable by time** (IDs generated at $T_2 > T_1$ must be numerically larger).

### Non-Functional Requirements:
* High Throughput: Support $> 100,000 \text{ IDs/sec}$ globally.
* Low Latency: Sub-millisecond generation time ($< 1\text{ms}$).
* High Availability: 99.999% uptime, zero single point of failure (SPOF).

---

## 2. 64-Bit Bit-Layout Architecture (Snowflake Pattern)

```
 1 Bit     41 Bits                          10 Bits             12 Bits
+-------+--------------------------------+--------------------+---------------------+
| Sign  | Timestamp (Milliseconds since  | Worker Node ID     | Sequence Counter    |
| (0)   | custom epoch: 41 Bits)         | (Datacenter + Node)| (Per Millisecond)   |
+-------+--------------------------------+--------------------+---------------------+
  1 Bit               41 Bits                  10 Bits               12 Bits
```

### Bit Specification Breakdown:
1. **Sign Bit (1 Bit):** Always `0` to keep IDs positive signed 64-bit integers.
2. **Timestamp Bits (41 Bits):** Milliseconds since a custom epoch (e.g., `2026-01-01 00:00:00 UTC`).
   $$\text{Lifespan} = \frac{2^{41} - 1}{1000 \times 60 \times 60 \times 24 \times 365} \approx 69.7 \text{ Years}$$
3. **Worker Node ID Bits (10 Bits):** 5 bits for Data Center ID ($2^5 = 32$) + 5 bits for Worker Node ID ($2^5 = 32$), allowing up to $1,024$ worker instances.
4. **Sequence Counter Bits (12 Bits):** Increments for every ID generated on the local node within the same millisecond. Resets to `0` every new millisecond.
   $$\text{Max Throughput per Node} = 2^{12} = 4,096 \text{ IDs / ms / node}$$
   $$\text{Global System Throughput} = 4,096 \times 1,024 \text{ nodes} \approx 4.19 \text{ Million IDs / sec}$$

---

## 3. High-Level Architecture Diagram

```
 Client Request (Generate ID)
        |
        +-----------------------------------+-----------------------------------+
        |                                   |                                   |
        v                                   v                                   v
+-------------------+               +-------------------+               +-------------------+
| ID Generator      |               | ID Generator      |               | ID Generator      |
| Node 1 (DC1)      |               | Node 2 (DC1)      |               | Node 3 (DC2)      |
| (Node ID: 0x01)   |               | (Node ID: 0x02)   |               | (Node ID: 0x21)   |
+---------+---------+               +---------+---------+               +---------+---------+
          |                                   |                                   |
          v                                   v                                   v
   Generates ID                        Generates ID                        Generates ID
```

---

## 4. Deep Dive & Clock Drift Mitigation

### Clock Backward Drift Hazard (NTP Synchronization)
If a worker server's system clock is adjusted backward via Network Time Protocol (NTP), the generator could issue duplicate IDs for timestamps that were already processed.

```
 Server Time: 10:00:05.100  ---> NTP Sync Adjusts Clock Backward ---> Server Time: 10:00:05.090
                                                                                |
                                                                                v
                                                             Risk of Duplicate ID Generation!
```

### Mitigation Strategy:
1. **Clock Drift Detection:** When generating an ID, compare current system time ($T_{\text{now}}$) with the previous recorded timestamp ($T_{\text{last}}$).
2. **If $T_{\text{now}} < T_{\text{last}}$:**
   * **Small Drift ($< 10\text{ms}$):** Pause thread execution (`Thread.sleep(T_{\text{last}} - T_{\text{now}})`), waiting for system time to catch up.
   * **Large Drift ($\ge 10\text{ms}$):** Throw a runtime exception (`ClockMovedBackwardsException`), triggering client failover to an alternative worker node.
