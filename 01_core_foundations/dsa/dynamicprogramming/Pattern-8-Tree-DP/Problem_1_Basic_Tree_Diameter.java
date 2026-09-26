/**
 * ============================================================
 *  PATTERN 8 — TREE DP
 *  Problem 1 (Basic): Tree Diameter   LC 543
 * ============================================================
 *
 *  Difficulty  : Easy-Medium (classified "Basic" for pattern entry)
 *  Pattern     : Tree DP — post-order DFS, global maximum
 *  LeetCode    : https://leetcode.com/problems/diameter-of-binary-tree/
 *                (LC 1245 generalizes to N-ary trees / unweighted graphs)
 *
 *  PROBLEM STATEMENT (LC 543):
 *    Given the root of a binary tree, return the length of the DIAMETER —
 *    the length of the longest path between ANY two nodes.
 *    The path may or may not pass through the root.
 *    Length = number of edges.
 *
 *  EXAMPLES:
 *    Tree:   1         → diameter = 3 (path: 4→2→1→3 or 5→2→1→3)
 *           / \
 *          2   3
 *         / \
 *        4   5
 *
 *    Tree:   1         → diameter = 1
 *             \
 *              2
 *
 * ============================================================
 */
import java.util.*;

// TreeNode definition
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int v) { val = v; }
    TreeNode(int v, TreeNode l, TreeNode r) { val = v; left = l; right = r; }
}

class Problem1_TreeDiameter {

    // =========================================================
    // APPROACH 1 — TREE DP: Binary Tree Diameter (LC 543)
    // =========================================================
    /**
     * KEY INSIGHT:
     *   At each node u, the longest path THROUGH u is:
     *     leftHeight(u) + rightHeight(u)
     *   where leftHeight = longest path going down through left subtree.
     *
     *   The DIAMETER is the maximum such value across all nodes.
     *
     * DP FORMULATION:
     *   dfs(u) returns: longest path from u going DOWN into its subtree (= height of u)
     *
     *   At node u:
     *     leftH  = dfs(u.left)    (longest path into left subtree from u)
     *     rightH = dfs(u.right)   (longest path into right subtree from u)
     *
     *     Update global diameter: diameter = max(diameter, leftH + rightH)
     *     Return to parent:       return 1 + max(leftH, rightH)  ← only ONE branch
     *
     *   Base: dfs(null) = 0 (no path from null)
     *
     * VISUAL for the example:
     *   dfs(4) = 0+1=1 (no children, return 1? wait: return 1 means 1 edge to parent)
     *   Actually: dfs(null) = 0 (leaf's children are null)
     *   dfs(4): left=0, right=0, diameter=max(d, 0+0)=0, return 1+max(0,0)=1
     *   dfs(5): similarly returns 1
     *   dfs(2): left=1, right=1, diameter=max(0, 1+1)=2, return 1+max(1,1)=2
     *   dfs(3): left=0, right=0, diameter=max(2, 0)=2, return 1
     *   dfs(1): left=2, right=1, diameter=max(2, 2+1)=3, return 1+max(2,1)=3
     *   → diameter = 3 ✓
     *
     * Time:  O(n) — each node visited once
     * Space: O(h) — recursion stack (h = height = O(log n) balanced, O(n) skewed)
     *
     * Follow-up: Diameter of N-ary Tree (LC 1522)?
     *   Same approach, but track top-2 longest child paths at each node.
     * Follow-up: Diameter of weighted tree?
     *   Use edge weights in the height computation.
     */
    private int diameter;

    public int diameterOfBinaryTree(TreeNode root) {
        diameter = 0;
        heightAndDiameter(root);
        return diameter;
    }

    /**
     * Returns the height of the subtree rooted at node
     * (= longest path from node going DOWN = number of edges).
     * Updates the global diameter as a side effect.
     */
    private int heightAndDiameter(TreeNode node) {
        if (node == null) return 0;

        int leftH  = heightAndDiameter(node.left);
        int rightH = heightAndDiameter(node.right);

        // Diameter candidate: path through this node
        diameter = Math.max(diameter, leftH + rightH);

        // Height of this node: 1 edge + the taller subtree
        return 1 + Math.max(leftH, rightH);
    }

    // =========================================================
    // APPROACH 2 — LONGEST PATH IN GENERAL TREE / GRAPH (LC 1245)
    // =========================================================
    /**
     * Given n nodes and edges of an UNDIRECTED TREE (not necessarily binary),
     * find the diameter (longest path by number of edges).
     *
     * Use an adjacency list and pass 'parent' to avoid revisiting.
     *
     * At each node, track the TOP-2 longest child paths.
     * Diameter at node u = top1 + top2.
     * Return to parent: top1 + 1.
     *
     * Time:  O(n)
     * Space: O(n) — adjacency list + recursion stack
     */
    private int treeDiameter;

