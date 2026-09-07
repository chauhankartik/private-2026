# SQL Interview Prep — Syntax Cheat Sheet & Theory
> **Role:** Senior Data Engineer reference. All clauses, execution order, window functions,
> indexing strategy, and query optimization in one place.

---

## 1. Logical Query Processing Order

This is the **actual order the engine evaluates your query** — NOT the order you write it.
Understanding this is the key to writing correct, optimized SQL.

```
1. FROM        → identify tables, perform JOINs
2. WHERE       → filter rows (before grouping) — cannot use aliases or aggregates
3. GROUP BY    → aggregate rows into groups
4. HAVING      → filter groups (after aggregation) — CAN use aggregates
5. SELECT      → compute expressions, aliases created here
6. DISTINCT    → remove duplicates
7. ORDER BY    → sort results — CAN use SELECT aliases
8. LIMIT/TOP   → truncate output
```

**Common trap:**
```sql
-- ✗ WRONG — WHERE cannot see the alias or aggregate
SELECT dept, COUNT(*) AS cnt FROM emp WHERE cnt > 10 GROUP BY dept;

-- ✓ CORRECT — HAVING runs after GROUP BY
SELECT dept, COUNT(*) AS cnt FROM emp GROUP BY dept HAVING COUNT(*) > 10;
```

---

## 2. SELECT Clause Reference

```sql
SELECT DISTINCT column1, column2                -- unique rows
       expression AS alias,                     -- computed column
       CASE WHEN cond THEN val1 ELSE val2 END   -- conditional
FROM   table1 t1
JOIN   table2 t2 ON t1.id = t2.id              -- inner join
LEFT   JOIN table3 t3 ON t1.id = t3.id         -- left outer join
WHERE  condition                                -- row filter
GROUP  BY column1                               -- aggregation
HAVING aggregate_condition                      -- group filter
ORDER  BY column1 ASC, column2 DESC            -- sorting
LIMIT  10 OFFSET 20;                           -- pagination
```

---

## 3. JOIN Types

```
INNER JOIN   → only matching rows in BOTH tables
LEFT JOIN    → all rows from LEFT + matching from right (NULL if no match)
RIGHT JOIN   → all rows from RIGHT + matching from left
FULL JOIN    → all rows from BOTH (NULL where no match)
CROSS JOIN   → cartesian product (every row × every row)
SELF JOIN    → table joined with itself (aliased)
```

```sql
-- INNER JOIN: employees with their managers (both must exist)
SELECT e.name, m.name AS manager
FROM employees e
JOIN employees m ON e.manager_id = m.id;

-- LEFT JOIN: all employees, even without a department
SELECT e.name, d.dept_name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.id;

-- Find employees WITHOUT a department (anti-join pattern)
SELECT e.name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.id
WHERE d.id IS NULL;           -- ← key: filter on the RIGHT table's NULL
```

---

## 4. Aggregation Functions

```sql
COUNT(*)          -- count all rows (including NULLs)
COUNT(col)        -- count non-NULL values
COUNT(DISTINCT col) -- count unique non-NULL values
SUM(col)          -- sum (ignores NULLs)
AVG(col)          -- average (ignores NULLs)
MIN(col)          -- minimum
MAX(col)          -- maximum
GROUP_CONCAT(col) -- MySQL: concatenate values in group
STRING_AGG(col, ',') -- PostgreSQL: same as above
```

---

## 5. HAVING vs WHERE

| | WHERE | HAVING |
|---|---|---|
| **Execution phase** | Before GROUP BY | After GROUP BY |
| **Can use aggregates?** | ✗ No | ✓ Yes |
| **Can use row filters?** | ✓ Yes | ✓ Yes (but slower — filter BEFORE grouping when possible) |
| **Performance** | Faster (filters early, reduces rows for grouping) | Slower (groups first, then filters) |

```sql
-- Best practice: use WHERE for non-aggregate conditions (runs earlier)
SELECT dept, COUNT(*) AS cnt
FROM employees
WHERE salary > 50000       -- ← filter rows BEFORE grouping (fast)
GROUP BY dept
HAVING COUNT(*) >= 5;      -- ← filter groups AFTER aggregation
```

---

## 6. Window Functions — The Most Important Interview Topic

Window functions compute over a **sliding window of rows** related to the current row.
Unlike GROUP BY, they do NOT collapse rows.

```sql
function() OVER (
    PARTITION BY col    -- divide into groups (like GROUP BY but rows preserved)
    ORDER BY col        -- order within each partition
    ROWS/RANGE frame    -- optional: define the window frame
)
```

### Ranking Functions

