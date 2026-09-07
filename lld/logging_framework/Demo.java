// =============================================================================
// FILE: 08_Demo.java
// TOPIC: Logging Framework LLD — Complete Demo (Interview Walkthrough)
//
// This file wires all components together and demonstrates:
//   1. Basic usage (getLogger + convenience methods)
//   2. Custom Logger with File + Console dual appenders
//   3. Filter chain (level + regex)
//   4. Async logging
//   5. JSON structured logging
//   6. Exception logging
//   7. Runtime level change
//   8. LogManager lifecycle
//
// RUN: javac -d out logging_framework/*.java && java -cp out logging_framework.Demo
// =============================================================================

package logging_framework;

public class Demo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=".repeat(70));
        System.out.println("  LOGGING FRAMEWORK LLD — Complete Demo");
        System.out.println("=".repeat(70));

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 1: Default Logger via LogManager (simplest usage)
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 1: Default Logger (INFO level, Console)");
        System.out.println("-".repeat(50));

        LogManager manager = LogManager.getInstance();

        Logger appLogger = manager.getLogger("com.myapp.Application");
        appLogger.info("Application started successfully");
        appLogger.debug("This DEBUG will be SUPPRESSED (logger level = INFO)");
        appLogger.warn("Low disk space detected");

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 2: Custom Logger — dual appenders (Console + File)
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 2: Dual Appenders — Console + File");
        System.out.println("-".repeat(50));

        LogFormatter simpleFormatter  = new SimpleFormatter();
        LogFormatter patternFormatter = new PatternFormatter("[%d] [%l] [%n] → %m");

        LogAppender consoleAppender = new ConsoleAppender(simpleFormatter);
        LogAppender fileAppender    = new FileAppender("app.log", patternFormatter,
                                                       new LevelFilter(LogLevel.WARN),
                                                       true);

        Logger serviceLogger = new Logger.Builder("com.myapp.UserService")
                .level(LogLevel.DEBUG)
                .appender(consoleAppender)
                .appender(fileAppender)   // File only gets WARN+ records (has LevelFilter)
                .build();

        manager.registerLogger(serviceLogger);

        serviceLogger.debug("Fetching user from cache");         // Console only
        serviceLogger.info("User fetched: userId=42");            // Console only
        serviceLogger.warn("Cache miss — falling back to DB");    // Console + File
        serviceLogger.error("DB connection timeout for userId=42"); // Console + File

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 3: Filter chain — Level + Regex combined
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 3: CompositeFilter (LevelFilter AND RegexFilter)");
        System.out.println("-".repeat(50));

        // Only log WARN+ messages that don't mention "heartbeat" (noise suppression)
        LogFilter compositeFilter = new CompositeFilter(
                new LevelFilter(LogLevel.WARN),
                new RegexFilter("heartbeat", RegexFilter.Mode.EXCLUDE)
        );

        LogAppender filteredConsole = new ConsoleAppender(simpleFormatter, compositeFilter);

        Logger monitorLogger = new Logger.Builder("com.myapp.HealthMonitor")
                .level(LogLevel.DEBUG)   // Logger allows all; appender filters
                .appender(filteredConsole)
                .build();

        monitorLogger.info("heartbeat OK");            // Suppressed: level < WARN
        monitorLogger.warn("heartbeat OK");            // Suppressed by regex (EXCLUDE)
        monitorLogger.warn("High CPU: 92% usage");     // ✓ Passes both filters → logged
        monitorLogger.error("Service UNREACHABLE");    // ✓ Passes both filters → logged

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 4: Exception logging
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 4: Exception (Throwable) Logging");
        System.out.println("-".repeat(50));

        Logger exLogger = new Logger.Builder("com.myapp.PaymentService")
                .level(LogLevel.ERROR)
                .appender(new ConsoleAppender(simpleFormatter))
                .build();

        try {
            processPayment(-100);
        } catch (IllegalArgumentException e) {
            exLogger.error("Payment processing failed", e);
        }

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 5: JSON Structured Logging
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 5: JSON Formatter (Structured Logging)");
        System.out.println("-".repeat(50));

        LogFormatter jsonFormatter = new JsonFormatter();
        Logger jsonLogger = new Logger.Builder("com.myapp.OrderService")
                .level(LogLevel.INFO)
                .appender(new ConsoleAppender(jsonFormatter))
                .build();

        jsonLogger.info("Order placed: orderId=ORD-9901");
        jsonLogger.warn("Order delayed: SLA breach imminent");

        try {
            throw new RuntimeException("Inventory service unreachable");
        } catch (RuntimeException e) {
            jsonLogger.error("Order fulfillment failed", e);
        }

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 6: Async Logger
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 6: Async Logging (non-blocking)");
        System.out.println("-".repeat(50));

        // Wrap FileAppender in AsyncAppender — caller threads are never blocked
        LogAppender asyncFileAppender = new AsyncAppender(
                new FileAppender("async.log", simpleFormatter));

        Logger asyncLogger = new Logger.Builder("com.myapp.EventService")
                .level(LogLevel.DEBUG)
                .appender(asyncFileAppender)   // async! caller returns immediately
                .build();

        System.out.println("Sending 5 records asynchronously...");
        for (int i = 1; i <= 5; i++) {
            asyncLogger.info("Async event #" + i + " enqueued");
        }
        System.out.println("All 5 enqueued (caller never blocked). Check async.log.");

        // Give the background thread time to drain the queue
        Thread.sleep(200);

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 7: Runtime level change
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 7: Runtime Level Change");
        System.out.println("-".repeat(50));

        Logger configLogger = manager.getLogger("com.myapp.ConfigService");
        configLogger.debug("DEBUG before level change — suppressed");
        configLogger.info("INFO before level change — visible");

        System.out.println("[Config reload: switching to DEBUG level]");
        configLogger.setLevel(LogLevel.DEBUG);   // volatile write — instant visibility

        configLogger.debug("DEBUG after level change — NOW VISIBLE");
        configLogger.info("INFO after level change — still visible");

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 8: ThresholdFilter — route only ERROR to dedicated sink
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 8: ThresholdFilter — errors-only sink");
        System.out.println("-".repeat(50));

        LogFilter errorOnly = new ThresholdFilter(LogLevel.ERROR);
        LogAppender errorConsole = new ConsoleAppender(
                new PatternFormatter("🚨 [%l] %m"), errorOnly);

        Logger auditLogger = new Logger.Builder("com.myapp.AuditService")
                .level(LogLevel.DEBUG)
                .appender(new ConsoleAppender(simpleFormatter))   // all levels
                .appender(errorConsole)                            // only ERROR
                .build();

        auditLogger.info("Audit: user login event");          // first appender only
        auditLogger.warn("Audit: suspicious request rate");   // first appender only
        auditLogger.error("Audit: unauthorized admin access");// BOTH appenders → double output

        // ─────────────────────────────────────────────────────────────────────
        // SHUTDOWN
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SHUTDOWN — closing all loggers and appenders");
        System.out.println("-".repeat(50));
        manager.shutdownAll();
        System.out.println("All appenders closed. Shutdown complete.");
    }

    // ─── Helper to simulate a business exception ─────────────────────────────
    private static void processPayment(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount must be non-negative, got: " + amount);
        }
    }
}


// =============================================================================
// ■ EXPECTED OUTPUT SUMMARY (when run)
// =============================================================================
//
// SCENARIO 1: Default Logger
//   [timestamp] [INFO] [com.myapp.Application] - Application started successfully
//   [timestamp] [WARN] [com.myapp.Application] - Low disk space detected
//   (DEBUG suppressed — logger level = INFO)
//
// SCENARIO 2: Dual Appenders
//   Debug + Info → Console only
//   Warn + Error → Console AND file (app.log)
//
// SCENARIO 3: CompositeFilter
//   Only "High CPU" and "Service UNREACHABLE" appear (heartbeat filtered; INFO < WARN)
//
// SCENARIO 4: Exception
//   Full stack trace appended to ERROR message
//
// SCENARIO 5: JSON
//   Pretty-printed JSON objects for each log event
//
// SCENARIO 6: Async
//   Caller returns immediately after offer(); background thread writes to async.log
//
// SCENARIO 7: Runtime level change
//   DEBUG suppressed before setLevel(DEBUG); visible after
//
// SCENARIO 8: ThresholdFilter
//   Error line appears twice (once from each appender — one filtered, one not)
