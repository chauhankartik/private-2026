/**
 * ============================================================
 *  JAVA INTERFACES — ADVANCED
 *  Comparable, Comparator, generics bounds, real-world patterns
 *  (Strategy, Repository, Plugin), and interface segregation.
 * ============================================================
 *
 * Run: javac 03_advanced.java && java AdvancedInterfaceDemo
 */
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// ─────────────────────────────────────────────────────────────
// PART 1: COMPARABLE vs COMPARATOR
// ─────────────────────────────────────────────────────────────

class Student implements Comparable<Student> {
    final String name;
    final double gpa;
    final int    age;

    Student(String name, double gpa, int age) {
        this.name = name; this.gpa = gpa; this.age = age;
    }

    /**
     * NATURAL ordering — defined inside the class.
     * Conventions: return negative if this < other, 0 if equal, positive if this > other.
     * Here: sort by GPA descending.
     */
    @Override
    public int compareTo(Student other) {
        return Double.compare(other.gpa, this.gpa);  // descending GPA
    }

    @Override
    public String toString() {
        return String.format("%s(gpa=%.1f, age=%d)", name, gpa, age);
    }
}

// ─────────────────────────────────────────────────────────────
// PART 2: GENERIC INTERFACE BOUNDS
// ─────────────────────────────────────────────────────────────

/**
 * Stackable — a generic interface.
 * T extends Comparable<T> means T must have a natural ordering.
 */
interface Stackable<T extends Comparable<T>> {
    void   push(T item);
    T      pop();
    T      peek();
    boolean isEmpty();
    int    size();

    // Default method using the bound — finds max of all elements
    default T max() {
        if (isEmpty()) throw new NoSuchElementException();
        List<T> snapshot = new ArrayList<>();
        Stackable<T> temp = this;   // can't drain self — just conceptual
        // For demo, we collect via stream from a copy
        return snapshot.stream().max(Comparator.naturalOrder()).orElseThrow();
    }
}

class BoundedStack<T extends Comparable<T>> implements Stackable<T> {
    private final LinkedList<T> data = new LinkedList<>();
    private final int capacity;

    BoundedStack(int capacity) { this.capacity = capacity; }

    @Override public void push(T item) {
        if (data.size() >= capacity) throw new IllegalStateException("Stack full");
        data.push(item);
    }
    @Override public T      pop()     { return data.pop(); }
    @Override public T      peek()    { return data.peek(); }
    @Override public boolean isEmpty() { return data.isEmpty(); }
    @Override public int    size()    { return data.size(); }
}

// ─────────────────────────────────────────────────────────────
// PART 3: STRATEGY PATTERN VIA INTERFACE
// ─────────────────────────────────────────────────────────────

/**
 * DiscountStrategy — pluggable pricing algorithm.
 * Different pricing rules implement the same interface.
 * The Order class doesn't care which strategy is active.
 */
interface DiscountStrategy {
    double apply(double originalPrice);
    String describe();

    // Static factories — convenient without exposing implementation classes
    static DiscountStrategy none()             { return new DiscountStrategy() {
        @Override public double apply(double p) { return p; }
        @Override public String describe()      { return "No discount"; }
    };}
    static DiscountStrategy percentage(double pct) { return new DiscountStrategy() {
        @Override public double apply(double p) { return p * (1 - pct / 100); }
        @Override public String describe()      { return pct + "% off"; }
    };}
    static DiscountStrategy flat(double amount) { return new DiscountStrategy() {
        @Override public double apply(double p) { return Math.max(0, p - amount); }
        @Override public String describe()      { return "₹" + amount + " flat off"; }
    };}
    static DiscountStrategy bogo() { return new DiscountStrategy() {
        @Override public double apply(double p) { return p / 2; }  // buy one get one = 50% off total
        @Override public String describe()      { return "BOGO (50% effective)"; }
    };}
}

class PricingEngine {
    private DiscountStrategy strategy;

