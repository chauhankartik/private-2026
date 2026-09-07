// =============================================================================
// FILE: 05_Appenders.java
// TOPIC: Logging Framework LLD — Appender Implementations
// PATTERNS: Strategy (concrete), Decorator (AsyncAppender), Observer (sinks)
// SOLID: OCP, SRP, DIP
// =============================================================================

package logging_framework;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// =============================================================================
// ■ ConsoleAppender — writes to stdout
//
// Thread-safety: System.out.println is synchronized internally in Java.
// For high-throughput, prefer PrintStream with synchronized block or use async.
// =============================================================================
class ConsoleAppender implements LogAppender {

    private final LogFormatter formatter;
    private final LogFilter    filter;    // nullable — if null, accept all records

    /**
     * @param formatter formats LogRecord → String before printing
     * @param filter    optional filter; null = no filtering (accept all)
     */
    ConsoleAppender(LogFormatter formatter, LogFilter filter) {
        if (formatter == null) throw new IllegalArgumentException("formatter required");
        this.formatter = formatter;
        this.filter    = filter;
    }

    ConsoleAppender(LogFormatter formatter) {
        this(formatter, null);  // convenience: no filter
    }

    @Override
    public void open() {
        // No resources to open for stdout
    }

    @Override
    public void append(LogRecord record) {
        // Apply appender-level filter (can differ from logger-level filter)
        if (filter != null && !filter.isLoggable(record)) return;
        System.out.println(formatter.format(record));
    }

    @Override
    public void close() {
        System.out.flush();  // flush stdout on shutdown
    }
}


// =============================================================================
// ■ FileAppender — writes to a log file
//
// Thread-safety: synchronized on 'this' for append() and close().
//
// Production enhancements (mention in interview):
//   - RolloverStrategy: rotate by size or date (daily, hourly)
//   - RetentionPolicy: delete files older than N days
//   - BufferedWriter for throughput vs FileWriter for latency
// =============================================================================
class FileAppender implements LogAppender {

    private final String       filePath;
    private final LogFormatter formatter;
    private final LogFilter    filter;    // nullable
    private final boolean      append;   // true = append to existing file

    private PrintWriter writer;           // guarded by synchronized(this)

    FileAppender(String filePath, LogFormatter formatter, LogFilter filter, boolean append) {
        if (filePath == null || filePath.isBlank())
            throw new IllegalArgumentException("filePath required");
        if (formatter == null)
            throw new IllegalArgumentException("formatter required");
        this.filePath  = filePath;
        this.formatter = formatter;
        this.filter    = filter;
        this.append    = append;
    }

    FileAppender(String filePath, LogFormatter formatter) {
        this(filePath, formatter, null, true);   // default: append, no filter
    }

    @Override
    public synchronized void open() {
        try {
            // auto-flush = false for performance; we flush on close / periodically
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, append)));
        } catch (IOException e) {
            throw new RuntimeException("Cannot open log file: " + filePath, e);
        }
    }

    @Override
    public synchronized void append(LogRecord record) {
        if (writer == null)
            throw new IllegalStateException("FileAppender not opened. Call open() first.");
        if (filter != null && !filter.isLoggable(record)) return;

        writer.println(formatter.format(record));
        // Note: not calling flush() here for performance.
        // Production: use a periodic flush or auto-flush=true for critical logs.
    }

    @Override
    public synchronized void close() {
        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
        }
    }
}


// =============================================================================
// ■ AsyncAppender — Decorator Pattern wrapping any LogAppender
//
// ┌─────────────┐         ┌──────────────────────────────────┐
// │   Logger    │──────▶  │        AsyncAppender             │
// │             │         │  (Decorator wrapping a delegate) │
// └─────────────┘         └──────────┬───────────────────────┘
//                                    │ delegates to
//                                    ▼
//                          ┌──────────────────┐
//                          │  FileAppender or  │
//                          │  ConsoleAppender  │
//                          └──────────────────┘
//
// How it works:
//   1. Logger calls async.append(record) — non-blocking, record queued.
//   2. Background daemon thread drains the queue and calls delegate.append().
//   3. Logger thread is NEVER blocked waiting for I/O.
//
// Trade-offs to discuss:
//   PRO:  Zero I/O blocking on calling threads → lower latency
//   CON:  Possible record loss if JVM crashes before queue is drained
//   CON:  Memory overhead of the queue
//   Fix:  Use shutdown hook to drain queue before JVM exits.
// =============================================================================
class AsyncAppender implements LogAppender {

