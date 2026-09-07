# Chapter 5: High Availability (Replication & Redis Sentinel) — Deep Dive Notes

> **Core Theme:** Scaling read traffic via **Replication** and achieving automatic failover via **Redis Sentinel**.

---

## 1. Redis Replication Mechanics

Redis uses single-leader asynchronous replication. All writes hit the Master node; Master streams write commands to Replicas.

```
Client ─── Writes ──► Master (Replication ID, Offset: 10500)
                        │
             ┌──────────┴──────────┐
             ▼ (Async Stream)      ▼
         Replica 1             Replica 2 (Offset: 10490)
```

### 1. Full Synchronization (`FULLRESYNC`):
Occurs when a new replica joins or when replica offset is lost.
1. Master runs `BGSAVE` to create RDB snapshot.
2. Master buffers new incoming writes in **Replication Buffer**.
3. Master sends RDB file + replication buffer to replica.

### 2. Partial Synchronization (`PSYNC`):
If a replica temporarily disconnects (e.g. 5-second network glitch):
1. Replica reconnects sending `PSYNC <master_replid> <replica_offset>`.
2. If replica offset is still present inside Master's in-memory circular **`repl-backlog-buffer`**, Master streams only missing bytes without triggering RDB disk snapshot!

---

## 2. Redis Sentinel Architecture

Redis Sentinel is a distributed monitoring system providing high availability without manual administrator intervention.

```
                  REDIS SENTINEL CLUSTER
                  
    [ Sentinel 1 ] ─── (Gossip) ─── [ Sentinel 2 ]
          │                              │
          └──────────────┬───────────────┘
                         ▼
             [ Master Node (Port 6379) ]
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
        [ Replica 1 ]           [ Replica 2 ]
```

---

## 3. Sentinel Failover Protocol

### Step 1: Failure Detection (`SDOWN` vs. `ODOWN`)
- **Subjective Down (SDOWN):** A single Sentinel node fails to receive `PING` responses from Master for `down-after-milliseconds` (e.g. 30s).
- **Objective Down (ODOWN):** Sentinel queries peer Sentinels via Gossip. If **Quorum** ($Q$, e.g. 2 out of 3) Sentinels confirm `SDOWN`, Master is marked `ODOWN`.

### Step 2: Leader Sentinel Election
Sentinels run an election (Raft consensus) to elect a single **Leader Sentinel** responsible for executing failover.

### Step 3: Replica Selection & Promotion
Leader Sentinel selects the best Replica to promote using criteria:
1. Excludes disconnected / unhealthy replicas.
2. Picks replica with lowest `replica-priority`.
3. Picks replica with highest **Replication Offset** (most up-to-date data).
4. Tiebreaker: Lowest `runid`.

### Step 4: Cluster Reconfiguration
1. Promotes selected Replica to Master (`SLAVEOF NO ONE`).
2. Reconfigures remaining Replicas to follow new Master (`SLAVEOF <new_master_ip> <port>`).
3. Updates Old Master configuration so if it recovers, it rejoins as a Replica.
