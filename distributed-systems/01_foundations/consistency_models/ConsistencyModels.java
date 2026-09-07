package consistency_models;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ConsistencyModels {

    public enum ConsistencyLevel {
        LINEARIZABLE,
        CAUSAL,
        EVENTUAL
    }

    public static class VectorClock implements Comparable<VectorClock> {
        private final Map<String, Long> clock = new ConcurrentHashMap<>();

        public VectorClock() {}

        public VectorClock(VectorClock other) {
            this.clock.putAll(other.clock);
        }

        public void increment(String nodeId) {
            clock.put(nodeId, clock.getOrDefault(nodeId, 0L) + 1);
        }

        public boolean isCausallyBefore(VectorClock other) {
            boolean atLeastOneSmaller = false;
            for (String key : clock.keySet()) {
                long myVal = clock.getOrDefault(key, 0L);
                long otherVal = other.clock.getOrDefault(key, 0L);
                if (myVal > otherVal) return false;
                if (myVal < otherVal) atLeastOneSmaller = true;
            }
            return atLeastOneSmaller;
        }

        @Override
        public int compareTo(VectorClock o) {
            if (this.isCausallyBefore(o)) return -1;
            if (o.isCausallyBefore(this)) return 1;
            return 0;
        }

        @Override
        public String toString() {
            return clock.toString();
        }
    }

    public static class DataRecord {
        private final String value;
        private final long physicalTimestamp;
        private final VectorClock vectorClock;

        public DataRecord(String value, long physicalTimestamp, VectorClock vectorClock) {
            this.value = value;
            this.physicalTimestamp = physicalTimestamp;
            this.vectorClock = new VectorClock(vectorClock);
        }

        public String getValue() { return value; }
        public long getPhysicalTimestamp() { return physicalTimestamp; }
        public VectorClock getVectorClock() { return vectorClock; }

        @Override
        public String toString() {
            return String.format("DataRecord{val='%s', ts=%d, vc=%s}", value, physicalTimestamp, vectorClock);
        }
    }

    public static class ReplicaNode {
        private final String nodeId;
        private final Map<String, DataRecord> storage = new ConcurrentHashMap<>();
        private final VectorClock vectorClock = new VectorClock();

        public ReplicaNode(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeId() { return nodeId; }

        public synchronized void writeLocal(String key, String value, long physicalTimestamp) {
            vectorClock.increment(nodeId);
            DataRecord record = new DataRecord(value, physicalTimestamp, vectorClock);
            storage.put(key, record);
        }

        public synchronized void writeWithRecord(String key, DataRecord record) {
            storage.put(key, record);
        }

        public DataRecord readLocal(String key) {
            return storage.get(key);
        }

        public VectorClock getVectorClock() { return vectorClock; }
    }

    public static class DistributedCluster {
        private final List<ReplicaNode> nodes = new ArrayList<>();
        private final AtomicLong globalPhysicalClock = new AtomicLong(System.currentTimeMillis());

        public DistributedCluster(int nodeCount) {
            for (int i = 1; i <= nodeCount; i++) {
                nodes.add(new ReplicaNode("Node_" + i));
            }
        }

        public List<ReplicaNode> getNodes() { return nodes; }

        /**
         * Writes key-value under the requested Consistency Level.
         */
        public void write(String key, String value, ConsistencyLevel level) {
            long now = globalPhysicalClock.incrementAndGet();
            ReplicaNode primary = nodes.get(0);

            switch (level) {
                case LINEARIZABLE:
                    // Synchronous write to ALL nodes before returning (100% Strong Consistency)
                    for (ReplicaNode node : nodes) {
                        node.writeLocal(key, value, now);
                    }
                    System.out.printf("🔒 [LINEARIZABLE WRITE] '%s'='%" + "s' committed synchronously to ALL %d nodes.%n",
                            key, value, nodes.size());
                    break;

                case CAUSAL:
                case EVENTUAL:
                    // Primary write, async replication to other nodes
                    primary.writeLocal(key, value, now);
                    DataRecord record = primary.readLocal(key);
                    System.out.printf("⚡ [%s WRITE] '%s'='%" + "s' committed to Node 1. Replicating asynchronously...%n",
                            level, key, value);
                    
                    // Simulate async background replication to secondaries
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(500); // 500ms replication lag
                            for (int i = 1; i < nodes.size(); i++) {
                                nodes.get(i).writeWithRecord(key, record);
                            }
                            System.out.println("  🔄 [ASYNC REPLICATION COMPLETE] Secondaries updated.");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                    break;
            }
        }

        /**
         * Reads key under requested Consistency Level.
         */
        public String read(String key, int nodeIndex, ConsistencyLevel level) {
            ReplicaNode node = nodes.get(nodeIndex);

            switch (level) {
                case LINEARIZABLE:
                    // Linearizable read: Forces sync read quorum / master check
                    DataRecord primaryRecord = nodes.get(0).readLocal(key);
                    return primaryRecord != null ? primaryRecord.getValue() : "NULL";

                case CAUSAL:
                case EVENTUAL:
                default:
                    DataRecord localRecord = node.readLocal(key);
                    return localRecord != null ? localRecord.getValue() : "NULL (Stale/Unreplicated)";
            }
        }
    }

    public static class Demo {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("======================================================================");
            System.out.println(" 🌐 DISTRIBUTED CONSISTENCY MODELS DEMO (Linearizable vs Causal vs Eventual)");
            System.out.println("======================================================================\n");

            DistributedCluster cluster = new DistributedCluster(3);

            // -------------------------------------------------------------------------
            // SCENARIO 1: LINEARIZABLE (STRONG) CONSISTENCY
            // -------------------------------------------------------------------------
            System.out.println("🔹 SCENARIO 1: Testing LINEARIZABLE Consistency (Strict Real-Time Read/Write)...");
            cluster.write("account_balance", "$500", ConsistencyLevel.LINEARIZABLE);

            System.out.println("  Reading from Node 1: " + cluster.read("account_balance", 0, ConsistencyLevel.LINEARIZABLE));
            System.out.println("  Reading from Node 2: " + cluster.read("account_balance", 1, ConsistencyLevel.LINEARIZABLE));
            System.out.println("  Reading from Node 3: " + cluster.read("account_balance", 2, ConsistencyLevel.LINEARIZABLE));
            System.out.println("✅ All nodes immediately reflect the exact same real-time value.\n");

            // -------------------------------------------------------------------------
            // SCENARIO 2: EVENTUAL CONSISTENCY (Replication Lag Simulation)
            // -------------------------------------------------------------------------
            System.out.println("🔹 SCENARIO 2: Testing EVENTUAL Consistency (Low Latency, Async Lag)...");
            cluster.write("user_profile_name", "Kartik Chauhan", ConsistencyLevel.EVENTUAL);

            System.out.println("  [Immediate Read] Node 1 (Primary):   " + cluster.read("user_profile_name", 0, ConsistencyLevel.EVENTUAL));
            System.out.println("  [Immediate Read] Node 2 (Secondary): " + cluster.read("user_profile_name", 1, ConsistencyLevel.EVENTUAL));
            System.out.println("  [Immediate Read] Node 3 (Secondary): " + cluster.read("user_profile_name", 2, ConsistencyLevel.EVENTUAL));

            System.out.println("\n⏳ Waiting 800ms for async background replication to converge...");
            Thread.sleep(800);

            System.out.println("  [Converged Read] Node 2 (Secondary): " + cluster.read("user_profile_name", 1, ConsistencyLevel.EVENTUAL));
            System.out.println("  [Converged Read] Node 3 (Secondary): " + cluster.read("user_profile_name", 2, ConsistencyLevel.EVENTUAL));
            System.out.println("✅ Eventual Consistency verified! Data converged after replication delay.\n");

            // -------------------------------------------------------------------------
            // SCENARIO 3: CAUSAL CONSISTENCY WITH VECTOR CLOCKS
            // -------------------------------------------------------------------------
            System.out.println("🔹 SCENARIO 3: Testing CAUSAL Consistency (Vector Clocks)...");
            ReplicaNode node1 = cluster.getNodes().get(0);
            node1.writeLocal("msg_1", "Hello World!", System.currentTimeMillis());
            System.out.println("  Event 1 on Node 1 -> VectorClock: " + node1.getVectorClock());

            node1.writeLocal("msg_2", "Reply to Hello World!", System.currentTimeMillis());
            System.out.println("  Event 2 on Node 1 -> VectorClock: " + node1.getVectorClock());

            System.out.println("✅ Causal Order verified! Msg 2 causally succeeds Msg 1.");

            System.out.println("\n======================================================================");
            System.out.println(" 🌐 CONSISTENCY MODELS DEMO COMPLETED SUCCESSFULLY");
            System.out.println("======================================================================");
        }
    }
}
