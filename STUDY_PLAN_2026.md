# Master Study Plan 2026: Staff & Principal Software Engineer Preparation

Welcome to the **2026 Master Study Plan**. This comprehensive preparation roadmap synthesizes all technical modules across this repository into a structured **13-Week Intensive Curriculum** designed for Staff Software Engineers, Senior Systems Architects, and Tech Leads preparing for top-tier system design, algorithms, infrastructure, database engineering, and operating systems interviews.

---

## 1. Curriculum Architecture & Overview

```mermaid
mindmap
  root((2026 Study Plan))
    Phase 1 Core Foundations
      Data Structures and Algorithms dsa
      CSES Problem Set Roadmap cses
      Advanced Java Core java
      Effective Java 90 Items effective_java
      Java Concurrency in Practice jcip
      SQL Query Masterclass and FAANG Patterns sql
      C Programming A Modern Approach c_programming_a_modern_approach
      The C++ Programming Language cpp_programming_language
      A Tour of C++ tour_of_cpp
      Effective Modern C++ effective_modern_cpp
      C++ Concurrency in Action cpp_concurrency_in_action
      C++ Standard Template Library cpp_stl
      Java vs C++ Mind Map java_vs_cpp
    Phase 2 System Design LLD and HLD
      SOLID Principles and Clean Architecture
      GoF Design Patterns lld
      High Level System Design HLD
      4 Step Framework and Capacity Math
      System Design Interview Vol 1 and 2 system_design_interview
      Fundamentals of Software Architecture fundamentals_of_software_architecture
      Monolith to Microservices monolith_to_microservices
    Phase 3 Systems & Database Engineering
      Operating Systems Three Easy Pieces ostep
      UIUC CS341 System Programming uiuc_cs341_system_programming
      Computer Networks 5 Layer Stack computer_networks
      Database System Concepts dbsc
      Database Internals B-Trees LSM Raft database_internals
      Designing Data Intensive Applications ddia
      Consensus Raft Paxos distributed systems
      Understanding Distributed Systems understanding_distributed_systems
      MIT 6.5840 6.824 Distributed Systems mit_6_5840_distributed_systems
      Stanford CS244B Advanced Distributed Systems stanford_cs244b_distributed_systems
      CMU 15-418 Parallel Computer Architecture cmu_15418_parallel_programming
      Stanford EE382C Advanced Computer Architecture stanford_ee382c_advanced_computer_architecture
      MIT 6.1060 6.172 Performance Engineering mit_61060_performance_engineering
      UC Berkeley CS267 Parallel Computing berkeley_cs267_parallel_computing
      Information Retrieval Search Engines information_retrieval_and_search_engines
      Relevant Search Solr Elasticsearch relevant_search_solr_elasticsearch
      Lucene in Action lucene_in_action
    Phase 4 Infrastructure Tech Stack & Security
      Data Tier Redis Postgres JDBC Storage
      Streaming Kafka Edge Gateways Tomcat Netty
      High Throughput Data Pipelines high_throughput_data_pipelines
      Containers Orchestration Kubernetes
      API Security in Action OWASP OAuth2 SPIFFE api_security_in_action
```

---

## 2. Weekly Curriculum Schedule

### Phase 1: Core Foundations — Algorithms, Languages, Books & SQL (Weeks 1–4)

#### Week 1: Data Structures, Algorithms, CSES Problem Set & C Programming (K. N. King)
* **Target Modules:** [`01_core_foundations/dsa/`](01_core_foundations/dsa/README.md), [`01_core_foundations/dsa/greedy`](01_core_foundations/dsa/greedy/00_theory.md), [`01_core_foundations/dsa/trie`](01_core_foundations/dsa/trie/00_theory.md), [`01_core_foundations/dsa/backtracking`](01_core_foundations/dsa/backtracking/00_theory.md), [`01_core_foundations/cses/`](01_core_foundations/cses/README.md), [`01_core_foundations/interviewbit/`](01_core_foundations/interviewbit/), [`01_core_foundations/c_programming_a_modern_approach/`](01_core_foundations/c_programming_a_modern_approach/README.md)
* **Focus Topics:**
  * Arrays, Two Pointers, & Sliding Window techniques (`01_core_foundations/dsa/array`, `01_core_foundations/dsa/twopointers`, `01_core_foundations/dsa/sliding_window`).
  * Linked Lists, Stacks, Queues, & Hashing mechanics (`01_core_foundations/dsa/linkedlist`, `01_core_foundations/dsa/stack`, `01_core_foundations/dsa/queue`, `01_core_foundations/dsa/hashing`).
  * **C Programming: A Modern Approach (K. N. King)** [`01_core_foundations/c_programming_a_modern_approach/`](01_core_foundations/c_programming_a_modern_approach/README.md):
    * Fixed-width types (`<stdint.h>`), sequence points, side effects, C99 Variable-Length Arrays (VLAs), storage classes & linkage (`static` vs `extern`).
    * Pointers & pointer arithmetic, function pointers (`void (*fn)()` ), `malloc`/`calloc`/`realloc`/`free`, custom Arena/Region allocators, struct alignment padding (`offsetof`), bit-fields, tagged unions.
    * Preprocessor metaprogramming (`#`, `##`, `__VA_ARGS__`), `<stdio.h>` buffered streams, `memcpy` vs `memmove` overlapping safety, C99 designated initializers, `restrict`, C11 `_Generic` type-generic macros.
  * Greedy Algorithms & Formal Proofs: Exchange Arguments, Stays Ahead Proofs, Interval Scheduling, Heap Greedy, & Monotonic Stack (`01_core_foundations/dsa/greedy`, `01_core_foundations/interviewbit/greedy`).
  * Trie (Prefix Trees): Array vs HashMap Node Layouts, Bitwise XOR Tries, Reverse Suffix Tries, & Autocomplete Engine Design (`01_core_foundations/dsa/trie`).
  * Recursion & Backtracking (`01_core_foundations/dsa/backtracking`): State-Space Tree search, Choose-Explore-Unchoose 3-step paradigm, Branch-and-Bound pruning, duplicate handling, N-Queens, Sudoku, & CSES Grid Paths (48-step path counting).
  * CSES Problem Set introductory, sorting, and searching modules (`01_core_foundations/cses/01_introductory`, `01_core_foundations/cses/02_sorting_and_searching`).
* **Deliverable:** Solve 20 classic medium/hard problem patterns from `01_core_foundations/dsa/` and track CSES progress in `01_core_foundations/cses/00_progress_tracker.md`.

