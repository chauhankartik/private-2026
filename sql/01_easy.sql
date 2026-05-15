-- ============================================================
--  SQL INTERVIEW PREP — EASY QUERIES
--  Schema + 10 Easy Problems + Optimization Notes
-- ============================================================
--
--  E1.  List all employees in a department
--  E2.  Count employees per department
--  E3.  Find max salary in each department
--  E4.  Employees earning above average salary
--  E5.  Second highest salary
--  E6.  Find duplicate emails
--  E7.  Employees without a department (NULL handling)
--  E8.  Top 5 highest paid employees
--  E9.  Department with the most employees
--  E10. Employees hired in the last 30 days
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- SCHEMA SETUP (run this first to create the practice tables)
-- ─────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS departments (
    id         INT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    budget     DECIMAL(15,2),
    location   VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS employees (
    id         INT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150),
    salary     DECIMAL(10,2),
    dept_id    INT REFERENCES departments(id),
    manager_id INT REFERENCES employees(id),
    hire_date  DATE,
    job_title  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS orders (
    id          INT PRIMARY KEY,
    customer_id INT,
    product_id  INT,
    amount      DECIMAL(10,2),
    order_date  DATE,
    status      VARCHAR(50)   -- 'completed', 'pending', 'cancelled'
);

-- Sample data
INSERT INTO departments VALUES
(1, 'Engineering',  5000000, 'Bangalore'),
(2, 'Marketing',    1500000, 'Mumbai'),
(3, 'Finance',      2000000, 'Delhi'),
(4, 'HR',           800000,  'Bangalore'),
(5, 'Sales',        3000000, 'Chennai');

INSERT INTO employees VALUES
(1,  'Alice Chen',    'alice@co.com',   150000, 1, NULL, '2021-03-15', 'Senior Engineer'),
(2,  'Bob Sharma',    'bob@co.com',     120000, 1, 1,    '2022-07-01', 'Engineer'),
(3,  'Carol Mehta',   'carol@co.com',   95000,  1, 1,    '2023-01-10', 'Junior Engineer'),
(4,  'David Kumar',   'david@co.com',   180000, 2, NULL, '2020-05-20', 'Marketing Director'),
(5,  'Eve Singh',     'eve@co.com',     110000, 2, 4,    '2021-11-30', 'Marketing Manager'),
(6,  'Frank Ali',     'frank@co.com',   200000, 3, NULL, '2019-08-12', 'CFO'),
(7,  'Grace Nair',    'grace@co.com',   90000,  3, 6,    '2022-04-05', 'Analyst'),
(8,  'Henry Patel',   'henry@co.com',   75000,  4, NULL, '2023-06-01', 'HR Manager'),
(9,  'Iris Das',      'alice@co.com',   85000,  NULL,NULL,'2024-03-01', 'Consultant'), -- duplicate email, no dept
(10, 'James Roy',     'james@co.com',   95000,  5, NULL, '2024-04-15', 'Sales Lead');

-- ─────────────────────────────────────────────────────────────
-- E1. List all employees in the Engineering department
-- Pattern: JOIN + WHERE
-- ─────────────────────────────────────────────────────────────

-- Q: Write a query to show all employee names and their salaries
--    who work in the 'Engineering' department.

SELECT e.name, e.salary, e.job_title
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE d.name = 'Engineering'
ORDER BY e.salary DESC;

-- Optimization Note:
--   Index on employees.dept_id → speeds up the JOIN.
--   Index on departments.name → speeds up the WHERE filter.
--   Without index: full scan of employees × full scan of departments.
--   With index: O(log n) lookup on departments, then index scan on employees.

-- Follow-up: What if you want employees with NO department?
--   Change JOIN → LEFT JOIN, add WHERE d.id IS NULL

-- ─────────────────────────────────────────────────────────────
-- E2. Count employees per department
-- Pattern: GROUP BY + aggregate
-- ─────────────────────────────────────────────────────────────

-- Q: Show department name and employee count, for departments with ≥ 2 employees.

SELECT d.name AS department, COUNT(e.id) AS employee_count
FROM departments d
LEFT JOIN employees e ON d.id = e.dept_id   -- LEFT JOIN to include departments with 0 employees
GROUP BY d.id, d.name
HAVING COUNT(e.id) >= 2
ORDER BY employee_count DESC;

-- MISTAKE TO AVOID: Using WHERE instead of HAVING for aggregate condition
-- WHERE COUNT(*) >= 2  ← COMPILE ERROR — aggregate in WHERE clause

-- Optimization Note:
--   GROUP BY d.id is enough (id is unique). d.name must be in GROUP BY or aggregate
--   in strict SQL (MySQL allows it with non-strict mode — don't rely on that).
--   Index on employees.dept_id speeds up the group aggregation significantly.

-- ─────────────────────────────────────────────────────────────
-- E3. Max salary in each department
-- Pattern: GROUP BY + MAX (with department name)
-- ─────────────────────────────────────────────────────────────

-- Q: Show the highest salary in each department.

SELECT d.name AS department, MAX(e.salary) AS max_salary
FROM employees e
JOIN departments d ON e.dept_id = d.id
GROUP BY d.id, d.name
ORDER BY max_salary DESC;

-- Follow-up: Show the EMPLOYEE who has the max salary in each dept.
-- (Cannot use WHERE MAX — it's an aggregate. Use subquery or window function.)

-- Subquery approach:
SELECT e.name, e.salary, d.name AS department
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.salary = (
    SELECT MAX(e2.salary)
    FROM employees e2
    WHERE e2.dept_id = e.dept_id   -- correlated subquery — runs once per row
);

-- Window approach (faster — one pass):
SELECT name, salary, dept_name FROM (
    SELECT e.name, e.salary, d.name AS dept_name,
           MAX(e.salary) OVER (PARTITION BY e.dept_id) AS dept_max
    FROM employees e JOIN departments d ON e.dept_id = d.id
) t WHERE salary = dept_max;

-- ─────────────────────────────────────────────────────────────
-- E4. Employees earning above company-wide average salary
-- Pattern: Subquery in WHERE
-- ─────────────────────────────────────────────────────────────

-- Q: Find all employees who earn MORE than the company's average salary.

SELECT name, salary, job_title
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees)
ORDER BY salary DESC;

-- Optimization Note:
--   The subquery (SELECT AVG(salary)) is a SCALAR SUBQUERY — evaluated ONCE.
--   The engine computes the average first, substitutes the value, then filters.
--   Much faster than a correlated subquery (which runs per row).

-- Follow-up: Employees earning above their DEPARTMENT average (harder — see medium)

-- ─────────────────────────────────────────────────────────────
-- E5. Second Highest Salary
-- Pattern: OFFSET or DENSE_RANK
-- ─────────────────────────────────────────────────────────────

-- Q: Find the second highest salary. Return NULL if it doesn't exist.

-- Method 1: OFFSET (simple, but MySQL/PostgreSQL specific)
SELECT DISTINCT salary AS second_highest
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;    -- skip the highest, take the next

-- Method 2: Subquery (portable across databases)
SELECT MAX(salary) AS second_highest
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);

-- Method 3: Window function (most flexible — generalizes to Nth highest)
SELECT salary AS second_highest FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS dr
    FROM employees
) t WHERE dr = 2
LIMIT 1;

