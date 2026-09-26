# Low-Level Design (LLD) Mind Map & Pattern Taxonomy

> **Purpose:** Master the classification of LLD problems, design pattern selection, and structural decision-making for Google & Amazon Staff/Senior Software Engineer interviews.

---

## 🧠 Interactive LLD Mind Map (Mermaid Diagram)

```mermaid
mindmap
  root(("LLD Mastery"))
    "01 Foundations"
      "SOLID Principles"
        "Single Responsibility - SRP"
        "Open Closed - OCP"
        "Liskov Substitution - LSP"
        "Interface Segregation - ISP"
        "Dependency Inversion - DIP"
    "02 Design Patterns"
      "Creational"
        "Singleton - Thread Safe Instance"
        "Factory - Subclass Instantiation"
        "Builder - Fluent Immutability"
      "Structural"
        "Decorator - Dynamic wrapper"
        "Facade - Unified interface"
        "Adapter - Interface compatibility"
        "Composite - Tree structure"
      "Behavioral"
        "Observer - Event notification"
        "State - State machine transitions"
        "Strategy - Interchangeable algorithms"
        "Chain of Responsibility - Pipelines"
        "Command - Undo Redo stacks"
    "03 System Designs"
      "Infrastructure and Storage"
        "Version Control System - Mini Git SHA-1"
        "Logging Framework - Log4j pipeline"
        "In Memory Cache - LRU, LFU, FIFO"
        "Distributed Rate Limiter - 5 Algorithms"
      "Resource Booking and Space"
        "Parking Lot System"
        "Movie Booking System - BookMyShow TTL"
        "Elevator Control System - LOOK Algorithm"
      "Finance and Expense Management"
        "Splitwise Expense Sharing - Min Cash Flow"
      "Concurrency and Scheduling"
        "OS Task Scheduler - MLFQ Algorithm"
```

---

## 📊 System Problem Taxonomy & Pattern Selection Matrix

| Problem Category | Key Characteristics | Recommended Primary Patterns | Secondary / Supporting Patterns | Benchmark Problems in Repo |
|---|---|---|---|---|
| **01. Foundations** | Core OOP Principles & Refactoring | SOLID, Clean Architecture | Interfaces, Inheritance vs Composition | [`solid/`](01_foundations/solid/) |
| **02. Creational Patterns** | Object Instantiation & Immutability | Singleton, Factory, Builder | Prototype, Object Pool | [`singleton/`](02_design_patterns/creational/singleton/), [`factory/`](02_design_patterns/creational/factory/), [`builder/`](02_design_patterns/creational/builder/) |
| **03. Structural Patterns** | Dynamic Wrapping & Subsystem Simplification | Decorator, Facade, Adapter, Composite | Proxy, Bridge | [`decorator/`](02_design_patterns/structural/decorator/), [`facade/`](02_design_patterns/structural/facade/), [`adapter/`](02_design_patterns/structural/adapter/), [`composite/`](02_design_patterns/structural/composite/) |
| **04. Behavioral Patterns** | Event Notifications, Pipelines & State Machine Transitions | Observer, State, Strategy, Chain of Responsibility, Command | Mediator, Memento, Iterator | [`observer/`](02_design_patterns/behavioral/observer/), [`state/`](02_design_patterns/behavioral/state/), [`strategy/`](02_design_patterns/behavioral/strategy/), [`chain_of_responsibility/`](02_design_patterns/behavioral/chain_of_responsibility/), [`command/`](02_design_patterns/behavioral/command/) |
| **05. Infrastructure & Utilities** | High-Throughput Pipelines & Extensible Storage | Composite, Strategy, Chain of Responsibility | Builder, Singleton | [`version_control_system/`](03_system_designs/infrastructure/version_control_system/), [`logging_framework/`](03_system_designs/infrastructure/logging_framework/), [`cache/`](03_system_designs/infrastructure/cache/) |
| **06. Resource & Booking Systems** | Physical/Digital Reservations & Fine-grained Locking | Facade, Strategy, State | Factory Method, ReentrantLock | [`parking_lot/`](03_system_designs/resource_booking/parking_lot/), [`movie_booking_system/`](03_system_designs/resource_booking/movie_booking_system/) |
| **07. Concurrency & Scheduling** | Multithreading, Queueing & CPU/Task Dispatching | Strategy, Command | PriorityQueue, ConcurrentHashMap | [`task_scheduler/`](03_system_designs/concurrency_scheduling/task_scheduler/) |

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
           │ Factory / Builder / │            │
           │ Singleton           │   ┌────────┴────────┐
           └─────────────────────┘  YES               NO
                                     │                 │
                                     ▼                 ▼
                        ┌─────────────────────────┐   Are you wrapping or simplifying subsystems?
                        │ Strategy / Observer /   │            │
                        │ Chain of Responsibility │   ┌────────┴────────┐
                        │ / Command               │  YES               NO
                        └─────────────────────────┘   │                 │
                                                      ▼                 ▼
                                          ┌─────────────────────┐   ┌───────────────────────────┐
                                          │ Decorator / Facade  │   │ State / Concurrent Lock   │
                                          │ / Adapter /Composite│   │                           │
                                          └─────────────────────┘   └───────────────────────────┘
```
