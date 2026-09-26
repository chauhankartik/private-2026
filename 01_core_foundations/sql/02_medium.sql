-- ============================================================
--  SQL INTERVIEW PREP — MEDIUM QUERIES
--  Window Functions, CTEs, Self-Joins, Correlated Subqueries
-- ============================================================
--
--  M1.  Employees earning above their department average
--  M2.  Running total of sales by date
--  M3.  Rank employees by salary within each department
--  M4.  Month-over-month revenue growth
--  M5.  Find employees who are also managers (self-join)
--  M6.  Employees with the same salary as someone in another dept
--  M7.  Consecutive login days (gaps & islands)
--  M8.  First and last order per customer
--  M9.  Pivot: Revenue by product per quarter
--  M10. Delete duplicate rows, keep the lowest ID
-- ============================================================

-- (Using the same schema from 01_easy.sql)

-- Additional tables for medium problems:
CREATE TABLE IF NOT EXISTS sales (
    id          INT PRIMARY KEY,
    rep_id      INT,           -- employee who made the sale
    product     VARCHAR(100),
    amount      DECIMAL(10,2),
    sale_date   DATE,
    quarter     VARCHAR(6)     -- 'Q1', 'Q2', 'Q3', 'Q4'
);

CREATE TABLE IF NOT EXISTS user_logins (
    user_id     INT,
    login_date  DATE,
    PRIMARY KEY (user_id, login_date)
);

INSERT INTO sales VALUES
(1, 10, 'Widget A', 5000,  '2024-01-05', 'Q1'),
(2, 10, 'Widget B', 8000,  '2024-01-20', 'Q1'),
(3, 10, 'Widget A', 12000, '2024-02-10', 'Q1'),
(4, 5,  'Widget C', 3000,  '2024-02-15', 'Q1'),
(5, 5,  'Widget A', 7000,  '2024-03-01', 'Q1'),
(6, 10, 'Widget B', 15000, '2024-04-10', 'Q2'),
(7, 5,  'Widget C', 9000,  '2024-04-25', 'Q2'),
(8, 10, 'Widget A', 11000, '2024-05-15', 'Q2'),
(9, 5,  'Widget B', 6000,  '2024-06-30', 'Q2');

INSERT INTO user_logins VALUES
(1, '2024-01-01'), (1, '2024-01-02'), (1, '2024-01-03'),  -- 3 consecutive
(1, '2024-01-05'), (1, '2024-01-06'),                      -- gap on 4th, then 2 more
(2, '2024-01-01'), (2, '2024-01-03'), (2, '2024-01-05');   -- every other day (no consecutive)

-- ─────────────────────────────────────────────────────────────
-- M1. Employees Earning Above Their Department Average
-- Pattern: Correlated Subquery or Window Function
-- ─────────────────────────────────────────────────────────────

-- Q: Find employees who earn more than the average salary of their department.

-- Method 1: Correlated Subquery (intuitive but runs per row — O(n × m))
SELECT e.name, e.salary, d.name AS department
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.salary > (
    SELECT AVG(e2.salary)
    FROM employees e2
    WHERE e2.dept_id = e.dept_id    -- correlated: references outer query's dept_id
);

-- Method 2: CTE with pre-computed averages (one pass — O(n))
WITH dept_avg AS (
    SELECT dept_id, AVG(salary) AS avg_salary
    FROM employees
    GROUP BY dept_id
)
SELECT e.name, e.salary, d.name AS department, da.avg_salary
FROM employees e
JOIN departments d ON e.dept_id = d.id
JOIN dept_avg da   ON e.dept_id = da.dept_id
WHERE e.salary > da.avg_salary;

-- Method 3: Window function (cleanest — no subquery, no CTE)
SELECT name, salary, department, dept_avg FROM (
    SELECT e.name, e.salary, d.name AS department,
           AVG(e.salary) OVER (PARTITION BY e.dept_id) AS dept_avg
    FROM employees e
    JOIN departments d ON e.dept_id = d.id
) t WHERE salary > dept_avg;

-- Optimization Note:
--   Method 1: N correlated subqueries — slow for large tables.
--   Method 2: 2 passes (CTE + join) — good.
--   Method 3: 1 pass — the optimizer can compute AVG while scanning employees.
--   ALWAYS prefer window function or CTE over correlated subquery for aggregates.

-- ─────────────────────────────────────────────────────────────
-- M2. Running Total of Sales by Date
-- Pattern: SUM() as Window Function with ORDER BY
-- ─────────────────────────────────────────────────────────────

-- Q: Show each sale with a running total of all sales up to that date.

