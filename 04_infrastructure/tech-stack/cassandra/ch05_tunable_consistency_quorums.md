# Chapter 5: Tunable Distributed Consistency & Quorum Mathematics

## 1. Masterless Quorum Mathematics ($R + W > N$)

Cassandra allows tuning the trade-off between strict consistency and write/read latency on a per-query basis.

### Core Consistency Parameters:
* **$N$ (Replication Factor):** Total number of replica nodes in the cluster storing copies of a given data partition.
* **$W$ (Write Consistency Level):** Number of replica nodes that must acknowledge a write before returning success to the client application.
* **$R$ (Read Consistency Level):** Number of replica nodes that must respond to a read request before returning data to the client application.

### Strong Consistency Quorum Inequality
To guarantee that a read query will always see the most recent committed write, set $R$ and $W$ such that:

$$R + W > N$$

```
Replication Factor N = 3

   Scenario A: Strong Consistency (R + W > N)
   Write Level W = QUORUM (2 Nodes)  [ Node 1 (Updated), Node 2 (Updated) ]
   Read Level R  = QUORUM (2 Nodes)  [ Node 2 (Updated), Node 3 (Stale)   ]
   Overlap: Node 2 is present in both sets -> Client sees newest cell timestamp.

   Scenario B: Eventual Consistency (R + W <= N)
   Write Level W = ONE (1 Node)      [ Node 1 (Updated) ]
   Read Level R  = ONE (1 Node)      [ Node 3 (Stale)   ]
   No Overlap: Client reads stale data!
```

---

## 2. Consistency Levels Reference

| Consistency Level | Quorum Calculation | Latency Guarantee | Fault Tolerance ($N=3$) |
| :--- | :--- | :--- | :--- |
| `ONE` | 1 Replica | Lowest Write/Read Latency | Tolerates 2 node failures |
| `TWO` | 2 Replicas | Medium Latency | Tolerates 1 node failure |
| `QUORUM` | $\lfloor N / 2 \rfloor + 1$ | Balanced Latency | Tolerates 1 node failure |
| `LOCAL_QUORUM` | $\lfloor N_{\text{datacenter}} / 2 \rfloor + 1$ | Low Multi-DC Latency (DC local) | Tolerates local DC node failures |
| `ALL` | $N$ Replicas | Highest Latency / Strict Consistency | Tolerates 0 node failures |

---

## 3. Anti-Entropy & Failure Recovery Mechanisms

To reconcile stale data across replicas caused by network partitions or temporary node downtime, Cassandra implements three anti-entropy mechanisms:

### 1. Hinted Handoff
* When a write is sent to a coordinator node with write level `ONE` or `QUORUM`, and one of the target replicas is marked `DOWN`, the coordinator stores a **Hint** mutation locally on disk.
* When the target replica comes back online, the coordinator streams the buffered hints to catch it up.
* **Max Hint Window (`max_hint_window_in_ms`):** Default **3 hours**. If a node is down longer than 3 hours, hints stop accumulating.

### 2. Read Repair
* During a read query executed at `QUORUM`, the coordinator node requests full row data from 1 replica and cryptographic checksum hashes from the remaining $R-1$ replicas.
* If a hash mismatch is detected, the coordinator fetches full data from all $R$ replicas, determines the newest cell timestamp, returns the latest row to the client, and sends an asynchronous **Read Repair** write to the stale replica.

### 3. Merkle Trees & Anti-Entropy Repair (`nodetool repair`)
* For offline nodes down longer than 3 hours, manual or scheduled anti-entropy repair must be executed via `nodetool repair`.
* **Merkle Trees:** Nodes build cryptographic binary tree hashes of partition token ranges. Nodes exchange top-level Merkle tree hashes; if hashes match, token ranges are identical. Only mismatched leaves trigger network streaming of SSTable ranges.

```
                    Root Hash (Node A vs Node B)
                   /                            \
        Subtree Hash L                         Subtree Hash R (Mismatch!)
       /              \                       /              \
  Leaf 1            Leaf 2               Leaf 3 (Match)     Leaf 4 (Mismatch -> Stream Range!)
```
