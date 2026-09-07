// =============================================================================
// FILE: 03_SchedulingAlgorithm.java
// TOPIC: OS Task Scheduler LLD — All Scheduling Algorithms
// PATTERNS: Strategy (interface + 6 concrete implementations), Template Method
// SOLID: OCP — new algorithm = new class; CPUScheduler never changes
// =============================================================================

package task_scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

// =============================================================================
// ■ SchedulingAlgorithm — Strategy interface
//
// Every scheduling algorithm must:
//   1. addProcess(p, currentTime): admit a process to the ready queue
//   2. selectNext(): pick the next process to run (the core algorithm decision)
//   3. onTick(currentProcess, currentTime): per-tick hook (aging, preemption checks)
//   4. hasReady(): whether any process is currently waiting
//   5. getPolicy(): which algorithm this is (for labelling)
//
// CPUScheduler drives the simulation clock and calls these methods.
// The algorithm manages its own internal data structures (queues/heaps).
// =============================================================================
interface SchedulingAlgorithm {

    /**
     * Add an arrived process to the algorithm's ready queue.
     * @param process     process that has just arrived (arrivalTime <= currentTime)
     * @param currentTime the current simulation clock tick
     */
    void addProcess(Process process, int currentTime);

    /**
     * Select and remove the next process to dispatch.
     * @return the process to run next; null if no process is available
     */
    Process selectNext();

    /**
     * Called every time-tick while a process is running.
     * Used for: aging updates, preemption checks, quantum exhaustion.
     *
     * @param current     the process currently on CPU
     * @param currentTime current simulation clock
     * @return true = preempt (put current back in queue, schedule new); false = continue
     */
    boolean onTick(Process current, int currentTime);

    /**
     * Called every tick for ALL processes in the ready queue.
     * Used to increment waiting time counters and aging.
     * @param currentTime current simulation clock
     */
    void tickWaiting(int currentTime);

    /** Returns true if there is at least one process ready to run. */
    boolean hasReady();

    /** The scheduling policy this algorithm implements. */
    SchedulingPolicy getPolicy();
}


// =============================================================================
// ■ FCFSAlgorithm — First Come First Served
//
// Data structure: FIFO Queue (arrival order)
// Preemptive: No
//
// selectNext(): dequeue head (earliest arrival)
// onTick():     always return false (non-preemptive — never interrupts)
//
// Weakness: Convoy Effect — if P1 needs 100ms and P2 needs 1ms, P2 waits
//           the full 100ms even though it could finish almost instantly.
// =============================================================================
class FCFSAlgorithm implements SchedulingAlgorithm {

    private final Queue<Process> readyQueue = new LinkedList<>();

    @Override
    public void addProcess(Process p, int currentTime) {
        p.setState(ProcessState.READY);
        readyQueue.offer(p);
    }

    @Override
    public Process selectNext() {
        Process p = readyQueue.poll();
        if (p != null) p.setState(ProcessState.RUNNING);
        return p;
    }

    @Override
    public boolean onTick(Process current, int currentTime) {
        return false;   // non-preemptive: NEVER interrupt a running process
    }

    @Override
    public void tickWaiting(int currentTime) {
        readyQueue.forEach(Process::incrementWaitingTime);
    }

    @Override
    public boolean hasReady() { return !readyQueue.isEmpty(); }

    @Override
    public SchedulingPolicy getPolicy() { return SchedulingPolicy.FCFS; }
}


// =============================================================================
// ■ SJFAlgorithm — Shortest Job First (Non-Preemptive)
//
// Data structure: Min-Heap by burst time (among processes that have arrived)
// Preemptive: No
//
// Once a process starts, it runs to completion — no preemption.
// Optimal: Proven to give minimum average waiting time (among non-preemptive).
// Weakness: Starvation — long jobs may never run if short jobs keep arriving.
// =============================================================================
class SJFAlgorithm implements SchedulingAlgorithm {

    // Min-heap: smallest burst time first; tie-break by arrival time
    private final PriorityQueue<Process> readyQueue =
            new PriorityQueue<>(Comparator.comparingInt(Process::getBurstTime)
                                          .thenComparingInt(Process::getArrivalTime));

    @Override
    public void addProcess(Process p, int currentTime) {
        p.setState(ProcessState.READY);
        readyQueue.offer(p);
    }

