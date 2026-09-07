# Chapter 7: ScyllaDB C++ Architecture & Performance Diagnostics

## 1. ScyllaDB & The Seastar C++ Framework

**ScyllaDB** is an open-source, API-compatible drop-in C++ replacement for Apache Cassandra. ScyllaDB addresses Cassandra's primary operational pain point—JVM Stop-The-World Garbage Collection (GC) pauses—by rewriting the database core using the **Seastar Framework**.

```
 Apache Cassandra (JVM Architecture)              ScyllaDB (Seastar C++ Architecture)
+------------------------------------+          +------------------------------------+
|  JVM Heap Memory & Garbage Collector|          |  Thread-Per-Core Shared-Nothing    |
|  Multi-threaded Shared State Locks |          |  Non-Blocking Event Loops (EPoll)  |
|  Kernel Context Switching & Paging |          |  Direct DMA Disk IO & Direct Network|
+------------------------------------+          +------------------------------------+
```

### Key Seastar Innovations:
1. **Thread-Per-Core Shared-Nothing Model:** Pin 1 OS thread to each CPU core (`pthread_setaffinity_np`). Each thread manages its own RAM memory pool, event loop, and I/O scheduler. CPU threads never share lock primitives, eliminating cross-core context switching and cache bouncing.
2. **Asynchronous DMA Direct I/O:** Bypasses kernel page cache locks using Linux `io_uring` / `aio` direct memory access (`O_DIRECT`).
3. **Zero JVM GC Overhead:** Manual C++ memory management guarantees 99.9th percentile latencies $< 1\text{ms}$ under heavy write loads.

---

## 2. Operational Diagnostics & Nodetool

Cassandra and ScyllaDB provide `nodetool` for cluster inspection and operational diagnostics.

### `nodetool status`
Displays cluster ring state, node availability, load, and token ownership:
```bash
Datacenter: dc1
================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address    Load       Tokens  Owns  Host ID                               Rack
UN  10.0.0.1   142.5 GB   256     33.3% 12345678-abcd-1234-abcd-123456789abc  rack1
UN  10.0.0.2   138.2 GB   256     33.3% 87654321-abcd-1234-abcd-123456789abc  rack1
UN  10.0.0.3   145.1 GB   256     33.4% abcdef12-abcd-1234-abcd-123456789abc  rack1
```

### `nodetool tpstats`
Inspects internal stage thread pools and dropped messages:
```bash
Pool Name                    Active   Pending      Completed   Blocked  All time blocked
MutationStage                     0         0      458920192         0                 0
ReadStage                         0         0       89201948         0                 0
GossipStage                       0         0        1293029         0                 0
```
* **`Pending` / `Blocked`:** Non-zero pending counts indicate thread pool bottlenecks. Dropped mutations (`MUTATION_REQ`) signal storage write stalls.

### `nodetool cfstats` / `tablestats`
Reports per-table metrics: SSTable count, read/write latency histograms, Bloom filter false positives, and tombstone scan counts.

---

## 3. Recommended Production Heap & Kernel Tuning

### Cassandra JVM Heap Sizing Guidelines (`jvm.options`)
* Max Heap Allocation: **16 GB to 31 GB** (Never exceed 32 GB to preserve Compressed OOPs).
* Garbage Collector: Use **G1GC** or **Shenandoah/ZGC**:
```ini
-XX:+UseG1GC
-XX:G1RSetUpdatingPauseTimePercent=5
-XX:MaxGCPauseMillis=50
```

### OS Kernel Tuning (`/etc/sysctl.conf`)
```ini
# Disable OS Swap to prevent latency spikes
vm.swappiness = 1

# Increase max open files for SSTables
fs.file-max = 1048576

# Memory mapping limits for index files
vm.max_map_count = 1048576
```
