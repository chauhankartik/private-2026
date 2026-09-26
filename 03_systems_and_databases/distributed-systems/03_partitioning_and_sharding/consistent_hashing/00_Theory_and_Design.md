# Consistent Hashing — Deep Dive & Implementation

> **Core Focus:** Consistent Hashing Ring Topology, Virtual Nodes (VNodes), Minimal Key Movement, Hotspot Mitigation, and Hash Distribution (MD5 / Murmur3).

---

## 1. The Problem with Naive Hash Sharding

In simple hash sharding, partition index is determined by:

$$\text{Partition} = \text{hash}(\text{key}) \pmod N$$

Where $N$ is the number of database nodes.

### The Critical Flaw:
If $N$ changes (a node crashes or a new node is added), almost **100% of keys remap** to a new partition! This causes cluster-wide cache invalidation and massive data rebalancing traffic.

---

## 2. How Consistent Hashing Works

Consistent Hashing (Karger et al., MIT 1997) maps both **Nodes** and **Keys** to a circular 32-bit integer space ($0 \dots 2^{32} - 1$) called the **Hash Ring**.

```
                   CONSISTENT HASH RING
                           0
                      ┌─────────┐
                      │  Node A │ (Hash: 100)
                      │         │
       Node C ────────┼─────────┼──────── Key 1 (Hash: 150) -> Node B
      (Hash: 300)     │         │
                      │  Node B │ (Hash: 200)
                      └─────────┘
                      2^32 - 1
```

### Routing Rule:
To locate a key's server node, hash the key to a position on the ring, then walk **clockwise** until encountering the first node.

### Key Movement Guarantee:
When a node joins or leaves the cluster, only $K/N$ keys need to be reassigned (where $K$ is total keys and $N$ is total nodes).

---

## 3. Virtual Nodes (VNodes) for Uniform Balance

### The Heterogeneity & Hotspot Problem:
Standard consistent hashing with physical nodes creates non-uniform arc segments on the ring, leading to severe load imbalance (**Hotspots**).

### The Solution: Virtual Nodes
Each physical server is assigned $V$ virtual nodes on the ring (e.g. `NodeA-VN-0`, `NodeA-VN-1` ... `NodeA-VN-100`).

- **Benefits:**
  1. Even distribution across hash space.
  2. Heterogeneous capacity (Faster machines receive more virtual nodes).
  3. Parallel rebalancing when a node fails (Its virtual node ranges are split among all remaining physical nodes).

---

## 4. Production Systems Using Consistent Hashing

| System | Hash Algorithm | Virtual Node Count | Notes |
| :--- | :--- | :--- | :--- |
| **Apache Cassandra** | Murmur3 | 128 - 256 vnodes | Token Ring Architecture |
| **Amazon DynamoDB** | MD5 | Vnodes | Sloppy Quorum Handoff |
| **Discord (ScyllaDB)** | Murmur3 | Vnodes | Real-time messaging state |
