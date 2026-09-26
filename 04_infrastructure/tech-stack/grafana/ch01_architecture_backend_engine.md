# Chapter 1: Grafana Server Architecture & Backend Engine

Grafana is designed as a lightweight, high-concurrency Go application backed by a SQL database for metadata storage, serving a React-based single-page application (SPA).

---

## 1. Go Backend Architecture & Service Registry

The Grafana server (`grafana-server`) is written in Go, structured around an internal service registry.

```
+-------------------------------------------------------------------+
| grafana-server (Go Process)                                       |
|                                                                   |
| [ HTTP Router (chi) ]  --> Handles Web API & Auth Middleware       |
|         |                                                         |
|         v                                                         |
| [ Service Registry / Dependency Injection ]                       |
|   ├── SqlStore Service (DB Access)                                |
|   ├── QuotaService & AuthService (RBAC)                           |
|   ├── DataProxy Service (HTTP / gRPC Data Source Proxy)           |
|   └── AlertingEngine Service (NGAlert Scheduler)                  |
+-------------------------------------------------------------------+
            |
            v GORM / xorm ORM
+-------------------------------------------------------------------+
| SQL Metadata Backend (PostgreSQL / MySQL / SQLite)                |
+-------------------------------------------------------------------+
```

### 1.1 SQL Metadata Backend
Grafana does **NOT** store time-series metrics data itself. It uses a relational database solely for application state and metadata:
* **SQLite3:** Default for single-instance developer setups (stored locally in `grafana.db`).
* **PostgreSQL / MySQL:** Mandatory for enterprise High-Availability (HA) clustered deployments. Stores dashboards, alert rules, user credentials, preferences, data source definitions, and permission trees.

---

## 2. Multi-Tenancy & Access Control (RBAC)

Grafana isolates data and administrative capabilities across a multi-tenant model.

```
[ Organization (Org) ]  ---> Top-level tenant boundary (Complete Data Isolation)
       |
       +---> [ Teams ]  ---> Groups of users for permission assignment
       |
       +---> [ Users ]  ---> Org-level Roles: Admin | Editor | Viewer
       |
       v
[ Folder / Dashboard RBAC Tree ]
  ├── Folder: "Payment Services" (Team 'FinOps' = Editor)
  └── Dashboard: "Latency SLOs" (Role 'Viewer' = View Only)
```

### 2.1 Organization Isolation
* Every data source, dashboard, alert rule, and folder belongs to a specific **Organization (`org_id`)**.
* Queries issued by a user are strictly scoped to their currently active `org_id` context in the session token.

---

## 3. Frontend Rendering Architecture

Grafana's user interface is a React single-page application executing in the user's browser.

```
React Application (Web Browser)
       |
       ├── State Management (Redux Toolkit & React Query)
       ├── Panel Plugin System (@grafana/ui & @grafana/data)
       |
       v Time-Series Canvas Rendering Engine
[ uPlot Engine ] ---> Renders 150,000+ Data Points in < 25ms
```

### 3.1 `uPlot` Rendering Engine
* Older versions of Grafana used Flot (jQuery-based SVG rendering), which stalled browsers on large datasets.
* Modern Grafana uses **uPlot**, an ultra-fast, memory-efficient 2D Canvas chart rendering library capable of rendering 150,000 series data points in under 25 milliseconds with minimal garbage collection overhead.

---

## 4. Staff Engineer Backend SLA Guidelines
1. **Never Use SQLite in Production HA:** Always back production Grafana clusters with PostgreSQL or MySQL. SQLite locks the entire database file during writes, causing HTTP 500 errors when multiple Grafana server instances attempt concurrent writes.
2. **Enable Database Connection Pooling:** Tune `[database]` settings in `grafana.ini` (`max_open_conn = 30`, `max_idle_conn = 30`) to match your relational database capacity.
3. **Use Session Caching (Redis):** Set `[session] provider = redis` in clustered deployments to maintain seamless user login sessions across load-balanced Grafana instances.
