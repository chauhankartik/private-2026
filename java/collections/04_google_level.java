import java.util.*;
import java.util.concurrent.*;

/**
 * ============================================================
 *  JAVA COLLECTIONS — GOOGLE LEVEL
 *  Highly optimized structures, concurrency, and niche APIs.
 * ============================================================
 *
 *  Problems:
 *   G1. Time-Based Key-Value Store (TreeMap with floorKey)
 *   G2. Snapshot Array (List of TreeMaps)
 *   G3. Max Stack (TreeMap / PriorityQueue + DoubleLinkedList)
 *   G4. Concurrent Collections Deep Dive
 *
 * ============================================================
 */
public class GoogleLevel {

    // =========================================================
    // G1. TIME-BASED KEY-VALUE STORE
    // Pattern: HashMap -> TreeMap (floorKey)
    // =========================================================
    /**
     * Problem: Design a time-based key-value data structure that can store 
     * multiple values for the same key at different time stamps and retrieve 
     * the key's value at a certain timestamp. (LeetCode 981)
     *
     * Concept: 
     * For each key, we store a TreeMap mapping timestamp -> value.
     * floorEntry(timestamp) gives the greatest key less than or equal to timestamp.
     */
    class TimeMap {
        private Map<String, TreeMap<Integer, String>> map;

        public TimeMap() {
            map = new HashMap<>();
        }

        public void set(String key, String value, int timestamp) {
            map.computeIfAbsent(key, k -> new TreeMap<>()).put(timestamp, value);
        }

        public String get(String key, int timestamp) {
            if (!map.containsKey(key)) return "";
            
            Map.Entry<Integer, String> entry = map.get(key).floorEntry(timestamp);
            return entry == null ? "" : entry.getValue();
        }
    }

    // =========================================================
    // G2. SNAPSHOT ARRAY
    // Pattern: Array of TreeMaps
    // =========================================================
    /**
     * Problem: Implement a SnapshotArray that supports set(index, val), 
     * snap() which takes a snapshot and returns snap_id, and 
     * get(index, snap_id) which returns the value at the given snap_id.
     * (LeetCode 1146)
     *
     * Concept: 
     * Instead of copying the whole array on every snap (Memory Limit Exceeded),
     * we keep track of history for EACH index using a TreeMap<SnapId, Value>.
     */
    class SnapshotArray {
        private List<TreeMap<Integer, Integer>> arr;
        private int snapId;

        public SnapshotArray(int length) {
            arr = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                map.put(0, 0); // initial value is 0 at snap 0
                arr.add(map);
            }
            snapId = 0;
        }

        public void set(int index, int val) {
            arr.get(index).put(snapId, val);
        }

        public int snap() {
            return snapId++;
        }

        public int get(int index, int snap_id) {
            // floorEntry because the value at snap_id is the last set value 
            // before or at snap_id.
            return arr.get(index).floorEntry(snap_id).getValue();
        }
    }

    // =========================================================
    // G3. MAX STACK
    // Pattern: DoublyLinkedList + TreeMap
    // =========================================================
    /**
     * Problem: Design a max stack that supports push, pop, top, peekMax, and popMax.
     * (LeetCode 716)
     *
     * Concept: 
     * To support O(1) push, pop, top, we need a Stack or LinkedList.
     * To support O(log N) peekMax, popMax, we need a TreeMap mapping Value -> List of Nodes.
     * 
     * When popMax is called, we find the max value in TreeMap, take the last inserted Node 
     * from the List, remove it from the DoublyLinkedList, and remove it from TreeMap.
     */
    class MaxStack {
        class Node {
            int val;
            Node prev, next;
            public Node(int v) { val = v; }
        }

        class DoubleLinkedList {
            Node head, tail;
            public DoubleLinkedList() {
                head = new Node(0);
                tail = new Node(0);
                head.next = tail;
                tail.prev = head;
            }

            public Node append(int val) {
                Node x = new Node(val);
                x.next = tail;
                x.prev = tail.prev;
                tail.prev.next = x;
                tail.prev = x;
                return x;
            }

            public Node pop() {
                return unlink(tail.prev);
            }

            public Node peek() {
                return tail.prev;
            }

            public Node unlink(Node node) {
                node.prev.next = node.next;
                node.next.prev = node.prev;
                return node;
            }
        }

        private TreeMap<Integer, List<Node>> map;
        private DoubleLinkedList dll;

        public MaxStack() {
            map = new TreeMap<>();
            dll = new DoubleLinkedList();
        }

        public void push(int x) {
            Node node = dll.append(x);
            map.computeIfAbsent(x, k -> new ArrayList<>()).add(node);
        }

        public int pop() {
            int val = dll.pop().val;
            List<Node> list = map.get(val);
            list.remove(list.size() - 1);
            if (list.isEmpty()) map.remove(val);
            return val;
        }

        public int top() {
            return dll.peek().val;
        }

        public int peekMax() {
            return map.lastKey();
        }

        public int popMax() {
            int max = peekMax();
            List<Node> list = map.get(max);
            Node node = list.remove(list.size() - 1);
            dll.unlink(node);
            if (list.isEmpty()) map.remove(max);
            return max;
        }
    }

    // =========================================================
    // G4. CONCURRENT COLLECTIONS (Interview Talking Points)
    // =========================================================
    /**
     * Google interviewers might probe your understanding of thread safety in Java collections.
     * 
     * Q: "Why not just use Collections.synchronizedMap(new HashMap<>())?"
     * A: It locks the ENTIRE map on every operation. Very poor throughput under high concurrency.
     * 
     * Q: "How does ConcurrentHashMap improve this?"
     * A: 
     *   - Java 7 used "Segmented Locking" (lock striping over 16 segments).
     *   - Java 8+ uses fine-grained locking. It synchronizes on the FIRST NODE of the bucket 
     *     (using synchronized blocks) and uses CAS (Compare-And-Swap) operations to add nodes
     *     to empty buckets.
     *   - Reads (get) are generally lock-free and never block!
     * 
     * Q: "When would you use CopyOnWriteArrayList?"
     * A: When reads vastly outnumber writes (e.g., event listeners). Every mutative operation 
     *    (add, set, remove) makes a fresh copy of the underlying array. Iteration happens on 
     *    a snapshot, preventing ConcurrentModificationException without locks.
     */
}
