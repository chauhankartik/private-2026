/**
 * ============================================================
 *  JAVA MEMORY MODEL — VISIBILITY BUGS & volatile
 * ============================================================
 *
 * Demonstrates: what happens WITHOUT visibility guarantees,
 * WHY it happens (CPU caches, store buffers), and HOW
 * volatile and synchronized fix it.
 *
 * Run: javac 01_visibility.java && java VisibilityDemo
 *
 * NOTE: Some demos are inherently flaky because the bug may or
 * may not manifest depending on the JIT / hardware timing.
 * The explanations are the important part.
 * ============================================================
 */
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class VisibilityDemo {

    public static void main(String[] args) throws Exception {
        demo1_staleRead_problem();
        demo2_staleRead_fixed_volatile();
        demo3_staleRead_fixed_synchronized();
        demo4_volatile_piggyback();
        demo5_volatile_vs_atomic();
        demo6_visibility_chain();
    }

    // =========================================================
    // Demo 1: Stale Read — The Visibility Bug
    // =========================================================
    /**
     * WHAT HAPPENS WITHOUT volatile:
     *
     * The JIT compiler may hoist the read of 'running' OUT of the loop:
     *
     *   // Original Java:
     *   while (!running) { work(); }
     *
     *   // JIT may compile to (approximately):
     *   boolean temp = running;   // read ONCE into CPU register
     *   while (!temp) { work(); }
     *
     * The writer thread changes 'running' in memory, but the reader thread
     * never re-reads from memory — it keeps using the stale CPU register value.
     *
     * On multi-core hardware: each core has its own L1/L2 cache.
     * A write on Core 0 goes to Core 0's cache first (store buffer).
     * Core 1 may never see it if there's no cache coherence flush.
     *
     * RESULT: The reader thread may loop forever, even after running=false.
     */
    static volatile boolean stopFlag1 = false;   // change to non-volatile to see the bug
    static          boolean stopFlag2 = false;   // ← this one is deliberately broken

    static void demo1_staleRead_problem() throws InterruptedException {
        separator("Demo 1: Stale Read (non-volatile flag)");

        // Reader thread — reads the flag continuously
        Thread reader = new Thread(() -> {
            long count = 0;
            while (!stopFlag2) {   // stopFlag2 is NOT volatile — may never see update
                count++;
                if (count > 500_000_000L) {
                    System.out.println("  [Reader] Gave up after 500M iterations — stale read demonstrated!");
                    return;
                }
            }
            System.out.println("  [Reader] Saw the flag change (lucky — hardware flushed the cache)");
        });

        // Writer thread — sets the flag
        Thread writer = new Thread(() -> {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            stopFlag2 = true;
            System.out.println("  [Writer] Set stopFlag2=true (non-volatile)");
        });

        reader.start(); writer.start();
        reader.join(2000);   // wait max 2s
        writer.join();

        if (reader.isAlive()) {
            reader.interrupt();
            System.out.println("  [Main]   Reader still running after 2s — visibility bug confirmed!");
        }

        System.out.println("""
          
          WHY THIS HAPPENS:
          1. JIT sees 'while (!stopFlag2)' in a hot loop.
          2. JIT caches 'stopFlag2' in a CPU register (optimization — valid for single-thread view).
          3. Writer writes to main memory, but reader's register is never updated.
          4. Reader loops forever on the stale register value.
        """);
    }

    // =========================================================
    // Demo 2: Fixed with volatile
    // =========================================================
    static void demo2_staleRead_fixed_volatile() throws InterruptedException {
        separator("Demo 2: Fixed with volatile");

        // volatile PREVENTS the JIT from caching in a register.
        // Every read fetches from memory (or ensures cache coherence).
        final boolean[] running = {true};   // won't work — use volatile field
        // Use our volatile stopFlag1 declared at class level

        stopFlag1 = true;   // reset for this demo

        Thread reader = new Thread(() -> {
            long count = 0;
            while (stopFlag1) { count++; }   // stopFlag1 IS volatile
            System.out.println("  [Reader] Stopped after " + count + " iterations ✓");
        });

        Thread writer = new Thread(() -> {
            try { Thread.sleep(20); } catch (InterruptedException e) {}
            stopFlag1 = false;   // volatile write — immediately visible
            System.out.println("  [Writer] Set stopFlag1=false (volatile)");
        });

        reader.start(); writer.start();
        reader.join(2000);
        writer.join();
        System.out.println("  Reader terminated: " + !reader.isAlive());

        System.out.println("""
          
          WHY volatile FIXES IT:
          volatile write: flush store buffer to main memory + StoreLoad memory barrier.
          volatile read:  always load from main memory (bypass CPU cache).
          JIT cannot hoist volatile reads out of loops.
        """);
    }

    // =========================================================
    // Demo 3: Fixed with synchronized
    // =========================================================
    static void demo3_staleRead_fixed_synchronized() throws InterruptedException {
        separator("Demo 3: Fixed with synchronized");

        class SharedFlag {
            private boolean running = true;
            synchronized void stop()        { running = false; }
            synchronized boolean isRunning() { return running; }
        }

        SharedFlag flag = new SharedFlag();

        Thread reader = new Thread(() -> {
            long count = 0;
            while (flag.isRunning()) { count++; }
            System.out.println("  [Reader] Stopped via synchronized ✓ (" + count + " iters)");
        });

        Thread writer = new Thread(() -> {
            try { Thread.sleep(20); } catch (InterruptedException e) {}
            flag.stop();
            System.out.println("  [Writer] Called stop() via synchronized");
        });

        reader.start(); writer.start();
        reader.join(2000); writer.join();
        System.out.println("  Reader terminated: " + !reader.isAlive());

        System.out.println("""
          
          WHY synchronized FIXES IT:
          Monitor unlock (exiting synchronized) flushes all writes to main memory.
          Monitor lock (entering synchronized) invalidates the CPU cache, forcing reads from memory.
          This provides both visibility AND atomicity (unlike volatile which only provides visibility).
        """);
    }

    // =========================================================
    // Demo 4: volatile "Piggyback" — synchronizing related data
    // =========================================================
    /**
     * KEY INSIGHT: volatile's happens-before is NOT just for the volatile variable itself.
     *
     * "A write to a volatile variable happens-before every subsequent read of that variable."
     * + the HB relationship is transitive.
     *
     * So: "all writes BEFORE a volatile write are visible to all reads AFTER
     *      the subsequent volatile read."
     *
     * You can use ONE volatile flag to safely publish MULTIPLE non-volatile fields.
     */
    static void demo4_volatile_piggyback() throws InterruptedException {
        separator("Demo 4: volatile Piggyback — Publishing Multiple Fields");

        class Config {
            int    port     = 0;     // NOT volatile
            String host     = null;  // NOT volatile
            int    timeout  = 0;     // NOT volatile
            volatile boolean ready = false;   // ← only this is volatile
        }

        Config config = new Config();

        Thread writer = new Thread(() -> {
            // Write non-volatile fields FIRST
            config.port    = 8080;
            config.host    = "api.example.com";
            config.timeout = 30;
            // Then the volatile write — acts as a "publish" fence
            config.ready = true;   // ← volatile write: all above writes are NOW visible
            System.out.println("  [Writer] Config published via volatile ready=true");
        });

        Thread reader = new Thread(() -> {
            // Spin until ready
            while (!config.ready) {}   // volatile read — once true, all above writes visible
            // Happens-before: writer's non-volatile writes are guaranteed visible here
            System.out.printf("  [Reader] Config: host=%s port=%d timeout=%d%n",
                config.host, config.port, config.timeout);
        });

        writer.start(); reader.start();
        writer.join(); reader.join();

        System.out.println("""
          
          Happens-Before chain:
            config.port=8080    ─── program order HB ───►
            config.host=...     ─── program order HB ───►
            config.ready=true   ─── volatile write HB ──►
            while(!config.ready) ─ volatile read HB ────►
            use(config.host)
          
          All non-volatile writes before the volatile write are visible
          to all code after the corresponding volatile read. ✓
        """);
    }

    // =========================================================
    // Demo 5: volatile vs Atomic — Why volatile int i++ is broken
    // =========================================================
    static void demo5_volatile_vs_atomic() throws InterruptedException {
        separator("Demo 5: volatile vs Atomic — Compound Operations");

        System.out.println("  volatile DOES NOT guarantee atomicity for compound ops.");
        System.out.println("  i++ = READ i → ADD 1 → WRITE i  (3 separate operations)\n");

        // This class is BROKEN despite volatile
        class BrokenCounter {
            volatile int count = 0;
            void increment() { count++; }   // race condition — visible but not atomic!
        }

        // This is CORRECT
        class CorrectCounter {
            AtomicInteger count = new AtomicInteger(0);
            void increment() { count.incrementAndGet(); }   // CAS — atomic read-modify-write
        }

        BrokenCounter broken  = new BrokenCounter();
        CorrectCounter correct = new CorrectCounter();

        Runnable incBroken  = () -> { for (int i = 0; i < 10_000; i++) broken.increment();  };
        Runnable incCorrect = () -> { for (int i = 0; i < 10_000; i++) correct.increment(); };

        Thread t1 = new Thread(incBroken);  Thread t2 = new Thread(incBroken);
        Thread t3 = new Thread(incCorrect); Thread t4 = new Thread(incCorrect);

        t1.start(); t2.start(); t3.start(); t4.start();
        t1.join();  t2.join();  t3.join();  t4.join();

        System.out.println("  volatile int (broken):     expected 20000, got " + broken.count
            + (broken.count < 20000 ? " ← RACE CONDITION! ✗" : " (lucky)"));
        System.out.println("  AtomicInteger (correct):   expected 20000, got " + correct.count.get()
            + " ✓");

        System.out.println("""
          
          Use volatile when:
            - One thread writes, many threads read.
            - The write is a SINGLE assignment (not read-modify-write).
            - You need to publish a value atomically.
          
          Use AtomicInteger / AtomicReference when:
            - Multiple threads read AND write (check-then-act, increment, CAS).
            - You need compareAndSet() (lock-free algorithms).
        """);
    }

    // =========================================================
    // Demo 6: The Visibility Chain — HB Transitivity
    // =========================================================
    /**
     * Demonstrates that HB is transitive — you can build chains.
     *
     * Thread A → write x, write y, volatile write z
     * Thread B → volatile read z, read y, read x  (all guaranteed visible)
     * Thread C → start after B, reads everything B wrote (thread start HB)
     */
    static void demo6_visibility_chain() throws InterruptedException {
        separator("Demo 6: Happens-Before Chain (Transitivity)");

        class State {
            int a = 0, b = 0, c = 0;
            volatile boolean published = false;
        }

        State state = new State();
        int[] readValues = new int[3];

        Thread writer = new Thread(() -> {
            state.a = 100;            // write 1
            state.b = 200;            // write 2
            state.c = 300;            // write 3
            state.published = true;   // volatile write — HB fence
        });

        Thread reader = new Thread(() -> {
            while (!state.published) {}   // volatile read — once true...
            // ...all writes before volatile write are visible here
            readValues[0] = state.a;
            readValues[1] = state.b;
            readValues[2] = state.c;
        });

        writer.start();
        writer.join();    // main joins writer → writer's actions HB join returns
        reader.start();
        reader.join();    // main joins reader → reader's actions HB join returns

        System.out.println("  Writer wrote: a=100, b=200, c=300");
        System.out.printf("  Reader read:  a=%d, b=%d, c=%d%n",
            readValues[0], readValues[1], readValues[2]);

        boolean correct = readValues[0] == 100 && readValues[1] == 200 && readValues[2] == 300;
        System.out.println("  All values correct: " + correct + " ✓");

        System.out.println("""
          
          HB Chain:
            state.a=100 ─── prog order ──► state.published=true ─── volatile write ──►
            while(!published) ─── volatile read ──► state.a read
          
          Also: writer.join() establishes HB, so reader.start() after join
          guarantees reader sees all writer's writes (even without volatile, in this case).
        """);
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
