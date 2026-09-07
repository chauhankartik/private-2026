package distributed_locks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Executable Java simulation of a Distributed Lock Manager with Fencing Tokens.
 * Demonstrates mutual exclusion, TTL expiration, and Monotonic Fencing Tokens to prevent split-brain writes.
 */
public class DistributedLockManager {

    public static class LockHandle {
        public final String resource;
        public final String clientId;
        public final long fencingToken;
        public final long leaseExpiryTimeMs;

        public LockHandle(String resource, String clientId, long fencingToken, long leaseExpiryTimeMs) {
            this.resource = resource;
            this.clientId = clientId;
            this.fencingToken = fencingToken;
            this.leaseExpiryTimeMs = leaseExpiryTimeMs;
        }

        @Override
        public String toString() {
            return String.format("[Resource:'%s', Client:'%s', FencingToken:%d]", resource, clientId, fencingToken);
        }
    }

    public static class LockService {
        private final Map<String, LockHandle> locks = new ConcurrentHashMap<>();
        private final AtomicLong fencingTokenGenerator = new AtomicLong(100);

        public synchronized LockHandle acquireLock(String resource, String clientId, long ttlMs) {
            long now = System.currentTimeMillis();
            LockHandle existing = locks.get(resource);

            if (existing != null && now < existing.leaseExpiryTimeMs) {
                System.out.printf("Lock Acquisition REJECTED for Client '%s' on '%s' (Held by '%s')\n",
                        clientId, resource, existing.clientId);
                return null;
            }

            long newToken = fencingTokenGenerator.incrementAndGet();
            LockHandle handle = new LockHandle(resource, clientId, newToken, now + ttlMs);
            locks.put(resource, handle);
            System.out.printf("Lock GRANTED: %s (TTL: %d ms)\n", handle, ttlMs);
            return handle;
        }

        public synchronized void releaseLock(LockHandle handle) {
            LockHandle existing = locks.get(handle.resource);
            if (existing != null && existing.clientId.equals(handle.clientId) && existing.fencingToken == handle.fencingToken) {
                locks.remove(handle.resource);
                System.out.printf("Lock RELEASED: %s\n", handle);
            }
        }
    }

    public static class ProtectedStorageServer {
        private long highestSeenFencingToken = 0;
        private String value = "";

        public synchronized boolean writeData(String clientId, long fencingToken, String newValue) {
            if (fencingToken < highestSeenFencingToken) {
                System.out.printf("Storage REJECTED write from Client '%s' (Token %d < HighestSeen %d) -> Split-Brain Blocked!\n",
                        clientId, fencingToken, highestSeenFencingToken);
                return false;
            }
            highestSeenFencingToken = fencingToken;
            this.value = newValue;
            System.out.printf("Storage ACCEPTED write from Client '%s' (Token %d): Value = '%s'\n",
                    clientId, fencingToken, newValue);
            return true;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Distributed Lock Manager with Fencing Tokens Simulation ===");

        LockService lockService = new LockService();
        ProtectedStorageServer storage = new ProtectedStorageServer();

        // 1. Client A acquires lock with 500ms TTL
        LockHandle lockA = lockService.acquireLock("file_db.dat", "Client_A", 500);
        storage.writeData("Client_A", lockA.fencingToken, "Data_v1");

        // 2. Client A suffers GC pause, TTL expires
        System.out.println("\n--- Simulating 600ms GC Pause on Client A ---");
        Thread.sleep(600);

        // 3. Client B acquires lock
        LockHandle lockB = lockService.acquireLock("file_db.dat", "Client_B", 1000);
        storage.writeData("Client_B", lockB.fencingToken, "Data_v2");

        // 4. Client A wakes up from GC pause and attempts to write with stale token
        System.out.println("\n--- Client A wakes up from GC pause and attempts write ---");
        storage.writeData("Client_A", lockA.fencingToken, "Stale_Data_v3");
    }
}
