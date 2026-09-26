/**
 * ============================================================
 *  JAVA REFLECTION — PHASE 2: MANIPULATION & ACCESS
 * ============================================================
 *
 * Topics:
 *   1. Dynamic Instantiation — Constructor.newInstance()
 *   2. Breaking Encapsulation — setAccessible(true)
 *   3. Method Invocation — Method.invoke(object, args)
 *   4. Field Mutation — field.get(obj) and field.set(obj, value)
 *
 * Run: javac 05_manipulation.java && java ManipulationDemo
 * ============================================================
 */
import java.lang.reflect.*;
import java.util.*;

// ─────────────────────────────────────────────────────────────
// Domain classes used across all demos
// ─────────────────────────────────────────────────────────────

class BankAccount {
    // private — should never be touched from outside
    private String owner;
    private double balance;
    private final String accountNumber;         // final — supposedly immutable
    private static int   totalAccounts = 0;     // static field

    // Public constructor
    public BankAccount(String owner, double initialBalance) {
        this.owner         = owner;
        this.balance       = initialBalance;
        this.accountNumber = "ACC-" + (++totalAccounts);
        System.out.println("  [BankAccount] Created: " + this);
    }

    // Private constructor — used by internal factory, hidden from outside
    private BankAccount(String owner, double balance, String accountNumber) {
        this.owner         = owner;
        this.balance       = balance;
        this.accountNumber = accountNumber;
        totalAccounts++;
    }

    // Public methods
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        balance += amount;
        System.out.println("  [BankAccount] Deposited " + amount + " → balance=" + balance);
    }

    // Private method — internal use only
    private void applyInterest(double rate) {
        balance += balance * rate;
        System.out.println("  [BankAccount] Interest applied at " + (rate * 100) + "% → balance=" + balance);
    }

    // Private static method
    private static int getNextAccountId() { return totalAccounts + 1; }

    @Override
    public String toString() {
        return "BankAccount{owner='" + owner + "', balance=" + balance
             + ", accountNo='" + accountNumber + "'}";
    }
}

class Plugin {
    private final String name;
    private       boolean enabled;

    public Plugin(String name) {
        this.name    = name;
        this.enabled = false;
    }

    private void initialize(String config) {
        System.out.println("  [Plugin:" + name + "] Initialized with config: " + config);
        this.enabled = true;
    }

    private String status() {
        return "Plugin[" + name + "] enabled=" + enabled;
    }
}

// ─────────────────────────────────────────────────────────────
public class ManipulationDemo {

    public static void main(String[] args) throws Exception {
        demo1_dynamicInstantiation();
        demo2_breakingEncapsulation();
        demo3_methodInvocation();
        demo4_fieldMutation();
        demo5_mutatingFinalAndStaticFields();
        demo6_puttingItAllTogether();
    }

