# Java Interview Recommended Books & Resources

A curated reading list of essential textbooks, official specifications, OpenJDK bug trackers, and articles for mastering Java interview edge cases, puzzles, and JVM gotchas.

---

## Must-Read Textbooks & References

1. **"Java Puzzlers: Traps, Pitfalls, and Corner Cases" by Joshua Bloch & Neal Gafter**
   - *Focus:* Definitive collection of 95 Java puzzles covering corner cases in expression evaluation, object-oriented design, library APIs, and multithreading.
   - *Key Puzzles:* Puzzle 1 (Oddity), Puzzle 24 (A Big Delight in Every Byte), Puzzle 50 (Not Your Average String), Puzzle 70 (Package Deal), Puzzle 85 (Lazy Initialization).

2. **"Effective Java" (3rd Edition) by Joshua Bloch**
   - *Focus:* Architectural rules, object creation traps, immutability, and serialization pitfalls.
   - *Key Items:*
     - **Item 10:** Obey the general contract when overriding `equals`.
     - **Item 11:** Always override `hashCode` when you override `equals`.
     - **Item 19:** Design and document for inheritance or else prohibit it (Never call overridable methods in constructors!).
     - **Item 79:** Avoid excessive synchronization (Open calls).

3. **"Java Concurrency in Practice" by Brian Goetz et al.**
   - *Focus:* Concurrency traps, memory visibility, publication safety, and thread pool worker leaks.
   - *Key Chapters:* Chapter 3 (Sharing Objects - Publication & Escape), Chapter 10 (Avoiding Liveness Hazards - Deadlocks & Starvation).

4. **"The Java Virtual Machine Specification" (JVMS) & Java Language Specification (JLS)**
   - *JLS Section 5.6:* Numeric Contextual Promotion Rules.
   - *JLS Section 15.12.2:* Compile-Time Step-by-Step Method Invocation Resolution (Overloading precedence).
   - *JLS Section 14.20:* Execution of `try-finally` blocks.

---

## OpenJDK Pointers & Bug Reports

- `src/java.base/share/classes/java/lang/Integer.java` — `IntegerCache` inner class implementation (`-XX:AutoBoxCacheMax`).
- `src/java.base/share/classes/java/lang/String.java` — `intern()` native method delegation and String table interaction.
- `src/java.base/share/classes/java/lang/ThreadLocal.java` — `ThreadLocalMap` weak reference key design and stale entry purging during `get()` / `set()`.
- OpenJDK Bug Database: Search for `JDK-8010319` (Sneaky throw type erasure) and `JDK-8025219` (Lambda synthetic method naming).
