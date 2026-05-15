-- ============================================================
--  SQL INTERVIEW PREP — HARD QUERIES
--  Complex window frames, recursive CTEs, query optimization,
--  EXPLAIN PLAN analysis, and advanced patterns.
-- ============================================================
--
--  H1.  Median salary (no built-in MEDIAN in most DBs)
--  H2.  Longest streak of profitable days
--  H3.  Employee travel problem (overlapping date ranges)
--  H4.  Sessionization (group events into sessions)
--  H5.  Tournament bracket results (recursive CTE)
--  H6.  Query plan analysis & rewriting for performance
--  H7.  Funnel analysis (multi-step conversion)
-- ============================================================

-- Additional schema for hard problems:
CREATE TABLE IF NOT EXISTS events (
    user_id     INT,
    event_type  VARCHAR(50),    -- 'page_view', 'add_to_cart', 'purchase'
    event_ts    TIMESTAMP,
    session_id  INT             -- NULL initially — we'll compute this
);

CREATE TABLE IF NOT EXISTS trips (
    emp_id      INT,
    city        VARCHAR(100),
    start_date  DATE,
    end_date    DATE
);

INSERT INTO events VALUES
(1, 'page_view',   '2024-01-01 10:00:00', NULL),
(1, 'page_view',   '2024-01-01 10:05:00', NULL),
(1, 'add_to_cart', '2024-01-01 10:08:00', NULL),
(1, 'page_view',   '2024-01-01 11:40:00', NULL),   -- >30 min gap → new session
(1, 'purchase',    '2024-01-01 11:45:00', NULL),
(2, 'page_view',   '2024-01-01 09:00:00', NULL),
(2, 'purchase',    '2024-01-01 09:10:00', NULL);

INSERT INTO trips VALUES
(1, 'Bangalore', '2024-01-05', '2024-01-10'),
(1, 'Mumbai',    '2024-01-08', '2024-01-15'),  -- overlaps with above
(1, 'Delhi',     '2024-01-20', '2024-01-25'),
(2, 'Chennai',   '2024-01-01', '2024-01-03'),
(2, 'Chennai',   '2024-01-03', '2024-01-06');  -- back-to-back (contiguous)

-- ─────────────────────────────────────────────────────────────
-- H1. Median Salary (Without Built-in MEDIAN)
-- Pattern: PERCENTILE_CONT or ROW_NUMBER trick
-- ─────────────────────────────────────────────────────────────

-- Q: Calculate the median salary of all employees.

-- Method 1: PERCENTILE_CONT (PostgreSQL, SQL Server 2012+)
SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) AS median_salary
FROM employees;

-- Method 2: ROW_NUMBER trick (portable — works in MySQL 8+, PostgreSQL, etc.)
-- For even count: average of two middle values. For odd count: single middle value.
WITH ranked AS (
    SELECT salary,
           ROW_NUMBER() OVER (ORDER BY salary ASC)  AS rn_asc,
           ROW_NUMBER() OVER (ORDER BY salary DESC) AS rn_desc,
           COUNT(*) OVER () AS total
    FROM employees
    WHERE salary IS NOT NULL
)
SELECT AVG(salary) AS median_salary
FROM ranked
WHERE rn_asc  IN (FLOOR((total + 1) / 2.0), CEIL((total + 1) / 2.0));
-- For odd total (5): both point to row 3 → AVG(row3) = row3
-- For even total (4): point to rows 2 and 3 → AVG of middle two

-- Method 3: MySQL-specific (before window functions)
SELECT AVG(salary) AS median_salary FROM (
    SELECT salary,
           ROW_NUMBER() OVER (ORDER BY salary) AS rn_asc,
           ROW_NUMBER() OVER (ORDER BY salary DESC) AS rn_desc
    FROM employees
) t WHERE rn_asc >= rn_desc - 1 AND rn_asc <= rn_desc + 1;

-- Optimization Note:
--   PERCENTILE_CONT uses a specialized algorithm (typically quickselect O(n)).
--   ROW_NUMBER approach sorts twice → O(n log n). Acceptable but costlier.
--   For very large tables: approximate median via NTILE(100) → percentile buckets.

