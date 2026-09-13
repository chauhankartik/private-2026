# Master Study Plan 2026: Staff & Principal Software Engineer Preparation

Welcome to the **2026 Master Study Plan**. This comprehensive preparation roadmap synthesizes all technical modules across this repository into a structured **12-Week Intensive Curriculum** designed for Staff Software Engineers, Senior Systems Architects, and Tech Leads preparing for top-tier system design, algorithms, infrastructure, and database engineering interviews.

---

## 1. Curriculum Architecture & Overview

```mermaid
mindmap
  root((2026 Study Plan))
    Phase 1 Core Foundations
      Data Structures and Algorithms dsa
      Advanced Java Internals java
      SQL Query Masterclass and FAANG Patterns sql
    Phase 2 System Design LLD and HLD
      SOLID Principles and Clean Architecture
      GoF Design Patterns lld
      High Level System Design HLD
      4 Step Framework and Capacity Math
    Phase 3 Distributed Systems Theory
      Designing Data Intensive Applications ddia
      Consensus Raft Paxos distributed systems
      Sharding Replication 2PC Transactions
    Phase 4 Infrastructure Tech Stack
      Data Tier Redis Postgres JDBC Storage
      Streaming Kafka Edge Gateways
      Containers Orchestration Kubernetes
```

---

## 2. Weekly Curriculum Schedule

### Phase 1: Core Foundations — Algorithms, Languages & SQL (Weeks 1–3)

#### Week 1: Data Structures & Algorithms Core Patterns
* **Target Modules:** [`dsa/`](dsa/README.md), [`dsa/greedy`](dsa/greedy/00_theory.md), [`dsa/trie`](dsa/trie/00_theory.md), [`dsa/Linear-Data-Structures-Masterclass`](dsa/Linear-Data-Structures-Masterclass), [`interviewbit/`](interviewbit/)
* **Focus Topics:**
  * Arrays, Two Pointers, & Sliding Window techniques (`dsa/array`, `dsa/twopointers`, `dsa/sliding_window`).
  * Linked Lists, Stacks, Queues, & Hashing mechanics (`dsa/linkedlist`, `dsa/stack`, `dsa/queue`, `dsa/hashing`).
  * Greedy Algorithms & Formal Proofs: Exchange Arguments, Stays Ahead Proofs, Interval Scheduling, Heap Greedy, & Monotonic Stack (`dsa/greedy`, `interviewbit/greedy`).
  * Trie (Prefix Trees): Array vs HashMap Node Layouts, Bitwise XOR Tries, Reverse Suffix Tries, & Autocomplete Engine Design (`dsa/trie`).
  * Monotonic Stack & Queue applications.
* **Deliverable:** Solve 20 classic medium/hard problem patterns from `dsa/` and `interviewbit/`.

