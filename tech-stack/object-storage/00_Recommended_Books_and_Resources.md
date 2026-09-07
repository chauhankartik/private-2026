# Recommended Books & Resources: Distributed Object Storage

A curated list of authoritative engineering papers, technical documentation, architectural guides, and open-source repositories for mastering distributed object storage.

---

## 1. Essential Papers & Technical Publications

1. **Ceph: A Scalable, High-Performance Distributed File System (SOSP '06)**  
   *Authors:* Sage A. Weil, Scott A. Brandt, Ethan L. Miller, Long A. Long, Carlos Maltzahn  
   *Focus:* The seminal paper introducing Ceph, RADOS, Object Storage Daemons (OSDs), and the **CRUSH algorithm** for deterministic object placement.

2. **RADOS: A Scalable, Reliable Storage Service for Petabyte-scale Storage Clusters (PDSW '07)**  
   *Authors:* Sage A. Weil, Andrew W. Uysal, Scott A. Brandt, Ethan L. Miller  
   *Focus:* Deep dive into RADOS self-managing storage nodes, peer-to-peer failure recovery, placement groups, and object replication.

3. **Amazon S3 Architecture & Design Principles**  
   *Publications & Talks:* Werner Vogels (AWS CTO) engineering blogs and AWS re:Invent architecture deep-dives on S3 Strong Consistency, high availability, and multi-region data persistence.

---

## 2. Technical Documentation & Manuals

* **[Ceph Official Architecture Documentation](https://docs.ceph.com/en/latest/architecture/)** — Comprehensive internal architecture of RADOS, CRUSH maps, OSD storage backends (BlueStore), and the RADOS Gateway (RGW).
* **[MinIO High-Performance Object Storage Docs](https://min.io/docs/minio/linux/index.html)** — Distributed MinIO setup, erasure code calculators, multi-tenant site replication, and security configuration.
* **[AWS S3 REST API Reference](https://docs.aws.amazon.com/AmazonS3/latest/API/Welcome.html)** — Canonical specification for S3 HTTP REST endpoints, SigV4 authentication, multipart uploads, and lifecycle rules.
* **[OpenStack Swift Architecture Guide](https://docs.openstack.org/swift/latest/)** — Ring architecture, proxy servers, container/account databases, and auditor processes.

---

## 3. Open-Source Codebases to Explore

* **[Ceph Repository (GitHub)](https://github.com/ceph/ceph)** — C++ implementation of RADOS, CRUSH (`src/crush/`), BlueStore (`src/os/bluestore/`), and RGW (`src/rgw/`).
* **[MinIO Server Repository (GitHub)](https://github.com/minio/minio)** — Go implementation of MinIO server, S3 API handlers, and erasure coding.
* **[MinIO Erasure Code SIMD Library (`minio/reed-solomon`)](https://github.com/minio/reed-solomon)** — Ultra-fast Go + Assembly implementation of Reed-Solomon erasure coding using AVX-512 and ARM NEON extensions.
