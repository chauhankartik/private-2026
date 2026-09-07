/**
 * ============================================================
 *  GOOGLE-LEVEL HASHING PROBLEMS
 *
 *  These problems are representative of actual Google interview
 *  questions. Each problem is designed to test MULTIPLE topics
 *  simultaneously and require system-level thinking.
 *
 *  Problems:
 *   G1. Design In-Memory File System (HashMap + Trie)
 *   G2. Random Pick with Blacklist (HashMap + Math)
 *   G3. Substring with Concatenation of All Words (Sliding Window + HashMap)
 *   G4. Stream Checker / Spell Check (HashMap + Trie / Hashing)
 *   G5. Minimum Operations to Reduce X to Zero (Prefix Sum + HashMap ↔ Sliding Window)
 *   G6. Count Subarrays Where Max Element Appears at Least K Times (HashMap/Stack variant)
 *
 *  Multi-topic matrix:
 *   G1: HashMap + Trie + OOP Design
 *   G2: HashMap + Math + Randomization
 *   G3: HashMap + Sliding Window + String
 *   G4: HashMap + Trie + Stream processing
 *   G5: Prefix Sum + HashMap + Two-pointer duality
 *   G6: Monotonic Stack + HashMap + Sliding Window
 * ============================================================
 */
import java.util.*;

public class GoogleLevel {

    // =========================================================
    // G1. DESIGN IN-MEMORY FILE SYSTEM
    // Topics: HashMap + Trie + OOP Design
    // =========================================================
    /**
     * Problem: Implement:
     *   ls(path)         — list files/dirs at path
     *   mkdir(path)      — create directory (and parents)
     *   addContentToFile(path, content) — create/append
     *   readContentFromFile(path)       — read file
     *
     * Design insight: Model as a TRIE where each node is a directory.
     *   HashMap<name, Node> for O(1) child lookup.
     *   Separate content field for files (null = directory).
     *
     * Topics tested: OOP design, Trie traversal, HashMap usage,
     *                string parsing, edge case handling.
     *
     * Time per operation: O(L) where L = path length
     * Space: O(total characters stored)
     */
    class FileSystem {
        private class Dir {
            Map<String, Dir> children = new TreeMap<>(); // sorted for ls()
            String content = null;
            boolean isFile = false;
        }

        private final Dir root = new Dir();

        public List<String> ls(String path) {
            Dir node = traverse(path);
            if (node.isFile) {
                // If path points to a file, return just the filename
                String[] parts = path.split("/");
                return Collections.singletonList(parts[parts.length - 1]);
            }
            return new ArrayList<>(node.children.keySet()); // sorted by TreeMap
        }

        public void mkdir(String path) {
            traverse(path); // creates nodes along the way
        }

        public void addContentToFile(String filePath, String content) {
            Dir node = traverse(filePath);
            node.isFile = true;
            node.content = (node.content == null ? "" : node.content) + content;
        }

        public String readContentFromFile(String filePath) {
            return traverse(filePath).content;
        }

        private Dir traverse(String path) {
            Dir cur = root;
            for (String part : path.split("/")) {
                if (part.isEmpty()) continue; // leading "/"
                cur.children.putIfAbsent(part, new Dir());
                cur = cur.children.get(part);
            }
            return cur;
        }
    }

    /**
     * Google Follow-ups (distinguish you from other candidates):
     *
     * 1. How would you handle concurrent reads and writes?
     *    → ReadWriteLock (ReentrantReadWriteLock) at node level.
     *    → Lock striping for better throughput.
     *
     * 2. How to support deletion?
     *    → Recursive delete (rm -rf) or single file delete.
     *    → Remove from parent's children map. GC handles memory.
     *
     * 3. How to make it persistent (survive restarts)?
     *    → Serialize trie to disk (JSON/protobuf).
     *    → Write-ahead log (WAL) for crash recovery.
     *
     * 4. How to scale to 1 billion files?
     *    → Distributed inode tables (like HDFS NameNode).
     *    → Consistent hashing to shard by directory prefix.
     */

    // =========================================================
    // G2. RANDOM PICK WITH BLACKLIST
    // Topics: HashMap + Math + Remap Strategy
    // =========================================================
    /**
     * Problem: Given n integers [0, n-1] and a blacklist B, design a class to
     * return a uniform random integer from the whitelist (non-blacklisted).
     *
     * Constraints: The pick() method must run in O(1) time.
     *
     * Naive: Store all whitelist elements → O(n) space. Not acceptable.
     *
     * Key insight — REMAPPING:
     *   Whitelist size = n - |B| = sz.
     *   Conceptually, we want to pick uniformly from [0, sz-1].
     *   - If the picked value [0, sz-1] is NOT blacklisted → return directly.
     *   - If it IS blacklisted → remap it to a whitelist element in [sz, n-1].
     *
     *   Build a map: {blacklisted value in [0,sz-1]} → {whitelist value in [sz,n-1]}
     *
     * Time: O(|B|) constructor, O(1) pick.
     * Space: O(|B|)
     *
     * Topics: Remapping strategy, probability/randomization reasoning.
     */
    class RandomPickWithBlacklist {
        private final Map<Integer, Integer> remap;
        private final int sz;
        private final Random rand = new Random();

