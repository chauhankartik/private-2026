// =============================================================================
// FILE: 06_CPUScheduler.java
// TOPIC: OS Task Scheduler LLD — Main Simulation Engine
// PATTERNS: Builder (CPUScheduler construction), Strategy (algorithm injection)
// SOLID: SRP — drives the clock; delegates algorithm decisions to SchedulingAlgorithm
// =============================================================================

package task_scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// =============================================================================
// ■ CPUScheduler — The simulation engine
//
// Drives a discrete-time clock from t=0 until all processes terminate.
//
// Core simulation loop (per tick):
//   [1] Admit arrivals: find all processes with arrivalTime == currentTime,
//       call algorithm.addProcess() for each.
//   [2] If no process is running, call selectNext().
//   [3] If a process is running, call onTick() to check for preemption.
//       If preempted: context switch, selectNext() for the new process.
//   [4] Dispatch: run one CPU tick on the current process.
//   [5] Gantt chart: record who ran this tick.
//   [6] tickWaiting: increment waiting counters for all processes in ready queue.
//   [7] If current process completed: record metrics, select next process.
//   [8] Advance clock by 1.
//
// All algorithms share this loop; only the algorithm's internal decisions vary.
// =============================================================================
class CPUScheduler {

    private final SchedulingAlgorithm algorithm;
    private final List<Process>        originalProcesses;  // source of truth (not modified)
    private final Dispatcher           dispatcher = new Dispatcher();

    // Private — only Builder creates instances
    private CPUScheduler(Builder builder) {
        this.algorithm          = builder.algorithm;
        this.originalProcesses  = builder.processes;
    }

