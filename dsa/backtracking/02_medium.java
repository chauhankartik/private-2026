/**
 * ============================================================
 *  RECURSION & BACKTRACKING — MEDIUM PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   M1. Subsets II (With Duplicates) (LeetCode 90)
 *   M2. Permutations II (With Duplicates) (LeetCode 47)
 *   M3. Combination Sum II (With Duplicates) (LeetCode 40)
 *   M4. Combination Sum III (LeetCode 216)
 *   M5. Palindrome Partitioning (LeetCode 131)
 *   M6. Word Search I - Grid Backtracking (LeetCode 79)
 *   M7. Restore IP Addresses (LeetCode 93)
 *   M8. Partition to K Equal Sum Subsets (LeetCode 698)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Medium {

    // =========================================================
    // M1. SUBSETS II (WITH DUPLICATES)
    // Pattern: Duplicate Pruning (`i > start && nums[i] == nums[i-1]`)
    // LeetCode: 90
    // =========================================================
    /**
     * Problem: Return all possible subsets of nums that may contain duplicates.
     *
     * Optimal O(2^N): Sort nums first. Skip duplicates at same depth (`i > start && nums[i] == nums[i-1]`).
     *
     * Time: O(N · 2^N)
     * Space: O(N)
     */
    public List<List<Integer>> subsetsWithDup(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        backtrackSubsetsWithDup(0, nums, new ArrayList<>(), result);
        return result;
    }

    private void backtrackSubsetsWithDup(int start, int[] nums, List<Integer> current, List<List<Integer>> result) {
        result.add(new ArrayList<>(current));
        for (int i = start; i < nums.length; i++) {
            if (i > start && nums[i] == nums[i - 1]) continue; // Skip sister branch duplicates
            current.add(nums[i]);
            backtrackSubsetsWithDup(i + 1, nums, current, result);
            current.remove(current.size() - 1);
        }
    }

    // =========================================================
    // M2. PERMUTATIONS II (WITH DUPLICATES)
    // Pattern: Used Array + Duplicate Pruning
    // LeetCode: 47
    // =========================================================
    /**
     * Problem: Return all unique permutations of nums containing duplicates.
     *
     * Optimal O(N!): Sort nums. Skip duplicate if `!used[i-1] && nums[i] == nums[i-1]`.
     *
     * Time: O(N · N!)
     * Space: O(N)
     */
    public List<List<Integer>> permuteUnique(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        backtrackPermuteUnique(nums, new boolean[nums.length], new ArrayList<>(), result);
        return result;
    }

    private void backtrackPermuteUnique(int[] nums, boolean[] used, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            // Key duplicate skip rule: if previous duplicate was NOT used, skip current to prevent duplicate tree branch
            if (i > 0 && nums[i] == nums[i - 1] && !used[i - 1]) continue;

            used[i] = true;
            current.add(nums[i]);
            backtrackPermuteUnique(nums, used, current, result);
            current.remove(current.size() - 1);
            used[i] = false;
        }
    }

    // =========================================================
    // M3. COMBINATION SUM II (WITH DUPLICATES)
    // Pattern: Single Use + Duplicate Pruning
    // LeetCode: 40
    // =========================================================
    /**
     * Problem: Each number in candidates may only be used ONCE in combination. Candidates contain duplicates.
     *
     * Optimal O(2^N): Sort candidates. Pass `i + 1` and skip duplicates at same level.
     *
     * Time: O(2^N)
     * Space: O(N)
     */
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(candidates);
        backtrackCombSum2(0, candidates, target, new ArrayList<>(), result);
        return result;
    }

    private void backtrackCombSum2(int start, int[] candidates, int target, List<Integer> current, List<List<Integer>> result) {
        if (target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            if (candidates[i] > target) break; // Prune
            if (i > start && candidates[i] == candidates[i - 1]) continue; // Duplicate prune

            current.add(candidates[i]);
            backtrackCombSum2(i + 1, candidates, target - candidates[i], current, result);
            current.remove(current.size() - 1);
        }
    }

    // =========================================================
    // M4. COMBINATION SUM III
    // Pattern: Fixed Length K + Single Use Range [1, 9]
    // LeetCode: 216
    // =========================================================
    /**
     * Problem: Find combinations of k numbers that sum to n using numbers 1 through 9. Each number used at most once.
     *
     * Optimal O(C(9, k)): Range 1 to 9 backtracking with bounds.
     *
     * Time: O(C(9, k))
     * Space: O(k)
     */
    public List<List<Integer>> combinationSum3(int k, int n) {
        List<List<Integer>> result = new ArrayList<>();
        backtrackCombSum3(1, k, n, new ArrayList<>(), result);
        return result;
    }

    private void backtrackCombSum3(int start, int k, int target, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == k) {
            if (target == 0) result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i <= 9; i++) {
            if (i > target) break; // Prune
            current.add(i);
            backtrackCombSum3(i + 1, k, target - i, current, result);
            current.remove(current.size() - 1);
        }
    }

    // =========================================================
    // M5. PALINDROME PARTITIONING
    // Pattern: String Partitioning Backtracking
    // LeetCode: 131
    // =========================================================
    /**
     * Problem: Partition string s such that every substring of the partition is a palindrome.
     *
     * Optimal O(N · 2^N): For index start, try all valid palindrome substrings s[start..i].
     *
     * Time: O(N · 2^N)
     * Space: O(N)
     */
    public List<List<String>> partition(String s) {
        List<List<String>> result = new ArrayList<>();
        backtrackPartition(0, s, new ArrayList<>(), result);
        return result;
    }

    private void backtrackPartition(int start, String s, List<String> current, List<List<String>> result) {
        if (start == s.length()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < s.length(); i++) {
            if (isPalindrome(s, start, i)) {
                current.add(s.substring(start, i + 1));
                backtrackPartition(i + 1, s, current, result);
                current.remove(current.size() - 1);
            }
        }
    }

    private boolean isPalindrome(String s, int left, int right) {
        while (left < right) {
            if (s.charAt(left++) != s.charAt(right--)) return false;
        }
        return true;
    }

    // =========================================================
    // M6. WORD SEARCH I
    // Pattern: Grid Backtracking DFS with Cell In-Place Marking
    // LeetCode: 79
    // =========================================================
    /**
     * Problem: Given m x n grid and word, return true if word exists in grid.
     *
     * Optimal O(M · N · 3^L): DFS from every matching cell. Mark cell '#' temporarily.
     *
     * Time: O(M · N · 3^L) where L is word length
     * Space: O(L) recursion stack
     */
    public boolean exist(char[][] board, String word) {
        int m = board.length, n = board[0].length;
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == word.charAt(0)) {
                    if (dfsWordSearch(board, r, c, word, 0)) return true;
                }
            }
        }
        return false;
    }

    private boolean dfsWordSearch(char[][] board, int r, int c, String word, int index) {
        if (index == word.length()) return true;
        if (r < 0 || r >= board.length || c < 0 || c >= board[0].length || board[r][c] != word.charAt(index)) {
            return false;
        }

        char temp = board[r][c];
        board[r][c] = '#'; // Mark cell visited in-place

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            if (dfsWordSearch(board, r + dr[i], c + dc[i], word, index + 1)) {
                board[r][c] = temp; // Un-mark before return
                return true;
            }
        }

        board[r][c] = temp; // Un-choose / Backtrack
        return false;
    }

    // =========================================================
    // M7. RESTORE IP ADDRESSES
    // Pattern: String Segment 4-Part Partitioning
    // LeetCode: 93
    // =========================================================
    /**
     * Problem: Return all valid IPv4 addresses that can be formed from string s.
     *
     * Optimal O(3^4) = O(1): Try segment lengths 1, 2, 3. Validate numeric value <= 255 and no leading zeros.
     *
     * Time: O(1) — string length bounded by 12
     * Space: O(1)
     */
    public List<String> restoreIpAddresses(String s) {
        List<String> result = new ArrayList<>();
        if (s.length() < 4 || s.length() > 12) return result;
        backtrackIP(0, 0, s, new StringBuilder(), result);
        return result;
    }

    private void backtrackIP(int index, int dots, String s, StringBuilder sb, List<String> result) {
        if (dots == 4) {
            if (index == s.length()) {
                result.add(sb.substring(0, sb.length() - 1)); // Remove trailing dot
            }
            return;
        }

        int len = sb.length();
        for (int i = index; i < Math.min(index + 3, s.length()); i++) {
            String segment = s.substring(index, i + 1);
            if (isValidSegment(segment)) {
                sb.append(segment).append('.');
                backtrackIP(i + 1, dots + 1, s, sb, result);
                sb.setLength(len); // Reset StringBuilder back to original state
            }
        }
    }

    private boolean isValidSegment(String seg) {
        if (seg.length() > 1 && seg.charAt(0) == '0') return false; // No leading zeros
        int val = Integer.parseInt(seg);
        return val >= 0 && val <= 255;
    }

    // =========================================================
    // M8. PARTITION TO K EQUAL SUM SUBSETS
    // Pattern: Bucket Partitioning + Descending Sort Pruning
    // LeetCode: 698
    // =========================================================
    /**
     * Problem: Partition nums into k non-empty subsets with equal sums.
     *
     * Optimal O(k · 2^N):
     * 1. Total sum must be divisible by k. Target bucket sum = total / k.
     * 2. Sort nums descending (prunes invalid branches early).
     * 3. Fill buckets one by one.
     *
     * Time: O(k · 2^N)
     * Space: O(N)
     */
    public boolean canPartitionKSubsets(int[] nums, int k) {
        int sum = 0;
        for (int n : nums) sum += n;
        if (sum % k != 0) return false;
        int target = sum / k;

        Arrays.sort(nums);
        int n = nums.length;
        // Reverse array to sort descending
        for (int i = 0; i < n / 2; i++) {
            int tmp = nums[i];
            nums[i] = nums[n - 1 - i];
            nums[n - 1 - i] = tmp;
        }

        if (nums[0] > target) return false;

        int[] buckets = new int[k];
        return backtrackBuckets(0, nums, target, buckets);
    }

    private boolean backtrackBuckets(int index, int[] nums, int target, int[] buckets) {
        if (index == nums.length) return true;

        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] + nums[index] <= target) {
                buckets[i] += nums[index];
                if (backtrackBuckets(index + 1, nums, target, buckets)) return true;
                buckets[i] -= nums[index];
            }
            // Prune duplicate empty buckets to eliminate identical permutations
            if (buckets[i] == 0) break;
        }
        return false;
    }
}
