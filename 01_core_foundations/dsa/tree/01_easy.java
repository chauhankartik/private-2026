/**
 * ============================================================
 *  BINARY TREE — EASY PROBLEMS
 * ============================================================
 *
 *  E1.  Maximum Depth of Binary Tree            LC 104
 *  E2.  Minimum Depth of Binary Tree            LC 111
 *  E3.  Invert Binary Tree                      LC 226
 *  E4.  Symmetric Tree                          LC 101
 *  E5.  Same Tree                               LC 100
 *  E6.  Path Sum I                              LC 112
 *  E7.  Count Nodes Equal to Average of Subtree LC 2265
 *  E8.  Level Order Traversal (BFS template)    LC 102
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int v) { val = v; }
    TreeNode(int v, TreeNode l, TreeNode r) { val = v; left = l; right = r; }
}

class TreeEasy {

    // ─── helpers ─────────────────────────────────────────────
    //       1
    //      / \
    //     2   3
    //    / \   \
    //   4   5   6
    static TreeNode sample() {
        return new TreeNode(1,
            new TreeNode(2,
                new TreeNode(4), new TreeNode(5)),
            new TreeNode(3,
                null, new TreeNode(6)));
    }

    static List<Integer> levelOrderFlat(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) return res;
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            TreeNode n = q.poll();
            res.add(n.val);
            if (n.left  != null) q.offer(n.left);
            if (n.right != null) q.offer(n.right);
        }
        return res;
    }

    // =========================================================
    // E1. Maximum Depth  LC 104
    // Pattern: RECURSIVE DFS (bottom-up)
    // =========================================================
    /**
     * Depth of tree = 1 + max(depth(left), depth(right)).
     * Base case: null node has depth 0.
     *
     * BFS alternative: count levels as you process layer by layer.
     *   BFS is O(n) time O(w) space (w=max width).
     *   Recursive DFS is O(n) time O(h) space (h=height).
     *   For balanced trees: O(log n) stack. For skewed: O(n).
     *
     * Time:  O(n)
     * Space: O(h) — h = height
     */
    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }

    // BFS version — counts levels explicitly
    public int maxDepthBFS(TreeNode root) {
        if (root == null) return 0;
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        int depth = 0;
        while (!q.isEmpty()) {
            depth++;
            int size = q.size();   // snapshot: number of nodes at this level
            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                if (node.left  != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
        }
        return depth;
    }

    // =========================================================
    // E2. Minimum Depth  LC 111
    // Pattern: BFS (finds min-depth leaf faster) or DFS
    // =========================================================
    /**
     * Minimum depth = shortest path from root to a LEAF.
     * CRITICAL DISTINCTION vs maxDepth:
     *   A node with only ONE child is NOT a leaf.
     *   Minimum depth cannot shortcut to that side.
     *
     *   Tree:  1
     *         /
     *        2
     *   minDepth = 2 (not 1, because node 1 has no right child)
     *   maxDepth = 2
     *
     * BFS finds minimum-depth leaf faster (first leaf reached = minimum depth).
     * DFS must visit all nodes to confirm the minimum.
     *
     * Time:  O(n)
     * Space: O(w) BFS — w = max width
     */
    public int minDepth(TreeNode root) {
        if (root == null) return 0;

        // BFS — return depth when FIRST leaf is reached
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        int depth = 0;
        while (!q.isEmpty()) {
            depth++;
            int size = q.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                if (node.left == null && node.right == null) return depth; // first leaf!
                if (node.left  != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
        }
        return depth;
    }

    public int minDepthDFS(TreeNode root) {
        if (root == null) return 0;
        if (root.left  == null) return 1 + minDepthDFS(root.right); // only right child
        if (root.right == null) return 1 + minDepthDFS(root.left);  // only left child
        return 1 + Math.min(minDepthDFS(root.left), minDepthDFS(root.right));
    }

    // =========================================================
    // E3. Invert Binary Tree  LC 226
    // Pattern: RECURSIVE DFS
    // =========================================================
    /**
     * Swap left and right children at every node.
     * Classic recursive: process children first, then swap (postorder style),
     * OR swap first, then recurse (preorder style) — both correct.
     *
     * Time:  O(n)
     * Space: O(h)
     */
    public TreeNode invertTree(TreeNode root) {
        if (root == null) return null;

        // Swap children
        TreeNode temp = root.left;
        root.left  = root.right;
        root.right = temp;

        // Recurse on swapped subtrees
        invertTree(root.left);
        invertTree(root.right);

        return root;
    }

    // Iterative BFS version
    public TreeNode invertTreeBFS(TreeNode root) {
        if (root == null) return null;
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            TreeNode node = q.poll();
            TreeNode tmp = node.left; node.left = node.right; node.right = tmp;
            if (node.left  != null) q.offer(node.left);
            if (node.right != null) q.offer(node.right);
        }
        return root;
    }

    // =========================================================
    // E4. Symmetric Tree  LC 101
    // Pattern: RECURSIVE / BFS MIRROR CHECK
    // =========================================================
    /**
     * A tree is symmetric if it's a mirror of itself around its center.
     *
     * Recursive: check if left subtree mirrors right subtree.
     *   isMirror(L, R):
     *     - both null → true
     *     - one null  → false
     *     - L.val != R.val → false
     *     - isMirror(L.left, R.right) AND isMirror(L.right, R.left)
     *
     * Time:  O(n)
     * Space: O(h)
     */
    public boolean isSymmetric(TreeNode root) {
        return isMirror(root.left, root.right);
    }

    private boolean isMirror(TreeNode left, TreeNode right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.val == right.val
            && isMirror(left.left,  right.right)   // outer pair
            && isMirror(left.right, right.left);   // inner pair
    }

    // Iterative (BFS with paired queue)
    public boolean isSymmetricIterative(TreeNode root) {
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root.left); q.offer(root.right);
        while (!q.isEmpty()) {
            TreeNode L = q.poll(), R = q.poll();
            if (L == null && R == null) continue;
            if (L == null || R == null || L.val != R.val) return false;
            q.offer(L.left);  q.offer(R.right);  // outer
            q.offer(L.right); q.offer(R.left);   // inner
        }
        return true;
    }

    // =========================================================
    // E5. Same Tree  LC 100
    // Pattern: RECURSIVE DFS (structural comparison)
    // =========================================================
    /**
     * Two trees are the same if they have identical structure and values.
     * Classic simultaneous DFS on both trees.
     *
     * Time:  O(n)   where n = min(nodes in p, nodes in q)
     * Space: O(h)
     */
    public boolean isSameTree(TreeNode p, TreeNode q) {
        if (p == null && q == null) return true;
        if (p == null || q == null) return false;
        return p.val == q.val
            && isSameTree(p.left,  q.left)
            && isSameTree(p.right, q.right);
    }

    // =========================================================
    // E6. Path Sum I  LC 112
    // Pattern: RECURSIVE DFS (root-to-leaf path)
    // =========================================================
    /**
     * Does a root-to-leaf path exist with sum = targetSum?
     *
     * Key: at a LEAF (no children), check if remaining == 0.
     * Subtract node.val at each step (no need to track running sum).
     *
     * Common mistake: checking at null nodes (allows paths that don't end at a leaf).
     *
     * Time:  O(n)
     * Space: O(h)
     */
    public boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) return false;

        // At a leaf: check if the remaining sum is exactly this node's value
        if (root.left == null && root.right == null) return root.val == targetSum;

        int remaining = targetSum - root.val;
        return hasPathSum(root.left, remaining) || hasPathSum(root.right, remaining);
    }

    // =========================================================
    // E7. Count Nodes Equal to Average of Subtree  LC 2265
    // Pattern: RECURSIVE DFS (return multiple values: sum + count)
    // =========================================================
    /**
     * Count nodes where node.val == average of its subtree (including itself).
     *
     * Return [sum, count] from each subtree.
     * At each node: total_sum = sum_left + sum_right + node.val
     *               total_count = count_left + count_right + 1
     *               if node.val == total_sum / total_count → ans++
     *
     * Pattern: return array/pair to propagate multiple values upward.
     *
     * Time:  O(n)
     * Space: O(h)
     */
    private int avgCount = 0;

    public int averageOfSubtree(TreeNode root) {
        avgCount = 0;
        subtreeSum(root);
        return avgCount;
    }

    // Returns [sum, count] of subtree rooted at node
    private int[] subtreeSum(TreeNode node) {
        if (node == null) return new int[]{0, 0};

        int[] left  = subtreeSum(node.left);
        int[] right = subtreeSum(node.right);

        int sum   = left[0] + right[0] + node.val;
        int count = left[1] + right[1] + 1;

        if (node.val == sum / count) avgCount++;  // integer division = floor

        return new int[]{sum, count};
    }

    // =========================================================
    // E8. Level Order Traversal  LC 102
    // Pattern: BFS — the canonical template
    // =========================================================
    /**
     * Return node values grouped by level.
     *
     * THE BFS TEMPLATE:
     *   1. Offer root to queue.
     *   2. While queue not empty:
     *      a. Snapshot level size = q.size()
     *      b. Process exactly 'size' nodes (one full level)
     *      c. Offer their children for next iteration
     *
     * Time:  O(n)
     * Space: O(w) — w = max width (worst case O(n) for perfect BT bottom level)
     */
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);

        while (!q.isEmpty()) {
            int size = q.size();               // ← CRITICAL: snapshot before inner loop
            List<Integer> level = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                level.add(node.val);
                if (node.left  != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }

            result.add(level);
        }

        return result;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        TreeEasy sol = new TreeEasy();

        //       1
        //      / \
        //     2   3
        //    / \   \
        //   4   5   6
        TreeNode tree = sample();

        System.out.println("═══ E1: Max Depth ═══");
        System.out.println(sol.maxDepth(tree));      // 3
        System.out.println(sol.maxDepthBFS(tree));   // 3

        System.out.println("\n═══ E2: Min Depth ═══");
        System.out.println(sol.minDepth(tree));      // 2 (path 1→3)
        System.out.println(sol.minDepthDFS(tree));   // 2

        System.out.println("\n═══ E3: Invert ═══");
        TreeNode t2 = sample();
        System.out.println("Before: " + levelOrderFlat(t2));   // [1,2,3,4,5,6]
        sol.invertTree(t2);
        System.out.println("After:  " + levelOrderFlat(t2));   // [1,3,2,6,4,5]

        System.out.println("\n═══ E4: Symmetric ═══");
        //     1
        //    / \
        //   2   2
        //  / \ / \
        // 3  4 4  3
        TreeNode sym = new TreeNode(1,
            new TreeNode(2, new TreeNode(3), new TreeNode(4)),
            new TreeNode(2, new TreeNode(4), new TreeNode(3)));
        System.out.println("Symmetric: " + sol.isSymmetric(sym));  // true
        System.out.println("sample():  " + sol.isSymmetric(sample())); // false

        System.out.println("\n═══ E5: Same Tree ═══");
        System.out.println(sol.isSameTree(sample(), sample())); // true

        System.out.println("\n═══ E6: Path Sum ═══");
        // Paths in sample: 1→2→4=7, 1→2→5=8, 1→3→6=10
        System.out.println(sol.hasPathSum(sample(), 7));   // true
        System.out.println(sol.hasPathSum(sample(), 8));   // true
        System.out.println(sol.hasPathSum(sample(), 10));  // true
        System.out.println(sol.hasPathSum(sample(), 5));   // false

        System.out.println("\n═══ E7: Average of Subtree ═══");
        //     4
        //    / \
        //   8   5
        //  / \   \
        // 0   1   6
        TreeNode avg = new TreeNode(4,
            new TreeNode(8, new TreeNode(0), new TreeNode(1)),
            new TreeNode(5, null, new TreeNode(6)));
        System.out.println(sol.averageOfSubtree(avg)); // expected 5

        System.out.println("\n═══ E8: Level Order ═══");
        System.out.println(sol.levelOrder(sample()));
        // [[1], [2, 3], [4, 5, 6]]
    }
}
