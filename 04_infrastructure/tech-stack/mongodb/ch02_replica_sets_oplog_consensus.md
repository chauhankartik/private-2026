# Chapter 2: Replica Sets, Oplog Replication & Distributed Consensus

## 1. Replica Set Architecture & Raft Consensus Variant

A MongoDB **Replica Set** consists of a group of `mongod` instances maintaining the exact same data set. High availability is achieved through single-primary master election based on a variant of the **Raft consensus algorithm**.

```
                   +-----------------------+
                   |  Primary (Write/Read) |
                   +-----------+-----------+
                               |
            Oplog Stream       | Heartbeats (2s)
            (Async/Sync)       | Ping & Votes
                               v
         +---------------------+---------------------+
         |                                           |
         v                                           v
+------------------+                       +------------------+
| Secondary Node 1 |                       | Secondary Node 2 |
|   (Read-Only)    |                       |   (Read-Only)    |
+------------------+                       +------------------+
```

### Election Mechanics
* **Heartbeat Mechanism:** All members send heartbeats (pings) to every other member every **2 seconds**. If a primary does not respond within **10 seconds** (`electionTimeoutMillis`), secondaries initiate an election.
* **Term & Priority:** Elections use monotonic terms (`electionTerm`). Nodes with `priority: 0` can never become Primary.
* **Strict Majority Rule:** A node requires a strict majority of voting members ($V_{\text{majority}} = \lfloor N/2 \rfloor + 1$) to be elected Primary.

---

## 2. Oplog Internal Architecture (`local.oplog.rs`)

The **Oplog** (Operations Log) is a special **capped collection** residing in the `local` database of every replica set member.

### Oplog Features:
1. **Idempotence:** Every operation in the Oplog is strictly idempotent. Multi-document operations are unrolled into explicit point mutations so re-applying an entry multiple times yields identical state.
2. **Field Structure:**
   ```json
   {
     "ts": Timestamp(1700000000, 1),
     "t": NumberLong(1),
     "h": NumberLong("4829104820194829"),
     "v": 2,
     "op": "i",
     "ns": "store.orders",
     "ui": UUID("a3b2c1..."),
     "o": { "_id": 101, "item": "Laptop", "status": "PAID" }
   }
   ```
   * `ts`: UTC timestamp and ordinal sequence counter.
   * `op`: Operation type (`i`: insert, `u`: update, `d`: delete, `c`: DDL command).
   * `ns`: Namespace (`database.collection`).

### Replication Pull Loop
* Secondaries maintain a long-poll tailing cursor over the Primary's `local.oplog.rs`.
* Operations are fetched in batches, buffered in memory, and applied concurrently to the local storage engine via multi-threaded batch appliers (grouped by document ID hash to prevent race conditions).

---

## 3. Write Concerns (`w` and `j`)

Write Concern specifies the level of acknowledgment requested from MongoDB for a write operation before returning success to the client.

```
client.collection.insert_one(doc, write_concern=WriteConcern(w="majority", wtimeout=5000))
```

| Write Concern | Acknowledgment Condition | Durability / Rollback Risk |
| :--- | :--- | :--- |
| `w: 1` | Primary node wrote operation to memory (WiredTiger cache). | **High Risk:** If primary crashes before oplog replicate, write is lost & rolled back. |
| `w: majority` | Write acknowledged by strict majority of voting replica set members. | **Zero Rollback Risk:** Guaranteed to survive any single-primary election failure. |
| `j: true` | Primary node flushed operation to disk Write-Ahead Log (Journal). | Protects against total power outage on primary node. |

---

## 4. Read Concerns & Read Preferences

Read Concern controls the isolation level and consistency guarantee of data read from the cluster.

### Read Concern Levels
1. **`local` / `available`:** Returns most recent data on the targeted node. No guarantee that data has been committed to a majority (susceptible to dirty reads and rollbacks).
2. **`majority`:** Returns data acknowledged by a strict majority of replica set nodes. Guaranteed never to be rolled back. Reads from a WiredTiger in-memory majority snapshot.
3. **`linearizable`:** Guarantees that the read reflects all majority-committed writes completed prior to the read. The primary node verifies its primary status with a majority heartbeat check before responding.

### Read Preferences
* `primary` (Default): All reads directed to Primary.
* `primaryPreferred`: Read from Primary; fall back to Secondary if Primary unavailable.
* `secondary`: All reads directed to Secondaries.
* `secondaryPreferred`: Read from Secondary; fall back to Primary.
* `nearest`: Read from node with lowest network latency (ping rtt).
