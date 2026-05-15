-- ============================================================
--  SQL INTERVIEW PREP — GOOGLE-LEVEL SCENARIOS
--  Classic Google SQL interview problems from real interviews.
-- ============================================================
--
--  G1.  Consecutive Available Seats (LC 603)
--  G2.  Trips and Users (LC 262) ★ Google Classic
--  G3.  Human Traffic of Stadium (LC 601) ★ Google
--  G4.  Department Top Three Salaries (LC 185)
--  G5.  Friend Requests Acceptance Rate (LC 597)
--  G6.  Active Businesses (LC 1454) ★ Google
--  G7.  Report Contiguous Dates (LC 1225) ★ Google Hard
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- G1. Consecutive Available Seats  LC 603
-- ─────────────────────────────────────────────────────────────

-- WHY GOOGLE ASKS IT:
--   Tests self-join and understanding of consecutive row detection.
--   Common in reservation systems (theaters, flights).

-- Schema: Cinema(seat_id INT PK, free BOOLEAN)
-- Q: Find all seat IDs that have at least two consecutive available seats.

CREATE TABLE IF NOT EXISTS Cinema (seat_id INT PRIMARY KEY, free INT);
INSERT INTO Cinema VALUES (1,1),(2,0),(3,1),(4,1),(5,1);

-- Method 1: Self-join on consecutive seat_id
SELECT DISTINCT c1.seat_id
FROM Cinema c1
JOIN Cinema c2 ON ABS(c1.seat_id - c2.seat_id) = 1   -- seats are adjacent
WHERE c1.free = 1 AND c2.free = 1
ORDER BY c1.seat_id;

-- Expected: 3, 4, 5 (seats 3-4 adjacent, 4-5 adjacent)

-- Optimization Note:
--   With index on seat_id (already PK), the self-join is O(n log n).
--   Alternatively use LAG/LEAD:
SELECT seat_id FROM (
    SELECT seat_id,
           free,
           LAG(free) OVER (ORDER BY seat_id) AS prev_free,
           LEAD(free) OVER (ORDER BY seat_id) AS next_free
    FROM Cinema
) t WHERE free = 1 AND (prev_free = 1 OR next_free = 1);

-- ─────────────────────────────────────────────────────────────
-- G2. Trips and Users  LC 262  ★ Google Classic
-- ─────────────────────────────────────────────────────────────

-- WHY GOOGLE ASKS IT:
--   Multi-table join with filtered aggregation.
--   Real-world scenario from Uber/Ola internal analytics.
--   Tests ability to JOIN + filter on related table conditions.

-- Schema: Trips(id, client_id, driver_id, city_id, status, request_at DATE)
--         Users(users_id, banned, role) — role: 'client' or 'driver'

-- Q: Find the cancellation rate of requests made between 2013-10-01 and 2013-10-03,
--    for NON-BANNED users. Round to 2 decimal places.
--    Cancellation = status is 'cancelled_by_driver' or 'cancelled_by_client'.

CREATE TABLE IF NOT EXISTS Trips (
    id INT PRIMARY KEY, client_id INT, driver_id INT, status VARCHAR(50), request_at DATE
);
CREATE TABLE IF NOT EXISTS Users (users_id INT PRIMARY KEY, banned VARCHAR(5), role VARCHAR(10));

INSERT INTO Users VALUES (1,'No','client'),(2,'Yes','client'),(3,'No','client'),
                          (4,'No','driver'),(10,'No','driver'),(11,'No','driver');
INSERT INTO Trips VALUES
(1,1,10,'completed','2013-10-01'),
(2,2,11,'cancelled_by_driver','2013-10-01'),  -- client 2 is BANNED
(3,3,12,'completed','2013-10-01'),
(4,3,13,'cancelled_by_client','2013-10-02'),
(5,1,10,'completed','2013-10-02'),
(6,2,11,'completed','2013-10-02'),            -- client 2 is BANNED
(7,3,10,'cancelled_by_driver','2013-10-03');

SELECT
    t.request_at AS Day,
    ROUND(
        SUM(CASE WHEN t.status != 'completed' THEN 1.0 ELSE 0 END) / COUNT(*),
        2
    ) AS "Cancellation Rate"