-- Optimization Note:
--   Method 3 generalizes to ANY Nth highest — just change dr = N.
--   Method 1 is fastest on indexed salary column.
--   DENSE_RANK handles ties (same salary = same rank).

-- ─────────────────────────────────────────────────────────────
-- E6. Find Duplicate Emails
-- Pattern: GROUP BY + HAVING COUNT > 1
-- ─────────────────────────────────────────────────────────────

-- Q: Find all email addresses that appear more than once.

SELECT email, COUNT(*) AS occurrences
FROM employees
WHERE email IS NOT NULL
GROUP BY email
HAVING COUNT(*) > 1
ORDER BY occurrences DESC;

-- Follow-up: Show the actual rows with duplicate emails (not just the email)
SELECT *
FROM employees
WHERE email IN (
    SELECT email FROM employees GROUP BY email HAVING COUNT(*) > 1
);

-- Or with window function:
SELECT * FROM (
    SELECT *, COUNT(*) OVER (PARTITION BY email) AS email_count
    FROM employees
) t WHERE email_count > 1;

-- ─────────────────────────────────────────────────────────────
-- E7. Employees Without a Department (NULL Handling)
-- Pattern: LEFT JOIN + IS NULL  or  IS NULL filter
-- ─────────────────────────────────────────────────────────────

