package leader_follower;

import java.util.*;

/**
 * Executable Java simulation of Dynamo-style Leaderless Quorum Replication (R + W > N).
 * Demonstrates Write Quorums, Read Quorums, Versioning, and Read Repair inline.
 */
public class ReplicationManager {

    public static class RecordVersion {
        public final String value;
        public final long timestamp;

        public RecordVersion(String value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("'%s' (ts:%d)", value, timestamp);
        }
    }

    public static class ReplicaNode {
        public final int nodeId;
        private final Map<String, RecordVersion> storage = new HashMap<>();

        public ReplicaNode(int nodeId) {
            this.nodeId = nodeId;
        }

        public synchronized void write(String key, String value, long timestamp) {
            RecordVersion existing = storage.get(key);
            if (existing == null || timestamp > existing.timestamp) {
                storage.put(key, new RecordVersion(value, timestamp));
                System.out.printf("Replica %d: Stored %s = '%s' (ts:%d)\n", nodeId, key, value, timestamp);
            }
        }

        public synchronized RecordVersion read(String key) {
            return storage.get(key);
        }
    }

    public static class LeaderlessCluster {
        private final List<ReplicaNode> nodes;
        private final int N; // Total replicas
        private final int W; // Write Quorum
        private final int R; // Read Quorum

        public LeaderlessCluster(List<ReplicaNode> nodes, int W, int R) {
            this.nodes = nodes;
            this.N = nodes.size();
            this.W = W;
            this.R = R;
        }

        // Quorum Write
        public boolean put(String key, String value) {
            long timestamp = System.currentTimeMillis();
            System.out.printf("\n--- Client PUT Key:'%s' Value:'%s' (Quorum W=%d, N=%d) ---\n", key, value, W, N);

            int ackCount = 0;
            for (ReplicaNode node : nodes) {
                try {
                    node.write(key, value, timestamp);
                    ackCount++;
                } catch (Exception ignored) {}
            }

            boolean success = ackCount >= W;
            System.out.printf("Write Result: %s (ACKs: %d/%d required: %d)\n",
                    success ? "SUCCESS" : "FAILED", ackCount, N, W);
            return success;
        }

        // Quorum Read with Read Repair
        public String get(String key) {
            System.out.printf("\n--- Client GET Key:'%s' (Quorum R=%d, N=%d) ---\n", key, R, N);
            List<ReplicaNode> readRespondents = new ArrayList<>();
            RecordVersion latestVersion = null;

            for (ReplicaNode node : nodes) {
                if (readRespondents.size() < R) {
                    RecordVersion ver = node.read(key);
                    readRespondents.add(node);
                    if (ver != null) {
                        if (latestVersion == null || ver.timestamp > latestVersion.timestamp) {
                            latestVersion = ver;
                        }
                    }
                }
            }

            if (latestVersion == null) {
                System.out.println("Read Result: Key Not Found");
                return null;
            }

            System.out.printf("Read Quorum Picked Latest Version: %s\n", latestVersion);

            // Read Repair: Trigger inline background repair on stale nodes
            for (ReplicaNode node : readRespondents) {
                RecordVersion ver = node.read(key);
                if (ver == null || ver.timestamp < latestVersion.timestamp) {
                    System.out.printf(">>> [Read Repair] Fixing Stale Node %d with Latest Version %s <<<\n",
                            node.nodeId, latestVersion);
                    node.write(key, latestVersion.value, latestVersion.timestamp);
                }
            }

            return latestVersion.value;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Leaderless Dynamo Quorum Simulation ===");

        List<ReplicaNode> nodes = Arrays.asList(
                new ReplicaNode(1),
                new ReplicaNode(2),
                new ReplicaNode(3),
                new ReplicaNode(4),
                new ReplicaNode(5)
        );

        // N = 5, W = 3, R = 3 (Quorum condition R + W > N is satisfied: 3 + 3 = 6 > 5)
        LeaderlessCluster cluster = new LeaderlessCluster(nodes, 3, 3);

        // Client writes key 'user:101'
        cluster.put("user:101", "Bob");

        // Simulate Node 3 falling behind / stale state
        nodes.get(2).write("user:101", "StaleBob", 1000L);

        // Client reads key 'user:101' -> Triggers Read Repair
        cluster.get("user:101");
    }
}
