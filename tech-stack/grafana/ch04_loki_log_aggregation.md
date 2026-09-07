# Chapter 4: Grafana Loki & Log Aggregation (LogQL)

Grafana **Loki** is a horizontally-scalable, highly-available, multi-tenant log aggregation system designed specifically to complement Prometheus by indexing labels rather than full log text.

---

## 1. Loki Architecture: Indexless Log Storage

Unlike traditional log search engines (Elasticsearch, OpenSearch) that build massive inverted indexes on every tokenized word in a log message, Loki **indexes only metadata labels**.

```
[ Application Logs ] ---> [ Promtail / Alloy Agent ]
                                  |
                                  v Streams Compressed Batches (/loki/api/v1/push)
+-------------------------------------------------------------------+
| Grafana Loki Cluster                                              |
|                                                                   |
| [ Distributor ]  --> Validates & hashes labels onto Hashing Ring  |
|        |                                                          |
|        v                                                          |
| [ Ingester ]     --> Buffers log chunks in RAM, writes compressed |
|        |             blocks to Object Storage                     |
|        v                                                          |
| [ Querier ]      --> Scans log chunks in parallel using LogQL     |
+-------------------------------------------------------------------+
                               |
                               v Writes Chunks & Compact Indexes
+-------------------------------------------------------------------+
| Object Storage (AWS S3, Google Cloud Storage, MinIO)              |
+-------------------------------------------------------------------+
```

### 1.1 Efficiency Trade-offs

| Feature | Elasticsearch / OpenSearch | Grafana Loki |
| :--- | :--- | :--- |
| **Indexing Strategy** | Full-text inverted index on every log line word. | **Metadata Labels only** (e.g., `app`, `env`, `pod`). |
| **Storage Footprint** | Heavy (Index size often 100-200% of raw log size). | **Ultra Small** (10-20% of raw size via gzip/snappy). |
| **Ingestion Throughput**| Medium (Bottlenecked by continuous CPU index build).| **Extremely High** (Zero text index overhead). |
| **Query Speed** | Instant for arbitrary word searches. | Fast for label-filtered range queries (Scans raw chunks). |

---

## 2. LogQL Query Language Deep-Dive

LogQL queries consist of two parts: a **Log Stream Selector** and an optional **Log Pipeline**.

```
{app="payment-api", env="prod"} |= "error" | json | status_code >= 500
└───────────┬─────────────────┘ └────┬────┘ └──┬─┘ └──────┬───────────┘
     Stream Selector         Line Filter  Parser   Filter Expression
```

### 2.1 Log Pipeline Operators
* **Line Filter Operators:**
  * `|=` : Line contains string (e.g., `|= "Exception"`).
  * `!=` : Line does not contain string.
  * `|~` : Line matches regex pattern (e.g., `|~ "status_code=[45][0-9]{2}"`).
  * `!~` : Line does not match regex pattern.
* **Parser Operators:**
  * `| json` : Extracts JSON fields into LogQL labels.
  * `| logfmt` : Extracts key-value `logfmt` fields into LogQL labels.
  * `| pattern "<ip> - <user> [<timestamp>] \"<method> <path>\""` : Custom pattern extraction.

### 2.2 Log-to-Metric Aggregations
Transform log streams into time-series graphs:

```sql
# Calculates per-second rate of error logs matching 'payment-api'
sum(rate({app="payment-api"} |= "ERROR" [$__interval])) by (pod)
```

---

## 3. Correlating Logs with Metrics & Traces

Grafana links Loki logs seamlessly with Prometheus metrics and Tempo traces using **Derived Fields**.

```json
// Data Source Configuration: Derived Field setup for Trace ID
{
  "derivedFields": [
    {
      "name": "TraceID",
      "matcherType": "regex",
      "matcherRegex": "trace_id=([a-f0-9]+)",
      "url": "$${__value.raw}",
      "datasourceUid": "tempo-datasource-uid"
    }
  ]
}
```

* When viewing logs in Grafana Explore, any extracted `trace_id` becomes an interactive link. Clicking the link opens the exact distributed trace in Grafana Tempo side-by-side with log output.

---

## 4. Staff Engineer Loki Operational Rules
1. **Beware Label High-Cardinality:** Do **NOT** put unique identifiers (`user_id`, `email`, `ip_address`, `trace_id`) into Loki labels! Doing so creates millions of tiny streams, destroying Loki ingester performance. Put high-cardinality values inside the log line text and use `| json` or line filters to extract them at query time.
2. **Configure Chunk Retention Rules:** Enable the Loki Compactor service with `retention_deletes_enabled: true` and define `retention_period` (e.g., 30d) to automatically purge expired object storage chunks.
3. **Use Promtail / Alloy Label Relabeling:** Drop unnecessary labels during log collection to keep stream count under control.