    // =========================================================
    // Demo 1: Dynamic Instantiation
    // =========================================================
    /**
     * Problem: You don't know the class at compile time.
     * The class name comes from config, a database, or user input.
     *
     * Solution: Class.forName() + Constructor.newInstance()
     *
     * ─── Why not just clazz.newInstance()? ─────────────────────
     * clazz.newInstance() is DEPRECATED since Java 9.
     * It only calls the no-arg constructor and wraps any exception
     * in InstantiationException, hiding the real cause.
     *
     * Use: clazz.getDeclaredConstructor(...).newInstance(args)
     * This is explicit, supports any constructor, and propagates
     * the real exception via InvocationTargetException.getCause().
     */
    static void demo1_dynamicInstantiation() throws Exception {
        separator("Demo 1: Dynamic Instantiation");

        Class<?> clazz = BankAccount.class;

        // ── 1a. No-arg constructor (doesn't exist here — showing the pattern)
        // Constructor<?> noArg = clazz.getDeclaredConstructor();
        // BankAccount acc = (BankAccount) noArg.newInstance();

        // ── 1b. Public parameterized constructor ──────────────────
        System.out.println("1b. Public ctor (String, double):");
        Constructor<?> publicCtor = clazz.getConstructor(String.class, double.class);
        BankAccount acc1 = (BankAccount) publicCtor.newInstance("Alice", 5000.0);
        System.out.println("    Created: " + acc1);

        // ── 1c. Private constructor ───────────────────────────────
        System.out.println("\n1c. Private ctor (String, double, String):");
        Constructor<?> privateCtor = clazz.getDeclaredConstructor(
            String.class, double.class, String.class
        );
        // setAccessible BEFORE newInstance
        privateCtor.setAccessible(true);
        BankAccount acc2 = (BankAccount) privateCtor.newInstance("Bob", 99999.0, "VIP-001");
        System.out.println("    Created: " + acc2);

        // ── 1d. InvocationTargetException — the real exception ────
        System.out.println("\n1d. Exception inside constructor:");
        try {
            // deposit() will throw if amount <= 0; simulate via constructor logic
            Constructor<?> ctor = clazz.getConstructor(String.class, double.class);
            BankAccount acc3 = (BankAccount) ctor.newInstance("Bad", -100.0);
            // balance is set normally — constructor doesn't validate in our class
            // Let's force an exception differently: call a method that throws
            Method deposit = clazz.getMethod("deposit", double.class);
            deposit.invoke(acc3, -50.0);  // will throw IllegalArgumentException
        } catch (InvocationTargetException e) {
            // ★ InvocationTargetException WRAPS the real cause
            System.out.println("    InvocationTargetException caught");
            System.out.println("    Real cause: " + e.getCause().getClass().getSimpleName()
                + ": " + e.getCause().getMessage());
        }

        // ── 1e. Listing all constructors ─────────────────────────
        System.out.println("\n1e. All constructors on BankAccount:");
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            System.out.println("    " + Modifier.toString(c.getModifiers())
                + " BankAccount(" + Arrays.stream(c.getParameterTypes())
                                          .map(Class::getSimpleName)
                                          .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)
                + ")");
        }
    }

    // =========================================================
    // Demo 2: setAccessible(true) — Breaking Encapsulation
    // =========================================================
    /**
     * setAccessible(true) BYPASSES the Java access control system.
     * It does NOT change the field/method modifier (it's still private).
     * It tells the JVM: "skip the access check for this reflective call."
     *
     * ─── What it affects ──────────────────────────────────────
     * Without setAccessible(true) on a private member:
     *   → IllegalAccessException is thrown immediately.
     *
     * With setAccessible(true):
     *   → You can read/write private fields, call private methods.
     *
     * ─── Security Risk ────────────────────────────────────────
     * 1. Breaks encapsulation: invariants can be violated (e.g., balance < 0).
     * 2. Bypasses final: you can change "immutable" values.
     * 3. Java 9+ Modules: setAccessible is RESTRICTED across module boundaries.
     *    You need `opens com.example to some.other.module` in module-info.java.
     *    Without it: InaccessibleObjectException.
     * 4. SecurityManager (deprecated Java 17, removed Java 18+) could block it.
     *
     * ─── Best practice ────────────────────────────────────────
     * Only use in framework-level code (serializers, DI containers, test utils).
     * NEVER in business logic. Always pair with setAccessible(false) after use
     * (or use try-finally) if you need to restore original behavior.
     */
    static void demo2_breakingEncapsulation() throws Exception {
        separator("Demo 2: setAccessible(true) — Breaking Encapsulation");

        BankAccount acc = new BankAccount("Carol", 1000.0);
        Class<?> clazz = acc.getClass();

        // ── Without setAccessible — throws IllegalAccessException ─
        System.out.println("Without setAccessible:");
        Field balanceField = clazz.getDeclaredField("balance");
        try {
            double val = (double) balanceField.get(acc);  // ← will throw
        } catch (IllegalAccessException e) {
            System.out.println("  ✗ IllegalAccessException: " + e.getMessage());
        }

        // ── With setAccessible(true) ──────────────────────────────
        System.out.println("\nWith setAccessible(true):");
        balanceField.setAccessible(true);                  // bypass access control
        double balance = (double) balanceField.get(acc);   // now works
        System.out.println("  ✓ Read private balance: " + balance);

        // ── canAccess() — check before attempting (Java 9+) ──────
        System.out.println("  canAccess(): " + balanceField.canAccess(acc));  // true (after setAccessible)

        // ── Try-finally pattern (restore when done) ───────────────
        Field ownerField = clazz.getDeclaredField("owner");
        try {
            ownerField.setAccessible(true);
            System.out.println("  Owner: " + ownerField.get(acc));
        } finally {
            ownerField.setAccessible(false);  // restore
        }
    }

    // =========================================================
    // Demo 3: Method.invoke(object, args)
    // =========================================================
    /**
     * Method.invoke(obj, args...) dynamically calls a method.
     *
     * Signature: Object invoke(Object obj, Object... args)
     *   - obj  = the instance to call on (null for static methods)
     *   - args = the method arguments (auto-boxed for primitives)
     *   - returns Object (auto-boxed return value, null for void)
     *
     * ─── Exception mapping ───────────────────────────────────
     * IllegalAccessException      → forgot setAccessible(true)
     * IllegalArgumentException    → wrong number/type of args
     * InvocationTargetException   → the method itself threw — unwrap via getCause()
     */
    static void demo3_methodInvocation() throws Exception {
        separator("Demo 3: Method.invoke()");

        BankAccount acc = new BankAccount("Dave", 2000.0);
        Class<?> clazz = acc.getClass();

        // ── 3a. Public instance method ────────────────────────────
        System.out.println("3a. Public method deposit(500.0):");
        Method deposit = clazz.getMethod("deposit", double.class);
        deposit.invoke(acc, 500.0);     // acc.deposit(500.0)

        // ── 3b. Private instance method ───────────────────────────
        System.out.println("\n3b. Private method applyInterest(0.05):");
        Method applyInterest = clazz.getDeclaredMethod("applyInterest", double.class);
        applyInterest.setAccessible(true);
        applyInterest.invoke(acc, 0.05);  // acc.applyInterest(0.05)

        // ── 3c. Private static method ─────────────────────────────
        System.out.println("\n3c. Private static method getNextAccountId():");
        Method nextId = clazz.getDeclaredMethod("getNextAccountId");
        nextId.setAccessible(true);
        int id = (int) nextId.invoke(null);  // null → static method
        System.out.println("  Next account ID: " + id);

        // ── 3d. Handling InvocationTargetException correctly ──────
        System.out.println("\n3d. Method that throws (deposit with -1):");
        try {
            deposit.invoke(acc, -1.0);
        } catch (InvocationTargetException e) {
            // e itself is not the real exception — getCause() is
            Throwable realCause = e.getCause();
            System.out.println("  InvocationTargetException caught");
            System.out.println("  getCause(): " + realCause.getClass().getSimpleName()
                + ": " + realCause.getMessage());
        }

        // ── 3e. Return value handling ─────────────────────────────
        System.out.println("\n3e. Return value from invoke():");
        System.out.println("  toString() via invoke: " + clazz.getMethod("toString").invoke(acc));

        // ── 3f. Iterating and invoking all getters ────────────────
        System.out.println("\n3f. Calling all public methods with no params:");
        for (Method m : clazz.getMethods()) {
            if (m.getParameterCount() == 0
                    && m.getDeclaringClass() != Object.class) {  // skip Object methods
                Object result = m.invoke(acc);
                System.out.println("  " + m.getName() + "() → " + result);
            }
        }
    }

    // =========================================================
    // Demo 4: Field Mutation — get() and set()
    // =========================================================
    /**
     * field.get(obj)        → reads the field value from obj (returns Object)
     * field.set(obj, value) → writes value into the field of obj
     *
     * For primitives, get() returns the boxed type (int → Integer).
     * For static fields, obj can be null.
     *
     * ─── Type-specific getters (avoid boxing overhead) ───────
     * field.getInt(obj), field.getDouble(obj), field.getBoolean(obj), etc.
     * field.setInt(obj, val), field.setDouble(obj, val), etc.
     */
    static void demo4_fieldMutation() throws Exception {
        separator("Demo 4: Field Mutation — get() and set()");

        BankAccount acc = new BankAccount("Eve", 3000.0);
        Class<?> clazz  = acc.getClass();

        // ── 4a. Reading a private field ───────────────────────────
        Field balanceField = clazz.getDeclaredField("balance");
        balanceField.setAccessible(true);

        System.out.println("4a. Reading private field:");
        System.out.println("  balance (via get):        " + balanceField.get(acc));
        System.out.println("  balance (via getDouble):  " + balanceField.getDouble(acc));  // no boxing

        // ── 4b. Writing a private field ───────────────────────────
        System.out.println("\n4b. Writing private field:");
        System.out.println("  Before: " + acc);
        balanceField.set(acc, 999999.99);      // bypass setters entirely
        System.out.println("  After set(999999.99): " + acc);
        balanceField.setDouble(acc, 1.0);      // type-specific setter
        System.out.println("  After setDouble(1.0): " + acc);

        // ── 4c. Reading a static field ────────────────────────────
        System.out.println("\n4c. Reading/Writing static field:");
        Field totalField = clazz.getDeclaredField("totalAccounts");
        totalField.setAccessible(true);
        System.out.println("  totalAccounts (static): " + totalField.get(null)); // null for static

        // ── 4d. Dumping ALL field values of an object ─────────────
        System.out.println("\n4d. Full field dump of object:");
        fieldDump(acc);
    }

    static void fieldDump(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        System.out.println("  Fields of " + clazz.getSimpleName() + ":");
        for (Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            Object value = Modifier.isStatic(f.getModifiers()) ? f.get(null) : f.get(obj);
            System.out.printf("    %-15s %-10s = %s%n",
                Modifier.toString(f.getModifiers()), f.getName(), value);
        }
    }

    // =========================================================
    // Demo 5: Mutating final and static Fields
    // =========================================================
    /**
     * ─── Mutating final fields ────────────────────────────────
     * final fields are meant to be immutable after construction.
     * Reflection CAN change them in most cases (Java 8–16).
     * Java 17+ restricts this: JEP 416 makes certain core final
     * fields truly immutable; attempts may silently fail or throw.
     * For your own classes, it still works in Java 17–21.
     *
     * ─── Why it's a risk ─────────────────────────────────────
     * Changing a final field violates the JVM's optimization assumptions.
     * The JIT compiler may have inlined the "constant" value.
     * The change via Reflection writes to the object but the JIT-compiled
     * code may still read the old inlined constant → subtle bugs.
     */
    static void demo5_mutatingFinalAndStaticFields() throws Exception {
        separator("Demo 5: Mutating final & static Fields");

        BankAccount acc = new BankAccount("Frank", 500.0);
        Class<?> clazz  = acc.getClass();

        // ── 5a. Mutating a private final field ───────────────────
        Field accNumField = clazz.getDeclaredField("accountNumber");
        accNumField.setAccessible(true);

        System.out.println("5a. Before mutation: accountNumber = " + accNumField.get(acc));
        accNumField.set(acc, "HACKED-999");
        System.out.println("    After mutation:  accountNumber = " + accNumField.get(acc));
        System.out.println("    Full object: " + acc);

        // ── 5b. Mutating a static field ───────────────────────────
        Field totalField = clazz.getDeclaredField("totalAccounts");
        totalField.setAccessible(true);

        System.out.println("\n5b. Before: totalAccounts = " + totalField.get(null));
        totalField.set(null, 1);        // null because it's static
        System.out.println("    After:  totalAccounts = " + totalField.get(null));

        // ── 5c. Security warning ──────────────────────────────────
        System.out.println("\n5c. Security implications:");
        System.out.println("  - We bypassed final: accountNumber is no longer immutable.");
        System.out.println("  - We reset totalAccounts: class-level invariant violated.");
        System.out.println("  - In Java 9+ modules, this requires 'opens' in module-info.java.");
        System.out.println("  - In a JVM with SecurityManager, setAccessible() can be blocked.");
    }

    // =========================================================
    // Demo 6: Putting It All Together — Generic Object Copier
    // =========================================================
    /**
     * Combines all four techniques:
     * 1. Constructor.newInstance() — create blank target
     * 2. getDeclaredFields() — discover all fields
     * 3. setAccessible(true) — access private fields
     * 4. field.get(source) + field.set(target, value) — copy values
     *
     * This is a simplified version of what Apache BeanUtils,
     * MapStruct, and Spring BeanUtils.copyProperties() do.
     */
    static void demo6_puttingItAllTogether() throws Exception {
        separator("Demo 6: Generic Object Copier (Deep Dive)");

        BankAccount source = new BankAccount("Grace", 7500.0);
        source.deposit(2500.0);  // balance is now 10000

        System.out.println("\nSource: " + source);

        BankAccount copy = shallowCopy(source);
        System.out.println("Copy:   " + copy);
        System.out.println("Same object? " + (source == copy));   // false
        System.out.println("Equal fields? (balance check)");

        // Verify independence
        Field bf = BankAccount.class.getDeclaredField("balance");
        bf.setAccessible(true);
        System.out.println("  source.balance: " + bf.get(source));
        System.out.println("  copy.balance:   " + bf.get(copy));
    }

    /**
     * Creates a new instance of the same class and copies all
     * declared fields (including private and final) from source to target.
     */
    @SuppressWarnings("unchecked")
    static <T> T shallowCopy(T source) throws Exception {
        Class<?> clazz = source.getClass();

        // 1. Create a blank instance via no-arg constructor
        //    If no-arg doesn't exist, we'd need sun.misc.Unsafe (framework trick)
        //    Here we use the public (String, double) ctor as the "blank" ctor
        Constructor<?> ctor = clazz.getConstructor(String.class, double.class);
        T target = (T) ctor.newInstance("COPY_PLACEHOLDER", 0.0);

        // 2. Walk all declared fields (this class only; for inheritance, walk superclass too)
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue; // skip static

            field.setAccessible(true);
            Object value = field.get(source);   // 3. read from source
            field.set(target, value);           // 4. write to target
        }
        return target;
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
 * DYNAMIC INSTANTIATION:
 *   Constructor<?> c = clazz.getDeclaredConstructor(String.class, int.class);
 *   c.setAccessible(true);          // if private
 *   Object obj = c.newInstance("Alice", 30);
 *   ✗ Avoid clazz.newInstance() — deprecated, hides real exceptions
 *
 * BREAKING ENCAPSULATION:
 *   field.setAccessible(true)   → skip access check for field reads/writes
 *   method.setAccessible(true)  → skip access check for method invocation
 *   ctor.setAccessible(true)    → skip access check for constructor calls
 *   Risks: violates invariants, final may be bypassed, module system restricts it
 *
 * METHOD INVOCATION:
 *   Object result = method.invoke(obj, arg1, arg2);
 *   For static:  method.invoke(null, args);
 *   For void:    invoke() returns null
 *   Primitives:  args are auto-boxed; return is auto-boxed
 *   Exceptions:  InvocationTargetException → getCause() for the real exception
 *
 * FIELD MUTATION:
 *   field.get(obj)              → read (returns Object, auto-boxed)
 *   field.getDouble(obj)        → read without boxing (primitive fields)
 *   field.set(obj, value)       → write
 *   field.setDouble(obj, val)   → write without boxing
 *   For static fields: pass null as obj
 *
 * EXCEPTION CHEAT-SHEET:
 *   IllegalAccessException      → forgot setAccessible(true)
 *   IllegalArgumentException    → wrong arg types or count
 *   InvocationTargetException   → method/ctor itself threw; unwrap with getCause()
 *   NoSuchFieldException        → field name typo
 *   NoSuchMethodException       → method name or param types wrong
 *   ClassNotFoundException      → class name typo in Class.forName()
 */
