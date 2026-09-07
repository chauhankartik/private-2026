// =============================================================================
// FILE: 07_LogManager.java
// TOPIC: Logging Framework LLD — LogManager (Registry + Factory)
// PATTERNS: Singleton (double-checked locking), Factory Method
// SOLID: SRP (manages logger registry), DIP (clients get Logger via interface)
// =============================================================================

package logging_framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// =============================================================================
// ■ LogManager — The global registry of named Loggers
//
// Responsibilities:
//   1. Singleton: one instance per JVM → one source of truth for all loggers.
//   2. Registry: maintains name → Logger map.
//   3. Factory: creates Loggers with sensible defaults if not yet registered.
//   4. Shutdown: closes all loggers and their appenders cleanly.
//
// Thread-safety:
//   - Double-checked locking for getInstance() (volatile + synchronized).
//   - ConcurrentHashMap for the logger registry: lock-free reads, fine-grained
//     write locking → high concurrency with no global lock.
// =============================================================================
final class LogManager {

    // ─── Singleton ────────────────────────────────────────────────────────────

    // volatile: ensures the partially-constructed object isn't visible to other threads
    private static volatile LogManager instance;

    private LogManager() {
        // Register JVM shutdown hook to cleanly close all loggers
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownAll, "logmanager-shutdown"));
    }

    /**
     * Double-checked locking Singleton.
     *
     * Why double-checked locking?
     *   - First check (no lock): fast path for the common case where instance exists.
     *   - synchronized block: guarantees only one thread initializes.
     *   - Second check (inside lock): another thread may have initialized between
     *     the first check and acquiring the lock.
     *   - volatile: prevents CPU / compiler reordering that could expose a
     *     partially-constructed instance to the first check.
     */
    public static LogManager getInstance() {
        if (instance == null) {                         // First check (no lock)
            synchronized (LogManager.class) {
                if (instance == null) {                 // Second check (inside lock)
                    instance = new LogManager();
                }
            }
        }
        return instance;
    }

    // ─── Registry ─────────────────────────────────────────────────────────────

    // ConcurrentHashMap: computeIfAbsent is atomic → no duplicate Logger creation
    private final Map<String, Logger> registry = new ConcurrentHashMap<>();

    /**
     * Get or create a Logger by name.
     * Convention: use fully-qualified class name (e.g., "com.myapp.UserService").
     *
     * Factory Method: hides Logger construction from client. Client only knows
     * the Logger interface, not that a Builder was used internally.
     *
     * computeIfAbsent is atomic in ConcurrentHashMap → only one Logger per name.
     */
    public Logger getLogger(String name) {
        return registry.computeIfAbsent(name, this::createDefaultLogger);
    }

    /**
     * Register a pre-built Logger (from Logger.Builder) into the registry.
     * Useful when callers want full control over appenders/filters.
     *
     * If a Logger with this name already exists, it is replaced.
     */
    public void registerLogger(Logger logger) {
        if (logger == null) throw new IllegalArgumentException("logger must not be null");
        Logger previous = registry.put(logger.getName(), logger);
        if (previous != null && previous != logger) {
            previous.shutdown();   // close appenders of replaced logger
        }
    }

    /**
     * Check if a Logger has already been registered.
     */
    public boolean hasLogger(String name) {
        return registry.containsKey(name);
    }

    /**
     * Retrieve a Logger only if it was previously registered. Returns null otherwise.
     * Use this when you want to avoid creating a default logger accidentally.
     */
    public Logger findLogger(String name) {
        return registry.get(name);
    }

    // ─── Shutdown ─────────────────────────────────────────────────────────────

    /**
     * Close all registered loggers and their appenders.
     * Called by the JVM shutdown hook. May also be called explicitly in tests.
     */
    public void shutdownAll() {
        registry.values().forEach(logger -> {
            try {
                logger.shutdown();
            } catch (Exception e) {
                System.err.println("[LogManager] Error shutting down logger: " + logger.getName());
            }
        });
        registry.clear();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Default logger: INFO level, simple formatter, console output.
     * Clients can override by calling registerLogger() with a custom one.
     */
    private Logger createDefaultLogger(String name) {
        LogFormatter formatter = new SimpleFormatter();
        LogAppender  appender  = new ConsoleAppender(formatter);
        return new Logger.Builder(name)
                .level(LogLevel.INFO)
                .appender(appender)
                .build();
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Explain double-checked locking. Why is 'volatile' needed?
// A: Without volatile, the CPU or compiler may reorder instructions inside the
//    synchronized block. Specifically, "instance = new LogManager()" is three steps:
//      1. Allocate memory
//      2. Call constructor (initialize fields)
//      3. Assign reference to 'instance'
//    Without volatile, steps 2 and 3 can be reordered. Another thread could
//    see a non-null 'instance' (step 3 done) but an uninitialized object
//    (step 2 not yet done) — this is the "partially-constructed object" bug.
//    volatile prevents this reordering, ensuring a fully-constructed object
//    is visible before any thread sees the reference.
//
// Q: Why ConcurrentHashMap over HashMap + synchronized?
// A: ConcurrentHashMap uses fine-grained segment locking (Java 8+: even finer
//    bin-level CAS). Multiple threads can read and write to different buckets
//    concurrently. HashMap + synchronized(this) serializes ALL operations —
//    a global bottleneck. computeIfAbsent() in ConcurrentHashMap is atomic,
//    guaranteeing exactly one Logger per name.
//
// Q: Why does getLogger() create a default Logger if name not found?
// A: Follows Log4j / SLF4J behavior: calling getLogger("com.App") always
//    succeeds. If the name hasn't been explicitly configured, a safe default
//    (INFO, Console) is returned. This avoids NPEs in client code and matches
//    the Principle of Least Surprise.
//
// Q: Is there a risk of two threads both calling getLogger("com.App")
//    simultaneously and creating two Loggers?
// A: No — ConcurrentHashMap.computeIfAbsent() is atomic. Only one thread
//    executes the factory function (createDefaultLogger). The other thread
//    waits and gets the same Logger instance.
//
// Q: How would you support a logger hierarchy (like Log4j's parent-child)?
// A: Parse the name by "." segments:
//    "com.myapp.UserService" → parent: "com.myapp" → parent: "com"
//    If a logger has no appenders, walk up to parent to inherit appenders.
//    Implement with a getParent(name) helper that strips the last segment.
//    This is exactly how Log4j's Logger hierarchy works.
//
// Q: How would you add hot-reload of logging configuration?
// A: 1. Watch the config file for changes (WatchService or polling).
//    2. On change, parse new config into LogLevel/Appender/Filter settings.
//    3. Call logger.setLevel(newLevel) and add/remove appenders atomically.
//    Since 'level' is volatile and appenders use CopyOnWriteArrayList,
//    changes propagate to all logging threads safely without restart.
