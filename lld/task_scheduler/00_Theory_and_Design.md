# OS Task Scheduler — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** Strategy, Builder, Template Method, Observer, Singleton  
> **OS Concepts:** FCFS, SJF, SRTF, Round Robin, Priority, MLFQ  
> **SOLID Coverage:** All 5 principles  

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design an Operating System Task Scheduler. It should support:
> - Multiple scheduling algorithms: FCFS, SJF, SRTF, Round Robin, Priority
> - Process lifecycle management (NEW → READY → RUNNING → TERMINATED)
> - Process Control Block (PCB) with all metadata
> - CPU scheduling simulation with a discrete-time clock
> - Metrics: Turnaround Time, Waiting Time, Response Time, CPU Utilization
> - Gantt Chart for visual execution timeline
> - Pluggable algorithms (swap without changing core scheduler)"

---

## 2. Clarifying Questions (Ask These First!)

| Question | Why It Matters |
|---|---|
| **Single-core or multi-core?** | Multi-core needs parallel dispatch queues |
| **Preemptive or non-preemptive?** | SRTF/RR are preemptive; FCFS/SJF are not |
| **Is burst time known in advance?** | Real OS doesn't know; we simulate with given values |
| **How is priority assigned?** | Static (fixed) vs. dynamic (aging) |
| **What is the I/O model?** | Blocked state, I/O burst, re-entry to ready queue |
| **Should starvation be handled?** | Aging mechanism in Priority scheduling |
| **Is MLFQ required?** | Most complex — multiple queues with demotion/promotion |

---

## 3. Core Entities Identification

```
ProcessState          → enum: NEW, READY, RUNNING, BLOCKED, TERMINATED
Priority              → enum: HIGH, MEDIUM, LOW (maps to integer value)
Process (PCB)         → all per-process metadata: pid, burst, arrival, state, times
SchedulingAlgorithm   → Strategy interface: selectNext(readyQueue) → Process
FCFSAlgorithm         → First Come First Served
SJFAlgorithm          → Shortest Job First (non-preemptive)
SRTFAlgorithm         → Shortest Remaining Time First (preemptive SJF)
RoundRobinAlgorithm   → circular, time-quantum based
PriorityAlgorithm     → highest priority runs; aging prevents starvation
MLFQAlgorithm         → Multi-Level Feedback Queue (adaptive)
Dispatcher            → context switch + CPU execution simulation
CPUScheduler          → orchestrator: time clock + ready queue + algorithm
SchedulingMetrics     → turnaround, waiting, response, utilization, throughput
GanttChart            → visual execution timeline (ASCII art)
```

---

## 4. Scheduling Algorithm Deep Dive

### 4a. FCFS — First Come First Served

```
Data Structure: FIFO Queue (insertion order = arrival order)
Preemptive:     No
Pros:           Simple; no starvation; fair in order
Cons:           Convoy Effect — one slow process blocks all behind it
Optimal for:    Batch systems, sequential workloads

Example: P1(8ms) → P2(4ms) → P3(2ms) (arrival order)
  Average waiting = (0 + 8 + 12) / 3 = 6.7 ms
```

### 4b. SJF — Shortest Job First (Non-Preemptive)

```
Data Structure: Min-Heap (by burst time) among ARRIVED processes
Preemptive:     No — once started, runs to completion
Optimal:        Minimum average waiting time (proven optimal)
Cons:           Starvation of long jobs; requires burst time knowledge
                (Real OS: predict with exponential averaging of past bursts)

Example: P1(6ms), P2(2ms), P3(4ms) (all arrive at t=0)
  Order: P2 → P3 → P1
  Average waiting = (0 + 2 + 6) / 3 = 2.7 ms (vs FCFS: 4 ms)
```

### 4c. SRTF — Shortest Remaining Time First (Preemptive SJF)

```
Data Structure: Min-Heap (by remaining burst time), re-evaluated every tick
Preemptive:     Yes — new arrival with shorter remaining preempts current
Optimal:        Best possible average waiting time (theoretical)
Cons:           High context switch overhead; starvation; burst must be known

Preemption rule: if newProcess.remainingTime < currentProcess.remainingTime
  → preempt currentProcess (put back in ready queue), run newProcess
```

### 4d. Round Robin

```
Data Structure: Circular Queue (FIFO)
Preemptive:     Yes — forced preemption at every quantum boundary
Time Quantum q: If process needs > q ms, runs for q ms, re-enqueued
Pros:           Fair; good response time; no starvation
Cons:           High context switches if q is small; throughput suffers

Quantum tuning:
  Too small q → too many context switches (overhead dominates)
  Too large q → degenerates to FCFS
  Rule of thumb: q > 80% of CPU burst length

Example: q=2, P1(5ms), P2(3ms), P3(2ms)
  Timeline: P1(2) P2(2) P3(2) P1(2) P2(1) P1(1)
```

### 4e. Priority Scheduling

```
Data Structure: Max-Heap (by priority); ties broken by arrival time
Preemptive:     Both versions exist (we implement preemptive)
Problem:        STARVATION — low-priority process may never run
Solution:       AGING — gradually increase priority of waiting processes
                e.g., +1 priority every 5 time units of waiting
```

### 4f. MLFQ — Multi-Level Feedback Queue