```sql
ROW_NUMBER()   -- unique sequential number (1, 2, 3) — no ties
RANK()         -- ties get same rank, GAPS after ties (1, 1, 3)
DENSE_RANK()   -- ties get same rank, NO GAPS (1, 1, 2)
NTILE(n)       -- divide into n equal-sized buckets
```

```sql
SELECT name, salary,
    ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary DESC) AS row_num,
    RANK()       OVER (PARTITION BY dept ORDER BY salary DESC) AS rnk,
    DENSE_RANK() OVER (PARTITION BY dept ORDER BY salary DESC) AS dense_rnk
FROM employees;

-- Get TOP 3 salaries per department (keep ties → use RANK or DENSE_RANK)
SELECT * FROM (
    SELECT *, DENSE_RANK() OVER (PARTITION BY dept ORDER BY salary DESC) AS dr
    FROM employees
) t WHERE dr <= 3;
```

### Value Functions

```sql
LAG(col, offset, default)   -- value from N rows BEFORE current row
LEAD(col, offset, default)  -- value from N rows AFTER current row
FIRST_VALUE(col)            -- first value in the window
LAST_VALUE(col)             -- last value in the window
NTH_VALUE(col, n)           -- nth value in the window
```

```sql
-- Month-over-month revenue change
SELECT month, revenue,
    LAG(revenue, 1, 0) OVER (ORDER BY month) AS prev_revenue,
    revenue - LAG(revenue, 1, 0) OVER (ORDER BY month) AS change
FROM monthly_sales;
```

### Aggregate Window Functions

```sql
-- Running total (cumulative sum)
SELECT date, amount,
    SUM(amount) OVER (ORDER BY date) AS running_total
FROM transactions;

-- Running average per department
SELECT dept, salary,
    AVG(salary) OVER (PARTITION BY dept ORDER BY salary
                      ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_avg
FROM employees;
```

### Window Frame Syntax

```sql
ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW  -- cumulative
ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING           -- 5-row sliding window
ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING   -- reverse cumulative
RANGE BETWEEN INTERVAL '7' DAY PRECEDING AND CURRENT ROW  -- date-based
```

---

## 7. Subqueries vs CTEs vs JOINs

```sql
-- SUBQUERY (inline, hard to reuse)
SELECT name FROM employees
WHERE dept_id IN (SELECT id FROM departments WHERE budget > 1000000);

-- CTE — Common Table Expression (readable, reusable in same query)
WITH high_budget_depts AS (
    SELECT id FROM departments WHERE budget > 1000000
)
SELECT name FROM employees
WHERE dept_id IN (SELECT id FROM high_budget_depts);

-- CTE: ALWAYS prefer over nested subqueries for readability
-- Multiple CTEs chained together:
WITH
dept_budgets AS (SELECT dept_id, SUM(salary) AS total FROM employees GROUP BY dept_id),
dept_info AS (SELECT d.name, db.total FROM departments d JOIN dept_budgets db ON d.id = db.dept_id)
SELECT * FROM dept_info WHERE total > 1000000;

-- RECURSIVE CTE — for hierarchical data (org charts, trees)
WITH RECURSIVE subordinates AS (
    SELECT id, name, manager_id, 0 AS level
    FROM employees WHERE manager_id IS NULL   -- root (CEO)
    UNION ALL
    SELECT e.id, e.name, e.manager_id, s.level + 1
    FROM employees e
    JOIN subordinates s ON e.manager_id = s.id
)
SELECT * FROM subordinates;
```

---

## 8. Set Operations

```sql
UNION     -- combine results, remove duplicates (sorts → slower)
UNION ALL -- combine results, keep duplicates (faster — no sort)
INTERSECT -- rows in BOTH queries
EXCEPT    -- rows in first query but NOT second (MINUS in Oracle)
```

```sql
-- Find customers who bought in both Q1 and Q2
SELECT customer_id FROM orders WHERE quarter = 'Q1'
INTERSECT
SELECT customer_id FROM orders WHERE quarter = 'Q2';
```

---

## 9. Indexing Strategy

```sql
-- Create index
CREATE INDEX idx_emp_dept ON employees (dept_id);
CREATE UNIQUE INDEX idx_emp_email ON employees (email);
CREATE INDEX idx_emp_name_dept ON employees (dept_id, last_name); -- composite
```

**Index rules:**
```
✓ Index columns used in: WHERE, JOIN ON, ORDER BY, GROUP BY
✓ Composite index: leftmost prefix rule — index (A, B, C) serves queries on A, (A,B), (A,B,C)
✗ Don't index: low-cardinality columns (e.g., boolean, gender — table scan is faster)
✗ Don't over-index: every index slows INSERT/UPDATE/DELETE

Covering index: index contains ALL columns the query needs → no table lookup (fastest reads)
```

