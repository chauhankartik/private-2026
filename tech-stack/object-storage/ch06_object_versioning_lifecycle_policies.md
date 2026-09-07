# Chapter 6: Object Versioning, WORM Immutability & Lifecycle Policies

## 1. Object Versioning Mechanics & Delete Markers

When **Object Versioning** is enabled on an S3 bucket, mutations (`PUT`, `POST`, `DELETE`) do not overwrite existing object binaries on disk. Instead, every mutation creates a new version tagged with a unique, monotonically increasing **`VersionId`**.

```
 Key: "documents/contract.pdf"

 Version History Stack (Sorted by Timestamp Descending):
 +-----------------------------------------------------------------------------------+
 | VersionId | Event        | Metadata State                                         |
 +-----------+--------------+--------------------------------------------------------+
 | v3_latest | DeleteMarker | Null payload marker; GET returns `HTTP 404 Not Found`. |
 | v2_sub    | PUT          | 12 MB payload (Updated draft).                         |
 | v1_init   | PUT          | 10 MB payload (Initial upload).                        |
 +-----------------------------------------------------------------------------------+
```

### Delete Marker Behavior:
* Executing a `DELETE /bucket/object.pdf` without specifying a `versionId` does **not** erase data from disk.
* Instead, the storage gateway appends a **Delete Marker** as the latest version `v3_latest`.
* Standard `GET` requests encounter the Delete Marker and return `HTTP 404 Not Found`.
* To permanently erase an object from disk, the client must issue an explicit versioned delete:
  `DELETE /bucket/object.pdf?versionId=v1_init`.

---

## 2. Write-Once-Read-Many (WORM) Immutability & Object Lock

**S3 Object Lock** enforces Write-Once-Read-Many (WORM) immutability, preventing objects from being overwritten or deleted by any user (including the AWS root account or storage admin) during a retention period.

```
                      Client Request: DELETE /bucket/financial_audit.pdf?versionId=v1
                                                     |
                                                     v
                                      Checks S3 Object Lock Policy
                                                     |
                        +----------------------------+----------------------------+
                        |                                                         |
                        v Compliance Mode                                         v Governance Mode
       Strict Retention Enforcement                              Bypass Authorization Check
       - NO ONE (including admin/root)                           - Users with `s3:BypassGovernanceRetention`
         can delete or modify object.                              permission can override lock.
       - Enforced for legal SEC 17a-4 compliance.               - Used for internal dev/test safety locks.
```

---

## 3. Index Lifecycle Management (ILM)

**Index Lifecycle Management (ILM)** automates object migration across storage tiers based on object age, access frequency, and prefix rules.

```
 +--------------------+       Transitions after 30 days       +--------------------+
 | S3 Standard        |-------------------------------------->| S3 Infrequent Access|
 | (Hot NVMe/SSD)     |                                       | (Warm HDDs)        |
 +--------------------+                                       +---------+----------+
                                                                        |
                                              Transitions after 90 days |
                                                                        v
 +--------------------+       Transitions after 365 days      +--------------------+
 | S3 Glacier Deep    |<--------------------------------------| S3 Glacier Flexible|
 | (Cold Tape Archive)|                                       | (Cold Disk)        |
 +--------------------+                                       +--------------------+
```

### Background Garbage Collection Rules:
1. **Expired Object Deletion:** ILM continuously scans metadata indexes, purging objects whose age exceeds `Expiration` policy days.
2. **Expired Delete Marker Cleanup:** When all underlying object versions have been purged, background ILM worker threads automatically sweep and remove orphaned Delete Markers from RocksDB metadata stores.
