/**
 * ============================================================
 *  TRIE (PREFIX TREE) — HARD PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   H1. Word Search II (LeetCode 212 - Trie + Backtracking + Node Pruning)
 *   H2. Word Break II (LeetCode 140 - Trie + Memoized DP Backtracking)
 *   H3. Prefix and Suffix Search (LeetCode 745 - Combined Wrapped Trie)
 *   H4. Maximum XOR With an Element From Array (LeetCode 1707 - Offline Queries + Bitwise Trie)
 *   H5. Design Search Autocomplete System (LeetCode 642 - Trie + Frequency Top 3)
 *   H6. Count Pairs With XOR in a Range (LeetCode 1803 - Bitwise Trie Subtree Count)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Hard {

    // =========================================================
    // H1. WORD SEARCH II
    // Pattern: Trie Grid Backtracking + Dynamic Node Pruning
    // LeetCode: 212
    // =========================================================
    /**
     * Problem: Given an m x n board of characters and a list of words, return all words on the board.
     *
     * Optimal O(M · N · 4^L):
     * Build Trie from words. Run DFS from every cell in grid.
     * Store `word` directly at terminal Trie node to eliminate string building.
     * **Crucial Pruning:** Set `node.word = null` after match to prevent duplicates,
     * and decrement child reference counts to prune empty Trie subtrees dynamically.
     *
     * Time: O(M · N · 4^L) where L is max word length
     * Space: O(W · L)
     */
    static class WordSearchII {
        private static class TrieNode {
            TrieNode[] children = new TrieNode[26];
            String word = null; // Stores word at end node
            int refs = 0;      // Active children count for pruning
        }

        private void insert(TrieNode root, String word) {
            TrieNode curr = root;
            curr.refs++;
            for (char ch : word.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) {
                    curr.children[idx] = new TrieNode();
                }
                curr = curr.children[idx];
                curr.refs++;
            }
            curr.word = word;
        }

        public List<String> findWords(char[][] board, String[] words) {
            TrieNode root = new TrieNode();
            for (String w : words) insert(root, w);

            List<String> result = new ArrayList<>();
            int m = board.length, n = board[0].length;

            for (int r = 0; r < m; r++) {
                for (int c = 0; c < n; c++) {
                    int idx = board[r][c] - 'a';
                    if (root.children[idx] != null) {
                        dfs(board, r, c, root, result);
                    }
                }
            }
            return result;
        }

        private void dfs(char[][] board, int r, int c, TrieNode parent, List<String> result) {
            char ch = board[r][c];
            int idx = ch - 'a';
            TrieNode curr = parent.children[idx];

            if (curr == null || curr.refs == 0) return;

            if (curr.word != null) {
                result.add(curr.word);
                curr.word = null; // Prevent duplicate additions
            }

            board[r][c] = '#'; // Mark visited

            int[] dr = {-1, 1, 0, 0};
            int[] dc = {0, 0, -1, 1};

            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr >= 0 && nr < board.length && nc >= 0 && nc < board[0].length && board[nr][nc] != '#') {
                    if (curr.children[board[nr][nc] - 'a'] != null) {
                        dfs(board, nr, nc, curr, result);
                    }
                }
            }

            board[r][c] = ch; // Backtrack
        }
    }

    // =========================================================
    // H2. WORD BREAK II
    // Pattern: Trie + Memoized DP Backtracking
    // LeetCode: 140
    // =========================================================
    /**
     * Problem: Given a string s and wordDict, insert spaces to construct all valid sentences.
     *
     * Optimal O(N · 2^N) worst case:
     * Build Trie for wordDict. Run DFS with HashMap memoization `memo.put(start, list_of_sentences)`.
     *
     * Time: O(N · 2^N)
     * Space: O(N · 2^N)
     */
    private static class WordBreakNode {
        WordBreakNode[] children = new WordBreakNode[26];
        boolean isEnd = false;
    }

    public List<String> wordBreak(String s, List<String> wordDict) {
        WordBreakNode root = new WordBreakNode();
        for (String w : wordDict) {
            WordBreakNode curr = root;
            for (char ch : w.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) curr.children[idx] = new WordBreakNode();
                curr = curr.children[idx];
            }
            curr.isEnd = true;
        }

        Map<Integer, List<String>> memo = new HashMap<>();
        return dfsWordBreak(s, 0, root, memo);
    }

    private List<String> dfsWordBreak(String s, int start, WordBreakNode root, Map<Integer, List<String>> memo) {
        if (memo.containsKey(start)) return memo.get(start);

        List<String> sentences = new ArrayList<>();
        if (start == s.length()) {
            sentences.add("");
            return sentences;
        }

        WordBreakNode curr = root;
        for (int end = start; end < s.length(); end++) {
            int idx = s.charAt(end) - 'a';
            if (curr.children[idx] == null) break;
            curr = curr.children[idx];

            if (curr.isEnd) {
                String word = s.substring(start, end + 1);
                List<String> subSentences = dfsWordBreak(s, end + 1, root, memo);
                for (String sub : subSentences) {
                    sentences.add(word + (sub.isEmpty() ? "" : " " + sub));
                }
            }
        }

        memo.put(start, sentences);
        return sentences;
    }

    // =========================================================
    // H3. PREFIX AND SUFFIX SEARCH
    // Pattern: Wrapped Combination Trie (`suffix + '{' + prefix`)
    // LeetCode: 745
    // =========================================================
    /**
     * Problem: Design WordFilter(words) supporting f(prefix, suffix) returning max index of matching word.
     *
     * Optimal Trick:
     * For word "apple" with index i, insert all wrapped suffix combinations:
     * "e{apple", "le{apple", "ple{apple", "pple{apple", "apple{apple"
     * Then query `suffix + '{' + prefix` in Trie!
     *
     * Time:  Build: O(N · L²), Query: O(PrefixLen + SuffixLen)
     * Space: O(N · L²)
     */
    static class WordFilter {
        private static class Node {
            Node[] children = new Node[27]; // 26 letters + 1 for '{'
            int maxWeight = -1;
        }

        private final Node root = new Node();

        public WordFilter(String[] words) {
            for (int weight = 0; weight < words.length; weight++) {
                String w = words[weight];
                int len = w.length();
                for (int i = 0; i <= len; i++) {
                    String wrapped = w.substring(i) + "{" + w;
                    insert(wrapped, weight);
                }
            }
        }

        private void insert(String key, int weight) {
            Node curr = root;
            for (char ch : key.toCharArray()) {
                int idx = ch - 'a'; // '{' - 'a' = 123 - 97 = 26
                if (curr.children[idx] == null) {
                    curr.children[idx] = new Node();
                }
                curr = curr.children[idx];
                curr.maxWeight = weight;
            }
        }

        public int f(String prefix, String suffix) {
            String searchKey = suffix + "{" + prefix;
            Node curr = root;
            for (char ch : searchKey.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) return -1;
                curr = curr.children[idx];
            }
            return curr.maxWeight;
        }
    }

    // =========================================================
    // H4. MAXIMUM XOR WITH AN ELEMENT FROM ARRAY
    // Pattern: Offline Sorted Queries + Bitwise Trie
    // LeetCode: 1707
    // =========================================================
    /**
     * Problem: Given nums and queries [xi, mi], find max XOR of xi with nums[j] <= mi.
     *
     * Optimal O(N log N + Q log Q):
     * 1. Sort nums ascending.
     * 2. Sort queries by constraint mi ascending (store original query index).
     * 3. Process queries offline: incrementally insert nums[j] <= mi into Bitwise Trie.
     * 4. Query max XOR for xi in Bitwise Trie.
     *
     * Time: O(N log N + Q log Q)
     * Space: O(32 · N + Q)
     */
    public int[] maximizeXor(int[] nums, int[][] queries) {
        Arrays.sort(nums);
        int q = queries.length;
        int[][] sortedQ = new int[q][3];
        for (int i = 0; i < q; i++) {
            sortedQ[i][0] = queries[i][0]; // xi
            sortedQ[i][1] = queries[i][1]; // mi
            sortedQ[i][2] = i;             // original index
        }
        Arrays.sort(sortedQ, (a, b) -> Integer.compare(a[1], b[1]));

        class BinaryNode {
            BinaryNode[] children = new BinaryNode[2];
        }

        BinaryNode root = new BinaryNode();
        int[] ans = new int[q];
        int numIdx = 0, n = nums.length;

        for (int[] query : sortedQ) {
            int x = query[0];
            int m = query[1];
            int origIdx = query[2];

            // Insert all nums <= m into Binary Trie
            while (numIdx < n && nums[numIdx] <= m) {
                BinaryNode curr = root;
                for (int i = 31; i >= 0; i--) {
                    int bit = (nums[numIdx] >> i) & 1;
                    if (curr.children[bit] == null) curr.children[bit] = new BinaryNode();
                    curr = curr.children[bit];
                }
                numIdx++;
            }

            if (numIdx == 0) {
                ans[origIdx] = -1; // No number <= m exists
            } else {
                BinaryNode curr = root;
                int maxXOR = 0;
                for (int i = 31; i >= 0; i--) {
                    int bit = (x >> i) & 1;
                    int opp = bit ^ 1;
                    if (curr.children[opp] != null) {
                        maxXOR |= (1 << i);
                        curr = curr.children[opp];
                    } else {
                        curr = curr.children[bit];
                    }
                }
                ans[origIdx] = maxXOR;
            }
        }
        return ans;
    }

    // =========================================================
    // H5. DESIGN SEARCH AUTOCOMPLETE SYSTEM
    // Pattern: Trie Node Frequency Top-3 Tracking
    // LeetCode: 642
    // =========================================================
    /**
     * Problem: Design Autocomplete System returning top 3 historical search sentences matching prefix.
     *
     * Optimal O(L):
     * Store sentences and frequencies. Each Trie node maintains a top 3 candidate list.
     *
     * Time:  Input char: O(1) by maintaining current search node
     * Space: O(N · L)
     */
    static class AutocompleteSystem {
        private static class Node {
            Node[] children = new Node[27]; // 26 letters + 1 for ' '
            Map<String, Integer> counts = new HashMap<>();
        }

        private final Node root = new Node();
        private Node currNode;
        private StringBuilder currentSentence = new StringBuilder();

        public AutocompleteSystem(String[] sentences, int[] times) {
            currNode = root;
            for (int i = 0; i < sentences.length; i++) {
                insert(sentences[i], times[i]);
            }
        }

        private void insert(String s, int count) {
            Node curr = root;
            for (char ch : s.toCharArray()) {
                int idx = getIndex(ch);
                if (curr.children[idx] == null) curr.children[idx] = new Node();
                curr = curr.children[idx];
                curr.counts.put(s, curr.counts.getOrDefault(s, 0) + count);
            }
        }

        private int getIndex(char ch) {
            return ch == ' ' ? 26 : ch - 'a';
        }

        public List<String> input(char c) {
            if (c == '#') {
                insert(currentSentence.toString(), 1);
                currentSentence.setLength(0);
                currNode = root;
                return Collections.emptyList();
            }

            currentSentence.append(c);
            int idx = getIndex(c);
            if (currNode != null) currNode = currNode.children[idx];
            if (currNode == null) return Collections.emptyList();

            PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                (a, b) -> a.getValue().equals(b.getValue()) ?
                    b.getKey().compareTo(a.getKey()) : Integer.compare(a.getValue(), b.getValue())
            );

            for (Map.Entry<String, Integer> e : currNode.counts.entrySet()) {
                pq.offer(e);
                if (pq.size() > 3) pq.poll();
            }

            List<String> res = new ArrayList<>();
            while (!pq.isEmpty()) res.add(0, pq.poll().getKey());
            return res;
        }
    }

    // =========================================================
    // H6. COUNT PAIRS WITH XOR IN A RANGE
    // Pattern: Bitwise Trie Subtree Counter
    // LeetCode: 1803
    // =========================================================
    /**
     * Problem: Count pairs (i, j) such that low <= (nums[i] XOR nums[j]) <= high.
     *
     * Optimal O(32 · N):
     * Count pairs with XOR <= K using countPairsLessThan(nums, K + 1).
     * Answer = countPairsLessThan(high + 1) - countPairsLessThan(low).
     *
     * Time: O(32 · N)
     * Space: O(32 · N)
     */
    public int countPairs(int[] nums, int low, int high) {
        return countPairsLessThan(nums, high + 1) - countPairsLessThan(nums, low);
    }

    private int countPairsLessThan(int[] nums, int k) {
        class Node {
            Node[] children = new Node[2];
            int count = 0;
        }

        Node root = new Node();
        int totalPairs = 0;

        for (int num : nums) {
            // 1. Query pairs with num having XOR < k
            Node curr = root;
            for (int i = 31; i >= 0 && curr != null; i--) {
                int numBit = (num >> i) & 1;
                int kBit = (k >> i) & 1;

                if (kBit == 1) {
                    // If kBit is 1, any path taking numBit (which gives XOR bit 0 < 1) contributes all subtrees
                    if (curr.children[numBit] != null) {
                        totalPairs += curr.children[numBit].count;
                    }
                    curr = curr.children[numBit ^ 1]; // Move down path giving XOR bit 1
                } else {
                    // If kBit is 0, must take numBit branch (which gives XOR bit 0)
                    curr = curr.children[numBit];
                }
            }

            // 2. Insert num into Binary Trie
            curr = root;
            curr.count++;
            for (int i = 31; i >= 0; i--) {
                int bit = (num >> i) & 1;
                if (curr.children[bit] == null) curr.children[bit] = new Node();
                curr = curr.children[bit];
                curr.count++;
            }
        }
        return totalPairs;
    }
}