SELECT sale_date, amount, product,
       SUM(amount) OVER (ORDER BY sale_date
                         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total,
       SUM(amount) OVER (PARTITION BY quarter
                         ORDER BY sale_date) AS quarterly_running_total
FROM sales
ORDER BY sale_date;

-- Note: ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW is the DEFAULT frame
--       when ORDER BY is present in the window. Can omit it for running totals.

-- Moving 3-day average (rolling window):
SELECT sale_date, amount,
       AVG(amount) OVER (ORDER BY sale_date ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)
           AS moving_3day_avg
FROM sales
ORDER BY sale_date;

-- ─────────────────────────────────────────────────────────────
-- M3. Rank Employees by Salary Within Each Department
-- Pattern: RANK / DENSE_RANK / ROW_NUMBER
-- ─────────────────────────────────────────────────────────────

-- Q: Rank all employees by salary within their department.
--    Show the top-2 earners per department.

-- Differences between ranking functions (MEMORIZE THIS):
--   Salaries: 5000, 5000, 3000
--   ROW_NUMBER:  1, 2, 3  (unique — no ties)
--   RANK:        1, 1, 3  (gaps after ties)
--   DENSE_RANK:  1, 1, 2  (no gaps)

SELECT name, salary, dept_id,
    ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS row_num,
    RANK()       OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk,
    DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS dense_rnk
FROM employees
WHERE dept_id IS NOT NULL;

-- Top 2 per department (keeping ties → use DENSE_RANK):
SELECT name, salary, department FROM (
    SELECT e.name, e.salary, d.name AS department,
           DENSE_RANK() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC) AS dr
    FROM employees e
    JOIN departments d ON e.dept_id = d.id
) t WHERE dr <= 2;

-- ─────────────────────────────────────────────────────────────
-- M4. Month-over-Month Revenue Growth (%)
-- Pattern: LAG() window function
-- ─────────────────────────────────────────────────────────────

-- Q: Calculate month-over-month revenue change and growth percentage.

WITH monthly_revenue AS (
    SELECT DATE_TRUNC('month', sale_date) AS month,   -- PostgreSQL
           SUM(amount) AS revenue
    FROM sales
    GROUP BY DATE_TRUNC('month', sale_date)
)
SELECT
    month,
    revenue,
    LAG(revenue) OVER (ORDER BY month) AS prev_revenue,
    revenue - LAG(revenue) OVER (ORDER BY month) AS change,
    ROUND(
        100.0 * (revenue - LAG(revenue) OVER (ORDER BY month))
              / NULLIF(LAG(revenue) OVER (ORDER BY month), 0),
        2
    ) AS growth_pct
FROM monthly_revenue
ORDER BY month;

-- NULLIF(prev_revenue, 0) → avoids division by zero (returns NULL instead of error)
-- ROUND(..., 2) → 2 decimal places
-- MySQL equivalent of DATE_TRUNC: DATE_FORMAT(sale_date, '%Y-%m-01')

-- ─────────────────────────────────────────────────────────────
-- M5. Employees Who Are Also Managers (Self-Join)
-- Pattern: SELF JOIN
-- ─────────────────────────────────────────────────────────────

-- Q: Find employees who manage at least one other employee.

-- Method 1: Self-join
SELECT DISTINCT m.name AS manager, m.job_title, COUNT(e.id) AS direct_reports
FROM employees m
JOIN employees e ON e.manager_id = m.id    -- e is the subordinate, m is the manager
GROUP BY m.id, m.name, m.job_title
ORDER BY direct_reports DESC;

-- Method 2: Subquery (simpler but slower)
SELECT name, job_title
FROM employees
WHERE id IN (SELECT DISTINCT manager_id FROM employees WHERE manager_id IS NOT NULL);

-- Follow-up: Find the full management chain (recursive CTE)
WITH RECURSIVE org_chart AS (
    -- Base: top-level managers (no manager)
    SELECT id, name, manager_id, 0 AS depth, CAST(name AS VARCHAR(500)) AS path
    FROM employees
    WHERE manager_id IS NULL

    UNION ALL

    -- Recursive: add subordinates
    SELECT e.id, e.name, e.manager_id, oc.depth + 1,
           CAST(oc.path || ' → ' || e.name AS VARCHAR(500))
    FROM employees e
    JOIN org_chart oc ON e.manager_id = oc.id
)
SELECT depth, name, path FROM org_chart ORDER BY path;

-- ─────────────────────────────────────────────────────────────
-- M6. Employees with Same Salary as Someone in Another Department
-- Pattern: EXISTS / IN with cross-dept condition
-- ─────────────────────────────────────────────────────────────

-- Q: Find employees who have the same salary as at least one employee
--    in a DIFFERENT department.

-- Method 1: Self-join
SELECT DISTINCT e1.name, e1.salary, e1.dept_id
FROM employees e1
JOIN employees e2 ON e1.salary = e2.salary
                 AND e1.dept_id != e2.dept_id   -- different department
                 AND e1.id != e2.id;            -- not the same person

-- Method 2: EXISTS (often faster for large tables — stops at first match)
SELECT e1.name, e1.salary, e1.dept_id
FROM employees e1
WHERE EXISTS (
    SELECT 1 FROM employees e2
    WHERE e2.salary = e1.salary
      AND e2.dept_id != e1.dept_id
      AND e2.id != e1.id
);

-- ─────────────────────────────────────────────────────────────
-- M7. Consecutive Login Days (Gaps & Islands)
-- Pattern: ROW_NUMBER() subtraction trick
-- ─────────────────────────────────────────────────────────────

-- Q: Find users who logged in for 3 or more consecutive days.

