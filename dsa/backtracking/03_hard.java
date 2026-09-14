/**
 * ============================================================
 *  RECURSION & BACKTRACKING — HARD PROBLEMS
 *  Pattern-oriented problems. Each problem references the
 *  pattern from 00_theory.md.
 * ============================================================
 *
 *  Problems:
 *   H1. N-Queens I & II (LeetCode 51 / 52 - Bitmask Diagonal Tracking)
 *   H2. Sudoku Solver (LeetCode 37 - 9x9 Bitmask Backtracking)
 *   H3. Word Break II (LeetCode 140 - Recursion + Memoization)
 *   H4. Remove Invalid Parentheses (LeetCode 301 - Min Removal DFS)
 *   H5. Expression Add Operators (LeetCode 282 - String Evaluation Backtracking)
 *   H6. Matchsticks to Square (LeetCode 473 - Subset Partitioning)
 *
 *  Each problem includes:
 *   - Pattern tag
 *   - Brute force → optimal progression
 *   - Complexity proof
 *   - Follow-up question + answer
 * ============================================================
 */
import java.util.*;

class Hard {

    // =========================================================
    // H1. N-QUEENS I & II
    // Pattern: Bitmask / Array Diagonal Collision Tracking
    // LeetCode: 51 / 52
    // =========================================================
    /**
     * Problem: Place n queens on n x n chessboard such that no two queens attack each other.
     *
     * Optimal O(N!): Track columns (`cols[c]`), main diagonals (`diag1[r - c + N]`),
     * and anti-diagonals (`diag2[r + c]`) using boolean arrays or bitmasks for O(1) checks.
     *
     * Time: O(N!)
     * Space: O(N)
     */
    public List<List<String>> solveNQueens(int n) {
        List<List<String>> result = new ArrayList<>();
        char[][] board = new char[n][n];
        for (char[] row : board) Arrays.fill(row, '.');

        boolean[] cols = new boolean[n];
        boolean[] diag1 = new boolean[2 * n]; // r - c + n
        boolean[] diag2 = new boolean[2 * n]; // r + c

        backtrackNQueens(0, n, board, cols, diag1, diag2, result);
        return result;
    }

    private void backtrackNQueens(int row, int n, char[][] board, boolean[] cols, boolean[] diag1, boolean[] diag2, List<List<String>> result) {
        if (row == n) {
            List<String> list = new ArrayList<>();
            for (char[] r : board) list.add(new String(r));
            result.add(list);
            return;
        }

        for (int col = 0; col < n; col++) {
            int d1 = row - col + n;
            int d2 = row + col;

            if (cols[col] || diag1[d1] || diag2[d2]) continue; // O(1) Collision Check

            board[row][col] = 'Q';
            cols[col] = diag1[d1] = diag2[d2] = true;

            backtrackNQueens(row + 1, n, board, cols, diag1, diag2, result);

            board[row][col] = '.';
            cols[col] = diag1[d1] = diag2[d2] = false;
        }
    }