**Index use vs full table scan (EXPLAIN output):**
```sql
EXPLAIN SELECT * FROM employees WHERE dept_id = 5;
-- Look for: "type: ref" (index used) vs "type: ALL" (full scan)
-- Key metrics: rows (estimated scan), Extra (Using index, Using filesort)
```

---

## 10. NULL Handling

```sql
-- NULL is NOT a value — comparisons with NULL always return UNKNOWN
SELECT * FROM t WHERE col = NULL;    -- ✗ always returns 0 rows!
SELECT * FROM t WHERE col IS NULL;   -- ✓ correct
SELECT * FROM t WHERE col IS NOT NULL;

-- COALESCE: return first non-NULL value
SELECT COALESCE(preferred_name, first_name, 'Unknown') AS display_name FROM users;

-- NULLIF: return NULL if two values are equal (useful to avoid division by zero)
SELECT revenue / NULLIF(units_sold, 0) AS price_per_unit FROM sales;

-- Aggregates ignore NULLs (except COUNT(*))
AVG(salary)  -- ignores NULL salaries (may mislead — use COALESCE to treat NULL as 0)
```

---

## 11. Common Interview Patterns

```sql
-- ─── Pattern 1: Nth highest salary ─────────────────────────
SELECT DISTINCT salary FROM employees
ORDER BY salary DESC LIMIT 1 OFFSET N-1;   -- OFFSET is 0-indexed

-- Or with window:
SELECT salary FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS dr
    FROM employees
) t WHERE dr = N;

-- ─── Pattern 2: Duplicate detection ─────────────────────────
SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;

-- ─── Pattern 3: Running totals ───────────────────────────────
SELECT date, amount, SUM(amount) OVER (ORDER BY date) AS cumulative
FROM transactions;

-- ─── Pattern 4: Consecutive rows / gaps ─────────────────────
-- Find consecutive dates (using LAG)
SELECT date FROM (
    SELECT date,
           LAG(date) OVER (ORDER BY date) AS prev_date
    FROM logs
) t WHERE date = prev_date + INTERVAL '1 DAY';

-- ─── Pattern 5: Self-join for comparisons ────────────────────
-- Find employees who earn more than their manager
SELECT e.name, e.salary
FROM employees e
JOIN employees m ON e.manager_id = m.id
WHERE e.salary > m.salary;

-- ─── Pattern 6: Pivot (conditional aggregation) ──────────────
SELECT dept,
    SUM(CASE WHEN year = 2023 THEN revenue ELSE 0 END) AS rev_2023,
    SUM(CASE WHEN year = 2024 THEN revenue ELSE 0 END) AS rev_2024
FROM sales GROUP BY dept;

-- ─── Pattern 7: Delete duplicates, keep one ──────────────────
DELETE FROM users
WHERE id NOT IN (
    SELECT MIN(id) FROM users GROUP BY email
);
```

---

## 12. EXPLAIN / Query Plan Reading

```sql
EXPLAIN ANALYZE SELECT ...;   -- PostgreSQL: actual execution stats
EXPLAIN FORMAT=JSON SELECT ...; -- MySQL/modern DBs

-- Key things to look for:
-- Seq Scan → full table scan (no index used)
-- Index Scan → using index (good)
-- Index Only Scan → covering index (best)
-- Nested Loop → small datasets, index-driven
-- Hash Join → large datasets, equality joins
-- Merge Join → pre-sorted data
-- Sort → filesort needed (add index on ORDER BY cols to avoid)
-- rows=1000000 estimated vs actual=100 → stale statistics (run ANALYZE)
```

---

## 13. ACID & Transactions

```sql
BEGIN;                        -- start transaction
  UPDATE accounts SET balance = balance - 100 WHERE id = 1;
  UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;                       -- persist both changes atomically
-- or ROLLBACK; to undo both

-- Atomicity:  all or nothing
-- Consistency: DB moves from one valid state to another
-- Isolation:  concurrent transactions don't interfere
-- Durability: committed data survives crashes
```

---

*Files:*
- `01_easy.sql` — Basic SELECT, GROUP BY, JOINs, simple aggregations
- `02_medium.sql` — Window functions, CTEs, subqueries, self-joins
- `03_hard.sql` — Recursive CTEs, complex window frames, EXPLAIN analysis
- `04_google_level.sql` — Classic Google SQL interview scenarios
- `05_faang_staff_interview_patterns.sql` — Gaps & Islands, User Sessionization, Cohort Retention, Recursive CTE Trees, Exact Medians, Overlapping Intervals

