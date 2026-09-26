# Database Internals 30-Second Interview Cheatsheet

---

## ⚡ B-Tree vs LSM-Tree Storage Engine Trade-Offs

| Metric | B-Tree / $B^+$ Tree | LSM-Tree (Log-Structured Merge-Tree) |
|---|---|---|
| **Primary Workload Target** | Read-Heavy Workloads & Point Lookups | Write-Heavy Workloads & High Ingestion |
| **Write Mechanism** | In-Place Overwrites (Random I/O on Disk) | Sequential Memory Append + Sequential SSTable Flushes |
| **Read Amplification** | Low (Single B+ Tree Index Walk: $O(\log N)$) | Higher (May check MemTable + Bloom Filters + Multiple Levels) |
| **Write Amplification** | High (Modifying 1 byte updates full 8KB Page) | Medium-High (Repeated SSTable Compaction passes) |
| **Space Amplification** | Medium (Internal Page Fragmentation / Free Slots) | Low (Fully Compacted Sorted Blocks) |
| **Concurrency Control** | Page Latches & Latching Crabbing | Lock-free MemTable Writes + Immutable SSTable Reads |

---

## 📊 Bloom Filter Mathematical Formulas

For a set of $n$ elements inserted into a bit array of size $m$ using $k$ independent hash functions:

- **Optimal Number of Hash Functions ($k$)**:
  $$k = \frac{m}{n} \ln 2 \approx 0.693 \times \frac{m}{n}$$

- **False Positive Probability ($p$)**:
  $$p \approx \left(1 - e^{-kn/m}\right)^k$$
  *(If $m/n = 10$ bits per element and $k = 7$ hash functions, false positive rate $p \approx 1\%$).*

---

## 🛡️ Raft Consensus Safety Invariants

1. **Election Safety**: At most one leader can be elected per term.
2. **Leader Append-Only**: A leader never overwrites or truncates its log entries; it only appends new entries.
3. **Log Matching**: If two logs contain an entry with the same index and term, then the logs are identical in all entries up through the given index.
4. **Leader Completeness**: If a log entry is committed in a given term, then that entry will be present in the logs of the leaders for all higher-numbered terms.
5. **State Machine Safety**: If a server has applied a log entry at a given index to its state machine, no other server will ever apply a different log entry for the same index.

---

## 🧠 Quorum Read & Write Overlap ($R + W > N$)

```mermaid
flowchart TD
    subgraph Cluster of N=5 Nodes
        Node1["Node 1 (Write ACK)"]
        Node2["Node 2 (Write ACK)"]
        Node3["Node 3 (Write ACK & Read Target)"]
        Node4["Node 4 (Read Target)"]
        Node5["Node 5 (Read Target)"]
    end

    W["Write Quorum W=3"] --> Node1
    W --> Node2
    W --> Node3

    R["Read Quorum R=3"] --> Node3
    R --> Node4
    R --> Node5

    Node3 -. "Overlap (Node 3): Guarantees latest version is read!" .-> R
```
