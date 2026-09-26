# Java Interview Masterclass: Tricky Questions, Gotchas & Edge Cases

Welcome to the **Java Interview Masterclass: Tricky Questions, Gotchas & Edge Cases Suite**. This module provides a staff/principal-engineer level deep dive into Java's most deceptive language traps, runtime puzzles, JVM specification edge cases, memory leakage vectors, polymorphic initialization pitfalls, and concurrency gotchas frequently targeted by top-tier FAANG/Big-Tech technical interviewers.

---

## Module Sitemap

| File | Type | Description |
| :--- | :--- | :--- |
| **[`README.md`](README.md)** | Index | Master module index, sitemap, interview preparation framework, and core topic taxonomy. |
| **[`00_Interview_MindMap.md`](00_Interview_MindMap.md)** | Mind Map | Interactive visual Mermaid diagram mapping Syntax & Types, OOP Inheritance, Control Flow & Exceptions, Memory & GC, Concurrency, and Collections. |
| **[`00_Recommended_Books_and_Resources.md`](00_Recommended_Books_and_Resources.md)** | Resources | Recommended textbooks (*Java Puzzlers*, *Effective Java*, *Java Concurrency in Practice*), OpenJDK bug trackers, and JLS edge-case sections. |
| **[`00_theory.md`](00_theory.md)** | Theory | Exhaustive theoretical reference covering 25 Staff/Principal Java Interview Questions with deep architectural answers and bytecode explanations. |
| **[`01_syntax_types_gotchas.java`](01_syntax_types_gotchas.java)** | Code | Integer Caching (`-128` to `127`), floating-point precision (`0.1 + 0.2 != 0.3`), ternary numeric auto-promotion (`flag ? 1 : 2.0`), String Pool `intern()`, and `Double.NaN == Double.NaN` (`false`). |
| **[`02_oop_inheritance_override_traps.java`](02_oop_inheritance_override_traps.java)** | Code | Polymorphic field hiding vs method overriding, calling overridden methods in super constructor (uninitialized child field reads), overload resolution order (widening vs boxing vs varargs), and default interface method diamond resolution. |
| **[`03_control_flow_exceptions_edge_cases.java`](03_control_flow_exceptions_edge_cases.java)** | Code | `finally` block return overrides & exception swallowing, `sneakyThrow` checked exception erasure, try-with-resources auto-close order & suppressed exceptions (`Throwable.getSuppressed()`), and `System.exit()`. |
| **[`04_collections_concurrency_gotchas.java`](04_collections_concurrency_gotchas.java)** | Code | Mutating `HashMap` key object `hashCode()` after insertion, `Arrays.asList()` fixed-size mutation traps, `ThreadLocal` task leaks across thread pool workers, and `ConcurrentHashMap` null-key restrictions. |
| **[`05_memory_gc_classloader_traps.java`](05_memory_gc_classloader_traps.java)** | Code | ClassLoader delegation model hierarchy, static field memory leaks, PhantomReference vs WeakReference vs SoftReference GC handling, and off-heap native memory via `Unsafe`. |
| **[`06_staff_interview_puzzle_suite.java`](06_staff_interview_puzzle_suite.java)** | Code | Executable Java Interview Puzzle Suite compiling 10 real-world FAANG staff/principal interview puzzles with automated assertions and explanations. |

---

## High-Frequency Interview Gotchas Overview

```
1. Integer Caching:
   Integer a = 100, b = 100; (a == b) -> TRUE  (Cached in IntegerCache)
   Integer x = 200, y = 200; (x == y) -> FALSE (Heap allocated Object reference check)

2. Super Constructor Overridden Method Invocation:
   Parent() constructor calls overridden print() -> Subclass field is STILL UNINITIALIZED (0/null)!

3. Exception Swallowing in Finally:
   try { throw new RuntimeException("A"); } finally { return 42; } -> Exception "A" IS ERASED!

4. Ternary Operator Numeric Promotion:
   boolean flag = true;
   Object obj = flag ? 1 : 2.0; -> obj is Double 1.0 (promoted to common type Double)!

5. Overload Resolution Hierarchy:
   Exact Match -> Widening (int to long) -> Auto-boxing (int to Integer) -> Varargs (int...)
```
