// =============================================================================
// FILE: 04_Filters.java
// TOPIC: Logging Framework LLD — Filter Implementations
// PATTERNS: Chain of Responsibility, Composite, Strategy
// SOLID: OCP, ISP, SRP
// =============================================================================

package logging_framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

// =============================================================================
// ■ LevelFilter — Most common filter
//
// Allows records ONLY at or above a minimum level.
// Interview use case: A FileAppender logs only WARN+, Console logs all DEBUG+.
//
// Pattern: Strategy — plugged into Logger or Appender as a filter strategy.
// =============================================================================
class LevelFilter implements LogFilter {

    private final LogLevel minimumLevel;

    LevelFilter(LogLevel minimumLevel) {
        if (minimumLevel == null)
            throw new IllegalArgumentException("minimumLevel must not be null");
        this.minimumLevel = minimumLevel;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        // Pass only if record's level ordinal >= threshold ordinal
        return record.getLevel().isLoggableAt(minimumLevel);
    }

    @Override
    public String toString() {
        return "LevelFilter[min=" + minimumLevel + "]";
    }
}


// =============================================================================
// ■ RegexFilter — Message content-based filtering
//
// Use case: Suppress noisy heartbeat messages; filter PII patterns;
//           include only records mentioning a specific service name.
//
// Pattern: Strategy — another pluggable filter.
// =============================================================================
class RegexFilter implements LogFilter {

    public enum Mode { INCLUDE, EXCLUDE }

    private final Pattern pattern;
    private final Mode    mode;

    /**
     * @param regex regex to match against message
     * @param mode  INCLUDE = only pass matching records
     *              EXCLUDE = pass all records EXCEPT matching ones
     */
    RegexFilter(String regex, Mode mode) {
        if (regex == null || regex.isBlank())
            throw new IllegalArgumentException("regex must not be blank");
        this.pattern = Pattern.compile(regex);
        this.mode    = mode;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        boolean matches = pattern.matcher(record.getMessage()).find();
        return (mode == Mode.INCLUDE) ? matches : !matches;
    }

    @Override
    public String toString() {
        return "RegexFilter[mode=" + mode + ", pattern=" + pattern + "]";
    }
}


// =============================================================================
// ■ CompositeFilter — Composite Pattern
//
// Composes multiple LogFilter instances into a chain.
// Semantics: AND logic — ALL filters must pass for the record to be logged.
//
// Pattern: Composite — treats a group of filters as a single filter.
// Pattern: Chain of Responsibility — filters evaluated in order; first DENY wins.
//
// Interview point: This is how Logback's FilterReply chain works internally.
// =============================================================================
class CompositeFilter implements LogFilter {

    private final List<LogFilter> filters;

    CompositeFilter(LogFilter... filters) {
        this.filters = new ArrayList<>(Arrays.asList(filters));
    }

    /** Add a filter at runtime. */
    void addFilter(LogFilter filter) {
        if (filter != null) filters.add(filter);
    }

    /**
     * AND semantics: record passes only if ALL filters approve.
     * Short-circuits on first rejection (Chain of Responsibility).
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        for (LogFilter filter : filters) {
            if (!filter.isLoggable(record)) {
                return false;   // short-circuit: reject immediately
            }
        }
        return true;            // all filters passed
    }

    @Override
    public String toString() {
        return "CompositeFilter" + filters;
    }
}


// =============================================================================
// ■ ThresholdFilter — Exact level match (for completeness)
//
// Passes records that are EXACTLY at the specified level.
// Use case: Route all ERRORs to a dedicated error.log, nothing else.
// =============================================================================
class ThresholdFilter implements LogFilter {

    private final LogLevel targetLevel;

    ThresholdFilter(LogLevel targetLevel) {
        this.targetLevel = targetLevel;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return record.getLevel() == targetLevel;
    }

    @Override
    public String toString() {
        return "ThresholdFilter[level=" + targetLevel + "]";
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Explain Chain of Responsibility in the context of filters.
// A: Each filter in the chain independently decides pass/reject. The chain
//    processes them in order. The first filter to reject short-circuits the
//    chain (no further filters are evaluated). The record only reaches the
//    appender if every filter in the chain approves it.
//
// Q: What's the Composite pattern doing in CompositeFilter?
// A: CompositeFilter implements LogFilter — so to the Logger, it looks exactly
//    like a single LogFilter. But internally it holds many LogFilters and
//    delegates to all of them. Client code doesn't know if it's dealing with
//    one filter or twenty. That's the Composite pattern.
//
// Q: How would you add OR semantics instead of AND?
// A: class AnyFilter implements LogFilter {
//        // returns true if ANY child filter approves
//        for (LogFilter f : filters) if (f.isLoggable(record)) return true;
//        return false;
//    }
//    You'd compose AndFilter/OrFilter trees. This is how Logback's
//    TurboFilter chains work.
//
// Q: How does RegexFilter help in production?
// A: Suppressing heartbeat spam: RegexFilter("heartbeat", EXCLUDE)
//    Isolating a service's logs: RegexFilter("PaymentService", INCLUDE)
//    These are applied per-appender, so you can write filtered subsets to
//    dedicated log files without changing application code.
