# Chapter 5: Indexing Access Paths & Cost-Based Query Optimizers

Indexes and cost-based query planners are the primary mechanisms that determine relational database performance. This chapter explores **B+ Trees, GIN, BRIN, Columnstore indexes**, and the execution strategies used by **PostgreSQL** and **Microsoft SQL Server**.

---

## 1. Index Structures & Mechanics

### 1.1 B+ Tree Indexes
Both PostgreSQL and MS SQL Server use **B+ Trees** as their default indexing structure.
* **Internal Nodes:** Store search keys and child page pointers to direct search navigation.
* **Leaf Nodes:** Store indexed column values plus row locators (`ctid` in Postgres; Clustered Key or RID in SQL Server).
* **Doubly-Linked Leaf Chains:** Leaf pages are linked left-to-right (`prev_page` / `next_page`), enabling $O(\log N)$ point lookups and efficient sequential range scans.

```
                  [ Root Node: Key 50 ]
                 /                     \
      [ Page 1: Keys 10, 30 ]       [ Page 2: Keys 70, 90 ]
      /          |          \       /          |          \
 [Leaf: 1..9] <-> [Leaf: 10..29] <-> [Leaf: 30..49] <-> [Leaf: 50..69] ...
```

---

## 2. Advanced Index Types

### 2.1 PostgreSQL Specialized Indexes
* **GIN (Generalized Inverted Index):**
  * **Structure:** Inverted index mapping internal component values (e.g., keys/elements in JSONB or words in text) to a list or bitmap of heap `ctid` pointers.
  * **Use Case:** JSONB document queries (`jsonb_path_ops`), array containment (`@>`), full-text search (`tsvector`).
* **BRIN (Block Range Index):**
  * **Structure:** Maps continuous ranges of physical pages (e.g., 128 pages = 1 MB block range) storing only the `minimum` and `maximum` values contained within that range.
  * **Use Case:** Multi-gigabyte/terabyte append-only time-series tables ordered naturally by timestamp. Extremely tiny index footprint (kilobytes instead of gigabytes).

### 2.2 MS SQL Server Columnstore Indexes
* **Clustered Columnstore Index (CCI):**
  * **Structure:** Transforms row-oriented page storage into columnar format. Data is partitioned into **Rowgroups** (~1 million rows each). Each column within a rowgroup is compressed independently using dictionary encoding and vector bit-packing.
  * **Segment Elimination:** Metadata tracks min/max values for each column segment, allowing the engine to skip scanning entire rowgroups during queries.
  * **Batch Mode Execution:** Processes vectors of ~900 rows simultaneously in CPU registers instead of row-by-row iteration (Row Mode), delivering 10x-100x query acceleration for analytical workloads.

---

## 3. Cost-Based Query Optimizer (CBO) & Statistics

The **Query Optimizer** parses SQL, generates candidate execution trees, estimates cost using statistical data, and selects the lowest-cost execution plan.

### 3.1 Data Statistics & Cardinality Estimation
* **PostgreSQL (`pg_statistic` / `pg_stats`):**
  * Tracks Most Common Values (`MCV` list), MCV frequencies, quantile histograms (`histogram_bounds`), distinct value count (`n_distinct`), and correlation (physical row order vs logical value order).
* **MS SQL Server Statistics:**
  * Stores density vectors and 200-step equi-depth histograms. SQL Server automatically creates and updates column-level statistics (`AUTO_CREATE_STATISTICS`, `AUTO_UPDATE_STATISTICS`).

---

## 4. Join Algorithms Deep-Dive

When joining two tables ($R$ inner table, $S$ outer table), the optimizer chooses one of three fundamental physical join algorithms:

```
+-----------------------------------------------------------------------------------+
| 1. NESTED LOOP JOIN                                                               |
| For each row r in Outer Table S:                                                  |
|     Lookup matching rows in Inner Table R (using index on R)                      |
| Complexity: O(S * log R)  | Ideal: Small Outer table + Indexed Inner table        |
+-----------------------------------------------------------------------------------+
| 2. HASH JOIN                                                                      |
| Build Phase: Read smaller table R into in-memory Hash Table (keyed on join col)   |
| Probe Phase: Scan outer table S, hash join key, probe Hash Table for matches      |
| Complexity: O(R + S)      | Ideal: Large unindexed datasets fitting in work_mem  |
+-----------------------------------------------------------------------------------+
| 3. SORT-MERGE JOIN                                                                |
| Step 1: Sort Table R on Join Key (if not already sorted by index)                 |
| Step 2: Sort Table S on Join Key (if not already sorted by index)                 |
| Step 3: Advance dual pointers through R and S simultaneously                      |
| Complexity: O(R log R + S log S) | Ideal: Large sorted inputs / Range join tests |
+-----------------------------------------------------------------------------------+
```

---

## 5. Architectural Index Comparison

| Feature | PostgreSQL Indexing | MS SQL Server Indexing |
| :--- | :--- | :--- |
| **Default Index** | B-Tree | B-Tree (Clustered Index default for Primary Keys) |
| **JSON Search Index** | **GIN** (`jsonb_path_ops`) | Expression Indexes / B-Tree on Computed Columns |
| **Massive Time-Series Index** | **BRIN** (Block Range Index) | Clustered Columnstore / Partitioning |
| **Analytical / OLAP Execution** | Vectorized query engine via extension (`pg_analytics`) | Native **Columnstore Indexes with Batch Mode Execution** |
| **Covering Index Syntax** | `CREATE INDEX ... INCLUDE (col1, col2)` | `CREATE INDEX ... INCLUDE (col1, col2)` |

---

## 6. Staff Engineer Tuning Checklist
1. **Prevent Hash Join Disk Spilling:**
   * In Postgres: Increase `work_mem` (e.g., from 4MB to 64MB) to prevent hash joins from spilling batches to disk (`workfile`).
   * In SQL Server: Monitor `Hash Warning` events in Extended Events / SQL Profiler indicating `tempdb` spilling.
2. **Fix Cardinality Estimate Drift:**
   * Stale statistics cause bad join selection (e.g., choosing Nested Loop for 5 million rows). Run `ANALYZE table_name` in Postgres or `UPDATE STATISTICS table_name WITH FULLSCAN` in SQL Server.
3. **Exploit BRIN for Append-Only Logs:**
   * Replace massive B-trees on timestamp columns with BRIN indexes in Postgres. Reduces index size from 20GB to 50MB with near-zero insert overhead.
