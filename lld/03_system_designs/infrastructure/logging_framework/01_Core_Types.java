// =============================================================================
// FILE: 01_Core_Types.java
// TOPIC: Logging Framework LLD — Core Types
// PATTERNS: Enum, Builder, Immutable Object
// =============================================================================

package logging_framework;

import java.time.Instant;

// =============================================================================
// ■ LogLevel — Ordered severity enum
//
// Interview point: ordinal() gives natural ordering (DEBUG=0 < FATAL=4).
// We can use level.ordinal() >= threshold.ordinal() for level comparison.
// =============================================================================

enum LogLevel {
    DEBUG,    // ordinal 0 — fine-grained diagnostic info
    INFO,     // ordinal 1 — general informational events
    WARN,     // ordinal 2 — potential problems that aren't errors yet
    ERROR,    // ordinal 3 — errors that allow continued execution
    FATAL;    // ordinal 4 — severe: app may abort

    public boolean isLoggableAt(LogLevel threshold) {
        return this.ordinal() >= threshold.ordinal();
    }
}


// =============================================================================
// ■ LogRecord — Immutable snapshot of one log event
//
// Interview point: Immutable = inherently thread-safe. We use the Builder
// pattern so that optional fields (throwable, context) don't bloat constructors.
//
// Pattern: Builder — avoids telescoping constructors, handles optional fields.
// =============================================================================

final class LogRecord {

    // All fields are final → immutable after construction
    private final Instant    timestamp;
    private final LogLevel   level;
    private final String     loggerName;
    private final String     message;
    private final String     threadName;
    private final Throwable  throwable;   // nullable — only present on exceptions

    // Private constructor — only Builder can call it
    private LogRecord(Builder builder) {
        this.timestamp  = builder.timestamp;
        this.level      = builder.level;
        this.loggerName = builder.loggerName;
        this.message    = builder.message;
        this.threadName = builder.threadName;
        this.throwable  = builder.throwable;
    }

    // ─── Getters (no setters — immutable) ────────────────────────────────
    public Instant   getTimestamp()  { return timestamp; }
    public LogLevel  getLevel()      { return level; }
    public String    getLoggerName() { return loggerName; }
    public String    getMessage()    { return message; }
    public String    getThreadName() { return threadName; }
    public Throwable getThrowable()  { return throwable; }  // may be null

    // ─── toString for debugging the record itself ─────────────────────────
    @Override
    public String toString() {
        return String.format("[%s] [%s] [%s] %s",
                timestamp, level, loggerName, message);
    }

    // =========================================================================
    // ■ Builder (static inner class)
    //
    // Usage:
    //   LogRecord record = new LogRecord.Builder(INFO, "com.App", "Server started")
    //                          .throwable(e)
    //                          .build();
    // =========================================================================
    static final class Builder {

        // Required fields
        private final LogLevel level;
        private final String   loggerName;
        private final String   message;

        // Auto-populated fields
        private final Instant timestamp  = Instant.now();
        private final String  threadName = Thread.currentThread().getName();

        // Optional field
        private Throwable throwable;

        // Required-field constructor
        Builder(LogLevel level, String loggerName, String message) {
            if (level == null)      throw new IllegalArgumentException("level must not be null");
            if (loggerName == null) throw new IllegalArgumentException("loggerName must not be null");
            if (message == null)    throw new IllegalArgumentException("message must not be null");
            this.level      = level;
            this.loggerName = loggerName;
            this.message    = message;
        }

        // Optional: attach an exception
        Builder throwable(Throwable t) {
            this.throwable = t;
            return this;
        }

        LogRecord build() {
            return new LogRecord(this);
        }
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why is LogRecord immutable?
// A: Once created, log records are passed across threads (e.g. to AsyncAppender).
//    Immutability eliminates race conditions without synchronization overhead.
//
// Q: Why Builder over a constructor with many parameters?
// A: Avoids "telescoping constructors" anti-pattern. Makes optional fields
//    (throwable) clean. Self-documents which fields are required vs. optional.
//
// Q: Could you use a record (Java 16+) instead?
// A: Yes — records are implicitly immutable and final. But Builder is more
//    flexible and works in all Java versions, which interviews often expect.
//
// Q: Why ordinal() for LogLevel comparison?
// A: Enums are defined in ascending severity order. ordinal() is stable within
//    a single JVM run and gives us O(1) level comparison without a Map.
//    (Caveat: ordinal is fragile if enum order changes — mention this trade-off.)
