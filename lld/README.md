# Low-Level Design (LLD) — Google Interview Preparation

> **Goal:** Master object-oriented design principles and design patterns  
> to demonstrate clean, extensible, production-grade code in Google interviews.

---

## 📁 Repository Structure

```
lld/
├── solid/              — SOLID Principles (the foundation)
├── decorator/          — Decorator Pattern (structural)
├── observer/           — Observer Pattern (behavioral)
├── state/              — State Pattern (behavioral)
├── facade/             — Facade Pattern (structural)
├── logging_framework/  — LLD: Logging Framework
├── cache/              — LLD: In-Memory Cache with Eviction Policies
├── parking_lot/        — LLD: Parking Lot System
├── task_scheduler/     — LLD: OS Task Scheduler (FCFS/SJF/SRTF/RR/Priority/MLFQ)
└── README.md           — This file
```

---

## 📚 Modules

### 1. SOLID Principles — `solid/`

The five foundational OOP principles every Google engineer must know.

| File | Principle | One-Liner |
|---|---|---|
| [00_SOLID_Cheatsheet.md](solid/00_SOLID_Cheatsheet.md) | All 5 | Quick reference & interview script |
| [01_S_SingleResponsibility.java](solid/01_S_SingleResponsibility.java) | **S**RP | A class should have only one reason to change |
| [02_O_OpenClosed.java](solid/02_O_OpenClosed.java) | **O**CP | Open for extension, closed for modification |
| [03_L_LiskovSubstitution.java](solid/03_L_LiskovSubstitution.java) | **L**SP | Subtypes must be substitutable for base types |
| [04_I_InterfaceSegregation.java](solid/04_I_InterfaceSegregation.java) | **I**SP | Prefer many small interfaces over one fat one |
| [05_D_DependencyInversion.java](solid/05_D_DependencyInversion.java) | **D**IP | Depend on abstractions, not concretions |

---

### 2. Decorator Pattern — `decorator/`

Structural pattern: attach new behavior to objects dynamically without modifying their class.

| File | Level | Focus |
|---|---|---|
| [00_Basic_Theory.md](decorator/00_Basic_Theory.md) | Theory | When & why to use Decorator, UML, trade-offs |
| [01_Basic_Decorator_Code.java](decorator/01_Basic_Decorator_Code.java) | Basic | Coffee shop example — classic Decorator walkthrough |
| [02_Intermediate_Decorator_Code.java](decorator/02_Intermediate_Decorator_Code.java) | Intermediate | I/O streams, real-world Java Decorator usage |
| [03_Advanced_Decorator_Code.java](decorator/03_Advanced_Decorator_Code.java) | Advanced | Combining with other patterns, thread safety |

---

### 3. Observer Pattern — `observer/`

Behavioral pattern: one-to-many dependency where subjects notify observers of state changes.

| File | Level | Focus |
|---|---|---|
| [00_theory_staff_swe.md](observer/00_theory_staff_swe.md) | Staff-level Theory | Deep dive — push vs pull, distributed systems |
| [01_ObserverPatternDeepDive.java](observer/01_ObserverPatternDeepDive.java) | Advanced | Production-grade Observer with generics |
| [02_Basic_Theory.md](observer/02_Basic_Theory.md) | Theory | Core concepts, UML, event-driven design |
| [03_Basic_Observer_Code.java](observer/03_Basic_Observer_Code.java) | Basic | Weather station example — classic Observer |

---

### 4. Logging Framework LLD — `logging_framework/`

Classic Google/Amazon LLD interview question. Implements a Log4j-style logging framework from scratch.

**Patterns:** Singleton, Builder, Strategy, Chain of Responsibility, Observer, Decorator  
**SOLID:** All 5 principles demonstrated across all classes

