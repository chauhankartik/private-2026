# Chapter 7: Performance Tuning & Operational Diagnostics

## 1. Real-Time Performance Diagnostics Tools

MongoDB provides built-in command-line tools and database commands to inspect server metrics, locks, thread queues, and storage performance.

### `mongostat`
`mongostat` provides a real-time status view of running `mongod` / `mongos` instances (sampled every 1 second).

```bash
mongostat --host rs0/10.0.0.1:27017,10.0.0.2:27017 1
```
* **`insert`, `query`, `update`, `delete`:** Ops per second.
* **`getmore`:** Cursor batch iterations per second.
* **`command`:** Internal database commands per second.
* **`dirty` / `used`:** WiredTiger cache percentages. If `dirty` $> 20\%$, writes will stall.
* **`qr|qw` / `ar|aw`:** Queued Reads/Writes vs Active Reads/Writes. High queued ops indicate thread pool saturation.

### `mongotop`
`mongotop` tracks read/write CPU and disk time spent by `mongod` per collection.

```bash
mongotop 1
```

---

## 2. Database Profiler & Slow Query Analysis

The **Database Profiler** captures slow queries and writes execution details to the `system.profile` collection.

```javascript
// Enable profiler for queries taking longer than 100ms
db.setProfilingLevel(1, { slowms: 100 })

// Query top 5 slowest queries in system.profile
db.system.profile.find().sort({ millis: -1 }).limit(5).pretty()
```

### Analyzing `explain("executionStats")`
```javascript
db.orders.find({ status: "PAID" }).sort({ createdAt: -1 }).explain("executionStats")
```

Key Execution Metrics:
* **`stage`:**
  * `COLLSCAN`: Full collection scan (**Bad** - missing index).
  * `IXSCAN`: Index scan (**Good**).
  * `FETCH`: Document retrieval from disk/cache.
  * `SORT`: In-memory sorting (**Bad** - missing sort index).
* **`totalKeysExamined` vs `nReturned`:** Ratio of index keys scanned to documents returned. In an optimal query, $\frac{\text{totalKeysExamined}}{\text{nReturned}} \approx 1$.
* **`totalDocsExamined`:** Number of documents read from WiredTiger storage. For covered queries, `totalDocsExamined` is `0`.

---

## 3. WiredTiger Memory Tuning & Index Cardinality

### Cache Size Sizing
Set `storage.wiredtiger.engineConfig.cacheSizeGB` in `/etc/mongod.conf`:
```yaml
storage:
  dbPath: /var/lib/mongodb
  wiredTiger:
    engineConfig:
      cacheSizeGB: 16
```
* Rule of thumb: Dedicated MongoDB servers should allocate 50% of system RAM to WiredTiger, leaving the remainder for the OS filesystem cache and `mongod` connection overhead.

---

## 4. Storage Defragmentation (`compact`)

Over time, frequent document deletions and updates lead to fragmented disk blocks within WiredTiger collection files.

```javascript
// Compact collection to release unallocated space back to OS
db.runCommand({ compact: "orders" })
```
* **Impact:** Rebuilds collection B-Tree structures and indexes, freeing allocated empty storage blocks to the underlying filesystem.