-- KEY INSIGHT (the gaps & islands technique):
--   If logins are consecutive, date - row_number() = CONSTANT.
--   Non-consecutive days produce different constants → different "islands."

WITH login_groups AS (
    SELECT user_id, login_date,
           login_date - CAST(ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date)
                             AS INT) AS grp
    FROM user_logins
),
consecutive_counts AS (
    SELECT user_id, grp,
           MIN(login_date) AS streak_start,
           MAX(login_date) AS streak_end,
           COUNT(*) AS streak_length
    FROM login_groups
    GROUP BY user_id, grp
)
SELECT user_id, streak_start, streak_end, streak_length
FROM consecutive_counts
WHERE streak_length >= 3
ORDER BY user_id, streak_start;

-- Explanation of the trick:
--   user_id=1: dates = Jan 1, 2, 3, 5, 6
--   row_nums =       1, 2, 3, 4, 5
--   date - rn =      Dec31, Dec31, Dec31, Jan1, Jan1  → groups of consecutive dates!

-- ─────────────────────────────────────────────────────────────
-- M8. First and Last Order per Customer
-- Pattern: FIRST_VALUE / LAST_VALUE or MIN/MAX with GROUP BY
-- ─────────────────────────────────────────────────────────────

-- Q: For each customer, show their first order date, last order date,
--    and total number of orders.

-- Method 1: GROUP BY with MIN/MAX (simple)
SELECT customer_id,
       MIN(order_date) AS first_order,
       MAX(order_date) AS last_order,
       COUNT(*) AS total_orders,
       SUM(amount) AS total_spent
FROM orders
GROUP BY customer_id
ORDER BY total_spent DESC;

-- Method 2: Window function (also gives the full row of first/last order)
SELECT DISTINCT customer_id,
    FIRST_VALUE(order_date) OVER (PARTITION BY customer_id ORDER BY order_date
                                  ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
        AS first_order,
    LAST_VALUE(order_date) OVER (PARTITION BY customer_id ORDER BY order_date
                                 ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
        AS last_order
FROM orders;

-- IMPORTANT: LAST_VALUE requires explicit frame UNBOUNDED FOLLOWING.
--            Default frame is CURRENT ROW → LAST_VALUE returns current row's value!

-- ─────────────────────────────────────────────────────────────
-- M9. Pivot: Revenue by Product per Quarter
-- Pattern: CASE WHEN conditional aggregation
-- ─────────────────────────────────────────────────────────────

-- Q: Create a pivot table showing total revenue per product per quarter.

SELECT product,
    SUM(CASE WHEN quarter = 'Q1' THEN amount ELSE 0 END) AS Q1_revenue,
    SUM(CASE WHEN quarter = 'Q2' THEN amount ELSE 0 END) AS Q2_revenue,
    SUM(amount) AS total_revenue
FROM sales
GROUP BY product
ORDER BY total_revenue DESC;

-- Output:
--   product  | Q1_revenue | Q2_revenue | total_revenue
--   Widget A | 24000      | 11000      | 35000
--   Widget B | 8000       | 15000      | 29000

-- Optimization Note:
--   This is called "conditional aggregation" — simulates PIVOT without pivot syntax.
--   In databases that support PIVOT (SQL Server, Oracle), you can use:
--   PIVOT (SUM(amount) FOR quarter IN ('Q1','Q2','Q3','Q4'))
--   But conditional aggregation is portable and explicit.

-- ─────────────────────────────────────────────────────────────
-- M10. Delete Duplicate Rows, Keep Lowest ID
-- Pattern: DELETE with CTE / ROW_NUMBER
-- ─────────────────────────────────────────────────────────────

-- Q: The employees table has duplicate emails. Delete all but the one with the lowest ID.

-- Method 1: Using subquery (standard SQL)
DELETE FROM employees
WHERE id NOT IN (
    SELECT MIN(id)
    FROM employees
    GROUP BY email    -- for each email, keep the row with minimum id
);

-- Method 2: Using CTE + ROW_NUMBER (PostgreSQL / SQL Server)
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY email ORDER BY id ASC) AS rn
    FROM employees
)
DELETE FROM employees
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);   -- delete all but rn=1 (lowest id)

-- Method 3: Self-join (MySQL compatible)
DELETE e1
FROM employees e1
JOIN employees e2 ON e1.email = e2.email
                 AND e1.id > e2.id;   -- delete the one with higher id

-- Optimization Note:
--   Before deleting, ALWAYS verify with a SELECT first:
--   Replace DELETE with SELECT * to preview what will be deleted.
--   Add a transaction wrapper: BEGIN; DELETE ...; -- check row count; COMMIT/ROLLBACK;

-- ─────────────────────────────────────────────────────────────
-- PHASE 2 PRACTICE QUESTIONS
-- ─────────────────────────────────────────────────────────────

-- Q1: What is the difference between ROW_NUMBER, RANK, and DENSE_RANK?
--     Give an example where you'd use each.

-- Q2: A correlated subquery in M1 runs once per row. If the employees table
--     has 1 million rows, how many times does the subquery execute?
--     What is the fix?

-- Q3: Why does LAST_VALUE() often return unexpected results, and how do you fix it?
