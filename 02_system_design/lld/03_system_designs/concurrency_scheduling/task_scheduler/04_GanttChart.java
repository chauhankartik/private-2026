// =============================================================================
// FILE: 04_GanttChart.java
// TOPIC: OS Task Scheduler LLD — Gantt Chart + Scheduling Metrics
// PATTERNS: Value Object (SchedulingMetrics), Observer-collected data (GanttChart)
// =============================================================================

package task_scheduler;

import java.util.ArrayList;
import java.util.List;

// =============================================================================
// ■ GanttChart — Records and visualises the CPU execution timeline
//
// A Gantt chart shows which process ran on the CPU at each time unit.
// It is a standard OS scheduling visualization used in every textbook.
//
// Example output:
//   | P1  | P2  | P1  | P3  | P2  |
//   0     3     5     8    10    12
//
// Implementation:
//   - GanttEntry: (pid/label, startTime, endTime)
//   - Consecutive entries for the same process are merged for readability.
//   - IDLE entries are created when no process is available (CPU idle).
// =============================================================================
class GanttChart {

    // ─── GanttEntry — one block in the chart ──────────────────────────────────
    static class GanttEntry {
        final String label;     // "P1" or "IDLE"
        final int    startTime;
        int          endTime;   // mutable: merged with consecutive same-process blocks

        GanttEntry(String label, int startTime, int endTime) {
            this.label     = label;
            this.startTime = startTime;
            this.endTime   = endTime;
        }

        @Override
        public String toString() {
            return label + "[" + startTime + "-" + endTime + "]";
        }
    }

    // ─── State ────────────────────────────────────────────────────────────────

    private final List<GanttEntry> entries = new ArrayList<>();
    private       GanttEntry       last    = null;

    // ─── Recording ────────────────────────────────────────────────────────────

    /**
     * Record one CPU tick: which process (or IDLE) was running.
     * Consecutive ticks for the same process are merged into one entry.
     *
     * @param label      "P1", "P2", or "IDLE"
     * @param tickStart  start of this tick
     */
    void record(String label, int tickStart) {
        if (last != null && last.label.equals(label)) {
            last.endTime = tickStart + 1;    // extend existing entry
        } else {
            GanttEntry entry = new GanttEntry(label, tickStart, tickStart + 1);
            entries.add(entry);
            last = entry;
        }
    }

    // ─── Visualisation ───────────────────────────────────────────────────────

    /**
     * Print the Gantt chart as ASCII art.
     *
     * Example:
     *   ┌──────┬──────┬────┬──────────┐
     *   │  P1  │  P2  │IDLE│    P3    │
     *   └──────┴──────┴────┴──────────┘
     *   0      4      6    7         11
     */
    void print() {
        if (entries.isEmpty()) {
            System.out.println("  [Empty Gantt Chart]");
            return;
        }

        // ── Top border ────────────────────────────────────────────────────────
        StringBuilder topBorder = new StringBuilder("  ┌");
        StringBuilder labels    = new StringBuilder("  │");
        StringBuilder botBorder = new StringBuilder("  └");
        StringBuilder times     = new StringBuilder("  ");

        for (GanttEntry e : entries) {
            int width = Math.max((e.endTime - e.startTime) * 2 + 2, e.label.length() + 2);
            String cell = padCenter(e.label, width);
            topBorder.append("─".repeat(width)).append("┬");
            labels.append(cell).append("│");
            botBorder.append("─".repeat(width)).append("┴");
        }

        // Replace last ┬ with ┐ and last ┴ with ┘
        topBorder.setCharAt(topBorder.length() - 1, '┐');
        botBorder.setCharAt(botBorder.length() - 1, '┘');

        System.out.println(topBorder);
        System.out.println(labels);
        System.out.println(botBorder);

        // ── Time axis ─────────────────────────────────────────────────────────
        int lastPrinted = -1;
        for (GanttEntry e : entries) {
            int width = Math.max((e.endTime - e.startTime) * 2 + 2, e.label.length() + 2);
            String startStr = String.valueOf(e.startTime);
            if (e.startTime != lastPrinted) {
                times.append(startStr);
                lastPrinted = e.startTime;
            }
            // Pad to width + 1 (accounting for the │ border)
            int pad = width + 1 - startStr.length();
            times.append(" ".repeat(Math.max(0, pad)));
        }
        // Append the final time
        if (!entries.isEmpty()) {
            times.append(entries.get(entries.size() - 1).endTime);
        }
        System.out.println(times);
    }

