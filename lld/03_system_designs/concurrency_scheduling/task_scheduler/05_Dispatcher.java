// =============================================================================
// FILE: 05_Dispatcher.java
// TOPIC: OS Task Scheduler LLD — Dispatcher (Context Switch + CPU Execution)
// PATTERNS: SRP (only dispatches; CPUScheduler orchestrates)
// =============================================================================

package task_scheduler;

// =============================================================================
// ■ Dispatcher — Executes one CPU tick for the given process
//
// The Dispatcher represents the OS component responsible for:
//   1. Context switch overhead (saving/loading CPU registers)
//   2. Switching the CPU's mode (kernel → user mode)
//   3. Actually giving CPU control to the selected process
//
// In simulation:
//   - dispatch(process, currentTime): runs one clock tick
//     → sets startTime if first execution
//     → decrements remainingTime by 1
//     → returns true if process completed in this tick
//
//   - contextSwitch(outgoing, incoming): logs the switch
//     In a real OS, this takes ~1-10 microseconds (register save/restore).
//     For the simulation, we model it as zero-cost (no penalty tick).
//     Mentioning this in an interview is a great follow-up point.
//
// SRP: Dispatcher only moves processes between states and decrements time.
//      CPUScheduler decides WHEN to dispatch; Dispatcher decides HOW.
// =============================================================================
class Dispatcher {

    private int contextSwitchCount = 0;
    private int idleCount          = 0;   // ticks where no process was available

    /**
     * Execute one CPU tick for the given process.
     *
     * Actions:
     *   1. Record startTime on first execution (for Response Time).
     *   2. Decrement remainingTime by 1.
     *   3. If remainingTime == 0: mark TERMINATED, record completionTime.
     *
     * @param process     the process receiving CPU for this tick
     * @param currentTime the current simulation clock
     * @return            true if process completed in this tick
     */
    boolean dispatch(Process process, int currentTime) {
        // First CPU allocation — record for Response Time metric
        if (!process.hasStarted()) {
            process.setStartTime(currentTime);
        }

        // Execute one unit of CPU time
        process.decrementRemainingTime();

        // Check if process is done
        if (process.isCompleted()) {
            process.setState(ProcessState.TERMINATED);
            process.setCompletionTime(currentTime + 1);   // completes at end of this tick
            return true;
        }
        return false;
    }

    /**
     * Simulate a context switch between two processes.
     * In a real OS, this saves the CPU registers (PC, SP, general registers)
     * of 'outgoing' to its PCB and loads them from 'incoming's PCB.
     *
     * For metrics, we count context switches. A high context-switch count
     * indicates a small quantum in RR or excessive preemption in SRTF.
     *
     * @param outgoing process being preempted (null = no previous process)
     * @param incoming process being dispatched
     */
    void contextSwitch(Process outgoing, Process incoming) {
        if (outgoing != null && outgoing != incoming) {
            contextSwitchCount++;
            // Could add context switch overhead tick here:
            // idleCount++; (1 tick penalty per switch for realism)
        }
    }

    /**
     * Record an idle CPU tick (no process was available in the ready queue).
     * CPU idle happens when all processes have not yet arrived.
     */
    void recordIdle() {
        idleCount++;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int getContextSwitchCount() { return contextSwitchCount; }
    public int getIdleCount()          { return idleCount; }

    /** Reset counters for re-running simulations. */
    void reset() {
        contextSwitchCount = 0;
        idleCount          = 0;
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: What is a context switch and why does it matter?
// A: A context switch is the OS mechanism for saving the CPU state of the
//    outgoing process (registers, PC, SP, flags) into its PCB, and loading
//    the CPU state of the incoming process from its PCB.
//    Cost: ~1-10 microseconds on modern hardware (cache flush, TLB invalidation).
//    High context switch frequency (small RR quantum, aggressive SRTF) directly
//    reduces CPU efficiency — the CPU spends time switching instead of computing.
//    This is why Linux CFS uses a minimum granularity (1ms) to bound overhead.
//
// Q: Why does the Dispatcher only do one tick at a time?
// A: Discrete-event simulation approach. Each tick = 1 time unit.
//    This allows us to model arrival of new processes mid-burst, check
//    preemption conditions after every unit of work, and maintain accurate
//    Gantt chart entries. In real OS, the scheduler runs on timer interrupts
//    (typically 10ms or 1ms granularity — the "scheduling quanta").
//
// Q: What is the TLB and why does context switching invalidate it?
// A: TLB (Translation Lookaside Buffer) = hardware cache of virtual→physical
//    address mappings. Every process has different virtual address spaces.
//    On context switch, the TLB must be flushed (or tagged with ASID —
//    Address Space Identifier to avoid flush cost). TLB flush causes cache
//    misses on subsequent memory accesses of the new process — one reason
//    context switches are expensive beyond just register save/restore.
//
// Q: How does fork() relate to the PCB?
// A: fork() creates a copy of the parent's PCB (new PID, same memory image via
//    Copy-On-Write). The new process enters the READY queue immediately.
//    exec() then replaces the process image (code, data segments) while keeping
//    the same PCB/PID. The scheduler treats forked processes exactly like any
//    other NEW process entering the ready queue.