        public RandomPickWithBlacklist(int n, int[] blacklist) {
            Set<Integer> blackSet = new HashSet<>();
            for (int b : blacklist) blackSet.add(b);
            sz = n - blacklist.length;

            remap = new HashMap<>();
            // Collect whitelist elements in [sz, n-1]
            int m = sz;
            for (int b : blacklist) {
                if (b < sz) { // only remap blacklisted elements in the "pick range"
                    while (blackSet.contains(m)) m++; // find next valid in [sz, n-1]
                    remap.put(b, m++);
                }
            }
        }

        public int pick() {
            int v = rand.nextInt(sz);
            return remap.getOrDefault(v, v);
        }
    }

    /**
     * Follow-up 1: What if the same pick() is called millions of times?
     *   → Cache the random sequence for batch generation (thread-safe).
     *
     * Follow-up 2: Prove the distribution is uniform.
     *   → sz eligible values, each with probability 1/sz from nextInt(sz).
     *   → Blacklisted values are remapped 1-to-1 to whitelist values.
     *   → Each whitelist value still has exactly one "source" → uniform.
     *
     * Follow-up 3: Dynamic blacklist updates?
     *   → Rebuild remap on each update. O(|B|) per update.
     *   → For frequent updates: maintain augmented BST (order statistics tree).
     */

    // =========================================================
    // G3. SUBSTRING WITH CONCATENATION OF ALL WORDS
    // Topics: Sliding Window + HashMap + String Hashing
    // =========================================================
    /**
     * Problem: Find all start indices in s where s[i..i+total_len-1] is a
     * concatenation of ALL words in the given list (each used exactly once).
     *
     * Key challenge: words can repeat in the list; order doesn't matter.
     *
     * Algorithm:
     *   - Word length is fixed (call it wLen). Total window = words.length * wLen.
     *   - Instead of sliding character by character, slide WORD by WORD.
     *   - Run wLen separate sliding window passes (offsets 0..wLen-1).
     *
     * Time:  O(n * wLen) where n = s.length, wLen = word length
     *   → Each of the wLen passes is O(n/wLen * wLen) = O(n)
     * Space: O(words.length) — frequency maps
     *
     * Topics: Sliding window, frequency maps, offset-based partitioning.
     */
    public List<Integer> findSubstring(String s, String[] words) {
        List<Integer> result = new ArrayList<>();
        if (s == null || s.isEmpty() || words == null || words.length == 0) return result;

        int wLen = words[0].length(), wCount = words.length, total = wLen * wCount;
        if (s.length() < total) return result;

        Map<String, Integer> wordFreq = new HashMap<>();
        for (String w : words) wordFreq.merge(w, 1, Integer::sum);

        // Run sliding window for each alignment offset
        for (int i = 0; i < wLen; i++) {
            Map<String, Integer> windowFreq = new HashMap<>();
            int left = i, matched = 0;

            for (int right = i; right + wLen <= s.length(); right += wLen) {
                String word = s.substring(right, right + wLen);

                if (wordFreq.containsKey(word)) {
                    windowFreq.merge(word, 1, Integer::sum);
                    if (windowFreq.get(word).equals(wordFreq.get(word))) matched++;

                    // Shrink if over-count or window too large
                    while (windowFreq.get(word) > wordFreq.get(word)) {
                        String lw = s.substring(left, left + wLen);
                        if (windowFreq.get(lw).equals(wordFreq.get(lw))) matched--;
                        windowFreq.merge(lw, -1, Integer::sum);
                        left += wLen;
                    }

                    if (matched == wordFreq.size()) result.add(left);
                } else {
                    // Invalid word: reset window
                    windowFreq.clear();
                    matched = 0;
                    left = right + wLen;
                }
            }
        }
        return result;
    }

    /**
     * Follow-up 1: Words of different lengths?
     *   → Problem becomes much harder. Need DP or suffix automaton.
     *
     * Follow-up 2: Any permutation of any substring (not full s)?
     *   → Reduce to "Find All Anagrams" with word-level tokenization.
     *
     * Follow-up 3: What's the bottleneck for very long s with many unique words?
     *   → The inner string creation (substring). Use hash-based word IDs.
     */

