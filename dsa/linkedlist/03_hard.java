/**
 * ============================================================
 *  LINKED LIST — HARD PROBLEMS
 * ============================================================
 *
 *  H1.  Merge K Sorted Lists                    LC 23
 *  H2.  Reverse Nodes in k-Group               LC 25
 *  H3.  Sort List (Merge Sort)                  LC 148
 *  H4.  Flatten a Multilevel Doubly LL          LC 430
 *  H5.  LFU Cache                               LC 460
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class ListNode {
    int val;
    ListNode next;
    ListNode(int v) { val = v; }
}

class Hard {

    static ListNode build(int... vals) {
        ListNode dummy = new ListNode(0), cur = dummy;
        for (int v : vals) { cur.next = new ListNode(v); cur = cur.next; }
        return dummy.next;
    }

    static String str(ListNode h) {
        StringBuilder sb = new StringBuilder("[");
        while (h != null) { sb.append(h.val); if (h.next != null) sb.append("→"); h = h.next; }
        return sb.append("]").toString();
    }

    // =========================================================
    // H1. Merge K Sorted Lists  LC 23
    // Pattern: MIN-HEAP (Priority Queue)
    // =========================================================
    /**
     * Brute 1: merge one by one — O(kN) where N = total nodes
     * Brute 2: collect all to array, sort, rebuild — O(N log N) O(N)
     * Optimal: min-heap of size k — O(N log k) O(k)
     *
     * Algorithm:
     *   1. Push all k heads into a min-heap (ordered by node.val).
     *   2. Pop minimum, append to result, push popped.next back.
     *   3. Repeat until heap is empty.
     *
     * Why O(N log k)?
     *   N total nodes, each inserted/extracted once from a heap of max size k.
     *   Heap insert/extract = O(log k).
     *
     * Alternative: Divide & Conquer (also O(N log k), same complexity)
     *   Merge pairs of lists → merge pairs of merged lists → repeat.
     *   log k levels, each level O(N) work.
     *
     * Time:  O(N log k)  — N=total nodes, k=number of lists
     * Space: O(k)        — heap size
     */
    public ListNode mergeKLists(ListNode[] lists) {
        // Min-heap ordered by node value
        PriorityQueue<ListNode> heap = new PriorityQueue<>(
            Comparator.comparingInt(n -> n.val)
        );

        // Seed heap with all heads
        for (ListNode head : lists) {
            if (head != null) heap.offer(head);
        }

        ListNode dummy = new ListNode(0), cur = dummy;

        while (!heap.isEmpty()) {
            ListNode node = heap.poll();   // extract minimum
            cur.next = node;
            cur = cur.next;
            if (node.next != null) heap.offer(node.next); // push next if exists
        }

        return dummy.next;
    }

    // Divide & Conquer alternative — same complexity, no heap needed
    public ListNode mergeKListsDivideConquer(ListNode[] lists) {
        if (lists.length == 0) return null;
        int n = lists.length;
        while (n > 1) {
            for (int i = 0; i < n / 2; i++) {
                lists[i] = mergeTwoLists(lists[i], lists[n - 1 - i]);
            }
            n = (n + 1) / 2; // round up for odd count
        }
        return lists[0];
    }

    private ListNode mergeTwoLists(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), cur = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { cur.next = a; a = a.next; }
            else                { cur.next = b; b = b.next; }
            cur = cur.next;
        }
        cur.next = (a != null) ? a : b;
        return dummy.next;
    }

    // =========================================================
    // H2. Reverse Nodes in k-Group  LC 25
    // Pattern: GROUP REVERSE + RE-ATTACH
    // =========================================================
    /**
     * Reverse every k consecutive nodes. If remaining nodes < k, leave them as-is.
     *
     * 1 → 2 → 3 → 4 → 5  k=2  →  2 → 1 → 4 → 3 → 5
     * 1 → 2 → 3 → 4 → 5  k=3  →  3 → 2 → 1 → 4 → 5
     *
     * Algorithm (iterative):
     *   1. Count if k nodes remain. If not, stop.
     *   2. Reverse k nodes, keeping track of groupTail (= groupHead after reverse).
     *   3. Re-attach: before.next → reversed_head, groupTail.next → next group start.
     *   4. Advance before = groupTail, continue.
     *
     * Time:  O(n)
     * Space: O(1) iterative  |  O(n/k) recursive stack
     */
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode before = dummy;

        while (true) {
            // Check if k nodes are available
            ListNode check = before;
            for (int i = 0; i < k; i++) {
                check = check.next;
                if (check == null) return dummy.next; // fewer than k remaining
            }

            // Reverse k nodes starting at before.next
            ListNode groupHead = before.next;
            ListNode prev = null, curr = groupHead;
            for (int i = 0; i < k; i++) {
                ListNode next = curr.next;
                curr.next = prev;
                prev = curr;
                curr = next;
            }
            // prev = new head of reversed group
            // curr = start of next group
            // groupHead = tail of reversed group

            // Re-attach
            before.next = prev;           // connect to new head
            groupHead.next = curr;        // connect tail to next group
            before = groupHead;           // advance before to tail of this group
        }
    }

    // Recursive version (cleaner, but O(n/k) stack)
    public ListNode reverseKGroupRecursive(ListNode head, int k) {
        // Check if k nodes exist
        ListNode check = head;
        for (int i = 0; i < k; i++) {
            if (check == null) return head; // fewer than k — return unchanged
            check = check.next;
        }

        // Reverse k nodes
        ListNode prev = null, curr = head;
        for (int i = 0; i < k; i++) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        // head is now the tail of the reversed group
        // curr is the start of the remaining nodes
        head.next = reverseKGroupRecursive(curr, k); // recurse on rest
        return prev; // prev is new head
    }

    // =========================================================
    // H3. Sort List  LC 148
    // Pattern: MERGE SORT ON LINKED LIST
    // =========================================================
    /**
     * Sort a linked list in O(n log n) time and O(1) space.
     *
     * Why merge sort (not quick sort)?
     *   - Merge sort is stable, O(n log n) guaranteed.
     *   - Quick sort on LL is O(n log n) average but O(n²) worst.
     *   - Merge sort on LL is O(1) space (pointer re-linking, no array copying).
     *
     * Bottom-up merge sort (O(1) space, no recursion stack):
     *   - Start with sublists of size 1 (already sorted).
     *   - Repeatedly merge pairs of adjacent sublists.
     *   - Double the sublist size each pass.
     *   - O(log n) passes, each O(n) work.
     *
     * Top-down (recursive) is simpler to implement but O(log n) stack.
     *
     * Time:  O(n log n)
     * Space: O(log n) recursive  |  O(1) bottom-up iterative
     */
    public ListNode sortList(ListNode head) {
        // Top-down (recursive) — O(log n) stack
        if (head == null || head.next == null) return head;

        // Find middle and split
        ListNode mid = getMid(head);
        ListNode rightHalf = mid.next;
        mid.next = null; // cut

        ListNode left  = sortList(head);
        ListNode right = sortList(rightHalf);

        return mergeTwoLists(left, right);
    }

    private ListNode getMid(ListNode head) {
        // Returns END of first half (for even: left-leaning middle)
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    // Bottom-up O(1) space sort
    public ListNode sortListIterative(ListNode head) {
        if (head == null || head.next == null) return head;

        // Count length
        int n = 0;
        for (ListNode cur = head; cur != null; cur = cur.next) n++;

        ListNode dummy = new ListNode(0);
        dummy.next = head;

        for (int size = 1; size < n; size <<= 1) { // size = 1, 2, 4, 8, ...
            ListNode cur = dummy.next, tail = dummy;

            while (cur != null) {
                ListNode left = cur;
                ListNode right = split(left, size); // split left off (size nodes)
                cur = split(right, size);            // split right off (size nodes), cur = remainder

                // Merge left and right, append to tail
                ListNode[] merged = mergeReturn(left, right);
                tail.next = merged[0]; // head of merged
                tail = merged[1];       // tail of merged
            }
        }
        return dummy.next;
    }

    // Splits off 'size' nodes from head, returns the start of the remaining list
    private ListNode split(ListNode head, int size) {
        for (int i = 1; i < size && head != null; i++) head = head.next;
        if (head == null) return null;
        ListNode rest = head.next;
        head.next = null; // cut
        return rest;
    }

    // Merges two lists, returns [merged_head, merged_tail]
    private ListNode[] mergeReturn(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), cur = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { cur.next = a; a = a.next; }
            else                { cur.next = b; b = b.next; }
            cur = cur.next;
        }
        cur.next = (a != null) ? a : b;
        while (cur.next != null) cur = cur.next; // advance to actual tail
        return new ListNode[]{dummy.next, cur};
    }

    // =========================================================
    // H4. Flatten a Multilevel Doubly Linked List  LC 430
    // Pattern: DFS / ITERATIVE STACK
    // =========================================================
    /**
     * A doubly linked list where some nodes have a child pointer
     * pointing to another doubly linked list.
     * Flatten all levels into a single list (depth-first, child before next).
     *
     * 1 ← → 2 ← → 3 ← → 4 ← → 5
     *            |
     *            7 ← → 8 ← → 9
     *                  |
     *                  10 ← → 11
     *
     * Result: 1 ↔ 2 ↔ 3 ↔ 7 ↔ 8 ↔ 10 ↔ 11 ↔ 9 ↔ 4 ↔ 5
     *
     * Algorithm:
     *   When we see a node with a child:
     *   1. Find the TAIL of the child list.
     *   2. Insert child list between current and current.next.
     *   3. Clear child pointer.
     *   4. Continue traversal.
     *
     * Time:  O(n)
     * Space: O(1) iterative
     */
    static class MNode {
        int val;
        MNode prev, next, child;
        MNode(int v) { val = v; }
    }

    public MNode flatten(MNode head) {
        if (head == null) return null;

        MNode cur = head;
        while (cur != null) {
            if (cur.child != null) {
                MNode child = cur.child;
                MNode next  = cur.next;

                // Find tail of child list
                MNode childTail = child;
                while (childTail.next != null) childTail = childTail.next;

                // Insert child list between cur and next
                cur.next    = child;
                child.prev  = cur;
                childTail.next = next;
                if (next != null) next.prev = childTail;

                cur.child = null; // clear child pointer
            }
            cur = cur.next;
        }
        return head;
    }

    // =========================================================
    // H5. LFU Cache  LC 460
    // Pattern: MIN-FREQ TRACKING + DOUBLY LL PER FREQUENCY
    // =========================================================
    /**
     * LFU (Least Frequently Used): evict the item used fewest times.
     * Tie-break: among same frequency, evict Least Recently Used.
     *
     * Data structures:
     *   keyToNode: key → Node (for O(1) value + frequency lookup)
     *   freqToList: frequency → DLinkedList (LRU-ordered for same freq)
     *   minFreq: current minimum frequency (for O(1) eviction)
     *
     * On get(key):
     *   - Find node, increment frequency.
     *   - Move from freqToList[f] to freqToList[f+1].
     *   - If freqToList[minFreq] is empty → minFreq++.
     *
     * On put(key, val):
     *   - If exists → same as get + update value.
     *   - If new and capacity reached → evict LRU from freqToList[minFreq].
     *   - Insert new node at freqToList[1]. Set minFreq = 1.
     *
     * Time:  O(1) all operations
     * Space: O(capacity)
     */
    static class LFUCache {
        private final int capacity;
        private int minFreq;
        private final Map<Integer, int[]> keyToNode;  // key → [value, freq]
        private final Map<Integer, LinkedHashSet<Integer>> freqToKeys;
        // LinkedHashSet preserves insertion order → LRU within same frequency

        LFUCache(int capacity) {
            this.capacity    = capacity;
            this.minFreq     = 0;
            this.keyToNode   = new HashMap<>();
            this.freqToKeys  = new HashMap<>();
        }

        public int get(int key) {
            if (!keyToNode.containsKey(key)) return -1;
            incrementFreq(key);
            return keyToNode.get(key)[0];
        }

        public void put(int key, int value) {
            if (capacity <= 0) return;

            if (keyToNode.containsKey(key)) {
                keyToNode.get(key)[0] = value;
                incrementFreq(key);
                return;
            }

            if (keyToNode.size() >= capacity) {
                // Evict LFU (and LRU within that freq)
                LinkedHashSet<Integer> minFreqSet = freqToKeys.get(minFreq);
                int evictKey = minFreqSet.iterator().next(); // oldest = LRU
                minFreqSet.remove(evictKey);
                keyToNode.remove(evictKey);
            }

            // Insert new key with frequency 1
            keyToNode.put(key, new int[]{value, 1});
            freqToKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
            minFreq = 1;
        }

        private void incrementFreq(int key) {
            int[] node = keyToNode.get(key);
            int oldFreq = node[1];
            int newFreq = oldFreq + 1;
            node[1] = newFreq;

            // Remove from old freq bucket
            freqToKeys.get(oldFreq).remove(key);

            // Add to new freq bucket
            freqToKeys.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(key);

            // Update minFreq if old bucket is now empty
            if (freqToKeys.get(oldFreq).isEmpty() && oldFreq == minFreq) {
                minFreq++;
            }
        }
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        Hard sol = new Hard();

        System.out.println("═══ H1: Merge K Sorted Lists ═══");
        ListNode[] lists = { build(1, 4, 5), build(1, 3, 4), build(2, 6) };
        System.out.println(str(sol.mergeKLists(lists)));  // [1→1→2→3→4→4→5→6]

        System.out.println("\n═══ H1b: Divide & Conquer ═══");
        ListNode[] lists2 = { build(1, 4, 5), build(1, 3, 4), build(2, 6) };
        System.out.println(str(sol.mergeKListsDivideConquer(lists2)));

        System.out.println("\n═══ H2: Reverse K-Group ═══");
        System.out.println(str(sol.reverseKGroup(build(1, 2, 3, 4, 5), 2))); // [2→1→4→3→5]
        System.out.println(str(sol.reverseKGroup(build(1, 2, 3, 4, 5), 3))); // [3→2→1→4→5]

        System.out.println("\n═══ H3: Sort List ═══");
        System.out.println(str(sol.sortList(build(4, 2, 1, 3))));     // [1→2→3→4]
        System.out.println(str(sol.sortList(build(-1, 5, 3, 4, 0)))); // [-1→0→3→4→5]

        System.out.println("\n═══ H3b: Sort List Iterative O(1) space ═══");
        System.out.println(str(sol.sortListIterative(build(4, 2, 1, 3))));

        System.out.println("\n═══ H5: LFU Cache ═══");
        LFUCache lfu = new LFUCache(2);
        lfu.put(1, 1);
        lfu.put(2, 2);
        System.out.println("get(1): " + lfu.get(1)); // 1 (freq[1]=2, freq[2]=1)
        lfu.put(3, 3);                                 // evict key 2 (freq=1, LRU)
        System.out.println("get(2): " + lfu.get(2)); // -1 (evicted)
        System.out.println("get(3): " + lfu.get(3)); // 3
        lfu.put(4, 4);                                 // evict key 3 (freq=1 < freq[1]=2)
        System.out.println("get(1): " + lfu.get(1)); // 1
        System.out.println("get(3): " + lfu.get(3)); // -1 (evicted)
        System.out.println("get(4): " + lfu.get(4)); // 4
    }
}
