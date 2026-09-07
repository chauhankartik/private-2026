
/**
 * ============================================================
 *  JAVA GENERICS — WILDCARDS & PECS
 *  Unbounded, upper-bounded, lower-bounded wildcards.
 *  Producer Extends, Consumer Super.
 * ============================================================
 *
 * Run: javac 02_wildcards.java && java WildcardsDemo
 */
import java.util.*;
import java.util.function.*;

// ─────────────────────────────────────────────────────────────
// Domain hierarchy for all demos
// ─────────────────────────────────────────────────────────────

class Animal {
    String name;

    Animal(String name) {
        this.name = name;
    }

    void sound() {
        System.out.println("  " + name + ": ...");
    }

    @Override
    public String toString() {
        return "Animal(" + name + ")";
    }
}

class Dog extends Animal {
    Dog(String name) {
        super(name);
    }

    @Override
    public void sound() {
        System.out.println("  " + name + ": Woof!");
    }
}

class Cat extends Animal {
    Cat(String name) {
        super(name);
    }

    @Override
    public void sound() {
        System.out.println("  " + name + ": Meow!");
    }
}

class GuideDog extends Dog {
    GuideDog(String name) {
        super(name);
    }

    @Override
    public void sound() {
        System.out.println("  " + name + ": Woof (guide)!");
    }
}

// ─────────────────────────────────────────────────────────────
public class WildcardsDemo {

    public static void main(String[] args) {
        demo1_invariance_problem();
        demo2_unboundedWildcard();
        demo3_upperBoundedWildcard();
        demo4_lowerBoundedWildcard();
        demo5_pecs();
        demo6_wildcardCapture();
        demo7_real_world_collections_copy();
    }

    // =========================================================
    // Demo 1: The Invariance Problem — Why Wildcards Exist
    // =========================================================
    /**
     * Arrays are COVARIANT: Dog[] IS-A Animal[]
     * → allows ArrayStoreException at runtime
     *
     * Generics are INVARIANT: List<Dog> is NOT a List<Animal>
     * → prevents ClassCastException at compile time (safer!)
     *
     * The problem: we want to write methods that work on
     * List<Dog>, List<Cat>, AND List<Animal>.
     * Solution: wildcards.
     */
    static void demo1_invariance_problem() {
        separator("Demo 1: Invariance — Why Wildcards Are Needed");

        // ── Array covariance (dangerous) ──────────────────────────
        Animal[] animals = new Dog[3]; // compiles — arrays are covariant
        animals[0] = new Dog("Rex");
        try {
            animals[1] = new Cat("Whiskers"); // ArrayStoreException at RUNTIME
        } catch (ArrayStoreException e) {
            System.out.println("  ArrayStoreException: " + e.getMessage()
                    + " ← arrays allow this mistake at runtime");
        }

        // ── Generic invariance (safe) ─────────────────────────────
        // List<Animal> dogs = new ArrayList<Dog>(); // ✗ COMPILE ERROR ✓
        // This would be unsafe because:
        // dogs.add(new Cat(...)) // would compile, breaks at runtime if it were allowed

        System.out.println("  List<Dog> is NOT a subtype of List<Animal> — invariant!");
        System.out.println("  This is CORRECT — it prevents adding a Cat to a dog list.");
        System.out.println("  Solution: List<? extends Animal> for read-only polymorphism.");
    }

    // =========================================================
    // Demo 2: Unbounded Wildcard — <?>
    // =========================================================
    /**
     * List<?> = "a List of SOME unknown type"
     *
     * You can READ elements as Object.
     * You CANNOT add anything (except null) — because the type is unknown.
     *
     * Use when: you only use methods from Object, or you don't care about the type.
     */
    static void demo2_unboundedWildcard() {
        separator("Demo 2: Unbounded Wildcard <?>");

        List<Dog> dogs = List.of(new Dog("Rex"), new Dog("Buddy"));
        List<Cat> cats = List.of(new Cat("Whiskers"));
        List<String> strs = List.of("hello", "world");

        // printList accepts List of ANY type
        printList(dogs);
        printList(cats);
        printList(strs);

        // What you CAN'T do with List<?>:
        List<?> unknown = dogs;
        Object o = unknown.get(0); // ✓ can read as Object
        // unknown.add(new Dog("Rex")); // ✗ COMPILE ERROR — type is unknown!
        // Dog d = (Dog) unknown.get(0); // compiles but unsafe — avoid raw cast

        System.out.println("  Can read as Object: " + unknown.get(0));
        System.out.println("  Cannot add — ensures type safety ✓");
    }

    static void printList(List<?> list) {
        System.out.print("  printList: ");
        for (Object o : list)
            System.out.print(o + " ");
        System.out.println("(size=" + list.size() + ")");
    }

