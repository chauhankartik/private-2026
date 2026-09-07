/**
 * ============================================================
 *  BINARY TREE — HARD PROBLEMS
 * ============================================================
 *
 *  H1.  Binary Tree Maximum Path Sum            LC 124
 *  H2.  Serialize and Deserialize Binary Tree   LC 297
 *  H3.  Binary Tree Cameras                     LC 968
 *  H4.  Recover Binary Search Tree              LC 99
 *  H5.  Morris Traversal (O(1) space inorder)   LC concept
 *  H6.  Flatten Binary Tree to Linked List      LC 114
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

class TreeHard {

    // =========================================================
    // H1. Binary Tree Maximum Path Sum  LC 124
    // Pattern: BOTTOM-UP DFS (global ≠ return, allows negative paths)
    // =========================================================
    /**
     * A path can start and end at any node. Values can be negative.
     * Find the maximum sum of any path in the tree.
     *
     * KEY INSIGHT (same as diameter but with values):
     *   At each node, the path THROUGH it has sum: node.val + max(leftGain, 0) + max(rightGain, 0)
     *   (We take max with 0 to skip negative subtrees.)
     *
     *   Global update: ans = max(ans, node.val + leftGain + rightGain)
     *   Return to parent: node.val + max(leftGain, rightGain, 0)
     *   (Can only extend in ONE direction toward parent.)
     *
     * Edge case: all-negative tree → answer is the single maximum node.
     *   Handled by: initialize ans = Integer.MIN_VALUE (not 0).
     *
     * Time:  O(n)
     * Space: O(h)
     */
    private int maxPathAns = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        maxPathAns = Integer.MIN_VALUE;
        gainFrom(root);
        return maxPathAns;
    }

    // Returns the max gain this node can contribute to its parent's path
    private int gainFrom(TreeNode node) {
        if (node == null) return 0;

        int leftGain  = Math.max(gainFrom(node.left),  0); // skip if negative
        int rightGain = Math.max(gainFrom(node.right), 0);

        // Path through this node (may branch left + right)
        maxPathAns = Math.max(maxPathAns, node.val + leftGain + rightGain);

        // Return: can only go ONE direction to parent
        return node.val + Math.max(leftGain, rightGain);
    }

    // =========================================================
    // H2. Serialize and Deserialize Binary Tree  LC 297
    // Pattern: PREORDER DFS with NULL MARKERS
    // =========================================================
    /**
     * Serialize: encode the tree to a string.
     * Deserialize: decode the string back to the tree.
     *
     * Approach 1: Preorder with null markers (simpler, used here).
     *   "1,2,#,#,3,4,#,#,5,#,#" where # = null.
     *   Preorder naturally lets us reconstruct by reading left-to-right.
     *
     * Approach 2: Level order BFS (used by LeetCode's own format).
     *   More space but easier to debug visually.
     *
     * WHY PREORDER WORKS:
     *   We know exactly where the root is (first element).
     *   Left subtree consumes exactly the nodes it needs (tracked by null markers).
     *   Remaining nodes form the right subtree.
     *
     * Time:  O(n) serialize and deserialize
     * Space: O(n) — string + recursion stack O(h)
     */
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }

    private void serializeHelper(TreeNode node, StringBuilder sb) {
        if (node == null) { sb.append("#,"); return; }
        sb.append(node.val).append(",");
        serializeHelper(node.left,  sb);
        serializeHelper(node.right, sb);
    }

    public TreeNode deserialize(String data) {
        Deque<String> tokens = new ArrayDeque<>(Arrays.asList(data.split(",")));
        return deserializeHelper(tokens);
    }

    private TreeNode deserializeHelper(Deque<String> tokens) {
        String token = tokens.pollFirst();
        if ("#".equals(token)) return null;
        TreeNode node = new TreeNode(Integer.parseInt(token));
        node.left  = deserializeHelper(tokens);
        node.right = deserializeHelper(tokens);
        return node;
    }

    // BFS serialize/deserialize (alternative — visual format like LC)
    public String serializeBFS(TreeNode root) {
        if (root == null) return "";
        StringBuilder sb = new StringBuilder();
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            TreeNode node = q.poll();
            if (node == null) { sb.append("#,"); continue; }
            sb.append(node.val).append(",");
            q.offer(node.left);
            q.offer(node.right);
        }
        return sb.toString();
    }

    // =========================================================
    // H3. Binary Tree Cameras  LC 968
    // Pattern: GREEDY DFS (bottom-up, 3 states)
    // =========================================================
    /**
     * Place minimum cameras such that every node is watched.
     * A camera at node X watches: X, X's parent, X's children.
     *
     * Greedy insight: place cameras as HIGH as possible (avoid leaves).
     * If a leaf's parent has a camera, it covers both.
     * So: never place cameras at leaves (they'll be covered by parents).
     *
     * Three states for each node:
     *   0 = NOT covered (parent must install a camera)
     *   1 = HAS a camera
     *   2 = COVERED (but no camera here)
     *
     * Bottom-up DFS:
     *   - If any child is NOT covered (state 0) → install camera here (state 1)
     *   - If any child HAS camera (state 1) → this node is covered (state 2)
     *   - Otherwise (both children covered, no camera) → return state 0 (need parent to cover)
     *
     * After DFS: if root itself returned state 0 → add one more camera at root.
     *
     * Time:  O(n)
     * Space: O(h)
     */
    private int cameras = 0;

    public int minCameraCover(TreeNode root) {
        cameras = 0;
        return (cameraState(root) == 0 ? 1 : 0) + cameras;
        // if root is uncovered, add one camera there
    }

    private int cameraState(TreeNode node) {
        if (node == null) return 2; // null nodes are "covered" (don't need watching)

        int left  = cameraState(node.left);
        int right = cameraState(node.right);

        if (left == 0 || right == 0) {
            cameras++;
            return 1; // install camera here
        }
        if (left == 1 || right == 1) {
            return 2; // covered by child's camera
        }
        return 0; // both children are covered but no camera → this node is uncovered
    }

    // =========================================================
    // H4. Recover Binary Search Tree  LC 99
    // Pattern: INORDER + DETECT SWAPPED PAIR
    // =========================================================
    /**
     * Exactly two nodes were swapped in a BST. Find and recover them.
     *
     * BST inorder traversal gives a SORTED sequence.
     * If two nodes are swapped, the sorted sequence will have anomalies.
     *
     * Two anomaly cases:
     *   (a) Adjacent swap: one inversion — [1, 3, 2, 4] → swap 3 and 2
     *       first  = 3, second = 2
     *   (b) Non-adjacent swap: two inversions — [1, 4, 3, 2, 5] → swap 4 and 2
     *       first  = 4 (from first inversion: 4 > 3)
     *       second = 2 (from second inversion: 3 > 2)
     *
     * Track: first, second (nodes to swap) and prev (previous inorder node).
     * At each inversion (prev.val > curr.val):
     *   - If first is null: first = prev (left partner)
     *   - Always update: second = curr (right partner)
     *
     * Swap only values: first.val ↔ second.val.
     *
     * Time:  O(n)
     * Space: O(h) recursive  |  O(1) with Morris traversal
     */
    private TreeNode first, second, prev;

    public void recoverTree(TreeNode root) {
        first = second = prev = null;
        inorderRecover(root);
        // Swap the two misplaced nodes
        int tmp = first.val; first.val = second.val; second.val = tmp;
    }

    private void inorderRecover(TreeNode node) {
        if (node == null) return;
        inorderRecover(node.left);

        // Check for inversion
        if (prev != null && prev.val > node.val) {
            if (first == null)  first  = prev;   // first time → capture prev as left partner
            second = node;                         // always update second to current
        }
        prev = node;

        inorderRecover(node.right);
    }

    // =========================================================
    // H5. Morris Traversal — O(1) Space Inorder
    // =========================================================
    /**
     * Standard inorder needs O(h) stack space. Morris traversal uses O(1) space.
     *
     * KEY IDEA: Temporarily link a node to its inorder PREDECESSOR.
     * This allows us to "return" to the parent without a stack.
     *
     * Algorithm:
     *   while curr != null:
     *     if no left child:
     *       VISIT curr
     *       curr = curr.right
     *     else:
     *       find inorder predecessor of curr (rightmost node in left subtree)
     *       if predecessor.right == null:
     *         predecessor.right = curr  ← THREAD (create temporary link)
     *         curr = curr.left
     *       else:
     *         predecessor.right = null  ← UNTHREAD (restore original structure)
     *         VISIT curr
     *         curr = curr.right
     *
     * Time:  O(n) — each node visited at most twice
     * Space: O(1)
     */
    public List<Integer> morrisInorder(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        TreeNode curr = root;

        while (curr != null) {
            if (curr.left == null) {
                result.add(curr.val);  // visit
                curr = curr.right;
            } else {
                // Find inorder predecessor (rightmost in left subtree)
                TreeNode pred = curr.left;
                while (pred.right != null && pred.right != curr) pred = pred.right;

                if (pred.right == null) {
                    pred.right = curr;   // thread: create temporary link back
                    curr = curr.left;
                } else {
                    pred.right = null;   // unthread: restore structure
                    result.add(curr.val); // visit
                    curr = curr.right;
                }
            }
        }

        return result;
    }

    // =========================================================
    // H6. Flatten Binary Tree to Linked List  LC 114
    // =========================================================
    /**
     * Flatten tree in-place to a right-skewed "linked list" using PREORDER order.
     *
     *       1                1
     *      / \               \
     *     2   5    →          2
     *    / \   \               \
     *   3   4   6               3
     *                            \
     *                             4
     *                              \
     *                               5
     *                                \
     *                                 6
     *
     * Approach 1: Reverse postorder (right → left → root) with running "prev" pointer.
     *   At each node: node.right = prev; node.left = null; prev = node.
     *   Process in order: right subtree → left subtree → root (reverse preorder).
     *
     * Approach 2: Iterative with Morris-like threading.
     *   For each node with a left child:
     *     Find rightmost of left subtree.
     *     Attach current right subtree there.
     *     Move left subtree to right. Clear left.
     *
     * Time:  O(n)
     * Space: O(h) approach 1  |  O(1) approach 2
     */
    // Approach 1: Reverse preorder (right → left → root) — O(h) stack
    private TreeNode flattenPrev = null;

    public void flatten(TreeNode root) {
        flattenPrev = null;
        flattenHelper(root);
    }

    private void flattenHelper(TreeNode node) {
        if (node == null) return;
        flattenHelper(node.right);   // process right first
        flattenHelper(node.left);    // then left
        node.right = flattenPrev;    // current's right = previously processed node
        node.left  = null;           // clear left
        flattenPrev = node;          // move prev to current
    }

    // Approach 2: Iterative O(1) space
    public void flattenIterative(TreeNode root) {
        TreeNode curr = root;
        while (curr != null) {
            if (curr.left != null) {
                // Find rightmost node of left subtree
                TreeNode rightmost = curr.left;
                while (rightmost.right != null) rightmost = rightmost.right;

                // Attach current right subtree to rightmost
                rightmost.right = curr.right;

                // Move left subtree to right
                curr.right = curr.left;
                curr.left  = null;
            }
            curr = curr.right;
        }
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        TreeHard sol = new TreeHard();

        System.out.println("═══ H1: Max Path Sum ═══");
        //    -10
        //    / \
        //   9  20
        //     /  \
        //    15    7
        TreeNode mps = new TreeNode(-10,
            new TreeNode(9),
            new TreeNode(20, new TreeNode(15), new TreeNode(7)));
        System.out.println(sol.maxPathSum(mps));  // 42 (15+20+7)

        System.out.println("\n═══ H2: Serialize / Deserialize ═══");
        TreeNode orig = new TreeNode(1,
            new TreeNode(2), new TreeNode(3, new TreeNode(4), new TreeNode(5)));
        String encoded = sol.serialize(orig);
        System.out.println("Serialized: " + encoded);
        TreeNode decoded = sol.deserialize(encoded);
        System.out.println("Root after decode: " + decoded.val);
        System.out.println("Left: " + decoded.left.val + "  Right: " + decoded.right.val);

        System.out.println("\n═══ H3: Min Cameras ═══");
        //   0
        //    \
        //     0
        //    / \
        //   0   0
        TreeNode cam = new TreeNode(0,
            null,
            new TreeNode(0, new TreeNode(0), new TreeNode(0)));
        System.out.println(sol.minCameraCover(cam));  // 1

        System.out.println("\n═══ H4: Recover BST ═══");
        // BST where 1 and 3 are swapped: [3, 2, 1] inorder should be [1, 2, 3]
        TreeNode bst = new TreeNode(3, new TreeNode(1), null);
        // Should be: root=1, root.left=3 — swap 3 and 1
        sol.recoverTree(bst);
        System.out.println("After recovery root: " + bst.val);  // 1

        System.out.println("\n═══ H5: Morris Inorder ═══");
        //   4
        //  / \
        // 2   5
        // / \
        //1   3
        TreeNode morris = new TreeNode(4,
            new TreeNode(2, new TreeNode(1), new TreeNode(3)),
            new TreeNode(5));
        System.out.println(sol.morrisInorder(morris)); // [1, 2, 3, 4, 5]

        System.out.println("\n═══ H6: Flatten to LL ═══");
        TreeNode flat = new TreeNode(1,
            new TreeNode(2, new TreeNode(3), new TreeNode(4)),
            new TreeNode(5, null, new TreeNode(6)));
        sol.flattenIterative(flat);
        TreeNode cur = flat;
        while (cur != null) { System.out.print(cur.val + " "); cur = cur.right; }
        System.out.println(); // 1 2 3 4 5 6
    }
}
