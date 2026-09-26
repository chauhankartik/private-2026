# Parking Lot System — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** Singleton, Strategy, Builder, Factory Method, Observer, State  
> **SOLID Coverage:** All 5 principles applied  

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design a Parking Lot System. It should support:
> - Multiple floors / levels
> - Multiple vehicle types (Motorcycle, Car, Truck, Electric Vehicle)
> - Multiple spot types (Compact, Large, Motorcycle, Handicapped, EV)
> - Vehicle-to-spot assignment rules (vehicles fit only appropriate spots)
> - Ticket generation on entry and fee calculation on exit
> - Multiple payment methods (Cash, Card, UPI)
> - Real-time availability display
> - Thread-safe concurrent vehicle entry/exit"

---

## 2. Clarifying Questions (Ask These First!)

| Question | Why It Matters |
|---|---|
| How many **floors and spots per floor**? | Data model depth |
| Can a **car park in a large spot** if compact is full? | Flexible vs. strict spot matching |
| Is **fee calculation** flat, hourly, or tiered? | FeeStrategy complexity |
| Is **reservation** (pre-booking) required? | SpotState complexity |
| Should **EV spots** have charger support? | EVSpot subclass |
| Are **entry and exit gates** separate? | Gate entity design |
| Is **payment** online or at a kiosk? | Payment workflow |
| Should we track **license plates** to prevent duplicate entry? | Vehicle registry check |

---

## 3. Core Entities Identification

```
VehicleType          → enum: MOTORCYCLE, CAR, TRUCK, ELECTRIC_VEHICLE
SpotType             → enum: MOTORCYCLE, COMPACT, LARGE, HANDICAPPED, EV
SpotState            → enum: AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE
Vehicle              → abstract + concrete subtypes (Car, Truck, Motorcycle, EV)
ParkingSpot          → abstract + concrete subtypes (CompactSpot, LargeSpot, ...)
ParkingFloor         → manages spots on one floor; handles spot assignment
ParkingTicket        → immutable; issued on entry; carries entry time + spot ref
FeeStrategy          → pluggable fee calculation (hourly, flat, tiered)
Payment              → PaymentMethod interface + Cash, Card, UPI impls
PaymentStatus        → enum: PENDING, COMPLETED, FAILED, REFUNDED
DisplayBoard         → shows real-time spot counts per floor per type
ParkingLot           → Singleton; orchestrates floors, gates, display boards
```

---

## 4. Vehicle → Spot Compatibility Matrix

```
Vehicle Type      Allowed Spot Types
──────────────    ──────────────────────────────────
MOTORCYCLE    →   MOTORCYCLE, COMPACT, LARGE
CAR           →   COMPACT, LARGE
TRUCK         →   LARGE only
ELECTRIC_CAR  →   EV, COMPACT, LARGE

Key Rule: A vehicle can park in a spot of equal or LARGER size.
          A large vehicle can NEVER park in a smaller spot.
```

---

## 5. Class Diagram (UML)

```
┌──────────────────────────────────────────────────────────────────┐
│                    ParkingLot (Singleton)                         │
│  - floors: List<ParkingFloor>                                    │
│  - displayBoard: DisplayBoard                                    │
│  - feeStrategy: FeeStrategy                                      │
│  + parkVehicle(vehicle): ParkingTicket                           │
│  + unparkVehicle(ticket, payment): Receipt                       │
│  + getAvailability(): Map<SpotType, Integer>                    │
└──────────────────────────────────────────────────────────────────┘
      │ has-many               │ uses
      ▼                        ▼
┌─────────────────┐    ┌────────────────────┐
│  ParkingFloor   │    │   FeeStrategy      │
│  - spots        │    │  <<interface>>     │
│  - floorNumber  │    │  + calculate(t,e)  │
│  + findSpot()   │    └────────────────────┘
│  + releaseSpot()│          ▲      ▲      ▲
└─────────────────┘   HourlyFee FlatFee TieredFee
      │ has-many
      ▼
┌──────────────────────────────────────────┐
│           ParkingSpot (abstract)          │
│  - spotId, type, state                   │
│  - lock: ReentrantLock                   │
│  + canFit(vehicle): boolean              │
│  + assign(vehicle)                       │
│  + release()                             │
└──────────────────────────────────────────┘
    ▲        ▲        ▲          ▲
  Compact  Large  Motorcycle   EVSpot

┌──────────────────────┐   ┌──────────────────────┐
│  Vehicle (abstract)  │   │    ParkingTicket      │
│  - licensePlate      │   │  - ticketId           │
│  - type              │   │  - vehicle            │
└──────────────────────┘   │  - spot               │
    ▲    ▲    ▲    ▲       │  - floor              │
   Car Truck Moto EV       │  - entryTime          │
                           │  + getDuration()      │
                           └──────────────────────┘

┌────────────────────┐   ┌───────────────────┐
│  PaymentMethod     │   │   DisplayBoard    │
│  <<interface>>     │   │  + update(floor)  │
│  + pay(amount)     │   │  + showStatus()   │
└────────────────────┘   └───────────────────┘
    ▲      ▲      ▲
  Cash   Card    UPI
```

---

## 6. Design Patterns Applied

