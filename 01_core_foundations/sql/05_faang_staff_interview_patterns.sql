-- ============================================================================
-- FAANG & Staff-Level SQL Interview Patterns Master Suite
-- Comprehensive DDL Schema, Test Data, Problem Statements, and CTE Solutions
-- Compatible with PostgreSQL 12+, MySQL 8.0+, SQLite 3.25+, and Snowflake
-- ============================================================================

-------------------------------------------------------------------------------
-- PATTERN 1: Gaps & Islands Problem (Consecutive Active Days / Login Streaks)
-------------------------------------------------------------------------------
-- Problem Statement:
-- Given a table of user login dates, identify contiguous "islands" of consecutive
-- login days for each user. Return each user's login streaks with start date, 
-- end date, and total consecutive days, keeping only streaks of >= 3 days.

-- DDL & Test Data Setup:
DROP TABLE IF EXISTS user_logins;
CREATE TABLE user_logins (
    user_id INT,
    login_date DATE
);

INSERT INTO user_logins (user_id, login_date) VALUES
(101, '2026-01-01'),
(101, '2026-01-02'),
(101, '2026-01-03'), -- Island 1: 2026-01-01 to 2026-01-03 (3 days)
(101, '2026-01-05'),
(101, '2026-01-06'), -- Gap on Jan 4. Island 2: 2 days (Filtered out)
(101, '2026-01-08'),
(101, '2026-01-09'),
(101, '2026-01-10'),
(101, '2026-01-11'), -- Island 3: 2026-01-08 to 2026-01-11 (4 days)
(102, '2026-01-01'),
(102, '2026-01-02'); -- Island: 2 days (Filtered out)

-- SOLUTION (ROW_NUMBER Difference Method):
-- Concept: Subtracting a dense row_number (in days) from a sequential date produces
-- a CONSTANT grouping key for contiguous date islands!
WITH distinct_logins AS (
    SELECT DISTINCT user_id, login_date
    FROM user_logins
),
date_groups AS (
    SELECT 
        user_id,
        login_date,
        -- Subtracting N days from date creates a constant group identifier
        login_date - CAST(ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) AS INT) AS island_group
    FROM distinct_logins
)
SELECT 
    user_id,
    MIN(login_date) AS streak_start_date,
    MAX(login_date) AS streak_end_date,
    COUNT(*) AS consecutive_days
FROM date_groups
GROUP BY user_id, island_group
HAVING COUNT(*) >= 3
ORDER BY user_id, streak_start_date;


-------------------------------------------------------------------------------
-- PATTERN 2: User Sessionization (30-Minute Inactivity Threshold)
-------------------------------------------------------------------------------
-- Problem Statement:
-- Convert a raw web clickstream event log into distinct User Sessions. A new 
-- session is started whenever a user has been inactive for > 30 minutes. Assign
-- a unique session_id to each event.

-- DDL & Test Data Setup:
DROP TABLE IF EXISTS event_logs;
CREATE TABLE event_logs (
    event_id INT,
    user_id INT,
    event_time TIMESTAMP,
    page_url VARCHAR(100)
);

INSERT INTO event_logs (event_id, user_id, event_time, page_url) VALUES
(1, 201, '2026-01-01 10:00:00', '/home'),
(2, 201, '2026-01-01 10:15:00', '/product'),
(3, 201, '2026-01-01 10:25:00', '/cart'),    -- Session 1 (Within 30 mins)
(4, 201, '2026-01-01 11:10:00', '/checkout'),-- Session 2 (45 min gap > 30 mins)
(5, 201, '2026-01-01 11:20:00', '/success'), -- Session 2 (10 min gap)
(6, 202, '2026-01-01 12:00:00', '/home');

-- SOLUTION (Lag & Running Sum of Session Flags):
WITH timestamp_diffs AS (
    SELECT 
        event_id,
        user_id,
        event_time,
        page_url,
        LAG(event_time) OVER (PARTITION BY user_id ORDER BY event_time) AS prev_event_time
    FROM event_logs
),
session_flags AS (
    SELECT 
        event_id,
        user_id,
        event_time,
        page_url,
        CASE 
            WHEN prev_event_time IS NULL THEN 1
            -- If time difference > 30 minutes (1800 seconds), mark as new session flag 1
            WHEN EXTRACT(EPOCH FROM (event_time - prev_event_time)) > 1800 THEN 1
            ELSE 0
        END AS is_new_session
    FROM timestamp_diffs
)
SELECT 
    event_id,
    user_id,
    event_time,
    page_url,
    -- Running SUM of new session flags creates incremental Session IDs
    SUM(is_new_session) OVER (PARTITION BY user_id ORDER BY event_time) AS user_session_seq
