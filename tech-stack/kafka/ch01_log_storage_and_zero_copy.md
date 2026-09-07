# Chapter 1: Core Architecture & Log Storage Internals — Deep Dive Notes

> **Core Theme:** Kafka is an append-only, distributed commit log. How Kafka achieves multi-gigabyte throughput using **Zero-Copy Technology (`sendfile`)**, **OS Page Cache**, and **Sparse Indexing**.

---

## 1. Log-Based Streaming vs. Traditional Queues

```
TRADITIONAL QUEUE (RabbitMQ / JMS)          KAFKA LOG-BASED STREAM
┌──────────────────────────────────┐        ┌──────────────────────────────────┐
│ [Msg 1] [Msg 2] [Msg 3]          │        │ [0] [1] [2] [3] [4] [5] [6] [7]  │
│  (Deleted immediately after ACK) │        │  (Append-Only Persistent Log)    │
└──────────────────────────────────┘        └──────────────────────────────────┘
                                                          ▲           ▲
                                                          │           └─ Consumer B (Offset 6)
                                                          └─ Consumer A (Offset 3)
```

- **Traditional Queues:** Ephemeral. Messages are deleted as soon as consumers acknowledge receipt.
- **Kafka Distributed Log:** Immutable & Persistent. Messages remain on disk regardless of consumption, allowing multiple independent consumer groups to replay history at different speeds.

---

## 2. Partition Log Segment Structure (`.log`, `.index`, `.timeindex`)

Each Topic Partition is divided into physical **Log Segments** (e.g. 1GB segment files).

```
/var/lib/kafka/data/orders-0/
├── 00000000000000000000.log         (Raw binary messages)
├── 00000000000000000000.index       (Sparse Offset-to-Byte index)
└── 00000000000000000000.timeindex   (Timestamp-to-Offset index)
```

### Sparse Indexing ($O(\log N)$ Lookup):
Instead of indexing every single message, Kafka writes an entry to `.index` every $N$ bytes (Default: 4KB).

1. To read Offset `105`: Binary search `.index` file to locate closest preceding entry (e.g. Offset `100` $\to$ Byte Position `4096`).
2. Jump to byte `4096` in `.log` file and scan sequentially until reaching Offset `105`.

---

## 3. Zero-Copy Technology (`sendfile` Syscall)

Standard file transmission over network requires **4 context switches** and **3 data copies** through JVM memory space.

### Traditional Non-Zero-Copy Path:
$$\text{Disk} \xrightarrow{\text{DMA}} \text{OS Page Cache} \xrightarrow{\text{CPU}} \text{JVM User Memory} \xrightarrow{\text{CPU}} \text{Socket Buffer} \xrightarrow{\text{DMA}} \text{NIC}$$

### Kafka Zero-Copy Path (`sendfile` Syscall):
$$\text{Disk} \xrightarrow{\text{DMA}} \text{OS Page Cache} \xrightarrow{\text{Direct DMA Transfer}} \text{NIC}$$

```
Disk ──(DMA)──► OS Page Cache ──(Direct DMA Transfer via sendfile)──► Network Card (NIC)
```

- **Benefits:** Bypasses JVM user memory completely. Eliminates CPU copying and JVM Garbage Collection (GC) pauses!

---

## 4. Log Compaction (`cleanup.policy=compact`)

For state-restoration topics (e.g. Change Data Capture / KTables), Kafka supports **Log Compaction**.

- Retains at least the **most recent message value for every key** in the partition log.
- Background cleaner threads merge segment files, removing older duplicate keys while preserving full state history.
