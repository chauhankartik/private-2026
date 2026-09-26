# Java ClassLoader Architecture, Internals & Custom Loading

Welcome to the **Java ClassLoader Architecture & Internals** module. This module provides an in-depth exploration of how the Java Virtual Machine (JVM) dynamically loads, links, and initializes classes at runtime.

---

## 📂 Module Sitemap

| File | Topic Covered | Highlights |
| :--- | :--- | :--- |
| [**`00_theory.md`**](./00_theory.md) | ClassLoader Core Theory & Internals | 3 Loading Phases, Delegation Hierarchy, Parent-First vs Child-First, Metaspace Layout |
| [**`01_classloader_hierarchy.java`**](./01_classloader_hierarchy.java) | ClassLoader Delegation Hierarchy | Programmatic tree inspection, Bootstrap vs Platform vs System, Thread Context ClassLoader |
| [**`02_custom_classloader.java`**](./02_custom_classloader.java) | Custom Byte Encrypted ClassLoader | Overriding `findClass()`, `defineClass()`, loading encrypted byte arrays / custom network streams |
| [**`03_child_first_classloader.java`**](./03_child_first_classloader.java) | Child-First (Reverse Delegation) | Overriding `loadClass()`, Tomcat/Jetty WebApp ClassLoader mechanics, JDK delegation safeguards |
| [**`04_bytecode_redefinition_agent.java`**](./04_bytecode_redefinition_agent.java) | Bytecode Instrumentation & Hot Swap | `ClassFileTransformer`, dynamic bytecode manipulation, `java.lang.instrument` API |
| [**`05_classloader_leaks_metaspace.java`**](./05_classloader_leaks_metaspace.java) | Metaspace Leaks & Diagnostics | `java.lang.OutOfMemoryError: Metaspace`, ThreadLocal leaks, Class unloading requirements |

---

## 🧠 Core Architecture Mindmap

```
                    JVM Class Loading Pipeline
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
    1. LOADING              2. LINKING             3. INITIALIZATION
 ┌─────────────┐       ┌─────────────────┐       ┌──────────────────┐
 │ Locate byte │       │ A. Verification │       │ Execute <clinit> │
 │ stream via  │  ───> │ B. Preparation  │  ───> │ static blocks &  │
 │ ClassLoader │       │ C. Resolution   │       │ static field init│
 └─────────────┘       └─────────────────┘       └──────────────────┘
```

---

## 🚀 Key Takeaways

1. **Delegation Hierarchy:** ClassLoaders delegate loading requests up to their parent before attempting to load the class themselves (Parent-First).
2. **Class Identity:** In the JVM, a class is uniquely identified by the pair: `(Fully Qualified Class Name, ClassLoader Instance)`. Two classes loaded by different ClassLoaders are incompatible types even if their bytecode is identical!
3. **Child-First Exception:** Web containers (Tomcat, Jetty) use Child-First delegation for web application dependencies while protecting `java.*` system packages.
4. **Metaspace Leak Warning:** A custom ClassLoader cannot be garbage collected if any instance of a class loaded by it, any `Class` object, or any thread (via ThreadLocal) holds a strong reference to it!
