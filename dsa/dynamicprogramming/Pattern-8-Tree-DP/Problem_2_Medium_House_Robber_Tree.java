/**
 * ============================================================
 *  PATTERN 8 — TREE DP
 *  Problem 2 (Medium): House Robber III   LC 337
 * ============================================================
 *
 *  Difficulty  : Medium
 *  Pattern     : Tree DP — dp[node][0/1] (two states per node)
 *  LeetCode    : https://leetcode.com/problems/house-robber-iii/
 *
 *  PROBLEM STATEMENT:
 *    Houses are arranged in a binary tree. A robber cannot rob two directly
 *    connected houses (parent and child). Find the maximum amount that can be robbed.
 *
 *  EXAMPLES:
 *    Tree:   3             → 7 (rob 3, 3, 1 = 7)
 *           / \
 *          2   3
 *           \   \
 *            3   1
 *
 *    Tree:   3             → 9 (rob 4, 5 = 9)
 *           / \
 *          4   5
 *         / \   \
 *        1   3   1
 *
 *  KEY INSIGHT:
 *    At each node, we have exactly TWO choices:
 *      1. ROB this node → cannot rob any direct child.
 *      2. SKIP this node → children can be robbed OR not (take the best).
 *
 * ============================================================
 */
import java.util.*;

class Problem2_HouseRobberTree {

    // =========================================================
    // APPROACH 1 — TREE DP: dp[node][0/1] (optimal, O(n))
    // =========================================================
    /**
     * STATE:
     *   For each subtree rooted at node u, compute:
     *     dp[0] = max loot from u's subtree when u is NOT robbed
     *     dp[1] = max loot from u's subtree when u IS robbed
     *
     * TRANSITION:
     *   Let left  = dfs(u.left)  → [left_skip, left_rob]
     *   Let right = dfs(u.right) → [right_skip, right_rob]
     *
     *   NOT ROB u:
     *     Children can be either robbed or not → take the max for each:
     *     dp[0] = max(left[0], left[1]) + max(right[0], right[1])
     *
     *   ROB u:
     *     Children CANNOT be robbed:
     *     dp[1] = u.val + left[0] + right[0]
     *
     * BASE CASE:
     *   dfs(null) = [0, 0]  (no node contributes nothing either way)
     *
     * VISUAL for Tree [3,2,3,null,3,null,1]:
     *       3
     *      / \
     *     2   3
     *      \   \
     *       3   1
     *
     *   dfs(3-leaf)=[0,3], dfs(1-leaf)=[0,1]
     *   dfs(2): left=[0,0], right=[0,3]
     *     dp[0] = max(0,0)+max(0,3) = 0+3 = 3
     *     dp[1] = 2 + 0 + 0 = 2
     *     → [3, 2]
     *   dfs(3-right): left=[0,0], right=[0,1]
     *     dp[0] = max(0,0)+max(0,1) = 0+1 = 1
     *     dp[1] = 3 + 0 + 0 = 3
     *     → [1, 3]
     *   dfs(3-root): left=[3,2], right=[1,3]
     *     dp[0] = max(3,2)+max(1,3) = 3+3 = 6
     *     dp[1] = 3 + 3 + 1 = 7
     *     → [6, 7]
     *   Answer: max(6, 7) = 7 ✓
     *
     * Time:  O(n) — each node visited once
     * Space: O(h) — recursion stack (h = tree height)
     *
     * Follow-up: House Robber on a General Tree (not binary)?
     *   Same DP, iterate over all children (not just left/right).
     * Follow-up: House Robber on a Circular Array (LC 213)?
     *   Run linear DP twice: once excluding first element, once excluding last.
     */
    public int rob(TreeNode root) {
        int[] result = dfs(root);
        return Math.max(result[0], result[1]);
    }

    /**
     * @return int[] {notRobbed, robbed} = max loot when root is {not robbed, robbed}
     */
    private int[] dfs(TreeNode node) {
        if (node == null) return new int[]{0, 0};

        int[] left  = dfs(node.left);
        int[] right = dfs(node.right);

        // Not robbing this node: children can be optimally robbed or not
        int notRob = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);

        // Robbing this node: children MUST be not robbed
        int doRob = node.val + left[0] + right[0];