    // =========================================================================
    // ■ run() — Execute the full simulation
    //
    // Returns a SchedulingMetrics snapshot with all results.
    // Processes are deep-copied (reset) before each run, so the same
    // CPUScheduler can be re-used for multiple simulations if needed.
    // =========================================================================
    public SchedulingMetrics run() {

        // ── Reset: make fresh copies for this simulation run ──────────────────
        List<Process> processes = deepCopyAndReset(originalProcesses);
        dispatcher.reset();

        // Sort by arrival time for admission control
        List<Process> notYetArrived = new ArrayList<>(processes);
        notYetArrived.sort(Comparator.comparingInt(Process::getArrivalTime));

        GanttChart ganttChart = new GanttChart();
        Process    current    = null;        // process currently on CPU
        int        clock      = 0;           // simulation clock
        int        completed  = 0;           // processes that have terminated

        int maxTime = processes.stream().mapToInt(p -> p.getArrivalTime() + p.getBurstTime()).max().orElse(0) + 1;

        // ── Main simulation loop ──────────────────────────────────────────────
        while (completed < processes.size() && clock <= maxTime) {

            // [1] ADMIT ARRIVALS: processes whose arrivalTime == clock
            boolean newArrival = false;
            for (Process p : new ArrayList<>(notYetArrived)) {
                if (p.getArrivalTime() <= clock) {
                    algorithm.addProcess(p, clock);
                    notYetArrived.remove(p);
                    newArrival = true;
                }
            }

            // [2] CPU IDLE: no process running and none ready
            if (current == null && !algorithm.hasReady()) {
                ganttChart.record("IDLE", clock);
                dispatcher.recordIdle();
                clock++;
                continue;
            }

            // [3] SELECT NEXT if CPU is free
            if (current == null) {
                current = algorithm.selectNext();
                if (current != null) {
                    dispatcher.contextSwitch(null, current);
                }
            }

            // [4] PREEMPTION CHECK (on tick)
            if (current != null) {
                boolean preempted = algorithm.onTick(current, clock);
                if (preempted && !current.isCompleted()) {
                    // current was put back in ready queue by algorithm.onTick()
                    Process next = algorithm.selectNext();
                    if (next != null && next != current) {
                        dispatcher.contextSwitch(current, next);
                        current = next;
                    } else if (next == current) {
                        // same process won again (e.g., nothing better in queue)
                        current.setState(ProcessState.RUNNING);
                    }
                } else if (preempted && current.isCompleted()) {
                    // RR onTick tried to re-enqueue but process is done — handle below
                    // (Dispatcher will mark completed after dispatch below)
                }
            }

            // [5] DISPATCH: run one CPU tick
            if (current != null) {
                ganttChart.record("P" + current.getPid(), clock);

                // Tick waiting for all processes in the ready queue
                algorithm.tickWaiting(clock);

                boolean justCompleted = dispatcher.dispatch(current, clock);

                if (justCompleted) {
                    completed++;
                    current = null;        // free the CPU for next tick's selectNext()
                }
            }

            clock++;

            // [6] After tick: if current finished, select next at start of next loop
            //     If preemption re-enqueued the process, it's back in ready queue.
            //     If still running, current remains.
        }

        return new SchedulingMetrics(
                algorithm.getPolicy(),
                processes,
                ganttChart,
                clock,
                dispatcher.getIdleCount());
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /** Create fresh copies of all processes (reset runtime state). */
    private List<Process> deepCopyAndReset(List<Process> original) {
        List<Process> copies = new ArrayList<>();
        for (Process p : original) {
            Process copy = new Process.Builder(p.getPid(), p.getName())
                    .arrivalTime(p.getArrivalTime())
                    .burstTime(p.getBurstTime())
                    .priority(p.getPriority())
                    .build();
            copies.add(copy);
        }
        return copies;
    }


    // =========================================================================
    // ■ CPUScheduler.Builder — Fluent simulation setup
    //
    // Usage:
    //   SchedulingMetrics metrics = new CPUScheduler.Builder()
    //       .algorithm(new RoundRobinAlgorithm(2))
    //       .addProcess(new Process.Builder(1, "P1").arrivalTime(0).burstTime(6).build())
    //       .addProcess(new Process.Builder(2, "P2").arrivalTime(2).burstTime(4).build())
    //       .build()
    //       .run();
    //   metrics.printReport();
    // =========================================================================
    static final class Builder {

        private SchedulingAlgorithm algorithm;
        private final List<Process>  processes = new ArrayList<>();

        Builder algorithm(SchedulingAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        Builder addProcess(Process p) {
            this.processes.add(p);
            return this;
        }

        Builder addProcesses(List<Process> list) {
            this.processes.addAll(list);
            return this;
        }

        CPUScheduler build() {
            if (algorithm == null)
                throw new IllegalStateException("A SchedulingAlgorithm is required.");
            if (processes.isEmpty())
                throw new IllegalStateException("At least one Process is required.");
            return new CPUScheduler(this);
        }
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Walk me through the main simulation loop.
// A: Each iteration of the while loop = 1 CPU clock tick.
//    1. Admit all processes that have arrived by this tick.
//    2. If CPU is idle and nothing is ready → mark IDLE, advance clock.
//    3. If CPU is free → selectNext() from the algorithm.
//    4. Per tick, call algorithm.onTick(current) → may signal preemption.
//       If preempted: context switch, select new process.
//    5. Dispatch (run one tick for current process).
//    6. Record in Gantt chart.
//    7. If process completes → free CPU.
//    Loop terminates when all processes are TERMINATED.
//
// Q: How does the Simulator handle a process completing mid-quantum in RR?
// A: RR.onTick() increments quantumUsed and checks >= quantum. But if the
//    process completes (remainingTime → 0) in dispatcher.dispatch() BEFORE the
//    quantum expires, the process is marked TERMINATED and removed from CPU.
//    The key is: dispatcher.dispatch() runs before the preemption re-enqueue takes
//    effect. We check isCompleted() after dispatch and handle accordingly.
//
// Q: Why deep-copy processes before each simulation run?
// A: So that multiple algorithms can be compared on the SAME process set.
//    In Demo.java, we run all 6 algorithms on identical processes. Without reset,
//    the second algorithm would see completionTime already set, remainingTime=0
//    from the first run — completely invalid simulation. Deep-copy + reset()
//    ensures each simulation starts with clean process state.
//
// Q: How would you extend this to multi-core (SMP) scheduling?
// A: Maintain multiple CPUScheduler instances (one per core). Add a
//    ProcessMigrationPolicy to decide: should a process migrate between cores?
//    Key challenge: cache affinity (a process should prefer the core whose cache
//    already has its working set). Linux's scheduler uses per-CPU run queues
//    with periodic load balancing via work-stealing.