    public int treeDiameterNary(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }
        treeDiameter = 0;
        dfsNary(0, -1, adj);
        return treeDiameter;
    }

    private int dfsNary(int u, int parent, List<List<Integer>> adj) {
        int top1 = 0, top2 = 0;  // top 2 longest child paths from u

        for (int v : adj.get(u)) {
            if (v == parent) continue;
            int childLen = dfsNary(v, u, adj) + 1;  // +1 for the edge u-v
            if (childLen > top1) {
                top2 = top1;
                top1 = childLen;
            } else if (childLen > top2) {
                top2 = childLen;
            }
        }

        treeDiameter = Math.max(treeDiameter, top1 + top2);
        return top1;  // return the longest path downward from u
    }

    // =========================================================
    // APPROACH 3 — ITERATIVE TREE DP (anti stack-overflow)
    // =========================================================
    /**
     * For very large trees (n up to 10^5), recursive DFS can overflow the stack.
     * Use iterative post-order DFS with an explicit stack.
     *
     * Steps:
     *   1. Compute DFS order and parent array.
     *   2. Process nodes in REVERSE DFS order (= post-order).
     *   3. At each node, combine children's heights.
     */
    public int diameterIterative(int n, int[][] edges) {
        if (n == 1) return 0;

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }

        int[] order  = new int[n];
        int[] par    = new int[n];
        Arrays.fill(par, -1);
        int[] height = new int[n];
        boolean[] visited = new boolean[n];
        int idx = 0;

        // Iterative DFS to get processing order
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        visited[0] = true;
        while (!stack.isEmpty()) {
            int u = stack.pop();
            order[idx++] = u;
            for (int v : adj.get(u)) {
                if (!visited[v]) {
                    visited[v] = true;
                    par[v] = u;
                    stack.push(v);
                }
            }
        }

        int ans = 0;
        // Process in reverse DFS order (post-order: children before parent)
        for (int i = idx - 1; i >= 0; i--) {
            int u = order[i];
            int top1 = 0, top2 = 0;
            for (int v : adj.get(u)) {
                if (v == par[u]) continue;
                int childH = height[v] + 1;
                if (childH > top1) {
                    top2 = top1; top1 = childH;
                } else if (childH > top2) {
                    top2 = childH;
                }
            }
            ans = Math.max(ans, top1 + top2);
            height[u] = top1;
        }

        return ans;
    }

    // =========================================================
    // Helper: Build binary tree from level-order array
    // =========================================================
    private TreeNode buildTree(Integer[] vals) {
        if (vals == null || vals.length == 0 || vals[0] == null) return null;
        TreeNode root = new TreeNode(vals[0]);
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        int i = 1;
        while (!q.isEmpty() && i < vals.length) {
            TreeNode curr = q.poll();
            if (i < vals.length && vals[i] != null) {
                curr.left = new TreeNode(vals[i]);
                q.offer(curr.left);
            }
            i++;
            if (i < vals.length && vals[i] != null) {
                curr.right = new TreeNode(vals[i]);
                q.offer(curr.right);
            }
            i++;
        }
        return root;
    }

    // =========================================================
    // Main: run demos
    // =========================================================
    public static void main(String[] args) {
        Problem1_TreeDiameter sol = new Problem1_TreeDiameter();

        System.out.println("=== LC 543: Diameter of Binary Tree ===");

        // Tree: 1->2->4,5; 1->3
        TreeNode root1 = new TreeNode(1,
            new TreeNode(2, new TreeNode(4), new TreeNode(5)),
            new TreeNode(3));
        System.out.println("Tree [1,2,3,4,5] diameter: " + sol.diameterOfBinaryTree(root1)); // 3

        // Tree: 1->2
        TreeNode root2 = new TreeNode(1, null, new TreeNode(2));
        System.out.println("Tree [1,null,2] diameter: " + sol.diameterOfBinaryTree(root2)); // 1

        // Single node
        TreeNode root3 = new TreeNode(1);
        System.out.println("Tree [1] diameter: " + sol.diameterOfBinaryTree(root3)); // 0

        System.out.println("\n=== N-ary Tree Diameter (General Graph) ===");
        // Path: 0-1-2-3
        int[][] edges1 = {{0,1},{1,2},{2,3}};
        System.out.println("Path 0-1-2-3: " + sol.treeDiameterNary(4, edges1)); // 3

        // Star: 0-1, 0-2, 0-3
        int[][] edges2 = {{0,1},{0,2},{0,3}};
        System.out.println("Star(0-1,0-2,0-3): " + sol.treeDiameterNary(4, edges2)); // 2

        System.out.println("\n=== Iterative Tree DP ===");
        int[][] edges3 = {{0,1},{1,2},{2,3},{1,4},{4,5}};
        System.out.println("Complex tree: " + sol.diameterIterative(6, edges3)); // 4 (0-1-4-5 or 3-2-1-4-5)
    }
}
