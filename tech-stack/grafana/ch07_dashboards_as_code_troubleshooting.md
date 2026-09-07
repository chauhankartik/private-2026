# Chapter 7: Dashboards-as-Code, Security & Production Troubleshooting

Managing Grafana at scale requires GitOps automation for dashboard provisioning and structured diagnostic playbooks for operational failures.

---

## 1. GitOps & Dashboards-as-Code

Manually creating dashboards via the GUI leads to configuration drift and loss of audit history. Enterprise teams manage Grafana declaratively via **Dashboards-as-Code**.

```
[ Git Repository ] ---> [ Jsonnet / Grafonnet Templates ]
                                  |
                                  v Compiles to JSON
                        [ Grafana Dashboard JSON ]
                                  |
            +---------------------+---------------------+
            |                                           |
            v Terraform Apply                           v File Provisioning
[ Terraform Grafana Provider ]             [ Grafana Auto-Provisioning Engine ]
(Pushes via API)                           (Loads from /etc/grafana/provisioning)
```

### 1.1 Terraform Grafana Provider Example
```hcl
resource "grafana_dashboard" "payment_metrics" {
  config_json = file("${path.module}/dashboards/payment-overview.json")
  folder      = grafana_folder.production_services.id
  overwrite   = true
}
```

---

## 2. High-Availability (HA) Clustered Topology

For 99.99% availability, run multiple stateless Grafana server instances behind a Load Balancer.

```
                    [ Layer 7 Load Balancer (ALB / Nginx) ]
                                /             \
                               v               v
                [ Grafana Instance 1 ]   [ Grafana Instance 2 ]
                                \             /
                                 v           v
                    +------------------------------------+
                    | Shared PostgreSQL Metadata DB      |
                    | Shared Redis Session Store         |
                    +------------------------------------+
```

* **Shared SQL Database:** Stores centralized dashboards, permissions, and unified alert rule states.
* **Shared Redis Cache:** Stores active user web sessions so users do not get logged out when load balancers route requests to another instance.

---

## 3. Production Troubleshooting Playbook

### 3.1 Diagnosing Slow Dashboard Panels
When a panel displays a loading spinner or freezes the browser:
1. Open **Panel Inspector** (`Panel Menu -> Inspect -> Query`).
2. Inspect **Query Stats**:
   * `Total request time`: Total time spent fetching data from server.
   * `Processing time`: Time spent transforming data on server-side expression engine.
3. If query time is high, check downstream database (Prometheus / Loki / Postgres) for missing indexes or high cardinality.

### 3.2 Common Grafana Errors & Resolutions

```
+---------------------------------------------------------------------------------------+
| Error & Symptom                                                                       |
|                                                                                       |
| HTTP 504 Gateway Timeout / Data Proxy Timeout                                         |
|   ├── Cause: Downstream data source took longer than dataproxy timeout to respond.    |
|   └── Fix: Increase `dataproxy.timeout = 300` in `grafana.ini` or optimize query.     |
|                                                                                       |
| Plugin Failed to Load / gRPC Exec Error                                               |
|   ├── Cause: Sub-process backend plugin crashed or architecture binary mismatch.     |
|   └── Fix: Check `/var/log/grafana/grafana.log` for plugin panic logs.                |
|                                                                                       |
| Database Locked (SQLite3 in HA Setup)                                                 |
|   ├── Cause: Multiple Grafana instances writing concurrently to SQLite file.          |
|   └── Fix: Migrate `[database]` to PostgreSQL or MySQL.                               |
+---------------------------------------------------------------------------------------+
```

---

## 4. Staff Engineer Grafana Operations SLA
1. **Enforce Version Control for Production Dashboards:** Lock production folders so users cannot edit dashboards directly in the UI. All dashboard changes must go through Git pull requests and Jsonnet/Terraform CI/CD pipelines.
2. **Set Panel Data Limits:** Always set explicit `limit` values on SQL and LogQL queries to prevent fetching millions of rows into browser RAM.
3. **Automate Metadata Backups:** Perform daily automated backups of Grafana's PostgreSQL metadata database (`pg_dump`) to ensure fast disaster recovery.