-- ─────────────────────────────────────────────────────────────
-- H2. Longest Streak of Consecutive Days with Positive Sales
-- Pattern: Gaps & Islands (advanced — numeric subtraction)
-- ─────────────────────────────────────────────────────────────

-- Q: Find the longest streak of consecutive days where total daily sales > 0.

WITH daily_sales AS (
    SELECT sale_date, SUM(amount) AS daily_total
    FROM sales
    GROUP BY sale_date
    HAVING SUM(amount) > 0
),
ranked AS (
    SELECT sale_date,
           ROW_NUMBER() OVER (ORDER BY sale_date) AS rn
    FROM daily_sales
),
groups AS (
    SELECT sale_date,
           sale_date - CAST(rn AS INT) AS grp    -- constant within consecutive dates
    FROM ranked
),
streaks AS (
    SELECT grp, MIN(sale_date) AS start_date, MAX(sale_date) AS end_date,
           COUNT(*) AS streak_length
    FROM groups
    GROUP BY grp
)
SELECT start_date, end_date, streak_length
FROM streaks
ORDER BY streak_length DESC
LIMIT 1;

-- EXPLAIN PLAN for this query (logical order):
-- 1. Scan sales table, GROUP BY date, HAVING filter → daily_sales CTE
-- 2. Window function ROW_NUMBER over daily_sales → ranked CTE
-- 3. Subtract rn from date → groups CTE (O(n) scan)
-- 4. GROUP BY grp → streaks CTE (group formation)
-- 5. Sort by streak_length DESC, LIMIT 1 → final result

-- ─────────────────────────────────────────────────────────────
-- H3. Employee Travel: Merge Overlapping Date Ranges
-- Pattern: Islands problem with date ranges
-- ─────────────────────────────────────────────────────────────

-- Q: For each employee, merge overlapping/adjacent trip date ranges
--    into single consolidated ranges. Show total travel days.

-- APPROACH: For each trip, find if its start_date overlaps with a previous range.
-- A new "island" starts when start_date > MAX(end_date seen so far).

WITH ordered_trips AS (
    SELECT emp_id, start_date, end_date,
           -- Is this trip's start AFTER the max end of all previous trips for this emp?
           MAX(end_date) OVER (PARTITION BY emp_id
                               ORDER BY start_date
                               ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) AS max_prev_end
    FROM trips
),
island_markers AS (
    SELECT emp_id, start_date, end_date,
           -- New island if start > max_prev_end (or first trip for this emp)
           CASE WHEN start_date > max_prev_end OR max_prev_end IS NULL THEN 1 ELSE 0 END AS is_new_island
    FROM ordered_trips
),
islands AS (
    SELECT emp_id, start_date, end_date,
           SUM(is_new_island) OVER (PARTITION BY emp_id ORDER BY start_date) AS island_id
    FROM island_markers
)
SELECT emp_id,
       MIN(start_date) AS merged_start,
       MAX(end_date) AS merged_end,
       MAX(end_date) - MIN(start_date) + 1 AS travel_days
FROM islands
GROUP BY emp_id, island_id
ORDER BY emp_id, merged_start;

-- ─────────────────────────────────────────────────────────────
-- H4. Sessionization — Group Events into Sessions
-- Pattern: LAG + cumulative SUM (session boundary detection)
-- ─────────────────────────────────────────────────────────────

-- Q: Group user events into sessions where a new session starts
--    if the gap since the last event > 30 minutes.

