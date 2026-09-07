# Java Functional Programming Recommended Books & Resources

A curated list of essential textbooks, official language specifications, OpenJDK source pointers, and articles for mastering functional programming in Java.

---

## Must-Read Textbooks & References

1. **"Modern Java in Action: Lambdas, streams, functional and reactive programming" by Raoul-Gabriel Urma, Mario Fusco, Alan Mycroft**
   - *Focus:* Comprehensive deep-dive into lambdas, streams, parallel data processing, custom collectors, optionals, and reactive programming.
   - *Key Chapters:* Chapter 2 (Passing Code with Behavior Parameterization), Chapter 3 (Lambda Expressions), Chapters 4–6 (Streams & Collectors), Chapter 7 (Parallel Data Processing & Performance), Chapter 11 (Optional).

2. **"Functional Programming in Java" by Pierre-Yves Saumont**
   - *Focus:* Applying pure functional programming concepts (immutability, recursion, memoization, monads, lazy evaluation, structural pattern matching) in Java.
   - *Key Chapters:* Chapter 2 (Functional Programming in Java), Chapter 3 (Making Java More Functional), Chapter 6 (Handling Optional Data), Chapter 7 (Handling Errors and Exceptions functionally).

3. **"Effective Java" (3rd Edition) by Joshua Bloch**
   - *Focus:* Best practices and anti-patterns when using Lambdas and Streams.
   - *Key Items:*
     - **Item 42:** Prefer lambdas to anonymous classes.
     - **Item 43:** Prefer method references to lambdas.
     - **Item 44:** Favor the use of standard functional interfaces.
     - **Item 45:** Use streams judiciously.
     - **Item 46:** Prefer side-effect-free functions in streams.
     - **Item 47:** Prefer Collection to Stream as a return type.
     - **Item 48:** Use caution when making streams parallel.
     - **Item 55:** Return optionals judiciously.

---

## Language Specifications & JVM Bytecode Mechanics

1. **Java Language Specification (JLS):**
   - [JLS Chapter 15.27: Lambda Expressions](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.27)
   - [JLS Chapter 15.28: Method Reference Expressions](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.28)

2. **JVM Specification & `invokedynamic`:**
   - JSR 292: Supporting Dynamically Typed Languages on the Java Platform (`java.lang.invoke`).
   - Brian Goetz paper: *"Translation of Lambda Expressions"* (describing `LambdaMetafactory` call site bootstrap).

---

## OpenJDK Source Code Pointers

To understand how Java translates lambdas and executes stream pipelines:

- `src/java.base/share/classes/java/lang/invoke/LambdaMetafactory.java` — `metafactory()` call site bootstrapper generating dynamic call targets via `java.lang.invoke`.
- `src/java.base/share/classes/java/util/stream/Stream.java` — Public stream pipeline interface methods.
- `src/java.base/share/classes/java/util/stream/AbstractPipeline.java` — Core internal implementation of stream evaluation, Sink chaining, and Spliterator linkage.
- `src/java.base/share/classes/java/util/stream/Collectors.java` — Implementations of `groupingBy()`, `partitioningBy()`, `toMap()`, `teeing()`, and predefined collectors.
- `src/java.base/share/classes/java/util/Optional.java` — Monadic method definitions (`map`, `flatMap`, `filter`, `or`).
