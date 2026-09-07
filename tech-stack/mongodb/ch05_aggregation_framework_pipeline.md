# Chapter 5: Aggregation Framework Pipeline Engine

## 1. Aggregation Pipeline Architecture

The MongoDB **Aggregation Framework** is a data processing pipeline model where documents pass through a sequence of multi-stage transformations.

```
+---------------+      +---------+      +---------+      +---------+      +------------------+
| Raw Collection|----->| $match  |----->| $group  |----->| $sort   |----->| Pipeline Results |
| Documents     |      | (IXSCAN)|      | (Memory)|      | (Memory)|      | (Cursor Stream)  |
+---------------+      +---------+      +---------+      +---------+      +------------------+
```

---

## 2. Pipeline Stage Execution & Optimizations

The aggregation optimizer automatically reshuffles, folds, and pushes down stages prior to execution to minimize memory consumption and document movement.

### Pipeline Optimization Rules:
1. **`$match` Pushdown & Stage Reordering:** If a `$match` stage follows a `$sort` or `$project`, the query optimizer shifts `$match` to the front of the pipeline. If `$match` is the first stage, it can leverage B-Tree indexes (`IXSCAN`).
2. **Stage Coalescing (`$sort` + `$limit`):** When `$sort` is immediately followed by `$limit`, the optimizer combines them into a single $TopN$ heap sort algorithm in memory, consuming memory proportional to $N$ rather than sorting the entire result set.
3. **Stage Folding (`$project` + `$project`):** Consecutive projection stages are folded into a single projection operation.

---

## 3. Memory Limits & Disk Spillover Mechanics

By default, any aggregation pipeline stage (such as `$group`, `$sort`, `$bucket`, or `$unwind`) has a strict memory allocation limit:

$$\text{Stage Memory Limit} = 100 \text{ MB RAM}$$

### Disk Spillover Handling (`allowDiskUse: true`):
If a pipeline stage exceeds 100MB RAM without an index, MongoDB throws a runtime error:
`QueryExceededMemoryLimitNoDiskUseAllowed: Exceeded memory limit for $group...`

To handle large dataset transformations exceeding 100MB RAM, pass `{ allowDiskUse: true }`:
```javascript
db.orders.aggregate([
  { $group: { _id: "$customer_id", totalSpent: { $sum: "$amount" } } }
], { allowDiskUse: true })
```
* **Internal Behavior:** When `allowDiskUse` is enabled, stages exceeding 100MB write temporary uncompressed block files to the `_tmp` directory under `dbPath`.
* **Performance Penalty:** Disk spillover increases query execution latency significantly due to disk I/O.

---

## 4. Multi-Collection Joins (`$lookup`) Internals

The `$lookup` stage performs an equality or pipeline-based join between documents in the current collection and a target collection.

```javascript
db.orders.aggregate([
  {
    $lookup: {
      from: "customers",
      localField: "customerId",
      foreignField: "_id",
      as: "customerDetails"
    }
  }
])
```

### Internal Execution Mechanics:
* **Nested Loop Join:** For every document arriving from the preceding stage, `$lookup` executes a query against the target collection (`from`).
* **Critical Optimization:** Ensure the `foreignField` on the joined collection is indexed. Without an index on `foreignField`, `$lookup` performs a full collection scan (`COLLSCAN`) for **every** document in the outer pipeline, causing $O(M \times N)$ execution complexity.
