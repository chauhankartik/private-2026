package interview;

/**
 * OOP, Polymorphism & Inheritance Tricky Traps
 *
 * Demonstrates:
 * 1. Polymorphic Field Hiding vs Method Overriding (Fields are statically bound!)
 * 2. Invoking Overridable Method inside Super Constructor (Uninitialized Child field read)
 * 3. Overload Resolution Precedence Hierarchy (Exact > Widening > Boxing > Varargs)
 * 4. Interface Default Method Diamond Resolution
 */
class OOPInheritanceTrapsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Polymorphic Field Hiding vs Method Overriding ===");
        demonstrateFieldHiding();

        System.out.println("\n=== 2. Super Constructor Overridden Method Invocation Trap ===");
        demonstrateConstructorTrap();

        System.out.println("\n=== 3. Overload Resolution Precedence Hierarchy ===");
        demonstrateOverloadPrecedence();

        System.out.println("\n[SUCCESS] OOP & Inheritance traps demonstration completed cleanly.");
    }

    // 1. Field Hiding vs Method Overriding
    static class Parent {
        int x = 10;
        public int getX() { return x; }
    }

    static class Child extends Parent {
        int x = 20; // Hides Parent.x!
        @Override
        public int getX() { return x; }
    }

    private static void demonstrateFieldHiding() {
        Parent p = new Child();
        System.out.println("  Parent p = new Child();");
        System.out.println("  p.x      = " + p.x + " // Static binding uses declared reference type (Parent.x = 10)");
        System.out.println("  p.getX() = " + p.getX() + " // Dynamic dispatch uses actual object type (Child.getX() = 20)");
    }

    // 2. Super Constructor Overridden Method Trap
    static class Base {
        Base() {
            printValue(); // Danger: Invokes overridden method before Sub instance fields are initialized!
        }
        void printValue() {
            System.out.println("    Base printValue()");
        }
    }

    static class Sub extends Base {
        String data = "INITIALIZED_DATA"; // Initialized AFTER Base() finishes!

        @Override
        void printValue() {
            System.out.println("    Sub printValue() -> data length: " + (data == null ? "NULL!" : data.length()));
        }
    }

    private static void demonstrateConstructorTrap() {
        System.out.println("  Instantiating new Sub():");
        Sub sub = new Sub();
        System.out.println("  Sub instantiation complete. Sub.data = " + sub.data);
        System.out.println("  >>> Notice: During Base() execution, Sub.data was NULL!");
    }

    // 3. Overload Resolution Precedence
    static class OverloadTester {
        static String test(long x, long y) { return "Widening (long, long)"; }
        static String test(Integer x, Integer y) { return "Boxing (Integer, Integer)"; }
        static String test(int... args) { return "Varargs (int...)"; }
    }

    private static void demonstrateOverloadPrecedence() {
        System.out.println("  OverloadTester.test(5, 10): " + OverloadTester.test(5, 10));
        System.out.println("  >>> Compiler selects Widening (long, long) BEFORE Boxing or Varargs!");
    }
}
