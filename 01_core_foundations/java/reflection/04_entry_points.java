/**
 * ============================================================
 *  JAVA REFLECTION — ENTRY POINTS & INSPECTION
 * ============================================================
 *
 * Focus:
 *   1. The three ways to get a Class<T> object
 *   2. getModifiers() — checking access & characteristics
 *   3. getFields() vs getDeclaredFields()
 *   4. getMethods() vs getDeclaredMethods()
 *
 * Run: javac 04_entry_points.java && java EntryPointsDemo
 * ============================================================
 */
import java.lang.reflect.*;
import java.util.Arrays;

// ─────────────────────────────────────────────────────────────
// Sample class hierarchy used across all demos
// ─────────────────────────────────────────────────────────────

class Animal {
    public    String species = "Unknown";        // public → inherited by Child
    protected String sound   = "...";            // protected → inherited
    private   int    age     = 0;                // private → NOT inherited

    public    void breathe()          { System.out.println("breathing"); }
    public    String getSpecies()     { return species; }
    protected void   sleep()          { System.out.println("sleeping"); }
    private   void   heartbeat()      { System.out.println("beating"); }
}

class Dog extends Animal {
    public    String name;               // public field in Dog itself
    private   String breed;              // private field in Dog itself
    static    int    dogCount = 0;       // static field
    final     String tag      = "DOG";   // final field

    public  Dog(String name, String breed) {
        this.name = name; this.breed = breed; dogCount++;
    }

    public    void bark()               { System.out.println("Woof!"); }
    public    static int getCount()     { return dogCount; }
    private   void   fetch()            { System.out.println("fetching"); }
    protected void   sniff()            { System.out.println("sniffing"); }
}

// ─────────────────────────────────────────────────────────────
public class EntryPointsDemo {

    public static void main(String[] args) throws Exception {
        demo1_threeWaysToGetClass();
        demo2_getModifiers();
        demo3_fields_getFields_vs_getDeclaredFields();
        demo4_methods_getMethods_vs_getDeclaredMethods();
        demo5_practical_inspector();
    }

    // =========================================================
    // Demo 1: The Three Ways to Get a Class<T> Object
    // =========================================================
    static void demo1_threeWaysToGetClass() throws ClassNotFoundException {
        separator("Demo 1: Three Ways to Get a Class Object");

        // ── Way 1: .class LITERAL ─────────────────────────────
        // - Evaluated at COMPILE time (no exception possible)
        // - Does NOT trigger class initialization (static blocks not run)
        // - Type-safe: returns Class<Dog>
        // - Use when you know the type at compile time
        Class<Dog> c1 = Dog.class;
        System.out.println("1. .class literal:   " + c1.getSimpleName());

        // ── Way 2: getClass() ON AN INSTANCE ──────────────────
        // - Returns the ACTUAL runtime type (important with polymorphism)
        // - Always returns Class<?> — you lose the compile-time type parameter
        // - Triggers class initialization if not already done
        Animal animal = new Dog("Rex", "Labrador");   // declared as Animal, actual type Dog
        Class<?> c2 = animal.getClass();
        System.out.println("2. getClass():        " + c2.getSimpleName()); // "Dog", NOT "Animal"!

        // ── Way 3: Class.forName() ─────────────────────────────
        // - Fully-qualified class name as a String — dynamic
        // - Triggers class loading AND initialization (static blocks run)
        // - Throws ClassNotFoundException if class not on classpath
        // - Use for plugin systems, config-driven class loading
        try {
            Class<?> c3 = Class.forName("Dog");   // simple name works if in default package
            System.out.println("3. Class.forName():  " + c3.getSimpleName());
        } catch (ClassNotFoundException e) {
            System.out.println("3. Class.forName():  " + e.getMessage());
        }

        // ── KEY DIFFERENCE: Polymorphism ──────────────────────
        System.out.println("\n--- Polymorphism test ---");
        Animal ref = new Dog("Buddy", "Beagle");
        System.out.println("Dog.class == ref.getClass()? " + (Dog.class == ref.getClass())); // true
        System.out.println("Animal.class == ref.getClass()? " + (Animal.class == ref.getClass())); // false!

        // ── Same Class object in JVM ───────────────────────────
        Dog d1 = new Dog("A", "X");
        Dog d2 = new Dog("B", "Y");
        System.out.println("d1.getClass() == d2.getClass()? " + (d1.getClass() == d2.getClass())); // true — ONE Class object per type
    }

