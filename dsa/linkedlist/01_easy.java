/**
 * ============================================================
 *  LINKED LIST — EASY PROBLEMS
 * ============================================================
 *
 *  E1.  Reverse Linked List                     LC 206
 *  E2.  Find Middle of Linked List              LC 876
 *  E3.  Detect Cycle                            LC 141
 *  E4.  Merge Two Sorted Lists                  LC 21
 *  E5.  Remove Nth Node From End                LC 19
 *  E6.  Palindrome Linked List                  LC 234
 *  E7.  Intersection of Two Linked Lists        LC 160
 *  E8.  Delete Node in a Linked List            LC 237
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
    ListNode(int v, ListNode n) { val = v; next = n; }
}

class Easy {

    // =========================================================
    // Helper: build and print lists
    // =========================================================
    static ListNode build(int... vals) {
        ListNode dummy = new ListNode(0);
        ListNode cur = dummy;
        for (int v : vals) { cur.next = new ListNode(v); cur = cur.next; }
        return dummy.next;
    }

    static String str(ListNode head) {
        StringBuilder sb = new StringBuilder();
        while (head != null) {
            sb.append(head.val);
            if (head.next != null) sb.append(" → ");
            head = head.next;
        }
        return "[" + sb + "]";
    }

    // =========================================================
    // E1. Reverse Linked List  LC 206
    // Pattern: REVERSE (iterative + recursive)
    // =========================================================
    /**
     * Brute: collect to array, rebuild — O(n) time O(n) space
     * Optimal: 3-pointer in-place — O(n) time O(1) space
     *
     * Key insight: at every step redirect curr.next → prev.
     * Always save next BEFORE redirect (or you lose the rest of the list).
     *
     * Time:  O(n)
     * Space: O(1) iterative  |  O(n) recursive stack
     */
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;

        while (curr != null) {
            ListNode next = curr.next; // SAVE before redirect
            curr.next = prev;          // redirect
            prev = curr;               // advance prev
            curr = next;               // advance curr
        }

        return prev; // prev is now the new head
    }

    // Recursive version — understand both for interviews
    public ListNode reverseListRecursive(ListNode head) {
        if (head == null || head.next == null) return head;
        ListNode newHead = reverseListRecursive(head.next); // recurse to tail
        head.next.next = head; // reverse the link
        head.next = null;      // cut old forward link
        return newHead;
    }

    // Follow-up: reverse only positions [left, right] — see medium file

    // =========================================================
    // E2. Find Middle of Linked List  LC 876
    // Pattern: FAST & SLOW (single pass)
    // =========================================================
    /**
     * Brute: count n, walk n/2 steps — 2 passes O(n)
     * Optimal: fast/slow pointers — 1 pass O(n)
     *
     * fast moves 2× slow. When fast reaches end, slow is at middle.
     *
     * ODD  length (5 nodes): slow stops at node 3 (index 2) ← middle
     * EVEN length (6 nodes): slow stops at node 4 (index 3) ← second middle
     *
     * If you need FIRST middle of even list:
     *   use: while (fast.next != null && fast.next.next != null)
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow; // second middle for even length
    }

    // =========================================================
    // E3. Detect Cycle  LC 141
    // Pattern: FAST & SLOW (Floyd's Tortoise & Hare)
    // =========================================================
    /**
     * Brute: HashSet of visited nodes — O(n) time O(n) space
     * Optimal: Floyd's — O(n) time O(1) space
     *
     * If there's a cycle, fast and slow will eventually meet.
     * If no cycle, fast reaches null first.
     *
     * WHY THEY MEET:
     *   Imagine fast "chasing" slow inside the cycle.
     *   Each step, fast gains 1 position on slow.
     *   Since they're in a finite cycle, they must meet.
     *
     * Time:  O(n)  — at most 2n steps before meeting or exit
     * Space: O(1)
     */
    public boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true; // they met → cycle exists
        }
        return false; // fast reached null → no cycle
    }

    // Follow-up: find the CYCLE ENTRY NODE  LC 142
    public ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;

        // Phase 1: detect cycle
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) break;
        }

        // No cycle
        if (fast == null || fast.next == null) return null;

        // Phase 2: find entry
        // Reset slow to head; keep fast at meeting point
        // Both move ONE step at a time — they meet at cycle entry
        slow = head;
        while (slow != fast) {
            slow = slow.next;
            fast = fast.next;
        }
        return slow; // cycle entry node
    }

    // =========================================================
    // E4. Merge Two Sorted Lists  LC 21
    // Pattern: MERGE + DUMMY NODE
    // =========================================================
    /**
     * Use a dummy head to avoid special-casing the first node.
     * At each step, pick the smaller of l1.val vs l2.val.
     * When one list is exhausted, append the rest of the other.
     *
     * Time:  O(m + n)
     * Space: O(1)  — in-place pointer reassignment, no new nodes
     */
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0); // sentinel — avoids head special case
        ListNode cur = dummy;

        while (l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                cur.next = l1;
                l1 = l1.next;
            } else {
                cur.next = l2;
                l2 = l2.next;
            }
            cur = cur.next;
        }

        // Attach remaining (at most one of these is non-null)
        cur.next = (l1 != null) ? l1 : l2;

        return dummy.next;
    }

    // =========================================================
    // E5. Remove Nth Node From End  LC 19
    // Pattern: FAST & SLOW + DUMMY NODE (single pass)
    // =========================================================
    /**
     * Brute: count length n, walk to (n-k)th node — 2 passes
     * Optimal: two pointers with n-step gap — 1 pass
     *
     * Trick:
     *   1. Advance fast n+1 steps from dummy.
     *   2. Move both until fast == null.
     *   3. slow.next is the node to delete.
     *
     * Why n+1? So slow lands on the node BEFORE the target
     * (making deletion O(1) by adjusting slow.next).
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode slow = dummy, fast = dummy;

        // Advance fast n+1 steps
        for (int i = 0; i <= n; i++) fast = fast.next;

        // Move both until fast == null
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }

        // slow.next is the target — skip it
        slow.next = slow.next.next;

        return dummy.next;
    }

    // =========================================================
    // E6. Palindrome Linked List  LC 234
    // Pattern: FAST & SLOW + REVERSE + COMPARE
    // =========================================================
    /**
     * Brute: copy to array, two-pointer check — O(n) time O(n) space
     * Optimal: find middle, reverse second half, compare — O(n) time O(1) space
     *
     * Steps:
     *   1. Find middle using fast/slow.
     *   2. Reverse second half.
     *   3. Compare first half with reversed second half.
     *   4. (Optional) Restore original list.
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public boolean isPalindrome(ListNode head) {
        if (head == null || head.next == null) return true;

        // Step 1: find end of first half
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        // slow is now at end of first half (for both odd and even lengths)

        // Step 2: reverse second half
        ListNode secondHalf = reverse(slow.next);

        // Step 3: compare
        ListNode p1 = head, p2 = secondHalf;
        boolean result = true;
        while (p2 != null) {
            if (p1.val != p2.val) { result = false; break; }
            p1 = p1.next;
            p2 = p2.next;
        }

        // Step 4: restore (good practice — don't mutate input permanently)
        slow.next = reverse(secondHalf);

        return result;
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
    // E7. Intersection of Two Linked Lists  LC 160
    // Pattern: TWO POINTERS (length equalization)
    // =========================================================
    /**
     * Brute: HashSet of nodes from list A, scan list B — O(m+n) O(m)
     * Optimal: two-pointer trick — O(m+n) O(1)
     *
     * Key insight:
     *   If lists A and B intersect at node X:
     *   pA travels: (lenA - lenX) + lenX + (lenB - lenX) = lenA + lenB - lenX
     *   pB travels: (lenB - lenX) + lenX + (lenA - lenX) = lenA + lenB - lenX
     *   Same distance → they meet at X!
     *
     *   If no intersection, both reach null at the same time.
     *
     * Time:  O(m + n)
     * Space: O(1)
     */
    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        if (headA == null || headB == null) return null;

        ListNode pA = headA, pB = headB;

        while (pA != pB) {
            pA = (pA == null) ? headB : pA.next; // when A exhausted, redirect to B's head
            pB = (pB == null) ? headA : pB.next; // when B exhausted, redirect to A's head
        }

        return pA; // either the intersection node or null (both null = no intersection)
    }

    // =========================================================
    // E8. Delete Node in a Linked List  LC 237
    // Pattern: VALUE COPY TRICK
    // =========================================================
    /**
     * Normally deletion requires access to the previous node.
     * Here we can only access the node to delete (not its predecessor).
     *
     * Trick: copy next node's value into current, then skip next.
     * Guaranteed: node is not the tail.
     *
     * Time:  O(1)
     * Space: O(1)
     */
    public void deleteNode(ListNode node) {
        node.val = node.next.val; // copy next value into current
        node.next = node.next.next; // skip the now-duplicate next node
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        Easy sol = new Easy();

        System.out.println("═══ E1: Reverse List ═══");
        ListNode l1 = build(1, 2, 3, 4, 5);
        System.out.println("Before: " + str(l1));
        System.out.println("After:  " + str(sol.reverseList(l1)));

        System.out.println("\n═══ E2: Find Middle ═══");
        System.out.println("5 nodes: " + str(sol.middleNode(build(1, 2, 3, 4, 5)))); // node 3
        System.out.println("6 nodes: " + str(sol.middleNode(build(1, 2, 3, 4, 5, 6)))); // node 4

        System.out.println("\n═══ E3: Detect Cycle ═══");
        ListNode noCycle = build(1, 2, 3, 4);
        System.out.println("No cycle: " + sol.hasCycle(noCycle)); // false
        ListNode n1 = new ListNode(1), n2 = new ListNode(2);
        ListNode n3 = new ListNode(3), n4 = new ListNode(4);
        n1.next = n2; n2.next = n3; n3.next = n4; n4.next = n2; // cycle at n2
        System.out.println("Has cycle: " + sol.hasCycle(n1)); // true

        System.out.println("\n═══ E4: Merge Two Sorted ═══");
        ListNode a = build(1, 3, 5), b = build(2, 4, 6);
        System.out.println(str(a) + " + " + str(b) + " = " + str(sol.mergeTwoLists(a, b)));

        System.out.println("\n═══ E5: Remove Nth from End ═══");
        ListNode r = build(1, 2, 3, 4, 5);
        System.out.println("Remove 2nd from end: " + str(sol.removeNthFromEnd(r, 2))); // [1,2,3,5]

        System.out.println("\n═══ E6: Palindrome ═══");
        System.out.println("1→2→2→1: " + sol.isPalindrome(build(1, 2, 2, 1))); // true
        System.out.println("1→2→3:   " + sol.isPalindrome(build(1, 2, 3)));     // false

        System.out.println("\n═══ E7: Intersection ═══");
        // Build: A: 4→1→8→4→5  B: 5→6→1→8→4→5  (intersect at node 8)
        ListNode shared = build(8, 4, 5);
        ListNode listA = new ListNode(4); listA.next = new ListNode(1); listA.next.next = shared;
        ListNode listB = new ListNode(5); listB.next = new ListNode(6);
        listB.next.next = new ListNode(1); listB.next.next.next = shared;
        ListNode inter = sol.getIntersectionNode(listA, listB);
        System.out.println("Intersection: " + (inter != null ? inter.val : "none")); // 8
    }
}
