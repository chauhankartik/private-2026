/**
 * ============================================================
 *  JAVA INTERFACES — BASICS
 *  Defining, implementing, extending, default & static methods.
 * ============================================================
 *
 * Run: javac 01_basics.java && java InterfaceBasics
 */

// ─────────────────────────────────────────────────────────────
// 1. DEFINING AN INTERFACE
// ─────────────────────────────────────────────────────────────

/**
 * A Shape interface — the contract.
 * All members are implicitly public.
 * All abstract methods are implicitly public abstract.
 * All fields are implicitly public static final (constants).
 */
interface Shape {
    double PI = Math.PI;       // public static final — constant

    double area();             // public abstract — must implement
    double perimeter();        // public abstract — must implement

    // Default method (Java 8) — optional to override
    default String describe() {
        return String.format("%s: area=%.2f, perimeter=%.2f",
            getClass().getSimpleName(), area(), perimeter());
    }

    // Static factory method (Java 8) — belongs to the interface
    static Shape circle(double radius) {
        return new Circle(radius);
    }
}

/**
 * Printable — a second interface (demonstrates multiple implementation)
 */
interface Printable {
    void print();

    default String format() {
        return "[Printable] " + toString();
    }
}

// ─────────────────────────────────────────────────────────────
// 2. IMPLEMENTING AN INTERFACE
// ─────────────────────────────────────────────────────────────

/**
 * Circle implements Shape — must provide ALL abstract methods.
 * Default methods (describe()) are inherited automatically.
 */
class Circle implements Shape {
    private final double radius;

    Circle(double radius) { this.radius = radius; }

    @Override
    public double area() { return PI * radius * radius; }

    @Override
    public double perimeter() { return 2 * PI * radius; }

    // Overriding a default method is OPTIONAL
    @Override
    public String describe() {
        return "Circle(r=" + radius + "): " + String.format("area=%.2f", area());
    }
}

/**
 * Rectangle implements both Shape AND Printable.
 * Java allows implementing MULTIPLE interfaces — unlike extends (single class only).
 */
class Rectangle implements Shape, Printable {
    private final double width, height;

    Rectangle(double width, double height) {
        this.width = width; this.height = height;
    }

    @Override public double area()      { return width * height; }
    @Override public double perimeter() { return 2 * (width + height); }

    // Must implement Printable.print()
    @Override public void print() {
        System.out.println("  Rectangle(" + width + "×" + height + ") → " + describe());
    }
}

/**
 * Square extends Rectangle — it inherits BOTH interface implementations.
 * No need to re-implement Shape or Printable.
 */
class Square extends Rectangle {
    Square(double side) { super(side, side); }

    @Override
    public String describe() {
        return "Square(s=" + (int)Math.sqrt(area()) + "): area=" + String.format("%.2f", area());
    }
}

// ─────────────────────────────────────────────────────────────
// 3. INTERFACE EXTENDING INTERFACE
// ─────────────────────────────────────────────────────────────

interface Resizable extends Shape {
    void resize(double factor);          // new abstract method
    default void doubleSize() { resize(2.0); }  // reuse via default
}

class ResizableCircle implements Resizable {
    private double radius;

    ResizableCircle(double radius) { this.radius = radius; }

    @Override public double area()      { return Math.PI * radius * radius; }
    @Override public double perimeter() { return 2 * Math.PI * radius; }

    @Override
    public void resize(double factor) {
        radius *= factor;
        System.out.println("  Resized to radius=" + String.format("%.2f", radius));
    }
}

// ─────────────────────────────────────────────────────────────
// 4. DEFAULT METHOD CONFLICT RESOLUTION
// ─────────────────────────────────────────────────────────────

interface A {
    default String greet() { return "Hello from A"; }
}

interface B {
    default String greet() { return "Hello from B"; }
}

class C implements A, B {
    // REQUIRED: compiler won't compile without explicit resolution
    @Override
    public String greet() {
        return A.super.greet() + " | " + B.super.greet();
    }
}

// ─────────────────────────────────────────────────────────────
// 5. MARKER INTERFACE
// ─────────────────────────────────────────────────────────────

interface Saveable { /* no methods — just a tag */ }

class Document implements Saveable {
    String content;
    Document(String content) { this.content = content; }
}

// ─────────────────────────────────────────────────────────────
// 6. DEMO RUNNER
// ─────────────────────────────────────────────────────────────
public class InterfaceBasics {
    public static void main(String[] args) {
        separator("Demo 1: Basic Implementation");
        Shape circle    = new Circle(5);
        Shape rectangle = new Rectangle(4, 6);
        Shape square    = new Square(4);

        // Using the interface reference — polymorphism
        for (Shape s : new Shape[]{ circle, rectangle, square }) {
            System.out.println("  " + s.describe());
        }

        separator("Demo 2: Static Factory on Interface");
        // Shape.circle() is a static method ON the interface
        Shape c = Shape.circle(3.0);
        System.out.println("  Via static factory: " + c.describe());

        separator("Demo 3: Multiple Interfaces (Shape + Printable)");
        Rectangle rect = new Rectangle(3, 7);
        rect.print();    // Printable.print()
        System.out.println("  " + rect.describe());  // Shape.describe()
        System.out.println("  " + rect.format());    // Printable.format()

        separator("Demo 4: Interface Extending Interface (Resizable)");
        ResizableCircle rc = new ResizableCircle(4);
        System.out.println("  Before: " + rc.describe());
        rc.resize(1.5);                    // own method
        rc.doubleSize();                   // inherited default from Resizable
        System.out.println("  After:  " + rc.describe());

        // Can assign to any interface in the hierarchy
        Shape s       = rc;   // Resizable extends Shape
        Resizable r   = rc;   // direct type

        separator("Demo 5: Default Method Conflict Resolution");
        C obj = new C();
        System.out.println("  " + obj.greet());

        separator("Demo 6: Marker Interface");
        Document doc = new Document("Hello World");
        if (doc instanceof Saveable) {
            System.out.println("  Document is Saveable — can be persisted");
        }

        separator("Demo 7: instanceof Checks");
        Object o = new Rectangle(2, 3);
        System.out.println("  o instanceof Shape?     " + (o instanceof Shape));
        System.out.println("  o instanceof Printable? " + (o instanceof Printable));
        System.out.println("  o instanceof Resizable? " + (o instanceof Resizable));
        System.out.println("  o instanceof Saveable?  " + (o instanceof Saveable));
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}

/*
 * INTERVIEW TALKING POINTS:
 *
 * Q: Why does Java not allow multiple class inheritance but allows multiple interface implementation?
 * A: The "Diamond Problem" — if two parent classes have the same method with different
 *    implementations, the compiler can't decide which to use. Interfaces solved this:
 *    - Before Java 8: interfaces had no implementations, so no conflict was possible.
 *    - Java 8+: default methods CAN conflict, but the compiler FORCES you to override.
 *      You must explicitly call A.super.method() or B.super.method().
 *
 * Q: When would you choose an interface over an abstract class?
 * A: Interface when:
 *    - You want multiple types to share a capability (Comparable, Runnable, Closeable).
 *    - You're defining a pure contract with no shared state.
 *    - You want flexibility — callers may implement other things too.
 *    Abstract class when:
 *    - You have partial implementation to share (AbstractList has common List logic).
 *    - You need constructors or instance fields.
 *    - You want protected methods for subclasses.
 */
