/**
 * ============================================================
 *  HASHING — MEDIUM PROBLEMS
 *  These problems combine hashing with other data structures
 *  or require non-obvious hash key design.
 * ============================================================
 *
 *  Problems:
 *   M1. Group Anagrams
 *   M2. Longest Consecutive Sequence
 *   M3. Top K Frequent Elements
 *   M4. Subarray Sum Equals K (Prefix Sum + Map)
 *   M5. Longest Substring Without Repeating Characters
 *   M6. 4Sum II  (Two-sum on pairs)
 *   M7. Find All Anagrams in a String (Sliding Window)
 *   M8. Contiguous Array (prefix XOR / prefix sum variant)
 *
 * ============================================================
 */
public class Medium {

    // =========================================================
    // M1. GROUP ANAGRAMS
    // Pattern: Grouping / Bucketing by Canonical Key
    // =========================================================
    /**
     * Problem: Group strings that are anagrams of each other.
     *
     * Key Challenge: What should the "key" in the map be?
     *   Option A: Sort each string → use sorted string as key.
     *             O(n · k log k) where k = avg string length.
     *   Option B: Encode character frequencies as key.
     *             O(n · k) — avoids the sort.
     *
     * Why sorting works: All anagrams have the same sorted form.
     *   "eat", "tea", "ate" → all sort to "aet"
     *
     * Time:  O(n · k log k)  — n strings, each sorted in O(k log k)
     * Space: O(n · k)        — storing all strings in the map
     */
    public List<List<String>> groupAnagrams(String[] strs) {
        // computeIfAbsent is the idiomatic Java grouping pattern
        Map<String, List<String>> map = new HashMap<>();
        for (String s : strs) {
            char[] arr = s.toCharArray();
            Arrays.sort(arr);
            String key = new String(arr);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Optimized: O(n·k) frequency encoding as key
     * Instead of sorting, encode as "#a1#b0#c3..." string.
     */
    public List<List<String>> groupAnagramsOptimal(String[] strs) {
        Map<String, List<String>> map = new HashMap<>();
        for (String s : strs) {
            int[] count = new int[26];
            for (char c : s.toCharArray()) count[c - 'a']++;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 26; i++) sb.append('#').append(count[i]);
            String key = sb.toString();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Follow-up 1: What if strs has duplicate strings?
     *   → The algorithm handles it — duplicates go to the same bucket.
     *
     * Follow-up 2: Return groups sorted by size (largest first)?
     *   → Collect values(), sort by list.size() descending.
     *
     * Follow-up 3: Unicode characters?
     *   → Use HashMap<Character, Integer> for frequency, serialize to key string.
     */

    // =========================================================
    // M2. LONGEST CONSECUTIVE SEQUENCE
    // Pattern: HashSet + Smart Starting Point
    // =========================================================
    /**
     * Problem: Find length of longest consecutive sequence in unsorted array.
     * Requirement: O(n) time.
     *
     * Naive: Sort → scan. O(n log n). Not acceptable per problem constraint.
     *
     * Key insight: Only START counting a sequence from its TRUE beginning.
     *   A number n is a sequence start iff (n-1) is NOT in the set.
     *   This ensures each number is visited at most twice total → O(n).
     *
     * Proof of O(n):
     *   - Building the set: O(n)
     *   - Outer loop: iterates n times
     *   - Inner while loop: for numbers that ARE sequence starts,
     *     the total number of inner loop iterations across ALL sequences
     *     equals the TOTAL sequence lengths = n (each element counted once)
     *   - Total: O(n)
     *
     * Time:  O(n)
     * Space: O(n)
     */
    public int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int n : nums) set.add(n);

        int best = 0;
        for (int n : set) {
            if (!set.contains(n - 1)) {  // n is sequence start
                int cur = n, len = 1;
                while (set.contains(cur + 1)) { cur++; len++; }
                best = Math.max(best, len);
            }
        }
        return best;
    }

    /**
     * Follow-up: Return the actual sequence, not just its length?
     *   → Track startOfBest, and reconstruct from startOfBest to startOfBest+best-1.
     *
     * Follow-up: What if we need the k longest consecutive sequences?
     *   → Collect all (start, length) pairs, sort by length, take top k.
     *   → O(n log n) for the sort.
     */

    // =========================================================
    // M3. TOP K FREQUENT ELEMENTS
    // Pattern: Frequency Map + Bucket Sort (or Min-Heap)
    // =========================================================
    /**
     * Problem: Return k most frequent elements. Better than O(n log n).
     *
     * Approach A — Min-Heap: O(n log k)
     *   Build frequency map, maintain a min-heap of size k.
     *
     * Approach B — Bucket Sort: O(n) ← optimal!
     *   Key insight: frequency can be at most n.
     *   Create n+1 buckets where bucket[i] = {elements with frequency i}.
     *   Scan from high to low frequency, collect k elements.
     *
     * Time:  O(n)   (bucket sort approach)
     * Space: O(n)   (frequency map + buckets)
     */
    @SuppressWarnings("unchecked")
    public int[] topKFrequent(int[] nums, int k) {
        // Step 1: Build frequency map
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        // Step 2: Bucket sort — bucket[i] = list of nums with freq i
        List<Integer>[] bucket = new List[nums.length + 1];
        for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
            int f = e.getValue();
            if (bucket[f] == null) bucket[f] = new ArrayList<>();
            bucket[f].add(e.getKey());
        }

        // Step 3: Collect top k from highest frequency down
        int[] result = new int[k];
        int idx = 0;
        for (int f = bucket.length - 1; f >= 0 && idx < k; f--) {
            if (bucket[f] != null) {
                for (int n : bucket[f]) {
                    if (idx < k) result[idx++] = n;
                }
            }
        }
        return result;
    }

