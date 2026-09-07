// =============================================================================
// FILE: 03_Formatters.java
// TOPIC: Logging Framework LLD — Formatter Implementations
// PATTERNS: Strategy (concrete strategies)
// SOLID: OCP — new format = new class, zero changes to Logger or Appender
// =============================================================================

package logging_framework;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// =============================================================================
// ■ SimpleFormatter
//
// Output: [2026-06-20 09:15:03] [INFO] [com.App] - Server started
//
// Use case: Console output during development.
// =============================================================================
class SimpleFormatter implements LogFormatter {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                             .withZone(ZoneId.systemDefault());

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(DATE_FMT.format(record.getTimestamp())).append(']');
        sb.append(" [").append(record.getLevel()).append(']');
        sb.append(" [").append(record.getLoggerName()).append(']');
        sb.append(" [").append(record.getThreadName()).append(']');
        sb.append(" - ").append(record.getMessage());

        // Append exception stack trace if present
        if (record.getThrowable() != null) {
            sb.append('\n').append(formatThrowable(record.getThrowable()));
        }
        return sb.toString();
    }

    private String formatThrowable(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage());
        for (StackTraceElement element : t.getStackTrace()) {
            sb.append("\n\tat ").append(element);
        }
        return sb.toString();
    }
}


// =============================================================================
// ■ PatternFormatter
//
// Allows a configurable pattern string, similar to Log4j's PatternLayout.
//
// Supported tokens:
//   %d  → timestamp (ISO instant)
//   %l  → log level
//   %n  → logger name
//   %t  → thread name
//   %m  → message
//   %e  → exception (if present)
//   %n  → newline (but we use \n in the replacement instead)
//
// Example pattern: "[%d] [%l] [%n] %m"
//
// Use case: Let operators configure the format via a config file without
//           recompiling. Open for extension via pattern tokens.
// =============================================================================
class PatternFormatter implements LogFormatter {

    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());

    private final String pattern;

    PatternFormatter(String pattern) {
        if (pattern == null || pattern.isBlank())
            throw new IllegalArgumentException("pattern must not be blank");
        this.pattern = pattern;
    }

    @Override
    public String format(LogRecord record) {
        String result = pattern
                .replace("%d", ISO_FMT.format(record.getTimestamp()))
                .replace("%l", record.getLevel().name())
                .replace("%n", record.getLoggerName())
                .replace("%t", record.getThreadName())
                .replace("%m", record.getMessage());

        if (record.getThrowable() != null) {
            result = result.replace("%e", record.getThrowable().toString());
        } else {
            result = result.replace("%e", "");
        }
        return result;
    }
}


// =============================================================================
// ■ JsonFormatter
//
// Output (structured logging):
// {
//   "timestamp": "2026-06-20T09:15:03Z",
//   "level": "ERROR",
//   "logger": "com.App",
//   "thread": "main",
//   "message": "Null pointer",
//   "exception": "java.lang.NullPointerException: ..."
// }
//
// Use case: Machine-parseable logs for ELK / Splunk / Cloud Logging.
// Interview point: Adding a new format = new class. Zero modification to Logger.
//                 This demonstrates OCP perfectly.
// =============================================================================
class JsonFormatter implements LogFormatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder("{\n");
        appendField(sb, "timestamp", record.getTimestamp().toString(), false);
        appendField(sb, "level",     record.getLevel().name(), false);
        appendField(sb, "logger",    record.getLoggerName(), false);
        appendField(sb, "thread",    record.getThreadName(), false);
        boolean hasException = record.getThrowable() != null;
        appendField(sb, "message",   escape(record.getMessage()), hasException);
        if (hasException) {
            appendField(sb, "exception", escape(record.getThrowable().toString()), false);
        }
        sb.append("}");
        return sb.toString();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private void appendField(StringBuilder sb, String key, String value, boolean moreFields) {
        sb.append("  \"").append(key).append("\": \"").append(value).append("\"");
        if (moreFields) sb.append(',');
        sb.append('\n');
    }

    /** Escape characters that would break JSON string syntax. */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: How is OCP demonstrated here?
// A: Logger depends on LogFormatter interface. When we added JsonFormatter,
//    we created a new class — zero modifications to Logger, SimpleFormatter,
//    or PatternFormatter. New behavior via new code only.
//
// Q: How would you add a CSV formatter?
// A: class CsvFormatter implements LogFormatter { ... }
//    Plug it into a FileAppender. Logger doesn't change.
//
// Q: What if the pattern string has an invalid token like "%x"?
// A: PatternFormatter silently leaves it (replace misses, leaves "%x" as-is).
//    Production solution: validate tokens at construction time and throw
//    IllegalArgumentException. Or replace unknown tokens with empty string.
//
// Q: Is JsonFormatter production-grade?
// A: No — it's hand-rolled. Production code would use Jackson or Gson to
//    properly handle all JSON edge cases (unicode escapes, nested objects).
//    For an interview, hand-rolled is fine and shows you understand the pattern.