| File | Level | Focus |
|---|---|---|
| [00_Theory_and_Design.md](logging_framework/00_Theory_and_Design.md) | Theory | UML, patterns map, trade-off scripts, clarifying questions |
| [01_Core_Types.java](logging_framework/01_Core_Types.java) | Core | `LogLevel` enum, `LogRecord` (immutable + Builder) |
| [02_Interfaces.java](logging_framework/02_Interfaces.java) | Core | `LogFormatter`, `LogFilter`, `LogAppender` interfaces (ISP) |
| [03_Formatters.java](logging_framework/03_Formatters.java) | Strategy | `SimpleFormatter`, `PatternFormatter`, `JsonFormatter` |
| [04_Filters.java](logging_framework/04_Filters.java) | Chain | `LevelFilter`, `RegexFilter`, `CompositeFilter`, `ThresholdFilter` |
| [05_Appenders.java](logging_framework/05_Appenders.java) | Observer/Decorator | `ConsoleAppender`, `FileAppender`, `AsyncAppender` |
| [06_Logger.java](logging_framework/06_Logger.java) | Core | `Logger` + `Logger.Builder` — gate → filter → fan-out pipeline |
| [07_LogManager.java](logging_framework/07_LogManager.java) | Singleton | `LogManager` — double-checked locking, ConcurrentHashMap registry |
| [08_Demo.java](logging_framework/08_Demo.java) | Demo | 8 end-to-end scenarios wiring all components together |

---

### 5. In-Memory Cache LLD — `cache/`

Classic Google/Amazon LLD interview question. Implements a production-grade in-memory cache with pluggable eviction policies.

**Patterns:** Strategy, Builder, Generic Types, Decorator (TTL), Null Object  
**SOLID:** All 5 principles · **Thread Safety:** ReadWriteLock + AtomicLong  

| File | Level | Focus |
|---|---|---|
| [00_Theory_and_Design.md](cache/00_Theory_and_Design.md) | Theory | UML, O(1) algorithm proofs, trade-off scripts, clarifying questions |
| [01_CacheEntry.java](cache/01_CacheEntry.java) | Core | Generic immutable entry with TTL metadata + factory method |
| [02_Interfaces.java](cache/02_Interfaces.java) | Core | `Cache<K,V>` + `EvictionPolicy<K>` interfaces (ISP, DIP) |
| [03_LRUEvictionPolicy.java](cache/03_LRUEvictionPolicy.java) | Algorithm | O(1) LRU via sentinel DoublyLinkedList + HashMap |
| [04_LFUEvictionPolicy.java](cache/04_LFUEvictionPolicy.java) | Algorithm | O(1) LFU via triple-map design (LeetCode 460) |
| [05_FIFOEvictionPolicy.java](cache/05_FIFOEvictionPolicy.java) | Algorithm | FIFO via Queue + lazy removal trick |
| [06_CacheStats.java](cache/06_CacheStats.java) | Metrics | Lock-free AtomicLong counters + hit rate computation |
| [07_InMemoryCache.java](cache/07_InMemoryCache.java) | Core | Main impl: ReadWriteLock + lazy TTL + background sweeper + CacheBuilder |
| [08_Demo.java](cache/08_Demo.java) | Demo | 8 scenarios: LRU, LFU, FIFO, TTL, sweeper, stats, concurrency, cache-aside |

---

### 6. Parking Lot System LLD — `parking_lot/`

Classic Google/Amazon LLD interview. Full parking lot with multi-floor, multi-vehicle, pluggable fees, payments, and thread-safe concurrent entry/exit.

**Patterns:** Singleton, Strategy, Builder, Factory Method, Observer, State  
**SOLID:** All 5 principles · **Thread Safety:** Per-spot ReentrantLock + ConcurrentHashMap  

| File | Level | Focus |
|---|---|---|
| [00_Theory_and_Design.md](parking_lot/00_Theory_and_Design.md) | Theory | UML, vehicle-spot matrix, trade-off scripts, clarifying questions |
| [01_Enums.java](parking_lot/01_Enums.java) | Core | VehicleType, SpotType, SpotState, PaymentStatus |
| [02_Vehicle.java](parking_lot/02_Vehicle.java) | Core | Abstract Vehicle + Motorcycle, Car, Truck, ElectricCar + VehicleFactory |
| [03_ParkingSpot.java](parking_lot/03_ParkingSpot.java) | Core | Abstract ParkingSpot + Compact, Large, Motorcycle, EV + SpotFactory |
| [04_ParkingTicket.java](parking_lot/04_ParkingTicket.java) | Core | Immutable ParkingTicket (Builder) + ParkingReceipt value object |
| [05_FeeStrategy.java](parking_lot/05_FeeStrategy.java) | Strategy | HourlyFee, FlatFee, TieredFee, WeekendFee (Decorator) |
| [06_Payment.java](parking_lot/06_Payment.java) | Strategy | PaymentMethod interface + Cash, Card, UPI implementations |
| [07_ParkingFloor.java](parking_lot/07_ParkingFloor.java) | Core | ParkingFloor (tryLock spot search) + Observer-notified DisplayBoard |
| [08_ParkingLot.java](parking_lot/08_ParkingLot.java) | Singleton | ParkingLot (DCL Singleton + Builder) + entry/exit pipeline + exceptions |
| [09_Demo.java](parking_lot/09_Demo.java) | Demo | 9 scenarios: park, unpark, vehicle types, duplicate, fees, concurrency |

