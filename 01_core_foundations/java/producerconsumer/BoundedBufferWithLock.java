import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBufferWithLock<T> {
    private final Queue<T> buffer = new LinkedList<>();
    private final int capacity;

    // Explicit Lock
    private final Lock lock = new ReentrantLock();

    // Separate Wait Queues (Conditions) linked to the lock
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BoundedBufferWithLock(int capacity) {
        this.capacity = capacity;
    }

    public void put(T item) throws InterruptedException {
        lock.lockInterruptibly(); // Acquire lock safely
        try {
            while (buffer.size() == capacity) {
                notFull.await(); // Producer sleeps here until space opens
            }

            buffer.add(item);
            System.out.println("Produced: " + item);

            // Signal ONLY waiting consumers
            notEmpty.signal();
        } finally {
            lock.unlock(); // Always release lock in finally block
        }
    }

    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (buffer.isEmpty()) {
                notEmpty.await(); // Consumer sleeps here until data arrives
            }

            T item = buffer.poll();
            System.out.println("Consumed: " + item);

            // Signal ONLY waiting producers
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        BoundedBufferWithLock<Integer> buffer = new BoundedBufferWithLock<>(5);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.put(i);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.take();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
    }
}