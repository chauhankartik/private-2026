# Chapter 3: Prometheus & PromQL Query Visualization

Prometheus is the most widely adopted time-series metrics data source in Grafana. Understanding **PromQL** query execution and dashboard variable optimization prevents metric rendering degradation.

---

## 1. PromQL Vector Types & Rate Functions

PromQL operates on two fundamental vector types:

```
Instant Vector  ---> Single value per time-series at timestamp T   (e.g., node_memory_Active_bytes)
Range Vector    ---> Matrix of values over duration window [5m]    (e.g., http_requests_total[5m])
```

### 1.1 `rate()` vs `irate()`

```
  Metric Value
      ^
      |         x (Sample 2)
      |        /
      |       /   <--- irate() looks ONLY at last 2 samples (Volatile, instant spikes)
      |      x (Sample 1)
      |     /
      |    x (Sample 0)  <--- rate() calculates average slope over ENTIRE [5m] window (Smooth)
      +-----------------------------> Time
```

* **`rate(v[5m])`:** Calculates the average per-second rate of increase across the entire range vector. Automatically handles counter resets (e.g., container restarts). Smooths out noise; recommended for alerts and general dashboards.
* **`irate(v[5m])`:** Calculates the per-second rate based strictly on the **last two data points** in the range vector. Responds instantly to rapid metric spikes, but sensitive to scrape noise.

---

## 2. Calculating Percentiles: `histogram_quantile`

Histograms track latency distributions in cumulative bucket counters (`le` label = "less than or equal to").

```sql
# Calculates 95th percentile HTTP latency grouped by handler
histogram_quantile(0.95, 
  sum(rate(http_request_duration_seconds_bucket[5m])) by (le, handler)
)
```

1. `rate(..._bucket[5m])`: Calculates per-second rate of observations entering each bucket.
2. `sum(...) by (le, handler)`: Aggregates bucket rates across Pod instances for each handler.
3. `histogram_quantile(0.95, ...)`: Performs linear interpolation between bucket boundaries to estimate the $95^{\text{th}}$ percentile latency value.

---

## 3. Grafana Panel Optimizations & Variables

To prevent high-resolution queries from crashing user browsers or overloading Prometheus servers, Grafana provides dynamic interval variables.

```
Dashboard Time Range: 30 Days (Pixel Width: 1920px)
        |
        v Grafana Auto-Calculates
$__interval = 15 Minutes
        |
        v Substituted into PromQL
sum(rate(http_requests_total[$__rate_interval]))
```

### 3.1 `$__interval` vs `$__rate_interval`
* **`$__interval`:** Dynamic time duration matching the width of the panel in pixels. Ensures that Grafana requests exactly one data point per screen pixel.
* **`$__rate_interval`:** The optimal range vector duration for `rate()` functions:
  $$\text{\$__rate\_interval} = \$__interval + \text{Scrape\_Interval}$$
  * **Why it matters:** Guarantees that the range vector **always contains at least 2 scrape points** even if scrape loops drop a frame, preventing blank query gaps (`no data`) on dashboards.

---

## 4. Staff Engineer PromQL Tuning SLA
1. **Always Use `$__rate_interval` in `rate()` Functions:** Replace static range vectors like `[5m]` with `[$__rate_interval]` in time-series panels to ensure smooth rendering across any time zoom level.
2. **Avoid High-Cardinality Labels in `by ()` Clauses:** Grouping by high-cardinality labels (e.g. `user_id`, `email`, `ip_address`) forces Prometheus to return millions of time-series streams, freezing Grafana panel rendering.
3. **Use Recording Rules for Expensive Queries:** If a dashboard query evaluates `histogram_quantile` over a 30-day window, pre-calculate the result using Prometheus **Recording Rules** (`record: job:request_duration_seconds:p95`).
