# Distributed Object Storage Architecture Mind Map

This document presents a structured visual breakdown of distributed object storage internals.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((Object Storage Deep Dive))
    Interface Layer
      Flat Namespace
        Bucket Organization
        Key Identifier Mapping
        No Inode Locks
      REST S3 Protocol
        HTTP GET PUT DELETE HEAD
        Virtual Hosted Style Routing
        Pre-signed URL Delegation
        AWS SigV4 HMAC Signing
    Placement Algorithms
      Ceph RADOS
        OSD Storage Nodes
        MON Paxos Cluster State
        Placement Groups PGs
      CRUSH Calculation
        Cluster Map Hash Lookup
        Failure Weighting Rack Host Disk
        Zero Lookup Table Bottleneck
    Durability Engine
      Replication Scheme
        3x Replication Overhead
        Cross DC Sync Async Replication
      Erasure Coding Scheme
        Reed Solomon K Data M Parity
        Galois Field Arithmetic
        15x Storage Factor
        Degraded Read Network Bandwidth
    Data Execution Path
      Write Path
        Strong Read After Write Consistency
        Atomic Metadata Commit
      Multipart Uploads
        Initiate Part Upload Complete
        5MB to 5GB Concurrent Chunks
        ETag Verification Hash
    Metadata Infrastructure
      Decoupled Architecture
        Raw Payload File Direct Disk
        Metadata LSM Tree Key Value
      Querying Capabilities
        Lexicographical Prefix Pagination
        S3 Select Pushdown Parquet Arrow
    Governance and Lifecycle
      Versioning Mechanics
        Monotonic Version Identifier
        Delete Marker Tombstone
      Retention Rules
        WORM Lock Compliance Governance
        ILM Lifecycle Transition Rules
```

---

## 2. Component Reference Table

| Component | Responsibility | Bottlenecks & Operational Risks |
| :--- | :--- | :--- |
| **S3 Gateway Router** | Translates REST HTTP SigV4 requests to internal RPCs | High CPU consumption during SigV4 HMAC signature verification |
| **CRUSH Engine (Ceph)** | Calculates target OSD array from PG ID and Cluster Map | Excessive data rebalancing during large OSD cluster topology changes |
| **Erasure Encoder** | Generates $M$ parity chunks using Galois Field matrix math | High CPU utilization without SIMD (AVX-512/NEON) hardware acceleration |
| **Metadata Engine (RocksDB)** | Stores key-to-block mappings, ACLs, and object attributes | LSM compaction write amplification locking bucket prefix scans |
| **Multipart Assembler** | Manages chunk manifests and verifies part ETags | Abandoned un-completed multipart upload chunks wasting disk storage |
| **ILM Engine** | Scans buckets and executes tier transition policies | High metadata scan overhead on buckets with hundreds of millions of objects |
