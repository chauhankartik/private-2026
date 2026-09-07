// =============================================================================
// FILE: 07_Demo.java
// TOPIC: OS Task Scheduler LLD — Complete Demo (Interview Walkthrough)
//
// Runs all 6 scheduling algorithms on the SAME process set and prints:
//   - Gantt chart for each algorithm
//   - Per-process TAT, WT, RT
//   - Aggregate averages
//   - Comparative summary table
//
// Classic process set (from Silberschatz OS textbook):
//   P1: arrival=0, burst=8, priority=HIGH
//   P2: arrival=1, burst=4, priority=MEDIUM
//   P3: arrival=2, burst=9, priority=LOW
//   P4: arrival=3, burst=5, priority=HIGH
//   P5: arrival=4, burst=2, priority=CRITICAL
//
// RUN: javac -d out task_scheduler/*.java && java -cp out task_scheduler.Demo
// =============================================================================

package task_scheduler;

import java.util.List;

public class Demo {

    public static void main(String[] args) {

        System.out.println("=".repeat(70));
        System.out.println("  OS TASK SCHEDULER LLD — All 6 Algorithms Compared");
        System.out.println("=".repeat(70));

        // ── Define process set (same for all algorithms) ──────────────────────
        List<Process> processes = List.of(
            new Process.Builder(1, "P1").arrivalTime(0).burstTime(8).priority(Priority.HIGH).build(),
            new Process.Builder(2, "P2").arrivalTime(1).burstTime(4).priority(Priority.MEDIUM).build(),
            new Process.Builder(3, "P3").arrivalTime(2).burstTime(9).priority(Priority.LOW).build(),
            new Process.Builder(4, "P4").arrivalTime(3).burstTime(5).priority(Priority.HIGH).build(),
            new Process.Builder(5, "P5").arrivalTime(4).burstTime(2).priority(Priority.CRITICAL).build()
        );

        System.out.println("\nProcess Set:");
        System.out.printf("  %-5s %-8s %8s %8s %-12s%n", "PID", "Name", "Arrival", "Burst", "Priority");
        System.out.println("  " + "─".repeat(50));
        for (Process p : processes) {
            System.out.printf("  P%-4d %-8s %8d %8d %-12s%n",
                    p.getPid(), p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority());
        }

        // ── Run all 6 algorithms ──────────────────────────────────────────────

        SchedulingMetrics fcfsMetrics = runAlgorithm("FCFS",
                new FCFSAlgorithm(), processes);

        SchedulingMetrics sjfMetrics = runAlgorithm("SJF (Non-Preemptive)",
                new SJFAlgorithm(), processes);

        SchedulingMetrics srtfMetrics = runAlgorithm("SRTF (Preemptive SJF)",
                new SRTFAlgorithm(), processes);

        SchedulingMetrics rrMetrics = runAlgorithm("Round Robin (quantum=2)",
                new RoundRobinAlgorithm(2), processes);

        SchedulingMetrics prioMetrics = runAlgorithm("Priority (Preemptive + Aging)",
                new PriorityAlgorithm(), processes);

        SchedulingMetrics mlfqMetrics = runAlgorithm("MLFQ (3-level)",
                new MLFQAlgorithm(), processes);

        // ── Comparative Summary Table ─────────────────────────────────────────
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  COMPARATIVE SUMMARY — All Algorithms");
        System.out.println("=".repeat(70));
        System.out.printf("  %-28s %8s %8s %8s %8s%n",
                "Algorithm", "Avg TAT", "Avg WT", "Avg RT", "CPU%");
        System.out.println("  " + "─".repeat(60));

        printSummaryRow("FCFS",               fcfsMetrics);
        printSummaryRow("SJF",                sjfMetrics);
        printSummaryRow("SRTF",               srtfMetrics);
        printSummaryRow("Round Robin (q=2)",  rrMetrics);
        printSummaryRow("Priority + Aging",   prioMetrics);
        printSummaryRow("MLFQ (3-level)",     mlfqMetrics);

        System.out.println("  " + "─".repeat(60));
        System.out.println("\n  TAT = Turnaround Time | WT = Waiting Time | RT = Response Time");

        // ── Interview Key Points ──────────────────────────────────────────────
        printInterviewPoints();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static SchedulingMetrics runAlgorithm(String label,
                                                   SchedulingAlgorithm algorithm,
                                                   List<Process> processes) {
        System.out.println("\n\n" + "─".repeat(70));
        System.out.println("  ▶ " + label);
        System.out.println("─".repeat(70));

        CPUScheduler scheduler = new CPUScheduler.Builder()
                .algorithm(algorithm)
                .addProcesses(processes)
                .build();

        SchedulingMetrics metrics = scheduler.run();
        metrics.printReport();
        return metrics;
    }

    private static void printSummaryRow(String name, SchedulingMetrics m) {
        System.out.printf("  %-28s %8.2f %8.2f %8.2f %7.1f%%%n",
                name,
                m.getAvgTurnaroundTime(),
                m.getAvgWaitingTime(),
                m.getAvgResponseTime(),
                m.getCpuUtilization());
    }

    private static void printInterviewPoints() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  KEY INTERVIEW TAKEAWAYS");
        System.out.println("=".repeat(70));
        System.out.println("""
            
            ▸ FCFS:  Simplest. No starvation. Convoy effect hurts avg WT.
                     Best for: Batch jobs with similar burst times.

            ▸ SJF:   Optimal avg WT among non-preemptive algorithms (provably).
                     Risk: Long processes starve if short ones keep arriving.
                     Real OS: Predict burst with exponential averaging.

            ▸ SRTF:  Globally optimal avg WT (preemptive SJF).
                     Risk: Highest context switch overhead + starvation.
                     Best for: Theoretical benchmark; rarely used as-is.

            ▸ RR:    Fairest. Best response time for interactive systems.
                     Quantum tuning is critical:
                       Too small → too many context switches.
                       Too large → degenerates to FCFS.
                     Best for: Time-sharing OS (Linux, Windows user processes).

            ▸ Priority: Flexible. Must add AGING to prevent starvation.
                     Best for: Real-time systems with known priority levels.

            ▸ MLFQ:  Self-adaptive. No starvation (with aging/promotion).
                     I/O-bound processes (short bursts) stay in Q0 (best service).
                     CPU-bound processes sink to Q2 (FCFS — still eventually run).
                     Best for: Modern OS schedulers (Linux CFS is spiritually MLFQ).

            ▸ Linux CFS: Uses a red-black tree sorted by virtual runtime.
                     Equivalent to weighted SJF with continuous fairness guarantee.
                     No fixed quantum — granularity = 6ms / log(n_processes).
            """);
    }
}
