package java.concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * Java 21+ Virtual Threads (Project Loom JEP 444) & Structured Concurrency (JEP 453)
 * Demonstrates Virtual Thread creation, high-concurrency throughput, pinning hazards,
 * and StructuredTaskScope for short-circuit task management.
 */
public class ModernVirtualThreadsDemo {

    private static final ReentrantLock safeLock = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. Starting Virtual Threads (Thread.ofVirtual()) ===");
        demoVirtualThreadCreation();

        System.out.println("\n=== 2. High-Concurrency Virtual Thread Throughput (10,000 Tasks) ===");
        demoHighThroughputVirtualThreads();

        System.out.println("\n=== 3. Pinning Hazard vs Safe ReentrantLock ===");
        demoPinningSolution();

        System.out.println("\n=== 4. Structured Concurrency (StructuredTaskScope) ===");
        demoStructuredConcurrency();
    }

    /**
     * Creation syntax for Java 21 Virtual Threads
     */
    private static void demoVirtualThreadCreation() throws InterruptedException {
        // Method A: Thread.ofVirtual() builder
        Thread vThread = Thread.ofVirtual()
                .name("custom-virtual-worker")
                .start(() -> {
                    System.out.println("Running inside: " + Thread.currentThread());
                });

        vThread.join();

        // Method B: Virtual Thread Per Task Executor
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                System.out.println("Executor Virtual Thread: " + Thread.currentThread());
            });
        }
    }

    /**
     * Executes 10,000 concurrent Virtual Threads without exhausting OS thread handles
     */
    private static void demoHighThroughputVirtualThreads() {
        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> {
                executor.submit(() -> {
                    try {
                        // Simulate I/O Blocking (Virtual Thread un-mounts cleanly!)
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return i;
                });
            });
        } // Auto-closes and waits for all 10,000 tasks to finish!

        long endTime = System.currentTimeMillis();
        System.out.println("Completed 10,000 Virtual Threads in: " + (endTime - startTime) + " ms");
    }

    /**
     * Demonstrates using ReentrantLock to avoid pinning Virtual Threads to OS Carrier Threads
     */
    private static void demoPinningSolution() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                safeLock.lock();
                try {
                    // Safe I/O blocking inside ReentrantLock (Does NOT pin carrier thread!)
                    Thread.sleep(50);
                    System.out.println("Executed safely inside ReentrantLock: " + Thread.currentThread());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    safeLock.unlock();
                }
            });
        }
    }

    /**
     * Structured Concurrency (StructuredTaskScope.ShutdownOnFailure)
     */
    private static void demoStructuredConcurrency() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork parallel sub-tasks
            StructuredTaskScope.Subtask<String> userTask = scope.fork(() -> fetchUserData());
            StructuredTaskScope.Subtask<String> orderTask = scope.fork(() -> fetchOrderData());

            scope.join();           // Join both concurrent sub-tasks
            scope.throwIfFailed();  // Propagate exception if ANY sub-task failed

            System.out.println("Combined Result: " + userTask.get() + " | " + orderTask.get());
        }
    }

    private static String fetchUserData() throws InterruptedException {
        Thread.sleep(50);
        return "UserPayload[id=101, name=Alice]";
    }

    private static String fetchOrderData() throws InterruptedException {
        Thread.sleep(80);
        return "OrderPayload[id=9901, status=PAID]";
    }
}