    List<GanttEntry> getEntries() { return entries; }

    // ─── Private helper ───────────────────────────────────────────────────────

    private String padCenter(String text, int width) {
        int padding = width - text.length();
        int left    = padding / 2;
        int right   = padding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }
}


// =============================================================================
// ■ SchedulingMetrics — Immutable snapshot of simulation results
//
// Computed after simulation completes. Contains:
//   - Per-process metrics (TAT, WT, RT) in a formatted table
//   - Aggregate averages
//   - CPU utilization and throughput
//   - The Gantt chart of execution order
//
// Value Object: identity by simulation context, not object reference.
// Immutable after construction — freely shareable and printable.
// =============================================================================
class SchedulingMetrics {

    private final SchedulingPolicy policy;
    private final List<Process>    processes;
    private final GanttChart       ganttChart;
    private final int              totalTime;
    private final int              idleTime;

    SchedulingMetrics(SchedulingPolicy policy, List<Process> processes,
                      GanttChart ganttChart, int totalTime, int idleTime) {
        this.policy     = policy;
        this.processes  = processes;
        this.ganttChart = ganttChart;
        this.totalTime  = totalTime;
        this.idleTime   = idleTime;
    }

    // ─── Aggregate metrics ────────────────────────────────────────────────────

    public double getAvgTurnaroundTime() {
        return processes.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0);
    }

    public double getAvgWaitingTime() {
        return processes.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0);
    }

    public double getAvgResponseTime() {
        return processes.stream()
                .mapToInt(Process::getResponseTime)
                .average()
                .orElse(0);
    }

    public double getCpuUtilization() {
        if (totalTime == 0) return 0;
        return ((double)(totalTime - idleTime) / totalTime) * 100.0;
    }

    public double getThroughput() {
        if (totalTime == 0) return 0;
        return (double) processes.size() / totalTime;
    }

    // ─── Report printing ──────────────────────────────────────────────────────

    /**
     * Print a complete, formatted simulation report:
     * Gantt Chart + per-process table + aggregate metrics.
     */
    public void printReport() {
        System.out.println("┌" + "─".repeat(68) + "┐");
        System.out.printf("│  %-64s  │%n", "SCHEDULING REPORT: " + policy);
        System.out.println("├" + "─".repeat(68) + "┤");

        // Gantt Chart
        System.out.println("│  Gantt Chart:                                                      │");
        ganttChart.print();

        // Per-process table
        System.out.println();
        System.out.printf("  %-5s %-10s %8s %8s %8s %8s %8s%n",
                "PID", "Name", "Arrival", "Burst", "TAT", "WT", "RT");
        System.out.println("  " + "─".repeat(60));
        for (Process p : processes) {
            System.out.printf("  P%-4d %-10s %8d %8d %8d %8d %8d%n",
                    p.getPid(), p.getName(),
                    p.getArrivalTime(), p.getBurstTime(),
                    p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime());
        }
        System.out.println("  " + "─".repeat(60));

        // Aggregate metrics
        System.out.printf("%n  Average Turnaround Time : %.2f ms%n", getAvgTurnaroundTime());
        System.out.printf("  Average Waiting Time    : %.2f ms%n",  getAvgWaitingTime());
        System.out.printf("  Average Response Time   : %.2f ms%n",  getAvgResponseTime());
        System.out.printf("  CPU Utilization         : %.1f%%%n",   getCpuUtilization());
        System.out.printf("  Throughput              : %.4f proc/ms%n%n", getThroughput());
        System.out.println("└" + "─".repeat(68) + "┘");
    }
}
