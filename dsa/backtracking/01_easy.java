/**
 * ============================================================
 *  RECURSION & BACKTRACKING — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Subsets / Power Set (LeetCode 78)
 *   E2. Permutations (LeetCode 46)
 *   E3. Combinations (LeetCode 77)
 *   E4. Generate Parentheses (LeetCode 22)
 *   E5. Letter Combinations of a Phone Number (LeetCode 17)
 *   E6. Binary Tree Path Sum I (LeetCode 112)
 *   E7. Combination Sum I (LeetCode 39)
 *   E8. Sum of All Subset XOR Totals (LeetCode 1863)
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
    // E1. SUBSETS / POWER SET
    // Pattern: Inclusion / Exclusion Backtracking
    // LeetCode: 78
    // =========================================================
    /**
     * Problem: Return all possible subsets (the power set) of unique elements nums.
     *
     * Optimal O(2^N): Backtracking DFS. At index i, either include or exclude nums[i].
     *
     * Time: O(N · 2^N)
     * Space: O(N) recursion stack
     */
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrackSubsets(0, nums, new ArrayList<>(), result);
        return result;
    }

    private void backtrackSubsets(int start, int[] nums, List<Integer> current, List<List<Integer>> result) {
        result.add(new ArrayList<>(current)); // Save snapshot
        for (int i = start; i < nums.length; i++) {
            current.add(nums[i]);                  // Choose
            backtrackSubsets(i + 1, nums, current, result); // Explore
            current.remove(current.size() - 1);    // Un-choose
        }
    }

    // =========================================================
    // E2. PERMUTATIONS
    // Pattern: Visited Array Backtracking
    // LeetCode: 46
    // =========================================================
    /**
     * Problem: Return all possible permutations of array nums of distinct integers.
     *
     * Optimal O(N!): Track visited elements using boolean array.
     *
     * Time: O(N · N!)
     * Space: O(N)
     */
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrackPermute(nums, new boolean[nums.length], new ArrayList<>(), result);
        return result;
    }

    private void backtrackPermute(int[] nums, boolean[] used, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true;
            current.add(nums[i]);
            backtrackPermute(nums, used, current, result);
            current.remove(current.size() - 1);
            used[i] = false;
        }
    }

    // =========================================================
    // E3. COMBINATIONS
    // Pattern: Fixed Length K Backtracking
    // LeetCode: 77
    // =========================================================
    /**
     * Problem: Return all combinations of k numbers chosen from range [1, n].
     *
     * Optimal O(C(n, k)): Backtrack from start to n. Prune if remaining items < required.
     *
     * Time: O(k · C(n, k))
     * Space: O(k)
     */
    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> result = new ArrayList<>();
        backtrackCombine(1, n, k, new ArrayList<>(), result);
        return result;
    }

    private void backtrackCombine(int start, int n, int k, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        // Pruning: need (k - current.size()) more elements
        int remaining = k - current.size();
        for (int i = start; i <= n - remaining + 1; i++) {
            current.add(i);
            backtrackCombine(i + 1, n, k, current, result);
            current.remove(current.size() - 1);
        }
    }

    // =========================================================
    // E4. GENERATE PARENTHESES
    // Pattern: Valid Prefix State Backtracking
    // LeetCode: 22
    // =========================================================
    /**
     * Problem: Given n pairs of parentheses, generate all combinations of well-formed parentheses.
     *
     * Optimal O(4^n / √n): Catalan Number combinations.
     * Only add '(' if open < n. Only add ')' if close < open.
     *
     * Time: O(C_n) where C_n is n-th Catalan number
     * Space: O(n)
     */
    public List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        backtrackParenthesis(0, 0, n, new StringBuilder(), result);
        return result;
    }

    private void backtrackParenthesis(int open, int close, int max, StringBuilder sb, List<String> result) {
        if (sb.length() == max * 2) {
            result.add(sb.toString());
            return;
        }

        if (open < max) {
            sb.append('(');
            backtrackParenthesis(open + 1, close, max, sb, result);
            sb.deleteCharAt(sb.length() - 1);
        }
        if (close < open) {
            sb.append(')');
            backtrackParenthesis(open, close + 1, max, sb, result);
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    // =========================================================
    // E5. LETTER COMBINATIONS OF A PHONE NUMBER
    // Pattern: Combination Mapping Backtracking
    // LeetCode: 17
    // =========================================================
    /**
     * Problem: Return all possible letter combinations for phone digits string.
     *
     * Optimal O(4^N): Branch per digit letter choices.
     *
     * Time: O(4^N)
     * Space: O(N)
     */
    private static final String[] MAP = {
        "", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"
    };

    public List<String> letterCombinations(String digits) {
        List<String> result = new ArrayList<>();
        if (digits == null || digits.isEmpty()) return result;
        backtrackPhone(0, digits, new StringBuilder(), result);
        return result;
    }

    private void backtrackPhone(int index, String digits, StringBuilder sb, List<String> result) {
        if (index == digits.length()) {
            result.add(sb.toString());
            return;
        }

        String letters = MAP[digits.charAt(index) - '0'];
        for (char ch : letters.toCharArray()) {
            sb.append(ch);
            backtrackPhone(index + 1, digits, sb, result);
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    // =========================================================
    // E6. BINARY TREE PATH SUM I
    // Pattern: Tree Root-to-Leaf Path Backtracking
    // LeetCode: 112
    // =========================================================
    public static class TreeNode {
        public int val;
        public TreeNode left;
        public TreeNode right;
        public TreeNode(int val) { this.val = val; }
    }

    public boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) return false;
        if (root.left == null && root.right == null) {
            return targetSum == root.val;
        }
        return hasPathSum(root.left, targetSum - root.val) ||
               hasPathSum(root.right, targetSum - root.val);
    }

    // =========================================================
    // E7. COMBINATION SUM I
    // Pattern: Unlimited Reuse Backtracking
    // LeetCode: 39
    // =========================================================
    /**
     * Problem: Find all unique combinations of candidates that sum to target.
     * Elements can be chosen unlimited times.
     *
     * Optimal O(2^T): Pass current index `i` (instead of `i + 1`) to allow reuse.
     *
     * Time: O(N^(T/M)) where T is target and M is min candidate
     * Space: O(T/M)
     */
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(candidates);
        backtrackCombSum(0, candidates, target, new ArrayList<>(), result);
        return result;
    }

    private void backtrackCombSum(int start, int[] candidates, int target, List<Integer> current, List<List<Integer>> result) {
        if (target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            if (candidates[i] > target) break; // Prune
            current.add(candidates[i]);
            backtrackCombSum(i, candidates, target - candidates[i], current, result); // Recurse with i (unlimited reuse)
            current.remove(current.size() - 1);
        }
    }

    // =========================================================
    // E8. SUM OF ALL SUBSET XOR TOTALS
    // Pattern: Bitwise Subset XOR Accumulation
    // LeetCode: 1863
    // =========================================================
    /**
     * Problem: Return the sum of all subset XOR totals for array nums.
     *
     * Optimal O(2^N): Sum XOR values recursively.
     *
     * Time: O(2^N)
     * Space: O(N)
     */
    public int subsetXORSum(int[] nums) {
        return dfsXOR(0, 0, nums);
    }

    private int dfsXOR(int index, int currentXOR, int[] nums) {
        if (index == nums.length) return currentXOR;
        int include = dfsXOR(index + 1, currentXOR ^ nums[index], nums);
        int exclude = dfsXOR(index + 1, currentXOR, nums);
        return include + exclude;
    }
}
