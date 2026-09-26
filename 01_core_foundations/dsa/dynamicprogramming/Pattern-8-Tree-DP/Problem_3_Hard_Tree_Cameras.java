/**
 * ============================================================
 *  PATTERN 8 — TREE DP
 *  Problem 3 (Hard): Binary Tree Cameras   LC 968
 * ============================================================
 *
 *  Difficulty  : Hard
 *  Pattern     : Tree DP — 3-state machine on tree (post-order greedy DP)
 *  LeetCode    : https://leetcode.com/problems/binary-tree-cameras/
 *
 *  PROBLEM STATEMENT:
 *    Place cameras on some nodes of a binary tree. A camera at node u
 *    monitors u, its parent, and its children.
 *    Return the minimum number of cameras needed to monitor ALL nodes.
 *
 *  EXAMPLES:
 *    Tree:   0         → 1 camera (at node 1)
 *           /
 *          0
 *         /
 *        0
 *
 *    Tree:   0         → 2 cameras
 *           / \
 *          0   0
 *         / \
 *        0   0
 *
 *  KEY INSIGHT — 3 STATES:
 *    State 0: Node is COVERED by a child's camera, has NO camera itself.
 *    State 1: Node HAS a camera.
 *    State 2: Node is NOT COVERED (needs parent to place a camera).
 *
 *  GREEDY STRATEGY:
 *    Place cameras as HIGH up as possible (delay camera placement to maximize coverage).
 *    Only place a camera at u when a child is in state 2 (uncovered).
 *
 * ============================================================
 */
import java.util.*;

class Problem3_TreeCameras {

    // =========================================================
    // APPROACH 1 — GREEDY + 3-STATE TREE DP (Optimal)
    // =========================================================
    /**
     * State encoding:
     *   0 = COVERED (no camera here, covered by a child's camera)
     *   1 = HAS CAMERA (this node has a camera)
     *   2 = NOT COVERED (no camera here, not covered; needs parent camera)
     *
     * Post-order DFS rules (determine state of u based on children's states):
     *
     *   Rule 1: If ANY child is in state 2 (not covered):
     *     → u MUST place a camera! → u = state 1, cameras++
     *     (We greedily push the camera as high as possible, but if a child is
     *      uncovered, we MUST place now because u's parent may not be there.)
     *
     *   Rule 2: If no child needs covering, but ANY child HAS a camera (state 1):
     *     → u is covered by that child's camera → u = state 0
     *
     *   Rule 3: If all children are in state 0 (covered, but no camera):
     *     → u is NOT covered → u = state 2
     *     (u will ask its parent to place a camera. If u is the root and in
     *      state 2 after DFS, we must place one more camera at the root.)
     *
     *   Base: null node returns state 0 (covered but no camera).
     *     (A null node doesn't need coverage; it "pretends" to be covered
     *      so that a leaf doesn't get a camera prematurely.)
     *
     * WHY NULL IS STATE 0 (COVERED):
     *   If we returned state 2 for null, leaf nodes would always place cameras.
     *   That's wasteful — we want cameras at parents of leaves, not leaves themselves.
     *   By returning "covered" (0) for null, leaves return state 2 (uncovered),
     *   asking THEIR parents to install cameras. This minimizes total cameras.
     *
     * VISUAL for [0, 0, 0, 0, 0]:
     *       0
     *      / \
     *     0   0
     *    / \
     *   0   0
     *
     *   dfs(0-leaf) = 2 (not covered, ask parent)
     *   dfs(0-leaf) = 2
     *   dfs(0-left): children both state 2 → place camera! → state 1, cameras=1
     *   dfs(0-right) = 2 (leaf → not covered)
     *   dfs(0-root): left=1 (has camera, so root is covered→0), right=2 (uncovered!)
     *     → right child is in state 2 → MUST place camera at root → state 1, cameras=2
     *   Root is state 1, so it's self-covered.
     *   → Total cameras = 2 ✓
     *
     * VISUAL for [0, 0, 0]:
     *       0
     *      /
     *     0
     *    /
     *   0
     *
     *   dfs(0-leaf-leaf) = 2 (not covered)
     *   dfs(0-middle): child is state 2 → place camera! → state 1, cameras=1
     *   dfs(0-root): child is state 1 → root is covered → state 0
     *   Root is state 0 (covered by child), not state 2, so no extra camera needed.
     *   → Total cameras = 1 ✓
     *
     * Time:  O(n) — each node visited once
     * Space: O(h) — recursion stack
     *
     * Follow-up: What if each camera can only monitor 1 hop vs 2 hops?
     *   Adjust the state machine accordingly (more states).
     * Follow-up: Minimum Dominating Set on a Tree?
     *   This is equivalent! "Dominating set" = each node is either in the set
     *   or adjacent to a node in the set. This DP solves it.
     */
    private int cameras;

