// =============================================================================
// FILE: 06_Logger.java
// TOPIC: Logging Framework LLD — The Logger class
// PATTERNS: Builder (Logger construction), Observer (notifies appenders)
// SOLID: SRP (Logger logs, nothing else), DIP (depends on interfaces)
// =============================================================================

package logging_framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// =============================================================================
// ■ Logger — Core class clients interact with
//
// Responsibilities:
//   1. Level-gate: reject records below the configured minimum level.
//   2. Filter-chain: pass the record through all registered filters.
//   3. Fan-out: send the surviving record to every registered appender.
//
// Thread-safety:
//   - appenders list uses CopyOnWriteArrayList → iteration is always safe,
//     even during concurrent add/remove, with no locking overhead for reads.
//   - Writes (add/remove appender) are O(n) copy but rare in practice.
//   - LogRecord is immutable → safe to pass across threads with no locking.
// =============================================================================
final class Logger {

    private final String name;                // unique logger name (e.g., class name)
    private volatile LogLevel level;          // volatile: visible across threads

    // CopyOnWriteArrayList: read-heavy, write-rare → perfect fit
    private final List<LogAppender> appenders;
    private final List<LogFilter>   filters;

    // Private: only Builder or LogManager creates loggers
    private Logger(Builder builder) {
        this.name      = builder.name;
        this.level     = builder.level;
        this.appenders = new CopyOnWriteArrayList<>(builder.appenders);
        this.filters   = new CopyOnWriteArrayList<>(builder.filters);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public String   getName()  { return name; }
    public LogLevel getLevel() { return level; }

    /** Dynamically change log level at runtime (e.g., from a config refresh). */
    public void setLevel(LogLevel level) {
        this.level = level;   // volatile write: immediately visible to all threads
    }

    // ─── Convenience methods (mirrors Log4j / SLF4J API) ─────────────────────

    public void debug(String message)              { log(LogLevel.DEBUG, message, null); }
    public void info (String message)              { log(LogLevel.INFO,  message, null); }
    public void warn (String message)              { log(LogLevel.WARN,  message, null); }
    public void error(String message)              { log(LogLevel.ERROR, message, null); }
    public void fatal(String message)              { log(LogLevel.FATAL, message, null); }

    public void error(String message, Throwable t) { log(LogLevel.ERROR, message, t); }
    public void fatal(String message, Throwable t) { log(LogLevel.FATAL, message, t); }

    // ─── Core log method ─────────────────────────────────────────────────────

    /**
     * Gate → Filter → Fan-out pipeline:
     *
     *   [1] Level gate:    Is record.level >= logger.level? If not, return fast.
     *   [2] Build record:  Create immutable LogRecord snapshot.
     *   [3] Filter chain:  All filters must approve (AND semantics).
     *   [4] Fan-out:       Deliver to every registered appender.
     */
    public void log(LogLevel recordLevel, String message, Throwable throwable) {

        // [1] Level gate — cheapest check first (avoid object creation)
        if (!recordLevel.isLoggableAt(this.level)) return;

        // [2] Build immutable record
        LogRecord.Builder builder = new LogRecord.Builder(recordLevel, name, message);
        if (throwable != null) builder.throwable(throwable);
        LogRecord record = builder.build();

        // [3] Filter chain — short-circuits on first rejection
        for (LogFilter filter : filters) {
            if (!filter.isLoggable(record)) return;
        }

        // [4] Fan-out to all appenders (Observer pattern)
        for (LogAppender appender : appenders) {
            try {
                appender.append(record);
            } catch (Exception e) {
                // Never let an appender failure crash the application.
                // Log to stderr as last resort.
                System.err.println("[Logger] Appender failure in " + name + ": " + e.getMessage());
            }
        }
    }

    // ─── Runtime management ──────────────────────────────────────────────────

    /** Add an appender after construction (e.g., in tests or from config reload). */
    public void addAppender(LogAppender appender) {
        if (appender != null) {
            appender.open();
            appenders.add(appender);
        }
    }

    /** Remove an appender and close its resources. */
    public void removeAppender(LogAppender appender) {
        if (appenders.remove(appender)) {
            appender.close();
        }
    }

    /** Add a filter to the chain at runtime. */
    public void addFilter(LogFilter filter) {
        if (filter != null) filters.add(filter);
    }

    /** Flush all appenders and release resources. Called on shutdown. */
    public void shutdown() {
        for (LogAppender appender : appenders) {
            try {
                appender.close();
            } catch (Exception e) {
                System.err.println("[Logger] Error closing appender: " + e.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return "Logger[name=" + name + ", level=" + level + ", appenders=" + appenders.size() + "]";
    }


    // =========================================================================
    // ■ Logger.Builder — Fluent construction
    //
    // Usage:
    //   Logger logger = new Logger.Builder("com.App")
    //       .level(LogLevel.DEBUG)
    //       .appender(consoleAppender)
    //       .appender(fileAppender)
    //       .filter(new LevelFilter(LogLevel.INFO))
    //       .build();
    // =========================================================================
    static final class Builder {

        private final String            name;
        private       LogLevel          level     = LogLevel.INFO;    // sensible default
        private final List<LogAppender> appenders = new ArrayList<>();
        private final List<LogFilter>   filters   = new ArrayList<>();

        Builder(String name) {
            if (name == null || name.isBlank())
                throw new IllegalArgumentException("Logger name must not be blank");
            this.name = name;
        }

        Builder level(LogLevel level) {
            this.level = level;
            return this;
        }

        Builder appender(LogAppender appender) {
            if (appender != null) {
                appender.open();               // open resource before registering
                this.appenders.add(appender);
            }
            return this;
        }

        Builder appenders(LogAppender... appenders) {
            Arrays.stream(appenders).forEach(this::appender);
            return this;
        }

        Builder filter(LogFilter filter) {
            if (filter != null) this.filters.add(filter);
            return this;
        }

        Logger build() {
            return new Logger(this);
        }
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why CopyOnWriteArrayList for appenders?
// A: The read path (iterate appenders during log()) must be fast and lock-free
//    in a concurrent system. CopyOnWriteArrayList snapshots the array on write
//    operations, so readers iterate over a stable copy without synchronization.
//    Writes (addAppender/removeAppender) are infrequent, so the O(n) copy is
//    acceptable. Alternative: ReadWriteLock, which allows concurrent reads.
//
// Q: Why is 'level' declared volatile?
// A: setLevel() is called from one thread (e.g., config reload thread) while
//    log() is called from many threads. Without volatile, the write to 'level'
//    may not be immediately visible to other CPU caches. volatile guarantees
//    visibility across threads with no synchronization overhead.
//
// Q: Why catch Exception in the fan-out loop?
// A: An appender failure (e.g., disk full, DB down) must NEVER crash the calling
//    application thread. The logger should be transparent to the app. This is
//    a critical production requirement. Log the failure to stderr as fallback.
//
// Q: Why call appender.open() in the Builder?
// A: Ensure resources are initialized before any append() call. The Builder is
//    the construction phase; open() is the lifecycle method that bridges
//    construction and operation. Matches the "initialize before use" contract.
//
// Q: What's the performance of the level gate check?
// A: O(1) — two ordinal() lookups and an integer comparison. This is the most
//    common path (most records are filtered out). SLF4J does the same: all
//    convenience methods (debug/info/warn) check level first, before building
//    any string (avoids String concatenation overhead for filtered records).
