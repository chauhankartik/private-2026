/**
 * ============================================================
 *  TRIE (PREFIX TREE) — GOOGLE-LEVEL PROBLEMS
 *  Pattern-oriented multi-pattern complex problems.
 * ============================================================
 *
 *  Problems:
 *   G1. Palindrome Pairs (LeetCode 336 - Reversed Trie + Palindrome Suffix Check)
 *   G2. Concatenated Words (LeetCode 472 - Trie + DP Memoization)
 *   G3. Word Squares (LeetCode 425 - Trie + Backtracking)
 *   G4. Encrypt and Decrypt Strings (LeetCode 2227 - Trie Frequency Count)
 *   G5. Sum of Prefix Scores of Strings (LeetCode 2416 - Pass-through Node Counter)
 *   G6. Delete Duplicate Folders in System (LeetCode 1948 - Trie Subtree Serialization Hashing)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class GoogleLevel {

    // =========================================================
    // G1. PALINDROME PAIRS
    // Pattern: Reversed Trie + Palindrome Prefix/Suffix Check
    // LeetCode: 336
    // =========================================================
    /**
     * Problem: Find all index pairs (i, j) such that words[i] + words[j] forms a palindrome.
     *
     * Optimal O(N · K²):
     * Insert reversed words into Trie storing word index and list of valid palindrome suffixes.
     * For each word, traverse Trie to find matching prefixes.
     *
     * Time: O(N · K²)
     * Space: O(N · K²)
     */
    static class PalindromePairs {
        private static class Node {
            Node[] children = new Node[26];
            int wordIdx = -1;
            List<Integer> palindromeRest = new ArrayList<>();
        }

        public List<List<Integer>> palindromePairs(String[] words) {
            Node root = new Node();
            int n = words.length;

            // 1. Build Reversed Trie
            for (int i = 0; i < n; i++) {
                String w = words[i];
                Node curr = root;
                for (int j = w.length() - 1; j >= 0; j--) {
                    if (isPalindrome(w, 0, j)) {
                        curr.palindromeRest.add(i);
                    }
                    int idx = w.charAt(j) - 'a';
                    if (curr.children[idx] == null) curr.children[idx] = new Node();
                    curr = curr.children[idx];
                }
                curr.wordIdx = i;
                curr.palindromeRest.add(i);
            }

            // 2. Search for pairs
            List<List<Integer>> res = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String w = words[i];
                Node curr = root;
                for (int j = 0; j < w.length(); j++) {
                    if (curr.wordIdx >= 0 && curr.wordIdx != i && isPalindrome(w, j, w.length() - 1)) {
                        res.add(Arrays.asList(i, curr.wordIdx));
                    }
                    int idx = w.charAt(j) - 'a';
                    curr = curr.children[idx];
                    if (curr == null) break;
                }

                if (curr != null) {
                    for (int k : curr.palindromeRest) {
                        if (i != k) {
                            res.add(Arrays.asList(i, k));
                        }
                    }
                }
            }
            return res;
        }

        private boolean isPalindrome(String s, int left, int right) {
            while (left < right) {
                if (s.charAt(left++) != s.charAt(right--)) return false;
            }
            return true;
        }
    }

    // =========================================================
    // G2. CONCATENATED WORDS
    // Pattern: Trie + DP Memoization
    // LeetCode: 472
    // =========================================================
    /**
     * Problem: Return all words in list that are formed by concatenating >= 2 shorter words in list.
     *
     * Optimal O(N · L²): Sort words by length. Insert words into Trie one by one.
     * For each word, run Trie DP to check if it can be partitioned into existing Trie words.
     *
     * Time: O(N · L²)
     * Space: O(N · L)
     */
    private static class ConcatNode {
        ConcatNode[] children = new ConcatNode[26];
        boolean isEnd = false;
    }

    public List<String> findAllConcatenatedWordsInADict(String[] words) {
        Arrays.sort(words, (a, b) -> Integer.compare(a.length(), b.length()));

        ConcatNode root = new ConcatNode();
        List<String> res = new ArrayList<>();

        for (String w : words) {
            if (w.isEmpty()) continue;
            if (canForm(w, 0, root, new Boolean[w.length()])) {
                res.add(w);
            } else {
                // Only insert into Trie if it cannot be formed by shorter words
                ConcatNode curr = root;
                for (char ch : w.toCharArray()) {
                    int idx = ch - 'a';
                    if (curr.children[idx] == null) curr.children[idx] = new ConcatNode();
                    curr = curr.children[idx];
                }
                curr.isEnd = true;
            }
        }
        return res;
    }

    private boolean canForm(String word, int start, ConcatNode root, Boolean[] memo) {
        if (start == word.length()) return true;
        if (memo[start] != null) return memo[start];

        ConcatNode curr = root;
        for (int i = start; i < word.length(); i++) {
            int idx = word.charAt(i) - 'a';
            if (curr.children[idx] == null) break;
            curr = curr.children[idx];
            if (curr.isEnd && canForm(word, i + 1, root, memo)) {
                return memo[start] = true;
            }
        }
        return memo[start] = false;
    }

    // =========================================================
    // G3. WORD SQUARES
    // Pattern: Trie Prefix Backtracking
    // LeetCode: 425
    // =========================================================
    /**
     * Problem: Build K x K word squares such that row k == column k for all k.
     *
     * Optimal O(N · L · 26^L):
     * Store all words matching prefix in Trie node list.
     * At step k, build target prefix from k-th column of existing square, query Trie, and backtrack.
     *
     * Time: O(N · L · 26^L)
     * Space: O(N · L)
     */
    static class WordSquares {
        private static class Node {
            Node[] children = new Node[26];
            List<String> prefixWords = new ArrayList<>();
        }

        public List<List<String>> wordSquares(String[] words) {
            Node root = new Node();
            int len = words[0].length();

            for (String w : words) {
                Node curr = root;
                curr.prefixWords.add(w);
                for (char ch : w.toCharArray()) {
                    int idx = ch - 'a';
                    if (curr.children[idx] == null) curr.children[idx] = new Node();
                    curr = curr.children[idx];
                    curr.prefixWords.add(w);
                }
            }

            List<List<String>> res = new ArrayList<>();
            backtrack(0, len, root, new ArrayList<>(), res);
            return res;
        }

        private void backtrack(int step, int len, Node root, List<String> square, List<List<String>> res) {
            if (step == len) {
                res.add(new ArrayList<>(square));
                return;
            }

            StringBuilder prefix = new StringBuilder();
            for (int i = 0; i < step; i++) {
                prefix.append(square.get(i).charAt(step));
            }

            Node curr = root;
            for (char ch : prefix.toString().toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) return;
                curr = curr.children[idx];
            }

            for (String candidate : curr.prefixWords) {
                square.add(candidate);
                backtrack(step + 1, len, root, square, res);
                square.remove(square.size() - 1);
            }
        }
    }

    // =========================================================
    // G4. ENCRYPT AND DECRYPT STRINGS
    // Pattern: Reverse Trie Frequency Matching
    // LeetCode: 2227
    // =========================================================
    /**
     * Problem: Encrypt key chars to 2-char codes. Decrypt 2-char codes count matching valid dictionary words.
     *
     * Optimal O(L) decrypt: Pre-encrypt dictionary words and store encrypted frequency count in HashMap!
     *
     * Time:  Encrypt: O(L), Decrypt: O(1)
     * Space: O(N · L)
     */
    static class Encrypter {
        private final Map<Character, String> encMap = new HashMap<>();
        private final Map<String, Integer> countMap = new HashMap<>();

        public Encrypter(char[] keys, String[] values, String[] dictionary) {
            for (int i = 0; i < keys.length; i++) {
                encMap.put(keys[i], values[i]);
            }
            for (String dictWord : dictionary) {
                String encrypted = encrypt(dictWord);
                if (!encrypted.isEmpty()) {
                    countMap.put(encrypted, countMap.getOrDefault(encrypted, 0) + 1);
                }
            }
        }

        public String encrypt(String word1) {
            StringBuilder sb = new StringBuilder();
            for (char ch : word1.toCharArray()) {
                if (!encMap.containsKey(ch)) return "";
                sb.append(encMap.get(ch));
            }
            return sb.toString();
        }

        public int decrypt(String word2) {
            return countMap.getOrDefault(word2, 0);
        }
    }

    // =========================================================
    // G5. SUM OF PREFIX SCORES OF STRINGS
    // Pattern: Pass-through Node Counter Trie
    // LeetCode: 2416
    // =========================================================
    /**
     * Problem: For each string, return sum of scores of all its non-empty prefixes (score = count of words with prefix).
     *
     * Optimal O(N · L):
     * Insert all words into Trie, incrementing `passCount` on every node visited.
     * For each word, sum `passCount` along its path.
     *
     * Time: O(N · L)
     * Space: O(N · L)
     */
    public int[] sumPrefixScores(String[] words) {
        class Node {
            Node[] children = new Node[26];
            int passCount = 0;
        }

        Node root = new Node();

        // 1. Build Trie with pass-through counter
        for (String w : words) {
            Node curr = root;
            for (char ch : w.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) curr.children[idx] = new Node();
                curr = curr.children[idx];
                curr.passCount++;
            }
        }

        // 2. Query sum of prefix scores
        int[] scores = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            Node curr = root;
            int totalScore = 0;
            for (char ch : words[i].toCharArray()) {
                int idx = ch - 'a';
                curr = curr.children[idx];
                totalScore += curr.passCount;
            }
            scores[i] = totalScore;
        }
        return scores;
    }

    // =========================================================
    // G6. DELETE DUPLICATE FOLDERS IN SYSTEM
    // Pattern: Trie Subtree Serialization Hashing
    // LeetCode: 1948
    // =========================================================
    /**
     * Problem: Delete all subtrees in folder structure that have identical sub-folder structure.
     *
     * Optimal O(N · L):
     * 1. Build folder Trie.
     * 2. Serialize subtrees bottom-up: `serialize(node) = folderName + "(" + children_serializations + ")"`.
     * 3. Count serialization occurrences in HashMap.
     * 4. Delete folders whose subtree serialization appears > 1 time.
     *
     * Time: O(N · L)
     * Space: O(N · L)
     */
    static class DeleteDuplicateFolders {
        private static class FolderNode {
            String name;
            Map<String, FolderNode> children = new TreeMap<>(); // TreeMap for sorted child serialization
            boolean deleted = false;
        }

        public List<List<String>> deleteDuplicateFolder(List<List<String>> paths) {
            FolderNode root = new FolderNode();
            root.name = "";

            // 1. Build Folder Trie
            for (List<String> path : paths) {
                FolderNode curr = root;
                for (String folder : path) {
                    curr.children.putIfAbsent(folder, new FolderNode());
                    curr = curr.children.get(folder);
                    curr.name = folder;
                }
            }

            // 2. Serialize subtrees and track frequency
            Map<String, Integer> serialCount = new HashMap<>();
            serialize(root, serialCount);

            // 3. Mark duplicate subtrees as deleted
            markDuplicates(root, serialCount);

            // 4. Collect remaining non-deleted paths
            List<List<String>> result = new ArrayList<>();
            collectPaths(root, new ArrayList<>(), result);
            return result;
        }

        private String serialize(FolderNode node, Map<String, Integer> serialCount) {
            if (node.children.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, FolderNode> entry : node.children.entrySet()) {
                sb.append(entry.getKey()).append("(").append(serialize(entry.getValue(), serialCount)).append(")");
            }

            String serial = sb.toString();
            serialCount.put(serial, serialCount.getOrDefault(serial, 0) + 1);
            return serial;
        }

        private void markDuplicates(FolderNode node, Map<String, Integer> serialCount) {
            if (node.children.isEmpty()) return;

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, FolderNode> entry : node.children.entrySet()) {
                sb.append(entry.getKey()).append("(").append(serializeHelper(entry.getValue())).append(")");
            }

            String serial = sb.toString();
            if (serialCount.getOrDefault(serial, 0) > 1) {
                node.deleted = true;
                return;
            }

            for (FolderNode child : node.children.values()) {
                markDuplicates(child, serialCount);
            }
        }

        private String serializeHelper(FolderNode node) {
            if (node.children.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, FolderNode> entry : node.children.entrySet()) {
                sb.append(entry.getKey()).append("(").append(serializeHelper(entry.getValue())).append(")");
            }
            return sb.toString();
        }

        private void collectPaths(FolderNode node, List<String> path, List<List<String>> result) {
            if (node.deleted) return;

            if (!node.name.isEmpty()) {
                path.add(node.name);
                result.add(new ArrayList<>(path));
            }

            for (FolderNode child : node.children.values()) {
                collectPaths(child, path, result);
            }

            if (!node.name.isEmpty()) {
                path.remove(path.size() - 1);
            }
        }
    }
}
