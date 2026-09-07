package range_vs_hash_sharding;

import java.util.*;

/**
 * Executable Java simulation comparing Range-Based Sharding vs Hash-Based Sharding routers.
 * Demonstrates routing decisions, range scans, and scatter-gather operations.
 */
public class ShardingManager {

    public static class ShardNode {
        public final int shardId;
        public final String name;
        public final Map<String, String> dataStore = new HashMap<>();

        public ShardNode(int shardId, String name) {
            this.shardId = shardId;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("Shard-%d[%s] (Count: %d)", shardId, name, dataStore.size());
        }
    }

    // --- Range-Based Sharding Router ---
    public static class RangeShardingRouter {
        private final List<ShardNode> shards;

        public RangeShardingRouter(List<ShardNode> shards) {
            this.shards = shards;
        }

        public ShardNode routeKey(String key) {
            char firstChar = Character.toUpperCase(key.charAt(0));
            if (firstChar >= 'A' && firstChar <= 'H') return shards.get(0);
            if (firstChar >= 'I' && firstChar <= 'P') return shards.get(1);
            return shards.get(2); // Q-Z
        }

        public void put(String key, String value) {
            ShardNode node = routeKey(key);
            node.dataStore.put(key, value);
            System.out.printf("[Range Router] Put '%s' -> %s\n", key, node.name);
        }
    }

    // --- Hash-Based Sharding Router ---
    public static class HashShardingRouter {
        private final List<ShardNode> shards;

        public HashShardingRouter(List<ShardNode> shards) {
            this.shards = shards;
        }

        public ShardNode routeKey(String key) {
            int hash = Math.abs(key.hashCode());
            int shardIndex = hash % shards.size();
            return shards.get(shardIndex);
        }

        public void put(String key, String value) {
            ShardNode node = routeKey(key);
            node.dataStore.put(key, value);
            System.out.printf("[Hash Router] Put '%s' (hash:%d) -> %s\n", key, key.hashCode(), node.name);
        }

        // Scatter-Gather Range Query Simulation
        public List<String> scatterGatherSearch(String valueSubstring) {
            System.out.printf("\n[Hash Router] Executing Scatter-Gather Scan for substring: '%s'\n", valueSubstring);
            List<String> results = new ArrayList<>();
            for (ShardNode shard : shards) {
                for (Map.Entry<String, String> entry : shard.dataStore.entrySet()) {
                    if (entry.getValue().contains(valueSubstring)) {
                        results.add(shard.name + ":" + entry.getKey());
                    }
                }
            }
            return results;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Range-Based vs Hash-Based Sharding Simulation ===");

        List<ShardNode> rangeShards = Arrays.asList(
                new ShardNode(1, "Node_A-H"),
                new ShardNode(2, "Node_I-P"),
                new ShardNode(3, "Node_Q-Z")
        );

        List<ShardNode> hashShards = Arrays.asList(
                new ShardNode(1, "Node_0"),
                new ShardNode(2, "Node_1"),
                new ShardNode(3, "Node_2")
        );

        RangeShardingRouter rangeRouter = new RangeShardingRouter(rangeShards);
        HashShardingRouter hashRouter = new HashShardingRouter(hashShards);

        // Populate Keys
        String[] keys = {"Alice", "Bob", "Charlie", "David", "Edward", "Frank", "Zelda"};
        for (String k : keys) {
            rangeRouter.put(k, "User_" + k);
            hashRouter.put(k, "User_" + k);
        }

        // Output Load Distribution
        System.out.println("\n--- Range Router Load Distribution ---");
        for (ShardNode s : rangeShards) System.out.println(s);

        System.out.println("\n--- Hash Router Load Distribution ---");
        for (ShardNode s : hashShards) System.out.println(s);

        // Scatter Gather Query
        List<String> matchResults = hashRouter.scatterGatherSearch("User_");
        System.out.println("Scatter-Gather Matches: " + matchResults);
    }
}
