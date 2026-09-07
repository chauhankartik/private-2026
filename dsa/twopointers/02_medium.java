/**
 * ============================================================
 *  TWO POINTERS — MEDIUM PROBLEMS
 *  Triplets, partitions, interval problems, and linked lists.
 * ============================================================
 *
 *  Problems:
 *   M1. 3Sum (LC 15)
 *   M2. Container With Most Water (LC 11)
 *   M3. Sort Colors — Dutch National Flag (LC 75)
 *   M4. 3Sum Closest (LC 16)
 *   M5. Linked List Cycle II — Find Cycle Start (LC 142)
 *   M6. Remove Nth Node From End of List (LC 19)
 *   M7. Intersection of Two Arrays II (LC 350)
 *   M8. Minimum Number of Moves to Seat Everyone (LC 2037)
 *
 * ============================================================
 */
import java.util.*;

class Medium {

    // =========================================================
    // M1. 3SUM
    // Pattern: Sort + Fix + Two-Pointer Inner Pass
    // LeetCode 15
    // =========================================================
    /**
     * Problem: Given integer array nums, return all triplets [i, j, k]
     * such that i != j, i != k, j != k, and nums[i]+nums[j]+nums[k] == 0.
     * Result must have NO duplicate triplets.
     *
     * Brute Force O(n³): Try all triples. Deduplicate with a set. Way too slow.
     *
     * Optimal O(n²):
     * 1. Sort the array.
     * 2. For each index i (fixed element), run two-pointer on [i+1 .. n-1].
     * 3. Skip duplicates at ALL levels.
     *
     * Duplicate skipping rules:
     *   - Outer: if nums[i] == nums[i-1], skip (same fixed element, same pairs found).
     *   - Inner (after match): advance left/right past same values before final step.
     *
     * Cannot do better than O(n²): lower bound for 3Sum is Ω(n²) in comparison model.
     *
     * Time: O(n²) — outer O(n) × inner O(n)
     * Space: O(1) extra (O(n) for sort)
     */
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();

        for (int i = 0; i < nums.length - 2; i++) {
            // Early termination: if the smallest possible sum > 0, done
            if (nums[i] > 0) break;

            // Skip duplicate fixed elements
            if (i > 0 && nums[i] == nums[i - 1]) continue;

            int left = i + 1, right = nums.length - 1;

            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];

