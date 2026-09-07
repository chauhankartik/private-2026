# Grafana & Observability Recommended Reading List & Technical References

A curated list of authoritative books, official documentation manuals, and open-source codebase pointers for mastering Grafana, Prometheus, Loki, Tempo, OpenTelemetry, and modern observability engineering.

---

## 📚 Recommended Books

1. **_Observability Engineering: Achieving Production Excellence_** — Charity Majors, Liz Fong-Jones, & George Miranda (O'Reilly)
   * **Why Read It:** The foundational textbook on cloud-native observability. Explains the shift from passive monitoring to active observability using high-cardinality structured events, metrics, logs, and distributed traces.
   * **Key Focus:** Architectural philosophy of modern observability systems.

2. **_Prometheus: Up & Running (2nd Edition)_** — Brian Brazil (O'Reilly)
   * **Why Read It:** Written by a core Prometheus maintainer. Deep dive into PromQL query mechanics, rate calculations, recording rules, alertmanager routing, and Grafana dashboard integration.

3. **_Grafana Metrics and Visualization: Build Operational Dashboards for Cloud-Native Systems_** — Practical Guides (Packt / Grafana Labs)
   * **Why Read It:** Comprehensive guide to Grafana panel design, dashboard variable scoping, data source configuration, unified alerting, and Jsonnet dashboards-as-code.

4. **_Cloud Native Observability with OpenTelemetry_** — Alex Boten (Packt Publishing)
   * **Why Read It:** Essential for understanding trace context propagation, OpenTelemetry Collector pipelines, span attributes, and sending telemetry to Grafana Tempo and Loki.

---

## 📄 Official Documentation & Reference Manuals

1. **[Grafana Documentation Portal](https://grafana.com/docs/grafana/latest/)**
   * **Topics:** Backend architecture, Plugin Development (gRPC), Server-Side Expressions, Unified Alerting engine, Dashboard JSON Schema.
2. **[Grafana Loki Documentation](https://grafana.com/docs/loki/latest/)**
   * **Topics:** LogQL query syntax, Promtail / Grafana Alloy log collectors, chunk storage, index compaction.
3. **[Grafana Tempo Documentation](https://grafana.com/docs/tempo/latest/)**
   * **Topics:** Distributed tracing backend, Parquet storage format, Trace-to-Logs correlation, TraceQL.

---

## 💻 Source Code References (Go Repositories)

Explore core components in the [Grafana GitHub Repositories](https://github.com/grafana):

* **`grafana/grafana` (`pkg/services/ngalert/`):** Next-Generation Alerting (Unified Alerting) scheduler, engine, and Alertmanager implementation.
* **`grafana/grafana` (`pkg/plugins/backendplugin/`):** HashiCorp `go-plugin` gRPC implementation for backend data source plugins.
* **`grafana/loki` (`pkg/logql/`):** LogQL query parser and evaluator for streaming log processing.
* **`grafana/tempo` (`modules/tempodb/`):** Storage engine scanning block indexes and Parquet trace files.
