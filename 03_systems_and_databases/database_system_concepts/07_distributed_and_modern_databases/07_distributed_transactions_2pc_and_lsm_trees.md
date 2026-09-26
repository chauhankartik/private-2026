# Part VII: Distributed Transactions, 2PC & LSM-Trees

Architectures for distributed database coordination and modern write-optimized storage engines (LSM-Trees).

---

## 📌 Two-Phase Commit (2PC) Protocol

In a distributed database, a transaction may update data across multiple database nodes (**Participants**). **Two-Phase Commit (2PC)** guarantees atomic commit across all nodes.

```mermaid
sequenceDiagram
    participant C as Transaction Coordinator
    participant P1 as Participant Node 1
    participant P2 as Participant Node 2

    Note over C,P2: Phase 1: Prepare Phase
    C->>P1: PREPARE (Can you commit Tx 100?)
    C->>P2: PREPARE (Can you commit Tx 100?)
    P1->>P1: Write VETO/OK to WAL
    P2->>P2: Write VETO/OK to WAL
    P1-->>C: VOTE_COMMIT
    P2-->>C: VOTE_COMMIT

    Note over C,P2: Phase 2: Commit Phase
    C->>C: Write GLOBAL_COMMIT to WAL
    C->>P1: GLOBAL_COMMIT
    C->>P2: GLOBAL_COMMIT
    P1->>P1: Commit & Release Locks
    P2->>P2: Commit & Release Locks
    P1-->>C: ACK
    P2-->>C: ACK
```

---

## 📌 CAP Theorem vs PACELC Theorem

- **CAP Theorem**: In a network partition ($P$), a distributed system can choose **Consistency ($C$)** OR **Availability ($A$)**, but not both.
- **PACELC Theorem**:
  - If **P**artition: Choose between **A**vailability or **C**onsistency.
  - **E**lse (Normal operation): Choose between **L**atency or **C**onsistency.

---

## 📌 LSM-Trees (Log-Structured Merge-Trees) vs B+ Trees

Traditional B+ Trees use in-place updates, causing random I/O bottlenecks under heavy write workloads.
**LSM-Trees** (used in RocksDB, Cassandra, Spanner) convert all writes into sequential memory appends.

```mermaid
flowchart TD
    Write["Client Write Request"] --> WAL["1. Append to Disk WAL (Durability)"]
    Write --> MemTable["2. Insert into In-Memory MemTable (SkipList)"]
    
    MemTable -- MemTable Full (~64MB) --> Flush["3. Flush to Disk as Immutable Level 0 SSTable"]
    
    subgraph Storage Level SSTables
        Flush --> L0["Level 0 SSTables (Unsorted Ranges)"]
        L0 -- Background Compaction --> L1["Level 1 SSTables (Sorted Non-overlapping Ranges)"]
        L1 -- Background Compaction --> L2["Level 2 SSTables"]
    end
```

### LSM-Tree vs B+ Tree Trade-off Matrix

| Metric | B+ Tree | LSM-Tree |
|---|---|---|
| **Write Throughput** | Low (Random I/O & Page In-Place Overwrites) | High (Sequential Memory Append & Flush) |
| **Read Latency** | Low (Direct Index Lookup $O(\log N)$) | Higher (May check MemTable + Bloom Filters + SSTables) |
| **Space Amplification** | Medium (Fragmented Page Free Space) | Low (Compacted Immutable Blocks) |
| **Write Amplification** | High (Updating 1 Byte writes full 8KB Page) | Medium-High (Repeated Compaction Passes) |