    // =========================================================
    // Demo 2: getModifiers() — Checking Access & Characteristics
    // =========================================================
    static void demo2_getModifiers() throws Exception {
        separator("Demo 2: getModifiers()");

        // ── What getModifiers() returns ────────────────────────
        // Returns a bitmask int. Use java.lang.reflect.Modifier to decode it.
        //
        // Modifier constants (can be combined with |):
        //   Modifier.PUBLIC    = 0x0001
        //   Modifier.PRIVATE   = 0x0002
        //   Modifier.PROTECTED = 0x0004
        //   Modifier.STATIC    = 0x0008
        //   Modifier.FINAL     = 0x0010
        //   Modifier.ABSTRACT  = 0x0400
        //   ...etc

        System.out.println("── CLASS MODIFIERS ───────────────────────");
        inspectClassModifiers(Dog.class);
        inspectClassModifiers(Animal.class);
        inspectClassModifiers(Runnable.class);

        System.out.println("\n── FIELD MODIFIERS ───────────────────────");
        for (Field f : Dog.class.getDeclaredFields()) {
            int mods = f.getModifiers();
            System.out.printf("  %-12s | public=%-5b private=%-5b static=%-5b final=%-5b%n",
                f.getName(),
                Modifier.isPublic(mods),
                Modifier.isPrivate(mods),
                Modifier.isStatic(mods),
                Modifier.isFinal(mods)
            );
        }

        System.out.println("\n── METHOD MODIFIERS ──────────────────────");
        for (Method m : Dog.class.getDeclaredMethods()) {
            int mods = m.getModifiers();
            System.out.printf("  %-15s | public=%-5b private=%-5b protected=%-5b static=%-5b%n",
                m.getName(),
                Modifier.isPublic(mods),
                Modifier.isPrivate(mods),
                Modifier.isProtected(mods),
                Modifier.isStatic(mods)
            );
        }

        System.out.println("\n── MODIFIER.toString() ───────────────────");
        for (Field f : Dog.class.getDeclaredFields()) {
            System.out.println("  " + Modifier.toString(f.getModifiers()) + " " + f.getName());
        }
    }

    static void inspectClassModifiers(Class<?> c) {
        int mods = c.getModifiers();
        System.out.printf("  %-15s | public=%-5b interface=%-5b abstract=%-5b final=%-5b%n",
            c.getSimpleName(),
            Modifier.isPublic(mods),
            Modifier.isInterface(mods),
            Modifier.isAbstract(mods),
            Modifier.isFinal(mods)
        );
    }

    // =========================================================
    // Demo 3: getFields() vs getDeclaredFields()
    // =========================================================
    /**
     * THE KEY DISTINCTION (memorize this):
     *
     * getFields()         → ALL public fields in THIS class + ALL ANCESTORS (inherited)
     *                        Does NOT include private/protected — even if defined here.
     *
     * getDeclaredFields() → ALL fields (public, private, protected, package-private)
     *                        declared DIRECTLY in THIS class ONLY.
     *                        Does NOT include inherited fields at all.
     *
     * To get ALL fields including private AND inherited:
     *   Walk up the class hierarchy manually using getSuperclass().
     */
    static void demo3_fields_getFields_vs_getDeclaredFields() throws Exception {
        separator("Demo 3: getFields() vs getDeclaredFields()");

        Class<?> c = Dog.class;

        // ── getFields() ────────────────────────────────────────
        System.out.println("getFields() — public fields in Dog + all ancestors:");
        for (Field f : c.getFields()) {
            System.out.println("  [" + f.getDeclaringClass().getSimpleName() + "] "
                + Modifier.toString(f.getModifiers()) + " " + f.getName());
        }
        // Expected: Dog.name, Dog.dogCount, Animal.species
        // NOT included: Dog.breed (private), Dog.tag (see note), Animal.sound (protected), Animal.age (private)

        System.out.println("\ngetDeclaredFields() — ALL fields declared ONLY in Dog:");
        for (Field f : c.getDeclaredFields()) {
            System.out.println("  [" + f.getDeclaringClass().getSimpleName() + "] "
                + Modifier.toString(f.getModifiers()) + " " + f.getName());
        }
        // Expected: Dog.name, Dog.breed, Dog.dogCount, Dog.tag
        // NOT included: Animal.species, Animal.sound, Animal.age (those are in Animal)

        // ── Walking the hierarchy to get ALL fields ────────────
        System.out.println("\n--- All fields including private + inherited (walk hierarchy) ---");
        getAllFields(Dog.class);
    }

    static void getAllFields(Class<?> clazz) {
        if (clazz == null) return;
        for (Field f : clazz.getDeclaredFields()) {
            System.out.println("  [" + clazz.getSimpleName() + "] "
                + Modifier.toString(f.getModifiers()) + " " + f.getName());
        }
        getAllFields(clazz.getSuperclass());  // recurse to parent
    }

