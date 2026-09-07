# Splunk Enterprise Architecture Taxonomy & Interactive Mind Map

Splunk Enterprise is a distributed log analytics, indexing, and SIEM (Security Information and Event Management) platform designed to ingest, parse, index, search, and visualize unstructured machine data at scale.

---

## 🧠 Interactive Splunk Architecture Mind Map

```mermaid
mindmap
  root(("Splunk Enterprise"))
    "01 Distributed Topology"
      "Universal Forwarder - Lightweight Log Collector"
      "Heavy Forwarder - Full Splunk Instance Parsing"
      "Indexer Cluster - Bucket Storage & Search Execution"
      "Search Head Cluster - Raft Captain & SHC Dispatch"
    "02 Data Pipeline Stages"
      "Input Stage - Stream Ingestion"
      "Parsing Stage - Line Breaking & Timestamps"
      "Indexing Stage - Key Indexing & Journal Write"
      "Search Stage - Search Time Field Extraction"
    "03 Storage Engine & Buckets"
      "Bucket Lifecycle - Hot Warm Cold Frozen Thawed"
      "journal.gz - Compressed Rawdata"
      "tsidx Files - Inverted Keyword Index"
      "Clustering - Replication & Search Factors"
    "04 Search Processing Language SPL"
      "Streaming Commands - eval rex lookup"
      "Reporting Commands - stats chart timechart"
      "Eventing Commands - transaction dedup"
      "Subsearches & Joins"
    "05 Search Acceleration"
      "Search Head Clustering Raft Election"
      "Data Model Acceleration"
      "tstats High Speed Search"
      "Summary Indexing"
    "06 Security & SIEM"
      "Enterprise Security SIEM Framework"
      "Risk Based Alerting"
      "Role Based Access Control RBAC"
      "Data Pipeline SSL Encryption"
    "07 Performance & Diagnostics"
      "_internal Index Monitoring"
      "btool Configuration Debugging"
      "Forwarder Queue Backpressure"
      "Licensing & Sizing"
```

---

## 📊 Core Component Matrix

| Component | Topology Role | Primary Responsibility | Key Configuration Files |
| :--- | :--- | :--- | :--- |
| **Universal Forwarder (UF)** | Agent | Collects local log files and streams compressed data to Indexers | `inputs.conf`, `outputs.conf` |
| **Heavy Forwarder (HF)** | Intermediate Server | Parses, routes, masks, or filters log data before indexing | `props.conf`, `transforms.conf` |
| **Indexer** | Storage Node | Parses data, writes `.tsidx` index files and `journal.gz`, executes search jobs | `indexes.conf`, `props.conf` |
| **Search Head (SH)** | Frontend Node | Handles user search requests, dispatches search jobs to Indexers, aggregates results | `savedsearches.conf`, `authorize.conf` |
| **Manager Node (CM)** | Cluster Controller | Manages Indexer Cluster bucket replication factor (RF) and search factor (SF) | `server.conf` |
| **Deployment Server (DS)**| Management Node | Pushes configuration apps and updates to Universal/Heavy Forwarders | `serverclass.conf` |
