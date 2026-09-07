/**
 * ============================================================
 *  JAVA REFLECTION — BASICS
 *  Class loading, field access, method invocation, constructors.
 * ============================================================
 *
 * Run: javac 01_basics.java && java ReflectionBasics
 * All demos are self-contained with printed output.
 */
import java.lang.reflect.*;
import java.util.*;

// ─── Sample domain classes used across demos ──────────────────
class Person {
    public    String name;
    private   int    age;
    protected String email;

    public Person() {
        this.name = "Unknown"; this.age = 0; this.email = "";
    }

    public Person(String name, int age) {
        this.name = name; this.age = age; this.email = name.toLowerCase() + "@example.com";
    }

    private Person(String name, int age, String email) {
        this.name = name; this.age = age; this.email = email;
    }

    public  String greet()                   { return "Hi, I'm " + name + " (" + age + ")"; }
    private int    calculateBonus(int base)  { return base + (age * 100); }
    public  static String species()          { return "Homo sapiens"; }

    @Override public String toString() {
        return "Person{name='" + name + "', age=" + age + ", email='" + email + "'}";
    }
}

// ─────────────────────────────────────────────────────────────
public class ReflectionBasics {

    public static void main(String[] args) throws Exception {
        demo1_gettingClassObject();
        demo2_inspectingClassMetadata();
        demo3_readingFields();
        demo4_writingPrivateFields();
        demo5_invokingMethods();
        demo6_invokingPrivateAndStaticMethods();
        demo7_constructors();
        demo8_methodHandleVsReflection();
    }

    // =========================================================
    // Demo 1: Three ways to get a Class object
    // =========================================================
    static void demo1_gettingClassObject() {
        separator("Demo 1: Getting Class Objects");

        // Way 1: .class literal — compile-time, no exception
        Class<Person> c1 = Person.class;
        System.out.println("Person.class:       " + c1);

        // Way 2: getClass() on an instance — actual runtime type
        Object obj = new Person("Alice", 30);
        Class<?> c2 = obj.getClass();
        System.out.println("obj.getClass():     " + c2);

        // Way 3: Class.forName() — dynamic, name from config/DB
        try {
            Class<?> c3 = Class.forName("Person");
            System.out.println("Class.forName():    " + c3);
        } catch (ClassNotFoundException e) {
            System.out.println("Class.forName():    " + e.getMessage());
        }

        // All three refer to the SAME Class object in the JVM
        System.out.println("c1 == c2: " + (c1 == c2)); // true — one Class per type
    }

