# Chapter 1: Splunk Distributed Architecture & Topologies

Splunk Enterprise utilizes a distributed, role-based component architecture to scale data ingestion, indexing, and search capabilities across thousands of enterprise servers.

---

## 1. Enterprise Component Topologies

```
[ Application Servers ]
  ├── Universal Forwarder 1 ──┐ (Raw TCP Stream - Port 9997)
  ├── Universal Forwarder 2 ──┼──────────────────────────────┐
  └── Universal Forwarder 3 ──┘                              |
                                                             v
[ Edge Ingestion Layer ]                        [ Heavy Forwarder Pool ]
                                                (Line Breaking / Masking)
                                                             |
                                                             v
+-------------------------------------------------------------------+
| Indexer Cluster                                                   |
|                                                                   |
| [ Cluster Manager ]  --> Manages Bucket Replication (RF/SF)       |
|        |                                                          |
|        +---> [ Indexer 1 ]  (Writes .tsidx & journal.gz)          |
|        +---> [ Indexer 2 ]  (Writes .tsidx & journal.gz)          |
|        +---> [ Indexer 3 ]  (Writes .tsidx & journal.gz)          |
+-------------------------------------------------------------------+
                               ^
                               | Dispatches Search Jobs (Port 8089)
+-------------------------------------------------------------------+
| Search Head Cluster (SHC)                                         |
|                                                                   |
| [ SHC Captain ] (Raft Leader) --> Dispatches Search Jobs to Indexers|
|   ├── Search Head 1 (Web UI - Port 8000)                          |
|   ├── Search Head 2 (Web UI - Port 8000)                          |
|   └── Search Head 3 (Web UI - Port 8000)                          |
+-------------------------------------------------------------------+
```

---

## 2. Component Roles & Capabilities

### 2.1 Universal Forwarder (UF) vs Heavy Forwarder (HF)

| Feature | Universal Forwarder (UF) | Heavy Forwarder (HF) |
| :--- | :--- | :--- |
| **Footprint** | Lightweight agent (~50MB RAM, minimal CPU). | Full Splunk Enterprise instance (~4GB+ RAM). |
| **Parsing Capabilities**| **None**. Captures raw byte streams only. | **Full Parsing**. Line breaking, timestamping, routing. |
| **Data Anonymization**| Cannot mask data via regex. | **Can mask sensitive data** (SSNs, API keys) via `transforms.conf`. |
| **Python Runtime** | Excludes full Python execution engine. | Includes full Python environment & Advanced Web APIs. |

### 2.2 Indexers & Indexer Clustering
* Indexers process incoming data through the Parsing and Indexing pipeline stages.
* **Manager Node (Cluster Manager):** Coordinates bucket replication across the Indexer pool to guarantee high availability:
  * **Replication Factor (RF = 3):** Cluster maintains 3 physical copies of rawdata (`journal.gz`).
  * **Search Factor (SF = 2):** Cluster maintains 2 searchable `.tsidx` index copies for immediate failover.

### 2.3 Search Head Clustering (SHC)
* Group of 3 or more Search Heads sharing search artifacts, user preferences, and scheduled search execution.
* **Raft Captain Election:** Members elect a **Captain** using the Raft consensus protocol. The Captain coordinates scheduled search job distribution and orchestrates KV Store replication.

---

## 3. Management & Control Plane Components

* **Deployment Server (DS):** Centralized deployment node that manages forwarder configurations via `serverclass.conf`, pushing app packages (`/etc/apps/`) to target forwarders dynamically.
* **License Manager (LM):** Tracks daily data ingestion quotas (e.g., 500GB/day) across all Indexer nodes.
* **Monitoring Console (MC):** Dedicated monitoring interface querying `_internal` logs to display real-time cluster health, indexing throughput, queue backpressure, and resource usage.

---

## 4. Staff Engineer Architecture Rules
1. **Never Parse Logs on Universal Forwarders:** Keep UFs lightweight. Offload line-breaking and timestamp parsing to Heavy Forwarders or Indexers to prevent host CPU contention on application servers.
2. **Deploy Heavy Forwarders at Network Boundaries:** Place HFs in DMZs or compliance zones to anonymize PII/PHI data (via `transforms.conf` SED script) *before* data crosses WAN links to central Indexers.
3. **Always Isolate Management Roles:** Do not co-locate the Deployment Server, License Manager, and Search Head Captain on a single production node in large deployments (`> 1TB/day` ingest).
