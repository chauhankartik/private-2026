/**
 * ============================================================
 *  LINKED LIST — MEDIUM PROBLEMS
 * ============================================================
 *
 *  M1.  Add Two Numbers                         LC 2
 *  M2.  Odd Even Linked List                    LC 328
 *  M3.  Reverse Linked List II (sublist)        LC 92
 *  M4.  Rotate List                             LC 61
 *  M5.  Copy List with Random Pointer           LC 138
 *  M6.  Reorder List                            LC 143
 *  M7.  LRU Cache                               LC 146
 *  M8.  Swap Nodes in Pairs                     LC 24
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

class Node {
    int val;
    Node next, random;
    Node(int v) { val = v; }
}

class Medium {

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
    // M1. Add Two Numbers  LC 2
    // Pattern: CARRY ARITHMETIC + DUMMY NODE
    // =========================================================
    /**
     * Numbers stored in REVERSE order (ones digit first).
     * Walk both lists simultaneously, carry the overflow.
     *
     * Key: handle unequal lengths + final carry.
     *   [2→4→3] + [5→6→4] = [7→0→8]  (342 + 465 = 807)
     *
     * Time:  O(max(m, n))
     * Space: O(max(m, n)) — for result list
     */
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0), cur = dummy;
        int carry = 0;

        while (l1 != null || l2 != null || carry != 0) {
            int sum = carry;
            if (l1 != null) { sum += l1.val; l1 = l1.next; }
            if (l2 != null) { sum += l2.val; l2 = l2.next; }

            carry = sum / 10;
            cur.next = new ListNode(sum % 10);
            cur = cur.next;
        }

        return dummy.next;
    }

    // Follow-up: numbers stored in forward order → reverse both first, add, reverse result
    // Or use a stack to process from end (but adds O(n) space)

    // =========================================================
    // M2. Odd Even Linked List  LC 328
    // Pattern: IN-PLACE POINTER MANIPULATION
    // =========================================================
    /**
     * Group all nodes at ODD positions first, then EVEN.
     * Position = 1-indexed (1st node is odd).
     *
     * Key: maintain odd-tail, even-head, and even-tail.
     *   Connect odd-tail → even-head at the end.
     *
     * 1 → 2 → 3 → 4 → 5
     * odd:  1 → 3 → 5
     * even: 2 → 4
     * result: 1 → 3 → 5 → 2 → 4
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public ListNode oddEvenList(ListNode head) {
        if (head == null) return null;

        ListNode odd = head, even = head.next, evenHead = even;

        while (even != null && even.next != null) {
            odd.next = even.next;   // link next odd
            odd = odd.next;
            even.next = odd.next;   // link next even
            even = even.next;
        }

        odd.next = evenHead;        // attach even list to end of odd list
        return head;
    }

    // =========================================================
    // M3. Reverse Linked List II  LC 92
    // Pattern: IN-PLACE SUBLIST REVERSE
    // =========================================================
    /**
     * Reverse nodes from position left to right (1-indexed).
     * Single pass — no extra space.
     *
     * Steps:
     *   1. Walk to node before position left (call it "before").
     *   2. Reverse the sublist [left, right] in place.
     *   3. Re-attach: before.next = new_head; old_head.next = after.
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode before = dummy;

        // Step 1: move to node just before 'left'
        for (int i = 1; i < left; i++) before = before.next;

        // Step 2: reverse [left, right]
        ListNode curr = before.next;
        ListNode prev = null;
        for (int i = 0; i <= right - left; i++) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        // prev = new head of reversed sublist
        // curr = node just after 'right'

        // Step 3: re-attach
        before.next.next = curr; // old left-node → node after right
        before.next = prev;      // before → new head of reversed sublist

        return dummy.next;
    }

    // =========================================================
    // M4. Rotate List  LC 61
    // Pattern: FIND NEW TAIL + RECONNECT
    // =========================================================
    /**
     * Rotate right by k positions.
     * 1 → 2 → 3 → 4 → 5  rotate 2  →  4 → 5 → 1 → 2 → 3
     *
     * Key insight: make it circular, then cut at the right spot.
     *   length n, rotate k → new tail is at position (n - k % n - 1)
     *
     * Steps:
     *   1. Count length n and connect tail → head (make circular).
     *   2. New tail index = n - k%n - 1. Walk to it.
     *   3. New head = new_tail.next. Cut: new_tail.next = null.
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public ListNode rotateRight(ListNode head, int k) {
        if (head == null || head.next == null || k == 0) return head;

        // Step 1: count length, find tail
        ListNode tail = head;
        int n = 1;
        while (tail.next != null) { tail = tail.next; n++; }

        // Make circular
        tail.next = head;

        // Step 2: find new tail
        k = k % n;
        int steps = n - k - 1; // steps from head to new tail
        ListNode newTail = head;
        for (int i = 0; i < steps; i++) newTail = newTail.next;

        // Step 3: cut
        ListNode newHead = newTail.next;
        newTail.next = null;

        return newHead;
    }

    // =========================================================
    // M5. Copy List with Random Pointer  LC 138
    // Pattern: HASH MAP (node → clone)
    // =========================================================
    /**
     * Brute: two passes with HashMap — O(n) time O(n) space
     * Optimal (no extra space): interleave original + clone nodes, set randoms, deinterleave
     *
     * HashMap approach (simpler, interview-preferred):
     *   Pass 1: clone all nodes, store map[original] = clone
     *   Pass 2: set clone.next = map[original.next]
     *            set clone.random = map[original.random]
     *
     * Time:  O(n)
     * Space: O(n)
     */
    public Node copyRandomList(Node head) {
        if (head == null) return null;

        Map<Node, Node> map = new HashMap<>();

        // Pass 1: create all clones
        Node cur = head;
        while (cur != null) {
            map.put(cur, new Node(cur.val));
            cur = cur.next;
        }

        // Pass 2: set next and random pointers
        cur = head;
        while (cur != null) {
            map.get(cur).next   = map.get(cur.next);   // null-safe: map.get(null) = null
            map.get(cur).random = map.get(cur.random);
            cur = cur.next;
        }

        return map.get(head);
    }

    // O(1) space version: interleave → set randoms → deinterleave
    public Node copyRandomListO1Space(Node head) {
        if (head == null) return null;

        // Step 1: interleave — A → A' → B → B' → C → C'
        Node cur = head;
        while (cur != null) {
            Node clone = new Node(cur.val);
            clone.next = cur.next;
            cur.next = clone;
            cur = clone.next;
        }

        // Step 2: set random pointers
        cur = head;
        while (cur != null) {
            if (cur.random != null)
                cur.next.random = cur.random.next; // clone's random = original.random's clone
            cur = cur.next.next;
        }

        // Step 3: deinterleave — restore original, extract clone
        cur = head;
        Node cloneHead = head.next, cloneCur = cloneHead;
        while (cur != null) {
            cur.next = cloneCur.next;             // restore original
            cloneCur.next = cur.next != null ? cur.next.next : null; // advance clone chain
            cur = cur.next;
            cloneCur = cloneCur.next;
        }

        return cloneHead;
    }

    // =========================================================
    // M6. Reorder List  LC 143
    // Pattern: FIND MIDDLE + REVERSE + MERGE
    // =========================================================
    /**
     * L0 → L1 → L2 → … → Ln-1 → Ln
     * Reorder to: L0 → Ln → L1 → Ln-1 → L2 → Ln-2 → …
     *
     * Steps:
     *   1. Find middle (fast/slow).
     *   2. Reverse second half.
     *   3. Merge-interleave first and reversed second half.
     *
     * 1 → 2 → 3 → 4 → 5
     * middle: 3, second half: 5 → 4
     * merge: 1 → 5 → 2 → 4 → 3
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public void reorderList(ListNode head) {
        if (head == null || head.next == null) return;

        // Step 1: find middle
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // Step 2: reverse second half
        ListNode secondHalf = reverse(slow.next);
        slow.next = null; // cut first half

        // Step 3: merge-interleave
        ListNode first = head, second = secondHalf;
        while (second != null) {
            ListNode tmp1 = first.next;
            ListNode tmp2 = second.next;
            first.next = second;
            second.next = tmp1;
            first = tmp1;
            second = tmp2;
        }
    }

    private ListNode reverse(ListNode head) {
        ListNode prev = null, curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    // =========================================================
    // M7. LRU Cache  LC 146
    // Pattern: DOUBLY LINKED LIST + HASHMAP
    // =========================================================
    /**
     * Requirements: get() and put() both O(1).
     *
     * Design:
     *   - HashMap<key, DNode> for O(1) lookup.
     *   - Doubly linked list for O(1) insertion/deletion at head/tail.
     *   - Most Recently Used (MRU) → keep at head.
     *   - Least Recently Used (LRU) → keep at tail (evict from here).
     *
     * On get:  move node to head.
     * On put:  if exists → update + move to head.
     *          if new    → insert at head, evict tail if over capacity.
     *
     * Time:  O(1) both operations
     * Space: O(capacity)
     */
    static class LRUCache {
        class DNode {
            int key, val;
            DNode prev, next;
            DNode(int k, int v) { key = k; val = v; }
        }

        private final int capacity;
        private final Map<Integer, DNode> map;
        private final DNode head, tail; // dummy sentinels

        LRUCache(int capacity) {
            this.capacity = capacity;
            this.map = new HashMap<>();
            this.head = new DNode(0, 0); // MRU end
            this.tail = new DNode(0, 0); // LRU end
            head.next = tail;
            tail.prev = head;
        }

        public int get(int key) {
            if (!map.containsKey(key)) return -1;
            DNode node = map.get(key);
            moveToHead(node); // mark as recently used
            return node.val;
        }

        public void put(int key, int value) {
            if (map.containsKey(key)) {
                DNode node = map.get(key);
                node.val = value;
                moveToHead(node);
            } else {
                DNode newNode = new DNode(key, value);
                map.put(key, newNode);
                addToHead(newNode);
                if (map.size() > capacity) {
                    DNode lru = removeTail(); // evict LRU
                    map.remove(lru.key);
                }
            }
        }

        private void addToHead(DNode node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
        }

        private void removeNode(DNode node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private void moveToHead(DNode node) {
            removeNode(node);
            addToHead(node);
        }

        private DNode removeTail() {
            DNode node = tail.prev; // node just before dummy tail
            removeNode(node);
            return node;
        }
    }

    // =========================================================
    // M8. Swap Nodes in Pairs  LC 24
    // Pattern: IN-PLACE PAIR SWAP + DUMMY NODE
    // =========================================================
    /**
     * Swap every two adjacent nodes.
     * 1 → 2 → 3 → 4  →  2 → 1 → 4 → 3
     *
     * Use dummy to handle the head swap cleanly.
     * Per pair: dummy → A → B → next
     *            dummy → B → A → next
     *
     * Time:  O(n)
     * Space: O(1) iterative  |  O(n) recursive
     */
    public ListNode swapPairs(ListNode head) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prev = dummy;

        while (prev.next != null && prev.next.next != null) {
            ListNode a = prev.next;
            ListNode b = prev.next.next;

            // Swap
            prev.next = b;
            a.next = b.next;
            b.next = a;

            prev = a; // a is now after b; advance to next pair
        }

        return dummy.next;
    }

    // Recursive version (clean but O(n) stack)
    public ListNode swapPairsRecursive(ListNode head) {
        if (head == null || head.next == null) return head;
        ListNode second = head.next;
        head.next = swapPairsRecursive(second.next);
        second.next = head;
        return second;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        Medium sol = new Medium();

        System.out.println("═══ M1: Add Two Numbers ═══");
        // 342 + 465 = 807
        System.out.println(str(sol.addTwoNumbers(build(2, 4, 3), build(5, 6, 4)))); // [7→0→8]

        System.out.println("\n═══ M2: Odd Even List ═══");
        System.out.println(str(sol.oddEvenList(build(1, 2, 3, 4, 5)))); // [1→3→5→2→4]

        System.out.println("\n═══ M3: Reverse Between ═══");
        System.out.println(str(sol.reverseBetween(build(1, 2, 3, 4, 5), 2, 4))); // [1→4→3→2→5]

        System.out.println("\n═══ M4: Rotate Right ═══");
        System.out.println(str(sol.rotateRight(build(1, 2, 3, 4, 5), 2))); // [4→5→1→2→3]

        System.out.println("\n═══ M6: Reorder List ═══");
        ListNode rl = build(1, 2, 3, 4, 5);
        sol.reorderList(rl);
        System.out.println(str(rl)); // [1→5→2→4→3]

        System.out.println("\n═══ M7: LRU Cache ═══");
        LRUCache lru = new LRUCache(2);
        lru.put(1, 1); lru.put(2, 2);
        System.out.println("get(1): " + lru.get(1));  // 1
        lru.put(3, 3);                                  // evicts key 2
        System.out.println("get(2): " + lru.get(2));  // -1 (evicted)
        lru.put(4, 4);                                  // evicts key 1
        System.out.println("get(1): " + lru.get(1));  // -1 (evicted)
        System.out.println("get(3): " + lru.get(3));  // 3
        System.out.println("get(4): " + lru.get(4));  // 4

        System.out.println("\n═══ M8: Swap Pairs ═══");
        System.out.println(str(sol.swapPairs(build(1, 2, 3, 4)))); // [2→1→4→3]
    }
}
