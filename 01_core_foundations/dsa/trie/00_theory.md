# Trie (Prefix Tree) — Theory, Internals & Memory Analysis
> **Study goal:** Understand Tries at the pointer, memory, and hardware level.
> Google interviewers expect you to trade off array vs HashMap node representation, analyze high pointer memory overhead, and use Bitwise Tries for XOR optimization.

---

## 1. What Is a Trie (Prefix Tree)?

A **Trie** (pronounced "try", coming from re**trie**val) is a tree-like data structure used to efficiently store and retrieve keys in a dataset of strings. Unlike a Binary Search Tree (BST), no node in the tree stores the key associated with that node. Instead, its **position in the tree** defines the key it is associated with.

All descendants of a node share a common prefix of the string associated with that node, which is why it is called a **Prefix Tree**.

```
Trie structure storing ["cat", "car", "card", "dog", "dot"]:

                  root (isEnd=false)
                /      \
               c        d
              /          \
             a            o
           /   \        /   \
          t*    r*     g*    t*
                 \
                  d*

(* denotes isEnd = true)
```

---

## 2. Node Design & Internal Memory Layout

### Option A: Fixed-Size Array (`TrieNode[26]`)
Best when the alphabet size $\Sigma$ is small and fixed (e.g., lowercase English letters $a-z$).

```java
class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEnd;
}
```

- **Pros:** Extremely fast $O(1)$ child lookup (index via `c - 'a'`), best CPU cache locality among direct children.
- **Cons:** High memory fragmentation/wasted space if nodes are sparse.
- **Memory Cost per Node (64-bit JVM):**
  - Object header: 12–16 bytes
  - Array header + length: 16 bytes
  - 26 object references: $26 \times 8 = 208$ bytes (or $208 \times 4$ with Compressed OOPs)
  - `boolean isEnd` + padding: 8 bytes
  - **Total per node:** $\approx 248$ bytes! Even if a node has only 1 child, it wastes 240 bytes.

### Option B: Dynamic Map (`Map<Character, TrieNode>`)
Best when alphabet size $\Sigma$ is large or variable (e.g., Unicode, full ASCII, UTF-8).

```java
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEnd;
}
```

- **Pros:** Space-efficient for sparse trees; only stores actual existing branches.
- **Cons:** High HashMap overhead per node (`HashMap.Entry` object overhead), slower child lookup ($O(1)$ average, but higher constant factor due to hashing).

---

## 3. Bitwise (Binary) Trie for XOR & Bit Manipulation

When operating on integers (e.g., finding Maximum XOR Pair), numbers are treated as 32-bit (or 64-bit) binary strings.

A **Bitwise Trie** is a binary tree where every node has at most 2 children: `0` (left) and `1` (right).

```
Binary Trie for 3-bit numbers {3 (011), 5 (101), 6 (110)}:

                      root
                     /    \
                    0      1
                     \    / \
                      1  0   1
                     /    \   \
                    1(3)  1(5) 0(6)
```

### Why Bitwise Trie Works for Maximum XOR:
To maximize $A \oplus B$, for each bit of $A$ from MSB (bit 31) to LSB (bit 0), we greedily attempt to traverse the branch with the **opposite bit** ($\text{bit} \oplus 1$). If the opposite branch exists, it contributes $2^i$ to the XOR sum; otherwise, we take the matching bit branch.

---

## 4. Advanced Variants: Compressed Trie & Suffix Trie

### 1. Radix Tree / Patricia Trie (Compressed Prefix Tree)
A Trie where single-child nodes are merged with their parent. Reduces node count drastically for long common paths (e.g., URL routes `/api/v1/users`).

```
Standard Trie:  c -> a -> r -> d
Compressed:     [card]
```

### 2. Suffix Trie
A Trie built by inserting all suffixes of a string $S$.
- Allows substring search in $O(M)$ time where $M$ is query length.
- Space complexity: $O(N^2)$ for naive Suffix Trie, reduced to $O(N)$ using **Suffix Tree (Ukkonen's Algorithm)**.

---

## 5. Complexity Comparison Matrix

| Operation / Structure | Trie | HashMap | Binary Search Tree (AVL/Red-Black) |
|---|---|---|---|
| **Search Word (Length $M$)** | $\mathcal{O}(M)$ | $\mathcal{O}(M)$ (hash string) | $\mathcal{O}(M \log N)$ |
| **Insert Word (Length $M$)** | $\mathcal{O}(M)$ | $\mathcal{O}(M)$ | $\mathcal{O}(M \log N)$ |
| **Delete Word** | $\mathcal{O}(M)$ | $\mathcal{O}(M)$ | $\mathcal{O}(M \log N)$ |
| **Prefix Search ("starts-with")** | $\mathcal{O}(M)$ | $\mathcal{O}(N \cdot M)$ (scan all keys) | $\mathcal{O}(M + \log N)$ |
| **Autocomplete / Top-K Prefixes** | $\mathcal{O}(M + K)$ | Hard ($\mathcal{O}(N)$) | $\mathcal{O}(M + K)$ |
| **Space Overhead** | $\mathcal{O}(N \cdot M \cdot \Sigma)$ | $\mathcal{O}(N \cdot M)$ | $\mathcal{O}(N \cdot M)$ |

---

## 6. Interview Vocabulary & Trade-off Argument

When presenting a Trie solution to a Google interviewer:

> *"I choose a Trie data structure here because the problem requires frequent **prefix matching** and **prefix aggregation** across $N$ words.
> While a HashMap provides average $O(M)$ lookup for exact word matches, it cannot perform prefix-matching without scanning all $N$ keys ($O(N \cdot M)$).
> The Trie gives us guaranteed $O(M)$ time for both exact word lookup and prefix checks.
> To optimize space when dealing with sparse branches, we can use a `HashMap<Character, TrieNode>` layout, or use a fixed `TrieNode[26]` array for maximum cache locality when $\Sigma = 26$."*
