# Relational Databases (PostgreSQL & MS SQL Server) Recommended Reading List

A curated list of books, technical papers, official documentation manuals, and source code pointers for mastering relational database storage engines, 8KB page structures, MVCC, WAL recovery, query optimization, and high availability.

---

## 📚 Recommended Books

1. **_Database Internals: A Deep Dive into How Distributed Data Systems Work_** — Alex Petrov (O'Reilly)
   * **Why Read It:** Comprehensive coverage of B+ trees, LSM-trees, page organization, buffer pools, ARIES crash recovery, transaction isolation levels, and MVCC mechanics.
   * **Key Focus:** Foundational database storage engine engineering.

2. **_PostgreSQL 14 Internals_** — Egor Rogov (Postgres Professional - Free Open Book)
   * **Why Read It:** The absolute gold standard deep-dive into PostgreSQL internals. Covers process architecture, buffer cache, page layout, WAL, `xmin`/`xmax` MVCC, autovacuum, locking, and all index types (B-tree, GIN, BRIN, GiST).
   * **Key Focus:** PostgreSQL low-level mechanics.

3. **_Pro SQL Server Internals (2nd Edition)_** — Dmitri Korotkevitch (Apress)
   * **Why Read It:** Definitive guide to Microsoft SQL Server internal engine design. Explains SQLOS thread scheduling, page structures, GAM/SGAM allocation maps, lock escalation, RCSI, and Columnstore batch mode.
   * **Key Focus:** MS SQL Server storage engine and query optimizer.

4. **_Designing Data-Intensive Applications_** — Martin Kleppmann (O'Reilly)
   * **Why Read It:** Chapters 2, 3, and 7 provide unmatched clarity on relational vs non-relational data models, index structures, write-ahead logs, and transaction isolation anomalies (Dirty Reads, Non-repeatable Reads, Phantom Reads, Write Skew).

---

## 📄 Official Manuals & Architecture Specifications

1. **[PostgreSQL Official Documentation — Internals Section](https://www.postgresql.org/docs/current/internals.html)**
   * **Topics:** `src/backend/` architecture, page header layouts (`PageHeaderData`), tuple header (`HeapTupleHeaderData`), WAL format, logical decoding.
2. **[Microsoft SQL Server Architecture Guides](https://learn.microsoft.com/en-us/sql/relational-databases/architecture-guide)**
   * **Topics:** Pages and Extents Architecture Guide, Transaction Locking and Row Versioning Guide, Thread and Task Architecture Guide, Query Processing Architecture Guide.

---

## 💻 Source Code References (PostgreSQL C Repository)

Explore core engine logic in the [PostgreSQL GitHub Repository](https://github.com/postgres/postgres):

* **`src/backend/storage/page/bufpage.c`:** Page header layout, line pointers (`ItemIdData`), free space management.
* **`src/backend/access/heap/heapam.c`:** Heap Access Method — tuple insertion, deletion, out-of-place update, `ctid` tracking.
* **`src/backend/access/transam/clog.c` & `varsup.c`:** Commit log (`pg_xact`), Transaction ID generation, and TxID freeze logic.
* **`src/backend/executor/nodeHashjoin.c`:** In-memory and disk-spilling Grace Hash Join execution node.
