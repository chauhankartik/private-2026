# Apache Kafka Architecture Mind Map & Decision Matrix

> **Purpose:** Structural decision-making for choosing Kafka delivery guarantees, replication factors, partition counts, consumer rebalancing strategies, and cluster metadata modes.

---

## 📊 Feature & Architecture Summary Matrix

| Mechanism | Low-Level Representation | Trade-off / Limitation | Recommended Configuration |
| :--- | :--- | :--- | :--- |
| **Log Storage** | `.log` append-only segment files | Disk space usage | `log.retention.hours = 168` (7 days) |
| **Network Read** | Linux `sendfile` Zero-Copy syscall | Bypasses JVM user-space transformation | Enabled by default |
| **Producer Delivery** | `acks=all` + `enable.idempotence=true` | Slightly higher latency (~5ms) | Guaranteed At-Least-Once / EOS |
| **Consumer Rebalance**| Cooperative Sticky Assignor | Brief consumption pause on single partition | `partition.assignment.strategy = CooperativeSticky` |
| **Replication** | ISR (In-Sync Replicas) + High Watermark | Writes fail if healthy ISR count $< \text{min}$ | `replication.factor=3`, `min.insync.replicas=2` |
| **Metadata Consensus**| KRaft (Kafka Raft Controller Mode) | Disables legacy ZooKeeper ensemble | KIP-500 KRaft Mode |
| **Stream Processing** | Kafka Streams KStream / KTable | RocksDB state store disk overhead | State store changelog topics enabled |
