/**
 * ============================================================
 *  GREEDY ALGORITHMS — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Assign Cookies (LeetCode 455)
 *   E2. Lemonade Change (LeetCode 860)
 *   E3. Can Place Flowers (LeetCode 605)
 *   E4. Best Time to Buy and Sell Stock II (LeetCode 122)
 *   E5. Max Array Sum After K Negations (LeetCode 1005)
 *   E6. Maximum Units on a Truck (LeetCode 1710)
 *   E7. Minimum Operations to Make Array Increasing (LeetCode 1827)
 *   E8. Majority Element (LeetCode 169 / InterviewBit)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Easy {

    // =========================================================
    // E1. ASSIGN COOKIES
    // Pattern: Two-Pointer Greedy
    // LeetCode: 455
    // =========================================================
    /**
     * Problem: Given greed factor array g and cookie size array s, assign
     * cookies to children to maximize the number of content children.
     *
     * Brute Force O(n!): Try all permutations of cookie assignments.
     *
     * Optimal O(n log n + m log m): Sort both arrays. Match smallest sufficient
     * cookie to child with smallest greed.
     *
     * Why does this work?
     * Giving a larger cookie than required to a low-greed child wastes capacity.
     * Smallest cookie satisfying smallest greed preserves larger cookies for higher greed.
     *
     * Time: O(n log n + m log m)
     * Space: O(1) or O(log n) sorting space
     */
    public int findContentChildren(int[] g, int[] s) {
        Arrays.sort(g);
        Arrays.sort(s);
        int i = 0, j = 0;
        while (i < g.length && j < s.length) {
            if (s[j] >= g[i]) {
                i++; // Child is content
            }
            j++; // Move to next cookie regardless
        }
        return i;
    }

    /**
     * Follow-up 1: What if cookies can be combined?
     * → Dynamic Programming / Knapsack variant (NP-hard).
     */

    // =========================================================
    // E2. LEMONADE CHANGE
    // Pattern: Local Greedy Choice (Prioritize larger change)
    // LeetCode: 860
    // =========================================================
    /**
     * Problem: Lemonade costs $5. Customers pay with $5, $10, or $20. Return true if
     * you can provide correct change to every customer starting with 0 bills.
     *
     * Optimal O(n): Track $5 and $10 bill counts.
     * - $5: No change needed.
     * - $10: Requires one $5 bill as change.
     * - $20: Requires $15 change -> Prefer $10 + $5 over three $5 bills.
     *
     * Why does this work?
     * $5 bills are more flexible because they can serve as change for both $10 and $20.
     * $10 bills can only serve as change for $20. Greedily preserving $5 bills is optimal.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public boolean lemonadeChange(int[] bills) {
        int five = 0, ten = 0;
        for (int bill : bills) {
            if (bill == 5) {
                five++;
            } else if (bill == 10) {
                if (five == 0) return false;
                five--;
                ten++;
            } else { // bill == 20
                if (ten > 0 && five > 0) {
                    ten--;
                    five--;
                } else if (five >= 3) {
                    five -= 3;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    // =========================================================
    // E3. CAN PLACE FLOWERS
    // Pattern: Single Pass Greedy Placement
    // LeetCode: 605
    // =========================================================
    /**
     * Problem: Given flowerbed array of 0s and 1s and n flowers, return true if n
     * flowers can be planted without violating no-adjacent rule.
     *
     * Optimal O(n): Single pass. Plant flower at index i if i-1, i, i+1 are all 0.
     *
     * Time: O(n)
     * Space: O(1)
     */
    public boolean canPlaceFlowers(int[] flowerbed, int n) {
        int count = 0;
        for (int i = 0; i < flowerbed.length; i++) {
            if (flowerbed[i] == 0) {
                boolean prevEmpty = (i == 0 || flowerbed[i - 1] == 0);
                boolean nextEmpty = (i == flowerbed.length - 1 || flowerbed[i + 1] == 0);
                if (prevEmpty && nextEmpty) {
                    flowerbed[i] = 1;
                    count++;
                    if (count >= n) return true;
                }
            }
        }
        return count >= n;
    }

    // =========================================================
    // E4. BEST TIME TO BUY AND SELL STOCK II
    // Pattern: Local Differences / Accumulation Greedy
    // LeetCode: 122
    // =========================================================
    /**
     * Problem: Given stock prices daily, buy and sell as many times as desired. Max profit.
     *
     * Optimal O(n): Sum up every positive price difference (prices[i] - prices[i-1]).
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int maxProfit(int[] prices) {
        int maxProfit = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i - 1]) {
                maxProfit += prices[i] - prices[i - 1];
            }
        }
        return maxProfit;
    }

    // =========================================================
    // E5. MAX ARRAY SUM AFTER K NEGATIONS
    // Pattern: Sorting & Smallest Element Greedy
    // LeetCode: 1005
    // =========================================================
    /**
     * Problem: Modify array sum by negating elements exactly K times. Maximize sum.
     *
     * Optimal O(n log n): Sort. Negate negative numbers from smallest to largest.
     * If K remains and K is odd, flip smallest absolute element.
     *
     * Time: O(n log n)
     * Space: O(1)
     */
    public int largestSumAfterKNegations(int[] nums, int k) {
        Arrays.sort(nums);
        for (int i = 0; i < nums.length && k > 0; i++) {
            if (nums[i] < 0) {
                nums[i] = -nums[i];
                k--;
            }
        }
        int sum = 0, minVal = Integer.MAX_VALUE;
        for (int val : nums) {
            sum += val;
            minVal = Math.min(minVal, val);
        }
        if (k % 2 == 1) {
            sum -= 2 * minVal;
        }
        return sum;
    }

    // =========================================================
    // E6. MAXIMUM UNITS ON A TRUCK
    // Pattern: Fractional Knapsack / Ratio Greedy
    // LeetCode: 1710
    // =========================================================
    /**
     * Problem: Given boxTypes [numberOfBoxes, unitsPerBox] and truckSize, maximize units.
     *
     * Optimal O(n log n): Sort boxTypes descending by unitsPerBox. Greedily take max boxes.
     *
     * Time: O(n log n)
     * Space: O(1)
     */
    public int maximumUnits(int[][] boxTypes, int truckSize) {
        Arrays.sort(boxTypes, (a, b) -> Integer.compare(b[1], a[1]));
        int totalUnits = 0;
        for (int[] box : boxTypes) {
            int count = Math.min(truckSize, box[0]);
            totalUnits += count * box[1];
            truckSize -= count;
            if (truckSize == 0) break;
        }
        return totalUnits;
    }

    // =========================================================
    // E7. MINIMUM OPERATIONS TO MAKE ARRAY INCREASING
    // Pattern: Sequential Pass Greedy Adjustment
    // LeetCode: 1827
    // =========================================================
    /**
     * Problem: Make nums strictly increasing by incrementing elements. Return min operations.
     *
     * Optimal O(n): Single pass. Ensure nums[i] = max(nums[i], nums[i-1] + 1).
     *
     * Time: O(n)
     * Space: O(1)
     */
    public int minOperations(int[] nums) {
        int ops = 0;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] <= nums[i - 1]) {
                int target = nums[i - 1] + 1;
                ops += target - nums[i];
                nums[i] = target;
            }
        }
        return ops;
    }

    // =========================================================
    // E8. MAJORITY ELEMENT
    // Pattern: Boyer-Moore Voting Algorithm
    // LeetCode: 169 / InterviewBit
    // =========================================================
    /**
     * Problem: Find element appearing > floor(N/2) times.
     *
     * Optimal O(n) Time, O(1) Space: Boyer-Moore Candidate Elimination.
     */
    public int majorityElement(int[] nums) {
        int candidate = nums[0], count = 0;
        for (int num : nums) {
            if (count == 0) {
                candidate = num;
                count = 1;
            } else if (num == candidate) {
                count++;
            } else {
                count--;
            }
        }
        return candidate;
    }
}
