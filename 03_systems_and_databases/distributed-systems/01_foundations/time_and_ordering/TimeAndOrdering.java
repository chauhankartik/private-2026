package time_and_ordering;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TimeAndOrdering {

    // -------------------------------------------------------------------------
    // 1. LAMPORT LOGICAL CLOCK
    // -------------------------------------------------------------------------
    public static class LamportClock {
        private final AtomicLong clock = new AtomicLong(0);

        public long tick() {
            return clock.incrementAndGet();
        }

        public long update(long receivedTimestamp) {
            long updated;
            long current;
            do {
                current = clock.get();
                updated = Math.max(current, receivedTimestamp) + 1;
            } while (!clock.compareAndSet(current, updated));
            return updated;
        }

        public long getTime() {
            return clock.get();
        }
    }

    // -------------------------------------------------------------------------
    // 2. VECTOR CLOCK (Causal Order & Conflict Detection)
    // -------------------------------------------------------------------------
    public static class VectorClock {
        private final Map<String, Long> vector = new ConcurrentHashMap<>();

        public VectorClock() {}

        public VectorClock(VectorClock other) {
            this.vector.putAll(other.vector);
        }

        public void tick(String processId) {
            vector.put(processId, vector.getOrDefault(processId, 0L) + 1);
        }

        public void merge(VectorClock messageClock, String processId) {
            for (Map.Entry<String, Long> entry : messageClock.vector.entrySet()) {
                String proc = entry.getKey();
                long val = entry.getValue();
                vector.put(proc, Math.max(vector.getOrDefault(proc, 0L), val));
            }
            tick(processId);
        }

        public boolean isCausallyBefore(VectorClock other) {
            boolean atLeastOneSmaller = false;
            Set<String> allProcesses = new HashSet<>(vector.keySet());
            allProcesses.addAll(other.vector.keySet());

            for (String proc : allProcesses) {
                long myVal = vector.getOrDefault(proc, 0L);
                long otherVal = other.vector.getOrDefault(proc, 0L);
                if (myVal > otherVal) return false;
                if (myVal < otherVal) atLeastOneSmaller = true;
            }
            return atLeastOneSmaller;
        }

        public boolean isConcurrentWith(VectorClock other) {
            return !this.isCausallyBefore(other) && !other.isCausallyBefore(this) && !this.equals(other);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VectorClock that = (VectorClock) o;
            return Objects.equals(vector, that.vector);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vector);
        }

        @Override
        public String toString() {
            return vector.toString();
        }
    }

    // -------------------------------------------------------------------------
    // 3. GOOGLE TRUETIME SIMULATOR (GPS + Atomic Clocks)
    // -------------------------------------------------------------------------
    public static class TrueTimeInterval {
        private final long earliest;
        private final long latest;

        public TrueTimeInterval(long earliest, long latest) {
            this.earliest = earliest;
            this.latest = latest;
        }

        public long getEarliest() { return earliest; }
        public long getLatest() { return latest; }
        public long getEpsilon() { return (latest - earliest) / 2; }

        @Override
        public String toString() {
            return String.format("[%d, %d] (ε = %dms)", earliest, latest, getEpsilon());
        }
    }

    public static class TrueTimeSimulator {
        private final long epsilonMillis;

        public TrueTimeSimulator(long epsilonMillis) {
            this.epsilonMillis = epsilonMillis;
        }

        public TrueTimeInterval now() {
            long realTime = System.currentTimeMillis();
            return new TrueTimeInterval(realTime - epsilonMillis, realTime + epsilonMillis);
        }

        /**
         * TrueTime Commit-Wait Protocol: Waits out uncertainty period (2 * epsilon)
         * to guarantee global external consistency / linearizability.
         */
        public long commitWait(long commitTimestamp) throws InterruptedException {
            System.out.printf("⏳ [TrueTime COMMIT-WAIT] Waiting out uncertainty for commit timestamp %d...%n", commitTimestamp);
            while (now().getEarliest() <= commitTimestamp) {
                Thread.sleep(2);
            }
            System.out.println("✅ Uncertainty period passed! Commit timestamp is globally safe.");
            return commitTimestamp;
        }
    }

    // -------------------------------------------------------------------------
    // DEMO SUITE
    // -------------------------------------------------------------------------
    public static class Demo {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("======================================================================");
            System.out.println(" ⏰ TIME & ORDERING IN DISTRIBUTED SYSTEMS DEMO");
            System.out.println("======================================================================\n");

            // -------------------------------------------------------------------------
            // SCENARIO 1: LAMPORT TIMESTAMPS
            // -------------------------------------------------------------------------
            System.out.println("🔹 SCENARIO 1: Testing Lamport Logical Clocks (Processes P1, P2, P3)...");
            LamportClock p1 = new LamportClock();
            LamportClock p2 = new LamportClock();
            LamportClock p3 = new LamportClock();

            long t1 = p1.tick(); // Event on P1
            System.out.printf("  P1 local event -> Timestamp: %d%n", t1);

            long msgP1toP2 = p1.tick(); // P1 sends message to P2
            System.out.printf("  P1 sends message to P2 -> Attached Timestamp: %d%n", msgP1toP2);

            long t2 = p2.update(msgP1toP2); // P2 receives message
            System.out.printf("  P2 receives message from P1 -> Updated P2 Timestamp: %d%n", t2);

            long t3 = p3.tick();
            System.out.printf("  P3 independent event -> Timestamp: %d%n", t3);
            System.out.println("✅ Lamport Logical Timestamps synchronized.\n");

            // -------------------------------------------------------------------------
            // SCENARIO 2: VECTOR CLOCKS & CONFLICT DETECTION
            // -------------------------------------------------------------------------
            System.out.println("🔹 SCENARIO 2: Testing Vector Clocks (Causal Ordering & Conflict Detection)...");
            VectorClock vcA = new VectorClock();
            VectorClock vcB = new VectorClock();

            vcA.tick("P1"); // P1 event
            System.out.println("  P1 Event 1 -> VectorClock A: " + vcA);

            vcB.merge(vcA, "P2"); // P2 receives P1 message and ticks
            System.out.println("  P2 receives P1 -> VectorClock B: " + vcB);

            // Create concurrent divergent branch
            VectorClock vcC = new VectorClock(vcA);
            vcC.tick("P3"); // P3 independent branch from P1

            System.out.println("  P3 independent branch -> VectorClock C: " + vcC);

            System.out.printf("  Is B causally after A? %b%n", vcA.isCausallyBefore(vcB));
            System.out.printf("  Are B and C concurrent (CONFLITING UPDATES)? %b%n", vcB.isConcurrentWith(vcC));

            if (vcB.isConcurrentWith(vcC)) {
                System.out.println("✅ Vector Clocks successfully detected concurrent branch conflict!");
            }
            System.out.println();

            // -------------------------------------------------------------------------
            // SCENARIO 3: GOOGLE TRUETIME (GPS + ATOMIC CLOCK SIMULATOR)
            // -------------------------------------------------------------------------
            System.out.println("🔹 SCENARIO 3: Testing Google TrueTime API & Commit-Wait Protocol...");
            TrueTimeSimulator trueTime = new TrueTimeSimulator(5); // 5ms uncertainty epsilon

            TrueTimeInterval interval = trueTime.now();
            System.out.println("  TrueTime.now() -> " + interval);

            long commitTs = interval.getLatest();
            System.out.printf("  Transaction 1 assigned commit timestamp s = %d%n", commitTs);

            trueTime.commitWait(commitTs);

            TrueTimeInterval postWaitInterval = trueTime.now();
            System.out.println("  Post-Wait TrueTime.now() -> " + postWaitInterval);
            System.out.println("✅ Global External Consistency (Linearizability) guaranteed by TrueTime!");

            System.out.println("\n======================================================================");
            System.out.println(" ⏰ TIME & ORDERING DEMO COMPLETED SUCCESSFULLY");
            System.out.println("======================================================================");
        }
    }
}
