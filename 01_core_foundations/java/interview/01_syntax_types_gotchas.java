package interview;

import java.math.BigDecimal;

/**
 * Java Syntax, Primitives & String Pool Tricky Gotchas
 *
 * Demonstrates:
 * 1. Integer Cache boundary behavior (-128 to 127) and auto-boxing equality
 * 2. Floating-point precision loss (0.1 + 0.2 != 0.3) & Double.NaN comparisons
 * 3. Ternary Operator Numeric Promotion rules (flag ? 1 : 2.0)
 * 4. String Pool interning vs Heap Object allocations
 */
class SyntaxTypesGotchasDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Integer Caching Behavior (-128 to 127) ===");
        demonstrateIntegerCache();

        System.out.println("\n=== 2. Floating-Point IEEE 754 Precision & NaN Traps ===");
        demonstrateFloatingPointTraps();

        System.out.println("\n=== 3. Ternary Operator Numeric Type Promotion ===");
        demonstrateTernaryPromotion();

        System.out.println("\n=== 4. String Pool vs Heap Object Allocations ===");
        demonstrateStringPool();

        System.out.println("\n[SUCCESS] Syntax & Types gotchas demonstration completed cleanly.");
    }

    private static void demonstrateIntegerCache() {
        Integer a = 100;
        Integer b = 100;
        System.out.println("  Integer a = 100, b = 100 -> (a == b)? " + (a == b) + "  // Cached in IntegerCache");

        Integer x = 200;
        Integer y = 200;
        System.out.println("  Integer x = 200, y = 200 -> (x == y)? " + (x == y) + " // Heap allocated distinct objects");
        System.out.println("  (x.equals(y))?                         " + x.equals(y) + "  // Value equality check");
    }

    private static void demonstrateFloatingPointTraps() {
        double d1 = 0.1 + 0.2;
        System.out.println("  0.1 + 0.2 = " + d1);
        System.out.println("  (0.1 + 0.2 == 0.3)? " + (d1 == 0.3) + " // False due to binary fraction rounding");

        // Solution with BigDecimal
        BigDecimal bd1 = new BigDecimal("0.1");
        BigDecimal bd2 = new BigDecimal("0.2");
        System.out.println("  BigDecimal(\"0.1\").add(BigDecimal(\"0.2\")): " + bd1.add(bd2));

        // Double.NaN comparison rule
        double nan = Double.NaN;
        System.out.println("  (Double.NaN == Double.NaN)? " + (nan == nan) + " // False per IEEE 754 spec!");
        System.out.println("  Double.isNaN(Double.NaN)?   " + Double.isNaN(nan));
    }

    private static void demonstrateTernaryPromotion() {
        boolean flag = true;

        // Condition evaluates '1' (int) and '2.0' (double) -> common promoted type is double (1.0)
        Object result = flag ? 1 : 2.0;
        System.out.println("  Object result = true ? 1 : 2.0;");
        System.out.println("  Result Type:  " + result.getClass().getName());
        System.out.println("  Result Value: " + result + " // Auto-promoted to Double 1.0!");
    }

    private static void demonstrateStringPool() {
        String s1 = "JavaInterview";
        String s2 = "JavaInterview";
        String s3 = new String("JavaInterview");

        System.out.println("  s1 = \"JavaInterview\", s2 = \"JavaInterview\", s3 = new String(\"JavaInterview\")");
        System.out.println("  (s1 == s2)? " + (s1 == s2) + " // Same String Pool reference");
        System.out.println("  (s1 == s3)? " + (s1 == s3) + " // False! s3 is distinct heap object");

        String s3Interned = s3.intern();
        System.out.println("  (s1 == s3.intern())? " + (s1 == s3Interned) + " // True! Canonical String Pool reference returned");
    }
}
