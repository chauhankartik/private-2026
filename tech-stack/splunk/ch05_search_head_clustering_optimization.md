# Chapter 5: Search Head Clustering & Query Acceleration

Search Head Clusters (SHC) provide horizontal scalability, search redundancy, and high availability for Splunk user interfaces and scheduled reporting pipelines.

---

## 1. Search Head Cluster (SHC) Architecture

A Search Head Cluster consists of a minimum of three Search Head instances sharing search jobs, user artifacts, knowledge objects, and KV Store state.

```
[ Load Balancer / Users ]
       |
       +---> [ Search Head 1 (Member) ] ──┐
       +---> [ Search Head 2 (Captain) ] ──┼── Raft Consensus & KVStore Sync
       +---> [ Search Head 3 (Member) ] ──┘
                   ^
                   | Pushes App Bundles (splunk apply shcluster-bundle)
         [ Deployer Node ]
```

### 1.1 The SHC Captain (Raft Consensus)
* **Raft Election:** SHC members dynamically elect a **Captain** using Raft consensus.
* **Captain Responsibilities:**
  1. Schedules and dispatches all cron-triggered alert searches (`savedsearches.conf`) across cluster members to balance CPU load.
  2. Synchronizes runtime configuration changes and KV Store data across all members.
  3. Manages search artifact replication (`/var/run/splunk/dispatch/`).

---

## 2. Distributed Search Job Dispatch Lifecycle

```
[ User Request ] ---> Search Head (Member)
                            |
                            ├── 1. Parses SPL Query & Generates Execution Tree
                            ├── 2. Separates Streaming vs Reporting Operations
                            |
                            v Dispatches Streaming Tasks (Port 8089)
       +--------------------+--------------------+
       |                    |                    |
       v                    v                    v
  [ Indexer 1 ]        [ Indexer 2 ]        [ Indexer 3 ]
  Reads .tsidx         Reads .tsidx         Reads .tsidx
  Executes eval/rex    Executes eval/rex    Executes eval/rex
       |                    |                    |
       +--------------------+--------------------+
                            |
                            v Returns Intermediate Streams
                     Search Head (Member)
                            |
                            ├── 3. Consolidates Intermediate Data Streams
                            ├── 4. Executes Final Reporting Commands (stats)
                            └── 5. Renders Dashboard Table / JSON Result
```

---

## 3. Query Acceleration Techniques

```
+-------------------------------------------------------------------+
| 1. Data Model Acceleration (DMA)                                  |
| - Pre-indexes structured fields into high-speed .tsidx summaries. |
| - Queried using `tstats` over `datamodel=Authentication`.        |
| - Acceleration: 100x query speedup.                              |
+-------------------------------------------------------------------+
| 2. Summary Indexing                                               |
| - Scheduled search runs hourly, writing aggregated summary metrics|
|   into a lightweight `index=summary`.                             |
| - Analysts query 1,000 summary rows instead of 10,000,000 raw logs|
+-------------------------------------------------------------------+
| 3. Report Acceleration                                            |
| - Automatically builds time-series index summaries for slow       |
|   reporting searches defined in `savedsearches.conf`.             |
+-------------------------------------------------------------------+
```

### 3.1 Data Model Acceleration Example
```spl
-- High-Speed Accelerated Data Model Search via tstats
| tstats summariesonly=t count 
  FROM datamodel=Network_Traffic.All_Traffic 
  WHERE All_Traffic.dest_port=80 
  BY All_Traffic.src
```

---

## 4. Staff Engineer Acceleration Rules
1. **Accelerate Data Models for SIEM:** Always enable Data Model Acceleration (DMA) for Common Information Model (CIM) data models used by Splunk Enterprise Security (ES).
2. **Never Edit App Bundles Directly on SHC Members:** Configurations modified directly on a member node will be overwritten during Captain synchronization or Deployer bundle deployment. Always deploy via the **Deployer**.
3. **Set Max Concurrent Searches:** Configure `max_searches_per_cpu` in `limits.conf` to prevent ad-hoc user queries from starving scheduled alert searches of CPU execution slots.