        return new int[]{notRob, doRob};
    }

    // =========================================================
    // APPROACH 2 — NAIVE RECURSION WITH MEMOIZATION (less elegant)
    // =========================================================
    /**
     * Naive idea: at each node, try rob / skip. But this leads to O(2^n) overlap.
     * Fix: memoize by node using a HashMap.
     *
     * This is less clean than Approach 1 but demonstrates the memoization approach.
     */
    private Map<TreeNode, Integer> memo = new HashMap<>();

    public int robMemo(TreeNode root) {
        if (root == null) return 0;
        if (memo.containsKey(root)) return memo.get(root);

        // Option 1: rob this node
        int doRob = root.val;
        if (root.left  != null) doRob += robMemo(root.left.left)  + robMemo(root.left.right);
        if (root.right != null) doRob += robMemo(root.right.left) + robMemo(root.right.right);

        // Option 2: skip this node (children are free to be robbed or not)
        int skip = robMemo(root.left) + robMemo(root.right);

        int best = Math.max(doRob, skip);
        memo.put(root, best);
        return best;
    }

    // =========================================================
    // APPROACH 3 — BRUTE FORCE (exponential, for validation)
    // =========================================================
    /**
     * Try both choices at each node without memoization.
     * Time: O(2^n) — exponential overlap due to re-solving subtrees.
     */
    public int robBrute(TreeNode root) {
        if (root == null) return 0;

        // Rob this node + grandchildren
        int doRob = root.val;
        if (root.left != null) {
            doRob += robBrute(root.left.left) + robBrute(root.left.right);
        }
        if (root.right != null) {
            doRob += robBrute(root.right.left) + robBrute(root.right.right);
        }

        // Skip this node
        int skip = robBrute(root.left) + robBrute(root.right);

        return Math.max(doRob, skip);
    }

    // =========================================================
    // EXTENSION: House Robber on General Tree (N-ary)
    // =========================================================
    /**
     * Each node has a list of children. Same DP logic extended.
     * Returns [notRobbed, robbed] for the general tree rooted at u.
     */
    public int robGeneralTree(int[] vals, List<List<Integer>> children, int root) {
        int[] result = dfsGeneral(root, vals, children);
        return Math.max(result[0], result[1]);
    }

    private int[] dfsGeneral(int u, int[] vals, List<List<Integer>> children) {
        int notRob = 0;
        int doRob  = vals[u];

        for (int child : children.get(u)) {
            int[] childRes = dfsGeneral(child, vals, children);
            notRob += Math.max(childRes[0], childRes[1]);  // child can be robbed or not
            doRob  += childRes[0];                         // child must NOT be robbed
        }

        return new int[]{notRob, doRob};
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem2_HouseRobberTree sol = new Problem2_HouseRobberTree();

        System.out.println("=== LC 337: House Robber III ===");

        // Tree: [3,2,3,null,3,null,1]  → 7
        TreeNode root1 = new TreeNode(3,
            new TreeNode(2, null, new TreeNode(3)),
            new TreeNode(3, null, new TreeNode(1)));
        System.out.println("Tree [3,2,3,_,3,_,1]:");
        System.out.println("  Tree DP   : " + sol.rob(root1));        // 7
        System.out.println("  Memo      : " + sol.robMemo(root1));     // 7
        System.out.println("  Brute     : " + sol.robBrute(root1));    // 7

        // Tree: [3,4,5,1,3,null,1]  → 9
        TreeNode root2 = new TreeNode(3,
            new TreeNode(4, new TreeNode(1), new TreeNode(3)),
            new TreeNode(5, null, new TreeNode(1)));
        System.out.println("\nTree [3,4,5,1,3,_,1]:");
        System.out.println("  Tree DP   : " + sol.rob(root2));        // 9
        System.out.println("  Memo      : " + sol.robMemo(root2));     // 9
        System.out.println("  Brute     : " + sol.robBrute(root2));    // 9

        // Single node
        TreeNode root3 = new TreeNode(10);
        System.out.println("\nSingle node [10]: " + sol.rob(root3));   // 10

        // General N-ary tree
        System.out.println("\n=== General N-ary Tree House Robber ===");
        // Tree structure: 0→{1,2,3}, 1→{4,5}, all others are leaves
        // vals: [1, 2, 3, 4, 5, 6]
        int[] vals = {1, 2, 3, 4, 5, 6};
        List<List<Integer>> children = new ArrayList<>();
        for (int i = 0; i < 6; i++) children.add(new ArrayList<>());
        children.get(0).addAll(List.of(1, 2, 3));
        children.get(1).addAll(List.of(4, 5));
        System.out.println("N-ary tree rob: " + sol.robGeneralTree(vals, children, 0));
        // Not-rob 0: max(rob/skip 1) + max(rob/skip 2) + max(rob/skip 3)
        // Rob 1 (=2+5+6=13) or skip1 (=0)?... let's trace:
        // dfs(4)=[0,5], dfs(5)=[0,6]
        // dfs(1): not=max(0,5)+max(0,6)=11, rob=2+0+0=2 → [11,2]
        // dfs(2): [0,3], dfs(3): [0,4]
        // dfs(0): not=max(11,2)+max(0,3)+max(0,4)=11+3+4=18, rob=1+0+0+0=1 → max=18
    }
}
