# Elevator Control System — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** State, Strategy, Singleton, Facade, Observer, Command  
> **SOLID Coverage:** All 5 principles applied  
> **Key Technical Challenge:** Multi-elevator scheduling algorithms (LOOK / SCAN), thread safety during movement, and state machine transitions.

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design a Multi-Elevator Control System for a tall building. It should support:
> - A building with $N$ floors and $M$ elevator cars.
> - Floor Buttons (External Hall Buttons: UP / DOWN) on each floor.
> - Elevator Car Buttons (Internal Panel: Floor numbers, Door Open/Close, Emergency).
> - Real-time Elevator status display (Current Floor, Moving Direction, Door State).
> - Multi-Elevator Dispatching & Scheduling Algorithms:
>   1. **LOOK / SCAN (Elevator Algorithm)** — Moves in current direction serving all requests until no further requests exist in that direction, then reverses.
>   2. **SSTF (Shortest Seek Time First)** — Serves nearest floor request first.
>   3. **FCFS (First Come First Served)** — Serves requests in arrival order.
> - Thread-safe movement simulation (concurrent request dispatching & floor-by-floor movement)."

---

## 2. Clarifying Questions (Ask These First!)

| Question | Why It Matters |
|---|---|
| Are **hall buttons** directional (UP/DOWN) or destination-selection (Destination Control System)? | Dictates external request model (`floor + direction` vs `source + destination`). |
| How do we handle **capacity / weight limits**? | May temporarily bypass external requests if max weight is reached. |
| What happens during an **Emergency / Maintenance mode**? | Requires State transition (`MAINTENANCE`) disabling request assignment. |
| Which **dispatch algorithm** should be default? | **LOOK/SCAN** is the industry standard for minimizing wait time & power consumption. |

---

## 3. Dispatching Algorithm Comparison

| Algorithm | Mechanism | Pros | Cons | Best Used In |
|---|---|---|---|---|
| **LOOK / SCAN** | Keeps moving in current direction as long as requests exist ahead; then reverses. | Low average wait time, prevents starvation. | Reversal overhead at top/bottom requested floors. | Commercial Skyscrapers / Real Elevators |
| **SSTF** | Picks the closest request to current floor. | Minimizes immediate travel distance. | Risk of starvation for distant floors under heavy traffic. | Low-traffic residential buildings |
| **FCFS** | Processes requests in exact order of queue arrival. | Simple & fair. | Extremely inefficient elevator movement (thrashing). | Test environments / Single-floor lifts |

---

## 4. Class Diagram (UML Architecture)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          ElevatorSystem (Facade/Singleton)                      │
│  - elevators: List<ElevatorController>                                         │
│  - dispatchStrategy: IDispatchStrategy                                         │
│  + requestElevator(sourceFloor, direction): void                              │
│  + pressInternalButton(elevatorId, destinationFloor): void                     │
└─────────────────────────────────────────────────────────────────────────────────┘
         │                                       │
         ▼ has-many                              ▼ uses
┌────────────────────────────────┐      ┌────────────────────────────────┐
│       ElevatorController       │      │       IDispatchStrategy        │
│  - elevator: Elevator          │      │         <<interface>>          │
│  - upRequests: TreeSet<Int>    │      │  + selectElevator(...): Elevator│
│  - downRequests: TreeSet<Int>  │      └────────────────────────────────┘
│  + addRequest(floor): void     │                       ▲
│  + step(): void                │           ┌───────────┴───────────┐
└────────────────────────────────┘           │                       │
                 │                     LOOKStrategy            SSTFStrategy
                 ▼ manages
┌────────────────────────────────┐
│            Elevator            │
│  - id: int                     │
│  - currentFloor: int           │
│  - direction: Direction        │
│  - state: ElevatorState        │
│  - door: Door                  │
│  - display: Display            │
└────────────────────────────────┘
```

---

## 5. Design Patterns Applied

| Pattern | Component | Why It Was Chosen |
|---|---|---|
| **State** | `ElevatorState` & `DoorState` | Encapsulates behavior based on status (`MOVING_UP`, `MOVING_DOWN`, `IDLE`, `DOOR_OPEN`). |
| **Strategy** | `IDispatchStrategy` | Pluggable elevator selection algorithms (LOOK, SSTF, FCFS). |
| **Facade / Singleton** | `ElevatorSystem` | Central entry point for external floor requests and system monitoring. |
| **Observer** | `Display` | Listens to floor changes and direction updates to refresh real-time display indicators. |
