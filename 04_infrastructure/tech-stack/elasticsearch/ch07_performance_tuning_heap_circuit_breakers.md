# Chapter 7: Operational Performance Tuning, Heap & Circuit Breakers

## 1. JVM Heap Sizing SLA: The 32GB Compressed OOPs Limit

In an Elasticsearch production node, memory configuration strictly follows the **50/50 RAM Rule**:

$$\text{JVM Heap Allocation} = \min\left(0.5 \times \text{Total Server RAM}, 31\text{ GB}\right)$$

```
 Total System RAM: 64 GB
 +-----------------------------------+-----------------------------------+
 | JVM Heap Memory (31 GB)           | OS Page Cache (33 GB)             |
 | (Master state, Ingest buffers,    | (Lucene Inverted Indexes, FST,    |
 | Fielddata, Query Cache, Circuit)  | DocValues, OS File Cache)         |
 +-----------------------------------+-----------------------------------+
```

### The 32GB Compressed OOPs Boundary
In 64-bit Java Virtual Machines, pointers (Ordinary Object Pointers - OOPs) consume 64 bits (8 bytes). To optimize memory density, JVMs use **Compressed OOPs**, encoding object references as 32-bit offset integers using 8-byte alignment addresses.

* **Threshold:** Compressed OOPs function only up to approximately **$31.5 \text{ GB}$ to $32 \text{ GB}$** of heap memory.
* **The Penalty:** Allocating $33 \text{ GB}$ of JVM Heap disables Compressed OOPs. Object pointers instantly swell from 32 bits to 64 bits, losing up to $40\%$ of effective heap capacity and triggering severe Garbage Collection (GC) latency pauses!

---

## 2. Circuit Breakers & Memory Protection

Elasticsearch uses **Circuit Breakers** to prevent nodes from crashing due to OutOfMemory (OOM) errors during heavy query execution.

```
 Total JVM Heap Allocation (100%)
 +---------------------------------------------------------+
 | Parent Circuit Breaker Limit (default 95%)              |
 | +-----------------------------------------------------+ |
 | | Fielddata Breaker Limit (default 40%)               | |
 | +-----------------------------------------------------+ |
 | | Request Breaker Limit (default 60%)                 | |
 | +-----------------------------------------------------+ |
 | | Inflight Requests Breaker Limit (default 100%)      | |
 | +-----------------------------------------------------+ |
 +---------------------------------------------------------+
```

### Circuit Breakers Reference:
* **Parent Circuit Breaker (`indices.breaker.total.use_real_memory`):** Limits overall memory consumption across all breakers (default 95% of heap).
* **Fielddata Circuit Breaker (`indices.breaker.fielddata.limit`):** Prevents fielddata allocations from exceeding 40% of heap memory. If a query exceeds this limit, Elasticsearch rejects the request with:
  `CircuitBreakingException: [parent] Data too large, data for [<rubbish>] would be [X] which is larger than the limit...`

---

## 3. Real-Time Diagnostics APIs (`_cat` Endpoints)

Operational debugging and cluster node monitoring are conducted using the human-readable `_cat` REST APIs:

```bash
# Inspect Node Health, Memory Usage, and Roles
GET /_cat/nodes?v=true&h=ip,name,role,heap.percent,ram.percent,cpu,master

# Inspect Shard Assignments & Status
GET /_cat/shards?v=true&s=state

# Monitor Thread Pool Queue Backlog
GET /_cat/thread_pool?v=true&h=node_name,name,active,queue,rejected

# Monitor Active Lucene Segment Count & Memory Overhead
GET /_cat/segments?v=true
```

---

## 4. Index Lifecycle Management (ILM)

**Index Lifecycle Management (ILM)** automates index state transitions over time as data ages across hot, warm, cold, and frozen hardware tiers.

```
+---------------+      Rollover      +---------------+      Shrink & Merge  +---------------+      Searchable      +---------------+
| Hot Phase     |------------------->| Warm Phase    |--------------------->| Cold Phase    |--------------------->| Frozen Phase  |
| (Write Active)|                    | (Read Heavy)  |                      | (Read-Only)   |                      | (Mounted S3)  |
+---------------+                    +---------------+                      +---------------+                      +---------------+
```

### ILM Tiers:
1. **Hot Phase:** High-IOPS NVMe SSD data nodes handling all write indexing and frequent searches.
2. **Warm Phase:** Index is made read-only. Primary shards are shrunk (`_shrink` API) and force-merged (`_forcemerge`) to 1 Lucene segment per shard.
3. **Cold Phase:** Read-only index stored on cheaper spinning disk storage; replica shards are detached.
4. **Frozen Phase:** Index is unmounted from local storage and backed up as a Searchable Snapshot on cloud object storage (AWS S3 / GCP Storage).