    private static final int DEFAULT_QUEUE_CAPACITY = 1024;

    private final LogAppender          delegate;        // the wrapped appender
    private final BlockingQueue<LogRecord> queue;
    private final AtomicBoolean        running = new AtomicBoolean(false);
    private       Thread               workerThread;

    AsyncAppender(LogAppender delegate) {
        this(delegate, DEFAULT_QUEUE_CAPACITY);
    }

    AsyncAppender(LogAppender delegate, int queueCapacity) {
        if (delegate == null) throw new IllegalArgumentException("delegate appender required");
        this.delegate = delegate;
        this.queue    = new LinkedBlockingQueue<>(queueCapacity);
    }

    @Override
    public void open() {
        delegate.open();                    // initialize underlying resource
        running.set(true);

        // Daemon thread: JVM won't wait for it on normal exit
        // (Register shutdown hook to drain queue on graceful shutdown)
        workerThread = new Thread(this::drainLoop, "async-appender-worker");
        workerThread.setDaemon(true);
        workerThread.start();

        // Register JVM shutdown hook to drain remaining records gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "async-appender-shutdown"));
    }

    /**
     * Non-blocking enqueue. If queue is full, record is dropped (fast path).
     * Production alternative: block with offer(record, timeout) or use caller-runs.
     */
    @Override
    public void append(LogRecord record) {
        if (!running.get()) return;        // silently drop if stopped

        boolean enqueued = queue.offer(record);  // non-blocking
        if (!enqueued) {
            // Queue full — log loss. In production: increment a lost-message counter,
            // or block with timeout: queue.offer(record, 100, TimeUnit.MILLISECONDS)
            System.err.println("[AsyncAppender] WARN: queue full, record dropped: "
                    + record.getMessage());
        }
    }

    @Override
    public void close() {
        running.set(false);

        // Drain remaining records (graceful shutdown)
        LogRecord record;
        while ((record = queue.poll()) != null) {
            try {
                delegate.append(record);
            } catch (Exception ignored) { /* best-effort drain */ }
        }

        // Interrupt worker if it's blocked on poll
        if (workerThread != null) {
            workerThread.interrupt();
        }

        delegate.close();   // close underlying resource
    }

    // ─── Background drain loop ────────────────────────────────────────────────

    private void drainLoop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                // Block for up to 500ms waiting for a record
                LogRecord record = queue.poll(500, TimeUnit.MILLISECONDS);
                if (record != null) {
                    delegate.append(record);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // restore interrupt flag
                break;
            }
        }
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: How does AsyncAppender demonstrate the Decorator pattern?
// A: AsyncAppender wraps any LogAppender (the delegate) and adds async behavior
//    to it without changing the delegate's code. Logger sees the same
//    LogAppender interface. The queue + background thread is the "decoration".
//    You can wrap ConsoleAppender, FileAppender, or even DatabaseAppender.
//
// Q: What happens if the JVM crashes in async mode?
// A: Records still in the queue are lost. Mitigations:
//    1. Shutdown hook (shown above) drains the queue on graceful shutdown.
//    2. Use a persistent queue (e.g., Chronicle Queue) for guaranteed delivery.
//    3. Reduce queue size to reduce potential data loss window.
//
// Q: How do you prevent queue overflow in high-throughput systems?
// A: Options in order of preference:
//    1. Drop newest (shown): offer() returns false → drop silently + count.
//    2. Drop oldest: queue.poll(); queue.offer(record) (lossy but bounded).
//    3. Block caller: queue.put(record) — backpressure, may block app threads.
//    4. Caller-runs: delegate.append(record) directly if queue is full (mixed mode).
//
// Q: Why is the worker thread a daemon thread?
// A: Daemon threads don't prevent JVM shutdown. If they were non-daemon,
//    the JVM would hang waiting for the worker to stop. The shutdown hook
//    provides the graceful drain before JVM exits.
//
// Q: Why synchronized on FileAppender.append() but not ConsoleAppender?
// A: System.out is a synchronized PrintStream internally. FileAppender's
//    PrintWriter is not thread-safe, so we must guard it ourselves.
