# Chapter 4: Replication, High Availability & ISR Mechanics — Deep Dive Notes

> **Core Theme:** Kafka partition replication, **In-Sync Replicas (ISR)**, **Log End Offset (LEO)**, and **High Watermark (HW)** math.

---

## 1. Partition Replication Topology

Every topic partition has one **Leader Replica** and zero or more **Follower Replicas** (`replication.factor=3`).

```
Producer ─── Writes ──► Leader Replica (Node 1)
                            │
               ┌────────────┴────────────┐
               ▼ (Fetch Stream)          ▼
         Follower (Node 2)         Follower (Node 3)
```

- All client writes and reads hit the **Leader Replica**.
- Followers issue `FetchRequests` to pull log entries from the Leader to stay synchronized.

---

## 2. ISR (In-Sync Replicas), LEO, and HW Mathematics

### Definitions:
- **LEO (Log End Offset):** The offset of the next message to be written to a specific replica's log.
- **HW (High Watermark):** The highest offset that has been successfully replicated across **ALL** members of the ISR set.

```
Leader Replica  (Node 1): [0] [1] [2] [3] [4] [5]  (LEO = 6)
Follower 1 (ISR Node 2): [0] [1] [2] [3] [4]       (LEO = 5)
Follower 2 (ISR Node 3): [0] [1] [2] [3]           (LEO = 4)

HIGH WATERMARK (HW) = min(LEO1, LEO2, LEO3) = 4
```

### The Consumer Visibility Rule:
- Consumers are ONLY permitted to read messages **up to the High Watermark (HW)** (Offsets 0 .. 3).
- Messages beyond HW (Offsets 4 and 5) are hidden from consumers until fully replicated across the ISR set!

---

## 3. ISR Shrinking & Expansion

- A follower is part of the **ISR Set** as long as it sends fetch requests within `replica.lag.time.max.ms` (Default: 30s).
- If a follower suffers a GC pause or network delay exceeding 30s, the Leader removes it from the ISR set and logs an `IsrShrink` event.
- Once the follower catches up to the Leader's LEO, it is re-added to the ISR set (`IsrExpand`).

---

## 4. Availability vs. Durability Trade-offs

### 1. Durability Configuration (No Data Loss):
```properties
acks = all
replication.factor = 3
min.insync.replicas = 2
unclean.leader.election.enable = false
```
- Requires at least 2 healthy ISR replicas to accept writes. If 2 nodes crash, writes fail safely with `NotEnoughReplicasException`.

### 2. Unclean Leader Election (`unclean.leader.election.enable = true`):
- If all ISR nodes crash, allows a **stale non-ISR follower** to be promoted to Leader.
- *Trade-off:* System stays Available, but ALL un-replicated messages on the old leader are **permanently lost**!
