# Recommended Books & Resources: Java Generics

A curated list of authoritative books, specification documents, JLS chapters, and open-source codebases for mastering Java Generics.

---

## 1. Essential Books

1. **Effective Java (3rd Edition)**  
   *Author:* Joshua Bloch  
   *Focus:* **Chapter 5: Generics (Items 26–33)**. The definitive practical guide covering Raw Types, Unchecked Warnings, Arrays vs Lists, PECS (`<? extends T>` vs `<? super T>`), `@SafeVarargs`, and Typesafe Heterogeneous Containers.

2. **Java Generics and Collections**  
   *Authors:* Maurice Naftalin, Philip Wadler  
   *Focus:* The seminal book dedicated entirely to Java Generics. Deep coverage of wildcards, subtyping, type erasure, reflection over generics, performance, and collection design.

3. **The Java Language Specification (JLS - Java SE 21 Edition)**  
   *Publisher:* Oracle / James Gosling et al.  
   *Focus:* **Chapter 4 (Types, Values, and Variables)** and **Chapter 8 (Classes)**. Canonical specification for type erasure, subtyping rules, captures, and bridge method generation.

---

## 2. Technical Articles & JEP Specifications

* **[Super Type Tokens by Neal Gafter](SDN/blogs)** — The original blog post introducing the Super Type Token pattern to capture generic type parameters at runtime via subclassing.
* **[JEP 218: Generics Over Primitive Types (Valhalla Exploration)](https://openjdk.org/jeps/218)** — Exploration of specialized generics for primitive types (`int`, `double`, `long`).
* **[JEP 401: Value Objects (Preview)](https://openjdk.org/jeps/401)** — Identityless value objects laying the foundation for Primitive Generics in Project Valhalla.

---

## 3. Recommended Codebase Exploration

* **[`java.util.Collections` Source Code (OpenJDK)](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/Collections.java)** — Inspect usage of PECS in standard library methods:
  * `Collections.copy(List<? super T> dest, List<? extends T> src)`
  * `Collections.sort(List<T> list, Comparator<? super T> c)`
* **[`org.springframework.core.ParameterizedTypeReference` (Spring Framework)](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/ParameterizedTypeReference.java)** — Real-world Super Type Token implementation for REST template deserialization.
