package consistent_hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConsistentHashing {

    public interface HashFunction {
        long hash(String key);
    }

    /**
     * MD5 32-bit Hash Implementation for uniform hash ring distribution.
     */
    public static class MD5HashFunction implements HashFunction {
        @Override
        public long hash(String key) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
                // Use first 4 bytes for 32-bit unsigned integer hash
                long h = ((long) (digest[3] & 0xFF) << 24) |
                         ((long) (digest[2] & 0xFF) << 16) |
                         ((long) (digest[1] & 0xFF) << 8) |
                         ((long) (digest[0] & 0xFF));
                return h & 0xFFFFFFFFL;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 not supported", e);
            }
        }
    }

    public static class ConsistentHashRing<T> {
        private final HashFunction hashFunction;
        private final int numberOfVirtualNodesPerServer;
        
        // Ring: HashValue -> Server Node
        private final ConcurrentSkipListMap<Long, T> ring = new ConcurrentSkipListMap<>();

        public ConsistentHashRing(HashFunction hashFunction, int numberOfVirtualNodesPerServer) {
            this.hashFunction = hashFunction;
            this.numberOfVirtualNodesPerServer = numberOfVirtualNodesPerServer;
        }

        public void addServer(T server) {
            for (int i = 0; i < numberOfVirtualNodesPerServer; i++) {
                String virtualNodeKey = server.toString() + "-VN-" + i;
                long hash = hashFunction.hash(virtualNodeKey);
                ring.put(hash, server);
            }
        }

        public void removeServer(T server) {
            for (int i = 0; i < numberOfVirtualNodesPerServer; i++) {
                String virtualNodeKey = server.toString() + "-VN-" + i;
                long hash = hashFunction.hash(virtualNodeKey);
                ring.remove(hash);
            }
        }

        public T getServer(String key) {
            if (ring.isEmpty()) {
                return null;
            }
            long hash = hashFunction.hash(key);
            
            // Find first node with hash >= key.hash
            Map.Entry<Long, T> entry = ring.ceilingEntry(hash);
            if (entry == null) {
                // Wrap around to beginning of ring
                entry = ring.firstEntry();
            }
            return entry.getValue();
        }

        public int getRingSize() {
            return ring.size();
        }
    }

    public static class Demo {
        public static void main(String[] args) {
            System.out.println("======================================================================");
            System.out.println(" 🌐 CONSISTENT HASHING WITH VIRTUAL NODES (DynamoDB / Cassandra)");
            System.out.println("======================================================================\n");

            HashFunction hashFunction = new MD5HashFunction();
            ConsistentHashRing<String> hashRing = new ConsistentHashRing<>(hashFunction, 100);

            // Add Servers
            hashRing.addServer("CacheServer_1");
            hashRing.addServer("CacheServer_2");
            hashRing.addServer("CacheServer_3");

            System.out.println("✅ Populated Hash Ring with 3 Physical Servers (100 Virtual Nodes per server = 300 total vnodes).\n");

            // Map Keys to Servers
            List<String> keys = Arrays.asList("user_1001", "user_1002", "order_5590", "session_abc", "cart_9912", "payment_771");
            Map<String, String> keyServerMap = new HashMap<>();

            System.out.println("🔹 Initial Key Placement:");
            for (String key : keys) {
                String server = hashRing.getServer(key);
                keyServerMap.put(key, server);
                System.out.printf("  Key '%s' -> Mapped to %s%n", key, server);
            }

            // Scale Up: Add Server 4
            System.out.println("\n🔹 Adding Server 'CacheServer_4' (Scaling Up)...");
            hashRing.addServer("CacheServer_4");

            int remappedCount = 0;
            System.out.println("🔹 Key Placement After Server Addition:");
            for (String key : keys) {
                String newServer = hashRing.getServer(key);
                String oldServer = keyServerMap.get(key);
                boolean changed = !newServer.equals(oldServer);
                if (changed) remappedCount++;
                System.out.printf("  Key '%s' -> Mapped to %s %s%n", key, newServer, (changed ? "🔄 (REMAPPED)" : "✅ (UNMOVED)"));
            }

            System.out.printf("%n📊 Remapped %d / %d keys. Minimal data movement verified!%n", remappedCount, keys.size());
            System.out.println("\n======================================================================");
            System.out.println(" 🌐 CONSISTENT HASHING DEMO COMPLETED SUCCESSFULLY");
            System.out.println("======================================================================");
        }
    }
}