    public int minCameraCover(TreeNode root) {
        cameras = 0;
        // If root itself is not covered after DFS, place one camera at root
        if (dfsCamera(root) == 2) cameras++;
        return cameras;
    }

    /**
     * @return state of this node:
     *   0 = covered (by a child camera), no camera here
     *   1 = has camera
     *   2 = not covered (no camera, no child camera)
     */
    private int dfsCamera(TreeNode node) {
        // Null = pretend covered (so leaves return "not covered", their parents install)
        if (node == null) return 0;

        int left  = dfsCamera(node.left);
        int right = dfsCamera(node.right);

        // Rule 1: Any child is not covered → MUST place camera here
        if (left == 2 || right == 2) {
            cameras++;
            return 1;  // has camera
        }

        // Rule 2: Any child HAS a camera → this node is covered
        if (left == 1 || right == 1) {
            return 0;  // covered, no camera needed here
        }

        // Rule 3: All children are covered (state 0) but no child camera
        // → This node is NOT covered, ask parent to cover it
        return 2;  // not covered
    }

    // =========================================================
    // APPROACH 2 — EXPLICIT dp[node][3] (same logic, more explicit)
    // =========================================================
    /**
     * Store dp[state] = min cameras needed for subtree of node, given its state.
     *
     * dp[0] = min cameras when node is NOT covered (state: "needs parent")
     * dp[1] = min cameras when node HAS a camera
     * dp[2] = min cameras when node is COVERED (but no camera here)
     *
     * Return [dp0, dp1, dp2] from each node.
     * Parent picks the best combination.
     *
     * This is more general and works when the greedy reasoning isn't obvious.
     *
     * TRANSITION:
     *   For node u with left and right children:
     *
     *   dp[1] = 1 + min(left_all) + min(right_all)  ← place camera here
     *     where "left_all" means the left child can be in any state (0, 1, or 2)
     *     and since the camera at u covers the children, all are valid.
     *
     *   dp[2] = best case where u is covered by a child camera:
     *     → at least one child must have state 1 (camera)
     *     Options:
     *       A: left has camera (1), right can be covered (2) or camera (1)
     *       B: right has camera (1), left can be covered (2) or camera (1)
     *
     *   dp[0] = min(left[1] or left[2]) + min(right[1] or right[2]) without any child camera
     *     → both children are "covered or not" but NOT having a camera (since no camera covers u)
     *     Wait: dp[0] means u is NOT covered. So children must each be "covered or have camera"
     *     since they can't rely on u (u has no camera):
     *     dp[0] = min(left[1], left[2]) + min(right[1], right[2])
     *
     * This formulation is O(n) time, O(n) space for the dp arrays (or O(h) with stack).
     *
     * The answer for the whole tree:
     *   min(dp_root[1], dp_root[2])  ← root must be covered (not in state 0)
     */
    public int minCameraCoverDP(TreeNode root) {
        int[] res = dfsDP(root);
        // Root must be covered or have camera (not state 0)
        return Math.min(res[1], res[2]);
    }