| Pattern | Where | Why |
|---|---|---|
| **Singleton** | `ParkingLot` | One lot per application; global access point |
| **Strategy** | `FeeStrategy` | Swap fee calculation (hourly/flat/tiered) without changing `ParkingLot` |
| **Builder** | `ParkingLot`, `ParkingTicket` | Fluent construction; complex multi-floor setup |
| **Factory Method** | `SpotFactory`, `VehicleFactory` | Create typed spots/vehicles without exposing subclass logic |
| **State** | `ParkingSpot` (AVAILABLE/OCCUPIED/RESERVED) | Spot behavior changes with its state |
| **Observer** | `DisplayBoard` observes floor spot changes | Auto-updates display when spots change state |
| **Template Method** | `ParkingSpot.canFit()` | Each subclass defines its own compatibility rule |

---

## 7. SOLID Principles Applied

| Principle | Application |
|---|---|
| **SRP** | `ParkingFloor` manages spots; `ParkingLot` orchestrates; `FeeStrategy` calculates fees; `ParkingTicket` holds entry data |
| **OCP** | New fee model = new `FeeStrategy` class; new vehicle = new `Vehicle` subclass; zero changes to core |
| **LSP** | `CompactSpot`, `LargeSpot`, `EVSpot` all substitutable for `ParkingSpot` |
| **ISP** | `FeeStrategy` has only `calculate()`; `PaymentMethod` has only `pay()` |
| **DIP** | `ParkingLot` depends on `FeeStrategy` interface, not `HourlyFeeStrategy` |

---

## 8. Thread Safety Strategy

```
ParkingSpot.assign() / release():
  Each spot has its own ReentrantLock.
  tryLock() is used for non-blocking spot assignment.
  Two threads can assign DIFFERENT spots concurrently.
  Only concurrent assignment to the SAME spot is serialized.

ParkingFloor.findSpot():
  Iterates spots; each spot independently guarded by its own lock.
  No floor-level lock needed (reduces contention).

ParkingTicket:
  Immutable — inherently thread-safe.

DisplayBoard:
  Updated after each spot state change.
  Uses AtomicInteger per counter — lock-free updates.

ParkingLot.activeTickets:
  ConcurrentHashMap — lock-free reads, fine-grained write locking.
```

---

## 9. Fee Calculation Design

```
Hourly Fee:
  duration = exitTime - entryTime (in hours, ceiling)
  fee = duration * ratePerHour
  Example: 2.5 hours * ₹50/hr = ₹150 (ceiling → 3 hrs)

Flat Fee:
  fee = fixedAmount (regardless of duration)

Tiered Fee:
  0–2 hrs:   ₹30/hr
  2–6 hrs:   ₹20/hr
  6+ hrs:    ₹15/hr (daily cap: ₹200)
```

---

## 10. Interview Trade-off Discussion Script

> **"How do you prevent double-booking a spot?"**  
> Each `ParkingSpot` has its own `ReentrantLock`. `assign()` calls `lock.tryLock()`  
> — only one thread can assign a given spot at a time. Other threads see  
> `state == OCCUPIED` and skip to the next available spot.

> **"How would you handle a full parking lot?"**  
> `ParkingFloor.findSpot()` returns `Optional.empty()`. `ParkingLot.parkVehicle()`  
> checks all floors sequentially. If all return empty, throw  
> `ParkingLotFullException`. Caller (gate system) informs the driver.

> **"How would you support pre-booking/reservation?"**  
> Add `RESERVED` to `SpotState`. A reservation service calls  
> `spot.reserve(reservationId)` which transitions AVAILABLE → RESERVED.  
> On arrival, the driver presents the reservation ID; the system finds the spot,  
> verifies the reservation, and transitions RESERVED → OCCUPIED.

> **"How would you scale this to multiple lots?"**  
> `ParkingLot` is currently a Singleton per JVM. For multiple lots:  
> Replace Singleton with a `ParkingLotManager` registry (like `LogManager`  
> from the Logging Framework LLD). Each lot is an independent `ParkingLot`  
> identified by `lotId`. A load balancer routes incoming vehicles.

> **"How do you handle EV charging billing?"**  
> Extend `FeeStrategy` to accept `VehicleType`. For `ELECTRIC_VEHICLE` in an  
> `EVSpot`, add `chargingRate * chargingHours` on top of the parking fee.  
> Store charging start time in `EVSpot` alongside parking start time.

---

## 11. File Structure

```
parking_lot/
├── 00_Theory_and_Design.md      ← This file (theory, UML, patterns)
├── 01_Enums.java                ← VehicleType, SpotType, SpotState, PaymentStatus
├── 02_Vehicle.java              ← Abstract Vehicle + Car, Truck, Motorcycle, ElectricCar
├── 03_ParkingSpot.java          ← Abstract ParkingSpot + CompactSpot, LargeSpot, etc.
├── 04_ParkingTicket.java        ← Immutable ticket + Builder + Receipt
├── 05_FeeStrategy.java          ← FeeStrategy interface + Hourly, Flat, Tiered impls
├── 06_Payment.java              ← PaymentMethod interface + Cash, Card, UPI
├── 07_ParkingFloor.java         ← Floor-level spot management + DisplayBoard
├── 08_ParkingLot.java           ← Singleton ParkingLot + Builder + orchestration
└── 09_Demo.java                 ← End-to-end: park, exit, fees, full lot, concurrency
```
