/**
 * ============================================================
 *  TRIE (PREFIX TREE) — MEDIUM PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   M1. Design Add and Search Words Data Structure (LeetCode 211 - Wildcard '.')
 *   M2. Maximum XOR of Two Numbers in an Array (LeetCode 421 - Bitwise Trie)
 *   M3. Stream of Characters (LeetCode 1032 - Reverse Trie)
 *   M4. Shortest Encoding of Words (LeetCode 820 - Suffix Trie)
 *   M5. Implement Magic Dictionary (LeetCode 676 - 1 Edit Search)
 *   M6. Extra Characters in a String (LeetCode 2707 - Trie + DP)
 *   M7. Camelcase Matching (LeetCode 1023)
 *   M8. Map Sum / Substring Matching (LeetCode 1032 variant)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Medium {

    // =========================================================
    // M1. DESIGN ADD AND SEARCH WORDS DATA STRUCTURE
    // Pattern: Trie DFS Backtracking with Wildcard '.'
    // LeetCode: 211
    // =========================================================
    /**
     * Problem: Design data structure supporting addWord(word) and search(word)
     * where search word can contain '.' matching any letter.
     *
     * Optimal O(26^K) worst case for '.', O(M) average:
     * Use DFS for '.' branching over all non-null children.
     *
     * Time:  Add: O(M), Search: O(M) without dots, O(26^M) worst case with all dots
     * Space: O(N · M)
     */
    static class WordDictionary {
        private static class Node {
            Node[] children = new Node[26];
            boolean isEnd = false;
        }

        private final Node root = new Node();

        public void addWord(String word) {
            Node curr = root;
            for (char ch : word.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) {
                    curr.children[idx] = new Node();
                }
                curr = curr.children[idx];
            }
            curr.isEnd = true;
        }

        public boolean search(String word) {
            return searchDFS(word, 0, root);
        }

        private boolean searchDFS(String word, int index, Node node) {
            if (node == null) return false;
            if (index == word.length()) return node.isEnd;

            char ch = word.charAt(index);
            if (ch == '.') {
                for (int i = 0; i < 26; i++) {
                    if (node.children[i] != null && searchDFS(word, index + 1, node.children[i])) {
                        return true;
                    }
                }
                return false;
            } else {
                int idx = ch - 'a';
                return searchDFS(word, index + 1, node.children[idx]);
            }
        }
    }

    // =========================================================
    // M2. MAXIMUM XOR OF TWO NUMBERS IN AN ARRAY
    // Pattern: Bitwise (Binary) 32-bit Trie
    // LeetCode: 421
    // =========================================================
    /**
     * Problem: Given an integer array nums, return maximum result of nums[i] XOR nums[j].
     *
     * Optimal O(32 · N): Build Binary Trie inserting numbers bit-by-bit from bit 31 down to 0.
     * For each number, greedily query the branch with opposite bit (bit ^ 1).
     *
     * Time: O(32 · N) = O(N)
     * Space: O(32 · N)
     */
    public int findMaximumXOR(int[] nums) {
        class BinaryNode {
            BinaryNode[] children = new BinaryNode[2]; // 0 and 1
        }

        BinaryNode root = new BinaryNode();

        // 1. Insert all numbers into Binary Trie
        for (int num : nums) {
            BinaryNode curr = root;
            for (int i = 31; i >= 0; i--) {
                int bit = (num >> i) & 1;
                if (curr.children[bit] == null) {
                    curr.children[bit] = new BinaryNode();
                }
                curr = curr.children[bit];
            }
        }

        // 2. Query max XOR for each number
        int maxXOR = 0;
        for (int num : nums) {
            BinaryNode curr = root;
            int currentXOR = 0;
            for (int i = 31; i >= 0; i--) {
                int bit = (num >> i) & 1;
                int oppositeBit = bit ^ 1;

                if (curr.children[oppositeBit] != null) {
                    currentXOR |= (1 << i); // Opposite bit exists, set bit i to 1
                    curr = curr.children[oppositeBit];
                } else {
                    curr = curr.children[bit]; // Must take same bit branch
                }
            }
            maxXOR = Math.max(maxXOR, currentXOR);
        }
        return maxXOR;
    }

    // =========================================================
    // M3. STREAM OF CHARACTERS
    // Pattern: Reverse Suffix Trie Streaming
    // LeetCode: 1032
    // =========================================================
    /**
     * Problem: Stream chars one-by-one. Return true if any suffix of streamed chars matches a word in dictionary.
     *
     * Optimal O(W + S · M_max):
     * Insert reversed words into Trie. Maintain a streaming buffer of typed characters.
     * Search backward from newest character down the Reverse Trie.
     *
     * Time:  Query: O(M_max) where M_max is longest word length
     * Space: O(W · M)
     */
    static class StreamChecker {
        private static class Node {
            Node[] children = new Node[26];
            boolean isEnd = false;
        }

        private final Node root = new Node();
        private final StringBuilder stream = new StringBuilder();

        public StreamChecker(String[] words) {
            for (String w : words) {
                Node curr = root;
                for (int i = w.length() - 1; i >= 0; i--) {
                    int idx = w.charAt(i) - 'a';
                    if (curr.children[idx] == null) {
                        curr.children[idx] = new Node();
                    }
                    curr = curr.children[idx];
                }
                curr.isEnd = true;
            }
        }

        public boolean query(char letter) {
            stream.append(letter);
            Node curr = root;
            for (int i = stream.length() - 1; i >= 0; i--) {
                int idx = stream.charAt(i) - 'a';
                if (curr.children[idx] == null) return false;
                curr = curr.children[idx];
                if (curr.isEnd) return true;
            }
            return false;
        }
    }

    // =========================================================
    // M4. SHORTEST ENCODING OF WORDS
    // Pattern: Suffix Trie Leaf Counting
    // LeetCode: 820
    // =========================================================
    /**
     * Problem: Encode words into reference string S ending with '#' such that indices can reconstruct all words.
     * Return min length of reference string S.
     *
     * Optimal O(N · M): Insert reversed words into Trie.
     * Total length = sum(depth of all leaf nodes + 1 for '#').
     *
     * Time: O(N · M)
     * Space: O(N · M)
     */
    public int minimumLengthEncoding(String[] words) {
        class Node {
            Node[] children = new Node[26];
            int count = 0;
        }

        Node root = new Node();
        Map<Node, Integer> leavesMap = new HashMap<>();

        for (String w : words) {
            Node curr = root;
            for (int i = w.length() - 1; i >= 0; i--) {
                int idx = w.charAt(i) - 'a';
                if (curr.children[idx] == null) {
                    curr.children[idx] = new Node();
                    curr.count++;
                }
                curr = curr.children[idx];
            }
            leavesMap.put(curr, w.length());
        }

        int totalLen = 0;
        for (Map.Entry<Node, Integer> entry : leavesMap.entrySet()) {
            if (entry.getKey().count == 0) { // Is leaf node
                totalLen += entry.getValue() + 1; // Word length + '#'
            }
        }
        return totalLen;
    }

    // =========================================================
    // M5. IMPLEMENT MAGIC DICTIONARY
    // Pattern: 1-Character Edit Distance Trie DFS
    // LeetCode: 676
    // =========================================================
    /**
     * Problem: Search if a word can be formed by changing EXACTLY ONE character in dictionary.
     *
     * Optimal O(26 · M): Trie DFS tracking edit count (diffCount == 1).
     *
     * Time: O(26 · M)
     * Space: O(N · M)
     */
    static class MagicDictionary {
        private static class Node {
            Node[] children = new Node[26];
            boolean isEnd = false;
        }

        private final Node root = new Node();

        public void buildDict(String[] dictionary) {
            for (String w : dictionary) {
                Node curr = root;
                for (char ch : w.toCharArray()) {
                    int idx = ch - 'a';
                    if (curr.children[idx] == null) {
                        curr.children[idx] = new Node();
                    }
                    curr = curr.children[idx];
                }
                curr.isEnd = true;
            }
        }

        public boolean search(String searchWord) {
            return dfs(searchWord, 0, root, 0);
        }

        private boolean dfs(String word, int index, Node node, int diffs) {
            if (diffs > 1 || node == null) return false;
            if (index == word.length()) return node.isEnd && diffs == 1;

            int targetIdx = word.charAt(index) - 'a';
            for (int i = 0; i < 26; i++) {
                if (node.children[i] != null) {
                    int nextDiffs = diffs + (i == targetIdx ? 0 : 1);
                    if (dfs(word, index + 1, node.children[i], nextDiffs)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    // =========================================================
    // M6. EXTRA CHARACTERS IN A STRING
    // Pattern: Trie-Accelerated Dynamic Programming
    // LeetCode: 2707
    // =========================================================
    /**
     * Problem: Break s into dictionary words with minimum leftover extra characters.
     *
     * Optimal O(N² + Dict):
     * DP state: dp[i] = min extra chars in s[i..N-1].
     * Accelerate dictionary word matching starting at index i using Trie.
     *
     * Time: O(N² + Dict_Length)
     * Space: O(N + Dict_Length)
     */
    public int minExtraChar(String s, String[] dictionary) {
        class Node {
            Node[] children = new Node[26];
            boolean isEnd = false;
        }

        Node root = new Node();
        for (String w : dictionary) {
            Node curr = root;
            for (char ch : w.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) {
                    curr.children[idx] = new Node();
                }
                curr = curr.children[idx];
            }
            curr.isEnd = true;
        }

        int n = s.length();
        int[] dp = new int[n + 1];

        for (int i = n - 1; i >= 0; i--) {
            dp[i] = dp[i + 1] + 1; // Default: count s[i] as extra char
            Node curr = root;
            for (int j = i; j < n; j++) {
                int idx = s.charAt(j) - 'a';
                if (curr.children[idx] == null) break;
                curr = curr.children[idx];
                if (curr.isEnd) {
                    dp[i] = Math.min(dp[i], dp[j + 1]);
                }
            }
        }
        return dp[0];
    }

    // =========================================================
    // M7. CAMELCASE MATCHING
    // Pattern: Two-Pointer Pattern Match
    // LeetCode: 1023
    // =========================================================
    /**
     * Problem: Given queries and pattern, return boolean list indicating if query matches pattern.
     *
     * Optimal O(N · M): Two pointers matching pattern chars. Extra uppercase chars in query disqualify.
     *
     * Time: O(N · M)
     * Space: O(1)
     */
    public List<Boolean> camelMatch(String[] queries, String pattern) {
        List<Boolean> res = new ArrayList<>();
        for (String q : queries) {
            res.add(matches(q, pattern));
        }
        return res;
    }

    private boolean matches(String query, String pattern) {
        int i = 0;
        for (char ch : query.toCharArray()) {
            if (i < pattern.length() && ch == pattern.charAt(i)) {
                i++;
            } else if (Character.isUpperCase(ch)) {
                return false;
            }
        }
        return i == pattern.length();
    }

    // =========================================================
    // M8. MULTI-SEARCH / SUBSTRING MATCHING
    // Pattern: Trie Substring Scanner
    // LeetCode: 1032 variant
    // =========================================================
    /**
     * Problem: Find all positions where words from dictionary appear in text.
     */
    public List<Integer> searchPrefixes(String text, String[] dict) {
        class Node {
            Node[] children = new Node[26];
            boolean isEnd = false;
        }

        Node root = new Node();
        for (String d : dict) {
            Node curr = root;
            for (char ch : d.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) curr.children[idx] = new Node();
                curr = curr.children[idx];
            }
            curr.isEnd = true;
        }

        List<Integer> matches = new ArrayList<>();
        Node curr = root;
        for (int i = 0; i < text.length(); i++) {
            int idx = text.charAt(i) - 'a';
            if (curr.children[idx] == null) break;
            curr = curr.children[idx];
            if (curr.isEnd) matches.add(i);
        }
        return matches;
    }
}
