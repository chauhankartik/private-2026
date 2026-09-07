package interview;

/**
 * FAANG Staff & Principal Engineer Java Interview Puzzle Suite
 *
 * Compiles 6 interactive puzzles testing tricky Java language edge cases with automated verification assertions.
 */
class StaffInterviewPuzzleSuite {

    public static void main(String[] args) {
        System.out.println("=== Running FAANG Staff Engineer Java Puzzle Suite ===");

        puzzle1_IntegerCacheEquivalence();
        puzzle2_PolymorphicMethodInitialization();
        puzzle3_FinallyOverridingReturn();
        puzzle4_TernaryTypePromotion();
        puzzle5_StringPoolIdentity();
        puzzle6_OverloadResolutionOrder();

        System.out.println("\n[ALL PUZZLES PASSED VERIFICATION cleanly!]");
    }

    private static void puzzle1_IntegerCacheEquivalence() {
        Integer a = 127;
        Integer b = 127;
        Integer c = 128;
        Integer d = 128;

        boolean cond1 = (a == b); // True (Cached)
        boolean cond2 = (c == d); // False (Not cached)

        assert cond1 && !cond2 : "Puzzle 1 Assertion Failed";
        System.out.printf("  [PASS] Puzzle 1 (Integer Cache): 127==127 is %b, 128==128 is %b%n", cond1, cond2);
    }

    static class BasePuzzle {
        int val = 5;
        BasePuzzle() {
            compute();
        }
        void compute() {
            val *= 2;
        }
    }

    static class SubPuzzle extends BasePuzzle {
        int multiplier = 10;
        @Override
        void compute() {
            val += multiplier; // During BasePuzzle(), multiplier is STILL 0!
        }
    }

    private static void puzzle2_PolymorphicMethodInitialization() {
        SubPuzzle sub = new SubPuzzle();
        // BasePuzzle constructor calls SubPuzzle.compute() -> val = 5 + 0 = 5.
        // Then SubPuzzle field multiplier is initialized to 10.
        assert sub.val == 5 : "Puzzle 2 Assertion Failed";
        System.out.printf("  [PASS] Puzzle 2 (Constructor Overridden Call): Sub.val = %d (Not 15!)%n", sub.val);
    }

    @SuppressWarnings("finally")
    private static int puzzle3Helper() {
        try {
            throw new IllegalArgumentException("ERR");
        } finally {
            return 99;
        }
    }

    private static void puzzle3_FinallyOverridingReturn() {
        int result = puzzle3Helper();
        assert result == 99 : "Puzzle 3 Assertion Failed";
        System.out.printf("  [PASS] Puzzle 3 (Finally Exception Swallowing): Returned %d%n", result);
    }

    private static void puzzle4_TernaryTypePromotion() {
        boolean test = true;
        Object obj = test ? 10 : 20.0;
        assert obj instanceof Double && obj.equals(10.0) : "Puzzle 4 Assertion Failed";
        System.out.printf("  [PASS] Puzzle 4 (Ternary Type Promotion): %s value = %s%n", obj.getClass().getSimpleName(), obj);
    }

    private static void puzzle5_StringPoolIdentity() {
        String s1 = "StaffEngineer";
        String s2 = new String("StaffEngineer");
        String s3 = s2.intern();

        assert s1 != s2 && s1 == s3 : "Puzzle 5 Assertion Failed";
        System.out.printf("  [PASS] Puzzle 5 (String Pool Identity): (s1 != s2) is %b, (s1 == s2.intern()) is %b%n", (s1 != s2), (s1 == s3));
    }

    static class OverloadSolver {
        static String resolve(long a, int b) { return "Widening"; }
        static String resolve(Integer a, Integer b) { return "Boxing"; }
        static String resolve(int... args) { return "Varargs"; }
    }

    private static void puzzle6_OverloadResolutionOrder() {
        String res = OverloadSolver.resolve(5, 5); // int 5, int 5 -> Widened to long 5, int 5
        assert res.equals("Widening") : "Puzzle 6 Assertion Failed";
        System.out.printf("  [PASS] Puzzle 6 (Overload Precedence): Selected '%s' over Boxing or Varargs%n", res);
    }
}