FROM Trips t
JOIN Users c ON t.client_id = c.users_id AND c.banned = 'No'   -- non-banned client
JOIN Users d ON t.driver_id = d.users_id AND d.banned = 'No'   -- non-banned driver
WHERE t.request_at BETWEEN '2013-10-01' AND '2013-10-03'
GROUP BY t.request_at
ORDER BY t.request_at;

-- EXPLAIN PLAN (what the interviewer expects you to describe):
-- 1. Filter Trips by date range (index on request_at ideal)
-- 2. Hash join with Users on client_id + banned='No'
-- 3. Hash join result with Users again on driver_id + banned='No'
-- 4. GROUP BY request_at → aggregate
-- 5. ROUND and ORDER

-- ─────────────────────────────────────────────────────────────
-- G3. Human Traffic of Stadium  LC 601  ★ Google
-- ─────────────────────────────────────────────────────────────

-- WHY GOOGLE ASKS IT:
--   Complex consecutive row pattern — requires identifying groups of 3+ consecutive
--   IDs all with people >= 100. Tests SQL creativity and window function mastery.

-- Schema: Stadium(id INT, visit_date DATE, people INT)
-- Q: Find all records where 3 or more consecutive stadium IDs have people >= 100.

CREATE TABLE IF NOT EXISTS Stadium (id INT PRIMARY KEY, visit_date DATE, people INT);
INSERT INTO Stadium VALUES
(1,'2017-01-01',10),(2,'2017-01-02',109),(3,'2017-01-03',150),
(4,'2017-01-04',99),(5,'2017-01-05',145),(6,'2017-01-06',1455),
(7,'2017-01-07',199),(8,'2017-01-09',188);

-- Strategy: use the island detection (id - row_number over filtered rows = constant)
WITH high_traffic AS (
    SELECT id, visit_date, people,
           id - ROW_NUMBER() OVER (ORDER BY id) AS grp  -- constant within consecutive ids
    FROM Stadium
    WHERE people >= 100
),
islands AS (
    SELECT grp, COUNT(*) AS island_size
    FROM high_traffic
    GROUP BY grp
    HAVING COUNT(*) >= 3   -- only islands with 3 or more consecutive rows
)
SELECT h.id, h.visit_date, h.people
FROM high_traffic h
JOIN islands i ON h.grp = i.grp
ORDER BY h.id;

-- Expected: ids 5, 6, 7 (all have people >= 100 and form a consecutive group of 3+)

-- Alternative approach using self-join (more verbose but shows the pattern clearly):
SELECT DISTINCT s1.*
FROM Stadium s1, Stadium s2, Stadium s3
WHERE s1.people >= 100 AND s2.people >= 100 AND s3.people >= 100
  AND (
    (s1.id = s2.id - 1 AND s1.id = s3.id - 2)   -- s1 s2 s3
    OR (s2.id = s1.id - 1 AND s2.id = s3.id - 2) -- s2 s1 s3
    OR (s3.id = s1.id - 1 AND s3.id = s2.id - 2) -- s3 s1 s2
  )
ORDER BY s1.id;

-- ─────────────────────────────────────────────────────────────
-- G4. Department Top Three Salaries  LC 185
-- ─────────────────────────────────────────────────────────────

-- WHY GOOGLE ASKS IT:
--   Classic "top N per group" — fundamental window function mastery.
--   The trick is DENSE_RANK (not RANK or ROW_NUMBER) to include all ties at rank 3.

-- Q: Find employees who are in the top 3 salary positions within their department.
--    Include ALL employees tied at the 3rd highest salary.

-- Reusing employees + departments from 01_easy.sql:
SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary
FROM (
    SELECT name, salary, dept_id,
           DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS salary_rank
    FROM employees
    WHERE dept_id IS NOT NULL
) e
JOIN departments d ON e.dept_id = d.id
WHERE e.salary_rank <= 3
ORDER BY d.name, e.salary DESC;

