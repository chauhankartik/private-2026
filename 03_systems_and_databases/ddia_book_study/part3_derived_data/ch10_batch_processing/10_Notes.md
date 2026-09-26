# Chapter 10: Batch Processing — Deep Dive Notes

> **Core Theme:** Processing large static datasets offline. Comparing **Unix Pipelines**, **Hadoop MapReduce**, **Reduce-Side vs. Map-Side Joins**, and modern **Dataflow Engines (Apache Spark / Flink)**.

---

## 1. Primary Systems vs. Derived Data Systems

- **System of Record (Source of Truth):** Holds authoritative data. Writes are committed directly here (e.g., OLTP database).
- **Derived Data System:** Created by transforming source data (e.g., search index, cache, data warehouse aggregate). If derived data is lost, it can be recomputed from the source of truth.

---

## 2. Batch Processing with Unix Tools

The Unix design philosophy is the foundation for modern batch processing.

```bash
# Count top 5 requested URLs in log file using Unix tools:
cat access.log | awk '{print $7}' | sort | uniq -c | sort -rn | head -n 5
```

### Key Principles of Unix Philosophy:
1. **Composability:** Each tool does one thing well. Tools connect via standard byte streams (`stdin` / `stdout`).
2. **Immutability:** Inputs are never modified in-place. Output is piped to a new stream.
3. **Decoupling:** Producer and consumer are decoupled; piping handles buffering and backpressure.

---

## 3. MapReduce and Distributed File Systems (HDFS)

MapReduce scales Unix-style data processing across thousands of commodity machines using **HDFS (Hadoop Distributed File System)**.

```
                  MAPREDUCE EXECUTION WORKFLOW
                  
Input File (HDFS) ──► Mapper ──► Shuffle & Sort ──► Reducer ──► Output File (HDFS)
```

### The Three Phases of MapReduce:
1. **Map Phase:** Mapper reads input records, extracts key-value pairs $(K_1, V_1) \to \text{list}(K_2, V_2)$.
2. **Shuffle & Sort Phase:** MapReduce framework sorts output by key $K_2$ and routes all values for key $K_2$ to the same Reducer partition (`hash(K2) mod R`).
3. **Reduce Phase:** Reducer iterates over all values associated with key $K_2$ and emits final result $(K_2, \text{list}(V_2)) \to (K_3, V_3)$.

### Fault Tolerance in MapReduce:
- If a mapper or reducer node crashes, the coordinator re-schedules the task on another node.
- Works because Map functions are **Deterministic & Idempotent** (no side effects).

---

## 4. Join Algorithms in Batch Processing

Joining large datasets without an index requires specialized distributed join strategies.

### 1. Reduce-Side Joins (Sort-Merge Joins)
- Used when joining two large datasets ($A$ and $B$).
- Mappers read both datasets, tagging key-value pairs with source dataset ID.
- Shuffle & Sort partitions and sorts both datasets by the join key.
- Reducer receives sorted records for key $K$ from both $A$ and $B$ together in memory, performing the join.

### 2. Map-Side Joins (Optimized Joins)
Avoids the expensive Shuffle & Sort phase over the network.
- **Broadcast Hash Join:** If dataset $A$ is small enough to fit in RAM, load $A$ into an in-memory hash table on every mapper machine. Mappers stream large dataset $B$ through memory and join inline.
- **Partitioned Hash Join (Bucket Join):** If $A$ and $B$ are partitioned on the join key using the same partition count and hash function, mappers only load local partition of $A$ into memory.

---

## 5. Beyond MapReduce: Dataflow Engines (Spark, Flink, Tez)

### Problems with MapReduce:
MapReduce materializes intermediate state to HDFS disk after **every single Map and Reduce step**, causing massive disk I/O and serialization latency overhead.

### Dataflow Engine Architecture (Apache Spark / Apache Flink):
- Models an entire multi-stage batch job as a single **Directed Acyclic Graph (DAG)** of operators.
- Intermediate results are kept in memory or pipelined directly across network without writing to disk.

```
               SPARK DAG EXECUTION
Input ──► Map ──► Filter ──► FlatMap ──► ReduceByKey ──► Output
         └───────────── In-Memory Pipeline ─────────────┘
```

### Fault Tolerance via Lineage Graphs:
- Spark does not write intermediate datasets to disk for fault tolerance.
- Instead, it tracks **RDD Lineage** (the exact sequence of DAG operations that built a partition). If a partition is lost due to node failure, Spark re-computes only that lost partition from its lineage parent!
