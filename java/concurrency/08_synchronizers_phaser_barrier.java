package java.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Production Synchronizer Primitives in Java:
 * CountDownLatch, CyclicBarrier, Phaser, Semaphore, and Exchanger.
 */
public class SynchronizerPrimitivesDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. CountDownLatch (One-Shot Multi-Worker Gate) ===");
        demoCountDownLatch();

        System.out.println("\n=== 2. CyclicBarrier (Reusable Iterative Barrier) ===");
        demoCyclicBarrier();

        System.out.println("\n=== 3. Phaser (Dynamic Registration Multi-Phase Barrier) ===");
        demoPhaser();

        System.out.println("\n=== 4. Semaphore (Permit Rate Limiter) ===");
        demoSemaphore();

        System.out.println("\n=== 5. Exchanger (Bi-Directional Thread Data Exchange) ===");
        demoExchanger();
    }

    /**
     * CountDownLatch: One-shot gate blocking a coordinator thread until N worker tasks complete.
     */
    private static void demoCountDownLatch() throws InterruptedException {
        int workerCount = 3;
        CountDownLatch latch = new CountDownLatch(workerCount);

        for (int i = 1; i <= workerCount; i++) {
            int workerId = i;
            new Thread(() -> {
                try {
                    Thread.sleep(workerId * 50);
                    System.out.println("Worker " + workerId + " initialization complete.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown(); // Decrements latch count
                }
            }).start();
        }

        System.out.println("Coordinator waiting for workers to complete...");
        latch.await(); // Blocks until latch count reaches 0!
        System.out.println("All workers ready. System service started!");
    }

    /**
     * CyclicBarrier: Reusable barrier where N threads wait for each other before advancing to next phase.
     */
    private static void demoCyclicBarrier() throws InterruptedException {
        int partyCount = 3;
        // Barrier action runs automatically whenever all 3 threads reach the barrier point
        CyclicBarrier barrier = new CyclicBarrier(partyCount, () -> {
            System.out.println("--- All threads reached barrier! Proceeding to next processing phase ---");
        });

        for (int i = 1; i <= partyCount; i++) {
            int threadId = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + threadId + " working on Phase 1...");
                    Thread.sleep(threadId * 40);
                    barrier.await(); // Waits for all 3 threads to reach barrier

                    System.out.println("Thread " + threadId + " working on Phase 2...");
                    Thread.sleep(threadId * 40);
                    barrier.await(); // Reusable! Waits again for Phase 2 completion
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Thread.sleep(400); // Allow threads to finish
    }

    /**
     * Phaser: Flexible synchronizer supporting dynamic thread registration/deregistration across multi-phase tasks.
     */
    private static void demoPhaser() {
        Phaser phaser = new Phaser(1); // Register main thread as participant (Phase 0)

        for (int i = 1; i <= 3; i++) {
            phaser.register(); // Dynamically register new worker thread
            int workerId = i;
            new Thread(() -> {
                System.out.println("Worker " + workerId + " starting Phase 0 (Data Download)...");
                phaser.arriveAndAwaitAdvance(); // Arrive at Phase 0 barrier and wait

                System.out.println("Worker " + workerId + " starting Phase 1 (Data Transformation)...");
                phaser.arriveAndAwaitAdvance(); // Arrive at Phase 1 barrier and wait

                phaser.arriveAndDeregister(); // Worker finishes and deregisters cleanly
            }).start();
        }

        // Main thread coordinates Phase 0
        System.out.println("Main thread waiting for Phase 0 completion...");
        phaser.arriveAndAwaitAdvance();

        // Main thread coordinates Phase 1
        System.out.println("Main thread waiting for Phase 1 completion...");
        phaser.arriveAndAwaitAdvance();

        phaser.arriveAndDeregister(); // Deregister main thread
        System.out.println("Phaser execution complete. Final Phase: " + phaser.getPhase());
    }

    /**
     * Semaphore: Controls concurrent access to a resource via N permits.
     */
    private static void demoSemaphore() throws InterruptedException {
        int maxPermits = 2; // Only 2 concurrent connections allowed
        Semaphore semaphore = new Semaphore(maxPermits, true); // Fair semaphore

        for (int i = 1; i <= 4; i++) {
            int clientId = i;
            new Thread(() -> {
                try {
                    if (semaphore.tryAcquire(200, TimeUnit.MILLISECONDS)) {
                        try {
                            System.out.println("Client " + clientId + " acquired connection permit.");
                            Thread.sleep(100);
                        } finally {
                            System.out.println("Client " + clientId + " releasing connection permit.");
                            semaphore.release();
                        }
                    } else {
                        System.out.println("Client " + clientId + " timed out waiting for connection permit.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Thread.sleep(500);
    }

    /**
     * Exchanger: Synchronous rendezvous point where two threads swap data objects.
     */
    private static void demoExchanger() throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        Thread producer = new Thread(() -> {
            try {
                String data = "BufferPayload_A";
                System.out.println("Producer offering: " + data);
                String received = exchanger.exchange(data);
                System.out.println("Producer received in exchange: " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                String data = "EmptyBuffer_B";
                System.out.println("Consumer offering: " + data);
                String received = exchanger.exchange(data);
                System.out.println("Consumer received in exchange: " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
