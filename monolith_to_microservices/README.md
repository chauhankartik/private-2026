# Monolith to Microservices

A production-grade guide to decomposing monolithic applications into microservices without breaking production. Based on Sam Newman's landmark book *Monolith to Microservices: Evolutionary Patterns to Transform Your Monolithic Application*.

---

## 📐 Table of Contents

| Section | Subject | Key Concepts |
| :--- | :--- | :--- |
| **00. Cheatsheet** | [Migration Cheatsheet](00_migration_cheatsheet.md) | Refactoring Patterns Matrix, Database Decomposition Decision Tree, Risk & Fallback Checklist |
| **01. Migration Strategy & Code Patterns** | [Strangler Fig & Code Decomposition](01_migration_strategy_and_patterns/01_strangler_fig_and_code_decomposition.md) | Strangler Fig Pattern, Branch by Abstraction, Parallel Run verification, UI Micro Frontends, Feature Toggles |
| **02. Database Decomposition & Transactions** | [Database Splitting, Outbox & Sagas](02_database_decomposition_and_transactions/02_database_splitting_outbox_and_sagas.md) | Breaking Monolithic DBs, 5-Step Database Split Pattern, Transactional Outbox Pattern, Change Data Capture (CDC / Debezium), Saga Pattern |
| **03. Communication & Observability** | [Event-Driven Messaging & Traffic Routing](03_communication_and_observability/03_event_driven_messaging_and_traffic_routing.md) | Sync vs Async communication, Schema evolution (Protobuf / Avro), API Gateway Canary Traffic Split (Envoy / NGINX), Distributed Tracing across boundaries |

---

## 🏗️ Strangler Fig Decomposition Architecture

The **Strangler Fig Pattern** allows you to incrementally replace monolithic sub-domains with microservices by intercepting incoming traffic at the edge and routing specific URI paths to newly extracted services.

```mermaid
flowchart TD
    subgraph Clients ["Clients & API Gateway"]
        User["Client Request"]
        Proxy["Strangler Fig Router / API Gateway"]
    end

    subgraph LegacyMonolith ["Legacy Monolith"]
        MonolithApp["Monolithic Core Application"]
        MonoDB[(Monolithic Shared Database)]
        MonolithApp --> MonoDB
    end

    subgraph ExtractedServices ["Extracted Microservices"]
        PaymentSvc["Payment Microservice (Extracted)"]
        PaymentDB[(Payment Database)]
        
        OrderSvc["Order Microservice (Extracted)"]
        OrderDB[(Order Database)]

        PaymentSvc --> PaymentDB
        OrderSvc --> OrderDB
    end

    User --> Proxy
    Proxy -->|GET /api/v1/users (Legacy Path)| MonolithApp
    Proxy -->|POST /api/v1/payments (Intercepted)| PaymentSvc
    Proxy -->|POST /api/v1/orders (Intercepted)| OrderSvc
```

---

## 🎯 Sam Newman's Core Evolutionary Principles

1. **Evolutionary Architecture over Big-Bang Rewrites**: Never attempt a complete "big-bang" rewrite. Migrate incrementally in small, low-risk vertical slices.
2. **Decouple Code First, Split Database Second**: Break in-memory code boundaries (interfaces, modules) before physically splitting database schemas.
3. **Embrace the Strangler Fig**: Intercept API traffic at the perimeter and route individual routes to microservices, leaving the monolith intact until all features are extracted.
4. **Avoid Shared Databases**: A shared database between microservices is a hidden monolith. Enforce database-per-service to protect bounded contexts.
5. **Transactional Outbox for Event Consistency**: Dual writes to a database and a message queue without distributed transactions lead to data corruption. Use the Transactional Outbox Pattern + Change Data Capture (CDC).
