# Chapter 2: Data Source Plugins & Query Execution Pipeline

Grafana acts as an abstraction layer across disparate storage engines. Understanding how queries traverse Grafana's backend via data source plugins is essential for high-throughput observability.

---

## 1. Data Source Plugin Architectures

Grafana supports two data source execution patterns: **Frontend Plugins** and **Backend Plugins**.

```
[ Web Browser ]
      |
      +---> (Direct CORS Request) --------> [ External Data Source ]
      |                                     (Legacy Frontend Mode)
      v
[ Grafana Backend Proxy (/api/datasources/proxy) ]
      |
      v gRPC over Unix Domain Socket
[ Backend Data Source Plugin ] (HashiCorp go-plugin Sub-Process)
      |
      v HTTP / Native Binary Protocol
[ Target Database ] (Prometheus, Postgres, Loki, ClickHouse)
```

### 1.1 Mechanical Comparison

| Dimension | Frontend Data Source Plugin | Backend Data Source Plugin (Modern Standard) |
| :--- | :--- | :--- |
| **Execution Point** | Executes inside user's Web Browser. | Executes on **Grafana Go Backend Server**. |
| **Security / Credentials**| Credentials must be exposed to browser (or proxied raw). | Secrets (API keys, DB passwords) **never leave Grafana server**. |
| **Alerting Support** | Cannot be used for Server-Side Alerting. | **Supports Unified Alerting & Server-Side Expressions**. |
| **IPC Mechanism** | Direct HTTP `fetch()` | **gRPC over HashiCorp `go-plugin`**. |

---

## 2. HashiCorp `go-plugin` Architecture

Modern Grafana plugins run as standalone OS sub-processes spawned by `grafana-server`.

```
+-------------------------------------------------------------------+
| grafana-server (Parent Process)                                   |
|                                                                   |
| [ Plugin Manager ] <--- gRPC over Unix Socket ---> [ Sub-Process ]|
|                                                    (Plugin Process)|
+-------------------------------------------------------------------+
```

### 2.1 Process Isolation Benefits
1. **Fault Isolation:** A panic, crash, or memory leak inside a custom plugin process terminates only that sub-process. `grafana-server` automatically restarts the plugin without dropping user web sessions.
2. **Security Sandboxing:** Plugin processes run with restricted OS permissions and cannot mutate Grafana's core SQL metadata database.

---

## 3. Server-Side Expression (SSE) Engine

The **Server-Side Expression (SSE)** engine allows Grafana to execute mathematical transformations across query results from different data sources before returning data to panels or alerting rules.

```
Query A (Prometheus: Memory Usage) ──┐
                                     ├──> [ Server-Side Expression Engine ] ──> Final Panel Series
Query B (Postgres: Memory Limit)   ──┘    Operation: Math ($A / $B * 100)
```

### 3.1 Core Expression Types
* **Math:** Evaluates free-form mathematical expressions on time-series data using operators (`+`, `-`, `*`, `/`, `>`, `<`).
* **Reduce:** Compresses a time-series vector into a single scalar value (`mean`, `max`, `min`, `sum`, `last`).
* **Resample:** Standardizes time intervals across multiple data sources (e.g. resampling a 10s Prometheus stream to match a 1m Postgres stream).
* **Threshold:** Evaluates state boundaries (`OK`, `Warning`, `Critical`) for alerting rules.

---

## 4. Staff Engineer Plugin Operations
1. **Enforce Backend Plugins for Alerting:** Always use backend data source plugins for infrastructure monitoring to allow Grafana Unified Alerting to evaluate rules continuously when browsers are closed.
2. **Limit Max Concurrent Queries:** Configure `[dataproxy] max_idle_connections` and query concurrency limits in `grafana.ini` to prevent 50 open dashboards from overloading downstream Prometheus or SQL databases during auto-refresh bursts.
3. **Monitor Plugin Process Health:** Monitor `grafana_plugin_request_duration_seconds` metrics to catch failing or slow custom gRPC plugins.