    @Override
    public Process selectNext() {
        Process p = readyQueue.poll();
        if (p != null) p.setState(ProcessState.RUNNING);
        return p;
    }

    @Override
    public boolean onTick(Process current, int currentTime) {
        return false;   // non-preemptive: never interrupt
    }

    @Override
    public void tickWaiting(int currentTime) {
        readyQueue.forEach(Process::incrementWaitingTime);
    }

    @Override
    public boolean hasReady() { return !readyQueue.isEmpty(); }

    @Override
    public SchedulingPolicy getPolicy() { return SchedulingPolicy.SJF; }
}


// =============================================================================
// ■ SRTFAlgorithm — Shortest Remaining Time First (Preemptive SJF)
//
// Data structure: Min-Heap by REMAINING time (re-evaluated every tick)
// Preemptive: Yes — preempts if a new arrival has shorter remaining time
//
// onTick(): check if any ready process has remaining < current.remaining
//           If yes → preempt (put current back in queue, return true)
//
// Optimal: Best possible average waiting time (theoretical lower bound).
// Weakness: High context switch overhead; starvation of long processes.
// =============================================================================
class SRTFAlgorithm implements SchedulingAlgorithm {

    private final PriorityQueue<Process> readyQueue =
            new PriorityQueue<>(Comparator.comparingInt(Process::getRemainingTime)
                                          .thenComparingInt(Process::getArrivalTime));

    @Override
    public void addProcess(Process p, int currentTime) {
        p.setState(ProcessState.READY);
        readyQueue.offer(p);
    }

    @Override
    public Process selectNext() {
        Process p = readyQueue.poll();
        if (p != null) p.setState(ProcessState.RUNNING);
        return p;
    }

    /**
     * Preempt if the head of the ready queue has shorter remaining time than current.
     * This is the key difference from SJF: we re-evaluate every tick.
     */
    @Override
    public boolean onTick(Process current, int currentTime) {
        if (readyQueue.isEmpty()) return false;
        Process shortest = readyQueue.peek();
        if (shortest.getRemainingTime() < current.getRemainingTime()) {
            // Preempt: put current back (it's already had its remaining decremented by dispatcher)
            current.setState(ProcessState.READY);
            readyQueue.offer(current);
            return true;    // signal CPUScheduler to call selectNext()
        }
        return false;
    }

    @Override
    public void tickWaiting(int currentTime) {
        readyQueue.forEach(Process::incrementWaitingTime);
    }

    @Override
    public boolean hasReady() { return !readyQueue.isEmpty(); }

    @Override
    public SchedulingPolicy getPolicy() { return SchedulingPolicy.SRTF; }
}


// =============================================================================
// ■ RoundRobinAlgorithm — Circular, Time-Quantum Based
//
// Data structure: FIFO Circular Queue
// Preemptive: Yes — forced preemption when quantum expires
// Time Quantum (q): configurable (default 2 ticks)
//
// onTick(): increment quantumUsed; if >= quantum → preempt (re-enqueue at tail)
//
// Key insight: q too small → too many context switches (overhead dominates)
//              q too large → degenerates to FCFS
//              Rule of thumb: q > 80% of CPU bursts → only 20% preemptions
// =============================================================================
class RoundRobinAlgorithm implements SchedulingAlgorithm {

    private final Queue<Process> readyQueue = new LinkedList<>();
    private final int            quantum;

    RoundRobinAlgorithm(int quantum) {
        if (quantum <= 0) throw new IllegalArgumentException("quantum must be > 0");
        this.quantum = quantum;
    }

    @Override
    public void addProcess(Process p, int currentTime) {
        p.setState(ProcessState.READY);
        p.resetQuantumUsed();   // fresh quantum for newly admitted process
        readyQueue.offer(p);
    }

    @Override
    public Process selectNext() {
        Process p = readyQueue.poll();
        if (p != null) {
            p.setState(ProcessState.RUNNING);
            p.resetQuantumUsed();
        }
        return p;
    }

    /**
     * Quantum-based preemption: increment quantumUsed each tick.
     * If quantumUsed == quantum → re-enqueue at tail, signal preemption.
     */
    @Override
    public boolean onTick(Process current, int currentTime) {
        current.incrementQuantumUsed();
        if (current.getQuantumUsed() >= quantum) {
            // Quantum expired — preempt (if process still has remaining time)
            if (!current.isCompleted()) {
                current.setState(ProcessState.READY);
                current.resetQuantumUsed();
                readyQueue.offer(current);   // re-enqueue at tail (circular)
            }
            return true;
        }
        return false;
    }

