
/**
 * ============================================================
 *  BINARY TREE — MEDIUM PROBLEMS
 * ============================================================
 *
 *  M1.  Binary Tree Diameter                    LC 543
 *  M2.  Binary Tree Right Side View             LC 199
 *  M3.  Path Sum II (all paths)                 LC 113
 *  M4.  Path Sum III (any node, any direction)  LC 437
 *  M5.  Lowest Common Ancestor (BT)             LC 236
 *  M6.  Construct from Preorder + Inorder       LC 105
 *  M7.  Validate BST                            LC 98
 *  M8.  Kth Smallest in BST                     LC 230
 *  M9.  Zigzag Level Order                      LC 103
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

    TreeNode(int v) {
        val = v;
    }

    TreeNode(int v, TreeNode l, TreeNode r) {
        val = v;
        left = l;
        right = r;
    }
}

class TreeMedium {

    // =========================================================
    // M1. Binary Tree Diameter LC 543
    // Pattern: BOTTOM-UP DFS (key: global vs return are DIFFERENT)
    // =========================================================
    /**
     * Diameter = longest path between ANY two nodes (may not pass through root).
     * The path passes through some node N: its length = height(N.left) +
     * height(N.right).
     *
     * KEY INSIGHT — Two different values at each node:
     * Global update: diameter = max(diameter, left_height + right_height)
     * Return to parent: height = max(left_height, right_height) + 1
     * (We can only extend the path in ONE direction to the parent)
     *
     * Common mistake: thinking diameter = height(root.left) + height(root.right).
     * This is WRONG — the longest path may not go through the root!
     *
     * Time: O(n)
     * Space: O(h)
     */
    private int diameter = 0;

    public int diameterOfBinaryTree(TreeNode root) {
        diameter = 0;
        heightForDiameter(root);
        return diameter;
    }

    private int heightForDiameter(TreeNode node) {
        if (node == null)
            return 0;
        int left = heightForDiameter(node.left);
        int right = heightForDiameter(node.right);
        diameter = Math.max(diameter, left + right); // path through this node
        return Math.max(left, right) + 1; // height returned to parent
    }

    // =========================================================
    // M2. Binary Tree Right Side View LC 199
    // Pattern: BFS (last node of each level)
    // =========================================================
    /**
     * Return the value of the rightmost node at each level.
     *
     * BFS approach: at each level, the LAST node polled is the rightmost.
     * DFS approach: do preorder (root → right → left); first visit at each depth is
     * the rightmost.
     *
     * Time: O(n)
     * Space: O(w) BFS — max width
     */
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null)
            return result;

        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                if (i == size - 1)
                    result.add(node.val); // rightmost of this level
                if (node.left != null)
                    q.offer(node.left);
                if (node.right != null)
                    q.offer(node.right);
            }
        }
        return result;
    }

    // DFS alternative — cleaner, works if you need left side view too (swap
    // right/left order)
    public List<Integer> rightSideViewDFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        dfsRight(root, 0, result);
        return result;
    }

    private void dfsRight(TreeNode node, int depth, List<Integer> result) {
        if (node == null)
            return;
        if (depth == result.size())
            result.add(node.val); // first visit at this depth
        dfsRight(node.right, depth + 1, result); // right first
        dfsRight(node.left, depth + 1, result);
    }

    // ALL root-to-leave paths string representation
    public List<String> binaryTreePaths(TreeNode root) {
        List<String> ans = new ArrayList<>();
        findPaths(root, "", ans);
        return ans;
    }

    void findPaths(TreeNode node, String current, List<String> ans) {
        if (node == null)
            return;

        if (current.isEmpty())
            current += node.val;
        else
            current += "->" + node.val;

        if (node.left == null && node.right == null) {
            ans.add(current);
            return;
        }

        findPaths(node.left, current, ans);
        findPaths(node.right, current, ans);
    }

    // =========================================================
    // M3. Path Sum II — All Root-to-Leaf Paths LC 113
    // Pattern: DFS BACKTRACKING
    // =========================================================
    /**
     * Collect ALL root-to-leaf paths that sum to targetSum.
     * Use backtracking: add node to path, recurse, remove on return.
     *
     * Time: O(n²) worst case — n nodes × O(n) to copy each path
     * Space: O(h) — recursion stack + path (not counting output)
     */
    public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(root, targetSum, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(TreeNode node, int remaining, List<Integer> path,
            List<List<Integer>> result) {
        if (node == null)
            return;

        path.add(node.val);
        remaining -= node.val;

        // At leaf: check if path sums to target
        if (node.left == null && node.right == null && remaining == 0) {
            result.add(new ArrayList<>(path)); // make a copy!
        }

        backtrack(node.left, remaining, path, result);
        backtrack(node.right, remaining, path, result);

        path.remove(path.size() - 1); // backtrack — undo this node
    }

    // =========================================================
    // M4. Path Sum III — Any Node to Node LC 437
    // Pattern: DFS + PREFIX SUM (HashMap)
    // =========================================================
    /**
     * Count paths (any start, any end, must go downward) that sum to targetSum.
     *
     * Brute: from each node, DFS all paths below — O(n²) time
     * Optimal: prefix sum map — O(n) time O(n) space
     *
     * KEY INSIGHT (same as subarray sum = k):
     * prefixSum at node = sum from root to current node.
     * If prefixSum(curr) - prefixSum(ancestor) == target,
     * then the path from (ancestor+1) to curr sums to target.
     * Count how many ancestors have prefixSum = prefixSum(curr) - target.
     *
     * Time: O(n)
     * Space: O(n) — prefix sum map
     */
    public int pathSumIII(TreeNode root, int targetSum) {
        Map<Long, Integer> prefixCount = new HashMap<>();
        prefixCount.put(0L, 1); // empty path (from root) has sum 0
        return dfsPathSum(root, 0L, targetSum, prefixCount);
    }

    private int dfsPathSum(TreeNode node, long curr, int target,
            Map<Long, Integer> prefixCount) {
        if (node == null)
            return 0;

        curr += node.val;
        // How many ancestors have prefixSum = curr - target?
        int count = prefixCount.getOrDefault(curr - target, 0);

        prefixCount.merge(curr, 1, Integer::sum); // add current prefix

        count += dfsPathSum(node.left, curr, target, prefixCount);
        count += dfsPathSum(node.right, curr, target, prefixCount);

        prefixCount.merge(curr, -1, Integer::sum); // backtrack — remove current prefix

        return count;
    }

    // =========================================================
    // M5. Lowest Common Ancestor — Binary Tree LC 236
    // Pattern: RECURSIVE DFS (bottom-up search)
    // =========================================================
    /**
     * LCA of two nodes p and q is the deepest node that has both p and q as
     * descendants.
     *
     * Algorithm:
     * If root is null → return null.
     * If root is p or q → return root (found one of them).
     * Recurse left and right.
     * If both sides return non-null → root is the LCA (p and q are on different
     * sides).
     * If only one side returns non-null → LCA is in that subtree.
     *
     * Why this works:
     * The first node where we "see" p from one side and q from the other is the
     * LCA.
     *
     * Time: O(n)
     * Space: O(h)
     */
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null)
            return null;
        if (root == p || root == q)
            return root; // found p or q

        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);

        if (left != null && right != null)
            return root; // p and q on different sides
        return (left != null) ? left : right; // both in same subtree
    }

    // Follow-up: LCA in BST — use BST property (simpler, O(h) space vs O(n) in BT)
    public TreeNode lcaBST(TreeNode root, TreeNode p, TreeNode q) {
        if (root.val > p.val && root.val > q.val)
            return lcaBST(root.left, p, q);
        if (root.val < p.val && root.val < q.val)
            return lcaBST(root.right, p, q);
        return root; // root is between p and q (or equal to one) → it's the LCA
    }

    // =========================================================
    // M6. Construct from Preorder + Inorder LC 105
    // Pattern: DIVIDE & CONQUER with HashMap index
    // =========================================================
    /**
     * Preorder: [root, left subtree, right subtree]
     * Inorder: [left subtree, root, right subtree]
     *
     * Algorithm:
     * 1. preorder[0] = root.
     * 2. Find root in inorder → everything left of it = left subtree,
     * everything right = right subtree.
     * 3. Recursively build left and right subtrees.
     *
     * HashMap maps inorder value → index for O(1) root lookup.
     * Without HashMap: O(n²). With HashMap: O(n).
     *
     * Time: O(n)
     * Space: O(n) — HashMap + O(h) stack
     */
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        Map<Integer, Integer> inMap = new HashMap<>();
        for (int i = 0; i < inorder.length; i++)
            inMap.put(inorder[i], i);
        return buildHelper(preorder, 0, preorder.length - 1,
                inorder, 0, inorder.length - 1, inMap);
    }

    private TreeNode buildHelper(int[] pre, int preStart, int preEnd,
            int[] in, int inStart, int inEnd,
            Map<Integer, Integer> inMap) {
        if (preStart > preEnd)
            return null;

        int rootVal = pre[preStart];
        TreeNode root = new TreeNode(rootVal);
        int inIdx = inMap.get(rootVal); // position of root in inorder
        int leftSize = inIdx - inStart; // size of left subtree

        root.left = buildHelper(pre, preStart + 1, preStart + leftSize,
                in, inStart, inIdx - 1, inMap);
        root.right = buildHelper(pre, preStart + leftSize + 1, preEnd,
                in, inIdx + 1, inEnd, inMap);
        return root;
    }

    // Follow-up: construct from Postorder + Inorder — same idea, root =
    // postorder[last]

    // =========================================================
    // M7. Validate BST LC 98
    // Pattern: DFS WITH VALID RANGE [min, max]
    // =========================================================
    /**
     * BST property: left subtree values < node.val < right subtree values.
     * This must hold for ALL ancestors, not just direct parent.
     *
     * 5
     * / \
     * 1 4 ← 4 < 5 (direct parent check passes)
     * / \
     * 3 6 ← 3 < 4 (direct parent check passes)
     * BUT 3 < 5 is violated! (3 is in right subtree of 5)
     *
     * Solution: pass valid range [min, max] at each node.
     * When going LEFT from node N: upper bound becomes N.val.
     * When going RIGHT from node N: lower bound becomes N.val.
     *
     * Common mistake: only comparing with direct parent (misses the above bug).
     *
     * Time: O(n)
     * Space: O(h)
     */
    public boolean isValidBST(TreeNode root) {
        return validateBST(root, Long.MIN_VALUE, Long.MAX_VALUE);
        // Use Long to handle Integer.MIN_VALUE / Integer.MAX_VALUE edge cases
    }

    private boolean validateBST(TreeNode node, long min, long max) {
        if (node == null)
            return true;
        if (node.val <= min || node.val >= max)
            return false;
        return validateBST(node.left, min, node.val)
                && validateBST(node.right, node.val, max);
    }

    // Alternative: inorder traversal should produce strictly increasing sequence
    public boolean isValidBSTInorder(TreeNode root) {
        long prev = Long.MIN_VALUE;
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode curr = root;
        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
            curr = stack.pop();
            if (curr.val <= prev)
                return false; // not strictly increasing
            prev = curr.val;
            curr = curr.right;
        }
        return true;
    }

    // =========================================================
    // M8. Kth Smallest in BST LC 230
    // Pattern: INORDER TRAVERSAL (BST inorder = sorted)
    // =========================================================
    /**
     * BST inorder traversal visits nodes in ASCENDING order.
     * Stop at the kth node visited.
     *
     * Recursive: count during inorder — O(n) in worst case (k = n)
     * Iterative: explicit stack, stop early at kth node — O(h + k)
     *
     * Time: O(h + k) — h to reach leftmost, k more steps
     * Space: O(h)
     */
    public int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode curr = root;

        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
            curr = stack.pop();
            k--;
            if (k == 0)
                return curr.val; // found the kth smallest
            curr = curr.right;
        }
        return -1; // should not reach here if k is valid
    }

    // Follow-up: if BST is frequently modified and kth smallest is frequently
    // called,
    // augment the tree with subtree size at each node (order-statistics tree).
    // Then kth smallest = O(h) using rank queries.

    // =========================================================
    // M9. Binary Tree Zigzag Level Order LC 103
    // Pattern: BFS + DIRECTION FLAG
    // =========================================================
    /**
     * Level order but alternating direction (left-to-right then right-to-left).
     *
     * Options:
     * a. BFS with deque: addFirst vs addLast based on level parity.
     * b. BFS with list: reverse odd levels.
     * c. DFS with depth tracking.
     *
     * Time: O(n)
     * Space: O(w) — max width
     */
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null)
            return result;

        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        boolean leftToRight = true;

        while (!q.isEmpty()) {
            int size = q.size();
            Deque<Integer> level = new ArrayDeque<>();

            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                if (leftToRight)
                    level.addLast(node.val); // append to end
                else
                    level.addFirst(node.val); // prepend to front
                if (node.left != null)
                    q.offer(node.left);
                if (node.right != null)
                    q.offer(node.right);
            }

            result.add(new ArrayList<>(level));
            leftToRight = !leftToRight; // flip direction
        }

        return result;
    }

    // =========================================================
    // Main: run all demos
    // =========================================================
    public static void main(String[] args) {
        TreeMedium sol = new TreeMedium();

        // 1
        // / \
        // 2 3
        // / \
        // 4 5
        TreeNode tree = new TreeNode(1,
                new TreeNode(2, new TreeNode(4), new TreeNode(5)),
                new TreeNode(3));

        System.out.println("═══ M1: Diameter ═══");
        // 1
        // / \
        // 2 3
        // / \
        // 4 5
        // Longest path: 4→2→5 (len 2) or 4→2→1→3 (len 3)
        System.out.println(sol.diameterOfBinaryTree(tree)); // 3

        System.out.println("\n═══ M2: Right Side View ═══");
        System.out.println(sol.rightSideView(tree)); // [1, 3, 5]

        System.out.println("\n═══ M3: Path Sum II ═══");
        // Tree: root=5, left=4→11→(7,2), right=8→(13,4→5)
        TreeNode ps = new TreeNode(5,
                new TreeNode(4, new TreeNode(11, new TreeNode(7), new TreeNode(2)), null),
                new TreeNode(8, new TreeNode(13), new TreeNode(4, new TreeNode(5), null)));
        System.out.println(sol.pathSum(ps, 22)); // [[5,4,11,2],[5,8,4,5]]

        System.out.println("\n═══ M4: Path Sum III ═══");
        TreeNode ps3 = new TreeNode(10,
                new TreeNode(5, new TreeNode(3, new TreeNode(3), new TreeNode(-2)),
                        new TreeNode(2, null, new TreeNode(1))),
                new TreeNode(-3, null, new TreeNode(11)));
        System.out.println(sol.pathSumIII(ps3, 8)); // 3

        System.out.println("\n═══ M5: LCA ═══");
        // 3
        // / \
        // 5 1
        // / \
        // 6 2
        TreeNode lcaTree = new TreeNode(3,
                new TreeNode(5, new TreeNode(6),
                        new TreeNode(2)),
                new TreeNode(1));
        TreeNode p = lcaTree.left; // 5
        TreeNode q = lcaTree.left.left; // 6
        System.out.println(sol.lowestCommonAncestor(lcaTree, p, q).val); // 5

        System.out.println("\n═══ M6: Build from Pre+In ═══");
        int[] pre = { 3, 9, 20, 15, 7 }, in = { 9, 3, 15, 20, 7 };
        TreeNode built = sol.buildTree(pre, in);
        // Level order of result: [3, 9, 20, 15, 7]
        System.out.println(built.val + " left=" + built.left.val + " right=" + built.right.val);

        System.out.println("\n═══ M7: Validate BST ═══");
        TreeNode valid = new TreeNode(2, new TreeNode(1), new TreeNode(3));
        TreeNode invalid = new TreeNode(5, new TreeNode(1), new TreeNode(4,
                new TreeNode(3), new TreeNode(6)));
        System.out.println("valid:   " + sol.isValidBST(valid)); // true
        System.out.println("invalid: " + sol.isValidBST(invalid)); // false

        System.out.println("\n═══ M8: Kth Smallest BST ═══");
        // 3
        // / \
        // 1 4
        // \
        // 2
        TreeNode bst = new TreeNode(3, new TreeNode(1, null, new TreeNode(2)), new TreeNode(4));
        System.out.println("k=1: " + sol.kthSmallest(bst, 1)); // 1
        System.out.println("k=2: " + sol.kthSmallest(bst, 2)); // 2
        System.out.println("k=3: " + sol.kthSmallest(bst, 3)); // 3

        System.out.println("\n═══ M9: Zigzag Level Order ═══");
        System.out.println(sol.zigzagLevelOrder(tree)); // [[1], [3, 2], [4, 5]]
    }
}