    PricingEngine(DiscountStrategy strategy) { this.strategy = strategy; }
    void setStrategy(DiscountStrategy s)     { this.strategy = s; }

    double calculate(double price) { return strategy.apply(price); }

    void print(double price) {
        System.out.printf("  [%s] ₹%.0f → ₹%.0f%n",
            strategy.describe(), price, calculate(price));
    }
}

// ─────────────────────────────────────────────────────────────
// PART 4: REPOSITORY PATTERN — Interface Segregation
// ─────────────────────────────────────────────────────────────

record Product(int id, String name, double price, String category) {}

// Interface Segregation Principle: split into small, focused interfaces
interface Readable<T, ID> {
    Optional<T> findById(ID id);
    List<T>     findAll();
}

interface Writable<T> {
    void save(T entity);
    void delete(T entity);
}

interface ProductQueryable {
    List<Product> findByCategory(String category);
    List<Product> findByPriceRange(double min, double max);
}

// Full repository combines them (for services that need everything)
interface ProductRepository extends Readable<Product, Integer>,
                                      Writable<Product>,
                                      ProductQueryable {}

// Read-only repository (for read-heavy services — avoids exposing write methods)
interface ProductReadRepository extends Readable<Product, Integer>, ProductQueryable {}

/**
 * In-memory implementation — easy to swap with a JPA/SQL version.
 */
class InMemoryProductRepository implements ProductRepository {
    private final Map<Integer, Product> store = new HashMap<>();

    @Override public void save(Product p)   { store.put(p.id(), p); }
    @Override public void delete(Product p) { store.remove(p.id()); }

    @Override public Optional<Product> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override public List<Product> findByCategory(String cat) {
        return store.values().stream()
            .filter(p -> p.category().equals(cat)).toList();
    }

    @Override public List<Product> findByPriceRange(double min, double max) {
        return store.values().stream()
            .filter(p -> p.price() >= min && p.price() <= max).toList();
    }
}

// ─────────────────────────────────────────────────────────────
// PART 5: PLUGIN SYSTEM (Interface as extension point)
// ─────────────────────────────────────────────────────────────

interface ReportPlugin {
    String format();           // "CSV", "PDF", "JSON"
    String generate(List<?> data);
}

class CsvPlugin implements ReportPlugin {
    @Override public String format() { return "CSV"; }
    @Override public String generate(List<?> data) {
        return data.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}

class JsonPlugin implements ReportPlugin {
    @Override public String format() { return "JSON"; }
    @Override public String generate(List<?> data) {
        return "[" + data.stream().map(o -> "\"" + o + "\"")
                         .collect(Collectors.joining(", ")) + "]";
    }
}

class ReportEngine {
    private final Map<String, ReportPlugin> plugins = new HashMap<>();

    void register(ReportPlugin plugin) { plugins.put(plugin.format(), plugin); }

    String generate(String format, List<?> data) {
        ReportPlugin plugin = plugins.get(format);
        if (plugin == null) throw new IllegalArgumentException("No plugin for: " + format);
        return plugin.generate(data);
    }
}

// ─────────────────────────────────────────────────────────────
// DEMO RUNNER
// ─────────────────────────────────────────────────────────────
public class AdvancedInterfaceDemo {

    public static void main(String[] args) {
        demo1_comparable_vs_comparator();
        demo2_generic_interface_bounds();
        demo3_strategy_pattern();
        demo4_repository_pattern();
        demo5_plugin_system();
    }

