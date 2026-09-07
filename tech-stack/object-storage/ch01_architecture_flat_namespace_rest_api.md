# Chapter 1: Flat Namespace Architecture & REST S3 Protocol Mechanics

## 1. Flat Namespace vs Hierarchical File Systems

Traditional filesystem models (POSIX, NFS, EXT4, HDFS) organize data in a hierarchical tree of nested directories and subdirectories governed by **Inodes**.

```
 Hierarchical File System (POSIX):
 /var
  └── log
       └── app
            └── 2026-09-07.log   <--- Requires traversing directory inodes & holding directory locks!

 Flat Namespace Object Storage (Amazon S3 / Ceph / MinIO):
 Bucket: "app-logs"
 Key:    "var/log/app/2026-09-07.log"   <--- Pure string identifier mapped directly to Object Payload!
```

### Why Flat Namespaces Scale to Exabytes:
1. **Elimination of Directory Inode Locks:** In POSIX filesystems, updating a file requires modifying parent directory inode metadata. In an object store with millions of concurrent writes, directory lock contention degrades performance. A flat namespace treats `"var/log/app/2026-09-07.log"` as an arbitrary flat string key, executing metadata updates in parallel.
2. **Decoupled Key-Value Lookup:** The object store indexes keys in a high-throughput key-value engine (e.g., RocksDB or distributed hash table).

---

## 2. REST S3 Protocol Standard & HTTP Verbs

Object storage interacts exclusively over HTTP/1.1 and HTTP/2 REST APIs.

```
+-----------------------------------------------------------------------------------+
| S3 REST API Operations                                                            |
+---------+--------------------+----------------------------------------------------+
| HTTP    | S3 Operation       | Description                                        |
+---------+--------------------+----------------------------------------------------+
| GET     | `GetObject`        | Stream object binary payload (Supports `Range`).   |
| PUT     | `PutObject`        | Create/replace object payload & system metadata.   |
| DELETE  | `DeleteObject`     | Remove object or write a Delete Marker.            |
| HEAD    | `HeadObject`       | Retrieve object headers/metadata without payload.  |
| POST    | `CompleteMultipart`| Finalize multi-part chunked object uploads.        |
+---------+--------------------+----------------------------------------------------+
```

### Range Requests (`Range: bytes=0-1048575`)
Client applications can read partial byte ranges of massive multi-gigabyte objects (e.g., querying Apache Parquet footer metadata without downloading the entire 50GB object):
```http
GET /analytics/events.parquet HTTP/1.1
Host: mybucket.s3.amazonaws.com
Range: bytes=0-1048575
```
* Returns `HTTP/1.1 206 Partial Content` with the requested 1MB slice.

---

## 3. Virtual-Hosted-Style vs Path-Style Routing

S3 API requests can be routed to storage endpoints using two URL styles:

```
 1. Virtual-Hosted-Style Routing (Recommended Standard):
    https://mybucket.s3.us-east-1.amazonaws.com/images/avatar.png
    └─────┬────┘ └───────────┬────────────┘ └────────┬─────────┘
        Bucket          Endpoint domain           Object Key

 2. Path-Style Routing (Deprecated Legacy):
    https://s3.us-east-1.amazonaws.com/mybucket/images/avatar.png
    └───────────┬────────────┘ └───┬────┘ └────────┬─────────┘
               Endpoint          Bucket       Object Key
```

* **Why Virtual-Hosted-Style is Preferred:** Virtual-Hosted-Style routes traffic via DNS CNAME lookup directly to dedicated bucket endpoint clusters, allowing wildcards (`*.s3.domain.com`) and distribution across independent physical gateway load balancers.

---

## 4. AWS Signature Version 4 (SigV4) Authentication

Every HTTP request to an S3-compatible object store must be cryptographically authenticated using **AWS Signature Version 4 (SigV4)**.

```
 Client Application                                                          S3 Gateway Router
        |                                                                           |
 1. Builds Canonical Request:                                                       |
    - HTTP Verb (PUT/GET)                                                           |
    - Canonical URI & Query Params                                                  |
    - Hash of Payload: SHA256(Body)                                                 |
        |                                                                           |
 2. Creates StringToSign:                                                           |
    - Algorithm (AWS4-HMAC-SHA256)                                                  |
    - Request Date (ISO8601)                                                        |
    - Credential Scope                                                              |
    - Hash(CanonicalRequest)                                                        |
        |                                                                           |
 3. Derives Signing Key via HMAC:                                                   |
    kDate = HMAC("AWS4" + SecretKey, Date)                                          |
    kRegion = HMAC(kDate, Region)                                                   |
    kService = HMAC(kRegion, "s3")                                                  |
    kSigning = HMAC(kService, "aws4_request")                                       |
        |                                                                           |
 4. Calculates Signature:                                                           |
    Signature = HexEncode(HMAC(kSigning, StringToSign))                             |
        |                                                                           |
        | ------------- HTTP Headers with Signature Authorization ----------------->|
        |                                                                           |
                                                                          Re-computes HMAC Signature
                                                                          Validates Client Request
```
