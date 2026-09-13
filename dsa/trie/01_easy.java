/**
 * ============================================================
 *  TRIE (PREFIX TREE) — EASY PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   E1. Implement Trie (Prefix Tree) (LeetCode 208)
 *   E2. Longest Common Prefix (LeetCode 14)
 *   E3. Replace Words (LeetCode 648)
 *   E4. Map Sum Pairs (LeetCode 677)
 *   E5. Counting Words With a Given Prefix (LeetCode 2185)
 *   E6. Index Pairs of a String (LeetCode 1065)
 *   E7. Search Suggestions System (LeetCode 1268 - Basic Prefix Match)
 *   E8. Remove Sub-Folders from the Filesystem (LeetCode 1233)
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
    // E1. IMPLEMENT TRIE (PREFIX TREE)
    // Pattern: Standard Array-Based Trie Node
    // LeetCode: 208
    // =========================================================
    /**
     * Problem: Implement a Trie with insert, search, and startsWith methods.
     *
     * Optimal O(M):
     * Node structure: TrieNode[26] children, boolean isEnd.
     *
     * Time:  O(M) per operation where M is string length
     * Space: O(N · M · 26) where N is number of inserted words
     */
    static class Trie {
        private static class TrieNode {
            TrieNode[] children = new TrieNode[26];
            boolean isEnd = false;
        }

        private final TrieNode root;

        public Trie() {
            root = new TrieNode();
        }

        public void insert(String word) {
            TrieNode node = root;
            for (char ch : word.toCharArray()) {
                int idx = ch - 'a';
                if (node.children[idx] == null) {
                    node.children[idx] = new TrieNode();
                }
                node = node.children[idx];
            }
            node.isEnd = true;
        }

        public boolean search(String word) {
            TrieNode node = findNode(word);
            return node != null && node.isEnd;
        }

        public boolean startsWith(String prefix) {
            return findNode(prefix) != null;
        }

        private TrieNode findNode(String str) {
            TrieNode node = root;
            for (char ch : str.toCharArray()) {
                int idx = ch - 'a';
                if (node.children[idx] == null) return null;
                node = node.children[idx];
            }
            return node;
        }
    }

    // =========================================================
    // E2. LONGEST COMMON PREFIX
    // Pattern: Single Branch Trie Traversal
    // LeetCode: 14
    // =========================================================
    /**
     * Problem: Find longest common prefix among an array of strings.
     *
     * Optimal O(N · M): Insert all strings into Trie. Traverse root downwards
     * as long as node has EXACTLY 1 child and isEnd is false.
     *
     * Time: O(N · M)
     * Space: O(N · M)
     */
    public String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        if (strs.length == 1) return strs[0];

        Trie trie = new Trie();
        for (String s : strs) {
            if (s.isEmpty()) return "";
            trie.insert(s);
        }

        StringBuilder sb = new StringBuilder();
        Easy.Trie.TrieNode curr = trie.root;

        while (curr != null && !curr.isEnd) {
            int childCount = 0;
            int nextIdx = -1;
            for (int i = 0; i < 26; i++) {
                if (curr.children[i] != null) {
                    childCount++;
                    nextIdx = i;
                }
            }
            if (childCount != 1) break;
            sb.append((char) ('a' + nextIdx));
            curr = curr.children[nextIdx];
        }
        return sb.toString();
    }

    // =========================================================
    // E3. REPLACE WORDS
    // Pattern: Shortest Prefix Matching Trie
    // LeetCode: 648
    // =========================================================
    /**
     * Problem: Replace sentence words with shortest matching root from dictionary.
     *
     * Optimal O(N · M + S): Insert dictionary into Trie. For each word in sentence,
     * traverse Trie and replace with prefix as soon as isEnd == true.
     *
     * Time: O(N · M + S)
     * Space: O(N · M)
     */
    public String replaceWords(List<String> dictionary, String sentence) {
        Trie trie = new Trie();
        for (String rootWord : dictionary) {
            trie.insert(rootWord);
        }

        String[] words = sentence.split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            result.append(findRoot(trie.root, words[i]));
        }
        return result.toString();
    }

    private String findRoot(Easy.Trie.TrieNode root, String word) {
        Easy.Trie.TrieNode node = root;
        StringBuilder prefix = new StringBuilder();
        for (char ch : word.toCharArray()) {
            int idx = ch - 'a';
            if (node.children[idx] == null) break;
            prefix.append(ch);
            node = node.children[idx];
            if (node.isEnd) return prefix.toString();
        }
        return word;
    }

    // =========================================================
    // E4. MAP SUM PAIRS
    // Pattern: Node Pass-through Sum Tracking
    // LeetCode: 677
    // =========================================================
    /**
     * Problem: MapSum pairs string key with int value. Return sum of values of all keys with given prefix.
     *
     * Optimal O(K) insert & prefix sum: Store `val` in each node representing sum of keys passing through.
     *
     * Time: O(K) per operation where K is key length
     * Space: O(N · K)
     */
    static class MapSum {
        private static class Node {
            Node[] children = new Node[26];
            int sum = 0;
        }

        private final Node root = new Node();
        private final Map<String, Integer> keyMap = new HashMap<>();

        public void insert(String key, int val) {
            int delta = val - keyMap.getOrDefault(key, 0);
            keyMap.put(key, val);

            Node node = root;
            for (char ch : key.toCharArray()) {
                int idx = ch - 'a';
                if (node.children[idx] == null) {
                    node.children[idx] = new Node();
                }
                node = node.children[idx];
                node.sum += delta;
            }
        }

        public int sum(String prefix) {
            Node node = root;
            for (char ch : prefix.toCharArray()) {
                int idx = ch - 'a';
                if (node.children[idx] == null) return 0;
                node = node.children[idx];
            }
            return node.sum;
        }
    }

    // =========================================================
    // E5. COUNTING WORDS WITH A GIVEN PREFIX
    // Pattern: Trie Count Property / String Prefix Check
    // LeetCode: 2185
    // =========================================================
    /**
     * Problem: Count strings in words that have pref as a prefix.
     *
     * Optimal O(N · M): Insert all words tracking pass-through count.
     *
     * Time: O(N · M)
     * Space: O(N · M)
     */
    public int prefixCount(String[] words, String pref) {
        int count = 0;
        for (String w : words) {
            if (w.startsWith(pref)) count++;
        }
        return count;
    }

    // =========================================================
    // E6. INDEX PAIRS OF A STRING
    // Pattern: Multi-pattern Substring Matching
    // LeetCode: 1065
    // =========================================================
    /**
     * Problem: Given text and words array, return all index pairs [i, j] such that text[i..j] is in words.
     *
     * Optimal O(T² + W): Build Trie from words. For each starting index i in text, walk Trie.
     *
     * Time: O(T² + W)
     * Space: O(W)
     */
    public int[][] indexPairs(String text, String[] words) {
        Trie trie = new Trie();
        for (String w : words) trie.insert(w);

        List<int[]> list = new ArrayList<>();
        int n = text.length();

        for (int i = 0; i < n; i++) {
            Easy.Trie.TrieNode node = trie.root;
            for (int j = i; j < n; j++) {
                int idx = text.charAt(j) - 'a';
                if (node.children[idx] == null) break;
                node = node.children[idx];
                if (node.isEnd) {
                    list.add(new int[]{i, j});
                }
            }
        }
        return list.toArray(new int[list.size()][]);
    }

    // =========================================================
    // E7. SEARCH SUGGESTIONS SYSTEM (BASIC)
    // Pattern: Prefix Node Lexicographical Search
    // LeetCode: 1268
    // =========================================================
    /**
     * Problem: Return top 3 lexicographical matching products for each character typed in searchWord.
     *
     * Optimal O(N log N + S): Sort products. Build Trie storing top 3 sorted words per node.
     *
     * Time: O(N log N + S)
     * Space: O(N · M)
     */
    public List<List<String>> suggestedProducts(String[] products, String searchWord) {
        Arrays.sort(products);

        class Node {
            Node[] children = new Node[26];
            List<String> top3 = new ArrayList<>();
        }

        Node root = new Node();
        for (String prod : products) {
            Node curr = root;
            for (char ch : prod.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) {
                    curr.children[idx] = new Node();
                }
                curr = curr.children[idx];
                if (curr.top3.size() < 3) {
                    curr.top3.add(prod);
                }
            }
        }

        List<List<String>> res = new ArrayList<>();
        Node curr = root;
        for (char ch : searchWord.toCharArray()) {
            int idx = ch - 'a';
            if (curr != null) curr = curr.children[idx];
            res.add(curr == null ? Collections.emptyList() : curr.top3);
        }
        return res;
    }

    // =========================================================
    // E8. REMOVE SUB-FOLDERS FROM THE FILESYSTEM
    // Pattern: Folder Path Hierarchy Trie
    // LeetCode: 1233
    // =========================================================
    /**
     * Problem: Remove all sub-folders from folder list.
     *
     * Optimal O(N · M): Sort folder paths. If current folder starts with prevFolder + "/", it is subfolder.
     *
     * Time: O(N · M log N)
     * Space: O(1) extra space
     */
    public List<String> removeSubfolders(String[] folder) {
        Arrays.sort(folder);
        List<String> res = new ArrayList<>();
        for (String f : folder) {
            if (res.isEmpty() || !f.startsWith(res.get(res.size() - 1) + "/")) {
                res.add(f);
            }
        }
        return res;
    }
}
