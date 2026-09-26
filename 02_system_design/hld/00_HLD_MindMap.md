# High-Level System Design (HLD) Mind Map

This document presents a structured visual breakdown of distributed system design components, architectural patterns, and scalability bottlenecks.

---

## 1. Visual Taxonomy

```mermaid
mindmap
  root((HLD Taxonomy))
    System Building Blocks
      DNS and Edge
        GeoDNS Anycast Routing
        Content Delivery Network CDN
      Traffic Management
        Layer 4 TCP vs Layer 7 HTTP
        Load Balancer Round Robin Consistent Hash
        API Gateway Authentication Rate Limiting
      Stateless Compute
        Auto Scaling Groups
        Microservice Decomposition
      Caching Tier
        Read Through Write Through Write Back
        Eviction LRU LFU TTL
      Storage Tier
        RDBMS Master Slave Sharding
        NoSQL KeyValue Document WideColumn
      Asynchronous Messaging
        Message Queues RabbitMQ SQS
        Distributed Log Kafka Event Streaming
    Non-Functional Trade-Offs
      Scalability
        Vertical Scale Up Hardware
        Horizontal Scale Out Nodes
      Availability
        SLA 99 Point 99 Percent Downtime Math
        Active Active vs Active Passive
      Consistency
        Strong Read After Write
        Eventual Consistency Quorum Math
    Canonical Architectures
      Distributed ID Snowflake
      Rate Limiter Token Bucket
      URL Shortener KGS Base62
      Web Crawler URL Frontier
      Real Time Chat WebSockets
      Video Streaming ABR HLS
```

---

## 2. Component Reference Table

| Component | Primary Function | Failure Modes & Scaling Bottlenecks |
| :--- | :--- | :--- |
| **API Gateway** | Request routing, SSL termination, authentication, rate limiting | Single point of failure if not deployed in multi-region active-active clusters |
| **Load Balancer** | Distributes incoming HTTP/TCP traffic across application nodes | Connection exhaustion on Layer 7 proxies during sudden traffic surges |
| **Redis Cache** | Sub-millisecond in-memory read acceleration | Cache stampede / thundering herd on key expiration; Out-Of-Memory (OOM) eviction |
| **Message Queue (Kafka)** | Decouples synchronous API calls via asynchronous pub/sub | Consumer group lag during spike events; partition skew |
| **Database Sharding** | Partitions relational tables across multiple database nodes | Cross-shard JOIN operations and distributed 2PC transaction overhead |
| **CDN (Edge)** | Caches static assets (images, video segments) near users | High cache miss penalty; purge synchronization latency |
