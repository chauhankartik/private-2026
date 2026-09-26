// =============================================================================
// FILE: 02_Interfaces.java
// TOPIC: Logging Framework LLD — Core Abstractions (Interfaces)
// PATTERNS: Strategy, ISP (Interface Segregation Principle)
// =============================================================================

package logging_framework;

// =============================================================================
// ■ LogFormatter — Strategy Interface for formatting
//
// SOLID: ISP — small, single-method interface
// SOLID: OCP — new formats = new class, no changes here
// Pattern: Strategy — Logger holds a formatter reference, swappable at runtime
// =============================================================================
interface LogFormatter {

    /**
     * Convert a LogRecord to a human-readable (or structured) String.
     *
     * @param record the log event to format
     * @return formatted string representation
     */
    String format(LogRecord record);
}


// =============================================================================
// ■ LogFilter — Strategy / Chain of Responsibility interface
//
// SOLID: ISP — one decision method, nothing else
// Pattern: Chain of Responsibility — filters are evaluated in sequence;
//          a record is only logged if ALL filters pass it.
// =============================================================================
interface LogFilter {

    /**
     * Decide whether this log record should proceed to an appender.
     *
     * @param record the log event to evaluate
     * @return true  = allow (keep processing)
     *         false = deny  (drop this record)
     */
    boolean isLoggable(LogRecord record);
}


// =============================================================================
// ■ LogAppender — Observer / Sink interface
//
// SOLID: ISP   — single responsibility: write a record somewhere
// SOLID: DIP   — Logger depends on this interface, not on concrete Console/File
// SOLID: OCP   — new sink types added via new class implementing this interface
// Pattern: Observer — Logger (subject) notifies all appenders (observers)
//
// Lifecycle: open() must be called before append(), close() on shutdown.
// =============================================================================
interface LogAppender {

    /**
     * Write (or enqueue) the formatted log record to the target sink.
     *
     * @param record the log event to persist/display
     */
    void append(LogRecord record);

    /**
     * Open / initialize underlying resources (file handles, DB connections).
     * Called once before the first append().
     */
    void open();

    /**
     * Release all underlying resources cleanly.
     * Should flush any buffered records before closing.
     */
    void close();
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why three separate interfaces instead of one big "Handler" class?
// A: Interface Segregation Principle. Formatters don't need to know about
//    sinks. Filters don't need to know about formatters. Keeps each interface
//    focused on one concern. Clients depend only on what they actually use.
//
// Q: Why does LogAppender have open() and close() lifecycle methods?
// A: File handles, DB connections, and network sockets are resources that must
//    be explicitly opened and released. open() is the "initialize" hook;
//    close() ensures graceful shutdown (flush buffers, no data loss).
//    Implementing AutoCloseable would be the Java idiomatic approach.
//
// Q: Why is LogFormatter a Strategy?
// A: The Logger delegates the "how to format" decision to the formatter object.
//    You can swap formatters at runtime (e.g., plain text in dev, JSON in prod)
//    without changing the Logger's core logic. That's the Strategy pattern.
//
// Q: Could LogFilter be a java.util.function.Predicate<LogRecord>?
// A: Yes — and that's a great production-level callout. A named interface
//    is more readable and self-documenting. In practice, SLF4J / Logback use
//    named interfaces for the same reason.