```
Queues:    Q0 (RR, q=2) → Q1 (RR, q=4) → Q2 (FCFS)
Entry:     New process enters Q0 (highest priority)
Demotion:  If process uses full quantum, demote to lower queue
Promotion: If process waits too long in lower queue, promote (aging)
Pros:      Self-adaptive — I/O-bound (short bursts) stays in Q0; CPU-bound sinks to Q2
```

---

## 5. Key Metrics Formulae

```
Turnaround Time   = Completion Time - Arrival Time
Waiting Time      = Turnaround Time - Burst Time
Response Time     = First CPU Allocation Time - Arrival Time
CPU Utilization   = (Total Busy Time / Total Simulation Time) × 100
Throughput        = Total Processes Completed / Total Simulation Time

Average of each metric computed across all processes.
```

---

## 6. Class Diagram (UML)

```
┌──────────────────────────────────────────────────────┐
│              CPUScheduler (Builder)                   │
│  - algorithm : SchedulingAlgorithm                   │
│  - processes : List<Process>                         │
│  - dispatcher: Dispatcher                            │
│  - clock     : int  (current time tick)              │
│  + run()     → SchedulingMetrics                     │
└──────────────────────────────────────────────────────┘
         │ uses                    │ delegates to
         ▼                        ▼
┌─────────────────────┐   ┌────────────────────────────┐
│  SchedulingAlgorithm│   │       Dispatcher            │
│   <<interface>>     │   │  + dispatch(p, ticks)       │
│  + selectNext(queue)│   │  + contextSwitch(old, new)  │
│  + onTick(queue)    │   └────────────────────────────┘
└─────────────────────┘
  ▲   ▲   ▲   ▲   ▲  ▲
 FCFS SJF SRTF RR Pri MLFQ

┌──────────────────────────────────────────────────────┐
│            Process (PCB — immutable identity)         │
│  - pid, name, arrivalTime, burstTime, priority       │
│  - remainingTime (mutable during simulation)          │
│  - state: ProcessState                               │
│  - startTime, completionTime, firstResponseTime      │
└──────────────────────────────────────────────────────┘

┌────────────────────────────────────┐
│         SchedulingMetrics          │
│  + avgTurnaroundTime : double      │
│  + avgWaitingTime    : double      │
│  + avgResponseTime   : double      │
│  + cpuUtilization    : double      │
│  + throughput        : double      │
│  + ganttChart        : GanttChart  │
│  + printReport()                   │
└────────────────────────────────────┘

┌─────────────────────────────────┐
│         GanttChart              │
│  - entries: List<Entry>         │
│    Entry { pid, start, end }    │
│  + print()                      │
└─────────────────────────────────┘
```

---

## 7. Design Patterns Applied

| Pattern | Where | Why |
|---|---|---|
| **Strategy** | `SchedulingAlgorithm` | Swap FCFS/SJF/RR without changing `CPUScheduler` |
| **Builder** | `CPUScheduler`, `Process` | Fluent construction; many optional fields |
| **Template Method** | `AbstractSchedulingAlgorithm` | Common addProcess/tick logic; each algo defines selectNext |
| **Observer** | `CPUScheduler` notifies `GanttChart` | Timeline updated on every dispatch event |
| **Value Object** | `SchedulingMetrics` | Immutable result snapshot after simulation |
| **Factory Method** | `Process.Builder`, algorithm constructors | Hides construction complexity |

---

## 8. SOLID Principles Applied

| Principle | Application |
|---|---|
| **SRP** | `CPUScheduler` runs clock; `Dispatcher` switches context; `SchedulingMetrics` computes results |
| **OCP** | New algorithm = new class implementing `SchedulingAlgorithm`; CPUScheduler unchanged |
| **LSP** | All algorithm impls substitutable for `SchedulingAlgorithm` in `CPUScheduler` |
| **ISP** | `SchedulingAlgorithm` has only scheduling methods; `Dispatcher` has only dispatch methods |
| **DIP** | `CPUScheduler` depends on `SchedulingAlgorithm` interface, not `RoundRobinAlgorithm` |

---

## 9. Algorithms Comparison Table

| Algorithm | Preemptive | Starvation | Optimal | Best Use Case |
|---|---|---|---|---|
| FCFS | No | No | No | Simple batch systems |
| SJF | No | Yes | Avg. Wait | Short-job dominated workloads |
| SRTF | Yes | Yes | Avg. Wait | Theoretical optimum |
| Round Robin | Yes | No | Response | Interactive time-sharing systems |
| Priority | Both | Yes (without aging) | — | Real-time systems |
| MLFQ | Yes | No (with aging) | Adaptive | Modern OS (Linux CFS-like) |

---

## 10. File Structure

```
task_scheduler/
├── 00_Theory_and_Design.md       ← This file (theory, algorithms, UML)
├── 01_Enums.java                 ← ProcessState, Priority, SchedulingPolicy
├── 02_Process.java               ← PCB (Process Control Block) + Builder
├── 03_SchedulingAlgorithm.java   ← Interface + FCFS, SJF, SRTF, RoundRobin, Priority, MLFQ
├── 04_GanttChart.java            ← Timeline recorder + ASCII visualization
├── 05_Dispatcher.java            ← Context switch simulation + CPU execution
├── 06_CPUScheduler.java          ← Main simulation engine + SchedulingMetrics + Builder
└── 07_Demo.java                  ← All 6 algorithms on same process set → comparative output
```
