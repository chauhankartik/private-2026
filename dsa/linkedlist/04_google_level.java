/**
 * ============================================================
 *  LINKED LIST — GOOGLE-LEVEL PROBLEMS
 * ============================================================
 *
 *  G1.  Design Browser History                 LC 1472
 *  G2.  All O'one Data Structure               LC 432  ★ Google
 *  G3.  Find the Duplicate Number (LL cycle)   LC 287  ★ Google
 *  G4.  Reverse Nodes in Even Length Groups    LC 2074
 *  G5.  Maximum Twin Sum of a Linked List      LC 2130
 *
 *  Each problem includes:
 *   - Why Google asks it
 *   - Brute → optimal trace
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

class GoogleLevel {

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
    // G1. Design Browser History  LC 1472
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests whether you naturally reach for a doubly linked list
     *   over an array/stack for bidirectional navigation.
     *
     * Design a browser with:
     *   visit(url)       — navigate to url (clear forward history)
     *   back(steps)      — go back up to 'steps' pages
     *   forward(steps)   — go forward up to 'steps' pages
     *
     * Brute: two stacks (back-stack, forward-stack) — O(1) visit, O(steps) back/forward
     * Optimal: doubly linked list — O(1) all operations
     *
     * The DLL models the exact mental model: current node = current page,
     * prev chain = back history, next chain = forward history.
     * visit() = insert after current + cut forward chain.
     *
     * Time:  O(1) visit  |  O(steps) back/forward (bounded by history length)
     * Space: O(n) — n = number of visited pages
     */
    static class BrowserHistory {
        private class Page {
            String url;
            Page prev, next;
            Page(String url) { this.url = url; }
        }

        private Page current;

        BrowserHistory(String homepage) {
            current = new Page(homepage);
        }

        void visit(String url) {
            Page page = new Page(url);
            page.prev = current;
            current.next = page;  // cut forward history (old next is garbage collected)
            current = page;
        }

        String back(int steps) {
            while (steps > 0 && current.prev != null) {
                current = current.prev;
                steps--;
            }
            return current.url;
        }

        String forward(int steps) {
            while (steps > 0 && current.next != null) {
                current = current.next;
                steps--;
            }
            return current.url;
        }
    }

    // =========================================================
    // G2. All O'one Data Structure  LC 432  ★ Google Favorite
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   One of the hardest design problems. Tests deep understanding of
     *   data structure composition. Requires O(1) for ALL four operations.
     *   Commonly asked in Google system design + coding rounds.
     *
     * Design a data structure supporting:
     *   inc(key)     — increment count of key by 1
     *   dec(key)     — decrement count of key by 1 (remove if count reaches 0)
     *   getMaxKey()  — return ANY key with max count
     *   getMinKey()  — return ANY key with min count
     *
     * ALL operations must be O(1) average.
     *
     * Design:
     *   Doubly linked list of "Bucket" nodes, ordered by count (ascending).
     *   Each Bucket holds a set of keys with that count.
     *   head.next = min count bucket, tail.prev = max count bucket.
     *
     *   HashMap<key, Bucket> — O(1) lookup of which bucket a key is in.
     *
     * Operations:
     *   inc(key): find current bucket, move key to next bucket (count+1).
     *             Create new bucket between if needed.
     *   dec(key): find current bucket, move key to prev bucket (count-1).
     *             Remove bucket if empty.
     *   getMaxKey(): return any key from tail.prev bucket.
     *   getMinKey(): return any key from head.next bucket.
     *
     * Time:  O(1) all operations
     * Space: O(n) — n = distinct keys
     */
    static class AllOne {
        private class Bucket {
            int count;
            Set<String> keys = new LinkedHashSet<>();
            Bucket prev, next;
            Bucket(int c) { count = c; }
        }

        private final Bucket head, tail;                 // dummy sentinels
        private final Map<String, Bucket> keyToBucket;

        AllOne() {
            head = new Bucket(Integer.MIN_VALUE);
            tail = new Bucket(Integer.MAX_VALUE);
            head.next = tail;
            tail.prev = head;
            keyToBucket = new HashMap<>();
        }

        public void inc(String key) {
            if (!keyToBucket.containsKey(key)) {
                // New key → goes into count=1 bucket (next after head)
                if (head.next.count != 1) insertBucketAfter(new Bucket(1), head);
                head.next.keys.add(key);
                keyToBucket.put(key, head.next);
            } else {
                Bucket cur = keyToBucket.get(key);
                int newCount = cur.count + 1;
                // Ensure bucket with newCount exists after cur
                if (cur.next.count != newCount) insertBucketAfter(new Bucket(newCount), cur);
                cur.next.keys.add(key);
                keyToBucket.put(key, cur.next);
                cur.keys.remove(key);
                if (cur.keys.isEmpty()) removeBucket(cur);
            }
        }

        public void dec(String key) {
            if (!keyToBucket.containsKey(key)) return;
            Bucket cur = keyToBucket.get(key);
            if (cur.count == 1) {
                keyToBucket.remove(key); // count drops to 0 → remove
            } else {
                int newCount = cur.count - 1;
                if (cur.prev.count != newCount) insertBucketAfter(new Bucket(newCount), cur.prev);
                cur.prev.keys.add(key);
                keyToBucket.put(key, cur.prev);
            }
            cur.keys.remove(key);
            if (cur.keys.isEmpty()) removeBucket(cur);
        }

        public String getMaxKey() {
            if (tail.prev == head) return "";
            return tail.prev.keys.iterator().next();
        }

        public String getMinKey() {
            if (head.next == tail) return "";
            return head.next.keys.iterator().next();
        }

        private void insertBucketAfter(Bucket newBucket, Bucket prev) {
            newBucket.prev = prev;
            newBucket.next = prev.next;
            prev.next.prev = newBucket;
            prev.next = newBucket;
        }

        private void removeBucket(Bucket bucket) {
            bucket.prev.next = bucket.next;
            bucket.next.prev = bucket.prev;
        }
    }

    // =========================================================
    // G3. Find the Duplicate Number  LC 287  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Hard to see the connection to linked lists. Tests pattern recognition.
     *   The key insight: treat the array as a linked list where arr[i] is next pointer.
     *   If there's a duplicate, there's a cycle → Floyd's algorithm!
     *
     * Given array of n+1 integers in [1, n]. One number appears twice.
     * Find it without modifying the array, O(1) extra space.
     *
     * Brute: sort → scan O(n log n)  |  HashSet O(n) space  — both violate constraints
     * Binary search on value range: O(n log n) time, O(1) space — valid but not optimal
     * Floyd's cycle detection: O(n) time, O(1) space — optimal
     *
     * Key insight:
     *   Treat arr as a function f(i) = arr[i].
     *   Since values are in [1, n] and there are n+1 elements, f maps [0,n] to [1,n].
     *   Starting from index 0, following arr[0] → arr[arr[0]] → ...
     *   creates a sequence. The duplicate causes a cycle.
     *   The cycle entry = the duplicate number.
     *
     * Example: [1, 3, 4, 2, 2]
     *   0 → 1 → 3 → 2 → 4 → 2 → 4 → 2 → ... (cycle at 2)
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public int findDuplicate(int[] nums) {
        // Phase 1: detect meeting point
        int slow = nums[0], fast = nums[0];
        do {
            slow = nums[slow];
            fast = nums[nums[fast]];
        } while (slow != fast);

        // Phase 2: find cycle entry (= duplicate)
        slow = nums[0];
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }
        return slow;
    }

    // =========================================================
    // G4. Reverse Nodes in Even Length Groups  LC 2074
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests ability to handle variable group sizes cleanly.
     *   Common trap: last group may be smaller than expected.
     *
     * Groups: 1 node, 2 nodes, 3 nodes, 4 nodes, ...
     * Reverse groups with EVEN number of nodes.
     *
     * Input:  5 → 2 → 6 → 3 → 9 → 1 → 7 → 3 → 8 → 4
     * Groups: [5] [2,6] [3,9,1] [7,3,8,4]   ← last group has 4 (even) nodes
     * Reverse even: [5] [6,2] [3,9,1] [4,8,3,7]
     * Result: 5 → 6 → 2 → 3 → 9 → 1 → 4 → 8 → 3 → 7
     *
     * Algorithm:
     *   Track prev (last node of previous group).
     *   For each group i: collect up to i nodes starting from prev.next.
     *   If actual count is even → reverse that sublist.
     *   Advance prev to end of current group.
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public ListNode reverseEvenLengthGroups(ListNode head) {
        ListNode prev = head; // prev is the last node of the previous group
        int groupSize = 2;    // group 1 already handled (size 1, always odd → skip)

        while (prev.next != null) {
            // Count actual nodes in this group (may be less than groupSize for last group)
            ListNode node = prev;
            int count = 0;
            while (count < groupSize && node.next != null) {
                node = node.next;
                count++;
            }

            // If even count, reverse this group
            if (count % 2 == 0) {
                // Reverse 'count' nodes starting at prev.next
                ListNode cur  = prev.next;
                ListNode nextGroup = node.next; // first node of next group
                node.next = null; // detach current group

                // Reverse
                ListNode reversed = reverse(cur);

                // Reattach
                prev.next = reversed;  // connect to new head of reversed group
                cur.next = nextGroup;  // old head (now tail) → next group
                prev = cur;            // advance prev to end of reversed group
            } else {
                prev = node; // advance prev to end of this (unchanged) group
            }

            groupSize++;
        }

        return head;
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
    // G5. Maximum Twin Sum of a Linked List  LC 2130
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Clean combination of two patterns: find middle + reverse second half.
     *   Tests pattern recognition and clean implementation.
     *
     * For a linked list of EVEN length n, twin of node i is node (n-1-i).
     * Return the maximum twin sum (i.val + twin_i.val).
     *
     * Example: 5 → 4 → 2 → 1
     *   Twins: (5,1) sum=6, (4,2) sum=6 → max=6
     *
     * Brute: collect to array, two-pointer O(n) space
     * Optimal: find middle, reverse second half, scan — O(n) time O(1) space
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public int pairSum(ListNode head) {
        // Step 1: find middle
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        // slow = start of second half

        // Step 2: reverse second half
        ListNode secondHalf = reverse(slow);

        // Step 3: scan both halves, compute max twin sum
        int maxSum = 0;
        ListNode p1 = head, p2 = secondHalf;
        while (p2 != null) {
            maxSum = Math.max(maxSum, p1.val + p2.val);
            p1 = p1.next;
            p2 = p2.next;
        }

        return maxSum;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        GoogleLevel sol = new GoogleLevel();

        System.out.println("═══ G1: Browser History ═══");
        BrowserHistory bh = new BrowserHistory("leetcode.com");
        bh.visit("google.com");
        bh.visit("facebook.com");
        bh.visit("youtube.com");
        System.out.println("back(1): "    + bh.back(1));    // facebook.com
        System.out.println("back(1): "    + bh.back(1));    // google.com
        System.out.println("forward(1): " + bh.forward(1)); // facebook.com
        bh.visit("twitter.com");
        System.out.println("forward(2): " + bh.forward(2)); // twitter.com (no forward)
        System.out.println("back(7): "    + bh.back(7));    // leetcode.com

        System.out.println("\n═══ G2: All O'one ═══");
        AllOne allOne = new AllOne();
        allOne.inc("a"); allOne.inc("b"); allOne.inc("b");
        allOne.inc("c"); allOne.inc("c"); allOne.inc("c");
        System.out.println("max: " + allOne.getMaxKey()); // c (count=3)
        System.out.println("min: " + allOne.getMinKey()); // a (count=1)
        allOne.dec("b"); allOne.dec("b");
        System.out.println("min after dec b×2: " + allOne.getMinKey()); // b or a

        System.out.println("\n═══ G3: Find Duplicate ═══");
        System.out.println(sol.findDuplicate(new int[]{1, 3, 4, 2, 2})); // 2
        System.out.println(sol.findDuplicate(new int[]{3, 1, 3, 4, 2})); // 3

        System.out.println("\n═══ G4: Reverse Even Groups ═══");
        ListNode l = build(5, 2, 6, 3, 9, 1, 7, 3, 8, 4);
        System.out.println(str(sol.reverseEvenLengthGroups(l)));
        // [5→6→2→3→9→1→4→8→3→7]

        System.out.println("\n═══ G5: Maximum Twin Sum ═══");
        System.out.println(sol.pairSum(build(5, 4, 2, 1)));  // 6
        System.out.println(sol.pairSum(build(4, 2, 2, 3)));  // 7
        System.out.println(sol.pairSum(build(1, 100000)));   // 100001
    }
}