    // =========================================================
    // Demo 3: Upper-Bounded Wildcard — <? extends T>
    // =========================================================
    /**
     * List<? extends Animal> = "a List of Animal or any SUBTYPE of Animal"
     * (Dog, Cat, GuideDog all qualify)
     *
     * You can READ elements as Animal (the upper bound).
     * You CANNOT add anything — because we don't know the EXACT type.
     * (It could be List<Cat> — we can't add a Dog to it!)
     *
     * Use when: you are READING from the list (it PRODUCES values for you).
     * Mnemonic: PECS — Producer Extends.
     */
    static void demo3_upperBoundedWildcard() {
        separator("Demo 3: Upper-Bounded Wildcard <? extends Animal>");

        List<Dog> dogs = new ArrayList<>(List.of(new Dog("Rex"), new Dog("Buddy")));
        List<Cat> cats = new ArrayList<>(List.of(new Cat("Whiskers"), new Cat("Felix")));
        List<GuideDog> guides = new ArrayList<>(List.of(new GuideDog("Buddy-guide")));
        List<Animal> animals = new ArrayList<>(List.of(new Dog("Mix"), new Cat("Mix2")));

        // makeAllSounds accepts List<Dog>, List<Cat>, List<GuideDog>, List<Animal>
        System.out.println("  Dogs:");
        makeAllSounds(dogs);
        System.out.println("  Cats:");
        makeAllSounds(cats);
        System.out.println("  GuideDogs:");
        makeAllSounds(guides);
        System.out.println("  Animals:");
        makeAllSounds(animals);

        // sumWeights — works with any List of Numbers
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> doubles = List.of(1.5, 2.5, 3.5);
        System.out.println("\n  sumWeights(ints):    " + sumWeights(ints));
        System.out.println("  sumWeights(doubles): " + sumWeights(doubles));

        // What you CAN'T do:
        List<? extends Animal> mixed = dogs;
        Animal a = mixed.get(0); // ✓ read as Animal
        // mixed.add(new Dog("Oops")); // ✗ COMPILE ERROR — may be List<Cat>!
        System.out.println("\n  Read OK: " + a + "  | Add: COMPILE ERROR ✓");
    }

    static void makeAllSounds(List<? extends Animal> animals) {
        animals.forEach(a -> System.out.print("    " + a.name + " "));
        System.out.println();
    }

    static double sumWeights(List<? extends Number> numbers) {
        return numbers.stream().mapToDouble(Number::doubleValue).sum();
    }

    // =========================================================
    // Demo 4: Lower-Bounded Wildcard — <? super T>
    // =========================================================
    /**
     * List<? super Dog> = "a List of Dog or any SUPERTYPE of Dog"
     * (Animal, Object both qualify)
     *
     * You can ADD Dog (and subtypes of Dog) — because any supertype list can hold a
     * Dog.
     * You can only READ as Object — because the exact type is unknown (could be
     * List<Animal>).
     *
     * Use when: you are WRITING to the list (it CONSUMES values you give it).
     * Mnemonic: PECS — Consumer Super.
     */
    static void demo4_lowerBoundedWildcard() {
        separator("Demo 4: Lower-Bounded Wildcard <? super Dog>");

        List<Animal> animalList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();
        // List<Cat> catList = new ArrayList<>(); // ← would NOT be accepted

        // addDogs accepts List<Dog>, List<Animal>, List<Object>
        addDogs(animalList);
        addDogs(objectList);
        System.out.println("  animalList after addDogs: " + animalList);
        System.out.println("  objectList after addDogs: " + objectList);

        // What you CAN'T do:
        List<? super Dog> dest = animalList;
        dest.add(new Dog("Added")); // ✓ Dog can go into any Dog-supertype list
        dest.add(new GuideDog("Guide")); // ✓ GuideDog IS-A Dog — also fine
        // dest.add(new Cat("Cat")); // ✗ COMPILE ERROR — Cat is not a Dog subtype
        Object o = dest.get(0); // ✓ can read as Object
        // Animal a = dest.get(0); // ✗ COMPILE ERROR — might be List<Object>

        System.out.println("  Read as Object: " + o + " | Can add Dog ✓");
    }

    static void addDogs(List<? super Dog> list) {
        list.add(new Dog("Rover"));
        list.add(new GuideDog("Guide-G")); // GuideDog extends Dog — also fine
    }