-- WHY DENSE_RANK and NOT RANK or ROW_NUMBER?
--   RANK:       salaries 200K, 150K, 150K, 120K → ranks 1, 2, 2, 4 → 120K is rank 4, excluded!
--   DENSE_RANK: same data → ranks 1, 2, 2, 3 → 120K is rank 3, INCLUDED correctly.
--   ROW_NUMBER: gives unique ranks 1,2,3,4 → may exclude a tied employee at rank 3.

-- ─────────────────────────────────────────────────────────────
-- G5. Active Businesses  LC 1454  ★ Google
-- ─────────────────────────────────────────────────────────────

-- WHY GOOGLE ASKS IT:
--   Requires computing a per-event-type average, then joining back to identify
--   businesses above average in MORE THAN ONE event type.
--   Tests CTEs, conditional logic, and aggregation chaining.

-- Schema: Events(business_id, event_type, occurrences)
-- Q: Find businesses that have MORE THAN ONE event type where their occurrences
--    are strictly greater than the AVERAGE occurrences for that event type.

CREATE TABLE IF NOT EXISTS Events (
    business_id INT, event_type VARCHAR(50), occurrences INT,
    PRIMARY KEY (business_id, event_type)
);
INSERT INTO Events VALUES
(1,'reviews',7),(3,'reviews',3),(1,'ads',11),(2,'ads',7),(3,'ads',6),
(1,'page views',3),(2,'page views',12);

WITH event_averages AS (
    SELECT event_type, AVG(occurrences) AS avg_occurrences
    FROM Events
    GROUP BY event_type
)
SELECT e.business_id
FROM Events e
JOIN event_averages ea ON e.event_type = ea.event_type
WHERE e.occurrences > ea.avg_occurrences
GROUP BY e.business_id
HAVING COUNT(*) > 1;    -- more than ONE event type above average

-- Expected: business_id = 1 (above average in 'reviews' and 'ads')

-- ─────────────────────────────────────────────────────────────
-- G6. Friend Requests Acceptance Rate  LC 597
-- ─────────────────────────────────────────────────────────────

-- WHY GOOGLE ASKS IT:
--   Social graph analysis — real-world Meta/Google data problem.
--   Tests handling of duplicates in both tables and NULL edge cases.

-- Schema: FriendRequest(sender_id, send_to_id, request_date)
--         RequestAccepted(requester_id, accepter_id, accept_date)
-- Q: Find the overall acceptance rate = accepted requests / sent requests.
--    Count unique pairs, not duplicate requests.

CREATE TABLE IF NOT EXISTS FriendRequest (
    sender_id INT, send_to_id INT, request_date DATE
);
CREATE TABLE IF NOT EXISTS RequestAccepted (
    requester_id INT, accepter_id INT, accept_date DATE
);
INSERT INTO FriendRequest VALUES (1,2,'2016-06-01'),(1,3,'2016-06-01'),(1,2,'2016-06-02'),
                                   (3,4,'2016-06-09');
INSERT INTO RequestAccepted VALUES (1,2,'2016-06-03'),(1,3,'2016-06-08'),(2,3,'2016-06-08'),
                                    (3,4,'2016-06-09'),(3,4,'2016-06-10');

SELECT ROUND(
    IFNULL(
        COUNT(DISTINCT requester_id, accepter_id) * 1.0 /
        COUNT(DISTINCT sender_id,    send_to_id),
        0   -- if no requests at all → rate = 0
    ), 2
) AS accept_rate
FROM RequestAccepted, FriendRequest;
-- Note: cross join is intentional here — we separately count from each table
-- Better written as:

SELECT ROUND(
    (SELECT COUNT(DISTINCT requester_id, accepter_id) FROM RequestAccepted) * 1.0
    / NULLIF((SELECT COUNT(DISTINCT sender_id, send_to_id) FROM FriendRequest), 0),
    2
) AS accept_rate;

-- ─────────────────────────────────────────────────────────────
-- G7. Report Contiguous Dates  LC 1225  ★ Google Hard
-- ─────────────────────────────────────────────────────────────

-- WHY GOOGLE ASKS IT:
--   Multi-table UNION ALL with consecutive date grouping.
--   Tests ability to combine gaps-and-islands pattern across two data sources.

