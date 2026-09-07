# Chapter 2: 8KB Page Layouts, Heap Tables vs Clustered Indexes

Both **PostgreSQL** and **Microsoft SQL Server** use an **8KB (8192 bytes)** block size as the fundamental unit of disk I/O and buffer pool storage. However, their internal page organization, space allocation tracking, and table storage models (Heap vs Clustered Index) differ significantly.

---

## 1. PostgreSQL 8KB Page Layout

A PostgreSQL data file is partitioned into 8KB blocks. Every page has a top-down and bottom-up structure that allows variable-length tuples without internal page fragmentation.

```
+-------------------------------------------------------------+
| PageHeaderData (24 bytes)                                   |
+-------------------------------------------------------------+
| ItemIdData[0] | ItemIdData[1] | ItemIdData[2] ...          | --> Line Pointers (4 bytes each)
| (Grows Downward ->)                                         |
+-------------------------------------------------------------+
|                     <--- FREE SPACE --->                    |
+-------------------------------------------------------------+
| (Grows Upward <-)                                           |
| ... Tuple 3 | Tuple 2 | Tuple 1                             | --> HeapTupleData (Data Rows)
+-------------------------------------------------------------+
| Special Space (Index specific metadata, e.g., B-tree flags) |
+-------------------------------------------------------------+
```

### 1.1 Header & Line Pointer Array
* **`PageHeaderData` (24 bytes):** Contains LSN (Log Sequence Number for WAL), checksum, `pd_lower` (byte offset to end of line pointers), `pd_upper` (byte offset to start of tuple data), `pd_special` (offset to special space).
* **Line Pointers (`ItemIdData` - 4 bytes each):** An array of pointers located immediately after the header. Each line pointer stores:
  * `off`: Byte offset to the actual tuple data in the page.
  * `flags`: Tuple state (`LP_USED`, `LP_UNUSED`, `LP_DEAD`, `LP_REDIRECT`).
  * `len`: Length of the tuple in bytes.
* **Tuple Identifier (`ctid`):** A physical address tuple identifier composed of `(BlockNumber, TupleOffsetIndex)`.
  * `BlockNumber`: 32-bit integer representing the page offset within the file.
  * `TupleOffsetIndex`: 16-bit integer pointing to the `ItemIdData` line pointer array index (1-based offset).

### 1.2 Auxiliary Storage Files
For every table file (e.g., file node `16384`), PostgreSQL maintains two companion files:
1. **Free Space Map (`_fsm`):** A binary tree of 8KB pages tracking the available free space in each data page, allowing fast target page selection for `INSERT` operations without scanning the heap.
2. **Visibility Map (`_vm`):** Tracks two key flags per heap page:
   * **`all-visible`:** All tuples on the page are visible to all current and future transactions (allows **Index-Only Scans** to skip visiting the heap page).
   * **`all-frozen`:** All tuples on the page have been frozen by `VACUUM` (prevents Transaction ID wraparound scans).

---

## 2. MS SQL Server 8KB Page Layout

SQL Server also uses 8KB pages (8192 bytes), but structures page headers, slot arrays, and allocation maps differently.

```
+-------------------------------------------------------------+
| Page Header (96 bytes fixed size)                           |
+-------------------------------------------------------------+
| Data Rows (Tuples, Variable-length column offset array)    |
|                                                             |
+-------------------------------------------------------------+
| Offset Slot Array (2 bytes per row, reverse order)          |
| [Slot 2 Offset] | [Slot 1 Offset] | [Slot 0 Offset]         |
+-------------------------------------------------------------+
```

### 2.1 Header & Offset Array
* **Page Header (96 bytes):** Stores Page ID, File ID, Object ID, Index ID, Free Space byte count, and pointers to Previous/Next pages in doubly-linked index chains.
* **Row Offset Array:** Located at the **very end of the page**, growing backward towards the header. Each slot is a 2-byte pointer indicating the byte offset from the start of the page where the corresponding data row begins.

### 2.2 SQL Server Page Allocation Tracking Maps
SQL Server manages disk allocation via specialized metadata pages placed every 64,000 pages (approx. 512 MB):
* **PFS (Page Free Space):** Tracks allocation status and free space percentage per page (Empty, 1-50%, 51-80%, 81-95%, 96-100%).
* **GAM (Global Allocation Map):** Tracks which **Extents** (groups of 8 contiguous 8KB pages = 64KB) are allocated.
* **SGAM (Shared Global Allocation Map):** Tracks mixed extents used for storing small tables with multiple objects.
* **IAM (Index Allocation Map):** Maps the extents used by a specific table or index within a database file.

---

## 3. Heap Tables vs Clustered Index Tables

| Architectural Feature | PostgreSQL Heap Model | MS SQL Server Clustered Index Model |
| :--- | :--- | :--- |
| **Primary Storage Unit** | **Heap File** (Unordered tuple placement) | **Clustered B+ Tree** (Data stored directly in leaf nodes) |
| **Row Locator** | `ctid` (Physical Page + Line Pointer Index) | **Clustering Key Value** (or RID `File:Page:Slot` for Heaps) |
| **Secondary Index Pointer** | Points directly to physical `ctid` | Points to the **Clustering Key** (requires Index Lookup traversal) |
| **Page Split Behavior** | No page splits on insert (appends to page with space or new page) | Page splits occur when inserting out-of-order rows into full leaf pages |
| **UPDATE Behavior** | Out-of-place tuple insertion (`xmin`/`xmax` versioning) | In-place update (unless row expands beyond page capacity) |

---

## 4. Deep-Dive: Mechanical Trade-offs

### 4.1 Secondary Index Traversal & Amplification
* **PostgreSQL:**
  * **Advantage:** Secondary index lookups are **O(log N)** to index leaf + **1 direct disk hit** to `ctid`. Fast reads for secondary queries.
  * **Disadvantage:** Any `UPDATE` that modifies a column indexed by secondary indexes forces insertion of a new tuple and **updates to all secondary index trees** pointing to the new `ctid` (mitigated by HOT - Heap-Only Tuple updates).
* **MS SQL Server:**
  * **Advantage:** Physical row relocations (e.g., page splits) do **not** require updates to secondary indexes, because secondary indexes store logical Clustering Keys rather than physical pointers.
  * **Disadvantage:** Every secondary index search requires a **Double Lookup** (B+ Tree traversal on Secondary Index followed by B+ Tree traversal on Clustered Index).

### 4.2 Page Splits vs Table Bloat
* **SQL Server Page Splits:** When inserting a row into a full clustered index page, SQL Server splits the page 50/50, allocating a new page and moving half the rows. This causes **index fragmentation** and write amplification.
* **Postgres Table Bloat:** Postgres never splits heap pages. Unused dead tuples accumulate inside pages until `VACUUM` reclaims line pointers. Dead space causes **table bloat**, increasing I/O overhead until `VACUUM FULL` or `pg_repack` rewrites the heap file.

---

## 5. Staff Engineer Summary & Best Practices
1. **Postgres Cluster Emulation:** Postgres does support `CLUSTER table_name USING index_name;`, but it performs a **one-time heap rewrite**. It does not maintain ordering on subsequent `INSERT`/`UPDATE` operations.
2. **Choosing Clustered Keys in SQL Server:** Always choose a **monotonically increasing key** (e.g., `BIGINT IDENTITY` or `Sequential GUID`) to avoid mid-page splits and high fragmentation.
3. **Postgres Line Pointer Redirects:** When HOT (Heap-Only Tuple) optimization triggers, dead line pointers are marked `LP_REDIRECT`, pointing to the updated tuple version on the same 8KB page without touching secondary indexes.
