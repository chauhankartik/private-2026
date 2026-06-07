import java.util.LinkedList;
import java.util.Queue;

public class BoundedBufferWithMonitor<T> {
    private final Queue<T> buffer = new LinkedList<>();
    private final int capacity;

    public BoundedBufferWithMonitor(int capacity) {
        this.capacity = capacity;
    }

    // Producer method
    public synchronized void put(T item) throws InterruptedException {
        // MUST use a while-loop, not an if-statement!
        while (buffer.size() == capacity) {
            wait(); // Releases the monitor lock and sleeps
        }

        buffer.add(item);
        System.out.println("Produced: " + item);

        // Wake up all threads (producers and consumers) waiting on this object monitor
        notifyAll();
    }

    // Consumer method
    public synchronized T take() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait(); // Releases the monitor lock and sleeps
        }

        T item = buffer.poll();
        System.out.println("Consumed: " + item);

        notifyAll(); // Wake up waiting producers
        return item;
    }

    public static void main(String[] args) {
        BoundedBufferWithMonitor<Integer> buffer = new BoundedBufferWithMonitor<>(5);

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