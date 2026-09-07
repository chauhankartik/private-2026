# SQL Architecture & Interview Mind Map

This document presents a structured visual and conceptual breakdown of SQL query execution, window functions, indexing, and interview problem patterns.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((SQL Engine & Interview Patterns))
    Query Execution Order
      FROM and JOINs
      WHERE Row Filter
      GROUP BY Aggregation
      HAVING Group Filter
      SELECT Column Calculations
      DISTINCT Deduplication
      ORDER BY Sorting
      LIMIT Offset Pagination
    Window Function Mechanics
      Ranking Functions
        ROW NUMBER Unique Monotonic
        RANK Gaps on Tie
        DENSE RANK No Gaps on Tie
        NTILE Buckets
      Value Functions
        LAG Previous Row
        LEAD Next Row
        FIRST VALUE Initial Row
        LAST VALUE Boundary Row
      Window Framing
        ROWS Physical Count
        RANGE Logical Values
        UNBOUNDED PRECEDING to CURRENT ROW
    Advanced Interview Algorithms
      Gaps and Islands
        ROW NUMBER Difference Technique
        LAG Boundary Flag Technique
      User Sessionization
        30 Minute Timeout Threshold
        Conditional SUM Running Total
      Cohort Analysis
        First Purchase Date
        MoM Retention Percentage
      Recursive Queries
        Anchor Member
        Recursive Member UNION ALL
      Exact Percentiles
        ROW NUMBER Division Math
        Middle Parity Filter
    Performance Optimization
      Sargable Filtering
        Avoid Function Calls on Indexed Columns
        Avoid Leading Wildcard LIKE
      Index Strategies
        B Tree Balanced Range Scans
        Composite Index Leftmost Prefix
        Covering Index Zero Table Heap Fetch
```

---

## 2. Component Reference Table

| Component | Execution Phase | Common Interview Pitfalls |
| :--- | :--- | :--- |
| **`WHERE` vs `HAVING`** | `WHERE` filters before aggregation; `HAVING` filters after. | Using `WHERE` on aggregated aliases (`COUNT(*) > 5`). |
| **`RANK()` vs `DENSE_RANK()`** | `RANK()` skips rank numbers on ties (1, 2, 2, 4); `DENSE_RANK()` does not skip (1, 2, 2, 3). | Selecting the wrong function when calculating 2nd highest salary. |
| **Window Frame `ROWS`** | Defines physical row boundaries (`ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`). | Default frame behavior without `ORDER BY` vs with `ORDER BY`. |
| **Recursive CTE** | Executes iterative union loop until no new rows are returned. | Forgetting termination condition causing infinite recursion loop. |
| **`NULL` Comparison** | Three-valued logic (`TRUE`, `FALSE`, `UNKNOWN`). `NULL = NULL` is `UNKNOWN` (evaluates to false). | Using `= NULL` instead of `IS NULL` or `IS NOT DISTINCT FROM`. |