    // =========================================================
    // Demo 5: PECS — Producer Extends, Consumer Super
    // =========================================================
    /**
     * PECS is the key heuristic for choosing wildcards:
     *
     * "If a parameterized type represents a PRODUCER (you READ from it) → extends"
     * "If a parameterized type represents a CONSUMER (you WRITE to it) → super"
     * "If you both read AND write → use a concrete type (no wildcard)"
     *
     * The classic example: Collections.copy(List<? super T> dest, List<? extends T>
     * src)
     * src PRODUCES T values → extends
     * dest CONSUMES T values → super
     */
    static void demo5_pecs() {
        separator("Demo 5: PECS — Producer Extends, Consumer Super");

        List<Dog> src = new ArrayList<>(List.of(new Dog("Rex"), new Dog("Buddy")));
        List<Animal> dest = new ArrayList<>();

        System.out.println("  BEFORE copy: src=" + src + " dest=" + dest);
        copy(src, dest); // Dog extends Animal — satisfies both bounds
        System.out.println("  AFTER  copy: dest=" + dest);

        // GuideDog extends Dog extends Animal — also works
        List<GuideDog> guides = new ArrayList<>(List.of(new GuideDog("G1"), new GuideDog("G2")));
        List<Animal> dest2 = new ArrayList<>();
        copy(guides, dest2);
        System.out.println("  GuideDog → Animal: " + dest2);

        // Numeric PECS example
        List<Integer> intSrc = List.of(1, 2, 3, 4, 5);
        List<Number> numDest = new ArrayList<>();
        copyNumbers(intSrc, numDest);
        System.out.println("  Integer → Number:  " + numDest);

        System.out.println("""

                  PECS decision tree:
                    Reading only?  → ? extends T   (Producer)
                    Writing only?  → ? super T     (Consumer)
                    Both?          → T             (no wildcard — exact type)
                    Don't care?    → ?             (unbounded)
                """);
    }

    // src PRODUCES T → ? extends T
    // dest CONSUMES T → ? super T
    static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src)
            dest.add(item);
    }

    static <T extends Number> void copyNumbers(List<? extends T> src, List<? super T> dest) {
        src.forEach(dest::add);
    }

    // =========================================================
    // Demo 6: Wildcard Capture
    // =========================================================
    /**
     * Sometimes you need to use the captured type of a wildcard in your code.
     * The compiler calls this "wildcard capture."
     *
     * You can use a GENERIC HELPER METHOD to capture the wildcard type:
     * The helper gives the wildcard a name (T), allowing you to work with it.
     */
    static void demo6_wildcardCapture() {
        separator("Demo 6: Wildcard Capture");

        List<?> list = new ArrayList<>(List.of(3, 1, 4, 1, 5, 9, 2, 6));

        // Can't sort List<?> directly — type unknown
        // Collections.sort(list); // ✗ COMPILE ERROR
        // list.set(0, list.get(1)); // ✗ COMPILE ERROR — can't write to List<?>

        // Capture the wildcard via a generic helper
        sortCapture(list); // internally uses <T> to name the unknown type
        System.out.println("  After sort via capture: " + list);

        // Swap elements via wildcard capture
        List<?> other = new ArrayList<>(List.of("banana", "apple", "cherry"));
        swapCapture(other, 0, 1);
        System.out.println("  After swap via capture: " + other);
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<T>> void sortCapture(List<?> list) {
        // We capture ? as T to enable sorting
        sortHelper((List<T>) list);
    }

    static <T extends Comparable<T>> void sortHelper(List<T> list) {
        Collections.sort(list); // now T is a named type — works!
    }

    static <T> void swapCapture(List<?> list, int i, int j) {
        swapHelper((List<T>) list, i, j);
    }

    @SuppressWarnings("unchecked")
    static <T> void swapHelper(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    // =========================================================
    // Demo 7: Real-World — Simulating Collections API
    // =========================================================
    /**
     * Demonstrates the exact patterns used in java.util.Collections.
     */
    static void demo7_real_world_collections_copy() {
        separator("Demo 7: Real-World Collections Patterns");

        // ── Collections.copy equivalent ───────────────────────────
        List<Integer> intList = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        List<Number> numList = new ArrayList<>(Arrays.asList(new Number[5]));
        copyAll(intList, numList);
        System.out.println("  copy Integer→Number: " + numList);

        // ── Collections.max equivalent ─────────────────────────────
        List<Integer> nums = List.of(3, 1, 4, 1, 5, 9, 2, 6);
        System.out.println("  max of ints:    " + myMax(nums));
        System.out.println("  max of strings: " + myMax(List.of("apple", "zebra", "mango")));

        // ── filter with PECS ─────────────────────────────────────────
        List<Dog> dogs = List.of(new Dog("Rex"), new Dog("Buddy"), new Dog("Spot"));
        List<Animal> result = new ArrayList<>();
        filter(dogs, a -> a.name.startsWith("R"), result);
        System.out.println("  filter dogs starting with 'R': " + result);

        // ── addAll equivalent ─────────────────────────────────────
        List<Animal> all = new ArrayList<>();
        addAll(all, new Dog("D1"), new Dog("D2"), new Cat("C1"));
        System.out.println("  addAll mixed: " + all);
    }

    static <T> void copyAll(List<? extends T> src, List<? super T> dest) {
        for (int i = 0; i < src.size(); i++)
            dest.set(i, src.get(i));
    }

    static <T extends Comparable<T>> T myMax(List<? extends T> list) {
        return list.stream().max(Comparator.naturalOrder()).orElseThrow();
    }

    static <T> void filter(List<? extends T> src, Predicate<T> pred, List<? super T> dest) {
        for (T item : src)
            if (pred.test(item))
                dest.add(item);
    }

    @SafeVarargs
    static <T> void addAll(List<? super T> dest, T... items) {
        for (T item : items)
            dest.add(item);
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