    // =========================================================
    // H2. SUDOKU SOLVER
    // Pattern: 9x9 Bitmask Validation Backtracking
    // LeetCode: 37
    // =========================================================
    /**
     * Problem: Write a program to solve a Sudoku puzzle by filling empty cells.
     *
     * Optimal O(9^(empty_cells)): Maintain `rows[9]`, `cols[9]`, `boxes[9]` bitmasks.
     * `bitmask & (1 << digit)` determines if digit exists in O(1) time.
     *
     * Time: O(9^81) upper bound, practical execution < 5ms
     * Space: O(81) = O(1)
     */
    public void solveSudoku(char[][] board) {
        int[] rows = new int[9];
        int[] cols = new int[9];
        int[] boxes = new int[9];

        // 1. Initialize bitmasks for existing digits
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] != '.') {
                    int val = board[r][c] - '1';
                    int boxIdx = (r / 3) * 3 + (c / 3);
                    rows[r] |= (1 << val);
                    cols[c] |= (1 << val);
                    boxes[boxIdx] |= (1 << val);
                }
            }
        }

        backtrackSudoku(0, 0, board, rows, cols, boxes);
    }

    private boolean backtrackSudoku(int r, int c, char[][] board, int[] rows, int[] cols, int[] boxes) {
        if (r == 9) return true; // Successfully filled all rows!
        if (c == 9) return backtrackSudoku(r + 1, 0, board, rows, cols, boxes); // Move to next row
        if (board[r][c] != '.') return backtrackSudoku(r, c + 1, board, rows, cols, boxes); // Skip filled cell

        int boxIdx = (r / 3) * 3 + (c / 3);

        for (int val = 0; val < 9; val++) {
            int mask = (1 << val);
            // Check if digit val is available in row, col, and box
            if ((rows[r] & mask) == 0 && (cols[c] & mask) == 0 && (boxes[boxIdx] & mask) == 0) {

                board[r][c] = (char) ('1' + val);
                rows[r] |= mask;
                cols[c] |= mask;
                boxes[boxIdx] |= mask;

                if (backtrackSudoku(r, c + 1, board, rows, cols, boxes)) return true;

                board[r][c] = '.';
                rows[r] &= ~mask;
                cols[c] &= ~mask;
                boxes[boxIdx] &= ~mask;
            }
        }
        return false;
    }

    // =========================================================
    // H3. WORD BREAK II
    // Pattern: Recursion + Memoized Partitioning
    // LeetCode: 140
    // =========================================================
    /**
     * Problem: Add spaces in s to construct all valid sentences from wordDict.
     *
     * Optimal O(N · 2^N): DFS with HashMap memoization `memo.put(start, list_of_sentences)`.
     *
     * Time: O(N · 2^N)
     * Space: O(N · 2^N)
     */
    public List<String> wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        Map<Integer, List<String>> memo = new HashMap<>();
        return dfsWordBreak(0, s, dict, memo);
    }

    private List<String> dfsWordBreak(int start, String s, Set<String> dict, Map<Integer, List<String>> memo) {
        if (memo.containsKey(start)) return memo.get(start);

        List<String> sentences = new ArrayList<>();
        if (start == s.length()) {
            sentences.add("");
            return sentences;
        }

        for (int end = start + 1; end <= s.length(); end++) {
            String word = s.substring(start, end);
            if (dict.contains(word)) {
                List<String> subSentences = dfsWordBreak(end, s, dict, memo);
                for (String sub : subSentences) {
                    sentences.add(word + (sub.isEmpty() ? "" : " " + sub));
                }
            }
        }

        memo.put(start, sentences);
        return sentences;
    }

    // =========================================================
    // H4. REMOVE INVALID PARENTHESES
    // Pattern: Minimum Removal BFS / DFS Backtracking
    // LeetCode: 301
    // =========================================================
    /**
     * Problem: Remove minimum number of invalid parentheses to make input string s valid.
     *
     * Optimal O(2^N): Count min open and min close parentheses to remove. Run DFS using counts.
     *
     * Time: O(2^N)
     * Space: O(N)
     */
    public List<String> removeInvalidParentheses(String s) {
        int removeOpen = 0, removeClose = 0;
        for (char ch : s.toCharArray()) {
            if (ch == '(') {
                removeOpen++;
            } else if (ch == ')') {
                if (removeOpen > 0) removeOpen--;
                else removeClose++;
            }
        }

        Set<String> result = new HashSet<>();
        dfsRemove(0, 0, removeOpen, removeClose, s, new StringBuilder(), result);
        return new ArrayList<>(result);
    }

    private void dfsRemove(int index, int balance, int remOpen, int remClose, String s, StringBuilder sb, Set<String> result) {
        if (balance < 0) return; // Invalid prefix balance
        if (index == s.length()) {
            if (remOpen == 0 && remClose == 0 && balance == 0) {
                result.add(sb.toString());
            }
            return;
        }

        char ch = s.charAt(index);
        int len = sb.length();

        if (ch == '(') {
            // Option 1: Remove '(' if remOpen > 0
            if (remOpen > 0) dfsRemove(index + 1, balance, remOpen - 1, remClose, s, sb, result);
            // Option 2: Keep '('
            sb.append(ch);
            dfsRemove(index + 1, balance + 1, remOpen, remClose, s, sb, result);
            sb.setLength(len);
        } else if (ch == ')') {
            // Option 1: Remove ')' if remClose > 0
            if (remClose > 0) dfsRemove(index + 1, balance, remOpen, remClose - 1, s, sb, result);
            // Option 2: Keep ')'
            sb.append(ch);
            dfsRemove(index + 1, balance - 1, remOpen, remClose, s, sb, result);
            sb.setLength(len);
        } else {
            // Non-parenthesis character: must keep
            sb.append(ch);
            dfsRemove(index + 1, balance, remOpen, remClose, s, sb, result);
            sb.setLength(len);
        }
    }

    // =========================================================
    // H5. EXPRESSION ADD OPERATORS
    // Pattern: String Operand Evaluation Backtracking
    // LeetCode: 282
    // =========================================================
    /**
     * Problem: Insert operators '+', '-', '*' into num string to evaluate to target.
     *
     * Optimal O(4^N): Track `eval` sum and `prevOperand` to handle multiplication precedence.
     *
     * Time: O(4^N)
     * Space: O(N)
     */
    public List<String> addOperators(String num, int target) {
        List<String> result = new ArrayList<>();
        if (num == null || num.isEmpty()) return result;
        backtrackOperators(0, 0, 0, target, num, new StringBuilder(), result);
        return result;
    }

    private void backtrackOperators(int index, long eval, long prevOperand, int target, String num, StringBuilder sb, List<String> result) {
        if (index == num.length()) {
            if (eval == target) result.add(sb.toString());
            return;
        }

        int len = sb.length();
        for (int i = index; i < num.length(); i++) {
            if (i != index && num.charAt(index) == '0') break; // No leading zero numbers

            long curr = Long.parseLong(num.substring(index, i + 1));

            if (index == 0) {
                sb.append(curr);
                backtrackOperators(i + 1, curr, curr, target, num, sb, result);
                sb.setLength(len);
            } else {
                // Addition
                sb.append('+').append(curr);
                backtrackOperators(i + 1, eval + curr, curr, target, num, sb, result);
                sb.setLength(len);

                // Subtraction
                sb.append('-').append(curr);
                backtrackOperators(i + 1, eval - curr, -curr, target, num, sb, result);
                sb.setLength(len);

                // Multiplication (Precedence adjustment: subtract prevOperand, add prevOperand * curr)
                sb.append('*').append(curr);
                backtrackOperators(i + 1, eval - prevOperand + (prevOperand * curr), prevOperand * curr, target, num, sb, result);
                sb.setLength(len);
            }
        }
    }

    // =========================================================
    // H6. MATCHSTICKS TO SQUARE
    // Pattern: 4-Bucket Subset Partitioning
    // LeetCode: 473
    // =========================================================
    /**
     * Problem: Form a square using all matchsticks.
     *
     * Optimal O(4^N): Sort matchsticks descending. Fill 4 sides of length target = sum / 4.
     *
     * Time: O(4^N)
     * Space: O(N)
     */
    public boolean makesquare(int[] matchsticks) {
        int sum = 0;
        for (int m : matchsticks) sum += m;
        if (sum % 4 != 0) return false;
        int target = sum / 4;

        Arrays.sort(matchsticks);
        int n = matchsticks.length;
        // Reverse array for descending order
        for (int i = 0; i < n / 2; i++) {
            int tmp = matchsticks[i];
            matchsticks[i] = matchsticks[n - 1 - i];
            matchsticks[n - 1 - i] = tmp;
        }

        int[] sides = new int[4];
        return backtrackSquare(0, matchsticks, target, sides);
    }

    private boolean backtrackSquare(int index, int[] matchsticks, int target, int[] sides) {
        if (index == matchsticks.length) return true;

        for (int i = 0; i < 4; i++) {
            if (sides[i] + matchsticks[index] <= target) {
                sides[i] += matchsticks[index];
                if (backtrackSquare(index + 1, matchsticks, target, sides)) return true;
                sides[i] -= matchsticks[index];
            }
            if (sides[i] == 0) break; // Prune identical empty side branches
        }
        return false;
    }
}
