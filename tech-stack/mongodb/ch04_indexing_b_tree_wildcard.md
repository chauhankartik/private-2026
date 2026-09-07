# Chapter 4: Indexing Engine: B-Trees, ESR Rule & Specialized Indexes

## 1. B-Tree Index Mechanics

MongoDB indexes are implemented as **B-Trees** managed directly inside the WiredTiger storage engine. Each index entry maps an indexed key value to a Record ID (pointing to the underlying BSON document location).

```
                      +-------------------+
                      |   Root B-Tree Node|
                      |   [ "K" | "S" ]   |
                      +---------+---------+
                                |
             +------------------+------------------+
             |                                     |
             v                                     v
   +-------------------+                 +-------------------+
   | Internal Node     |                 | Internal Node     |
   | [ "B" | "F" ]     |                 | [ "U" | "Z" ]     |
   +---------+---------+                 +---------+---------+
             |                                     |
             v                                     v
   +-------------------+                 +-------------------+
   | Leaf Node         |                 | Leaf Node         |
   | Key -> RecordID   |                 | Key -> RecordID   |
   +-------------------+                 +-------------------+
```

---

## 2. Compound Indexes & The ESR Rule

A **Compound Index** indexes multiple fields within a document structure. The ordering of fields in a compound index is critical to query performance.

### The ESR (Equality, Sort, Range) Rule
When building compound indexes to support complex queries, specify fields in this exact order:

$$\text{Compound Index Keys} = [\text{Equality Fields}, \text{Sort Fields}, \text{Range Fields}]$$

```javascript
// Sample Query:
db.orders.find({ status: "ACTIVE", amount: { $gte: 100 } }).sort({ createdAt: -1 })

// Optimal Compound Index according to ESR Rule:
// Equality: status
// Sort: createdAt
// Range: amount
db.orders.createIndex({ status: 1, createdAt: -1, amount: 1 })
```

* **Equality:** Exact match filters (`status: "ACTIVE"`) narrow candidate B-Tree nodes to a single contiguous subtree.
* **Sort:** Sorting fields (`createdAt: -1`) allow the B-Tree cursor to traverse index keys in order, avoiding an expensive **In-Memory Blocking Sort**.
* **Range:** Range filters (`amount: { $gte: 100 }`) scan index keys sequentially within the matched subtree.

---

## 3. Multikey Indexes (Arrays) & Index Explosions

When an index field points to an **Array**, MongoDB automatically creates a **Multikey Index**, indexing every scalar element inside the array separately.

### Index Explosion Hazard:
If a document contains multiple array fields, MongoDB prohibits creating a compound index across more than one array field per document:
$$\text{Compound Multikey Constraint}: \le 1 \text{ Array Field per Compound Index}$$
* *Reason:* Indexing 2 array fields each containing 10 elements would generate a Cartesian product ($10 \times 10 = 100$) of B-Tree index entries for a single document.

---

## 4. Wildcard Indexes

Introduced in MongoDB 4.2, **Wildcard Indexes** (`$**`) allow indexing arbitrary, highly dynamic, or custom attributes without pre-defining explicit mappings.

```javascript
// Index all fields under customAttributes document
db.products.createIndex({ "customAttributes.$**": 1 })
```

### Internal Index Mechanics:
Wildcard indexes flatten nested document structures into tuples of `(path, value)` stored within the B-Tree:
$$\text{Index Entry}: (\text{path: "customAttributes.color"}, \text{value: "Red"})$$

---

## 5. Covered Queries

A query is **Covered** when:
1. All fields in the query matching criteria are part of an index.
2. All fields returned in the query projection are included in the same index.

```javascript
// Index:
db.users.createIndex({ email: 1, status: 1 })

// Covered Query (Execution plan uses IXSCAN only, 0 Document Fetches):
db.users.find(
  { email: "user@example.com" },
  { email: 1, status: 1, _id: 0 }
)
```
* **Performance Gain:** The WiredTiger query engine satisfies the query entirely from the B-Tree RAM index, skipping main collection page reads (`FETCH` stage) entirely.
