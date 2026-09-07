// =============================================================================
// FILE: 02_Process.java
// TOPIC: OS Task Scheduler LLD — Process Control Block (PCB)
// PATTERNS: Builder (Process construction), Mutable runtime state
// =============================================================================

package task_scheduler;

// =============================================================================
// ■ Process — The Process Control Block (PCB)
//
// In a real OS, the PCB is the data structure the kernel maintains for
// every process. It includes: process ID, CPU registers, memory maps,
// open files, I/O state, accounting info.
//
// For this LLD simulation, we focus on scheduling-relevant fields:
//
// IDENTITY (set at creation, immutable):
//   pid          — unique process identifier
//   name         — human-readable label
//   arrivalTime  — when this process enters the ready queue (t=arrival)
//   burstTime    — total CPU time required to complete
//   priority     — scheduling priority (used by Priority/MLFQ algorithms)
//
// RUNTIME (mutable — updated by Dispatcher and Scheduler):
//   state          — current ProcessState (NEW/READY/RUNNING/TERMINATED)
//   remainingTime  — burstTime - already executed ticks
//   startTime      — first time CPU was allocated (for Response Time)
//   completionTime — when the process finished (for Turnaround Time)
//   waitingTime    — accumulated time spent in READY state
//   responseTime   — startTime - arrivalTime (set once, first CPU allocation)
//   currentQueue   — MLFQ queue level this process is currently in (0=top)
//   agingCounter   — ticks spent waiting in READY; used for aging in Priority
//
// Pattern: Builder — process has many fields; Builder avoids telescoping
//          constructors and clearly separates required from optional fields.
// =============================================================================
class Process implements Comparable<Process> {

    // ─── Identity (immutable) ─────────────────────────────────────────────────

    private final int      pid;
    private final String   name;
    private final int      arrivalTime;
    private final int      burstTime;
    private final Priority priority;

    // ─── Runtime state (mutable) ──────────────────────────────────────────────

    private ProcessState state            = ProcessState.NEW;
    private int          remainingTime;   // starts = burstTime
    private int          startTime        = -1;    // -1 = not yet started
    private int          completionTime   = -1;
    private int          waitingTimeTicks = 0;     // accumulated READY ticks
    private int          currentQueue     = 0;     // MLFQ level (0 = highest priority)
    private int          agingCounter     = 0;     // ticks spent waiting (for aging)
    private int          quantumUsed      = 0;     // ticks used in current quantum

    // Private — only Builder creates Process instances
    private Process(Builder builder) {
        this.pid           = builder.pid;
        this.name          = builder.name;
        this.arrivalTime   = builder.arrivalTime;
        this.burstTime     = builder.burstTime;
        this.priority      = builder.priority;
        this.remainingTime = builder.burstTime;    // starts as full burst
    }

    // ─── Identity Getters ─────────────────────────────────────────────────────

    public int      getPid()          { return pid; }
    public String   getName()         { return name; }
    public int      getArrivalTime()  { return arrivalTime; }
    public int      getBurstTime()    { return burstTime; }
    public Priority getPriority()     { return priority; }

    // ─── Runtime State Getters / Setters ──────────────────────────────────────

    public ProcessState getState()              { return state; }
    public void         setState(ProcessState s){ this.state = s; }

    public int  getRemainingTime()                 { return remainingTime; }
    public void setRemainingTime(int t)            { this.remainingTime = t; }
    public void decrementRemainingTime()           { this.remainingTime--; }
    public boolean isCompleted()                   { return remainingTime <= 0; }

    public int  getStartTime()                     { return startTime; }
    public boolean hasStarted()                    { return startTime != -1; }
    /** Record the first CPU allocation time (called once by Dispatcher). */
    public void setStartTime(int t)                {
        if (startTime == -1) this.startTime = t;   // set only once
    }

    public int  getCompletionTime()                { return completionTime; }
    public void setCompletionTime(int t)           { this.completionTime = t; }

    public int  getWaitingTimeTicks()              { return waitingTimeTicks; }
    public void incrementWaitingTime()             { this.waitingTimeTicks++; }

    public int  getCurrentQueue()                  { return currentQueue; }
    public void setCurrentQueue(int q)             { this.currentQueue = q; }

    public int  getAgingCounter()                  { return agingCounter; }
    public void incrementAgingCounter()            { this.agingCounter++; }
    public void resetAgingCounter()                { this.agingCounter = 0; }

    public int  getQuantumUsed()                   { return quantumUsed; }
    public void incrementQuantumUsed()             { this.quantumUsed++; }
    public void resetQuantumUsed()                 { this.quantumUsed = 0; }

    // ─── Derived Metrics (computed after completion) ──────────────────────────

