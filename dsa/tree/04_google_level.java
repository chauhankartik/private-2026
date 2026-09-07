/**
 * ============================================================
 *  BINARY TREE — GOOGLE-LEVEL PROBLEMS
 * ============================================================
 *
 *  G1.  BST Iterator                            LC 173  ★ Google
 *  G2.  Count Complete Tree Nodes               LC 222  ★ Google
 *  G3.  Vertical Order Traversal                LC 987  ★ Google
 *  G4.  Count Good Nodes in Binary Tree         LC 1448
 *  G5.  Step-by-Step Directions (LCA + path)    LC 2096 ★ Google
 *
 *  Each problem includes:
 *   - Why Google asks it
 *   - Brute → optimal trace
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

class TreeGoogleLevel {

    // =========================================================
    // G1. BST Iterator  LC 173  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Tests iterator design, lazy evaluation, and understanding of inorder traversal.
     *   The key insight: replicate the INORDER stack-based traversal incrementally.
     *   Each next() call should be O(1) amortized (not O(n)).
     *
     * Design a BST iterator that returns nodes in ascending order:
     *   hasNext() → O(1)
     *   next()    → O(h) amortized (O(1) amortized for complete BSTs)
     *
     * Key: use an explicit stack to simulate inorder traversal.
     * Pre-load by pushing all left nodes onto the stack.
     * On next():
     *   - Pop the top (smallest remaining node).
     *   - Push its right child's leftmost chain (set up for next call).
     *
     * Space: O(h) — max stack depth = height of BST.
     * For balanced BST: O(log n). For skewed: O(n).
     *
     * WHY O(h) NOT O(n):
     *   At any point, the stack contains nodes on the leftmost path from some node to a leaf.
     *   That path length = height.
     *
     * Amortized analysis:
     *   Each node is pushed and popped exactly ONCE across all next() calls.
     *   Total pushes = n. So amortized per next() = O(1).
     */
    static class BSTIterator {
        private final Deque<TreeNode> stack = new ArrayDeque<>();

        BSTIterator(TreeNode root) {
            pushLeft(root);  // pre-load leftmost chain
        }

        public int next() {
            TreeNode node = stack.pop();      // smallest remaining
            pushLeft(node.right);             // push right subtree's leftmost chain
            return node.val;
        }

        public boolean hasNext() {
            return !stack.isEmpty();
        }

        private void pushLeft(TreeNode node) {
            while (node != null) { stack.push(node); node = node.left; }
        }
    }

    // Follow-up: prev() operation? Maintain two stacks — one for inorder, one for reverse inorder.

    // =========================================================
    // G2. Count Complete Tree Nodes  LC 222  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   The naive O(n) answer is trivial. Google expects the O(log²n) approach.
     *   Tests knowledge of complete binary tree properties.
     *
     * Complete Binary Tree: all levels full except possibly last, filled left to right.
     *
     * Brute: DFS count all nodes — O(n)
     * Optimal: binary search on last row — O(log²n)
     *
     * Key insight:
     *   For a PERFECT binary tree with height h: node count = 2^h - 1.
     *   If left height == right height → left subtree is PERFECT → left has 2^leftH - 1 nodes.
     *   If left height != right height → right subtree is PERFECT (one level shorter).
     *
     * At each recursive call:
     *   Compute leftHeight (go all left) and rightHeight (go all right).
     *   If equal: left subtree is perfect → return 2^leftHeight - 1 + 1 (for current) + count(right)
     *   If not equal: right subtree is perfect → return count(left) + 1 + 2^rightHeight - 1
     *
     * Recursion depth: O(log n) levels.
     * At each level: O(log n) to compute heights.
     * Total: O(log²n).
     *
     * Time:  O(log²n)
     * Space: O(log n) — recursion stack
     */
    public int countNodes(TreeNode root) {
        if (root == null) return 0;

        int leftH  = leftHeight(root);   // leftmost path
        int rightH = rightHeight(root);  // rightmost path

        if (leftH == rightH) {
            // Perfect binary tree — count = 2^height - 1
            return (1 << leftH) - 1;
        }

        // Not perfect — recurse
        return 1 + countNodes(root.left) + countNodes(root.right);
    }

    private int leftHeight(TreeNode node) {
        int h = 0;
        while (node != null) { h++; node = node.left; }
        return h;
    }

    private int rightHeight(TreeNode node) {
        int h = 0;
        while (node != null) { h++; node = node.right; }
        return h;
    }

    // =========================================================
    // G3. Vertical Order Traversal  LC 987  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Multi-dimensional sorting — tests ability to track multiple coordinates.
     *   Requires clean coordinate system design.
     *
     * Assign (row, col) to each node:
     *   Root = (0, 0). Left child = (row+1, col-1). Right child = (row+1, col+1).
     *
     * Return nodes grouped by column, ordered by:
     *   1. Column (primary)
     *   2. Row (secondary, within same column)
     *   3. Value (tertiary, within same column AND row)
     *
     * Algorithm:
     *   DFS to collect (col, row, val) for each node.
     *   Sort by (col, row, val).
     *   Group by col.
     *
     * Time:  O(n log n)  — sorting
     * Space: O(n)
     */
    public List<List<Integer>> verticalTraversal(TreeNode root) {
        List<int[]> nodes = new ArrayList<>();  // [col, row, val]
        dfsVertical(root, 0, 0, nodes);

        // Sort: primary=col, secondary=row, tertiary=val
        nodes.sort((a, b) -> a[0] != b[0] ? a[0] - b[0] :
                             a[1] != b[1] ? a[1] - b[1] : a[2] - b[2]);

        List<List<Integer>> result = new ArrayList<>();
        int prevCol = Integer.MIN_VALUE;

        for (int[] node : nodes) {
            if (node[0] != prevCol) {
                result.add(new ArrayList<>());
                prevCol = node[0];
            }
            result.get(result.size() - 1).add(node[2]);
        }

        return result;
    }

    private void dfsVertical(TreeNode node, int row, int col, List<int[]> nodes) {
        if (node == null) return;
        nodes.add(new int[]{col, row, node.val});
        dfsVertical(node.left,  row + 1, col - 1, nodes);
        dfsVertical(node.right, row + 1, col + 1, nodes);
    }

    // =========================================================
    // G4. Count Good Nodes in Binary Tree  LC 1448
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Clean DFS path-tracking problem. Tests passing state DOWNWARD in DFS.
     *
     * A node X is "good" if no node on the path from root to X has value > X.val.
     * Count the good nodes.
     *
     * Note: ROOT is always good (no previous nodes).
     *
     * DFS: pass the maximum value seen on the path so far.
     * At each node: if node.val >= max_so_far → good node; update max.
     *
     * Time:  O(n)
     * Space: O(h)
     */
    public int goodNodes(TreeNode root) {
        return countGood(root, Integer.MIN_VALUE);
    }

    private int countGood(TreeNode node, int maxSoFar) {
        if (node == null) return 0;
        int count = node.val >= maxSoFar ? 1 : 0;  // this node is good
        int newMax = Math.max(maxSoFar, node.val);
        return count + countGood(node.left, newMax) + countGood(node.right, newMax);
    }

    // =========================================================
    // G5. Step-by-Step Directions (LCA + path)  LC 2096  ★ Google
    // =========================================================
    /**
     * WHY GOOGLE ASKS IT:
     *   Combines LCA with path extraction — two patterns fused together.
     *   Key insight: paths to LCA from source and destination give the directions.
     *
     * Find directions from node s to node t in a binary tree.
     * Directions: "L" (go left), "R" (go right), "U" (go up/parent).
     *
     * Algorithm:
     *   1. Find path from root to s: e.g., "LRL"
     *   2. Find path from root to t: e.g., "LRR"
     *   3. Remove the common prefix (= path to LCA): "LR" is common
     *   4. Path from s: replace each char in s_suffix with "U" (going up)
     *      s_suffix = "L" → 1 "U"
     *   5. Path to t: t_suffix = "R"
     *   6. Answer: "U" * len(s_suffix) + t_suffix = "UR"
     *
     * This works because:
     *   s → LCA requires going UP len(s_path) - len(common_prefix) times.
     *   LCA → t requires following the t_suffix directions.
     *
     * Time:  O(n)  — two DFS passes
     * Space: O(n)  — path strings + recursion
     */
    public String getDirections(TreeNode root, int startValue, int destValue) {
        StringBuilder pathToStart = new StringBuilder();
        StringBuilder pathToDest  = new StringBuilder();

        findPath(root, startValue, pathToStart);
        findPath(root, destValue,  pathToDest);

        String s = pathToStart.toString();
        String t = pathToDest.toString();

        // Remove common prefix (= path to LCA)
        int i = 0;
        while (i < s.length() && i < t.length() && s.charAt(i) == t.charAt(i)) i++;

        // From s to LCA: go up (s.length() - i) times
        // From LCA to t: follow t.substring(i)
        return "U".repeat(s.length() - i) + t.substring(i);
    }

    // Finds path from root to target, returns true if found
    private boolean findPath(TreeNode node, int target, StringBuilder path) {
        if (node == null) return false;
        if (node.val == target) return true;

        path.append('L');
        if (findPath(node.left,  target, path)) return true;
        path.deleteCharAt(path.length() - 1); // backtrack

        path.append('R');
        if (findPath(node.right, target, path)) return true;
        path.deleteCharAt(path.length() - 1); // backtrack

        return false;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        TreeGoogleLevel sol = new TreeGoogleLevel();

        System.out.println("═══ G1: BST Iterator ═══");
        //   7
        //  / \
        // 3  15
        //    / \
        //   9  20
        TreeNode bst = new TreeNode(7,
            new TreeNode(3),
            new TreeNode(15, new TreeNode(9), new TreeNode(20)));
        BSTIterator it = new BSTIterator(bst);
        List<Integer> itResult = new ArrayList<>();
        while (it.hasNext()) itResult.add(it.next());
        System.out.println(itResult); // [3, 7, 9, 15, 20]

        System.out.println("\n═══ G2: Count Complete Tree Nodes ═══");
        //       1
        //      / \
        //     2   3
        //    / \  /
        //   4   5 6
        TreeNode complete = new TreeNode(1,
            new TreeNode(2, new TreeNode(4), new TreeNode(5)),
            new TreeNode(3, new TreeNode(6), null));
        System.out.println(sol.countNodes(complete)); // 6

        TreeNode perfect = new TreeNode(1,
            new TreeNode(2, new TreeNode(4), new TreeNode(5)),
            new TreeNode(3, new TreeNode(6), new TreeNode(7)));
        System.out.println(sol.countNodes(perfect)); // 7

        System.out.println("\n═══ G3: Vertical Order Traversal ═══");
        //     3
        //    / \
        //   9  20
        //     /  \
        //    15   7
        TreeNode vt = new TreeNode(3,
            new TreeNode(9),
            new TreeNode(20, new TreeNode(15), new TreeNode(7)));
        System.out.println(sol.verticalTraversal(vt)); // [[9],[3,15],[20],[7]]

        System.out.println("\n═══ G4: Count Good Nodes ═══");
        //     3
        //    / \
        //   1   4
        //  /   / \
        // 3   1   5
        TreeNode good = new TreeNode(3,
            new TreeNode(1, new TreeNode(3), null),
            new TreeNode(4, new TreeNode(1), new TreeNode(5)));
        System.out.println(sol.goodNodes(good)); // 4 (nodes: 3, 4, 5, 3)

        System.out.println("\n═══ G5: Step-by-Step Directions ═══");
        //         5
        //        / \
        //       1   2
        //      /   / \
        //     3   6   4
        TreeNode dirs = new TreeNode(5,
            new TreeNode(1, new TreeNode(3), null),
            new TreeNode(2, new TreeNode(6), new TreeNode(4)));
        System.out.println(sol.getDirections(dirs, 3, 6)); // UURL
        System.out.println(sol.getDirections(dirs, 3, 4)); // UUURR... verify
    }
}