-- Q: Find all employees who are not assigned to any department.

-- Method 1: Direct NULL check
SELECT name, email FROM employees WHERE dept_id IS NULL;

-- Method 2: Anti-join (LEFT JOIN + NULL filter) — works when FK may not exist
SELECT e.name, e.email
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.id
WHERE d.id IS NULL;    -- no matching department row → not in any department

-- MISTAKE TO AVOID:
-- WHERE dept_id = NULL  ← ALWAYS returns 0 rows (NULL ≠ NULL in SQL)
-- Always use IS NULL / IS NOT NULL for NULL comparison.

-- ─────────────────────────────────────────────────────────────
-- E8. Top 5 Highest Paid Employees
-- Pattern: ORDER BY + LIMIT
-- ─────────────────────────────────────────────────────────────

-- Q: List the top 5 highest-paid employees with their department names.

SELECT e.name, e.salary, d.name AS department, e.job_title
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.id
ORDER BY e.salary DESC
LIMIT 5;

-- Optimization Note:
--   If you have an index on salary, the DB can scan it in DESC order and stop at 5.
--   Without index: sort all rows (O(n log n)), then take 5.
--   With index: O(1) seek + 5 reads = O(1) effectively.
--
--   For pagination (page 2):  LIMIT 5 OFFSET 5
--   OFFSET is slow on large datasets (scans OFFSET + LIMIT rows).
--   Better for large tables: keyset pagination using WHERE salary < :last_salary

-- ─────────────────────────────────────────────────────────────
-- E9. Department with the Most Employees
-- Pattern: GROUP BY + ORDER BY + LIMIT 1
-- ─────────────────────────────────────────────────────────────

-- Q: Which department has the most employees?

SELECT d.name AS department, COUNT(e.id) AS employee_count
FROM departments d
LEFT JOIN employees e ON d.id = e.dept_id
GROUP BY d.id, d.name
ORDER BY employee_count DESC
LIMIT 1;

-- Follow-up: What if there's a TIE for most employees?
-- LIMIT 1 drops the tied dept. Use window function to handle ties:
SELECT department, employee_count FROM (
    SELECT d.name AS department,
           COUNT(e.id) AS employee_count,
           RANK() OVER (ORDER BY COUNT(e.id) DESC) AS rnk
    FROM departments d
    LEFT JOIN employees e ON d.id = e.dept_id
    GROUP BY d.id, d.name
) t WHERE rnk = 1;

-- ─────────────────────────────────────────────────────────────
-- E10. Employees Hired in the Last 30 Days
-- Pattern: Date arithmetic
-- ─────────────────────────────────────────────────────────────

-- Q: Find all employees hired in the last 30 days. Show name and hire date.

-- PostgreSQL / standard SQL:
SELECT name, hire_date, job_title
FROM employees
WHERE hire_date >= CURRENT_DATE - INTERVAL '30 days'
ORDER BY hire_date DESC;

-- MySQL:
-- WHERE hire_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)

-- SQL Server:
-- WHERE hire_date >= DATEADD(DAY, -30, GETDATE())

-- Optimization Note:
--   NEVER wrap the column in a function:
--   WHERE YEAR(hire_date) = 2024  ← prevents index use (function on column)
--   WHERE hire_date >= '2024-01-01'  ← index CAN be used (sargable predicate)
--   "Sargable" = Search ARGument ABLE = predicate that can use an index

-- ─────────────────────────────────────────────────────────────
-- PHASE 1 PRACTICE QUESTIONS (answer before looking at solutions above)
-- ─────────────────────────────────────────────────────────────

-- Q1: When would you use HAVING instead of WHERE?
-- Q2: What is the difference between COUNT(*) and COUNT(column_name)?
-- Q3: You write: WHERE salary = NULL. Why does this return 0 rows,
--     and how do you fix it?
