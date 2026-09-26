package interview;

import java.io.IOException;

/**
 * Control Flow & Exception Edge Cases Masterclass
 *
 * Demonstrates:
 * 1. finally block return overriding & exception swallowing
 * 2. sneakyThrow pattern: Throwing checked exceptions without declaring 'throws'
 * 3. Try-with-resources auto-close execution order & Suppressed Exceptions (getSuppressed())
 */
class ControlFlowExceptionsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Finally Block Exception Swallowing & Return Override ===");
        demonstrateFinallySwallowing();

        System.out.println("\n=== 2. Try-With-Resources Suppressed Exceptions ===");
        demonstrateSuppressedExceptions();

        System.out.println("\n=== 3. Sneaky Throw (Erasing Checked Exceptions via Generics) ===");
        demonstrateSneakyThrow();

        System.out.println("\n[SUCCESS] Control Flow & Exception edge cases demonstration completed cleanly.");
    }

    private static void demonstrateFinallySwallowing() {
        int result = returnWithFinally();
        System.out.println("  Method containing throw + finally return -> Returned Value: " + result);
        System.out.println("  >>> Key Insight: Return in finally swallowed RuntimeException(\"PRIMARY_EXCEPTION\")!");
    }

    @SuppressWarnings("finally")
    private static int returnWithFinally() {
        try {
            throw new RuntimeException("PRIMARY_EXCEPTION");
        } finally {
            return 42; // Erases the RuntimeException!
        }
    }

    // AutoCloseable Resource that throws on close
    static class FaultyResource implements AutoCloseable {
        private final String name;

        FaultyResource(String name) {
            this.name = name;
        }

        @Override
        public void close() throws Exception {
            System.out.println("    Closing Resource: " + name);
            throw new IOException("Close Error in " + name);
        }
    }

    private static void demonstrateSuppressedExceptions() {
        try (FaultyResource r1 = new FaultyResource("Resource1");
             FaultyResource r2 = new FaultyResource("Resource2")) {

            System.out.println("  Inside Try block -> Throwing primary exception...");
            throw new IllegalStateException("PRIMARY_TRY_EXCEPTION");

        } catch (Exception e) {
            System.out.println("  Caught Exception: " + e.getMessage());
            Throwable[] suppressed = e.getSuppressed();
            System.out.println("  Suppressed Exceptions Count: " + suppressed.length);
            for (Throwable s : suppressed) {
                System.out.println("    [Suppressed]: " + s.getMessage());
            }
        }
        System.out.println("  >>> Notice: Auto-close order is REVERSE of declaration order (Resource2 closed before Resource1)!");
    }

    // Sneaky Throw Utility (Type Erasure trick to throw checked Exception without declaring throws)
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t; // Erasure casts T to Throwable, bypassing javac checked exception enforcement!
    }

    private static void demonstrateSneakyThrow() {
        try {
            System.out.print("  Executing sneakyThrow(new IOException()): ");
            sneakyThrow(new IOException("Sneaky Checked Exception!"));
        } catch (Exception e) {
            System.out.println("Caught sneaky exception: " + e.getClass().getName() + " -> " + e.getMessage());
        }
    }
}