#### Week 2: Advanced Graph Algorithms & Dynamic Programming
* **Target Modules:** [`dsa/Graph-Algorithms-Masterclass`](dsa/Graph-Algorithms-Masterclass), [`dsa/graph`](dsa/graph), [`dsa/dynamicprogramming`](dsa/dynamicprogramming), [`dsa/linesweep`](dsa/linesweep)
* **Focus Topics:**
  * BFS, DFS, Topological Sort (Kahn's algorithm), Dijkstra, Bellman-Ford, & Union-Find / Disjoint Set Union (DSU).
  * 1D/2D Dynamic Programming (Knapsack, LCS, LIS, Interval DP).
  * Line Sweep algorithms for interval overlap and geometry problems (`dsa/linesweep`).
* **Deliverable:** Complete graph traversal & DP state formulation exercises.

#### Week 3: Java Core Internals, Functional Programming, File I/O, Concurrency, Interview Edge Cases & FAANG SQL Masterclass
* **Target Modules:** [`java/`](java/), [`java/classloader/`](java/classloader/README.md), [`java/generics/`](java/generics/README.md), [`java/collections/`](java/collections/README.md), [`java/functional/`](java/functional/README.md), [`java/io/`](java/io/README.md), [`java/concurrency/`](java/concurrency/README.md), [`java/interview/`](java/interview/README.md), [`sql/`](sql/README.md)
* **Focus Topics:**
  * Java Memory Model (`java/memorymodel`), Reflection (`java/reflection`), ClassLoader Architecture & Internals (`java/classloader/README.md`), Generics (`java/generics/README.md`), & Collections (`java/collections/README.md`).
  * JVM Class Loading Lifecycle: Delegation Hierarchy (Parent-First vs Child-First WebApp ClassLoaders), On-The-Fly Bytecode Decryption, Bytecode Instrumentation (`java.lang.instrument`), & Metaspace Leak Diagnostics (`java/classloader`).
  * Functional Programming & Stream API (`java/functional/README.md`): Functional Interfaces, Lambdas & `invokedynamic`, Stream execution pipeline, Collectors (`groupingBy`, `partitioningBy`), Custom `Collector` / `Spliterator`, Monadic `Optional`, & FP design patterns.
  * Modern Java File I/O & NIO.2 (`java/io/README.md`): Streams vs Channels, `MappedByteBuffer` (`mmap`), Kernel Zero-Copy (`transferTo`/`sendfile`), POSIX permissions, `WatchService`, `FileLock`, & Deserialization Security.
  * Java Interview Tricky Gotchas & Edge Cases (`java/interview/README.md`): Integer Cache limits, String Pool, Overload resolution hierarchy, Overridden method calls in super constructors, Exception swallowing in `finally`, `ThreadLocal` leaks, & Staff Interview Puzzle Suite.
  * Threading primitives: Locks, ReentrantLock, Virtual Threads (`java/concurrency/06_virtual_threads_loom.java`), Lock-Free Atomics (`07_lock_free_atomics_aba.java`), Synchronizers (`08_synchronizers_phaser_barrier.java`), ForkJoin Pool (`09_forkjoin_work_stealing.java`), & Flow Reactive Streams (`10_reactive_streams_flow.java`).
  * Advanced SQL syntax (`sql/00_syntax_cheatsheet.md`), Window Functions (`ROW_NUMBER`, `DENSE_RANK`, `ROWS BETWEEN`), & FAANG Staff SQL Interview Patterns (`sql/05_faang_staff_interview_patterns.sql`):
    * Gaps & Islands, User Sessionization (30-min threshold), Cohort Retention, Recursive CTE Org Trees, Exact Medians, & Overlapping Intervals.
* **Deliverable:** Master Java concurrency primitives and solve all 6 FAANG Staff SQL interview patterns.

---

### Phase 2: System Design — Low-Level (LLD) & High-Level (HLD) (Weeks 4–7)

#### Week 4: Object-Oriented Analysis, Design Principles & SOLID
* **Target Modules:** [`lld/README.md`](lld/README.md), [`lld/01_foundations`](lld/01_foundations)
* **Focus Topics:**
  * SOLID Principles (Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion).
  * Encapsulation, Abstraction, Polymorphism, & Composition vs Inheritance trade-offs.
  * Class Diagrams, Sequence Diagrams, & Object-Oriented Domain Modeling.
* **Deliverable:** Diagram and code domain models for complex real-world entities.

#### Week 5: Gang of Four (GoF) Design Patterns in Practice
* **Target Modules:** [`lld/02_design_patterns`](lld/02_design_patterns)
* **Focus Topics:**
  * **Creational:** Singleton, Factory Method, Abstract Factory, Builder, Prototype.
  * **Structural:** Adapter, Composite, Proxy, Decorator, Facade, Bridge, Flyweight.
  * **Behavioral:** Strategy, Observer, Command, State, Chain of Responsibility, Iterator, Mediator, Template Method.
* **Deliverable:** Implement GoF design patterns in production-grade Java code without boilerplate.

#### Week 6: Low-Level System Design (LLD) Production Systems
* **Target Modules:** [`lld/03_system_designs`](lld/03_system_designs)
* **Focus Topics:**
  * Elevator System Design, LRU Cache, Parking Lot, Vending Machine, ATM System, Rate Limiter, & Logging Framework.
  * Concurrency safety, thread-safe data structures, lock granularities, and error handling in LLD implementations.
* **Deliverable:** End-to-end implementation of 5 full LLD interview systems.

#### Week 7: High-Level System Design (HLD) Framework & Core Architectures
* **Target Modules:** [`hld/`](hld/README.md), [`hld/01_system_design_interview_framework.md`](hld/01_system_design_interview_framework.md) to [`hld/07_video_streaming_youtube.md`](hld/07_video_streaming_youtube.md)
* **Focus Topics:**
  * **The 4-Step System Design Interview Framework:** Scope clarification, 99.99% availability math, QPS, 5-year storage capacity math, and 80/20 RAM cache sizing.
  * **System Design 1:** Distributed Unique ID Generator (Twitter Snowflake - 64-bit layout, timestamp epoch, worker bits, clock drift safeguards) (`hld/02_distributed_id_generator_snowflake.md`).
  * **System Design 2:** Distributed Rate Limiter (Token Bucket vs Sliding Window Counter, Redis + Lua scripts) (`hld/03_distributed_rate_limiter.md`).
  * **System Design 3:** Scalable URL Shortener (TinyURL / Base62 vs KGS + ZooKeeper) (`hld/04_url_shortener_tinyurl.md`).
  * **System Design 4:** Distributed Web Crawler (URL Frontier priority/politeness queues, SimHash deduplication) (`hld/05_distributed_web_crawler.md`).
  * **System Design 5:** Real-Time Chat System (WhatsApp / WebSockets, Cassandra history, Redis presence) (`hld/06_chat_messaging_system_whatsapp.md`).
  * **System Design 6:** Video Streaming Platform (YouTube / ABR HLS, Transcoding DAG pipeline, Multi-CDN edge delivery) (`hld/07_video_streaming_youtube.md`).
* **Deliverable:** Master back-of-the-envelope calculations and architect 6 end-to-end HLD systems.

---

### Phase 3: Distributed Systems Theory & Data-Intensive Architecture (Weeks 8–9)

#### Week 8: Foundations of Data-Intensive Applications & Consensus (DDIA)
* **Target Modules:** [`ddia_book_study/`](ddia_book_study/README.md), [`distributed-systems/`](distributed-systems/README.md), [`distributed-systems/01_foundations`](distributed-systems/01_foundations), [`distributed-systems/02_consensus_and_coordination`](distributed-systems/02_consensus_and_coordination)
* **Focus Topics:**
  * Data Models & Storage Engines: B-Trees vs LSM-Trees (WAL, Memtable, SSTable).
  * Encoding Formats: Protocol Buffers, Avro, Thrift, JSON/BSON.
  * CAP Theorem, PACELC, Vector Clocks, & Logical vs Physical Time (Lamport Timestamps, TrueTime).
  * Distributed Consensus: Raft Protocol (Leader Election, Log Replication) & Paxos.
  * Partitioning & Sharding: Range-based vs Hash-based (Consistent Hashing, Murmur3, Token Rings, vnodes).
* **Deliverable:** Complete Raft state transitions and consistent hashing algorithm mechanics.

#### Week 9: Distributed Transactions, Replication & Derived Data
* **Target Modules:** [`distributed-systems/04_replication_and_storage`](distributed-systems/04_replication_and_storage), [`distributed-systems/05_distributed_transactions`](distributed-systems/05_distributed_transactions), [`ddia_book_study/part3_derived_data`](ddia_book_study/part3_derived_data)
* **Focus Topics:**
  * Two-Phase Commit (2PC) & Three-Phase Commit (3PC) Protocols.
  * Saga Pattern (Choreography vs Orchestration) for microservices.
  * Single-Leader, Multi-Leader, & Leaderless Replication (Dynamo-style quorums).
  * Batch Processing (MapReduce, Spark) & Stream Processing (Kafka Streams, Flink).
* **Deliverable:** Compare 2PC vs Saga trade-offs across microservices boundaries.

---

### Phase 4: Staff-Level Infrastructure & Tech-Stack Mastery (Weeks 10–12)

#### Week 10: In-Memory, Relational, Driver & Document Data Engines
* **Target Modules:** [`tech-stack/redis/`](tech-stack/redis/README.md), [`tech-stack/relational-databases/`](tech-stack/relational-databases/README.md), [`tech-stack/jdbc/`](tech-stack/jdbc/README.md), [`tech-stack/hibernate/`](tech-stack/hibernate/README.md), [`tech-stack/mongodb/`](tech-stack/mongodb/README.md)
* **Focus Topics:**
  * Redis single-threaded event loop, RESP protocol, data structures, sentinel, cluster sharding.
  * PostgreSQL/MySQL MVCC, WAL, B-Tree indexes, query optimization (`EXPLAIN ANALYZE`).
  * JDBC Driver Types 1–4, HikariCP `ConcurrentBag` & `FastList`, `PreparedStatement` compilation, `ResultSet` streaming.
  * Hibernate/JPA N+1 select problem, first/second-level caching, dirty checking.
  * MongoDB WiredTiger B-Tree/cache, Replica Set Raft consensus, Oplog, Sharding balancer.
* **Deliverable:** Deep-dive operational mastery of relational, driver, and document databases.

#### Week 11: NoSQL Wide-Column, Search, Object Storage & Messaging
* **Target Modules:** [`tech-stack/cassandra/`](tech-stack/cassandra/README.md), [`tech-stack/elasticsearch/`](tech-stack/elasticsearch/README.md), [`tech-stack/object-storage/`](tech-stack/object-storage/README.md), [`tech-stack/kafka/`](tech-stack/kafka/README.md), [`tech-stack/gateways-proxies-loadbalancers/`](tech-stack/gateways-proxies-loadbalancers/README.md)
* **Focus Topics:**
  * Cassandra masterless P2P ring, Gossip, $\Phi$ Accrual, LSM engine, SSTables, Bloom filters, $R+W>N$ quorums.
  * Elasticsearch Lucene inverted index, FST, FOR/Roaring posting lists, 2-phase search, DocValues vs Fielddata, 32GB JVM heap limit.
  * Distributed Object Storage (S3/Ceph/MinIO) flat namespace, CRUSH algorithm, Reed-Solomon Erasure Coding ($K+M$), Multipart uploads, WORM locks.
  * Kafka distributed log segments, Zero-Copy transfer, consumer group rebalancing, ISR replicas.
  * Gateways & Proxies: NGINX, HAProxy, Envoy, Layer 4 vs Layer 7 load balancing algorithms, TLS termination.
* **Deliverable:** Master wide-column stores, full-text search, object storage, & messaging backbones.

#### Week 12: Containers, Orchestration, Observability & Frameworks
* **Target Modules:** [`tech-stack/docker/`](tech-stack/docker/README.md), [`tech-stack/kubernetes/`](tech-stack/kubernetes/README.md), [`tech-stack/grafana/`](tech-stack/grafana/README.md), [`tech-stack/splunk/`](tech-stack/splunk/README.md), [`tech-stack/spring-boot/`](tech-stack/spring-boot/README.md), [`tech-stack/git/`](tech-stack/git/README.md)
* **Focus Topics:**
  * Docker Linux namespaces, cgroups, Overlay2 storage driver, multi-stage builds.
  * Kubernetes Control Plane (kube-apiserver, etcd, kube-scheduler, kube-controller-manager), Pods, Deployments, Services, CNI networking, Ingress.
  * Grafana & Prometheus metrics dashboarding, TSDB storage engine, Alertmanager.
  * Splunk log indexing pipeline, SPL search processing language.
  * Spring Boot auto-configuration mechanics, IoC container, Bean lifecycle, Actuator diagnostics.
  * Git Directed Acyclic Graph (DAG), Object Store (blob, tree, commit, tag), Packfiles, Rebase vs Merge mechanics.
* **Deliverable:** Complete full-stack infrastructure review and final mock preparation.

---

## 3. Recommended Daily Routine & Study SLA

* **Target Commitment:** 15 – 20 hours per week (2.5 – 3 hours per day).

```
 Daily Routine Breakdown (3 Hours):
 +-----------------------------------------------------------------------------------+
 | Duration | Focus Activity                                                         |
 +----------+------------------------------------------------------------------------+
 | 45 Mins  | Theory & Architecture Reading (Module Chapters / Whitepapers)          |
 | 75 Mins  | Hands-on Coding / Problem Solving / Diagramming (DSA / LLD / HLD / SQL)|
 | 40 Mins  | Operational Diagnostics & System Trade-off Analysis                   |
 | 20 Mins  | Review & Flashcard / Notes Consolidation                               |
 +-----------------------------------------------------------------------------------+
```

---

## 4. Master Module Directory Map

| Domain Folder | Description | Key Modules Included |
| :--- | :--- | :--- |
| **[`dsa/`](dsa/README.md)** | Algorithms & Data Structures | Graph/Linear Masterclasses, Arrays, Greedy, Trie, DP, Line Sweep, Trees, Hashing |
| **[`java/`](java/) & [`concurrency/`](concurrency/)** | Java Core & Concurrency | Collections, Memory Model, Reflection, Generics, Locks, Producer-Consumer |
| **[`sql/`](sql/README.md)** | SQL Query Masterclass | Easy, Medium, Hard, Google-level, & FAANG Staff Interview Patterns (`05_faang_staff_interview_patterns.sql`) |
| **[`lld/`](lld/README.md)** | Low-Level System Design | SOLID Foundations, GoF Design Patterns, 5 Production LLD Systems |
| **[`hld/`](hld/README.md)** | High-Level System Design | 4-Step Framework, Estimation Math, 6 System Design Architectures (Snowflake, Rate Limiter, TinyURL, Web Crawler, Chat, YouTube) |
| **[`ddia_book_study/`](ddia_book_study/README.md)** | Data-Intensive Applications | Storage Engines, Replication, Sharding, Batch/Stream Processing |
| **[`distributed-systems/`](distributed-systems/README.md)** | Distributed Systems Theory | Raft/Paxos Consensus, Consistent Hashing, 2PC Transactions, CAP |
| **[`tech-stack/`](tech-stack/README.md)** | Infrastructure Tech-Stack | 16 Modules (Redis, Postgres, JDBC, Mongo, Cassandra, ES, Object Storage, Kafka, K8s, etc.) |
| **[`interviewbit/`](interviewbit/)** | Problem Practice Sets | Array, Greedy (Gas Station, Majority Element) & real-world interview problem collections |
