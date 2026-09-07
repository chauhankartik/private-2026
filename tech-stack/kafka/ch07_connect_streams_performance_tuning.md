# Chapter 7: Kafka Ecosystem: Connect, Streams & Tuning — Deep Dive Notes

> **Core Theme:** Extending Kafka using **Kafka Connect (CDC)**, **Kafka Streams (KStream/KTable)**, and tuning production metrics.

---

## 1. Kafka Connect & Change Data Capture (CDC)

Kafka Connect is a scalable, fault-tolerant framework for streaming data between Kafka and external systems.

```
PostgreSQL DB ──(Debezium CDC)──► Source Connector ──► Kafka Topic
                                                          │
Elasticsearch ◄────────────────── Sink Connector   ───────┘
```

- **Source Connectors:** Ingest data into Kafka (e.g. Debezium tailing Postgres WAL log).
- **Sink Connectors:** Export data from Kafka into data warehouses (Snowflake, Elasticsearch, S3).
- **Benefit:** Zero code required; fully managed offset tracking and task distribution.

---

## 2. Kafka Streams & Stream-Table Duality

Kafka Streams is a client library for building real-time stream processing microservices.

### The Stream-Table Duality:
- **`KStream` (Event Stream):** Represents an unbounded stream of independent immutable events (e.g., `(UserA, Click) -> (UserA, Click)`).
- **`KTable` (Changelog State):** Represents the latest current state for every key, similar to a database table (e.g., `(UserA, AddressV2)`).

```
               STREAM-TABLE DUALITY
Stream (Insert Stream): [ (K1, V1), (K1, V2), (K2, V1) ]
                               │
                      Aggregated to Table
                               ▼
Table (Latest State):  [ K1 -> V2,  K2 -> V1 ]
```

### State Stores (RocksDB):
Stateful operations (`windowedBy()`, `aggregate()`, `join()`) store local state inside an embedded **RocksDB** database instance per task, backed by an internal Kafka changelog topic for fault-tolerant recovery.

---

## 3. Production Performance Tuning & JMX Monitoring

### 1. JVM & OS Memory Configuration:
- **JVM Heap:** Keep small (e.g. 6GB to 10GB) to prevent long GC pauses.
- **OS Page Cache:** Assign remaining host RAM (64GB+) to Linux Page Cache for maximum Zero-Copy `sendfile` throughput.

### 2. Key JMX Metrics to Monitor (Alerting Rules):

| JMX Metric | Target Value | Severity if Violated | Description |
| :--- | :--- | :--- | :--- |
| **`UnderReplicatedPartitions`** | **`0`** | 🔴 CRITICAL | Count of partitions where ISR count $<$ replication factor. |
| **`OfflinePartitionsCount`** | **`0`** | 🔴 CRITICAL | Partitions with no active leader. Total unavailability! |
| **`ConsumerLag`** | Bounded | 🟡 WARNING | Distance between partition Log End Offset and Consumer Offset. |
| **`IsrShrinks` / `IsrExpands`** | Stable | 🟡 WARNING | Signals replica network drops or disk I/O bottlenecks. |