    /** Min-Heap approach — O(n log k) — simpler to remember */
    public int[] topKFrequentHeap(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        // Min-heap ordered by frequency (smallest frequency at top)
        PriorityQueue<Integer> heap = new PriorityQueue<>(
            (a, b) -> freq.get(a) - freq.get(b)
        );
        for (int n : freq.keySet()) {
            heap.offer(n);
            if (heap.size() > k) heap.poll(); // evict least frequent
        }
        return heap.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Follow-up: What if we need top k frequent WORDS (with lexicographic tiebreak)?
     *   → Use a max-heap with custom comparator: sort by freq DESC, then alphabetically ASC.
     */

    // =========================================================
    // M4. SUBARRAY SUM EQUALS K
    // Pattern: Prefix Sum + HashMap
    // =========================================================
    /**
     * Problem: Count subarrays with sum exactly equal to k.
     *
     * Key insight (the "aha" moment of prefix sums):
     *   Let prefix[i] = sum of nums[0..i].
     *   Sum of subarray [j+1..i] = prefix[i] - prefix[j].
     *   We want prefix[i] - prefix[j] == k
     *              ↔ prefix[j] == prefix[i] - k
     *
     * So: as we build prefix sums left to right, for each position i,
     * COUNT how many previous prefix sums equal (current - k).
     * A HashMap storing {prefixSum → count_of_occurrences} answers this in O(1).
     *
     * Initialize: map.put(0, 1) — empty prefix has sum 0, count 1.
     *   (Handles subarrays starting at index 0.)
     *
     * Time:  O(n)
     * Space: O(n)   — at most n distinct prefix sums
     *
     * This is one of the MOST IMPORTANT hashing patterns.
     * Variants appear in: subarray XOR, subarray mod k, 2D matrix subarrays.
     */
    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> prefixCount = new HashMap<>();
        prefixCount.put(0, 1);  // critical initialization

        int count = 0, sum = 0;
        for (int n : nums) {
            sum += n;
            count += prefixCount.getOrDefault(sum - k, 0);
            prefixCount.merge(sum, 1, Integer::sum);
        }
        return count;
    }

    /**
     * Follow-up 1: Return indices of ONE such subarray (not count)?
     *   → Store {prefixSum → index} instead of count.
     *   → On match: subarray is [map.get(sum-k)+1 .. i].
     *
     * Follow-up 2: Subarray with sum divisible by k?
     *   → Use prefix[i] % k as the key. Handle negative mods:
     *   → key = ((sum % k) + k) % k
     *   → Count prefix sums with the same mod value.
     *
     * Follow-up 3: Maximum length subarray with sum k?
     *   → Store {prefixSum → FIRST index seen} instead of count.
     *   → On match, update maxLen = max(maxLen, i - map.get(sum-k)).
     */

