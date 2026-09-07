# Kafka Recommended Reading List & Technical References

A curated list of books, Kafka Improvement Proposals (KIPs), foundational engineering blog posts, and source code walkthroughs for mastering Apache Kafka internals.

---

## 📚 Recommended Books

1. **_Kafka: The Definitive Guide (2nd Edition)_** — Gwen Shapira, Todd Palino, Rajini Sivaram, Krit Petty (O'Reilly)
   * **Why Read It:** The single best comprehensive textbook on Apache Kafka. Covers producer/consumer internals, broker log storage, ISR replication, KRaft metadata quorum, and security.
   * **Key Focus:** End-to-end Kafka architecture and operational management.

2. **_Designing Event-Driven Systems_** — Ben Stopford (O'Reilly)
   * **Why Read It:** Focuses on event-driven microservice architectures, stream processing, CQRS (Command Query Responsibility Segregation), and event sourcing using Kafka as an immutable commit log backbone.

3. **_Mastering Kafka Streams and ksqlDB_** — Mitch Seymour (O'Reilly)
   * **Why Read It:** Deep dive into stateful stream processing, windowing algorithms, state stores (RocksDB), interactive queries, and stream-table joins.

---

## 📄 Classic Papers & Kafka Improvement Proposals (KIPs)

1. **[The Log: What every software engineer should know about real-time data's unifying abstraction](https://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying-abstraction)** — Jay Kreps (2013)
   * **Why Read It:** The foundational LinkedIn engineering post that introduced Kafka's core philosophy: treating data integration as an append-only, distributed commit log.
2. **[KIP-500: Replace ZooKeeper with a Self-Managed Metadata Quorum (KRaft)](https://cwiki.apache.org/confluence/display/KAFKA/KIP-500%3A+Replace+ZooKeeper+with+a+Self-Managed+Metadata+Quorum)**
   * **Topics:** Replacing ZooKeeper with an event-driven Raft-based consensus protocol (KRaft) for sub-second failover and scaling to millions of partitions.
3. **[KIP-98: Exactly Once Delivery and Transactional Messaging](https://cwiki.apache.org/confluence/display/KAFKA/KIP-98+-+Exactly+Once+Delivery+and+Transactional+Messaging)**
   * **Topics:** Transaction Coordinator, `__transaction_state` log topic, 2-Phase Commit (2PC) protocol, and idempotent producers.

---

## 💻 Source Code References (Scala / Java Repository)

Explore core components in the [Apache Kafka GitHub Repository](https://github.com/apache/kafka):

* **`core/src/main/scala/kafka/log/LogSegment.scala`:** Partition log segment storage, `.log` binary records, `.index` offset index, and `.timeindex`.
* **`clients/src/main/java/org/apache/kafka/clients/producer/internals/RecordAccumulator.java`:** Memory buffer batching records into `ProducerBatch` per partition before network thread dispatch.
* **`core/src/main/scala/kafka/cluster/Partition.scala`:** In-Sync Replicas (ISR) tracking, High Watermark (`HW`), and Log End Offset (`LEO`) updates.
