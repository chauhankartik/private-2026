# Chapter 4: Write & Read Execution Paths, Multipart Uploads & Consistency Models

## 1. Eventual Consistency vs S3 Strong Read-After-Write Consistency

Historically (prior to December 2020), cloud object stores like Amazon S3 enforced **Eventual Consistency** for overwrite `PUT`s and `DELETE`s. A `GET` request immediately following a `PUT` might return stale data or a `404 Not Found`.

### S3 Strong Read-After-Write Consistency Architecture
Modern object stores enforce **Strong Read-After-Write Consistency** for all new object `PUT`s, overwrite `PUT`s, and `DELETE` operations across all AWS regions and on-premise implementations (MinIO / Ceph RGW).

```
 Client Application                                                           S3 Gateway Router                                                  Metadata Engine & Disk Storage
        |                                                                            |                                                                          |
 1. PUT /bucket/object.png --------------------------------------------------------->|                                                                          |
        |                                                                            | 2. Streams payload to disk drives                                        |
        |                                                                            |    (Generates Erasure Chunks / Copies)                                  |
        |                                                                            |------------------------------------------------------------------------->|
        |                                                                            |                                                                          |
        |                                                                            | 3. Writes Atomic Metadata Index Record                                   |
        |                                                                            |<-------------------------------------------------------------------------|
        |                                                                            |                                                                          |
        |<---------------- 4. HTTP 200 OK (ETag: "a3b2c1...") ----------------------|                                                                          |
        |                                                                            |                                                                          |
 5. Immediate GET /bucket/object.png ----------------------------------------------->|                                                                          |
        |                                                                            | 6. Fetches committed metadata snapshot                                   |
        |                                                                            |------------------------------------------------------------------------->|
        |<---------------- 7. Returns New Object Version ----------------------------|                                                                          |
```

* **Atomic Metadata Commits:** Once an HTTP `200 OK` is returned to the client, subsequent read requests (`GET` or `HEAD`) are guaranteed to return the newly committed object version immediately.

---

## 2. Multipart Upload Architecture

Uploading large objects (ranging from 100MB to 5TB) over a single HTTP `PUT` connection is unreliable: a network drop at 99% requires re-uploading the entire file.

To solve this, S3 defines the **Multipart Upload Protocol**.

```
 Client Application                                                S3 Gateway Router                                               Disk Drives / Storage
        |                                                                  |                                                                  |
 1. POST /file.iso?uploads ----------------------------------------------->|                                                                  |
        |<---------------- Returns UploadId: "upload_99812" ---------------|                                                                  |
        |                                                                  |                                                                  |
 2. Parallel Upload Part 1 (Bytes 0-100MB) -------------------------------->| ---------------------------------------------------------------->|
        |<---------------- Returns ETag 1: "hash_1" ----------------------|                                                                  |
        |                                                                  |                                                                  |
 3. Parallel Upload Part 2 (Bytes 100MB-200MB) ---------------------------->| ---------------------------------------------------------------->|
        |<---------------- Returns ETag 2: "hash_2" ----------------------|                                                                  |
        |                                                                  |                                                                  |
 4. POST /file.iso?uploadId=upload_99812 (Manifest [Part 1, Part 2]) ----->|                                                                  |
        |                                                                  | 5. Verifies all Part ETags & concatenates manifests              |
        |                                                                  |----------------------------------------------------------------->|
        |<---------------- Returns HTTP 200 OK (ETag: "combined-2") -------|                                                                  |
```

### Multipart Rules & Mechanics:
* **Part Size Range:** Minimum part size is **5 MB** (except for the last part); maximum part size is **5 GB**.
* **Part Count:** Maximum **10,000 parts** per object (Maximum supported object size: $10,000 \times 5\text{GB} = 5\text{TB}$).
* **ETag Format for Multipart Objects:** The `ETag` of a completed multipart upload is appended with the number of parts (e.g., `"a3b2c1d4e5f6-42"` indicates a 42-part upload).

---

## 3. Streaming Payload vs Metadata Commit

To maximize write throughput, object gateways decouple raw data payload streaming from metadata catalog indexing:

1. **Payload Streaming Phase:** incoming HTTP socket byte streams are fragmented into block buffers and written directly to raw disk storage or erased-coded blocks.
2. **Metadata Commit Phase:** Once all payload blocks write successfully, the gateway commits a single key-value record to the metadata index (recording object key, size, MIME type, ETag, creation timestamp, and block mapping array).
3. If payload streaming fails mid-transfer, un-committed raw blocks are garbage collected during background sweep cycles.