WITH event_gaps AS (
    SELECT user_id, event_type, event_ts,
           LAG(event_ts) OVER (PARTITION BY user_id ORDER BY event_ts) AS prev_ts
    FROM events
),
session_boundaries AS (
    SELECT user_id, event_type, event_ts,
           -- New session if: first event for user OR gap > 30 minutes
           CASE WHEN prev_ts IS NULL
                  OR event_ts - prev_ts > INTERVAL '30 minutes'
                THEN 1 ELSE 0 END AS is_new_session
    FROM event_gaps
),
sessions_numbered AS (
    SELECT user_id, event_type, event_ts,
           SUM(is_new_session) OVER (PARTITION BY user_id ORDER BY event_ts) AS session_num
    FROM session_boundaries
)
SELECT user_id, session_num,
       MIN(event_ts) AS session_start,
       MAX(event_ts) AS session_end,
       COUNT(*) AS event_count,
       -- Was there a purchase in this session?
       MAX(CASE WHEN event_type = 'purchase' THEN 1 ELSE 0 END) AS had_purchase
FROM sessions_numbered
GROUP BY user_id, session_num
ORDER BY user_id, session_num;

-- EXPLAIN PLAN Challenge (what to answer in an interview):
-- Step 1 (FROM + window): Scan events, compute LAG per user partition → event_gaps
-- Step 2 (CASE WHEN):     Mark session boundaries → session_boundaries
-- Step 3 (SUM window):    Cumulative sum of boundaries = session number → sessions_numbered
-- Step 4 (GROUP BY):      Aggregate each session → final result
-- Index recommendation: (user_id, event_ts) composite index → covers PARTITION BY + ORDER BY

-- ─────────────────────────────────────────────────────────────
-- H5. Funnel Analysis — Multi-Step Conversion
-- Pattern: Counting users at each funnel stage
-- ─────────────────────────────────────────────────────────────

-- Q: Calculate the conversion funnel:
--    page_view → add_to_cart → purchase
--    Show users at each step and drop-off rates.

WITH funnel AS (
    SELECT
        COUNT(DISTINCT CASE WHEN event_type = 'page_view'   THEN user_id END) AS step1_view,
        COUNT(DISTINCT CASE WHEN event_type = 'add_to_cart' THEN user_id END) AS step2_cart,
        COUNT(DISTINCT CASE WHEN event_type = 'purchase'    THEN user_id END) AS step3_purchase
    FROM events
)
SELECT
    step1_view,
    step2_cart,
    step3_purchase,
    ROUND(100.0 * step2_cart    / NULLIF(step1_view, 0), 1) AS view_to_cart_pct,
    ROUND(100.0 * step3_purchase / NULLIF(step2_cart, 0), 1) AS cart_to_purchase_pct,
    ROUND(100.0 * step3_purchase / NULLIF(step1_view, 0), 1) AS overall_conversion_pct
FROM funnel;

-- STRICT funnel (users who completed ALL prior steps):
-- A user who purchased without adding to cart is an anomaly → exclude from step3
WITH user_steps AS (
    SELECT user_id,
           MAX(CASE WHEN event_type = 'page_view'   THEN 1 ELSE 0 END) AS did_view,
           MAX(CASE WHEN event_type = 'add_to_cart' THEN 1 ELSE 0 END) AS did_cart,
           MAX(CASE WHEN event_type = 'purchase'    THEN 1 ELSE 0 END) AS did_purchase
    FROM events
    GROUP BY user_id
)
SELECT
    SUM(did_view) AS reached_step1,
    SUM(did_view * did_cart) AS reached_step2,
    SUM(did_view * did_cart * did_purchase) AS reached_step3
FROM user_steps;

-- ─────────────────────────────────────────────────────────────
-- H6. Query Plan Analysis & Rewriting
-- Pattern: EXPLAIN PLAN interpretation + rewrites
-- ─────────────────────────────────────────────────────────────

-- Q: Analyze this slow query and rewrite it to be faster.

-- SLOW QUERY (anti-patterns inside):
-- SELECT e.name, d.name, AVG(e.salary)
-- FROM employees e, departments d
-- WHERE e.dept_id = d.id
--   AND YEAR(e.hire_date) = 2023          ← function on column = not sargable
--   AND e.salary > (SELECT AVG(salary) FROM employees)  ← scalar subquery (ok)
--   AND e.dept_id IN (SELECT id FROM departments        ← redundant IN (already joined)
--                     WHERE budget > 1000000)
-- GROUP BY e.dept_id, d.name;