    /**
     * Turnaround Time = Completion Time - Arrival Time
     * Total time from submission to completion.
     */
    public int getTurnaroundTime() {
        if (completionTime == -1) return -1;
        return completionTime - arrivalTime;
    }

    /**
     * Waiting Time = Turnaround Time - Burst Time
     * Time spent in READY queue (not on CPU, not doing I/O).
     */
    public int getWaitingTime() {
        if (completionTime == -1) return -1;
        return getTurnaroundTime() - burstTime;
    }

    /**
     * Response Time = Start Time - Arrival Time
     * Time from submission to FIRST CPU allocation.
     * Key metric for interactive systems.
     */
    public int getResponseTime() {
        if (startTime == -1) return -1;
        return startTime - arrivalTime;
    }

    // ─── Comparable for priority queues ──────────────────────────────────────

    /**
     * Natural ordering by arrival time (used by FCFS).
     * Other orderings are defined in algorithm-specific Comparators.
     */
    @Override
    public int compareTo(Process other) {
        return Integer.compare(this.arrivalTime, other.arrivalTime);
    }

    // ─── Reset for re-running simulations ─────────────────────────────────────

    /** Reset all runtime state so the same process can be re-used in another simulation. */
    void reset() {
        this.state            = ProcessState.NEW;
        this.remainingTime    = this.burstTime;
        this.startTime        = -1;
        this.completionTime   = -1;
        this.waitingTimeTicks = 0;
        this.currentQueue     = 0;
        this.agingCounter     = 0;
        this.quantumUsed      = 0;
    }

    @Override
    public String toString() {
        return String.format("P%-2d(%-8s arr=%d burst=%d pri=%-8s rem=%d)",
                pid, name, arrivalTime, burstTime, priority, remainingTime);
    }


    // =========================================================================
    // ■ Process.Builder
    //
    // Usage:
    //   Process p = new Process.Builder(1, "P1")
    //       .arrivalTime(0)
    //       .burstTime(8)
    //       .priority(Priority.HIGH)
    //       .build();
    // =========================================================================
    static final class Builder {

        private final int    pid;
        private final String name;
        private int          arrivalTime = 0;
        private int          burstTime;
        private Priority     priority    = Priority.MEDIUM;

        Builder(int pid, String name) {
            if (name == null || name.isBlank())
                throw new IllegalArgumentException("Process name must not be blank");
            this.pid  = pid;
            this.name = name;
        }

        Builder arrivalTime(int t) {
            if (t < 0) throw new IllegalArgumentException("arrivalTime must be >= 0");
            this.arrivalTime = t;
            return this;
        }

        Builder burstTime(int b) {
            if (b <= 0) throw new IllegalArgumentException("burstTime must be > 0");
            this.burstTime = b;
            return this;
        }

        Builder priority(Priority p) {
            this.priority = p;
            return this;
        }

        Process build() {
            if (burstTime <= 0) throw new IllegalStateException("burstTime is required");
            return new Process(this);
        }
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: What is a Process Control Block (PCB) in a real OS?
// A: The PCB is the OS kernel's data structure representing a process. It stores:
//    - Process ID, state, program counter
//    - CPU register values (saved on context switch)
//    - Memory management info (page tables, segment tables)
//    - Accounting (CPU time used, start time, user ID)
//    - I/O status (open files, pending I/O requests)
//    On context switch, the CPU register state is saved to the outgoing PCB and
//    loaded from the incoming PCB. Our Process class is a simplified PCB.
//
// Q: What are the 3 key scheduling metrics and their formulae?
// A: 1. Turnaround Time = Completion - Arrival (total elapsed time)
//    2. Waiting Time = Turnaround - Burst (time wasted in ready queue)
//    3. Response Time = First CPU start - Arrival (first feel of CPU)
//    Response Time ≤ Waiting Time always.
//    For non-preemptive algorithms: Response Time = Waiting Time
//    For preemptive algorithms (RR): Response Time << Waiting Time (gets CPU early)
//
// Q: Why store 'agingCounter' in the Process?
// A: Aging is the technique to prevent starvation in Priority scheduling.
//    Each time tick a process spends in READY state, agingCounter increments.
//    The scheduler computes effectivePriority = basePriority.value + agingCounter/k
//    for some aging rate k. This ensures a LOW process eventually overtakes
//    a MEDIUM process that just arrived. agingCounter is reset when the process
//    gets CPU time.
//
// Q: Why 'quantumUsed' on the Process rather than tracked in the algorithm?
// A: For MLFQ, quantumUsed determines when to demote to a lower queue.
//    If tracked only in the algorithm, it's lost on context switch.
//    Storing on the Process makes it part of the "context" that survives
//    preemption — the process remembers how much of its quantum it consumed.
