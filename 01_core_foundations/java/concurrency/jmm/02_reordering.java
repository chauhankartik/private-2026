/**
 * ============================================================
 *  JAVA MEMORY MODEL — REORDERING, SAFE PUBLICATION & final
 * ============================================================
 *
 * Demonstrates:
 *   1. Instruction reordering — observable between threads
 *   2. The broken Singleton (DCL without volatile)
 *   3. Safe publication idioms — four patterns
 *   4. final fields — JMM guarantee and the constructor escape bug
 *   5. Immutable objects — the safest concurrency tool
 *
 * Run: javac 02_reordering.java && java ReorderingDemo
 * ============================================================
 */
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ReorderingDemo {

    public static void main(String[] args) throws Exception {
        demo1_reorderingObservable();
        demo2_brokenSingleton();
        demo3_fixedSingleton_volatile();
        demo4_fixedSingleton_holder();
        demo5_safePublication();
        demo6_finalFields();
        demo7_immutableObjects();
    }

    // =========================================================
    // Demo 1: Reordering — Observable Between Threads
    // =========================================================
    /**
     * The classic reordering litmus test (Dekker's example):
     *
     *   Thread A:   x = 1; r1 = y;
     *   Thread B:   y = 1; r2 = x;
     *
     * Sequential consistency would say: at least one of {r1, r2} must be 1.
     * (Because either A's x=1 happened before B reads x, or B's y=1 before A reads y.)
     *
     * BUT: with reordering, BOTH r1=0 AND r2=0 is possible!
     *   CPU A: reorders to: r1=y; x=1; (reads y=0 before writing x=1)
     *   CPU B: reorders to: r2=x; y=1; (reads x=0 before writing y=1)
     *
     * Without memory barriers, modern CPUs (x86/ARM) permit this.
     * The JMM says: without HB, this is allowed.
     *
     * Running this in Java: hard to see because x86 is strongly ordered.
     * The important thing is the JMM ALLOWS it — your code must not DEPEND on ordering
     * without HB.
     */
    static int x, y, r1, r2;

    static void demo1_reorderingObservable() throws Exception {
        separator("Demo 1: Reordering — JMM Allows r1=0 AND r2=0");

        int reorderings = 0;
        int iterations  = 100_000;

        for (int i = 0; i < iterations; i++) {
            x = 0; y = 0; r1 = 0; r2 = 0;

            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done  = new CountDownLatch(2);

            Thread a = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) {}
                x = 1;   // may be reordered with r1 = y
                r1 = y;
                done.countDown();
            });

            Thread b = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) {}
                y = 1;   // may be reordered with r2 = x
                r2 = x;
                done.countDown();
            });

            a.start(); b.start();
            start.countDown();  // release both threads simultaneously
            done.await();

            if (r1 == 0 && r2 == 0) reorderings++;
        }

        System.out.println("  Reorderings observed (r1=0 AND r2=0): " + reorderings
            + " / " + iterations);
        System.out.println("  (On x86 this is rare but possible; ARM shows it more often)");
        System.out.println("""
          
          KEY INSIGHT:
          Even if reorderings=0 here, the JMM PERMITS them.
          Code that depends on ordering without HB is BROKEN — it may fail
          on a different CPU, a different JVM version, or after JIT optimization.
        """);
    }

    // =========================================================
    // Demo 2: Broken Singleton — DCL Without volatile
    // =========================================================
    /**
     * The Double-Checked Locking (DCL) pattern WITHOUT volatile is BROKEN
     * because of reordering inside the synchronized block.
     *
     * new Singleton() expands to approximately:
     *   1. temp = allocate(Singleton.class)     // allocate memory
     *   2. instance = temp                       // PUBLISH reference ← step 2
     *   3. temp.init()                           // INITIALIZE fields ← step 3
     *
     * Steps 2 and 3 can be REORDERED by the JIT/CPU:
     *   → instance is non-null but uninitialized
     *   → Thread B reads non-null instance, calls method, sees uninitialized fields
     */
    static class BrokenSingleton {
        private static BrokenSingleton INSTANCE;  // NOT volatile — BROKEN!

        private final String data;

        private BrokenSingleton() {
            // Simulate time-consuming initialization
            try { Thread.sleep(1); } catch (InterruptedException e) {}
            data = "initialized";
        }

        static BrokenSingleton get() {
            if (INSTANCE == null) {                   // Check 1 — no lock
                synchronized (BrokenSingleton.class) {
                    if (INSTANCE == null) {           // Check 2 — with lock
                        INSTANCE = new BrokenSingleton();  // ← reordering possible!
                    }
                }
            }
            return INSTANCE;
        }

        // Returns null if thread sees uninitialized object (rare but possible)
        String getData() { return data; }
    }

    static void demo2_brokenSingleton() {
        separator("Demo 2: Broken Singleton (DCL without volatile)");
        System.out.println("""
          Problem: new Singleton() is NOT atomic.
          It can be reordered to: publish reference → then initialize fields.
          
          Thread A: synchronized { INSTANCE = new Singleton(); }
          JIT may reorder to:
            allocate memory
            write reference to INSTANCE  ← Thread B sees non-null here!
            initialize fields            ← NOT done yet when B reads
          
          Thread B: if (INSTANCE != null) → true (reference written but fields empty)
                    INSTANCE.getData()   → null! (data field not yet initialized)
          
          Without volatile, the JMM permits this reordering because
          the JIT cannot see across thread boundaries.
        """);

        // We can't reliably trigger this bug — it's JVM/hardware dependent.
        // But the pattern is structurally broken according to the JMM.
        BrokenSingleton s = BrokenSingleton.get();
        System.out.println("  get() returned data='" + s.getData() + "'");
        System.out.println("  (May be null under reordering — JMM does not guarantee it's safe)");
    }

    // =========================================================
    // Demo 3: Fixed Singleton — volatile DCL
    // =========================================================
    static class FixedSingleton_DCL {
        private static volatile FixedSingleton_DCL INSTANCE;  // ← volatile is the fix

        private final String data;

        private FixedSingleton_DCL() {
            data = "properly-initialized";
        }

        static FixedSingleton_DCL get() {
            if (INSTANCE == null) {
                synchronized (FixedSingleton_DCL.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new FixedSingleton_DCL();
                        // volatile write: StoreStore barrier before this write
                        // → initialization MUST complete before reference is written
                        // volatile write: StoreLoad barrier after this write
                        // → all subsequent reads see the initialized object
                    }
                }
            }
            return INSTANCE;
        }
    }

    static void demo3_fixedSingleton_volatile() {
        separator("Demo 3: Fixed Singleton — volatile DCL");

        for (int i = 0; i < 5; i++) {
            System.out.println("  get() → data='" + FixedSingleton_DCL.get().data + "'");
        }

        System.out.println("""
          
          WHY volatile FIXES IT:
          volatile write inserts a StoreStore barrier BEFORE writing INSTANCE.
          This means: all initialization writes MUST complete before INSTANCE is written.
          Any thread that reads INSTANCE (non-null) is guaranteed to see a fully initialized object.
          
          The first null-check (outside synchronized) is safe because:
          - If INSTANCE is non-null, the volatile read ensures a fully initialized object.
          - If INSTANCE is null, we enter synchronized where the second check is definitive.
        """);
    }

    // =========================================================
    // Demo 4: Best Singleton — Initialization-on-Demand Holder
    // =========================================================
    /**
     * The PREFERRED singleton pattern in Java.
     * No volatile, no synchronized, no DCL needed.
     *
     * WHY IT WORKS:
     *   Class initialization is guaranteed thread-safe by the JVM specification.
     *   The JVM holds a per-class lock during class initialization (§5.4.2, JLS).
     *   The static field INSTANCE is initialized ONCE when Holder is loaded.
     *   Holder is loaded LAZILY — only when get() is first called.
     *
     * Benefits: thread-safe, lazy, no synchronization overhead, simple.
     */
    static class BestSingleton {
        private final String data = "lazily-initialized";

        private BestSingleton() {}

        private static class Holder {
            static final BestSingleton INSTANCE = new BestSingleton();
            //           ↑ class init is thread-safe by JVM spec
        }

        static BestSingleton get() { return Holder.INSTANCE; }
    }

    static void demo4_fixedSingleton_holder() {
        separator("Demo 4: Best Singleton — Initialization-on-Demand Holder");

        for (int i = 0; i < 5; i++) {
            System.out.println("  get() → data='" + BestSingleton.get().data + "'");
        }

        System.out.println("""
          
          The Holder class is loaded when BestSingleton.get() is called.
          The JVM's class loading mechanism guarantees:
            1. Only one thread initializes the class.
            2. All other threads wait.
            3. After initialization, all threads see the initialized INSTANCE.
          
          This is safe publication via the class initialization guarantee.
          No volatile, no synchronized — zero overhead after the first call.
        """);
    }

    // =========================================================
    // Demo 5: Safe Publication Idioms
    // =========================================================
    /**
     * "Safe publication" = making an object visible to other threads
     * such that they see a fully initialized object (not half-baked).
     *
     * The FOUR safe publication idioms:
     */
    static void demo5_safePublication() {
        separator("Demo 5: Safe Publication Idioms");

        // ── 1. static initializer ────────────────────────────────
        class StaticInit {
            static final String CONSTANT = compute();   // initialized at class load
            static String compute() { return "computed"; }
        }
        System.out.println("  1. Static initializer: " + StaticInit.CONSTANT);

        // ── 2. volatile reference ────────────────────────────────
        class VolatileHolder {
            volatile String[] data;
            void publish(String[] d) { data = d; }   // volatile write
            String[] get()          { return data; } // volatile read — sees initialized array
        }
        VolatileHolder vh = new VolatileHolder();
        vh.publish(new String[]{"a", "b"});
        System.out.println("  2. volatile reference: " + vh.get()[0]);

        // ── 3. properly synchronized ─────────────────────────────
        class SyncHolder {
            String value;
            synchronized void set(String v) { value = v; }
            synchronized String get()       { return value; }
        }
        SyncHolder sh = new SyncHolder();
        sh.set("sync-published");
        System.out.println("  3. synchronized getter/setter: " + sh.get());

        // ── 4. concurrent collections ────────────────────────────
        // Putting an object into a ConcurrentHashMap, BlockingQueue, etc.
        // safely publishes it — the collection's internal synchronization provides HB.
        java.util.concurrent.BlockingQueue<String[]> q = new java.util.concurrent.LinkedBlockingQueue<>();
        q.offer(new String[]{"published", "via", "queue"});
        System.out.println("  4. BlockingQueue: " + q.poll()[0]);

        System.out.println("""
          
          UNSAFE publication (avoid):
            sharedRef = new Foo();   // raw assignment without any of the above
            // Another thread reading sharedRef may see Foo in mid-construction!
          
          SAFE means: the reference assignment and field initialization
          cannot be reordered such that the reference becomes visible before
          the object is fully constructed.
        """);
    }

    // =========================================================
    // Demo 6: final Fields — JMM Guarantee
    // =========================================================
    /**
     * The JMM's final field rule (JSR-133):
     *
     * "An object is considered to be completely initialized when its constructor finishes.
     *  A thread that can only see a reference to an object after that object has been
     *  completely initialized is GUARANTEED to see the correctly initialized values
     *  of that object's final fields."
     *
     * This is implemented via a "final field freeze" barrier:
     *   The JVM inserts a StoreStore barrier at the end of every constructor.
     *   This prevents reordering of final field writes with the constructor exit.
     *
     * IMPORTANT: This only works if the reference is safely published
     * (not leaked from the constructor).
     */
    static void demo6_finalFields() throws InterruptedException {
        separator("Demo 6: final Fields — Guaranteed Visibility");

        class SafePoint {
            final int x;
            final int y;

            SafePoint(int x, int y) {
                this.x = x;
                this.y = y;
                // StoreStore barrier inserted here by JVM
                // → x and y writes cannot be reordered after constructor exit
            }
        }

        // Any thread that receives a reference to SafePoint (after it's constructed)
        // is GUARANTEED to see x and y with their correct values.
        SafePoint[] ref = new SafePoint[1];

        Thread writer = new Thread(() -> {
            ref[0] = new SafePoint(10, 20);   // safe publication via array
        });

        Thread reader = new Thread(() -> {
            SafePoint p;
            while ((p = ref[0]) == null) {}   // spin-wait (not ideal in production)
            System.out.println("  [Reader] x=" + p.x + " y=" + p.y + " (final fields ✓)");
        });

        writer.start(); reader.start();
        writer.join(); reader.join();

        // ── The constructor escape bug ────────────────────────────
        System.out.println("\n  ── Constructor Escape Bug (DO NOT DO THIS) ──");

        class Broken {
            final int value;
            static Broken leakedRef;

            Broken() {
                leakedRef = this;  // ← LEAK 'this' before construction completes!
                value = 42;        // other threads may see leakedRef.value = 0
                // JMM final guarantee does NOT apply when 'this' escapes
            }
        }

        new Broken();
        System.out.println("  Broken.leakedRef.value = " + Broken.leakedRef.value
            + " (42 here, but JMM doesn't guarantee it if accessed concurrently)");
        System.out.println("  NEVER publish 'this' from inside a constructor.");
    }

    // =========================================================
    // Demo 7: Immutable Objects — The Safest Choice
    // =========================================================
    /**
     * A truly immutable object:
     *   1. All fields are private and final.
     *   2. No setter methods.
     *   3. The class is declared final (or all methods non-overridable).
     *   4. 'this' does NOT escape the constructor.
     *   5. Mutable components (arrays, collections) are defensively copied.
     *
     * RESULT: An immutable object can be safely published and shared by any number
     * of threads without synchronization, volatile, or locking.
     *
     * This is why Java's String, Integer, BigDecimal, etc. are immutable —
     * they can be freely shared.
     */
    static void demo7_immutableObjects() throws InterruptedException {
        separator("Demo 7: Immutable Objects — Thread Safety Without Locking");

        final class ImmutablePoint {
            private final int x;
            private final int y;
            private final String[] tags;   // mutable type — must defensive copy!

            ImmutablePoint(int x, int y, String[] tags) {
                this.x    = x;
                this.y    = y;
                this.tags = tags.clone();  // defensive copy of mutable input
            }

            int      getX()    { return x; }
            int      getY()    { return y; }
            String[] getTags() { return tags.clone(); }  // defensive copy on return

            ImmutablePoint translate(int dx, int dy) {
                return new ImmutablePoint(x + dx, y + dy, tags);  // new object, not mutation
            }

            @Override public String toString() {
                return "Point(" + x + ", " + y + ")";
            }
        }

        // Create one immutable point — share it with many threads without any synchronization
        ImmutablePoint point = new ImmutablePoint(5, 10, new String[]{"origin", "fixed"});

        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            final int id = i;
            pool.submit(() -> {
                // Read from shared immutable object — no locking needed!
                System.out.println("  [T-" + id + "] Reading: " + point + " tags=" + point.getTags()[0]);
                // Create a NEW object (doesn't affect shared point)
                ImmutablePoint moved = point.translate(id, id);
                System.out.println("  [T-" + id + "] Translated: " + moved);
            });
        }
        pool.shutdown(); pool.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("""
          
          Immutable objects are thread-safe because:
          1. No state can change after construction → no race conditions.
          2. final fields have JMM freeze guarantee → no visibility issues.
          3. No locks needed → no deadlocks, no contention.
          
          PREFER immutability as the first tool in concurrent design.
          Mutability should be the exception, not the default.
        """);
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
