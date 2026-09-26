package java.concurrency;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lock-Free Atomics, Cell-Striping (LongAdder), The ABA Problem, and
 * the Michael-Scott Lock-Free Queue algorithm.
 */
public class LockFreeAtomicsDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. High-Contention Cell Striping (LongAdder vs AtomicLong) ===");
        demoLongAdder();

        System.out.println("\n=== 2. Demonstrating the ABA Problem ===");
        demoABAProblem();

        System.out.println("\n=== 3. Resolving ABA with AtomicStampedReference (Version Stamp) ===");
        demoABASolution();

        System.out.println("\n=== 4. Michael-Scott Lock-Free Concurrent Queue ===");
        demoLockFreeQueue();
    }

    /**
     * LongAdder vs AtomicLong performance under high thread contention.
     * LongAdder maintains an array of internal cell counters to eliminate CAS contention loops.
     */
    private static void demoLongAdder() throws InterruptedException {
        LongAdder adder = new LongAdder();
        AtomicInteger atomicInt = new AtomicInteger(0);

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100_000; j++) {
                    adder.increment();
                    atomicInt.incrementAndGet();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("LongAdder Final Sum: " + adder.sum());
        System.out.println("AtomicInteger Final Sum: " + atomicInt.get());
    }

    /**
     * Demonstrates the ABA Problem using raw AtomicReference.
     * Thread 1 reads value 'A', gets preempted. Thread 2 mutates 'A' -> 'B' -> 'A'.
     * Thread 1 resumes and CAS succeeds even though the state changed in between!
     */
    private static void demoABAProblem() throws InterruptedException {
        AtomicReference<String> ref = new AtomicReference<>("StateA");

        Thread t1 = new Thread(() -> {
            String initial = ref.get();
            System.out.println("Thread 1 read initial value: " + initial);

            try {
                // Sleep to allow Thread 2 to perform A -> B -> A sequence
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // CAS succeeds because value is 'StateA' again, unaware of intermediate 'StateB'!
            boolean success = ref.compareAndSet(initial, "StateC");
            System.out.println("Thread 1 CAS (StateA -> StateC) succeeded? " + success + " | Current Value: " + ref.get());
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ref.compareAndSet("StateA", "StateB");
            System.out.println("Thread 2 mutated: StateA -> StateB");
            ref.compareAndSet("StateB", "StateA");
            System.out.println("Thread 2 mutated back: StateB -> StateA");
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    /**
     * Resolves the ABA Problem by pairing object reference with an integer Stamp Version Counter
     */
    private static void demoABASolution() throws InterruptedException {
        // Initial value "StateA", initial stamp version 1
        AtomicStampedReference<String> stampedRef = new AtomicStampedReference<>("StateA", 1);

        Thread t1 = new Thread(() -> {
            int initialStamp = stampedRef.getStamp();
            String initialVal = stampedRef.getReference();
            System.out.println("Thread 1 read: " + initialVal + " | Stamp: " + initialStamp);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // CAS checks BOTH reference AND stamp version!
            boolean success = stampedRef.compareAndSet(initialVal, "StateC", initialStamp, initialStamp + 1);
            System.out.println("Thread 1 CAS with Stamp succeeded? " + success + " | Current Value: " + stampedRef.getReference() + " | Current Stamp: " + stampedRef.getStamp());
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            int stamp1 = stampedRef.getStamp();
            stampedRef.compareAndSet("StateA", "StateB", stamp1, stamp1 + 1);
            System.out.println("Thread 2 mutated StateA -> StateB (Stamp: " + stampedRef.getStamp() + ")");

            int stamp2 = stampedRef.getStamp();
            stampedRef.compareAndSet("StateB", "StateA", stamp2, stamp2 + 1);
            System.out.println("Thread 2 mutated StateB -> StateA (Stamp: " + stampedRef.getStamp() + ")");
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    /**
     * Michael-Scott Lock-Free Queue implementation
     */
    private static void demoLockFreeQueue() {
        MichaelScottLockFreeQueue<Integer> queue = new MichaelScottLockFreeQueue<>();
        queue.enqueue(101);
        queue.enqueue(102);
        queue.enqueue(103);

        System.out.println("Dequeued from Lock-Free Queue: " + queue.dequeue());
        System.out.println("Dequeued from Lock-Free Queue: " + queue.dequeue());
        System.out.println("Dequeued from Lock-Free Queue: " + queue.dequeue());
    }

    /**
     * Michael-Scott Lock-Free Queue Data Structure Implementation
     */
    private static class MichaelScottLockFreeQueue<E> {
        private static class Node<E> {
            final E item;
            final AtomicReference<Node<E>> next;

            Node(E item, Node<E>> next) {
                this.item = item;
                this.next = new AtomicReference<>(next);
            }
        }

        private final Node<E> dummy = new Node<>(null, null);
        private final AtomicReference<Node<E>> head = new AtomicReference<>(dummy);
        private final AtomicReference<Node<E>> tail = new AtomicReference<>(dummy);

        public void enqueue(E item) {
            Node<E> newNode = new Node<>(item, null);
            while (true) {
                Node<E> curTail = tail.get();
                Node<E> tailNext = curTail.next.get();
                if (curTail == tail.get()) {
                    if (tailNext != null) {
                        // Queue in intermediate state; advance tail
                        tail.compareAndSet(curTail, tailNext);
                    } else {
                        // Try to link new node to tail
                        if (curTail.next.compareAndSet(null, newNode)) {
                            // Enqueue succeeded; advance tail pointer
                            tail.compareAndSet(curTail, newNode);
                            return;
                        }
                    }
                }
            }
        }

        public E dequeue() {
            while (true) {
                Node<E> curHead = head.get();
                Node<E> curTail = tail.get();
                Node<E> headNext = curHead.next.get();

                if (curHead == head.get()) {
                    if (curHead == curTail) {
                        if (headNext == null) {
                            return null; // Queue is empty
                        }
                        // Tail is lagging behind; advance tail pointer
                        tail.compareAndSet(curTail, headNext);
                    } else {
                        E item = headNext.item;
                        // Advance head pointer
                        if (head.compareAndSet(curHead, headNext)) {
                            return item;
                        }
                    }
                }
            }
        }
    }
}
