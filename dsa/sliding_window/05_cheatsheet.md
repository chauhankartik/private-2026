# Sliding Window — Quick Reference & Interview Cheatsheet
> Print this. Know this cold. This is your 30-second recall sheet.

---

## Pattern Recognition Table

| If the problem mentions... | Think... |
|---|---|
| "maximum/minimum sum of size k" | Fixed Sliding Window |
| "longest substring without repeating" | Variable Window + HashMap/Set |
| "at most k distinct characters" | Variable Window + HashMap |
| "at most k zeros/flips" | Variable Window + Counter |
| "permutation / anagram in string" | Fixed Window + Frequency Match |
| "exactly k distinct/odd" | atMost(k) - atMost(k-1) |
| "sliding window maximum/minimum" | Monotonic Deque |
| "shortest subarray with sum ≥ k" (negatives) | Prefix Sum + Monotonic Deque |
| "shortest subarray with sum ≥ k" (positives) | Variable Window — Shortest |
| "count subarrays with product < k" | Variable Window + count += right-left+1 |
| "all characters of t in s" | Variable Window — Shortest (Min Window Substring) |
| "consecutive ones with k flips" | Variable Window — Longest |
| "DP with look-back of k" | DP + Monotonic Deque |

---

## The Four Templates (Memorize These)

### Template 1: Fixed Window

```java
int windowSum = 0;
for (int i = 0; i < k; i++) windowSum += arr[i];  // init
int best = windowSum;

for (int i = k; i < n; i++) {
    windowSum += arr[i] - arr[i - k];              // slide
    best = Math.max(best, windowSum);              // update
}
```

### Template 2: Variable — Longest

```java
int left = 0, best = 0;
for (int right = 0; right < n; right++) {
    // expand: add arr[right] to state
    while (/* INVALID */) {
        // shrink: remove arr[left] from state
        left++;
    }
    best = Math.max(best, right - left + 1);
}
```

### Template 3: Variable — Shortest

```java
int left = 0, best = Integer.MAX_VALUE;
for (int right = 0; right < n; right++) {
    // expand: add arr[right]
    while (/* VALID */) {
        best = Math.min(best, right - left + 1);  // record BEFORE shrink
        // shrink: remove arr[left]
        left++;
    }
}
```

### Template 4: Exactly K (via atMost)

```java
int exactlyK(int[] arr, int k) {
    return atMostK(arr, k) - atMostK(arr, k - 1);
}

int atMostK(int[] arr, int k) {
    int left = 0, count = 0;
    // state tracking (e.g., freq map)
    for (int right = 0; right < arr.length; right++) {
        // expand
        while (/* state > k */) { /* shrink */ left++; }
        count += right - left + 1;  // ALL valid subarrays ending at right
    }
    return count;
}
```

---

## Frequency Match Template (Anagram/Permutation Detection)

```java
int[] pFreq = new int[26], sFreq = new int[26];
// build pFreq from pattern, sFreq from first window
int matches = 0;
for (int i = 0; i < 26; i++) if (pFreq[i] == sFreq[i]) matches++;

for (int i = p.length(); i < s.length(); i++) {
    if (matches == 26) /* found anagram at i - p.length() */;
    
    int add = s.charAt(i) - 'a';
    sFreq[add]++;
    if (sFreq[add] == pFreq[add]) matches++;
    else if (sFreq[add] == pFreq[add] + 1) matches--;
    
    int rem = s.charAt(i - p.length()) - 'a';
    sFreq[rem]--;
    if (sFreq[rem] == pFreq[rem]) matches++;
    else if (sFreq[rem] == pFreq[rem] - 1) matches--;
}
```

---

## Monotonic Deque Template (Window Max/Min)

```java
Deque<Integer> deque = new ArrayDeque<>();  // stores INDICES

for (int i = 0; i < n; i++) {
    // 1. Remove expired indices
    while (!deque.isEmpty() && deque.peekFirst() < i - k + 1)
        deque.pollFirst();
    
    // 2. Remove dominated elements (for MAX: remove smaller)
    while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i])
        deque.pollLast();
    
    deque.offerLast(i);
    
    // 3. Front = answer for current window
    if (i >= k - 1) result[i - k + 1] = nums[deque.peekFirst()];
}
```

---

## Complexity Quick Reference

| Problem Type | Time | Space |
|---|---|---|
| Fixed window (sum/max/count) | O(n) | O(1) |
| Variable window (longest/shortest) | O(n) | O(1) or O(k) |
| Frequency match (anagram) | O(n) | O(1) (26 chars) |
| Exactly K distinct | O(n) | O(n) |
| Sliding window max (deque) | O(n) | O(k) |
| Shortest subarray sum ≥ k (negatives) | O(n) | O(n) |
| DP + deque optimization | O(n) | O(n) |

---

## Problem → Pattern Map (for Google interviews)

```
Max Sum Subarray Size K       → Fixed Window
Max Vowels in Substring K     → Fixed Window + Counter
Longest Substr No Repeat      → Variable + HashMap (last seen)
Max Consec Ones III            → Variable + Zero Counter
Fruit Into Baskets             → Variable + At Most 2 Distinct
Char Replacement               → Variable + MaxFreq Trick
Permutation in String          → Fixed Window + Freq Match
Find All Anagrams              → Fixed Window + Freq Match
Subarray Product < K           → Variable + Counting
Min Window Substring           → Variable Shortest + Freq Match
Sliding Window Max             → Monotonic Deque
K Different Integers           → atMost(K) - atMost(K-1)
Nice Subarrays                 → atMost(K) - atMost(K-1)
Shortest Subarray Sum ≥ K     → Prefix Sum + Monotonic Deque
Constrained Subseq Sum        → DP + Monotonic Deque
```

---

## Interview Communication Script

When you start solving any sliding window problem:

> "I recognize this as a sliding window problem because we're looking for
> an optimal [contiguous subarray / substring] under a constraint.
> Let me identify the variant:
> - Fixed or variable size? → [Fixed: k is given / Variable: optimizing length]
> - Longest or shortest? → [Longest: shrink when invalid / Shortest: shrink when valid]
> - Exactly K? → I'll decompose into atMost(K) - atMost(K-1).
> The time complexity is O(n) because each element enters and leaves
> the window at most once."

---

## Complexity Proof Keywords for Interviews

- **Variable window O(n):** "Left and right pointers each move at most n times. Each element enters and leaves the window at most once → 2n operations."
- **Monotonic deque O(n):** "Each element is pushed once and popped at most once → 2n deque operations total."
- **Exactly K trick:** "Two passes of O(n) sliding window → O(n) total."
- **Frequency match O(n):** "Fixed window slides n times. Each slide updates 2 characters and checks matches in O(1)."

---

*Files in this module:*
- `00_theory.md` — Internals, proofs, templates, decision tree
- `01_easy.java` — 8 easy problems (fixed window foundations)
- `02_medium.java` — 8 medium problems (variable window, freq matching)
- `03_hard.java` — 6 hard problems (min window, deque, exactly K)
- `04_google_level.java` — 6 Google-level (deque + DP, prefix + deque)
- `05_cheatsheet.md` — This file