FROM session_flags
ORDER BY user_id, event_time;


-------------------------------------------------------------------------------
-- PATTERN 3: Cohort Analysis & Month-over-Month Retention Rate
-------------------------------------------------------------------------------
-- Problem Statement:
-- Calculate monthly user cohort retention. For users who made their first 
-- purchase in Month 0 (Cohort Month), calculate what percentage returned to 
-- make purchases in Month +1 and Month +2.

-- DDL & Test Data Setup:
DROP TABLE IF EXISTS customer_orders;
CREATE TABLE customer_orders (
    order_id INT,
    customer_id INT,
    order_date DATE
);

INSERT INTO customer_orders (order_id, customer_id, order_date) VALUES
(1, 301, '2026-01-10'), -- 301 First purchase Jan 2026
(2, 301, '2026-02-15'), -- 301 Active Month 1 (Feb)
(3, 301, '2026-03-20'), -- 301 Active Month 2 (Mar)
(4, 302, '2026-01-20'), -- 302 First purchase Jan 2026
(5, 302, '2026-03-05'), -- 302 Active Month 2 (Mar, skipped Feb)
(6, 303, '2026-02-01'); -- 303 First purchase Feb 2026

-- SOLUTION (Cohort Date Truncation & Pivot):
WITH customer_first_order AS (
    SELECT 
        customer_id,
        MIN(DATE_TRUNC('month', order_date)) AS cohort_month
    FROM customer_orders
    GROUP BY customer_id
),
customer_activity AS (
    SELECT DISTINCT
        o.customer_id,
        f.cohort_month,
        -- Calculate relative month offset from initial cohort month
        (EXTRACT(YEAR FROM o.order_date) - EXTRACT(YEAR FROM f.cohort_month)) * 12 +
        (EXTRACT(MONTH FROM o.order_date) - EXTRACT(MONTH FROM f.cohort_month)) AS month_number
    FROM customer_orders o
    JOIN customer_first_order f ON o.customer_id = f.customer_id
),
cohort_sizes AS (
    SELECT 
        cohort_month,
        COUNT(DISTINCT customer_id) AS total_cohort_size
    FROM customer_first_order
    GROUP BY cohort_month
)
SELECT 
    TO_CHAR(c.cohort_month, 'YYYY-MM') AS cohort,
    cs.total_cohort_size,
    ROUND(100.0 * COUNT(DISTINCT CASE WHEN c.month_number = 0 THEN c.customer_id END) / cs.total_cohort_size, 2) AS month_0_retention_pct,
    ROUND(100.0 * COUNT(DISTINCT CASE WHEN c.month_number = 1 THEN c.customer_id END) / cs.total_cohort_size, 2) AS month_1_retention_pct,
    ROUND(100.0 * COUNT(DISTINCT CASE WHEN c.month_number = 2 THEN c.customer_id END) / cs.total_cohort_size, 2) AS month_2_retention_pct
FROM customer_activity c
JOIN cohort_sizes cs ON c.cohort_month = cs.cohort_month
GROUP BY c.cohort_month, cs.total_cohort_size
ORDER BY c.cohort_month;


-------------------------------------------------------------------------------
-- PATTERN 4: Recursive CTE Org Chart Hierarchy & Tree Depth
-------------------------------------------------------------------------------
-- Problem Statement:
-- Given an employee table with manager relationships, write a recursive query to
-- output every employee's reporting hierarchy path, organizational depth level, 
-- and top-level VP/CEO manager.

-- DDL & Test Data Setup:
DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
    employee_id INT,
    name VARCHAR(50),
    manager_id INT
);

INSERT INTO employees (employee_id, name, manager_id) VALUES
(1, 'CEO Alice', NULL),
(2, 'VP Bob', 1),
(3, 'Director Charlie', 2),
(4, 'Dev Dan', 3),
(5, 'Dev Eve', 3);

