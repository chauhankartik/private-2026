# Chapter 3: Consumer Groups, Rebalancing & Offsets — Deep Dive Notes

> **Core Theme:** Scaling read throughput via **Consumer Groups**, partition assignment protocols (**Cooperative Sticky Rebalance**), and offset tracking in `__consumer_offsets`.

---

## 1. Consumer Group Architecture

A **Consumer Group** represents a set of client consumers sharing consumption of a single topic.

```
Topic 'orders' (4 Partitions)
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ Partition 0 │ Partition 1 │ Partition 2 │ Partition 3 │
└──────┬──────┴──────┬──────┴──────┬──────┴──────┬──────┘
       │             │             │             │
       ▼             ▼             ▼             ▼
 [ Consumer 1 ] [ Consumer 2 ] [ Consumer 3 ] [ Consumer 4 ]
 └────────────────────── Consumer Group A ─────────────────┘
```

### Partition Assignment Rule:
- Each partition is assigned to **EXACTLY ONE** consumer inside a given consumer group.
- If consumers > partitions, excess consumers remain in standby (idle) mode.
- Multiple separate consumer groups read the same topic partitions independently.

---

## 2. Group Coordinator & Rebalance Protocols

When a consumer joins, leaves, or crashes, the cluster triggers a **Partition Rebalance**.

### Group Coordinator Broker:
The broker managing a specific consumer group is selected by:

$$\text{Coordinator Broker} = \text{hash}(\text{group.id}) \pmod{\text{offsets.topic.num.partitions}}$$

---

### Rebalance Protocol Evolution:

```
Eager Rebalance (Legacy):
All Consumers Stop Consumption ──► Revoke All Partitions ──► Re-assign ──► Resume

Cooperative Sticky Rebalance (Modern - KIP-429):
Only Affected Partitions Revoked ──► Unaffected Consumers Keep Processing Inline!
```

1. **Eager Rebalancing (Legacy):** Stops ALL consumers in group, revokes ALL partition assignments, and re-assigns partitions from scratch (Causes Stop-The-World consumption pauses!).
2. **Cooperative Sticky Rebalancing (Modern Default):** Incremental rebalance. Only revokes partitions that are actually changing owners, allowing unaffected consumers to continue processing messages without interruption.

---

## 3. Offset Tracking (`__consumer_offsets`)

Consumers persist their progress by committing offsets to an internal Kafka topic named `__consumer_offsets`.

### Commit Strategies:
1. **Automatic Offset Commit (`enable.auto.commit=true`):** Commits current offset periodically every `auto.commit.interval.ms` (e.g. 5s).
   - *Risk:* If consumer crashes mid-batch, uncommitted processed messages will be re-read (**At-Least-Once** duplication).
2. **Manual Synchronous Commit (`commitSync()`):** Blocks execution until broker ACKs offset commit.
3. **Manual Asynchronous Commit (`commitAsync()`):** Non-blocking offset commit with higher throughput.
