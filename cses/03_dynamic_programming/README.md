# CSES Module 03: Dynamic Programming

This module covers state transitions, 1D/2D table optimizations, Knapsack variations, Digit DP, Tree DP, Bitmask DP, and Profile DP.

---

## 📋 Problem List (19 Problems)

| ID | Problem Name | Difficulty | Key Concept | Status |
| :---: | :--- | :---: | :--- | :---: |
| 1633 | Dice Combinations | Easy | 1D DP `dp[i] = sum(dp[i-1..i-6])` | 🔴 |
| 1634 | Minimizing Coins | Medium | Unbounded Knapsack min coins | 🔴 |
| 1635 | Coin Combinations I | Medium | Permutation coin ordering DP | 🔴 |
| 1636 | Coin Combinations II | Medium | Combination coin ordering DP (outer coin loop) | 🔴 |
| 1637 | Removing Digits | Easy | Greedily subtract max digit / 1D DP | 🔴 |
| 1638 | Grid Paths | Medium | 2D Grid path counting with obstacles | 🔴 |
| 1158 | Book Shop | Medium | 0/1 Knapsack space optimization | 🔴 |
| 1746 | Array Description | Hard | State DP `dp[i][val]` transition | 🔴 |
| 2413 | Counting Towers | Hard | State machine DP (joined vs split blocks) | 🔴 |
| 1639 | Edit Distance | Hard | 2D Levenshtein distance DP | 🔴 |
| 1744 | Rectangle Cutting | Hard | 2D grid cut minimization DP | 🔴 |
| 1745 | Money Sums | Medium | Bitset / 0/1 Knapsack reachable sums | 🔴 |
| 1097 | Removal Game | Hard | Minimax / Interval Range DP `dp[i][j]` | 🔴 |
| 1093 | Two Sets II | Medium | Subset sum DP `target = N(N+1)/4` | 🔴 |
| 1145 | Increasing Subsequence | Hard | Patience sorting $O(N \log N)$ LIS | 🔴 |
| 1140 | Projects | Hard | Interval DP + Binary search `dp[i]` | 🔴 |
| 1653 | Elevator Rides | Hard | Bitmask DP `dp[mask] = {rides, weight}` | 🔴 |
| 2181 | Counting Tilings | Hard | Broken profile Bitmask DP ($2^M \times N$) | 🔴 |
| 2220 | Counting Numbers | Hard | Digit DP `dp(index, prev_digit, is_less)` | 🔴 |