    // =========================================================
    // G4. MINIMUM OPERATIONS TO REDUCE X TO ZERO
    // Topics: Prefix Sum + HashMap ↔ Sliding Window Duality
    // =========================================================
    /**
     * Problem: Given nums and x, find minimum number of operations to reduce x to 0
     * by removing elements from LEFT or RIGHT of the array. Return -1 if impossible.
     *
     * Key insight (THE TRANSFORMATION):
     *   Instead of minimizing elements removed from ends,
     *   MAXIMIZE the length of a subarray in the MIDDLE with sum = total - x.
     *
     *   Why? Sum of all elements = total.
     *   If center subarray sums to (total - x), then the left+right parts sum to x.
     *   Maximize center = minimize ends (what we remove).
     *
     * This reduces the problem to: "Longest subarray with sum = target"
     * → Solved by sliding window (since all nums > 0 — monotonic sum property).
     *
     * Time:  O(n)
     * Space: O(1)  ← sliding window (not prefix sum map) since nums >= 0
     *
     * Topics: Problem transformation, sliding window for positive arrays,
     *         thinking "complement of the answer".
     */
    public int minOperations(int[] nums, int x) {
        int total = 0;
        for (int n : nums) total += n;
        int target = total - x;

        if (target < 0) return -1;
        if (target == 0) return nums.length;

        int maxLen = -1, sum = 0, left = 0;
        for (int right = 0; right < nums.length; right++) {
            sum += nums[right];
            while (sum > target) sum -= nums[left++]; // valid since nums > 0
            if (sum == target) maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen == -1 ? -1 : nums.length - maxLen;
    }

    /**
     * Follow-up 1: What if nums can be negative?
     *   → Sliding window no longer works (sum isn't monotonic).
     *   → Use prefix sum + HashMap: {sum → index}.
     *   → O(n) time, O(n) space.
     *
     * Follow-up 2: What if we want the actual elements removed?
     *   → Track the split point: store the left index and right index.
     *   → Reverse-engineer which prefix + suffix give the answer.
     *
     * Follow-up 3: K operations (remove from k different positions)?
     *   → Completely different: DP with bitmask or greedy with priority queue.
     */

    // =========================================================
    // G5. MAXIMUM POINTS YOU CAN OBTAIN FROM CARDS
    // Topics: Sliding Window + HashMap (Complement Thinking)
    // =========================================================
    /**
     * Problem: Given cardPoints[], pick exactly k cards from left and/or right
     * to maximize total points.
     *
     * This is a classic Google problem testing whether you see the
     * "complement" insight:
     *   Total cards = n. We pick k from ends → we LEAVE n-k consecutive cards in the middle.
     *   Maximize picked = Maximize total - Minimize middle window of size (n-k).
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * Topics tested: complement thinking, sliding window, linear scan.
     */
    public int maxScore(int[] cardPoints, int k) {
        int n = cardPoints.length;
        int total = 0;
        for (int p : cardPoints) total += p;

        int windowSize = n - k;
        if (windowSize == 0) return total;

        // Find minimum sum subarray of size windowSize
        int windowSum = 0;
        for (int i = 0; i < windowSize; i++) windowSum += cardPoints[i];
        int minWindowSum = windowSum;

        for (int i = windowSize; i < n; i++) {
            windowSum += cardPoints[i] - cardPoints[i - windowSize];
            minWindowSum = Math.min(minWindowSum, windowSum);
        }
        return total - minWindowSum;
    }

    /**
     * Follow-up 1: What if we can pick from any position (not just ends)?
     *   → Then it's a simple top-k sum problem (sort, take largest k).
     *
     * Follow-up 2: What if some cards have negative values?
     *   → Same algorithm still works (minimize window sum handles negatives).
     *   → The window minimum could be negative → we maximize the "remainder".
     *
     * Follow-up 3: You can pick from at most k positions (not exactly k)?
     *   → O(k²) DP: pick i from left, j from right, i+j ≤ k.
     */

    // =========================================================
    // G6. LONGEST SUBARRAY WITH MAXIMUM FREQUENCY
    // Topics: HashMap + Frequency Reasoning
    // =========================================================
    /**
     * Problem: Find the length of the longest subarray where the maximum element
     * appears at least K times.
     *
     * Strategy:
     *   - Find maxElement = max(nums).
     *   - Problem reduces to: longest subarray where maxElement appears >= k times.
     *   - Sliding window: track count of maxElement in window.
     *     Expand right, shrink left when count >= k (we just need first valid window).
     *
     * Wait — we want LONGEST, so we only shrink when necessary.
     *   Actually: expand right always, only expand left when count < k. 
     *   This is the "minimum window" approach adapted to "longest" via monotone window.
     *
     * Time:  O(n)
     * Space: O(1)
     */
    public long countSubarrays(int[] nums, int k) {
        int maxElement = Arrays.stream(nums).max().getAsInt();
        long result = 0;
        int count = 0, left = 0;

        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == maxElement) count++;

            while (count >= k) {
                result += nums.length - right;   // all extensions of this window are valid
                if (nums[left] == maxElement) count--;
                left++;
            }
        }
        return result;
    }

    /**
     * Follow-up 1: Count subarrays where EACH element appears exactly once?
     *   → atMost(k distinct) pattern.
     *
     * Follow-up 2: Subarrays where the most frequent element has frequency >= k
     *   AND meets some additional constraint?
     *   → Combine frequency map with monotone deque.
     *
     * Follow-up 3: Same problem on a CIRCULAR array?
     *   → Double the array (or use modular indexing), apply same algorithm.
     */
}