    /**
     * Returns int[] {notCovered, hasCamera, covered}:
     *   [0] = min cameras in subtree if this node is NOT covered
     *   [1] = min cameras in subtree if this node HAS a camera
     *   [2] = min cameras in subtree if this node is COVERED (by a child)
     */
    private int[] dfsDP(TreeNode node) {
        if (node == null) {
            // Null: notCovered=inf (null shouldn't be uncovered in real use),
            //       hasCamera=inf (null doesn't place camera),
            //       covered=0 (null is "covered" trivially, costs 0)
            return new int[]{Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, 0};
        }

        int[] L = dfsDP(node.left);
        int[] R = dfsDP(node.right);

        // State 1: place camera at this node (cost 1)
        //   Children can be in ANY state (camera here covers them)
        int hasCamera = 1 + Math.min(L[0], Math.min(L[1], L[2]))
                          + Math.min(R[0], Math.min(R[1], R[2]));

        // State 2: this node is COVERED by a child camera
        //   At least one child must have a camera.
        //   Option A: left has camera, right is covered or has camera (not uncovered)
        //   Option B: right has camera, left is covered or has camera (not uncovered)
        //   Option C: BOTH children have cameras
        int covered = Math.min(
            L[1] + Math.min(R[1], R[2]),  // left has camera, right has cam or is covered
            R[1] + Math.min(L[1], L[2])   // right has camera, left has cam or is covered
        );

        // State 0: this node is NOT covered (needs parent)
        //   Children must each be covered or have camera (not uncovered themselves):
        int notCovered = Math.min(L[1], L[2]) + Math.min(R[1], R[2]);

        return new int[]{notCovered, hasCamera, covered};
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem3_TreeCameras sol = new Problem3_TreeCameras();

        System.out.println("=== LC 968: Binary Tree Cameras ===");

        // Test 1: [0,0,0] — path of 3 → 1 camera
        TreeNode t1 = new TreeNode(0, new TreeNode(0, new TreeNode(0), null), null);
        System.out.println("Path [0,0,0]:");
        System.out.println("  Greedy DP : " + sol.minCameraCover(t1));   // 1
        System.out.println("  Explicit  : " + sol.minCameraCoverDP(t1)); // 1

        // Test 2: [0,0,0,0,0] → 2 cameras
        sol.cameras = 0;
        TreeNode t2 = new TreeNode(0,
            new TreeNode(0, new TreeNode(0), new TreeNode(0)),
            new TreeNode(0));
        System.out.println("\nTree [0,0,0,0,0]:");
        System.out.println("  Greedy DP : " + sol.minCameraCover(t2));   // 2
        System.out.println("  Explicit  : " + sol.minCameraCoverDP(t2)); // 2

        // Test 3: single node [0] → 1 camera
        sol.cameras = 0;
        TreeNode t3 = new TreeNode(0);
        System.out.println("\nSingle [0]:");
        System.out.println("  Greedy DP : " + sol.minCameraCover(t3));   // 1
        System.out.println("  Explicit  : " + sol.minCameraCoverDP(t3)); // 1

        // Test 4: Perfect binary tree depth 3 (7 nodes)
        //         Needs 2 cameras (at depth 1: children of root)
        sol.cameras = 0;
        TreeNode t4 = new TreeNode(0,
            new TreeNode(0, new TreeNode(0), new TreeNode(0)),
            new TreeNode(0, new TreeNode(0), new TreeNode(0)));
        System.out.println("\nPerfect tree (7 nodes, depth 3):");
        System.out.println("  Greedy DP : " + sol.minCameraCover(t4));
        System.out.println("  Explicit  : " + sol.minCameraCoverDP(t4));

        // Test 5: Long chain of 10 nodes
        sol.cameras = 0;
        TreeNode t5 = new TreeNode(0);
        TreeNode cur = t5;
        for (int i = 0; i < 9; i++) {
            cur.left = new TreeNode(0);
            cur = cur.left;
        }
        System.out.println("\nChain of 10 nodes:");
        System.out.println("  Greedy DP : " + sol.minCameraCover(t5));   // ceil(10/3) = 4
        System.out.println("  Explicit  : " + sol.minCameraCoverDP(t5)); // 4
    }
}