    // =========================================================
    // M5. LONGEST SUBSTRING WITHOUT REPEATING CHARACTERS
    // Pattern: Sliding Window + HashMap for last seen index
    // =========================================================
    /**
     * Problem: Find length of longest substring with all unique characters.
     *
     * Brute force: O(n²) — all substrings, check unique.
     *
     * Sliding Window: Maintain window [left, right] with all unique chars.
     *   When we see a duplicate: instead of shrinking one by one,
     *   JUMP left directly to (last seen index of duplicate + 1).
     *
     * Why jump instead of shrink?
     *   If char c was last seen at position p, and we're now at right = j,
     *   any window that includes both positions p and j has a duplicate.
     *   So the new window must start at p+1 at the earliest.
     *
     * IMPORTANT: left = max(left, map.get(c) + 1)
     *   The max is critical — we never move left backward when the
     *   last-seen index is OUTSIDE the current window.
     *
     * Time:  O(n) — each character visited at most twice (add/remove)
     * Space: O(min(n, σ)) where σ = alphabet size
     */
    public int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int maxLen = 0, left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastSeen.containsKey(c)) {
                left = Math.max(left, lastSeen.get(c) + 1);  // jump, don't shrink!
            }
            lastSeen.put(c, right);
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }

    /**
     * Follow-up 1: Longest substring with AT MOST k distinct characters?
     *   → Track char frequencies in window. Remove chars when distinct count > k.
     *   → LeetCode 340 (premium).
     *
     * Follow-up 2: Longest substring with EXACTLY k distinct characters?
     *   → atMost(k) - atMost(k-1)  — the "exact = ≤k minus ≤(k-1)" trick.
     */

    // =========================================================
    // M6. 4SUM II (Count Tuples Summing to Zero)
    // Pattern: Two-sum extension — Split into halves
    // =========================================================
    /**
     * Problem: Given 4 arrays A,B,C,D of length n, count tuples (i,j,k,l)
     * such that A[i]+B[j]+C[k]+D[l] == 0.
     *
     * Naive: O(n⁴)
     * Two-pair trick: Precompute all sums from A+B, store in map.
     *   Then for each C[k]+D[l], check if -(C[k]+D[l]) exists in map.
     *
     * This generalizes: "reduce 4 arrays to 2 × 2-sum problems."
     *
     * Time:  O(n²)   — two passes of O(n²) each
     * Space: O(n²)   — up to n² distinct pair sums
     */
    public int fourSumCount(int[] A, int[] B, int[] C, int[] D) {
        Map<Integer, Integer> abSums = new HashMap<>();
        for (int a : A)
            for (int b : B)
                abSums.merge(a + b, 1, Integer::sum);

        int count = 0;
        for (int c : C)
            for (int d : D)
                count += abSums.getOrDefault(-(c + d), 0);

        return count;
    }

    /**
     * Follow-up: What if arrays are of different lengths?
     *   → Same algorithm, split the longer two vs shorter two for best space.
     *
     * Follow-up: Can you solve it in O(n^(3/2)) for special cases?
     *   → Advanced: meet-in-the-middle approach on sorted arrays.
     */

    // =========================================================
    // M7. FIND ALL ANAGRAMS IN A STRING
    // Pattern: Sliding Window (Fixed Size) + Frequency Map
    // =========================================================
    /**
     * Problem: Find all start indices of p's anagrams in s.
     *
     * Fixed-size sliding window:
     *   - Maintain a frequency difference array between window and pattern.
     *   - Track how many characters have "equal" frequency (matches count).
     *   - When matches == 26 (or alphabet size), window is an anagram.
     *
     * Time:  O(n) — each element enters and leaves window once
     * Space: O(1) — 26-element arrays
     */
    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> result = new ArrayList<>();
        if (s.length() < p.length()) return result;

        int[] pFreq = new int[26], wFreq = new int[26];
        for (char c : p.toCharArray()) pFreq[c - 'a']++;

        int matches = 0;
        int k = p.length();

        // Initialize first window
        for (int i = 0; i < k; i++) wFreq[s.charAt(i) - 'a']++;
        for (int i = 0; i < 26; i++) if (wFreq[i] == pFreq[i]) matches++;

        for (int right = k; right < s.length(); right++) {
            if (matches == 26) result.add(right - k);

            // Add new char (right)
            int in = s.charAt(right) - 'a';
            if (wFreq[in] == pFreq[in]) matches--;
            wFreq[in]++;
            if (wFreq[in] == pFreq[in]) matches++;

            // Remove old char (right - k)
            int out = s.charAt(right - k) - 'a';
            if (wFreq[out] == pFreq[out]) matches--;
            wFreq[out]--;
            if (wFreq[out] == pFreq[out]) matches++;
        }
        if (matches == 26) result.add(s.length() - k);
        return result;
    }

    /**
     * Follow-up: Find any ONE anagram (not all positions)?
     *   → Stop at first match. Same algorithm, early return.
     *
     * Follow-up: Count anagram substrings?
     *   → result.size() gives the count.
     */

    // =========================================================
    // M8. CONTIGUOUS ARRAY (Equal 0s and 1s)
    // Pattern: Prefix Sum + Map (transform problem)
    // =========================================================
    /**
     * Problem: Find the max length subarray with equal 0s and 1s.
     *
     * Transform: Replace 0 with -1. Now we want max subarray with sum = 0.
     *   (Equal 0s and 1s ↔ sum of transformed = 0)
     *
     * This reduces to: longest subarray with sum k=0,
     * which is solved by prefix sum + first-seen index map.
     *
     * Time:  O(n)
     * Space: O(n)
     */
    public int findMaxLength(int[] nums) {
        Map<Integer, Integer> firstSeen = new HashMap<>();
        firstSeen.put(0, -1);  // sum 0 seen at index -1 (before array)

        int maxLen = 0, sum = 0;
        for (int i = 0; i < nums.length; i++) {
            sum += (nums[i] == 1) ? 1 : -1;
            if (firstSeen.containsKey(sum)) {
                maxLen = Math.max(maxLen, i - firstSeen.get(sum));
            } else {
                firstSeen.put(sum, i);  // only store FIRST occurrence
            }
        }
        return maxLen;
    }

    /**
     * Follow-up: What if we have k distinct values (not just 0 and 1)?
     *   → Track frequency differences between any two values as prefix sums.
     *
     * Follow-up: Count ALL equal-01 subarrays?
     *   → Combine count approach from M4 with this transform.
     */
}