    static void demo1_comparable_vs_comparator() {
        separator("Demo 1: Comparable vs Comparator");

        List<Student> students = new ArrayList<>(List.of(
            new Student("Alice",  3.8, 22),
            new Student("Bob",    3.5, 24),
            new Student("Carol",  3.9, 21),
            new Student("Dave",   3.5, 23)
        ));

        // Comparable — natural order (descending GPA, defined IN Student)
        Collections.sort(students);
        System.out.println("  Natural order (desc GPA): " + students);

        // Comparator — external, multiple options
        Comparator<Student> byName       = Comparator.comparing(s -> s.name);
        Comparator<Student> byGpaAsc     = Comparator.comparingDouble(s -> s.gpa);
        Comparator<Student> byAgeThenGpa = Comparator.comparingInt((Student s) -> s.age)
                                                      .thenComparingDouble(s -> s.gpa);

        students.sort(byName);
        System.out.println("  By name:         " + students);

        students.sort(byGpaAsc);
        System.out.println("  By GPA asc:      " + students);

        students.sort(byAgeThenGpa);
        System.out.println("  By age then GPA: " + students);

        // Comparator.comparing with key extractor
        students.sort(Comparator.comparing((Student s) -> s.name).reversed());
        System.out.println("  By name reversed:" + students);

        // PriorityQueue with custom comparator
        PriorityQueue<Student> pq = new PriorityQueue<>(Comparator.comparingDouble(s -> s.gpa));
        pq.addAll(students);
        System.out.println("  PQ (min GPA first): " + pq.poll());
    }

    static void demo2_generic_interface_bounds() {
        separator("Demo 2: Generic Interface Bounds");

        BoundedStack<Integer> stack = new BoundedStack<>(5);
        stack.push(10); stack.push(30); stack.push(20);
        System.out.println("  size: " + stack.size());
        System.out.println("  peek: " + stack.peek());
        System.out.println("  pop:  " + stack.pop());

        // Type safety — compiler rejects non-Comparable
        // BoundedStack<Object> invalid = new BoundedStack<>(5); // won't compile
    }

    static void demo3_strategy_pattern() {
        separator("Demo 3: Strategy Pattern (DiscountStrategy)");

        PricingEngine engine = new PricingEngine(DiscountStrategy.none());
        engine.print(1000);

        engine.setStrategy(DiscountStrategy.percentage(20));
        engine.print(1000);

        engine.setStrategy(DiscountStrategy.flat(150));
        engine.print(1000);

        engine.setStrategy(DiscountStrategy.bogo());
        engine.print(1000);

        // Lambda as strategy (no named class needed for simple rules)
        engine.setStrategy(price -> price > 500 ? price * 0.85 : price);
        engine.print(1000);
        engine.print(400);
    }

    static void demo4_repository_pattern() {
        separator("Demo 4: Repository Pattern (Interface Segregation)");

        InMemoryProductRepository repo = new InMemoryProductRepository();
        repo.save(new Product(1, "Phone",  29999, "Electronics"));
        repo.save(new Product(2, "Laptop", 89999, "Electronics"));
        repo.save(new Product(3, "Desk",   12999, "Furniture"));
        repo.save(new Product(4, "Chair",   8999, "Furniture"));

        // Read-only service only gets the read interface
        ProductReadRepository readOnly = repo;

        System.out.println("  All: " + readOnly.findAll().stream().map(Product::name).toList());
        System.out.println("  Electronics: " + readOnly.findByCategory("Electronics")
            .stream().map(Product::name).toList());
        System.out.println("  10k-30k: " + readOnly.findByPriceRange(10000, 30000)
            .stream().map(Product::name).toList());
        System.out.println("  findById(2): " + readOnly.findById(2).map(Product::name).orElse("-"));

        // Write service uses the full interface
        repo.delete(repo.findById(3).orElseThrow());
        System.out.println("  After delete: " + repo.findAll().stream().map(Product::name).toList());
    }

    static void demo5_plugin_system() {
        separator("Demo 5: Plugin System");

        ReportEngine engine = new ReportEngine();
        engine.register(new CsvPlugin());
        engine.register(new JsonPlugin());

        List<String> data = List.of("Alice", "Bob", "Carol");
        System.out.println("  CSV:  " + engine.generate("CSV", data));
        System.out.println("  JSON: " + engine.generate("JSON", data));
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
