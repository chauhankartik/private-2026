# Chapter 11: Stream Processing — Deep Dive Notes

> **Core Theme:** Processing unbounded, real-time data streams. Comparing **Log-Based Message Brokers (Kafka)**, **Change Data Capture (CDC)**, **Event Sourcing**, **Stream Windows**, and **Exactly-Once Semantics**.

---

## 1. Message Brokers: Traditional Queues vs. Log-Based Brokers

A stream is an unbounded sequence of event records produced continuously over time.

### 1. Traditional Message Brokers (JMS, AMQP, RabbitMQ)
- Assigns individual messages to consumers.
- Message is deleted from broker as soon as consumer acknowledges processing (`ACK`).
- **Use Case:** Work queues where ordering is not important and message deletion frees RAM.

### 2. Log-Based Message Brokers (Apache Kafka, Apache Pulsar)
- Writes incoming events to an append-only, partitioned log file on disk.
- **Non-Destructive Reads:** Consumers do not delete messages; they maintain an explicit **Consumer Offset** pointer.
- Multiple independent consumer groups can read the exact same log stream at different speeds and replay past events from offset 0.

```
Topic Partition Log (Disk):
┌───┬───┬───┬───┬───┬───┬───┬───┐
│ 0 │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │ 7 │  ... Appends new events at end
└───┴───┴───┴───┴───┴───┴───┴───┘
              ▲           ▲
              │           └─ Consumer Group B (Offset 6)
              └─ Consumer Group A (Offset 3)
```

---

## 2. Databases and Streams

### The Dual-Write Problem
When an application manually updates a database AND updates a search index / cache in code, race conditions and network drops inevitably cause the database and search index to drift out of sync!

```
Application ─── (1) Write DB ────────► Postgres DB
Application ─── (2) Write Index ─X──► Elasticsearch (Fails or out of order!)
```

### 1. Change Data Capture (CDC)
Automatically extracts data changes from a database's low-level transaction log (e.g. Postgres WAL, MySQL binlog) and streams them to Kafka.
- **Tools:** Debezium, Bottled Water.
- Guarantees search index / cache derived views stay 100% consistent with database source of truth.

### 2. Event Sourcing
An architectural pattern where all changes to application state are stored as an immutable sequence of domain events.
- **State is Derived:** Application state is computed by replaying events from the log.
- **Immutability:** Events are never overwritten or deleted. Deletions are logged as "Tombstone / Reversal" events.

---

## 3. Time Semantics & Windowing

Stream processing operates over infinite time; operations must be grouped into finite **Windows**.

### Three Definitions of Time:
1. **Event Time:** The timestamp when the event actually occurred on the client device (Authoritative).
2. **Ingestion Time:** The timestamp when the event was received by the message broker.
3. **Processing Time:** The timestamp when the event reached the stream processing engine CPU.

> **The Late Event Problem:** Network outages can cause events generated at 10:00 to arrive at 10:30. Stream engines use **Watermarks** to bound event-time latency windows.

---

### Window Types:

```
Tumbling Window (Non-Overlapping):
[ 00:00 - 00:05 ] [ 00:05 - 00:10 ] [ 00:10 - 00:15 ]

Hopping / Sliding Window (Overlapping):
[ 00:00 - 00:05 ]
      [ 00:01 - 00:06 ]
            [ 00:02 - 00:07 ]

Session Window (Activity-Based):
[ User Activity Cluster ] ─── (Idle Timeout Gap) ───► [ Next Session ]
```

1. **Tumbling Windows:** Fixed length, non-overlapping (e.g., 5-minute fixed chunks).
2. **Hopping Windows:** Fixed length, overlapping by a slide interval (e.g., 5-minute window sliding every 1 minute).
3. **Session Windows:** Variable length bounded by periods of user inactivity (e.g., group web clicks until user is idle for 30 minutes).

---

## 4. Stream Joins

1. **Stream-Stream Join (Windowed Join):** Joining two real-time streams within a time window (e.g., Join `SearchEvent` with `ClickEvent` if they occur within 30 minutes for the same user).
2. **Stream-Table Join (Enrichment Join):** Joining a real-time event stream with a database table (e.g., Stream of credit card transactions joined with `Users` database table to add user billing address).
3. **Table-Table Join:** Maintaining materialized views of joined tables in real time.

---

## 5. Fault Tolerance & Exactly-Once Semantics

In stream processing, "Exactly-Once" means the final state outcome is identical to what would happen if every message was processed *exactly once*, even if node failures caused retries.

### How Stream Engines Achieve Exactly-Once:
1. **Chandy-Lamport Checkpointing (Apache Flink):** Periodically injects barrier markers into the stream. When an operator receives a barrier, it snapshots its state to durable storage atomically.
2. **Idempotent Writes:** Ensuring downstream state writes are idempotent (e.g., `UPSERT` using unique message ID instead of `INCR`).
3. **Two-Phase Commit (2PC) Transactions:** Atomic transactions spanning Kafka source offsets and target storage outputs.