#### Week 2: Advanced Graph Algorithms, Dynamic Programming & CSES Mastery
* **Target Modules:** [`01_core_foundations/dsa/Graph-Algorithms-Masterclass`](01_core_foundations/dsa/Graph-Algorithms-Masterclass), [`01_core_foundations/dsa/graph`](01_core_foundations/dsa/graph), [`01_core_foundations/dsa/dynamicprogramming`](01_core_foundations/dsa/dynamicprogramming), [`01_core_foundations/dsa/linesweep`](01_core_foundations/dsa/linesweep), [`01_core_foundations/cses/`](01_core_foundations/cses/README.md)
* **Focus Topics:**
  * BFS, DFS, Topological Sort (Kahn's algorithm), Dijkstra, Bellman-Ford, & Union-Find / Disjoint Set Union (DSU).
  * 1D/2D Dynamic Programming (Knapsack, LCS, LIS, Interval DP).
  * Line Sweep algorithms for interval overlap and geometry problems (`01_core_foundations/dsa/linesweep`).
  * Advanced CSES modules: Dynamic Programming, Graph Algorithms, Range Queries, & Tree Algorithms (`01_core_foundations/cses/03_dynamic_programming`, `01_core_foundations/cses/04_graph_algorithms`, `01_core_foundations/cses/05_range_queries`, `01_core_foundations/cses/06_tree_algorithms`).
* **Deliverable:** Complete graph traversal & DP state formulation exercises across DSA and CSES problem sets.

#### Week 3: Java Internals, Effective Java (90 Items), Java Concurrency in Practice & FAANG SQL Masterclass
* **Target Modules:** [`01_core_foundations/java/`](01_core_foundations/java/), [`01_core_foundations/effective_java/`](01_core_foundations/effective_java/README.md), [`01_core_foundations/java_concurrency_in_practice/`](01_core_foundations/java_concurrency_in_practice/README.md), [`01_core_foundations/sql/`](01_core_foundations/sql/README.md)
* **Focus Topics:**
  * **Effective Java (3rd Edition)** [`01_core_foundations/effective_java/`](01_core_foundations/effective_java/README.md): Master all 12 Chapters and 90 Items:
    * Static Factory Methods, Builder Pattern, Enum Singletons, Try-With-Resources (Items 1–9).
    * `equals()`, `hashCode()`, `toString()`, Copy Constructors over `clone()`, `Comparable` (Items 10–14).
    * Encapsulation, Immutability & Java 17 Records, Composition over Inheritance, Sealed Classes, Static Member Classes (Items 15–25).
    * Raw Types avoidance, Lists vs Arrays, PECS Rule (`Producer Extends Consumer Super`), `@SafeVarargs`, Typesafe Heterogeneous Containers (Items 26–33).
    * Enums, `EnumSet`, `EnumMap`, Functional Interfaces, Streams Best Practices, Side-Effect Free Collectors (Items 34–48).
    * Parameter Validation, Defensive Copies, Method Signatures, Optionals, `BigDecimal`, Exceptions, Serialization (Items 49–90).
  * **Java Concurrency in Practice** [`01_core_foundations/java_concurrency_in_practice/`](01_core_foundations/java_concurrency_in_practice/README.md):
    * Thread Safety, Atomicity, Race Conditions, Reentrancy, Volatile Memory Barriers, `ThreadLocal`, Java Monitor Pattern (`01_fundamentals/`).
    * Executor Framework, Thread Pools, Futures, Interruption Policy, Thread Pool Sizing ($N_{\text{CPU}} \times U_{\text{CPU}} \times (1 + W/C)$), Saturation Policies (`02_structuring_applications/`).
    * Deadlocks, Open Calls, Lock Scope Narrowing, Lock Striping, JMH Microbenchmarking (`03_liveness_performance_testing/`).
    * `ReentrantLock`, `ReadWriteLock`, `StampedLock` Optimistic Reading, Condition Queues, AbstractQueuedSynchronizer (AQS) internal state & CLH queue, Hardware CAS, Treiber Stack, Java Memory Model (JMM) Happens-Before Rules (`04_advanced_topics/`).
    * Modern Java 21 Concurrency: Project Loom Virtual Threads, Carrier OS Threads, Carrier Thread Pinning hazards, `StructuredTaskScope` (`05_modern_java_concurrency/`).
  * **FAANG Staff SQL Masterclass** [`01_core_foundations/sql/`](01_core_foundations/sql/README.md): Window Functions (`ROW_NUMBER`, `DENSE_RANK`, `ROWS BETWEEN`), Gaps & Islands, User Sessionization, Cohort Retention, Recursive CTE Org Trees, & Exact Medians (`01_core_foundations/sql/05_faang_staff_interview_patterns.sql`).
* **Deliverable:** Review all 90 Effective Java items, master AQS & JMM mechanics, and solve all 6 FAANG Staff SQL interview patterns.

---

#### Week 4: Advanced C++, STL, and Modern Concurrency
* **Target Modules:** [`01_core_foundations/cpp_programming_language/`](01_core_foundations/cpp_programming_language/README.md), [`01_core_foundations/tour_of_cpp/`](01_core_foundations/tour_of_cpp/README.md), [`01_core_foundations/effective_modern_cpp/`](01_core_foundations/effective_modern_cpp/README.md), [`01_core_foundations/cpp_concurrency_in_action/`](01_core_foundations/cpp_concurrency_in_action/README.md), [`01_core_foundations/cpp_stl/`](01_core_foundations/cpp_stl/README.md), [`01_core_foundations/java_vs_cpp/`](01_core_foundations/java_vs_cpp/README.md)
* **Focus Topics:**
  * **The C++ Programming Language & A Tour of C++**: Memory model, RAII, move semantics, references vs pointers, `constexpr`, and template metaprogramming basics.
  * **Effective Modern C++**: Smart pointers (`std::unique_ptr`, `std::shared_ptr`), type deduction (`auto`, `decltype`), perfect forwarding (`std::forward`), lambda expressions.
  * **C++ Concurrency in Action**: `std::thread`, `std::atomic`, `memory_order_acquire`/`release`, lock-free queues, and thread pools.
  * **C++ STL**: Sequence/associative containers, iterators, `<algorithm>`, custom allocators, time complexities.
  * **Java vs C++ Mind Map**: Side-by-side comparison of GC vs RAII, generics vs templates, JVM threads vs `std::thread`, interfaces vs virtual functions.
* **Deliverable:** Master C++ memory management, construct STL-compliant custom allocators, implement lock-free concurrency patterns, and thoroughly contrast C++ mechanics with Java.

---

### Phase 2: System Design — Low-Level (LLD) & High-Level (HLD) (Weeks 5–8)

#### Week 5: Object-Oriented Analysis, Design Principles & SOLID
* **Target Modules:** [`02_system_design/lld/README.md`](02_system_design/lld/README.md), [`02_system_design/lld/01_foundations`](02_system_design/lld/01_foundations)
* **Focus Topics:**
  * SOLID Principles (Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion).
  * Encapsulation, Abstraction, Polymorphism, & Composition vs Inheritance trade-offs.
  * Class Diagrams, Sequence Diagrams, & Object-Oriented Domain Modeling.
* **Deliverable:** Diagram and code domain models for complex real-world entities.

#### Week 6: Gang of Four (GoF) Design Patterns in Practice
* **Target Modules:** [`02_system_design/lld/02_design_patterns`](02_system_design/lld/02_design_patterns)
* **Focus Topics:**
  * **Creational:** Singleton, Factory Method, Abstract Factory, Builder, Prototype.
  * **Structural:** Adapter, Composite, Proxy, Decorator, Facade, Bridge, Flyweight.
  * **Behavioral:** Strategy, Observer, Command, State, Chain of Responsibility, Iterator, Mediator, Template Method.
* **Deliverable:** Implement GoF design patterns in production-grade Java code without boilerplate.

#### Week 7: Low-Level System Design (LLD) Production Systems
* **Target Modules:** [`02_system_design/lld/03_system_designs`](02_system_design/lld/03_system_designs)
* **Focus Topics:**
  * Elevator System Design, LRU Cache, Parking Lot, Vending Machine, ATM System, Rate Limiter, & Logging Framework.
  * Concurrency safety, thread-safe data structures, lock granularities, and error handling in LLD implementations.
* **Deliverable:** End-to-end implementation of 5 full LLD interview systems.

#### Week 8: High-Level System Design (HLD), Alex Xu (Vol 1 & 2), Software Architecture & Microservices Refactoring
* **Target Modules:** [`02_system_design/hld/`](02_system_design/hld/README.md), [`02_system_design/system_design_interview/`](02_system_design/system_design_interview/README.md), [`02_system_design/fundamentals_of_software_architecture/`](02_system_design/fundamentals_of_software_architecture/README.md), [`02_system_design/monolith_to_microservices/`](02_system_design/monolith_to_microservices/README.md)
* **Focus Topics:**
  * **The 4-Step System Design Interview Framework:** Scope clarification, 99.99% availability math, QPS, 5-year storage capacity math, and 80/20 RAM cache sizing.
  * **System Design Interview – An Insider's Guide (Vol 1 & 2)** [`02_system_design/system_design_interview/`](02_system_design/system_design_interview/README.md):
    * Rate Limiter, Consistent Hashing with V-Nodes, Dynamo-Style Quorum KV Store, Twitter Snowflake 64-bit ID, TinyURL, Web Crawler URL Frontier, Trie Autocomplete, Real-Time WebSocket Chat, YouTube Transcoding DAG, Google Drive Chunking.
    * Proximity Service (Geohash / Quadtree / S2), Google Maps Routing A*, Distributed Message Queue (Kafka Broker & Zero-Copy), Metrics Monitoring (Prometheus TSDB), Ad Click Aggregation (MapReduce), Hotel Reservation Optimistic Locking, Payment Double-Entry Ledger, Digital Wallet, Stock Exchange Matching Engine (LMAX Disruptor Ring Buffer).
  * **Fundamentals of Software Architecture** [`02_system_design/fundamentals_of_software_architecture/`](02_system_design/fundamentals_of_software_architecture/README.md):
    * Architecture vs Design, 4 Expectations of an Architect, Measuring Characteristics ("-ilities"), Afferent ($C_a$) / Efferent ($C_e$) Coupling & Instability ($I = \frac{C_e}{C_a + C_e}$), Distance from Main Sequence ($D = |A + I - 1|$).
    * Architecture Styles: Monolithic (Layered, Pipeline, Microkernel/Plugin) vs Distributed (Service-Based, Event-Driven Broker vs Mediator, Space-Based, Microservices).
    * Governance & Techniques: Architectural Decision Records (ADR format), Automated Architectural Fitness Functions (ArchUnit CI/CD gates), Risk Analysis Matrix.
  * **Monolith to Microservices (Sam Newman)** [`02_system_design/monolith_to_microservices/`](02_system_design/monolith_to_microservices/README.md):
    * Strangler Fig pattern, Branch by Abstraction, Parallel Run verification, UI Micro Frontends.
    * 5-Step Database Split Pattern, Transactional Outbox Pattern, Change Data Capture (CDC / Debezium), Saga pattern.
    * API Gateway Canary Traffic Routing, Protobuf/Avro schema evolution, Cross-boundary distributed tracing context propagation.
* **Deliverable:** Master back-of-the-envelope calculations, architect all 28 Alex Xu systems, implement ArchUnit fitness functions, and execute non-breaking monolithic database refactoring strategies.

---

### Phase 3: Systems, Networking, Database Engineering & Distributed Systems (Weeks 9–10)

#### Week 9: Operating Systems (OSTEP), UIUC CS341 System Programming & Computer Networks (5-Layer Stack)
* **Target Modules:** [`03_systems_and_databases/ostep/`](03_systems_and_databases/ostep/README.md), [`03_systems_and_databases/uiuc_cs341_system_programming/`](03_systems_and_databases/uiuc_cs341_system_programming/README.md), [`03_systems_and_databases/computer_networks/`](03_systems_and_databases/computer_networks/README.md)
* **Focus Topics:**
  * **Operating Systems: Three Easy Pieces (OSTEP)** [`03_systems_and_databases/ostep/`](03_systems_and_databases/ostep/README.md):
    * **Virtualization**: Process API (`fork`, `exec`, `wait`), PCB (`struct proc`), Context Switching, Limited Direct Execution (LDE) Protocol, FIFO, SJF, STCF, Round Robin, Multi-Level Feedback Queue (MLFQ) 5 rules, Stride Scheduling, Base & Bound, Multi-Level Page Tables, Hardware TLB, Swap Space, Page Fault Handler, Clock Second-Chance Algorithm, Thrashing (`01_virtualization/`).
    * **Concurrency**: POSIX Threads (`pthread`), Mutual Exclusion, Hardware TAS/CAS, Linux Futex (`sys_futex`) two-phase locks, Condition Variables (`pthread_cond_wait`), Bounded Buffer Producer-Consumer, Semaphores (`sem_t`), Reader-Writer Locks, Concurrency Bugs (Atomicity/Order violations, Deadlock 4 conditions), Event-Based Concurrency & Linux `epoll` (`02_concurrency/`).
    * **Persistence**: I/O Controllers, Polling vs Interrupts, DMA (Direct Memory Access), HDD Scheduling (SSTF, SCAN, C-SCAN), SSD NAND Flash, Flash Translation Layer (FTL), Wear Leveling, File APIs (`open`, `read`, `write`, `fsync`), Inode Structure, Slotted-Page Layout, Very Simple File System (VSFS), Crash Consistency, Write-Ahead Logging (WAL / Journaling), Log-Structured File Systems (LFS) (`03_persistence/`).
  * **UIUC CS341 System Programming** [`03_systems_and_databases/uiuc_cs341_system_programming/`](03_systems_and_databases/uiuc_cs341_system_programming/README.md):
    * Asynchronous learning via course book and pre-recorded videos.
    * Lecture handouts and example code.
  * **Computer Networks (Top-Down Approach)** [`03_systems_and_databases/computer_networks/`](03_systems_and_databases/computer_networks/README.md):
    * **Application Layer**: HTTP/1.1 Pipelining vs HTTP/2 Binary Streams vs HTTP/3 QUIC (UDP), DNS Hierarchy & Iterative/Recursive Resolution, CDN Edge Networks, Socket Programming in C & Python (TCP Concurrent Server vs UDP Echo Server) (`01_application_layer/`).
    * **Transport Layer**: UDP Checksum, Reliable Data Transfer (RDT 3.0, Go-Back-N, Selective Repeat), TCP Header Specs, 3-Way Handshake & 4-Way Teardown State Machine, Flow Control (`rwnd`), Congestion Control (Slow Start, Congestion Avoidance, Fast Retransmit, Fast Recovery, AIMD, TCP Tahoe vs Reno vs BBR) (`02_transport_layer/`).
    * **Network Layer Data Plane**: Router Architecture, Longest Prefix Match (LPM) Trie, IPv4/IPv6 Headers, CIDR Subnetting Math, NAT Traversal, DHCP, ICMP (`03_network_layer_data_plane/`).
    * **Network Layer Control Plane**: Link-State Routing (Dijkstra), Distance-Vector Routing (Bellman-Ford & Poison Reverse), Hierarchical AS Routing, Intra-AS (OSPF), Inter-AS (BGP Path Vector, AS-PATH, Next-Hop), Software-Defined Networking (SDN OpenFlow) (`04_network_layer_control_plane/`).
    * **Link & Physical Layer**: Framing, Error Detection (CRC Modulo-2 Math), Multiple Access (CSMA/CD Ethernet Exponential Backoff, CSMA/CA WiFi 802.11 RTS/CTS), Address Resolution Protocol (ARP), L2 Self-Learning Switches vs L3 Routers, VLANs (`05_link_and_physical_layer/`).
* **Deliverable:** Master kernel syscalls, LDE protocol, Linux `epoll`, TCP state transitions, CIDR subnetting, and BGP routing mechanics.

#### Week 10: Database System Concepts, Database Internals, Distributed Systems, UDS, MIT 6.5840, Stanford CS244B, CMU 15-418, Stanford EE382C, MIT 6.1060, UC Berkeley CS267, Information Retrieval, Relevant Search & Lucene in Action
* **Target Modules:** [`03_systems_and_databases/database_system_concepts/`](03_systems_and_databases/database_system_concepts/README.md), [`03_systems_and_databases/database_internals/`](03_systems_and_databases/database_internals/README.md), [`03_systems_and_databases/ddia_book_study/`](03_systems_and_databases/ddia_book_study/README.md), [`03_systems_and_databases/distributed-systems/`](03_systems_and_databases/distributed-systems/README.md), [`03_systems_and_databases/understanding_distributed_systems/`](03_systems_and_databases/understanding_distributed_systems/README.md), [`03_systems_and_databases/mit_6_5840_distributed_systems/`](03_systems_and_databases/mit_6_5840_distributed_systems/README.md), [`03_systems_and_databases/stanford_cs244b_distributed_systems/`](03_systems_and_databases/stanford_cs244b_distributed_systems/README.md), [`03_systems_and_databases/cmu_15418_parallel_programming/`](03_systems_and_databases/cmu_15418_parallel_programming/README.md), [`03_systems_and_databases/stanford_ee382c_advanced_computer_architecture/`](03_systems_and_databases/stanford_ee382c_advanced_computer_architecture/README.md), [`03_systems_and_databases/mit_61060_performance_engineering/`](03_systems_and_databases/mit_61060_performance_engineering/README.md), [`03_systems_and_databases/berkeley_cs267_parallel_computing/`](03_systems_and_databases/berkeley_cs267_parallel_computing/README.md), [`03_systems_and_databases/information_retrieval_and_search_engines/`](03_systems_and_databases/information_retrieval_and_search_engines/README.md), [`03_systems_and_databases/relevant_search_solr_elasticsearch/`](03_systems_and_databases/relevant_search_solr_elasticsearch/README.md), [`03_systems_and_databases/lucene_in_action/`](03_systems_and_databases/lucene_in_action/README.md)
* **Focus Topics:**
  * **Database System Concepts (DBSC)** [`03_systems_and_databases/database_system_concepts/`](03_systems_and_databases/database_system_concepts/README.md):
    * **Relational Model & SQL**: Relational Algebra ($\sigma, \pi, \bowtie, \div$), SQL AST Parsing, Logical & Physical Execution Tree, Triggers, Views (`01_relational_model_and_sql/`).
    * **Database Normalization**: Functional Dependencies ($F^+$), Canonical Cover, 1NF, 2NF, 3NF, BCNF, 4NF, Lossless-Join Decomposition Proofs (`02_database_design_and_normalization/`).
    * **Storage & Indexing**: Slotted-Page Page Architecture, Buffer Pool Manager (LRU/Clock, Pin Count, Dirty Pages), B+ Tree Search/Insert/Delete & Node Splitting, Dynamic Hash Indexing (`03_storage_and_indexing/`).
    * **Query Execution & Optimization**: Volcano Iterator Model (`open`/`next`/`close`), External Merge Sort, Join Algorithms (Nested Loop, Hash Join, Grace Hash Join, Sort-Merge Join), System R Cost-Based Dynamic Programming Optimizer (`04_query_processing_and_optimization/`).
    * **Concurrency Control**: ACID Properties, Conflict Serializability, Precedence Graphs, Lock-Based Protocols (Shared/Exclusive Locks, 2PL, Strict 2PL, Rigorous 2PL), Multiple Granularity Intent Locks (IS, IX, SIX), Multiversion Concurrency Control (MVCC) Version Chains, Deadlocks & Wait-For Graphs (`05_transactions_and_concurrency/`).
    * **Recovery System**: Write-Ahead Logging (WAL), Steal/No-Force Policies, ARIES 3-Phase Recovery (Analysis, Redo / Repeating History, Undo / CLRs), Fuzzy Checkpointing (`06_recovery_system/`).
  * **Database Internals (Alex Petrov)** [`03_systems_and_databases/database_internals/`](03_systems_and_databases/database_internals/README.md):
    * **Storage Engines**: $B^{\text{link}}$ Trees with right-sibling pointers, Latching Crabbing (Coupled Latching), LSM-Tree Architecture, MemTable SkipList, WAL, SSTable Binary Format (Data, Index, Bloom, Summary, Footer), Bloom Filter Math ($k = \frac{m}{n} \ln 2$), Size-Tiered vs Leveled Compaction (LCS), Read/Write/Space Amplification trade-offs (`01_storage_engines/`).
    * **Distributed Storage & Consensus**: Leader/Leaderless Replication, Quorum Consistency ($R + W > N$, Read Repair, Hinted Handoff), Vector Clocks, Hybrid Logical Clocks (HLC), Raft Consensus Deep Dive (Leader Election, RequestVote, AppendEntries, Log Matching Property, Safety Invariants) (`02_distributed_storage/`).
    * **Distributed Transactions & Isolation**: Two-Phase Commit (2PC), Three-Phase Commit (3PC), Distributed Snapshot Isolation, Google Spanner Architecture & TrueTime API ($\epsilon$ uncertainty, Commit Wait Rule), Deterministic Transaction Engines (Calvin Architecture) (`03_distributed_transactions/`).
  * **Understanding Distributed Systems (Roberto Vitillo)** [`03_systems_and_databases/understanding_distributed_systems/`](03_systems_and_databases/understanding_distributed_systems/README.md):
    * TCP/UDP, HTTP/2 multiplexing, gRPC & Protobuf vs JSON serialization.
    * Physical Clocks (NTP, TrueTime) vs Logical Clocks (Lamport Timestamps, Vector Clocks).
    * Consistent Hashing with Virtual Nodes & Rendezvous Hashing.
    * Replication topologies, Raft Consensus state machine & log matching invariants.
    * Resiliency patterns: Circuit Breakers (CLOSED, OPEN, HALF_OPEN), Exponential Backoff with Decorrelated Jitter, Bulkheads, Idempotency keys.
    * Distributed Transactions (2PC vs Saga Orchestration/Choreography), Distributed Observability (Prometheus Metrics, W3C `traceparent` tracing context).
  * **MIT 6.5840 (6.824) Distributed Systems (Robert Morris)** [`03_systems_and_databases/mit_6_5840_distributed_systems/`](03_systems_and_databases/mit_6_5840_distributed_systems/README.md):
    * MapReduce Coordinator/Worker architecture, VMware FT Primary-Backup Deterministic Replay & Output Rule.
    * Raft Consensus State Machine (Labs 2 & 3: Leader Election, Log Replication, Persistence, Snapshots).
    * Fault-Tolerant Key-Value Service with Duplicate Request Table (`ClientId` + `SeqNum`).
    * Sharded KV Service (Lab 4: Shard Controller Reconfigurations & Cross-Group Data Migration).
    * Apache ZooKeeper (Zab protocol, Linearizable writes, FIFO client order) & Google Spanner (TrueTime, 2PC + Paxos).
  * **Stanford CS244B Advanced Distributed Systems** [`03_systems_and_databases/stanford_cs244b_distributed_systems/`](03_systems_and_databases/stanford_cs244b_distributed_systems/README.md):
    * Chord DHT Finger Tables ($O(\log N)$ routing, stabilize, fix_fingers) & Kademlia XOR metric routing ($d(x,y) = x \oplus y$, k-buckets, parallel $\alpha=3$ lookups).
    * Practical Byzantine Fault Tolerance (PBFT: Pre-Prepare, Prepare, Commit 3-phase algorithm, $f < \frac{N-1}{3}$) & Stellar Federated Byzantine Agreement (FBA: Quorum Slices, Quorum Intersection).
    * Total Order Broadcast via Vector Clocks & Shamir's Secret Sharing ($(k,n)$ threshold scheme, Lagrange Polynomial Interpolation).
  * **CMU 15-418 / 618 Parallel Computer Architecture** [`03_systems_and_databases/cmu_15418_parallel_programming/`](03_systems_and_databases/cmu_15418_parallel_programming/README.md):
    * Multi-core SIMD vectorization, CUDA GPU SIMT execution model (Warp Divergence, Shared Memory Bank Conflicts), Work-Stealing schedulers (Cilk deque, C++ engine).
    * Hardware Cache Coherence Protocols: MESI & MOESI 4/5-state machines, Invalidating vs Updating, Bus Snooping vs Directory-Based Coherence, Memory Consistency Models (Sequential Consistency, Total Store Order / TSO, Relaxed Consistency, Memory Barriers).
    * Interconnect Topologies (Crossbar, 2D Torus, Hypercube, Fat-Tree bisection bandwidth) & Lock-Free Data Structures (Michael-Scott Lock-Free Queue with CAS & ABA prevention via generational pointers).
  * **Stanford EE382C Advanced Computer Architecture** [`03_systems_and_databases/stanford_ee382c_advanced_computer_architecture/`](03_systems_and_databases/stanford_ee382c_advanced_computer_architecture/README.md):
    * Out-of-order superscalar microarchitecture: Tomasulo's Algorithm with Register Alias Table (RAT), Reorder Buffer (ROB) in-order commit, Load-Store Queue (LSQ) memory disambiguation, TAGE Branch Predictor.
    * Memory hierarchy & coherence: Non-blocking caches with Miss Status Holding Registers (MSHRs), Stride/Markov hardware prefetching, Scalable Directory-based Cache Coherence protocols.
    * Interconnects & AI Accelerators: William Dally Wormhole Routing, Virtual Channels (VCs), Credit-based Flow Control, Systolic Arrays (Output vs Weight Stationary GEMM), TPU matrix units, HBM3/CXL/NVLink.
  * **MIT 6.1060 (6.172) Performance Engineering of Software Systems** [`03_systems_and_databases/mit_61060_performance_engineering/`](03_systems_and_databases/mit_61060_performance_engineering/README.md):
    * Bit Leapery & compiler vectorization: Branchless programming, SWAR popcount, GCC/Clang diagnostic flags (`-O3`, `-march=native`), AVX2/AVX-512 FMA vector intrinsics (`_mm256_fmadd_ps`).
    * Cache blocking & Cache-Oblivious algorithms: Stride access penalties, Matrix Tiling, Recursive Divide & Conquer matrix multiplication ($O(N^3/\sqrt{Z})$ optimal cache misses across all hierarchy levels).
    * Cilk Work/Span model & profiling: DAG strand model, Work ($T_1$), Span ($T_\infty$), Greedy scheduler ($T_P \le T_1/P + T_\infty$), Cilksan SP-Bags data race detection algorithm, Linux `perf` & Flame Graphs.
  * **UC Berkeley CS267 Applications of Parallel Computers** [`03_systems_and_databases/berkeley_cs267_parallel_computing/`](03_systems_and_databases/berkeley_cs267_parallel_computing/README.md):
    * Programming models: Distributed-memory MPI (ISend/IRecv halo exchange), OpenMP work-sharing, PGAS (UPC++) one-sided Remote Memory Access (RMA).
    * Communication-avoiding algorithms: 2D SUMMA & Cannon's algorithms ($O(N^2/\sqrt{P})$ communication lower bounds), Compressed Sparse Row (CSR) SpMV, Parallel Conjugate Gradient (CG) solver.
    * Spatial & Graph algorithms: Barnes-Hut $O(N \log N)$ Octree spatial decomposition, Graph 500 parallel BFS ($y = A^T x$ semiring vector-matrix multiply), Space-Filling curves (Hilbert / Morton Z-order) & METIS graph partitioning.
  * **Information Retrieval & Search Engines (Manning IIR + Büttcher)** [`03_systems_and_databases/information_retrieval_and_search_engines/`](03_systems_and_databases/information_retrieval_and_search_engines/README.md):
    * Indexing & compression: Positional Inverted Index, Skip Pointers ($O(\sqrt{P})$ posting intersection), Variable Byte (VB) & Elias Gamma $d$-gap codecs.
    * Scoring & evaluation: Vector Space Model, Okapi BM25 ranking ($k_1, b$ saturation & length normalization), WAND query pruning, MAP, NDCG@K, MRR metrics.
    * Crawling & link analysis: PageRank Power Iteration ($\alpha=0.85$), Web Crawler URL Frontier, 64-bit SimHash near-duplicate detection, Learning to Rank (LambdaMART).
  * **Relevant Search: Solr & Elasticsearch (Turnbull & Berryman)** [`03_systems_and_databases/relevant_search_solr_elasticsearch/`](03_systems_and_databases/relevant_search_solr_elasticsearch/README.md):
    * Query parsing & boosting: Filter vs Scoring boundaries, DisMax `best_fields` tie-breaking ($0.3$), `cross_fields` matching, Function Score Gaussian decay functions.
    * Phrase slop & hybrid search: Match phrase `slop` proximity, Query-time `synonym_graph` expansion, Dense Vector k-NN + BM25 Hybrid Search via Reciprocal Rank Fusion (RRF).
    * Relevance analytics: QuePID judgment collections, Position Bias DBN click models, Automated CI/CD relevance regression test gates.
  * **Lucene in Action (McCandless et al.)** [`03_systems_and_databases/lucene_in_action/`](03_systems_and_databases/lucene_in_action/README.md):
    * Directory & Segment Lifecycle: `MMapDirectory` zero-copy mmap kernel page cache, `IndexWriter` buffer flushing, `TieredMergePolicy`, Commit points (`segments_N`), Near-Real-Time (NRT) Readers.
    * Analysis & BKD Points: `Analyzer`, `Tokenizer`, `TokenFilter` chain, Reusable `TokenStream` attributes (`CharTermAttribute`, `PayloadAttribute`), Block K-d (BKD) Tree spatial indexing.
    * Searchers, Collectors & DocValues: `IndexSearcher`, `Weight`, `Scorer` iterator (`nextDoc()`), Custom `Collector` API (`TopScoreDocCollector`), Columnar `DocValues` (`SortedDocValues`), Taxonomy & SortedSet Faceting.
  * **Distributed Systems Theory & DDIA** [`03_systems_and_databases/distributed-systems/`](03_systems_and_databases/distributed-systems/README.md), [`03_systems_and_databases/ddia_book_study/`](03_systems_and_databases/ddia_book_study/README.md): CAP Theorem, PACELC Theorem, Consistent Hashing (Murmur3, Token Rings, vnodes), 2PC vs Saga Pattern, Batch Processing (MapReduce, Spark) & Stream Processing (Kafka Streams, Flink).
* **Deliverable:** Master B+ Tree vs LSM-Tree trade-offs, Volcano execution, ARIES recovery, Raft consensus, Spanner TrueTime, PBFT phase transitions, Kademlia XOR routing, MESI/MOESI cache coherence, Tomasulo OoO ROB commit, Wormhole VC routing, Cache-Oblivious divide & conquer, Cilk Work/Span SP-Bags race detection, 2D SUMMA communication bounds, Barnes-Hut Octree force evaluation, Okapi BM25 scoring, NDCG@K search evaluation, PageRank power iteration, DisMax tie-breaking, Reciprocal Rank Fusion, Lucene segment merging, BKD spatial queries, Columnar DocValues faceting, and complete MIT 6.5840 Labs 1–4 implementations.

---

### Phase 4: Staff-Level Infrastructure & Tech-Stack Mastery (Weeks 11–13)

#### Week 11: In-Memory, Relational, Driver & Document Data Engines
* **Target Modules:** [`04_infrastructure/tech-stack/redis/`](04_infrastructure/tech-stack/redis/README.md), [`04_infrastructure/tech-stack/relational-databases/`](04_infrastructure/tech-stack/relational-databases/README.md), [`04_infrastructure/tech-stack/jdbc/`](04_infrastructure/tech-stack/jdbc/README.md), [`04_infrastructure/tech-stack/hibernate/`](04_infrastructure/tech-stack/hibernate/README.md), [`04_infrastructure/tech-stack/mongodb/`](04_infrastructure/tech-stack/mongodb/README.md)
* **Focus Topics:**
  * Redis single-threaded event loop, RESP protocol, data structures, sentinel, cluster sharding.
  * PostgreSQL/MySQL MVCC, WAL, B-Tree indexes, query optimization (`EXPLAIN ANALYZE`).
  * JDBC Driver Types 1–4, HikariCP `ConcurrentBag` & `FastList`, `PreparedStatement` compilation, `ResultSet` streaming.
  * Hibernate/JPA N+1 select problem, first/second-level caching, dirty checking.
  * MongoDB WiredTiger B-Tree/cache, Replica Set Raft consensus, Oplog, Sharding balancer.
* **Deliverable:** Deep-dive operational mastery of relational, driver, and document databases.

#### Week 12: NoSQL Wide-Column, Search, Object Storage, Messaging & High-Throughput Pipelines
* **Target Modules:** [`04_infrastructure/tech-stack/cassandra/`](04_infrastructure/tech-stack/cassandra/README.md), [`04_infrastructure/tech-stack/elasticsearch/`](04_infrastructure/tech-stack/elasticsearch/README.md), [`04_infrastructure/tech-stack/object-storage/`](04_infrastructure/tech-stack/object-storage/README.md), [`04_infrastructure/tech-stack/kafka/`](04_infrastructure/tech-stack/kafka/README.md), [`04_infrastructure/tech-stack/gateways-proxies-loadbalancers/`](04_infrastructure/tech-stack/gateways-proxies-loadbalancers/README.md), [`04_infrastructure/high_throughput_data_pipelines/`](04_infrastructure/high_throughput_data_pipelines/README.md)
* **Focus Topics:**
  * Cassandra masterless P2P ring, Gossip, $\Phi$ Accrual, LSM engine, SSTables, Bloom filters, $R+W>N$ quorums.
  * Elasticsearch Lucene inverted index, FST, FOR/Roaring posting lists, 2-phase search, DocValues vs Fielddata, 32GB JVM heap limit.
  * Distributed Object Storage (S3/Ceph/MinIO) flat namespace, CRUSH algorithm, Reed-Solomon Erasure Coding ($K+M$), Multipart uploads, WORM locks.
  * Kafka distributed log segments, Zero-Copy transfer, consumer group rebalancing, ISR replicas.
  * Gateways & Proxies: NGINX, HAProxy, Envoy, Layer 4 vs Layer 7 load balancing algorithms, TLS termination.
  * High-Throughput Data Pipelines: Lambda vs Kappa Architecture, Kafka producer batch tuning (`batch.size`, `linger.ms`, `snappy`), Flink stream DAG & windowing (Tumbling/Sliding/Session), Parquet columnar storage (Dictionary, RLE, Bit-packing), Linux kernel zero-copy (`sendfile`/`splice`), Chandy-Lamport checkpoint barriers, Flink 2PC sink, and Key Salting for partition skew mitigation.
* **Deliverable:** Master wide-column stores, full-text search, object storage, messaging backbones, and 1M+ QPS data pipeline engineering.

---

#### Week 13: Containers, Orchestration, Observability, Frameworks & API Security
* **Target Modules:** [`04_infrastructure/tech-stack/docker/`](04_infrastructure/tech-stack/docker/README.md), [`04_infrastructure/tech-stack/kubernetes/`](04_infrastructure/tech-stack/kubernetes/README.md), [`04_infrastructure/tech-stack/grafana/`](04_infrastructure/tech-stack/grafana/README.md), [`04_infrastructure/tech-stack/splunk/`](04_infrastructure/tech-stack/splunk/README.md), [`04_infrastructure/tech-stack/spring-boot/`](04_infrastructure/tech-stack/spring-boot/README.md), [`04_infrastructure/tech-stack/git/`](04_infrastructure/tech-stack/git/README.md), [`04_infrastructure/api_security_in_action/`](04_infrastructure/api_security_in_action/README.md)
* **Focus Topics:**
  * Docker Linux namespaces, cgroups, Overlay2 storage driver, multi-stage builds.
  * Kubernetes Control Plane (kube-apiserver, etcd, kube-scheduler, kube-controller-manager), Pods, Deployments, Services, CNI networking, Ingress.
  * Grafana & Prometheus metrics dashboarding, TSDB storage engine, Alertmanager.
  * Splunk log indexing pipeline, SPL search processing language.
  * Spring Boot auto-configuration mechanics, IoC container, Bean lifecycle, Actuator diagnostics.
  * Git Directed Acyclic Graph (DAG), Object Store (blob, tree, commit, tag), Packfiles, Rebase vs Merge mechanics.
  * API Security in Action: OWASP API Top 10 (BOLA/BFLA), Sliding Window Redis rate limiting, OAuth 2.1 / PKCE, OIDC, JWT vs Macaroons, TLS 1.3 & mTLS, HTTP Message Signing, RFC 8693 Token Exchange, SPIFFE/SPIRE Zero Trust, Envoy & OPA sidecar authorization.
* **Deliverable:** Complete full-stack infrastructure & API security review and final mock preparation.

---

## 3. Recommended Daily Routine & Study SLA

* **Target Commitment:** 15 – 20 hours per week (2.5 – 3 hours per day).

```
 Daily Routine Breakdown (3 Hours):
 +-----------------------------------------------------------------------------------+
 | Duration | Focus Activity                                                         |
 +----------+------------------------------------------------------------------------+
 | 45 Mins  | Theory & Architecture Reading (Book Modules / System Call Specs)       |
 | 75 Mins  | Hands-on Coding / Problem Solving / Diagramming (DSA / LLD / HLD / SQL)|
 | 40 Mins  | Operational Diagnostics & System Trade-off Analysis                   |
 | 20 Mins  | Review & Flashcard / Notes Consolidation                               |
 +-----------------------------------------------------------------------------------+
```

---

## 4. Master Module Directory Map

| Domain Folder | Description | Key Modules Included |
| :--- | :--- | :--- |
| **[`01_core_foundations/dsa/`](01_core_foundations/dsa/README.md)** | Algorithms & Data Structures | Graph/Linear Masterclasses, Arrays, Greedy, Trie, Backtracking, DP, Line Sweep |
| **[`01_core_foundations/cses/`](01_core_foundations/cses/README.md)** | Competitive Programming Tracker | CSES Problem Set 300+ problem roadmap & progress tracker (`00_progress_tracker.md`) |
| **[`01_core_foundations/java/`](01_core_foundations/java/README.md)** | Advanced Java Core | Collections, Memory Model, ClassLoader, Reflection, Generics, Functional, File I/O, IoC & Servlet Containers, Edge Cases |
| **[`01_core_foundations/effective_java/`](01_core_foundations/effective_java/README.md)** | Effective Java (3rd Edition) | 12 Chapters, 90 Item Guides, Modern Java 11-21 updates & 30-Second Cheatsheet |
| **[`01_core_foundations/java_concurrency_in_practice/`](01_core_foundations/java_concurrency_in_practice/README.md)** | Java Concurrency in Practice | 16 Chapters across 5 Parts: Thread Safety, Executors, AQS, JMM, Virtual Threads, Mermaid Diagrams |
| **[`03_systems_and_databases/ostep/`](03_systems_and_databases/ostep/README.md)** | Operating Systems: Three Easy Pieces | Virtualization (LDE, MLFQ, Paging, TLB), Concurrency (Threads, Futex, Epoll), Persistence (DMA, VSFS, WAL) |
| **[`03_systems_and_databases/uiuc_cs341_system_programming/`](03_systems_and_databases/uiuc_cs341_system_programming/README.md)** | UIUC CS341 System Programming | Coursebook, Pre-recorded Videos, Lecture Handouts, and System Programming concepts |
| **[`03_systems_and_databases/computer_networks/`](03_systems_and_databases/computer_networks/README.md)** | Computer Networks | 5 TCP/IP Layers: HTTP/QUIC, TCP/UDP Flow & Congestion Control, IP/CIDR/NAT, OSPF/BGP, Ethernet/WiFi/ARP |
| **[`03_systems_and_databases/database_system_concepts/`](03_systems_and_databases/database_system_concepts/README.md)** | Database System Concepts | 7 Core DB Pillars: Relational Algebra, Normalization, B+ Trees, Cost Optimizer, 2PL/MVCC, ARIES, 2PC/LSM |
| **[`03_systems_and_databases/database_internals/`](03_systems_and_databases/database_internals/README.md)** | Database Internals | Storage Engines ($B^{\text{link}}$ Trees, LSM/Compaction), Distributed Consensus (Raft/Paxos), Spanner TrueTime, Calvin |
| **[`01_core_foundations/sql/`](01_core_foundations/sql/README.md)** | SQL Query Masterclass | Easy, Medium, Hard, Google-level, & FAANG Staff Interview Patterns (`05_faang_staff_interview_patterns.sql`) |
| **[`02_system_design/lld/`](02_system_design/lld/README.md)** | Low-Level System Design | SOLID Foundations, GoF Design Patterns, 5 Production LLD Systems |
| **[`02_system_design/hld/`](02_system_design/hld/README.md)** | High-Level System Design | 4-Step Framework, Estimation Math, 6 System Design Architectures (Snowflake, Rate Limiter, TinyURL, Web Crawler, Chat, YouTube) |
| **[`02_system_design/system_design_interview/`](02_system_design/system_design_interview/README.md)** | System Design Interview (Vol 1 & 2) | Alex Xu 28 System Designs: Rate Limiter, Snowflake, Geohash, Kafka, Metrics, Payment Ledger, Stock Exchange |
| **[`02_system_design/fundamentals_of_software_architecture/`](02_system_design/fundamentals_of_software_architecture/README.md)** | Fundamentals of Software Architecture | Architecture Characteristics ("-ilities"), Coupling Math, Monolithic vs Distributed Styles, ADRs, ArchUnit |
| **[`02_system_design/monolith_to_microservices/`](02_system_design/monolith_to_microservices/README.md)** | Monolith to Microservices | Strangler Fig, Branch by Abstraction, 5-Step DB Split, Transactional Outbox, CDC, Canary Traffic Routing |
| **[`03_systems_and_databases/ddia_book_study/`](03_systems_and_databases/ddia_book_study/README.md)** | Data-Intensive Applications | Storage Engines, Replication, Sharding, Batch/Stream Processing |
| **[`03_systems_and_databases/distributed-systems/`](03_systems_and_databases/distributed-systems/README.md)** | Distributed Systems Theory | Raft/Paxos Consensus, Consistent Hashing, 2PC Transactions, CAP |
| **[`04_infrastructure/tech-stack/`](04_infrastructure/tech-stack/README.md)** | Infrastructure Tech-Stack | 18 Modules (Redis, Postgres, JDBC, Tomcat, Netty, Mongo, Cassandra, ES, Object Storage, Kafka, K8s, etc.) |
| **[`04_infrastructure/api_security_in_action/`](04_infrastructure/api_security_in_action/README.md)** | API Security in Action | OWASP Top 10, OAuth2/PKCE, OIDC, JWT, TLS 1.3, mTLS, HTTP Signatures, Gateways, SPIFFE/SPIRE |
| **[`03_systems_and_databases/understanding_distributed_systems/`](03_systems_and_databases/understanding_distributed_systems/README.md)** | Understanding Distributed Systems | PACELC, Vector Clocks, Consistent Hashing, Raft Consensus, Circuit Breakers, Sagas, Tracing |
| **[`04_infrastructure/high_throughput_data_pipelines/`](04_infrastructure/high_throughput_data_pipelines/README.md)** | High-Throughput Data Pipelines | Kappa/Lambda, Kafka Batching, Flink Streaming, Parquet Columnar, Zero-Copy, Chandy-Lamport, 2PC Sink |
| **[`03_systems_and_databases/mit_6_5840_distributed_systems/`](03_systems_and_databases/mit_6_5840_distributed_systems/README.md)** | MIT 6.5840 (6.824) Distributed Systems | Labs 1-4: MapReduce, VMware FT, Raft Consensus, Fault-Tolerant KV, Sharded KV, ZooKeeper, Spanner |
| **[`03_systems_and_databases/stanford_cs244b_distributed_systems/`](03_systems_and_databases/stanford_cs244b_distributed_systems/README.md)** | Stanford CS244B Advanced Distributed Systems | P2P DHTs (Chord/Kademlia), BFT & Stellar FBA (PBFT/FBA), Atomic Broadcast & Shamir Secret Sharing |
| **[`03_systems_and_databases/cmu_15418_parallel_programming/`](03_systems_and_databases/cmu_15418_parallel_programming/README.md)** | CMU 15-418/618 Parallel Programming | SIMD/GPU SIMT/Work-Stealing, MESI/MOESI Cache Coherence & Memory Models, Interconnects & Lock-Free Queues |
| **[`03_systems_and_databases/stanford_ee382c_advanced_computer_architecture/`](03_systems_and_databases/stanford_ee382c_advanced_computer_architecture/README.md)** | Stanford EE382C Advanced Computer Architecture | Tomasulo OoO & ROB, TAGE Predictor, MSHR Non-blocking Caches, Directory Coherence, Dally Wormhole VC Router, Systolic Arrays |
| **[`03_systems_and_databases/mit_61060_performance_engineering/`](03_systems_and_databases/mit_61060_performance_engineering/README.md)** | MIT 6.1060 (6.172) Performance Engineering | Bit Hacks & AVX SIMD, Matrix Tiling & Cache-Oblivious Algorithms, Cilk Work/Span & SP-Bags Race Detection, `perf` |
| **[`03_systems_and_databases/berkeley_cs267_parallel_computing/`](03_systems_and_databases/berkeley_cs267_parallel_computing/README.md)** | UC Berkeley CS267 Parallel Computing | MPI/OpenMP/PGAS Hybrid, 2D SUMMA & CSR SpMV, Barnes-Hut Octree & Graph 500 BFS, Space-Filling Curves |
| **[`03_systems_and_databases/information_retrieval_and_search_engines/`](03_systems_and_databases/information_retrieval_and_search_engines/README.md)** | Information Retrieval & Search Engines | Inverted Index & VB Compression, Okapi BM25 & NDCG@K Evaluation, PageRank & SimHash Deduplication |
| **[`01_core_foundations/c_programming_a_modern_approach/`](01_core_foundations/c_programming_a_modern_approach/README.md)** | C Programming: A Modern Approach (K. N. King) | Fixed-Width Types, Sequence Points, VLAs, Arena Allocators, Struct Padding (`offsetof`), `memmove`, `_Generic` |
| **[`01_core_foundations/cpp_programming_language/`](01_core_foundations/cpp_programming_language/README.md)** | The C++ Programming Language | Memory model, RAII, move semantics, references vs pointers, `constexpr`, and template metaprogramming |
| **[`01_core_foundations/tour_of_cpp/`](01_core_foundations/tour_of_cpp/README.md)** | A Tour of C++ | Modern C++ features, Concepts, Ranges, Modules, Coroutines |
| **[`01_core_foundations/effective_modern_cpp/`](01_core_foundations/effective_modern_cpp/README.md)** | Effective Modern C++ | Type deduction, Smart Pointers, Perfect forwarding, Lambdas |
| **[`01_core_foundations/cpp_concurrency_in_action/`](01_core_foundations/cpp_concurrency_in_action/README.md)** | C++ Concurrency in Action | Thread management, Atomics, Memory ordering, Lock-free data structures |
| **[`01_core_foundations/cpp_stl/`](01_core_foundations/cpp_stl/README.md)** | C++ Standard Template Library | Sequence/Associative containers, Iterators, Custom allocators, Complexity |
| **[`01_core_foundations/java_vs_cpp/`](01_core_foundations/java_vs_cpp/README.md)** | Java vs C++ Mind Map | JVM Threads vs `std::thread`, GC vs RAII, Interfaces vs Multiple Inheritance |
| **[`03_systems_and_databases/relevant_search_solr_elasticsearch/`](03_systems_and_databases/relevant_search_solr_elasticsearch/README.md)** | Relevant Search Solr & Elasticsearch | DisMax Multi-Match & Gaussian Decay, Query-Time Graph Synonyms, Hybrid Vector/BM25 RRF, QuePID |
| **[`03_systems_and_databases/lucene_in_action/`](03_systems_and_databases/lucene_in_action/README.md)** | Lucene in Action | `MMapDirectory` zero-copy mmap, `IndexWriter` Segment Merging, TokenStream attributes, BKD Spatial Trees, Columnar DocValues |
| **[`01_core_foundations/interviewbit/`](01_core_foundations/interviewbit/)** | Problem Practice Sets | Array, Greedy (Gas Station, Majority Element) & real-world interview problem collections |