                if (sum == 0) {
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    // Skip duplicates for left and right
                    while (left < right && nums[left] == nums[left + 1]) left++;
                    while (left < right && nums[right] == nums[right - 1]) right--;
                    left++;
                    right--;
                } else if (sum < 0) {
                    left++;  // need larger sum
                } else {
                    right--; // need smaller sum
                }
            }
        }
        return result;
    }

    /**
     * Follow-up 1: 3Sum with a DIFFERENT target (not zero)?
     *   → Same approach: change the target in the sum comparison.
     *
     * Follow-up 2: Count the number of triplets (don't collect them)?
     *   → When match found, count carefully how many left/right duplicates exist.
     *   → leftCount * rightCount contributes multiple triplets.
     *
     * Follow-up 3: 4Sum (LC 18)?
     *   → Add another outer loop (fix two elements), run two-pointer on rest.
     *   → O(n³) time.
     */

    // =========================================================
    // M2. CONTAINER WITH MOST WATER
    // Pattern: Opposite-End Two Pointers — Greedy Width Reduction
    // LeetCode 11
    // =========================================================
    /**
     * Problem: Given heights array, find two lines that together with
     * the x-axis forms a container holding the most water.
     *
     * Brute Force O(n²): Try all pairs.
     *
     * Optimal O(n): Two pointers squeeze inward.
     * Area = min(height[left], height[right]) * (right - left)
     *
     * Key insight (greedy proof):
     * The width decreases by 1 with each step. To have a chance of
     * improving area, we need height to increase. The bottleneck is
     * the SHORTER side. Moving the taller side inward can only maintain
     * or decrease the height cap → area can only decrease or stay.
     * Therefore: ALWAYS move the pointer at the SHORTER height.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int maxArea(int[] height) {
        int left = 0, right = height.length - 1;
        int maxWater = 0;

        while (left < right) {
            int water = Math.min(height[left], height[right]) * (right - left);
            maxWater = Math.max(maxWater, water);

            // Move the pointer at the shorter height inward
            if (height[left] <= height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return maxWater;
    }

    /**
     * Follow-up 1: What if we need to find the actual pair of indices?
     *   → Track bestLeft, bestRight alongside maxWater.
     *
     * Follow-up 2: Trapping Rain Water (LC 42) — 3D version?
     *   → Harder: water trapped BETWEEN walls (not just outermost two).
     *   → Use two-pointer with maxLeft, maxRight tracking. See Hard problems.
     */

    // =========================================================
    // M3. SORT COLORS — DUTCH NATIONAL FLAG
    // Pattern: Three Pointers — Dutch National Flag
    // LeetCode 75
    // =========================================================
    /**
     * Problem: Sort array with only 0s, 1s, 2s in-place in one pass.
     *
     * Invariants maintained at all times:
     *   [0 .. low-1]   = all 0s
     *   [low .. mid-1] = all 1s
     *   [mid .. high]  = unsorted / unknown
     *   [high+1 .. n-1]= all 2s
     *
     * Three cases:
     *   nums[mid] == 0: belongs in 0-region → swap(low, mid), low++, mid++
     *   nums[mid] == 1: already in 1-region → mid++
     *   nums[mid] == 2: belongs in 2-region → swap(mid, high), high--
     *                   DON'T advance mid (swapped element is unclassified)
     *
     * Time: O(n) — single pass
     * Space: O(1)
     */
    public void sortColors(int[] nums) {
        int low = 0, mid = 0, high = nums.length - 1;

        while (mid <= high) {
            if (nums[mid] == 0) {
                swap(nums, low++, mid++); // 0 goes left, both advance
            } else if (nums[mid] == 1) {
                mid++;                    // 1 is already in correct region
            } else { // nums[mid] == 2
                swap(nums, mid, high--);  // 2 goes right, only high retreats
                // DO NOT advance mid: re-examine the element that came from high
            }
        }
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    /**
     * Follow-up 1: k colors instead of 3?
     *   → Use counting sort: count each color, overwrite array. O(n+k).
     *   → Or use a generalized multi-pointer (complex).
     *
     * Follow-up 2: What if we could use extra space?
     *   → Counting sort: count 0s, 1s, 2s, rewrite. O(n) time, O(1) space too.
     */

    // =========================================================
    // M4. 3SUM CLOSEST
    // Pattern: Sort + Fix + Two-Pointer — Track Closest Sum
    // LeetCode 16
    // =========================================================
    /**
     * Problem: Given integer array nums and int target,
     * find three integers whose sum is CLOSEST to target.
     * Return that sum.
     *
     * Same structure as 3Sum, but instead of checking sum == 0:
     * track the sum with minimum |sum - target|.
     * No duplicate skipping needed (we want the closest, not unique triplets).
     *
     * Time: O(n²)
     * Space: O(1) extra
     */
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int closest = nums[0] + nums[1] + nums[2]; // initialize with first triplet

        for (int i = 0; i < nums.length - 2; i++) {
            int left = i + 1, right = nums.length - 1;

            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];

                if (Math.abs(sum - target) < Math.abs(closest - target)) {
                    closest = sum;
                }

                if (sum < target) {
                    left++;       // need to increase sum
                } else if (sum > target) {
                    right--;      // need to decrease sum
                } else {
                    return sum;   // exact match — can't do better
                }
            }
        }
        return closest;
    }

    /**
     * Follow-up 1: Return the actual triplet (not just the sum)?
     *   → Track bestI, bestL, bestR alongside closest.
     *
     * Follow-up 2: What if we want the FURTHEST from target?
     *   → Track the sum with maximum |sum - target|.
     *   → Move the pointer that DECREASES the distance (opposite logic).
     */

    // =========================================================
    // M5. LINKED LIST CYCLE II — FIND START OF CYCLE
    // Pattern: Fast/Slow Pointers — Floyd's Algorithm
    // LeetCode 142
    // =========================================================
    /**
     * Problem: Given linked list, return the node where the cycle begins.
     * Return null if no cycle.
     *
     * Phase 1 — Detect cycle (LC 141):
     *   slow moves 1 step, fast moves 2 steps.
     *   If they meet → cycle exists.
     *
     * Phase 2 — Find cycle start:
     *   Reset slow to head. Move both slow and fast ONE step at a time.
     *   They meet at the cycle start.
     *
     * Mathematical proof:
     *   Let F = distance from head to cycle start
     *       a = distance from cycle start to meeting point (within cycle)
     *       C = cycle length
     *
     *   At meeting point:
     *     slow traveled: F + a
     *     fast traveled: F + a + n*C  (fast did n extra laps)
     *     Since fast = 2 * slow:
     *       F + a + n*C = 2(F + a)
     *       n*C = F + a
     *       F = n*C - a
     *
     *   Moving from head: pointer at head travels F steps.
     *   Moving from meeting point: pointer travels n*C - a = F steps to cycle start.
     *   → They meet at cycle start!
     *
     * Time: O(n)
     * Space: O(1)
     */
    public ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;

        // Phase 1: find meeting point
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) break;
        }

        // No cycle
        if (fast == null || fast.next == null) return null;

        // Phase 2: find cycle start
        slow = head;
        while (slow != fast) {
            slow = slow.next;
            fast = fast.next; // both move 1 step now
        }
        return slow; // meeting point = cycle start
    }

    // Helper ListNode class
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /**
     * Follow-up 1: Find the LENGTH of the cycle?
     *   → After Phase 1, keep slow at meeting point.
     *   → Advance fast one step at a time until it returns to slow, counting steps.
     *
     * Follow-up 2: Remove the cycle (not just detect it)?
     *   → Run Phase 1 and 2 to find the cycle start.
     *   → Walk one pointer around the cycle until it reaches the node just before cycle start.
     *   → Set that node's next to null.
     */

    // =========================================================
    // M6. REMOVE NTH NODE FROM END OF LIST
    // Pattern: Fast/Slow Pointers — Distance-Gap Technique
    // LeetCode 19
    // =========================================================
    /**
     * Problem: Remove the n-th node from the end of a linked list.
     * Return the modified head. Do it in one pass.
     *
     * Key trick: Create a gap of n+1 between fast and slow.
     * When fast reaches null, slow is at the node BEFORE the target.
     *
     * Why dummy head?
     *   Edge case: removing the first node. A dummy head simplifies
     *   the "node before" pointer without null checks.
     *
     * Time: O(L) — single pass
     * Space: O(1)
     */
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        ListNode fast = dummy, slow = dummy;

        // Advance fast n+1 steps ahead (so gap = n+1)
        for (int i = 0; i <= n; i++) {
            fast = fast.next;
        }

        // Move both until fast reaches null
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }

        // slow is at the node BEFORE the target → remove target
        slow.next = slow.next.next;

        return dummy.next;
    }

    /**
     * Follow-up 1: What if n is equal to the length (remove first node)?
     *   → After advancing fast n+1 steps, fast reaches null if n == length.
     *   → Dummy head handles this: slow stays at dummy, removes dummy.next.
     *
     * Follow-up 2: Return the REMOVED node's value?
     *   → Capture slow.next.val before removing it.
     */

    // =========================================================
    // M7. INTERSECTION OF TWO ARRAYS II
    // Pattern: Sort + Merge-Style Two Pointers
    // LeetCode 350
    // =========================================================
    /**
     * Problem: Given two integer arrays nums1 and nums2, return an array
     * of their intersection (including duplicates).
     *
     * Approach 1 — HashMap O(m+n), O(min(m,n)) space:
     *   Count frequencies in smaller array. For each in larger array,
     *   check and decrement count.
     *
     * Approach 2 — Sort + Two Pointers O((m+n) log(m+n)), O(1) space:
     *   Preferred when arrays are ALREADY SORTED or memory is tight.
     *
     * Time: O(m log m + n log n) for sort, O(m+n) for merge
     * Space: O(1) extra (ignoring output)
     */
    public int[] intersect(int[] nums1, int[] nums2) {
        Arrays.sort(nums1);
        Arrays.sort(nums2);

        int i = 0, j = 0;
        List<Integer> result = new ArrayList<>();

        while (i < nums1.length && j < nums2.length) {
            if (nums1[i] == nums2[j]) {
                result.add(nums1[i]); // common element
                i++;
                j++;
            } else if (nums1[i] < nums2[j]) {
                i++; // advance the smaller
            } else {
                j++;
            }
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Follow-up 1: What if both arrays are sorted already?
     *   → Skip the sort. Pure O(m+n) time.
     *
     * Follow-up 2: What if nums1 fits in memory but nums2 is on disk?
     *   → Load nums1 into a HashMap. Stream nums2 from disk, look up each element.
     *
     * Follow-up 3: Intersection I (LC 349) — no duplicates in result?
     *   → Same two-pointer approach; just skip duplicates after each match.
     */

    // =========================================================
    // M8. MINIMUM NUMBER OF MOVES TO SEAT EVERYONE
    // Pattern: Sort Both Arrays + Parallel Two Pointers
    // LeetCode 2037
    // =========================================================
    /**
     * Problem: n seats and n students at positions seats[] and students[].
     * A move shifts a student one step. Find minimum total moves to
     * seat each student in a distinct seat.
     *
     * Key insight: After SORTING both arrays, the optimal assignment
     * is to pair the i-th student with the i-th seat.
     *
     * Proof (exchange argument):
     *   If student at position s_i is assigned seat t_j, and s_k is assigned t_i
     *   with j < i and s_i > s_k (crossing assignment), we can uncross them.
     *   Uncrossing never increases total distance (triangle inequality in sorted order).
     *   → Optimal assignment has no crossings → sort both and pair by index.
     *
     * Time: O(n log n)
     * Space: O(1) extra
     */
    public int minMovesToSeat(int[] seats, int[] students) {
        Arrays.sort(seats);
        Arrays.sort(students);

        int totalMoves = 0;
        for (int i = 0; i < seats.length; i++) {
            totalMoves += Math.abs(seats[i] - students[i]);
        }
        return totalMoves;
    }

    /**
     * Follow-up 1: What if each seat can hold multiple students?
     *   → Greedy assignment: group students into buckets.
     *   → Sort students; assign k consecutive students to each seat.
     *
     * Follow-up 2: What if moves can be diagonal (2D grid)?
     *   → Chebyshev distance. Decompose into x and y coordinates separately.
     */
}
