# Grafana Architecture Taxonomy & Interactive Mind Map

Grafana is an open-source, multi-platform operational observability and data visualization platform. It connects to disparate data sources (Prometheus, Loki, Tempo, PostgreSQL, Elasticsearch) to render real-time dashboards, unified alerting, and correlated telemetry across metrics, logs, and distributed traces (the **LGTM** stack).

---

## 🧠 Interactive Grafana Architecture Mind Map

```mermaid
mindmap
  root(("Grafana Architecture"))
    "01 Server & Backend Engine"
      "Go HTTP Backend Server"
      "SQL Storage - SQLite PostgreSQL MySQL"
      "Multi-Tenancy - Orgs Teams Users"
      "RBAC & Folder Permissions"
    "02 Data Source Plugins"
      "Frontend vs Backend Data Sources"
      "gRPC HashiCorp go-plugin"
      "Server-Side Expression Engine"
      "Query Multiplexing & Rate Limiting"
    "03 Prometheus Integration"
      "PromQL Vector & Scalar Queries"
      "Rate & Irate Calculations"
      "Panel Resolution - dollar_interval & dollar_rate_interval"
      "Time-Series & Heatmap Visualization"
    "04 Loki Log Aggregation"
      "Indexless Log Architecture"
      "LogQL Query Syntax - Stream Selectors & Line Filters"
      "Promtail & Alloy Log Collectors"
      "Correlating Logs with Metrics"
    "05 Tempo & Distributed Tracing"
      "OpenTelemetry OTel Standard"
      "Tempo Object Storage Architecture"
      "Trace-to-Logs Correlation"
      "Derived Span Metrics"
    "06 Unified Alerting Engine"
      "Alert Rules & Evaluation Scheduler"
      "State Manager - OK Pending Alerting"
      "Notification Routing & Contact Points"
      "Silence & Inhibition Rules"
    "07 Dashboards-as-Code & Ops"
      "Jsonnet & Grafonnet Provisioning"
      "Terraform Grafana Provider"
      "High Availability Clustering & Session Cache"
      "Diagnostic Troubleshooting & Query Optimization"
```

---

## 📊 Core Component Matrix

| Subsystem | Core Component / Tech | Primary Responsibility | Key Mechanism |
| :--- | :--- | :--- | :--- |
| **Backend Server** | Go HTTP Server, SQL Database | Manages web API, authentication, dashboard state, organizations, and permissions | SQLite / Postgres database schema, `chi` HTTP router |
| **Plugin Engine** | gRPC, `go-plugin` | Connects Grafana to third-party metric, log, trace, and relational databases | IPC via Unix sockets / TCP gRPC buffers |
| **Query Engine** | Server-Side Expressions (SSE) | Evaluates math, resample, reduce, and threshold operations on query streams | Go pipeline execution before client rendering |
| **Metrics Plugin** | Prometheus / PromQL | Queries time-series metrics data from Prometheus, Mimir, or Thanos | HTTP REST API `/api/v1/query_range` |
| **Log Engine** | Loki / LogQL | Queries log streams using label-based indexing without full-text indexing overhead | Chunk storage retrieval + LogQL stream evaluation |
| **Tracing Engine** | Tempo / OpenTelemetry | Fetches distributed trace trees matching TraceIDs or search filters | Object storage scan (S3/GCS) + Parquet indexing |
| **Alert Engine** | Unified Alerting | Schedules alert rule evaluations, tracks state changes, routes notifications | Alertmanager routing tree, PagerDuty/Slack webhooks |
