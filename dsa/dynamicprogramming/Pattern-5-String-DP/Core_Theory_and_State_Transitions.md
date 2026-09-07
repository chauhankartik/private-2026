# Pattern 5 — String DP (Two-Sequence)
> **Core Idea:** `dp[i][j]` = the optimal answer considering the first `i` characters
> of string `s1` and first `j` characters of string `s2`.
> Transition branches on whether `s1[i-1] == s2[j-1]`.

---

## 1. The Mental Model

```
"Align two strings. At each position pair (i, j), decide:
  — if s1[i-1] == s2[j-1]: we can 'match' (use both chars)
  — else: skip from s1 (move i), skip from s2 (move j), or replace"

dp[i][j] = answer for s1[0..i-1] and s2[0..j-1]

Base cases:
  dp[0][j] = answer when s1 is empty
  dp[i][0] = answer when s2 is empty
```

**Key signal words:** "longest common subsequence", "edit distance",
"interleaving strings", "wildcard matching", "shortest common supersequence".

---

## 2. Canonical Templates

### Template A: LCS (Longest Common Subsequence)
```java
int[][] dp = new int[m + 1][n + 1];
for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        if (s1.charAt(i-1) == s2.charAt(j-1))
            dp[i][j] = dp[i-1][j-1] + 1;     // match: extend LCS
        else
            dp[i][j] = Math.max(dp[i-1][j],   // skip s1[i]
                                dp[i][j-1]);   // skip s2[j]
    }
}
return dp[m][n];
// Space opt: two rows → O(min(m,n))
```

### Template B: Edit Distance
```java
int[][] dp = new int[m + 1][n + 1];
for (int i = 0; i <= m; i++) dp[i][0] = i;    // delete all of s1
for (int j = 0; j <= n; j++) dp[0][j] = j;    // insert all of s2
for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        if (s1.charAt(i-1) == s2.charAt(j-1))
            dp[i][j] = dp[i-1][j-1];           // match: free
        else
            dp[i][j] = 1 + Math.min(dp[i-1][j-1],  // replace
                           Math.min(dp[i-1][j],      // delete
                                    dp[i][j-1]));    // insert
    }
}
return dp[m][n];
```

### Template C: Longest Common Substring (contiguous)
```java
// dp[i][j] = length of common substring ending at s1[i-1] and s2[j-1]
int maxLen = 0;
int[][] dp = new int[m + 1][n + 1];
for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        if (s1.charAt(i-1) == s2.charAt(j-1)) {
            dp[i][j] = dp[i-1][j-1] + 1;       // must match (substring = contiguous)
            maxLen = Math.max(maxLen, dp[i][j]);
        }
        // else dp[i][j] = 0 (implicit, break in contiguity)
    }
}
return maxLen;
```

---

## 3. State Transition Diagrams

### LCS: s1="ABCBDAB", s2="BDCAB"
```
     ""  B  D  C  A  B
  ""  0  0  0  0  0  0
  A   0  0  0  0  1  1
  B   0  1  1  1  1  2
  C   0  1  1  2  2  2
  B   0  1  1  2  2  3
  D   0  1  2  2  2  3
  A   0  1  2  2  3  3
  B   0  1  2  2  3  4  ← LCS length = 4 (e.g., "BCAB" or "BDAB")
```

### Edit Distance: s1="horse", s2="ros"
```
     ""  r  o  s
  ""  0  1  2  3
  h   1  1  2  3
  o   2  2  1  2
  r   3  2  2  2
  s   4  3  3  2
  e   5  4  4  3  ← answer: 3 operations
```

### Shortest Common Supersequence (SCS)
```
SCS length = m + n - LCS(s1, s2)
(Merge s1 and s2 using LCS as the "shared" part)

To reconstruct SCS: backtrack through LCS dp table
  If chars match: include once (it's in both)
  Else: include the skipped char from whichever has fewer operations
```

