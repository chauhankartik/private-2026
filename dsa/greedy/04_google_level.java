/**
 * ============================================================
 *  GREEDY ALGORITHMS — GOOGLE-LEVEL PROBLEMS
 *  Pattern-oriented multi-pattern complex problems.
 * ============================================================
 *
 *  Problems:
 *   G1. Patching Array (LeetCode 330)
 *   G2. Construct Target Array With Multiple Sums (LeetCode 1354)
 *   G3. Stamping The Sequence (LeetCode 936)
 *   G4. Rearrange String k Distance Apart (LeetCode 358)
 *   G5. IPO - Initial Public Offering (LeetCode 502)
 *   G6. Minimum Cost to Hire K Workers (LeetCode 857)
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
    // G1. PATCHING ARRAY
    // Pattern: Reachability Range Expansion Greedy
    // LeetCode: 330
    // =========================================================
    /**
     * Problem: Given sorted nums array and integer n, add minimum patches
     * so that any number in [1, n] can be formed by sum of elements.
     *
     * Optimal O(log n + size): Maintain `miss` (smallest unformed sum).
     * If nums[i] <= miss: miss += nums[i] (range expanded to [1, miss + nums[i] - 1]).
     * Else: patch `miss` greedily, miss += miss (patches++).
     *
     * Time: O(log n + size)
     * Space: O(1)
     */
    public int minPatches(int[] nums, int n) {
        long miss = 1;
        int patches = 0;
        int i = 0;

        while (miss <= n) {
            if (i < nums.length && nums[i] <= miss) {
                miss += nums[i];
                i++;
            } else {
                miss += miss; // Patch miss greedily (doubles reachable range)
                patches++;
            }
        }
        return patches;
    }

    // =========================================================
    // G2. CONSTRUCT TARGET ARRAY WITH MULTIPLE SUMS
    // Pattern: Reverse Priority Queue Greedy
    // LeetCode: 1354
    // =========================================================
    /**
     * Problem: Start with array of 1s. Replace an element with total sum of array.
     * Given target array, return true if target can be constructed.
     *
     * Optimal O(N log(max_val)): Reverse engineering with Max-Heap.
     * Continuously replace max element in target with its previous value before replacement.
     *
     * Time: O(N log(max_val))
     * Space: O(N)
     */
    public boolean isPossible(int[] target) {
        if (target.length == 1) return target[0] == 1;

        PriorityQueue<Long> maxHeap = new PriorityQueue<>((a, b) -> Long.compare(b, a));
        long totalSum = 0;

        for (int num : target) {
            maxHeap.offer((long) num);
            totalSum += num;
        }

        while (true) {
            long maxVal = maxHeap.poll();
            long restSum = totalSum - maxVal;

            if (maxVal == 1 || restSum == 1) return true;
            if (restSum == 0 || maxVal <= restSum) return false;

            long prevVal = maxVal % restSum;
            if (prevVal == 0) return false;

            totalSum = restSum + prevVal;
            maxHeap.offer(prevVal);
        }
    }

    // =========================================================
    // G3. STAMPING THE SEQUENCE
    // Pattern: Reverse Matching Greedy
    // LeetCode: 936
    // =========================================================
    /**
     * Problem: Given stamp string and target string, return order of stamp indices to form target.
     *
     * Optimal O(N · (N - M)): Reverse stamping.
     * Replace occurrences of stamp in target with '?' wildcards until target is all '?'.
     *
     * Time: O(N · (N - M))
     * Space: O(N)
     */
    public int[] movesToStamp(String stamp, String target) {
        char[] s = stamp.toCharArray();
        char[] t = target.toCharArray();
        int n = t.length, m = s.length;
        List<Integer> res = new ArrayList<>();
        boolean[] visited = new boolean[n];
        int stars = 0;

        while (stars < n) {
            boolean doneReplace = false;
            for (int i = 0; i <= n - m; i++) {
                if (!visited[i] && canReplace(t, i, s)) {
                    stars += doReplace(t, i, m);
                    visited[i] = true;
                    doneReplace = true;
                    res.add(i);
                    if (stars == n) break;
                }
            }
            if (!doneReplace) return new int[0];
        }

        int[] ans = new int[res.size()];
        for (int i = 0; i < res.size(); i++) {
            ans[i] = res.get(res.size() - 1 - i); // Reverse operations
        }
        return ans;
    }

    private boolean canReplace(char[] t, int p, char[] s) {
        for (int i = 0; i < s.length; i++) {
            if (t[p + i] != '?' && t[p + i] != s[i]) return false;
        }
        return true;
    }

    private int doReplace(char[] t, int p, int m) {
        int count = 0;
        for (int i = 0; i < m; i++) {
            if (t[p + i] != '?') {
                t[p + i] = '?';
                count++;
            }
        }
        return count;
    }

    // =========================================================
    // G4. REARRANGE STRING K DISTANCE APART
    // Pattern: Priority Queue Frequency & Cooldown Queue
    // LeetCode: 358
    // =========================================================
    /**
     * Problem: Rearrange string s such that same characters are at least distance k apart.
     *
     * Optimal O(N log 26) = O(N): Max-Heap for frequencies + Queue for cooling window.
     *
     * Time: O(N)
     * Space: O(1)
     */
    public String rearrangeString(String s, int k) {
        if (k <= 1) return s;

        Map<Character, Integer> map = new HashMap<>();
        for (char c : s.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<Map.Entry<Character, Integer>> maxHeap =
            new PriorityQueue<>((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        maxHeap.addAll(map.entrySet());

        Queue<Map.Entry<Character, Integer>> waitQueue = new LinkedList<>();
        StringBuilder sb = new StringBuilder();

        while (!maxHeap.isEmpty()) {
            Map.Entry<Character, Integer> current = maxHeap.poll();
            sb.append(current.getKey());
            current.setValue(current.getValue() - 1);
            waitQueue.offer(current);

            if (waitQueue.size() >= k) {
                Map.Entry<Character, Integer> front = waitQueue.poll();
                if (front.getValue() > 0) {
                    maxHeap.offer(front);
                }
            }
        }
        return sb.length() == s.length() ? sb.toString() : "";
    }

    // =========================================================
    // G5. IPO (INITIAL PUBLIC OFFERING)
    // Pattern: Min-Capital Heap + Max-Profit Heap Dual Greedy
    // LeetCode: 502
    // =========================================================
    /**
     * Problem: Given capital w, pick at most k distinct projects to maximize capital.
     *
     * Optimal O(N log N + K log N):
     * 1. Min-Heap ordered by capital required.
     * 2. Max-Heap ordered by profit.
     * 3. Move all projects requiring <= current capital from Min-Heap to Max-Heap.
     * 4. Pop max profit project from Max-Heap.
     *
     * Time: O(N log N + K log N)
     * Space: O(N)
     */
    public int findMaximizedCapital(int k, int w, int[] profits, int[] capital) {
        int n = profits.length;
        int[][] projects = new int[n][2];
        for (int i = 0; i < n; i++) {
            projects[i][0] = capital[i];
            projects[i][1] = profits[i];
        }

        Arrays.sort(projects, (a, b) -> Integer.compare(a[0], b[0]));
        PriorityQueue<Integer> maxProfitHeap = new PriorityQueue<>((a, b) -> Integer.compare(b, a));

        int i = 0;
        for (int step = 0; step < k; step++) {
            while (i < n && projects[i][0] <= w) {
                maxProfitHeap.offer(projects[i][1]);
                i++;
            }
            if (maxProfitHeap.isEmpty()) break;
            w += maxProfitHeap.poll();
        }
        return w;
    }

    // =========================================================
    // G6. MINIMUM COST TO HIRE K WORKERS
    // Pattern: Sorting Ratio + PriorityQueue Sum Greedy
    // LeetCode: 857
    // =========================================================
    /**
     * Problem: Hire k workers such that worker pay is proportional to quality and
     * at least their minimum wage requirement. Minimize total cost.
     *
     * Optimal O(N log N): Sort workers by ratio wage/quality.
     * Maintain Max-Heap of quality for current k workers.
     * Total cost = current_ratio * sum_quality.
     *
     * Time: O(N log N)
     * Space: O(N)
     */
    public double mincostToHireWorkers(int[] quality, int[] wage, int k) {
        int n = quality.length;
        double[][] workers = new double[n][2];
        for (int i = 0; i < n; i++) {
            workers[i][0] = (double) wage[i] / quality[i]; // Ratio wage / quality
            workers[i][1] = quality[i];
        }

        Arrays.sort(workers, (a, b) -> Double.compare(a[0], b[0]));

        PriorityQueue<Double> maxQualityHeap = new PriorityQueue<>((a, b) -> Double.compare(b, a));
        double currentQualitySum = 0;
        double minCost = Double.MAX_VALUE;

        for (double[] worker : workers) {
            double ratio = worker[0];
            double q = worker[1];

            maxQualityHeap.offer(q);
            currentQualitySum += q;

            if (maxQualityHeap.size() > k) {
                currentQualitySum -= maxQualityHeap.poll();
            }

            if (maxQualityHeap.size() == k) {
                minCost = Math.min(minCost, currentQualitySum * ratio);
            }
        }
        return minCost;
    }
}
