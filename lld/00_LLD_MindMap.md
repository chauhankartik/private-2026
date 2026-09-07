# Low-Level Design (LLD) Mind Map & Pattern Taxonomy

> **Purpose:** Master the classification of LLD problems, design pattern selection, and structural decision-making for Google & Amazon Staff/Senior Software Engineer interviews.

---

## 🧠 Interactive LLD Mind Map (Mermaid Diagram)

```mermaid
mindmap
  root((LLD Mastery))
    01 Foundations
      SOLID Principles
        Single Responsibility (SRP)
        Open/Closed (OCP)
        Liskov Substitution (LSP)
        Interface Segregation (ISP)
        Dependency Inversion (DIP)
    02 Design Patterns
      Creational
        Singleton
        Factory Method / Abstract Factory
        Builder
      Structural
        Decorator (Dynamic wrapper)
        Facade (Unified interface)
        Adapter (Interface compatibility)
      Behavioral
        Observer (Event notification)
        State (State machine transitions)
        Strategy (Pluggable algorithms)
        Chain of Responsibility (Filtering pipeline)
    03 System Designs
      Infrastructure & Storage
        Logging Framework
          Chain of Responsibility (Filter pipeline)
          Strategy (Log formatters)
          Observer (Appenders fan-out)
        In-Memory Cache
          Strategy (Eviction: LRU/LFU/FIFO)
          ReadWriteLock (Concurrent storage)
        Distributed Rate Limiter
          Strategy (5 Core Algorithms)
          Factory (RateLimiterFactory)
      Resource Booking & Space
        Parking Lot System
          Strategy (Fee calculation)
          State (Spot status)
        Movie Booking System
          Facade (Service orchestrator)
          ReentrantLock (SeatLockManager TTL)
          Factory (Payment processors)
      Concurrency & Scheduling
        OS Task Scheduler
          PriorityQueue (Min-Heap FCFS/SJF/RR)
          Strategy (CPU Scheduling algorithms)
```

---

## 📊 System Problem Taxonomy & Pattern Selection Matrix

| Problem Category | Key Characteristics | Recommended Primary Patterns | Secondary / Supporting Patterns | Benchmark Problems in Repo |
|---|---|---|---|---|
| **01. Foundations** | Core OOP Principles & Refactoring | SOLID, Clean Architecture | Interfaces, Inheritance vs Composition | [`solid/`](01_foundations/solid/) |
| **02. Structural Patterns** | Dynamic Wrapping & Subsystem Simplification | Decorator, Facade | Proxy, Adapter | [`decorator/`](02_design_patterns/structural/decorator/), [`facade/`](02_design_patterns/structural/facade/) |
| **03. Behavioral Patterns** | Event Notifications & State Machine Transitions | Observer, State | Strategy, Command | [`observer/`](02_design_patterns/behavioral/observer/), [`state/`](02_design_patterns/behavioral/state/) |
| **04. Infrastructure & Utilities** | High-Throughput Pipelines & Extensible Processing | Chain of Responsibility, Strategy, Observer | Builder, Singleton | [`logging_framework/`](03_system_designs/infrastructure/logging_framework/), [`cache/`](03_system_designs/infrastructure/cache/) |
| **05. Resource & Booking Systems** | Physical/Digital Reservations & Fine-grained Locking | Facade, Strategy, State | Factory Method, ReentrantLock | [`parking_lot/`](03_system_designs/resource_booking/parking_lot/), [`movie_booking_system/`](03_system_designs/resource_booking/movie_booking_system/) |
| **06. Concurrency & Scheduling** | Multithreading, Queueing & CPU/Task Dispatching | Strategy, Command | PriorityQueue, ConcurrentHashMap | [`task_scheduler/`](03_system_designs/concurrency_scheduling/task_scheduler/) |

---

## 🎯 How to Choose the Right Pattern in an Interview

```
                      Do you need to create complex objects?
                                  │
                       ┌──────────┴──────────┐
                      YES                   NO
                       │                     │
                       ▼                     ▼
          ┌─────────────────────┐   Are you processing events or algorithm logic?
          │ Factory / Builder   │            │
          └─────────────────────┘   ┌────────┴────────┐
                                   YES               NO
                                    │                 │
                                    ▼                 ▼
                       ┌─────────────────────────┐   Are you wrapping or simplifying subsystems?
                       │ Strategy / Observer /   │            │
                       │ Chain of Responsibility │   ┌────────┴────────┐
                       └─────────────────────────┘  YES               NO
                                                     │                 │
                                                     ▼                 ▼
                                         ┌─────────────────────┐   ┌───────────────────────────┐
                                         │ Decorator / Facade  │   │ State / Concurrent Lock   │
                                         └─────────────────────┘   └───────────────────────────┘
```

---

## 📂 Reorganized Directory Mapping

```
lld/
├── 00_LLD_MindMap.md                         <-- You are here
├── 01_foundations/
│   └── solid/                                <-- SOLID Principles (SRP, OCP, LSP, ISP, DIP)
├── 02_design_patterns/
│   ├── structural/
│   │   ├── decorator/                        <-- Structural: Dynamic wrapping / Coffee shop / I/O
│   │   └── facade/                           <-- Structural: Unified system entrance
│   └── behavioral/
│       ├── observer/                         <-- Behavioral: Push/Pull event-driven notifications
│       └── state/                            <-- Behavioral: Finite state machine transitions
└── 03_system_designs/
    ├── infrastructure/
    │   ├── logging_framework/                <-- Infrastructure: Log4j pipeline (Chain, Formatter, Appender)
    │   ├── cache/                            <-- Infrastructure: O(1) Cache with LRU/LFU/FIFO + TTL
    │   └── rate_limiter/                     <-- Infrastructure: Rate Limiter (Token Bucket, Leaky Bucket, Sliding Window)
    ├── resource_booking/
    │   ├── parking_lot/                      <-- Reservation: Parking floor, spot matching & fee strategy
    │   └── movie_booking_system/             <-- Reservation: BookMyShow (SeatLockManager TTL, Payment)
    └── concurrency_scheduling/
        └── task_scheduler/                   <-- Concurrency: CPU Dispatcher & MLFQ Scheduling Algorithms
```
