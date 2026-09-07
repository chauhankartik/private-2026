# Chapter 5: Grafana Tempo, Jaeger & Distributed Tracing

Distributed tracing provides visibility into request propagation across complex microservice graphs, pinpointing latency bottlenecks and cascading failures.

---

## 1. Distributed Tracing Primitives & OpenTelemetry

```
Trace (TraceID: a1b2c3d4)
├── Span A: API Gateway (HTTP POST /checkout)  [0ms ───────────────> 120ms]
│   ├── Span B: Order Service (CreateOrder)    [10ms ──────> 60ms]
│   │   └── Span C: DB Insert (orders table)   [15ms ──> 40ms]
│   └── Span D: Payment Service (ChargeCard)   [65ms ─────────> 115ms]
```

### 1.1 Key Data Structures
* **TraceID:** Globally unique 128-bit identifier shared by all spans belonging to a single end-to-end request lifecycle.
* **SpanID:** 64-bit identifier uniquely identifying a specific operation segment.
* **ParentSpanID:** Identifies the parent span that initiated this child operation.
* **Context Propagation (W3C Trace Context):** Microservices pass the `traceparent` HTTP header (`00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01`) across HTTP/gRPC call boundaries.

---

## 2. Grafana Tempo Architecture

**Grafana Tempo** is a high-scale, zero-dependency distributed tracing backend that stores massive trace volumes directly in cheap cloud Object Storage.

```
[ Application (OTel SDK) ] ---> [ OpenTelemetry Collector ]
                                          |
                                          v OTLP (gRPC / HTTP)
+-------------------------------------------------------------------+
| Grafana Tempo Cluster                                             |
|                                                                   |
| [ Distributor ]  --> Ingests OTLP, Jaeger, Zipkin spans           |
|        |                                                          |
|        v                                                          |
| [ Ingester ]     --> Batches spans into blocks, builds Parquet    |
|        |             indexes, flushes to S3/GCS                  |
|        v                                                          |
| [ Querier ]      --> Searches traces via TraceID or TraceQL       |
+-------------------------------------------------------------------+
                               |
                               v Writes Parquet Trace Blocks
+-------------------------------------------------------------------+
| Object Storage (AWS S3, Google Cloud Storage, MinIO)              |
+-------------------------------------------------------------------+
```

---

## 3. Correlated Observability: Spans to Metrics & Logs

Grafana unifies the three pillars of observability (Metrics, Logs, Traces) through bidirectional linkages:

```
+------------------+                    +------------------+
| Prometheus Panel | <--- Metrics ----> | Grafana Tempo    |
| (High Latency)   |                    | Trace View       |
+------------------+                    +------------------+
                                                 |
                                         TraceID | Linkage
                                                 v
                                        +------------------+
                                        | Grafana Loki     |
                                        | Log Panel        |
                                        +------------------+
```

### 3.1 Derived Span Metrics
The OpenTelemetry Collector or Tempo's Metrics Generator inspects raw trace streams to automatically produce standard Prometheus metrics:
* **Service Graphs:** `traces_service_graph_request_total` (calculates call volume and error rates between services).
* **Span Latencies:** `traces_spanmetrics_latency_seconds_bucket` (produces metric histograms for every span name without modifying application code).

---

## 4. Staff Engineer Tracing Guidelines
1. **Use OpenTelemetry (OTel) Standard SDKs:** Standardize application instrumentation on OpenTelemetry SDKs rather than vendor-specific Jaegar/Zipkin libraries.
2. **Apply Head-Based or Tail-Based Sampling:** Ingesting 100% of traces at scale is prohibitively expensive. Use **Tail-Based Sampling** in the OTel Collector to retain 100% of error traces (`http.status_code >= 500`) and high-latency traces while sampling only 1% of successful $200\text{ OK}$ traces.
3. **Enable TraceQL in Grafana:** Use **TraceQL** (Tempo's query language) to search traces structurally, e.g.:
   ```text
   { .http.status_code >= 500 && duration > 2s }
   ```
