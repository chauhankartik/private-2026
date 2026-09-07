package collections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Production-Grade Custom Collection Data Structures Masterclass
 *
 * Demonstrates:
 * 1. Custom LRU Cache implemented using HashMap + Doubly-Linked List (O(1) get & put)
 * 2. Custom Bounded Blocking Queue implemented using ReentrantLock and Condition variables
 * 3. Custom Open-Addressing Linear Probing Hash Map implementation
 */
class CustomCollectionsDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. Custom O(1) LRU Cache (HashMap + Doubly-Linked List) ===");
        demonstrateCustomLRUCache();

        System.out.println("\n=== 2. Custom Bounded Blocking Queue (ReentrantLock + Conditions) ===");
        demonstrateCustomBlockingQueue();

        System.out.println("\n=== 3. Custom Open-Addressing Linear Probing Hash Map ===");
        demonstrateCustomLinearProbingMap();

        System.out.println("\n[SUCCESS] Custom Collections demonstration completed cleanly.");
    }

    /**
     * 1. Custom O(1) LRU Cache
     */
    static class CustomLRUCache<K, V> {
        private static class Node<K, V> {
            K key;
            V val;
            Node<K, V> prev;
            Node<K, V> next;

            Node(K key, V val) {
                this.key = key;
                this.val = val;
            }
        }

        private final int capacity;
        private final Map<K, Node<K, V>> map;
        private final Node<K, V> head;
        private final Node<K, V> tail;

        public CustomLRUCache(int capacity) {
            this.capacity = capacity;
            this.map = new HashMap<>();
            this.head = new Node<>(null, null);
            this.tail = new Node<>(null, null);
            head.next = tail;
            tail.prev = head;
        }

        public V get(K key) {
            Node<K, V> node = map.get(key);
            if (node == null) return null;
            moveToHead(node);
            return node.val;
        }

        public void put(K key, V val) {
            Node<K, V> node = map.get(key);
            if (node != null) {
                node.val = val;
                moveToHead(node);
            } else {
                if (map.size() >= capacity) {
                    Node<K, V> evicted = removeTail();
                    map.remove(evicted.key);
                }
                Node<K, V> newNode = new Node<>(key, val);
                map.put(key, newNode);
                addNode(newNode);
            }
        }

        private void addNode(Node<K, V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }

        private void removeNode(Node<K, V> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private void moveToHead(Node<K, V> node) {
            removeNode(node);
            addNode(node);
        }

        private Node<K, V> removeTail() {
            Node<K, V> res = tail.prev;
            removeNode(res);
            return res;
        }
    }

    private static void demonstrateCustomLRUCache() {
        CustomLRUCache<Integer, String> cache = new CustomLRUCache<>(2);
        cache.put(1, "A");
        cache.put(2, "B");
        System.out.println("  get(1): " + cache.get(1) + " -> Moved key 1 to most recently used");

        cache.put(3, "C"); // Evicts key 2 (least recently used)
        System.out.println("  put(3, 'C'): Evicted key 2");
        System.out.println("  get(2): " + cache.get(2) + " (null because evicted)");
        System.out.println("  get(3): " + cache.get(3));
    }

    /**
     * 2. Custom Bounded Blocking Queue
     */
    static class CustomBlockingQueue<E> {
        private final Object[] items;
        private int takeIndex;
        private int putIndex;
        private int count;

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        public CustomBlockingQueue(int capacity) {
            this.items = new Object[capacity];
        }

        public void put(E element) throws InterruptedException {
            lock.lock();
            try {
                while (count == items.length) {
                    notFull.await(); // Wait until space is available
                }
                items[putIndex] = element;
                if (++putIndex == items.length) putIndex = 0;
                count++;
                notEmpty.signal(); // Notify consumer
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        public E take() throws InterruptedException {
            lock.lock();
            try {
                while (count == 0) {
                    notEmpty.await(); // Wait until item is available
                }
                E element = (E) items[takeIndex];
                items[takeIndex] = null;
                if (++takeIndex == items.length) takeIndex = 0;
                count--;
                notFull.signal(); // Notify producer
                return element;
            } finally {
                lock.unlock();
            }
        }
    }

    private static void demonstrateCustomBlockingQueue() throws InterruptedException {
        CustomBlockingQueue<Integer> bq = new CustomBlockingQueue<>(2);

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(100);
                System.out.println("  [Consumer Thread] Taken item: " + bq.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        bq.put(10);
        bq.put(20);
        System.out.println("  [Main Thread] Put items 10, 20 into Bounded Queue (Capacity 2)");

        consumer.start();

        // Put item 30 will block until consumer takes item 10
        System.out.println("  [Main Thread] Attempting put(30) on full queue (blocking)...");
        bq.put(30);
        System.out.println("  [Main Thread] Successfully put item 30 after consumer freed space!");

        consumer.join();
    }

    /**
     * 3. Custom Open-Addressing Linear Probing Map
     */
    static class LinearProbingMap<K, V> {
        private int capacity = 16;
        private int size = 0;
        private K[] keys;
        private V[] values;

        @SuppressWarnings("unchecked")
        public LinearProbingMap() {
            keys = (K[]) new Object[capacity];
            values = (V[]) new Object[capacity];
        }

        private int hash(K key) {
            return (key.hashCode() & 0x7fffffff) % capacity;
        }

        public void put(K key, V val) {
            int i = hash(key);
            while (keys[i] != null) {
                if (keys[i].equals(key)) {
                    values[i] = val;
                    return;
                }
                i = (i + 1) % capacity; // Linear probing
            }
            keys[i] = key;
            values[i] = val;
            size++;
        }

        public V get(K key) {
            int i = hash(key);
            while (keys[i] != null) {
                if (keys[i].equals(key)) {
                    return values[i];
                }
                i = (i + 1) % capacity;
            }
            return null;
        }

        public int size() {
            return size;
        }
    }

    private static void demonstrateCustomLinearProbingMap() {
        LinearProbingMap<String, Integer> map = new LinearProbingMap<>();
        map.put("Key1", 100);
        map.put("Key2", 200);

        System.out.printf("  Linear Probing Map -> Size: %d, Key1: %d, Key2: %d%n",
                map.size(), map.get("Key1"), map.get("Key2"));
    }
}