    // =========================================================
    // Demo 2: Inspecting class metadata
    // =========================================================
    static void demo2_inspectingClassMetadata() {
        separator("Demo 2: Class Metadata");

        Class<?> c = Person.class;

        System.out.println("Name:           " + c.getName());
        System.out.println("SimpleName:     " + c.getSimpleName());
        System.out.println("Superclass:     " + c.getSuperclass());
        System.out.println("Interfaces:     " + Arrays.toString(c.getInterfaces()));
        System.out.println("IsInterface:    " + c.isInterface());
        System.out.println("IsEnum:         " + c.isEnum());
        System.out.println("Modifiers:      " + Modifier.toString(c.getModifiers()));

        System.out.println("\nAll declared fields:");
        for (Field f : c.getDeclaredFields()) {
            System.out.println("  " + Modifier.toString(f.getModifiers())
                + " " + f.getType().getSimpleName() + " " + f.getName());
        }

        System.out.println("\nAll declared methods:");
        for (Method m : c.getDeclaredMethods()) {
            System.out.println("  " + Modifier.toString(m.getModifiers())
                + " " + m.getReturnType().getSimpleName() + " " + m.getName()
                + "(" + Arrays.stream(m.getParameterTypes())
                              .map(Class::getSimpleName)
                              .reduce("", (a, b) -> a.isEmpty() ? b : a + ", b"))
                + ")");
        }
    }

    // =========================================================
    // Demo 3: Reading public and private fields
    // =========================================================
    static void demo3_readingFields() throws Exception {
        separator("Demo 3: Reading Fields");

        Person person = new Person("Bob", 25);
        Class<?> c = person.getClass();

        // Public field — direct access
        Field nameField = c.getField("name");        // getField = public only
        System.out.println("name (public):   " + nameField.get(person));

        // Private field — must bypass access control
        Field ageField = c.getDeclaredField("age");  // getDeclaredField = any access
        System.out.println("Before setAccessible — isAccessible: " + ageField.canAccess(person));

        ageField.setAccessible(true);                // ★ bypass private
        System.out.println("age (private):   " + ageField.get(person));

        // Reading all fields dynamically
        System.out.println("\nAll field values via reflection:");
        for (Field f : c.getDeclaredFields()) {
            f.setAccessible(true);
            System.out.println("  " + f.getName() + " = " + f.get(person));
        }
    }

    // =========================================================
    // Demo 4: Modifying private and final fields
    // =========================================================
    static void demo4_writingPrivateFields() throws Exception {
        separator("Demo 4: Writing Fields (including private)");

        Person person = new Person("Charlie", 20);
        System.out.println("Before: " + person);

        // Modify private field
        Field ageField = Person.class.getDeclaredField("age");
        ageField.setAccessible(true);
        ageField.set(person, 99);   // set value

        System.out.println("After setting age=99: " + person);

        // Cache reflection objects for repeated use (important for perf)
        // WRONG: look up field inside a loop
        // RIGHT: cache it as a static field
    }

    // =========================================================
    // Demo 5: Invoking public instance methods
    // =========================================================
    static void demo5_invokingMethods() throws Exception {
        separator("Demo 5: Invoking Methods");

        Person person = new Person("Diana", 28);
        Class<?> c = person.getClass();

        // Get method by name + parameter types (handles overloads)
        Method greetMethod = c.getMethod("greet");  // no params
        Object result = greetMethod.invoke(person); // invoke on instance
        System.out.println("greet() result:  " + result);

        // If the method throws, InvocationTargetException wraps the real exception
        try {
            Method bad = String.class.getMethod("charAt", int.class);
            bad.invoke("hello", 100);  // IndexOutOfBoundsException
        } catch (InvocationTargetException e) {
            System.out.println("Caught real cause: " + e.getCause().getClass().getSimpleName());
        }
    }

    // =========================================================
    // Demo 6: Invoking private and static methods
    // =========================================================
    static void demo6_invokingPrivateAndStaticMethods() throws Exception {
        separator("Demo 6: Private & Static Methods");

        Person person = new Person("Eve", 35);

        // Private method
        Method bonusMethod = Person.class.getDeclaredMethod("calculateBonus", int.class);
        bonusMethod.setAccessible(true);
        int bonus = (int) bonusMethod.invoke(person, 5000);
        System.out.println("calculateBonus(5000): " + bonus);  // 5000 + 35*100 = 8500

        // Static method — pass null as the instance
        Method speciesMethod = Person.class.getMethod("species");
        String species = (String) speciesMethod.invoke(null);
        System.out.println("species() static: " + species);
    }

    // =========================================================
    // Demo 7: Using constructors
    // =========================================================
    static void demo7_constructors() throws Exception {
        separator("Demo 7: Constructors");

        Class<?> c = Person.class;

        // No-arg constructor
        Constructor<?> noArg = c.getDeclaredConstructor();
        Person p1 = (Person) noArg.newInstance();
        System.out.println("No-arg ctor: " + p1);

        // Public parameterized constructor
        Constructor<?> withArgs = c.getConstructor(String.class, int.class);
        Person p2 = (Person) withArgs.newInstance("Frank", 40);
        System.out.println("Public ctor: " + p2);

        // Private constructor (e.g., Singleton pattern)
        Constructor<?> privCtor = c.getDeclaredConstructor(String.class, int.class, String.class);
        privCtor.setAccessible(true);
        Person p3 = (Person) privCtor.newInstance("Grace", 50, "grace@custom.org");
        System.out.println("Private ctor: " + p3);

        System.out.println("\nAll constructors:");
        for (Constructor<?> ctor : c.getDeclaredConstructors()) {
            System.out.println("  " + Modifier.toString(ctor.getModifiers())
                + " Person(" + Arrays.stream(ctor.getParameterTypes())
                                     .map(Class::getSimpleName)
                                     .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)
                + ")");
        }
    }

    // =========================================================
    // Demo 8: MethodHandles — the modern alternative
    // =========================================================
    /**
     * MethodHandle (java.lang.invoke) is the JIT-friendly alternative to Method.invoke().
     * ~2-5x faster for repeated calls because the JVM can inline and optimize it.
     * Prefer MethodHandle for hot paths in framework code.
     */
    static void demo8_methodHandleVsReflection() throws Throwable {
        separator("Demo 8: MethodHandles vs Method.invoke()");

        Person person = new Person("Henry", 30);

        // Old way: Method.invoke()
        Method greet = Person.class.getMethod("greet");
        System.out.println("Method.invoke():    " + greet.invoke(person));

        // New way: MethodHandle (Java 7+)
        java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
        java.lang.invoke.MethodType mt = java.lang.invoke.MethodType.methodType(String.class);
        java.lang.invoke.MethodHandle greetHandle = lookup.findVirtual(Person.class, "greet", mt);
        System.out.println("MethodHandle:       " + (String) greetHandle.invoke(person));

        System.out.println("\nMethodHandle advantages:");
        System.out.println("  - JIT-optimized (can be inlined)");
        System.out.println("  - Type-safe at handle-creation time");
        System.out.println("  - Preferred for Java 9+ module-aware access");
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