    @Override
    public void tickWaiting(int currentTime) {
        readyQueue.forEach(Process::incrementWaitingTime);
    }

    @Override
    public boolean hasReady() { return !readyQueue.isEmpty(); }

    @Override
    public SchedulingPolicy getPolicy() { return SchedulingPolicy.ROUND_ROBIN; }

    public int getQuantum() { return quantum; }
}


// =============================================================================
// ■ PriorityAlgorithm — Preemptive Priority Scheduling with Aging
//
// Data structure: Max-Heap by effectivePriority = base.value + agingBonus
// Preemptive: Yes — higher priority arrival preempts lower priority running
//
// Aging: every AGING_RATE ticks a waiting process gets +1 effective priority.
//        This prevents indefinite starvation of LOW processes.
//
// onTick(): check if any ready process has higher effective priority than current.
//           If yes → preempt.
// =============================================================================
class PriorityAlgorithm implements SchedulingAlgorithm {

    private static final int AGING_RATE = 5;   // +1 effective priority every 5 wait ticks

    // Max-heap: highest effective priority first
    private final PriorityQueue<Process> readyQueue =
            new PriorityQueue<>(Comparator.comparingInt(this::effectivePriority).reversed()
                                          .thenComparingInt(Process::getArrivalTime));

    @Override
    public void addProcess(Process p, int currentTime) {
        p.setState(ProcessState.READY);
        p.resetAgingCounter();
        readyQueue.offer(p);
    }

    @Override
    public Process selectNext() {
        // Rebuild heap to apply latest aging values before picking
        List<Process> all = new ArrayList<>(readyQueue);
        readyQueue.clear();
        readyQueue.addAll(all);

        Process p = readyQueue.poll();
        if (p != null) {
            p.setState(ProcessState.RUNNING);
            p.resetAgingCounter();   // reset aging when finally scheduled
        }
        return p;
    }

    /**
     * Preempt if a waiting process now has higher effective priority than current.
     */
    @Override
    public boolean onTick(Process current, int currentTime) {
        if (readyQueue.isEmpty()) return false;
        // Find the max-priority waiting process
        int maxWaitingPriority = readyQueue.stream()
                .mapToInt(this::effectivePriority)
                .max()
                .orElse(Integer.MIN_VALUE);
        if (maxWaitingPriority > effectivePriority(current)) {
            current.setState(ProcessState.READY);
            readyQueue.offer(current);
            return true;
        }
        return false;
    }

    @Override
    public void tickWaiting(int currentTime) {
        readyQueue.forEach(p -> {
            p.incrementWaitingTime();
            p.incrementAgingCounter();
        });
    }

    @Override
    public boolean hasReady() { return !readyQueue.isEmpty(); }

    @Override
    public SchedulingPolicy getPolicy() { return SchedulingPolicy.PRIORITY; }

    /** Effective priority = base priority value + aging bonus. */
    private int effectivePriority(Process p) {
        return p.getPriority().getValue() + (p.getAgingCounter() / AGING_RATE);
    }
}


// =============================================================================
// ■ MLFQAlgorithm — Multi-Level Feedback Queue
//
// Three queues:
//   Q0: Round Robin, quantum=2  (highest priority — new processes enter here)
//   Q1: Round Robin, quantum=4  (demoted from Q0)
//   Q2: FCFS                    (demoted from Q1 — CPU-bound, lowest priority)
//
// Rules:
//   - New process → Q0
//   - If process uses full quantum in Qi → demote to Q(i+1)
//   - If process completes within quantum → stays in same queue level
//   - Q0 is always served before Q1; Q1 before Q2 (strict priority)
//   - Aging: if process waits in Q1/Q2 too long → promote back to Q0
//
// This is the closest approximation to how real OS schedulers (Linux O(1),
// Windows scheduler) actually work.
// =============================================================================
class MLFQAlgorithm implements SchedulingAlgorithm {