-- Schema: Failed(fail_date DATE), Succeeded(success_date DATE)
-- Q: Report each contiguous period of tasks. For each period, include:
--    period_state ('failed' or 'succeeded'), start_date, end_date.
--    Only include dates in year 2019. Order by start_date.

CREATE TABLE IF NOT EXISTS Failed (fail_date DATE);
CREATE TABLE IF NOT EXISTS Succeeded (success_date DATE);
INSERT INTO Failed VALUES ('2019-01-01'),('2019-01-02'),('2019-01-04'),('2019-01-05');
INSERT INTO Succeeded VALUES ('2019-01-03'),('2019-01-06'),('2019-01-07');

WITH all_days AS (
    SELECT 'failed'    AS period_state, fail_date    AS task_date FROM Failed
    WHERE YEAR(fail_date)    = 2019
    UNION ALL
    SELECT 'succeeded' AS period_state, success_date AS task_date FROM Succeeded
    WHERE YEAR(success_date) = 2019
),
-- Assign group numbers: same state + consecutive dates → same group
ranked AS (
    SELECT period_state, task_date,
           ROW_NUMBER() OVER (ORDER BY task_date) AS rn_global,
           ROW_NUMBER() OVER (PARTITION BY period_state ORDER BY task_date) AS rn_state
    FROM all_days
),
-- If the state changes OR dates are non-consecutive, rn_global - rn_state changes
grouped AS (
    SELECT period_state, task_date,
           -- rn_global - rn_state = constant within same-state consecutive blocks
           rn_global - rn_state AS grp
    FROM ranked
)
SELECT period_state,
       MIN(task_date) AS start_date,
       MAX(task_date) AS end_date
FROM grouped
GROUP BY period_state, grp
ORDER BY start_date;

-- Expected output:
-- period_state | start_date | end_date
-- failed       | 2019-01-01 | 2019-01-02
-- succeeded    | 2019-01-03 | 2019-01-03
-- failed       | 2019-01-04 | 2019-01-05
-- succeeded    | 2019-01-06 | 2019-01-07

-- EXPLAIN PLAN Challenge (describe for the interviewer):
-- 1. Seq Scan on Failed with YEAR filter (better: WHERE fail_date >= '2019-01-01' AND < '2020-01-01')
-- 2. Seq Scan on Succeeded similarly
-- 3. UNION ALL → concatenate result sets (no deduplication needed — ALL)
-- 4. Two window functions on the unioned result → Sort by task_date
-- 5. GROUP BY (period_state, grp) → aggregate
-- 6. Sort by start_date for ORDER BY

-- ─────────────────────────────────────────────────────────────
-- SYNTAX DRILL — PHASE 1 ANSWERS
-- ─────────────────────────────────────────────────────────────

-- Q1: When would you use HAVING instead of WHERE?
-- A: Use HAVING to filter AFTER aggregation (when the condition references an aggregate
--    function like COUNT, SUM, AVG). Use WHERE to filter individual rows BEFORE
--    grouping — it cannot use aggregates.
--    Rule: "Can I evaluate this condition on a single row without grouping?" → WHERE.
--          "Does this condition involve COUNT/SUM/AVG/MIN/MAX?" → HAVING.

-- Q2: Difference between COUNT(*) and COUNT(column_name)?
-- A: COUNT(*) counts ALL rows including those with NULL values.
--    COUNT(column_name) counts only rows where column_name IS NOT NULL.
--    COUNT(DISTINCT column_name) counts unique non-NULL values.
--    Example: table has 5 rows, email column has 2 NULLs:
--      COUNT(*) = 5
--      COUNT(email) = 3
--      COUNT(DISTINCT email) = depends on duplicates

-- Q3: Why does WHERE salary = NULL return 0 rows?
-- A: NULL in SQL represents "unknown" or "missing" — it is not a value.
--    Any comparison with NULL (=, !=, <, >) evaluates to UNKNOWN, not TRUE or FALSE.
--    SQL WHERE only includes rows where the condition is TRUE.
--    Fix: Use WHERE salary IS NULL (or IS NOT NULL).
--    Memory aid: "NULL is not equal to anything, not even itself."
--                SELECT NULL = NULL → NULL (not TRUE, not FALSE)
