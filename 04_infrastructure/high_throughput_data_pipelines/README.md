# High-Throughput Data Pipelines

A production-grade masterclass on architecting, implementing, and operating high-throughput stream and batch data processing pipelines. Covers 1M+ QPS ingestion, stream processing (Kafka, Flink, Spark), columnar storage formats (Parquet/Avro), zero-copy I/O, Chandy-Lamport checkpointing, exactly-once semantics, and backpressure management.

---

## 📐 Table of Contents

| Section | Subject | Key Concepts |
| :--- | :--- | :--- |
| **00. Cheatsheet** | [Pipeline Cheatsheet](00_pipeline_cheatsheet.md) | Lambda vs Kappa Architecture Comparison, SerDe Benchmark Table, Windowing Cheat Sheet, Watermark Formulas |
| **01. Ingestion & Streaming** | [Kafka, Flink & Windowing](01_ingestion_and_streaming/01_kafka_flink_and_windowing.md) | High-throughput Kafka tuning (batch.size, linger.ms, compression), Flink stream execution DAG, Tumbling/Sliding/Session Windows |
| **02. Storage & Compression** | [Parquet, Columnar & Zero-Copy](02_storage_formats_and_compression/02_parquet_columnar_and_zero_copy.md) | Row-based vs Columnar storage, Parquet Page Layout (Dictionary, Bit-packing, RLE), Linux Kernel Zero-Copy (`sendfile`/`splice`), DirectByteBuffer |
| **03. Fault Tolerance & Exactly-Once** | [Watermarks, Checkpoints & Exactly-Once](03_fault_tolerance_and_exactly_once/03_watermarks_checkpoints_and_exactly_once.md) | Chandy-Lamport Distributed Snapshots, Flink 2PC Sink (`TwoPhaseCommitSinkFunction`), Reactive Streams Backpressure, Key Salting for Skew |

---

## 🏗️ 1 Million QPS High-Throughput Pipeline Architecture

```mermaid
flowchart TD
    subgraph EventSources ["Ingestion Layer (1 Million Events / Sec)"]
        Sensors["IoT Sensors / Microservices"]
        SDK["SDK Producers (Batching + Snappy)"]
    end

    subgraph StreamingMesh ["Distributed Event Stream (Kafka Cluster)"]
        Broker1["Kafka Broker 1 (Partition 0)"]
        Broker2["Kafka Broker 2 (Partition 1)"]
        Broker3["Kafka Broker 3 (Partition 2)"]
    end

    subgraph ProcessingEngine ["Stream Processing Engine (Apache Flink)"]
        SourceOp["Flink Kafka Source Operator"]
        WatermarkOp["Watermark Generator (Bounded Out-of-Orderness)"]
        KeyedWindow["Keyed Aggregation Window (5-Min Tumbling)"]
        Checkpointer["Chandy-Lamport State Checkpointer (RocksDB State Backend)"]
    end

    subgraph ServingStorage ["Storage & Lakehouse Tier"]
        ClickHouse[(ClickHouse OLAP - Sub-Second Analytics)]
        S3Lakehouse[(S3 Data Lakehouse - Parquet Format)]
        Prometheus[(Prometheus Metrics & Backpressure Alerting)]
    end

    Sensors --> SDK --> Broker1 & Broker2 & Broker3
    Broker1 & Broker2 & Broker3 -->|Zero-Copy Kernel Transport| SourceOp
    SourceOp --> WatermarkOp --> KeyedWindow
    KeyedWindow --> Checkpointer
    KeyedWindow -->|2PC Exactly-Once Sink| ClickHouse
    KeyedWindow -->|Bulk Parquet Writer| S3Lakehouse
    ProcessingEngine -.->|Metrics| Prometheus
```

---

## 🏛️ Lambda Architecture vs Kappa Architecture

```mermaid
flowchart TD
    subgraph LambdaArch ["Lambda Architecture (Dual Pipeline)"]
        BatchLayer["Batch Layer (Hadoop / Spark - Accurate, High Latency)"]
        SpeedLayer["Speed Layer (Storm / Flink - Low Latency, Approximate)"]
        ServingLayer1["Serving Layer (Merged Batch + Real-time Views)"]
    end

    subgraph KappaArch ["Kappa Architecture (Single Stream-First Pipeline)"]
        StreamLog["Immutable Event Log (Kafka / Pulsar)"]
        StreamEngine["Stream Engine (Flink - Single Codebase for Real-time & Historical Catch-up)"]
        ServingLayer2["Unified Serving Layer (ClickHouse / Iceberg)"]
    end
```

| Architecture | Pipeline Topology | Codebase Maintenance | Replay Capability | Primary Disadvantage |
| :--- | :--- | :--- | :--- | :--- |
| **Lambda** | Dual (Batch + Speed) | High (2 separate codebases) | Excellent | Logic drift between batch and real-time streams |
| **Kappa** | Single (Stream-only) | Low (1 unified codebase) | Excellent (Replay Kafka log) | Requires stream engine capable of historical backfill |
