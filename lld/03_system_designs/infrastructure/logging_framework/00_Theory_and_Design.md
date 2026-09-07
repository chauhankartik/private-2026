# Logging Framework — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** Singleton, Builder, Strategy, Chain of Responsibility, Observer  
> **SOLID Coverage:** All 5 principles applied  

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design a Logging Framework like Log4j / SLF4J. It should support:
> - Multiple log levels (DEBUG, INFO, WARN, ERROR, FATAL)
> - Multiple appenders / sinks (Console, File, Database)
> - Configurable log format / layout
> - Thread-safe and extensible
> - Filtering support
> - Async logging (bonus)"

---

## 2. Clarifying Questions (Ask These First!)

Before writing a single line of code, ask:

| Question | Why It Matters |
|---|---|
| Should it be a **singleton** per application? | Thread-safety scope, global vs. scoped |
| Should **multiple loggers** exist (per class/module)? | Logger hierarchy design |
| Do we need **async** logging? | Queue + background thread design |
| What **output formats** are needed? | Layout / Formatter abstraction |
| Should it support **runtime configuration**? | Config parser, hot-reload |
| Should log levels be **filterable per sink**? | Filter chain design |
| Do we need **log rotation** for files? | File appender complexity |

---

## 3. Core Entities Identification

```
Logger              → entry point for the client
LogLevel            → enum: DEBUG < INFO < WARN < ERROR < FATAL
LogRecord           → immutable data object (message + metadata)
LogFormatter        → formats LogRecord → String
LogFilter           → decides if a record should be logged
LogAppender         → writes formatted log to a sink
LoggerConfig        → configuration holder
LogManager          → factory / registry of loggers (Singleton)
```

---

## 4. Class Diagram (UML)

```
┌─────────────────────────────────────────────────────────────────┐
│                          LogManager (Singleton)                   │
│  - Map<String, Logger> loggerRegistry                            │
│  + getLogger(name) : Logger                                      │
└─────────────────────────────────────────────────────────────────┘
                              │ creates / caches
                              ▼
┌───────────────────────────────────────────────────────────────┐
│                            Logger                               │
│  - name : String                                               │
│  - level : LogLevel                                            │
│  - appenders : List<LogAppender>                              │
│  - filters  : List<LogFilter>                                  │
│  + log(level, message)                                         │
│  + debug/info/warn/error/fatal(message)                        │
└───────────────────────────────────────────────────────────────┘
         │ delegates to                   │ delegates to
         ▼                               ▼
┌──────────────────┐          ┌───────────────────────┐
│   LogAppender    │◄─────────│    LogFormatter        │
│  <<interface>>   │ uses     │   <<interface>>         │
│  + append(record)│          │  + format(record):String│
└──────────────────┘          └───────────────────────┘
    ▲         ▲                    ▲          ▲
    │         │                   │          │
ConsoleAppender FileAppender  SimpleFormatter PatternFormatter
DatabaseAppender AsyncAppender

┌───────────────────────────────┐
│        LogFilter <<interface>>│
│  + isLoggable(record): boolean│
└───────────────────────────────┘
         ▲            ▲
  LevelFilter    RegexFilter

┌────────────────────────────────┐
│      LogRecord (immutable)     │
│  - timestamp                   │
│  - level                       │
│  - loggerName                  │
│  - message                     │
│  - threadName                  │
│  - throwable (optional)        │
└────────────────────────────────┘
```

---

## 5. Design Patterns Applied

| Pattern | Where Used | Why |
|---|---|---|
| **Singleton** | `LogManager` | One global registry of loggers |
| **Builder** | `Logger`, `LogRecord` | Flexible construction, immutability |
| **Strategy** | `LogFormatter`, `LogFilter` | Swap formatting/filtering at runtime |
| **Chain of Responsibility** | `LogFilter` chain | Each filter decides pass/reject |
| **Observer** | `LogAppender` list | Logger notifies all appenders |
| **Factory Method** | `LogManager.getLogger()` | Hides Logger creation detail |
| **Decorator** | `AsyncAppender` wraps appender | Adds async behavior non-invasively |
| **Composite** | Filter chain | Multiple filters composed together |

---

## 6. SOLID Principles Applied

| Principle | Application |
|---|---|
| **SRP** | `Logger` logs, `LogFormatter` formats, `LogAppender` writes — no overlap |
| **OCP** | New appenders/formatters added via new classes, zero existing code change |
| **LSP** | All `LogAppender` impls are substitutable without breaking `Logger` |
| **ISP** | `LogFilter`, `LogFormatter`, `LogAppender` are small, focused interfaces |
| **DIP** | `Logger` depends on `LogAppender` interface, not `ConsoleAppender` directly |

---

## 7. Thread Safety Strategy

```
Logger.log()            → synchronized on appender list (or CopyOnWriteArrayList)
LogRecord               → immutable (inherently thread-safe)
LogManager              → double-checked locking singleton
FileAppender.append()   → synchronized on PrintWriter/BufferedWriter
AsyncAppender           → uses LinkedBlockingQueue + daemon thread
```

---

## 8. Log Level Hierarchy (Interview Trick)

```
DEBUG (0) < INFO (1) < WARN (2) < ERROR (3) < FATAL (4)

Rule: Logger at level X will only log records where record.level >= X
```

---

## 9. Interview Trade-off Discussion Script

> **"Why not just use `System.out.println`?"**  
> No level filtering, no routing to multiple sinks, no formatting, not thread-safe for concurrent apps.

> **"Why Singleton for LogManager?"**  
> All parts of the app need access to the same logger registry. Prevents duplicate loggers for the same name. Use double-checked locking for thread safety.

> **"How would you make it async?"**  
> Wrap any `LogAppender` with `AsyncAppender` (Decorator pattern): push records to a `BlockingQueue`, drain via a dedicated daemon thread. Caller is never blocked.

> **"How would you support log rotation?"**  
> Extend `FileAppender` with size/time-based rollover strategy — new file when threshold hit. Follow SRP: rotation logic is a separate `RolloverStrategy` interface.

> **"How would you add structured logging (JSON)?"**  
> Implement `JsonFormatter implements LogFormatter`. Zero change to Logger or Appender.

---

## 10. File Structure

```
logging_framework/
├── 00_Theory_and_Design.md          ← This file (theory, UML, patterns)
├── 01_Core_Types.java               ← LogLevel, LogRecord (Builder)
├── 02_Interfaces.java               ← LogFormatter, LogFilter, LogAppender
├── 03_Formatters.java               ← SimpleFormatter, PatternFormatter, JsonFormatter
├── 04_Filters.java                  ← LevelFilter, RegexFilter, CompositeFilter
├── 05_Appenders.java                ← ConsoleAppender, FileAppender, AsyncAppender
├── 06_Logger.java                   ← Logger (Builder + core log() logic)
├── 07_LogManager.java               ← Singleton registry
└── 08_Demo.java                     ← Main driver — wires everything together
```