---

### 7. OS Task Scheduler LLD — `task_scheduler/`

Design and simulate an OS CPU Scheduler. All 6 classic algorithms implemented with Gantt chart visualisation and comparative metrics.

**Patterns:** Strategy, Builder, Template Method, Value Object  
**OS Theory:** FCFS, SJF, SRTF, Round Robin, Priority (Aging), MLFQ  

| File | Level | Focus |
|---|---|---|
| [00_Theory_and_Design.md](task_scheduler/00_Theory_and_Design.md) | Theory | Algorithm deep-dives, metrics formulae, UML, interview trade-offs |
| [01_Enums.java](task_scheduler/01_Enums.java) | Core | ProcessState (5-state model), Priority, SchedulingPolicy |
| [02_Process.java](task_scheduler/02_Process.java) | Core | PCB — all identity + runtime state fields, TAT/WT/RT formulae, Builder |
| [03_SchedulingAlgorithm.java](task_scheduler/03_SchedulingAlgorithm.java) | Strategy | Interface + FCFS, SJF, SRTF, RoundRobin, Priority+Aging, MLFQ |
| [04_GanttChart.java](task_scheduler/04_GanttChart.java) | Metrics | GanttChart (ASCII art) + SchedulingMetrics value object + report |
| [05_Dispatcher.java](task_scheduler/05_Dispatcher.java) | Core | Context switch simulation, one-tick CPU execution, idle tracking |
| [06_CPUScheduler.java](task_scheduler/06_CPUScheduler.java) | Engine | Discrete-time simulation loop + Builder; algorithm-agnostic |
| [07_Demo.java](task_scheduler/07_Demo.java) | Demo | All 6 algorithms on same process set → Gantt + comparative table |

---

## 🗺️ Study Order

```
1. SOLID Principles     → Foundation for ALL design patterns
2. Observer Pattern     → Most common behavioral pattern
3. Decorator Pattern    → Most common structural pattern
4. Logging Framework    → Real LLD interview: combines 5+ patterns
5. In-Memory Cache      → Real LLD interview: DSA meets OOD (LRU/LFU/FIFO)
6. Parking Lot System   → Real LLD interview: full system with multiple entities
7. OS Task Scheduler    → Real LLD interview: algorithms + simulation engine
```

---

## 🎯 What Google Looks For in LLD Interviews

| Criteria | What They Assess |
|---|---|
| **Clean API design** | Are your interfaces intuitive and minimal? |
| **SOLID compliance** | Does your design respect the 5 principles? |
| **Extensibility** | Can new features be added without modifying existing code? |
| **Pattern knowledge** | Can you identify and apply the right pattern? |
| **Trade-off discussion** | Can you articulate WHY you chose a particular design? |
| **Code quality** | Naming, readability, separation of concerns |

---

## 📋 Patterns Roadmap (Future Modules)

| Pattern | Category | Priority | Use Case |
|---|---|---|---|
| **Strategy** | Behavioral | 🔴 High | Swappable algorithms at runtime |
| **Factory / Abstract Factory** | Creational | 🔴 High | Object creation without exposing logic |
| **Singleton** | Creational | 🟡 Medium | Single instance (thread-safe) |
| **Builder** | Creational | 🟡 Medium | Complex object construction |
| **Adapter** | Structural | 🟡 Medium | Interface compatibility |
| **Command** | Behavioral | 🟡 Medium | Undo/redo, task queuing |
| **State** | Behavioral | 🟢 Lower | Finite state machines |
| **Proxy** | Structural | 🟢 Lower | Lazy loading, access control |

---

## 💡 Interview Communication Template

When asked to design a system:

> "Let me start by identifying the **core entities** and their **responsibilities**.  
> I'll apply **SRP** — each class handles one concern.  
> For extensibility, I'll use **OCP** — new features via new classes, not modification.  
> I'll define **interfaces** first (ISP + DIP), then implement.  
> For [specific behavior], the **[Pattern Name]** pattern fits because [reason]."

This structured approach signals senior-level thinking.
