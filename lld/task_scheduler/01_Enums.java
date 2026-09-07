// =============================================================================
// FILE: 01_Enums.java
// TOPIC: OS Task Scheduler LLD — All Enumerations
// =============================================================================

package task_scheduler;

// =============================================================================
// ■ ProcessState — Lifecycle states of a process (OS state machine)
//
// Transitions:
//   NEW       → READY       (admitted to ready queue)
//   READY     → RUNNING     (dispatched by scheduler)
//   RUNNING   → READY       (preempted — time quantum expired)
//   RUNNING   → BLOCKED     (I/O request or event wait)
//   RUNNING   → TERMINATED  (burst time fully consumed)
//   BLOCKED   → READY       (I/O completion — re-enters ready queue)
//
// In this simulation we model: NEW → READY → RUNNING → (READY|BLOCKED|TERMINATED)
// =============================================================================
enum ProcessState {
    NEW,          // created, not yet in ready queue
    READY,        // in ready queue, waiting for CPU
    RUNNING,      // currently executing on CPU
    BLOCKED,      // waiting for I/O / event (not modelled in basic simulation)
    TERMINATED    // execution complete
}


// =============================================================================
// ■ Priority — Discrete priority levels
//
// Mapped to integer ordinals (higher ordinal = higher priority).
// Used by PriorityAlgorithm and MLFQ.
//
// Aging: a WAITING process's effective priority increases over time to
// prevent indefinite starvation of LOW-priority processes.
// =============================================================================
enum Priority {
    LOW(1),
    MEDIUM(5),
    HIGH(10),
    CRITICAL(20);   // real-time / interrupt-driven tasks

    private final int value;

    Priority(int value) { this.value = value; }

    public int getValue() { return value; }
}


// =============================================================================
// ■ SchedulingPolicy — Tag enum identifying which algorithm is in use
// Used for labelling in metrics reports and Gantt chart headers.
// =============================================================================
enum SchedulingPolicy {
    FCFS,
    SJF,
    SRTF,
    ROUND_ROBIN,
    PRIORITY,
    MLFQ
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why is Priority an enum with integer values instead of just an int?
// A: An enum with values gives named constants (HIGH, MEDIUM, LOW) that are
//    self-documenting and impossible to pass wrong values (e.g., priority=999).
//    The int value() maps to the algorithm's comparison logic without exposing
//    raw integers to the domain model. It also supports Aging: we can increase
//    effective priority by incrementing a separate counter, keeping the enum
//    as the "base" priority and computing effective = base.value + agingBonus.
//
// Q: What is the "5-state process model"?
// A: The classic OS model: New → Ready → Running → (Blocked | Terminated).
//    Blocked → Ready on I/O completion. This is what all major OS textbooks
//    (Silberschatz, Tanenbaum) define. The 7-state model adds
//    "Suspended Ready" and "Suspended Blocked" for processes swapped to disk.
//
// Q: What's the difference between BLOCKED and WAITING in Java's Thread?
// A: Java's Thread.State.BLOCKED = waiting to acquire a monitor lock.
//    Thread.State.WAITING = waiting indefinitely (Object.wait(), LockSupport.park()).
//    OS BLOCKED ≈ Java BLOCKED + WAITING + TIMED_WAITING combined.
//    They all mean "not runnable, waiting for something external."