-- PROBLEMS:
--   1. YEAR(hire_date) = 2023 → function on column → index on hire_date is NOT used.
--   2. Old implicit join syntax (comma-separated) → use explicit JOIN.
--   3. IN (SELECT ...) with the same departments table already joined → redundant.

-- REWRITTEN (optimized):
WITH dept_salary_avg AS (
    SELECT AVG(salary) AS company_avg FROM employees
)
SELECT e.name, d.name AS department, e.salary
FROM employees e
JOIN departments d ON e.dept_id = d.id
CROSS JOIN dept_salary_avg dsa        -- compute company avg ONCE
WHERE e.hire_date >= '2023-01-01'     -- sargable: no function on column
  AND e.hire_date <  '2024-01-01'     -- range scan → index usable
  AND e.salary > dsa.company_avg
  AND d.budget > 1000000;             -- filter in JOIN instead of IN subquery

-- EXPLAIN plan terms to know:
-- Seq Scan     → full table scan (bad for large tables without filter)
-- Index Scan   → index used for lookup
-- Index Only Scan → fastest (all needed columns in index → no table access)
-- Nested Loop  → for small datasets, uses index on inner table
-- Hash Join    → for large datasets, build hash table on smaller side
-- Merge Join   → when both sides pre-sorted (efficient for ORDER BY queries)
-- Hash Agg     → GROUP BY using in-memory hash table
-- Sort         → filesort needed (add index to avoid)
-- rows=X       → estimated rows; large overestimate → stale stats (run ANALYZE)

-- HOW TO USE EXPLAIN (PostgreSQL):
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT e.name, d.name
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.salary > 100000;

-- ─────────────────────────────────────────────────────────────
-- H7. Advanced Window Frame: 7-Day Rolling Average
-- Pattern: RANGE BETWEEN (date-based frame)
-- ─────────────────────────────────────────────────────────────

-- Q: Compute a 7-day rolling average of daily sales,
--    including only dates that actually have data (sparse dates).

-- ROWS frame (may include wrong dates if data is sparse):
SELECT sale_date, SUM(amount) AS daily,
    AVG(SUM(amount)) OVER (ORDER BY sale_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW)
        AS rolling_7day_rows
FROM sales
GROUP BY sale_date
ORDER BY sale_date;

-- RANGE frame (date-based — includes all dates within the date range, not just N rows):
SELECT sale_date, SUM(amount) AS daily,
    AVG(SUM(amount)) OVER (ORDER BY sale_date
                           RANGE BETWEEN INTERVAL '6 days' PRECEDING AND CURRENT ROW)
        AS rolling_7day_range
FROM sales
GROUP BY sale_date
ORDER BY sale_date;

-- DIFFERENCE:
-- ROWS BETWEEN 6 PRECEDING: always exactly 7 rows (even if dates are not consecutive)
-- RANGE BETWEEN 6 DAYS: includes all rows whose date is within the past 7 calendar days
-- For sparse timeseries data: RANGE is semantically correct; ROWS may span many more days.

-- ─────────────────────────────────────────────────────────────
-- PHASE 3: THE EXPLAIN PLAN CHALLENGE
-- For each query, describe the logical execution order and
-- the expected query plan (index usage, join type, sort needed).
-- ─────────────────────────────────────────────────────────────

-- Challenge 1:
-- Describe the execution plan for the sessionization query (H4).
-- Specifically:
--   a. What index would make it fastest?
--   b. What join type is used between CTEs?
--   c. Where would a SORT operation appear?
--   d. Which CTE would the optimizer most likely materialize (save to temp)?

-- Challenge 2:
-- You have a table with 10 million rows. Explain why this query is slow:
--   SELECT * FROM orders WHERE UPPER(status) = 'COMPLETED';
-- And give TWO ways to fix it.

-- Challenge 3:
-- A query uses RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC).
-- The employees table has 1M rows and 500 departments.
-- Estimate the memory usage and explain why PARTITION BY helps.
