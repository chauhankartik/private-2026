/**
 * ============================================================
 *  HASHING — HARD PROBLEMS
 *  Each problem requires combining hashing with advanced
 *  algorithmic thinking and multi-pattern reasoning.
 * ============================================================
 *
 *  Problems:
 *   H1. Minimum Window Substring
 *   H2. LRU Cache (HashMap + Doubly Linked List)
 *   H3. Longest Substring with At Most K Distinct Characters
 *   H4. Subarrays with K Different Integers
 *   H5. Palindrome Pairs (Hash Map approach)
 *   H6. Number of Distinct Substrings (Rolling Hash / Rabin-Karp)
 *
 * ============================================================
 */
public class Hard {

    // =========================================================
    // H1. MINIMUM WINDOW SUBSTRING
    // Pattern: Sliding Window + Frequency Map (Two Maps)
    // =========================================================
    /**
     * Problem: Find the smallest window in s containing all characters of t
     * (including duplicates). Return "" if no such window exists.
     *
     * Algorithm — "formed" counter trick:
     *   - Maintain tFreq (required) and wFreq (window frequency).
     *   - Track `formed` = number of unique chars in t that are SATISFIED
     *     (wFreq[c] >= tFreq[c]) in the current window.
     *   - When formed == required, try to shrink from left.
     *
     * Why is this O(n)?
     *   Each character is added to the window (right++) and removed (left++)
     *   at most once. Total: 2n operations → O(n).
     *
     * Time:  O(|s| + |t|)
     * Space: O(|t| + σ) where σ = distinct chars in s ∪ t
     */
    public String minWindow(String s, String t) {
        if (s.isEmpty() || t.isEmpty()) return "";

        Map<Character, Integer> tFreq = new HashMap<>();
        for (char c : t.toCharArray()) tFreq.merge(c, 1, Integer::sum);

        int required = tFreq.size();  // unique chars in t we need to satisfy
        int formed = 0;               // unique chars currently satisfied in window

        Map<Character, Integer> wFreq = new HashMap<>();
        int left = 0, minLen = Integer.MAX_VALUE, minLeft = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            wFreq.merge(c, 1, Integer::sum);

            // Check if this char just became "satisfied"
            if (tFreq.containsKey(c) && wFreq.get(c).equals(tFreq.get(c))) {
                formed++;
            }

            // Try to shrink window from left
            while (left <= right && formed == required) {
                if (right - left + 1 < minLen) {
                    minLen = right - left + 1;
                    minLeft = left;
                }
                char lc = s.charAt(left);
                wFreq.merge(lc, -1, Integer::sum);
                if (tFreq.containsKey(lc) && wFreq.get(lc) < tFreq.get(lc)) {
                    formed--;
                }
                left++;
            }
        }
        return minLen == Integer.MAX_VALUE ? "" : s.substring(minLeft, minLeft + minLen);
    }

    /**
     * Follow-up 1: What if s has 10^6 chars but t only has 10^2 distinct chars?
     *   → Optimized: Filter s to only include positions with chars in t.
     *   → Reduces effective length from |s| to |filtered| in the window walk.
     *
     * Follow-up 2: All minimum windows (not just one)?
     *   → Collect all windows of same minimum length.
     *   → Requires a second pass or tracking all minima.
     *
     * Follow-up 3: Minimum window subsequence (not substring)?
     *   → Two-pointer approach. Harder — DP or two directional scans.
     *   → LeetCode 727 (Hard).
     */

    // =========================================================
    // H2. LRU CACHE
    // Pattern: HashMap + Doubly Linked List (O(1) everything)
    // =========================================================
    /**
     * Problem: Design a data structure with O(1) get and put, evicting the
     * Least Recently Used item when capacity is exceeded.
     *
     * Design:
     *   - HashMap<key, Node>: O(1) access by key
     *   - Doubly Linked List: maintains usage order
     *     → Most recently used: near HEAD
     *     → Least recently used: near TAIL
     *   - On get: move node to head (O(1) with DLL)
     *   - On put: add at head; if over capacity, remove from tail
     *
     * Why not use LinkedHashMap directly?
     *   You can! But interviewers want to see you understand the internals.
     *   Java's LinkedHashMap with accessOrder=true does exactly this.
     *
     * Time:  O(1) for both get and put
     * Space: O(capacity)
     */
    class LRUCache {
        private class Node {
            int key, val;
            Node prev, next;
            Node(int k, int v) { key = k; val = v; }
        }

        private final int capacity;
        private final Map<Integer, Node> map;
        private final Node head, tail; // dummy sentinel nodes

        public LRUCache(int capacity) {
            this.capacity = capacity;
            map = new HashMap<>();
            head = new Node(0, 0);
            tail = new Node(0, 0);
            head.next = tail;
            tail.prev = head;
        }

        public int get(int key) {
            if (!map.containsKey(key)) return -1;
            Node node = map.get(key);
            moveToHead(node);
            return node.val;
        }

        public void put(int key, int value) {
            if (map.containsKey(key)) {
                Node node = map.get(key);
                node.val = value;
                moveToHead(node);
            } else {
                Node node = new Node(key, value);
                map.put(key, node);
                addToHead(node);
                if (map.size() > capacity) {
                    Node lru = removeTail();
                    map.remove(lru.key);
                }
            }
        }

        private void addToHead(Node node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
        }

        private void removeNode(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private void moveToHead(Node node) {
            removeNode(node);
            addToHead(node);
        }

        private Node removeTail() {
            Node lru = tail.prev;
            removeNode(lru);
            return lru;
        }
    }

    /**
     * LinkedHashMap one-liner version (know both; explain why DLL version for interviews):
     */
    class LRUCacheSimple extends LinkedHashMap<Integer, Integer> {
        private final int capacity;
        public LRUCacheSimple(int capacity) {
            super(capacity, 0.75f, true); // accessOrder = true
            this.capacity = capacity;
        }
        public int get(int key) { return super.getOrDefault(key, -1); }
        public void put(int key, int value) { super.put(key, value); }
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
            return size() > capacity;
        }
    }

    /**
     * Follow-up 1: LFU Cache (Least Frequently Used)?
     *   → Map: key→(value, freq)  +  Map: freq→LinkedHashSet of keys
     *   → Track minFreq. O(1) per operation.
     *   → LeetCode 460 (Hard).
     *
     * Follow-up 2: Thread-safe LRU?
     *   → Wrap with synchronized or use ConcurrentHashMap + explicit locking.
     *   → Or use a concurrent skip list for the ordering.
     *
     * Follow-up 3: Distributed LRU across machines?
     *   → Redis with TTL + LRU eviction policy. System design territory.
     */

    // =========================================================
    // H3. SUBARRAYS WITH K DIFFERENT INTEGERS
    // Pattern: Sliding Window (Exact = AtMost(k) - AtMost(k-1))
    // =========================================================
    /**
     * Problem: Count subarrays with exactly k distinct integers.
     *
     * Direct sliding window for "exactly k" is tricky because shrinking
     * the window isn't monotonic.
     *
     * The genius trick: Exact(k) = AtMost(k) - AtMost(k-1)
     *
     * Why this works:
     *   Count subarrays with AT MOST k distinct = f(k)
     *   Count subarrays with AT MOST k-1 distinct = f(k-1)
     *   Their difference = subarrays with EXACTLY k distinct.
     *
     * AtMost(k) is easy: classic sliding window.
     *   - Expand right, shrink left until distinct <= k.
     *   - Each valid right adds (right - left + 1) subarrays ending at right.
     *
     * Time:  O(n)
     * Space: O(n)  — frequency map for window
     */
    public int subarraysWithKDistinct(int[] nums, int k) {
        return atMost(nums, k) - atMost(nums, k - 1);
    }

    private int atMost(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        int left = 0, count = 0;
        for (int right = 0; right < nums.length; right++) {
            freq.merge(nums[right], 1, Integer::sum);
            while (freq.size() > k) {
                int lv = nums[left++];
                freq.merge(lv, -1, Integer::sum);
                if (freq.get(lv) == 0) freq.remove(lv);
            }
            count += right - left + 1; // all subarrays ending at right, starting at left..right
        }
        return count;
    }

    /**
     * Follow-up: Return all such subarrays (not count)?
     *   → Collect [left, right] pairs during the exact-k matching.
     *   → Requires two-pointer adaptation to track exact boundaries.
     *
     * Follow-up: What if we need sum of lengths instead of count?
     *   → Replace count += (right - left + 1) with length accumulator.
     */

    // =========================================================
    // H4. PALINDROME PAIRS
    // Pattern: HashMap + String Reversal Logic
    // =========================================================
    /**
     * Problem: Given a list of unique words, find all pairs (i,j) such that
     * words[i] + words[j] is a palindrome.
     *
     * Cases for words[i] + words[j] to be palindrome:
     *   Case 1: Reverse of words[j] == words[i] (equal length)
     *   Case 2: words[j] is empty (words[i] itself is palindrome)
     *   Case 3: The LEFT prefix of words[i] is palindrome AND
     *            reverse of its right part equals words[j]
     *   Case 4: The RIGHT suffix of words[i] is palindrome AND
     *            reverse of its left part equals words[j]
     *
     * For each word, try all splits and look up the complement in the map.
     *
     * Time:  O(n · k²)  where k = average word length (k splits, each O(k) check)
     * Space: O(n · k)   — storing all words in map
     */
    public List<List<Integer>> palindromePairs(String[] words) {
        Map<String, Integer> wordIndex = new HashMap<>();
        for (int i = 0; i < words.length; i++) wordIndex.put(words[i], i);

        List<List<Integer>> result = new ArrayList<>();

        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            int n = w.length();

            for (int cut = 0; cut <= n; cut++) {
                String left = w.substring(0, cut);
                String right = w.substring(cut);

                // Case: left is palindrome, look for reverse(right) as prefix
                if (isPalin(left)) {
                    String revRight = new StringBuilder(right).reverse().toString();
                    if (wordIndex.containsKey(revRight) && wordIndex.get(revRight) != i) {
                        result.add(Arrays.asList(wordIndex.get(revRight), i));
                    }
                }

                // Case: right is palindrome, look for reverse(left) as suffix
                // Exclude cut == n to avoid duplicates with the above
                if (cut != n && isPalin(right)) {
                    String revLeft = new StringBuilder(left).reverse().toString();
                    if (wordIndex.containsKey(revLeft) && wordIndex.get(revLeft) != i) {
                        result.add(Arrays.asList(i, wordIndex.get(revLeft)));
                    }
                }
            }
        }
        return result;
    }

    private boolean isPalin(String s) {
        int l = 0, r = s.length() - 1;
        while (l < r) if (s.charAt(l++) != s.charAt(r--)) return false;
        return true;
    }

    /**
     * Follow-up: Trie-based solution for O(n·k²) worst case with better constants?
     *   → Build a trie of reversed words. For each word's prefix, if the suffix
     *     is a palindrome, a match exists at the trie node.
     *   → Implementation is complex but avoids hash collision edge cases.
     *
     * Follow-up: Case-insensitive palindrome pairs?
     *   → Normalize words to lowercase before building map.
     */

    // =========================================================
    // H5. ROLLING HASH — Rabin-Karp Substring Search
    // Pattern: Polynomial Rolling Hash for O(n) substring matching
    // =========================================================
    /**
     * Rabin-Karp: Find all occurrences of pattern p in text t.
     *
     * Rolling Hash formula:
     *   hash(s[0..k-1]) = s[0]·B^(k-1) + s[1]·B^(k-2) + ... + s[k-1]·B^0  (mod MOD)
     *
     * Rolling update (slide window right by 1):
     *   hash(s[1..k]) = (hash(s[0..k-1]) - s[0]·B^(k-1)) · B + s[k]  (mod MOD)
     *
     * Why is this O(n + m)?
     *   - Computing initial hash: O(m)
     *   - Rolling n-m+1 windows: O(1) per window → O(n-m) total
     *   - String equality check on hash match: O(m) but rare (avg O(1))
     *   - Total: O(n + m)
     *
     * Time:  O(n + m) average, O(nm) worst (hash collisions)
     * Space: O(1)
     *
     * This is the foundation of suffix arrays and string matching at Google scale.
     */
    public List<Integer> rabinKarp(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (n < m) return result;

        final long MOD = 1_000_000_007L;
        final long B = 31L;  // base

        // Precompute B^(m-1) mod MOD
        long power = 1;
        for (int i = 0; i < m - 1; i++) power = power * B % MOD;

        // Compute pattern hash and initial window hash
        long pHash = 0, wHash = 0;
        for (int i = 0; i < m; i++) {
            pHash = (pHash * B + (pattern.charAt(i) - 'a' + 1)) % MOD;
            wHash = (wHash * B + (text.charAt(i) - 'a' + 1)) % MOD;
        }

        if (pHash == wHash && text.substring(0, m).equals(pattern)) result.add(0);

        for (int i = m; i < n; i++) {
            // Roll: remove leftmost, add rightmost
            wHash = (wHash - (text.charAt(i - m) - 'a' + 1) * power % MOD + MOD) % MOD;
            wHash = (wHash * B + (text.charAt(i) - 'a' + 1)) % MOD;

            if (wHash == pHash && text.substring(i - m + 1, i + 1).equals(pattern)) {
                result.add(i - m + 1);
            }
        }
        return result;
    }

    // =========================================================
    // H6. LONGEST DUPLICATE SUBSTRING (Binary Search + Rolling Hash)
    // Pattern: Binary Search on answer length + Rabin-Karp validation
    // =========================================================
    /**
     * Problem: Find longest substring that appears at least twice in s.
     *
     * Key insight: Binary search on the LENGTH of the answer.
     *   - If a substring of length k appears twice, one of length k-1 also does.
     *   - So: binary search on length, use rolling hash to check existence.
     *
     * Check(k): "Does any substring of length k appear at least twice?"
     *   → Compute rolling hash for all length-k windows.
     *   → If any two windows have the same hash → likely duplicate.
     *   → Verify with actual string comparison (anti-collision).
     *
     * Time:  O(n log n) — log n binary search steps × O(n) per check
     * Space: O(n)       — HashSet of window hashes
     */
    public String longestDupSubstring(String s) {
        int lo = 1, hi = s.length() - 1;
        String result = "";

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            String dup = getDuplicate(s, mid);
            if (dup != null) {
                result = dup;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return result;
    }

    private String getDuplicate(String s, int len) {
        final long MOD = (1L << 61) - 1; // Mersenne prime
        final long B = 31L;

        long power = 1;
        for (int i = 0; i < len - 1; i++) power = multiply(power, B, MOD);

        Set<Long> seen = new HashSet<>();
        long hash = 0;

        for (int i = 0; i < len; i++) hash = (multiply(hash, B, MOD) + s.charAt(i)) % MOD;
        seen.add(hash);

        for (int i = len; i < s.length(); i++) {
            hash = (multiply(hash - multiply(s.charAt(i - len), power, MOD) + MOD, B, MOD)
                    + s.charAt(i)) % MOD;
            if (seen.contains(hash)) return s.substring(i - len + 1, i + 1);
            seen.add(hash);
        }
        return null;
    }

    private long multiply(long a, long b, long mod) {
        return (a % mod) * (b % mod) % mod;
    }

    /**
     * Follow-up: What if we need ALL duplicate substrings, not just longest?
     *   → Suffix array + LCP array gives all in O(n log n) time.
     *   → More complex but optimal for exhaustive queries.
     *
     * Follow-up: How do you choose a good hash base and modulus?
     *   → Mersenne primes (2^61 - 1) reduce collision probability.
     *   → Using TWO hash functions (double hashing) reduces collisions to ~1/MOD².
     */
}