-- SOLUTION (Recursive CTE):
WITH RECURSIVE org_hierarchy AS (
    -- Anchor Member: Top-level executives (manager_id IS NULL)
    SELECT 
        employee_id,
        name,
        manager_id,
        1 AS depth_level,
        CAST(name AS VARCHAR(255)) AS hierarchy_path
    FROM employees
    WHERE manager_id IS NULL

    UNION ALL

    -- Recursive Member: Join employees to their managers in the CTE
    SELECT 
        e.employee_id,
        e.name,
        e.manager_id,
        h.depth_level + 1 AS depth_level,
        CAST(h.hierarchy_path || ' -> ' || e.name AS VARCHAR(255)) AS hierarchy_path
    FROM employees e
    JOIN org_hierarchy h ON e.manager_id = h.employee_id
)
SELECT 
    employee_id,
    name,
    manager_id,
    depth_level,
    hierarchy_path
FROM org_hierarchy
ORDER BY depth_level, employee_id;


-------------------------------------------------------------------------------
-- PATTERN 5: Exact Median Calculation Without Built-in Functions
-------------------------------------------------------------------------------
-- Problem Statement:
-- Calculate the exact median salary per department without using vendor-specific
-- aggregate functions (e.g., PERCENTILE_CONT). Works for both odd and even counts.

-- DDL & Test Data Setup:
DROP TABLE IF EXISTS department_salaries;
CREATE TABLE department_salaries (
    emp_id INT,
    department VARCHAR(50),
    salary DECIMAL(10,2)
);

INSERT INTO department_salaries (emp_id, department, salary) VALUES
(1, 'Engineering', 100000.00),
(2, 'Engineering', 120000.00),
(3, 'Engineering', 140000.00), -- Median for Eng (Odd 3 elements) = 120,000
(4, 'Marketing', 60000.00),
(5, 'Marketing', 80000.00),
(6, 'Marketing', 90000.00),
(7, 'Marketing', 110000.00);   -- Median for Mkt (Even 4 elements) = (80000+90000)/2 = 85,000

-- SOLUTION (Row Number Math Over Total Count Parity):
WITH ranked_salaries AS (
    SELECT 
        department,
        salary,
        ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary ASC) AS row_num,
        COUNT(*) OVER (PARTITION BY department) AS total_count
    FROM department_salaries
)
SELECT 
    department,
    -- Average middle value(s) to handle both odd and even element counts correctly
    ROUND(AVG(salary), 2) AS exact_median_salary
FROM ranked_salaries
WHERE row_num IN (
    (total_count + 1) / 2,   -- Handles Odd total counts (e.g., 3 -> row 2)
    (total_count + 2) / 2    -- Handles Even total counts (e.g., 4 -> rows 2 & 3)
)
GROUP BY department
ORDER BY department;


-------------------------------------------------------------------------------
-- PATTERN 6: Overlapping Intervals & Double-Booking Detection
-------------------------------------------------------------------------------
-- Problem Statement:
-- Detect overlapping hotel room bookings. Identify reservation IDs that conflict
-- with another existing reservation for the same room.

-- DDL & Test Data Setup:
DROP TABLE IF EXISTS room_bookings;
CREATE TABLE room_bookings (
    booking_id INT,
    room_number INT,
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

INSERT INTO room_bookings (booking_id, room_number, start_time, end_time) VALUES
(1, 101, '2026-01-01 14:00:00', '2026-01-03 11:00:00'),
(2, 101, '2026-01-02 12:00:00', '2026-01-04 10:00:00'), -- Overlaps with 1!
(3, 101, '2026-01-05 14:00:00', '2026-01-07 11:00:00'), -- Valid
(4, 102, '2026-01-01 14:00:00', '2026-01-03 11:00:00');

-- SOLUTION (Self-Join Overlap Condition: StartA < EndB AND StartB < EndA):
SELECT DISTINCT
    b1.booking_id AS booking_id_a,
    b2.booking_id AS booking_id_b,
    b1.room_number,
    b1.start_time AS start_a,
    b1.end_time AS end_a,
    b2.start_time AS start_b,
    b2.end_time AS end_b
FROM room_bookings b1
JOIN room_bookings b2 
  ON b1.room_number = b2.room_number
 AND b1.booking_id < b2.booking_id -- Avoid self-matching & mirror duplicates
 AND b1.start_time < b2.end_time
 AND b2.start_time < b1.end_time
ORDER BY b1.room_number, b1.booking_id;