    private static final int[] QUANTUM      = {2, 4, Integer.MAX_VALUE};  // Q0, Q1, Q2
    private static final int   AGING_TICKS  = 10;  // promote after 10 wait ticks
    private static final int   NUM_QUEUES   = 3;

    @SuppressWarnings("unchecked")
    private final Queue<Process>[] queues = new Queue[NUM_QUEUES];

    MLFQAlgorithm() {
        for (int i = 0; i < NUM_QUEUES; i++) {
            queues[i] = new LinkedList<>();
        }
    }

    @Override
    public void addProcess(Process p, int currentTime) {
        p.setState(ProcessState.READY);
        p.setCurrentQueue(0);       // all new processes enter Q0
        p.resetQuantumUsed();
        p.resetAgingCounter();
        queues[0].offer(p);
    }

    @Override
    public Process selectNext() {
        // Strict priority: serve Q0 first, then Q1, then Q2
        for (int i = 0; i < NUM_QUEUES; i++) {
            if (!queues[i].isEmpty()) {
                Process p = queues[i].poll();
                p.setState(ProcessState.RUNNING);
                p.resetQuantumUsed();
                return p;
            }
        }
        return null;
    }

    /**
     * Per-tick: increment quantum used. If quantum for this level exhausted → demote.
     */
    @Override
    public boolean onTick(Process current, int currentTime) {
        current.incrementQuantumUsed();
        int level = current.getCurrentQueue();
        if (current.getQuantumUsed() >= QUANTUM[level]) {
            // Quantum exhausted — demote (if not at lowest level already)
            if (!current.isCompleted()) {
                int nextLevel = Math.min(level + 1, NUM_QUEUES - 1);
                current.setCurrentQueue(nextLevel);
                current.setState(ProcessState.READY);
                current.resetQuantumUsed();
                queues[nextLevel].offer(current);
            }
            return true;
        }
        return false;
    }

    @Override
    public void tickWaiting(int currentTime) {
        for (int i = 0; i < NUM_QUEUES; i++) {
            for (Process p : queues[i]) {
                p.incrementWaitingTime();
                p.incrementAgingCounter();
                // Aging: promote back to Q0 if waited too long
                if (p.getAgingCounter() >= AGING_TICKS && i > 0) {
                    queues[i].remove(p);
                    p.setCurrentQueue(0);
                    p.resetAgingCounter();
                    p.resetQuantumUsed();
                    queues[0].offer(p);
                    break;   // restart loop after structural modification
                }
            }
        }
    }

    @Override
    public boolean hasReady() {
        for (Queue<Process> q : queues) {
            if (!q.isEmpty()) return true;
        }
        return false;
    }

    @Override
    public SchedulingPolicy getPolicy() { return SchedulingPolicy.MLFQ; }

    /** Returns which queue level a process is currently in. */
    int getQueueLevel(Process p) { return p.getCurrentQueue(); }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why is SchedulingAlgorithm a Strategy interface?
// A: CPUScheduler is algorithm-agnostic. It drives the simulation clock,
//    manages process arrivals, and collects metrics. The ONLY thing that differs
//    between algorithms is: which process runs next? That decision is delegated
//    to the SchedulingAlgorithm. Injecting a different strategy changes the
//    entire scheduling behavior without touching CPUScheduler.
//
// Q: Why separate addProcess() from selectNext()?
// A: addProcess() = arrival event (process enters ready queue at clock tick t).
//    selectNext() = dispatch event (CPU picks next process).
//    They happen at different times in the simulation clock. SJF and SRTF
//    need to rebuild their heap after new arrivals — addProcess is the hook.
//
// Q: Is Round Robin truly fair?
// A: It's fair in terms of CPU time distribution — each process gets equal
//    quantum slices. But it's not optimal: if all processes need 2ms and q=2,
//    RR and FCFS give the same result. RR excels for interactive workloads where
//    quick response time matters more than raw throughput.
//
// Q: How does Linux's CFS (Completely Fair Scheduler) relate to these algorithms?
// A: CFS replaced the O(1) scheduler in Linux 2.6.23. It uses a red-black tree
//    sorted by "virtual runtime" (time each process has used, normalized by priority).
//    It always runs the process with the smallest virtual runtime — effectively a
//    weighted SJF that also handles priority. The quantum is dynamic (6ms / log(n)
//    active processes). It eliminates starvation by design (all virtual runtimes
//    converge over time).