### Interleaving String (LC 97)
```
dp[i][j] = can s3[0..i+j-1] be formed by interleaving s1[0..i-1] and s2[0..j-1]?

Transition:
  dp[i][j] = (dp[i-1][j] && s1[i-1]==s3[i+j-1])   // take char from s1
           || (dp[i][j-1] && s2[j-1]==s3[i+j-1])    // take char from s2

Base: dp[0][0]=true
```

---

## 4. Derivations from LCS

| Problem | Derivation |
|---|---|
| LCS | Direct: dp[i][j] = dp[i-1][j-1]+1 or max(dp[i-1][j], dp[i][j-1]) |
| Edit Distance | LCS variant with insert/delete/replace costs |
| Shortest Common Superseq | m + n - LCS |
| Min deletions to make palindrome | n - LPS (longest palindromic subseq) |
| Min insertions to make palindrome | n - LPS |
| Longest Palindromic Subsequence | LCS(s, reverse(s)) |
| Count distinct LCS | Modify to count instead of maximize |
| Sequence Pattern Match | LCS(s, t) == t.length() |

---

## 5. Space Optimization: 2D → O(min(m,n))

```java
// Use two rows: prev[] and curr[]
int[] prev = new int[n + 1];
int[] curr = new int[n + 1];

for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        if (s1.charAt(i-1) == s2.charAt(j-1))
            curr[j] = prev[j-1] + 1;
        else
            curr[j] = Math.max(prev[j], curr[j-1]);
    }
    int[] tmp = prev; prev = curr; curr = tmp;
    Arrays.fill(curr, 0);
}
return prev[n];

// For edit distance: need prev[j-1] for diagonal
// Save it BEFORE updating:
//   int diagPrev = prev[j-1];
//   ... update prev[j] using diagPrev
```

---

## 6. Complexity Analysis

| Problem | Time | Space | Optimized |
|---|---|---|---|
| LCS | O(m × n) | O(m × n) | O(min(m,n)) |
| Edit Distance | O(m × n) | O(m × n) | O(min(m,n)) |
| LCS Substring | O(m × n) | O(m × n) | O(n) |
| Interleaving String | O(m × n) | O(m × n) | O(n) |
| Wildcard Matching | O(m × n) | O(m × n) | O(n) |
| Regex Matching | O(m × n) | O(m × n) | O(n) |

---

## 7. Common Pitfalls

```java
// PITFALL 1: 1-indexing in DP vs 0-indexing in string
// dp is (m+1)×(n+1). s1.charAt(i-1) is the i-th character (1-indexed).
// NEVER use s1.charAt(i) in a 1-indexed DP.

// PITFALL 2: Array size too small
// dp = new int[m][n] is WRONG for 1-indexed. Use new int[m+1][n+1].

// PITFALL 3: Not initializing base cases for Edit Distance
// dp[i][0] = i (delete i chars from s1) and dp[0][j] = j (insert j chars).
// Zero-initialized arrays give dp[i][0]=0 which is WRONG.

// PITFALL 4: Confusing LCS (subsequence) vs LCSubstring (contiguous)
// LCS: dp[i][j] = max(dp[i-1][j], dp[i][j-1]) on mismatch (can skip chars)
// LCStr: dp[i][j] = 0 on mismatch (substring must be contiguous)

// PITFALL 5: Printing actual LCS — wrong backtracking direction
// Backtrack from dp[m][n] to dp[0][0]:
//   if s1[i-1]==s2[j-1]: include char, move i-- j--
//   else: move toward larger dp value (i-- if dp[i-1][j]>=dp[i][j-1], else j--)
// Reverse the collected characters at the end.
```

---

## 8. Interview Communication Script

```
"This is a two-sequence String DP problem. The key is that
 dp[i][j] encodes the answer for s1[0..i-1] and s2[0..j-1].

 When s1[i-1] == s2[j-1]: we can match (extend by 1 or use diagonal dp[i-1][j-1]).
 When they differ: we choose the best of skipping one char from either string.

 Base: dp[i][0] and dp[0][j] handle the empty-string cases.
 Space optimization: only need 2 rows → O(min(m,n)) space.
 Time: O(m × n) — filling an (m+1)×(n+1) table."
```

*Next: Basic (LCS), Medium (Edit Distance), Hard (Interleaving String).*