    // =========================================================
    // Demo 4: getMethods() vs getDeclaredMethods()
    // =========================================================
    /**
     * THE KEY DISTINCTION (memorize this):
     *
     * getMethods()         → ALL public methods in THIS class + ALL ANCESTORS + Object
     *                        Does NOT include private/protected — even if defined here.
     *
     * getDeclaredMethods() → ALL methods (any access) declared ONLY in THIS class.
     *                        Does NOT include inherited methods.
     *
     * Same rule as fields — just scoped to methods.
     */
    static void demo4_methods_getMethods_vs_getDeclaredMethods() throws Exception {
        separator("Demo 4: getMethods() vs getDeclaredMethods()");

        Class<?> c = Dog.class;

        System.out.println("getMethods() — public methods in Dog + ancestors + Object:");
        for (Method m : c.getMethods()) {
            System.out.println("  [" + m.getDeclaringClass().getSimpleName() + "] "
                + m.getName() + "()");
        }
        // Includes: Dog.bark, Dog.getCount, Animal.breathe, Animal.getSpecies,
        // AND all public Object methods (toString, equals, hashCode, wait, notify, etc.)

        System.out.println("\ngetDeclaredMethods() — ALL methods declared ONLY in Dog:");
        for (Method m : c.getDeclaredMethods()) {
            System.out.println("  [" + m.getDeclaringClass().getSimpleName() + "] "
                + Modifier.toString(m.getModifiers()) + " " + m.getName() + "()");
        }
        // Only: bark (public), getCount (public static), fetch (private), sniff (protected)
        // NOT: Animal's methods, Object's methods
    }

    // =========================================================
    // Demo 5: Practical Inspector — print a full class report
    // =========================================================
    static void demo5_practical_inspector() throws Exception {
        separator("Demo 5: Full Class Inspector");
        printClassReport(Dog.class);
    }

    static void printClassReport(Class<?> c) {
        System.out.println("Class: " + Modifier.toString(c.getModifiers())
            + " " + c.getSimpleName());
        System.out.println("Extends: " + (c.getSuperclass() != null
            ? c.getSuperclass().getSimpleName() : "none"));
        System.out.println("Implements: "
            + Arrays.stream(c.getInterfaces())
                    .map(Class::getSimpleName)
                    .toList());

        System.out.println("\n  OWN Fields (getDeclaredFields):");
        for (Field f : c.getDeclaredFields()) {
            System.out.printf("    %-10s %-12s %s%n",
                Modifier.toString(f.getModifiers()), f.getType().getSimpleName(), f.getName());
        }

        System.out.println("\n  PUBLIC Fields including inherited (getFields):");
        for (Field f : c.getFields()) {
            System.out.printf("    %-10s %-12s %-15s (from %s)%n",
                Modifier.toString(f.getModifiers()), f.getType().getSimpleName(),
                f.getName(), f.getDeclaringClass().getSimpleName());
        }

        System.out.println("\n  OWN Methods (getDeclaredMethods):");
        for (Method m : c.getDeclaredMethods()) {
            System.out.printf("    %-20s %-15s %s(%s)%n",
                Modifier.toString(m.getModifiers()),
                m.getReturnType().getSimpleName(),
                m.getName(),
                Arrays.stream(m.getParameterTypes())
                      .map(Class::getSimpleName)
                      .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b));
        }
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}

/*
 * ─────────────────────────────────────────────────────────────
 *  QUICK REFERENCE SUMMARY
 * ─────────────────────────────────────────────────────────────
 *
 * Getting a Class object:
 * ┌───────────────────┬────────────────────┬───────────────────────────────────┐
 * │ Method            │ When               │ Notes                             │
 * ├───────────────────┼────────────────────┼───────────────────────────────────┤
 * │ Dog.class         │ Type known now     │ Compile-time, no init, type-safe  │
 * │ obj.getClass()    │ Have an instance   │ Actual runtime type (polymorphic) │
 * │ Class.forName(s)  │ Name from config   │ Loads + initializes class         │
 * └───────────────────┴────────────────────┴───────────────────────────────────┘
 *
 * Fields:
 * ┌───────────────────────┬───────────────────┬───────────────────────────────┐
 * │ Method                │ Access levels     │ Scope                         │
 * ├───────────────────────┼───────────────────┼───────────────────────────────┤
 * │ getFields()           │ PUBLIC only       │ This class + ALL ancestors    │
 * │ getDeclaredFields()   │ ALL (any access)  │ THIS class ONLY (no inherited)│
 * └───────────────────────┴───────────────────┴───────────────────────────────┘
 *   → To get ALL (any access + inherited): walk getSuperclass() recursively
 *
 * Methods:
 * ┌───────────────────────┬───────────────────┬───────────────────────────────┐
 * │ Method                │ Access levels     │ Scope                         │
 * ├───────────────────────┼───────────────────┼───────────────────────────────┤
 * │ getMethods()          │ PUBLIC only       │ This class + ancestors + Object│
 * │ getDeclaredMethods()  │ ALL (any access)  │ THIS class ONLY (no inherited)│
 * └───────────────────────┴───────────────────┴───────────────────────────────┘
 *
 * Modifier checks:
 *   Modifier.isPublic(mods)    Modifier.isPrivate(mods)
 *   Modifier.isProtected(mods) Modifier.isStatic(mods)
 *   Modifier.isFinal(mods)     Modifier.isAbstract(mods)
 *   Modifier.isInterface(mods) Modifier.toString(mods)  ← human-readable string
 */
