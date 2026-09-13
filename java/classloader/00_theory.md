# JVM ClassLoader Architecture, Internals & Memory Mechanics
> **Study Goal:** Master JVM Class Loading at the specification, bytecode, and Metaspace memory level.
> Staff & Senior Engineers are expected to debug ClassNotFoundException, NoClassDefFoundError, ClassCastException across ClassLoaders, and Metaspace memory leaks.

---

## 1. The 3 Phases of Class Lifecycle

When a Java class is referenced for the first time, the JVM executes a strict 3-phase lifecycle:

```
+-----------------------------------------------------------------------------------+
|                                1. LOADING                                         |
|  - Locate binary .class bytecode (File, JAR, Network stream, DB, or Generated)   |
|  - Create java.lang.Class object instance in Metaspace / Heap                    |
+-----------------------------------------------------------------------------------+
                                          │
                                          ▼
+-----------------------------------------------------------------------------------+
|                                2. LINKING                                         |
|  A. Verification: Validate bytecode layout, stack frames, type safety, bounds    |
|  B. Preparation:  Allocate memory for static fields & initialize to defaults     |
|                   (e.g., int -> 0, boolean -> false, Object -> null)              |
|  C. Resolution:   Replace symbolic references in Constant Pool with direct memory |
|                   pointers (can be lazy or eager)                                 |
+-----------------------------------------------------------------------------------+
                                          │
                                          ▼
+-----------------------------------------------------------------------------------+
|                              3. INITIALIZATION                                    |
|  - Execute class initializer <clinit>() method                                    |
|  - Assign actual initial values to static variables                               |
|  - Execute static initializer blocks in top-down source code order               |
+-----------------------------------------------------------------------------------+
```

---

## 2. ClassLoader Hierarchy (Built-in ClassLoaders)

Prior to Java 9 (and maintained with module boundaries in Java 9+):

```
                       Bootstrap ClassLoader (C++ Native / null)
                                      ▲
                                      │  Delegates Upward
                      Platform / Extension ClassLoader
                                      ▲
                                      │  Delegates Upward
                     Application / System ClassLoader
                                      ▲
                                      │  Delegates Upward
                        Custom / WebApp ClassLoader
```

### 1. Bootstrap ClassLoader
- Written in native C/C++ inside the JVM core.
- Loads base JDK classes (`java.lang.*`, `java.util.*`, `java.io.*` from `rt.jar` in Java 8, or `java.base` module in Java 9+).
- `Object.class.getClassLoader()` returns `null` in Java code because it is not a `java.lang.ClassLoader` Java object instance!

### 2. Platform ClassLoader (formerly Extension ClassLoader)
- Loads platform extension classes (`java.compiler`, `java.scripting`, etc.).
- In Java 8: loaded JARs from `lib/ext`.
- In Java 9+: instance of `jdk.internal.loader.ClassLoaders$PlatformClassLoader`.

### 3. Application (System) ClassLoader
- Loads application classes from the application `-classpath` / `--class-path` or `-jar`.
- Instance of `jdk.internal.loader.ClassLoaders$AppClassLoader`.
- Returned by `ClassLoader.getSystemClassLoader()`.

---

## 3. The Delegation Hierarchy Principle

When a ClassLoader is requested to load a class by name:

```
1. Check if class is already loaded in its local cache?
   └── YES -> Return Class object immediately.
   └── NO  -> Delegate to Parent ClassLoader.
              └── Parent delegates to its Parent... up to Bootstrap ClassLoader.
2. If Bootstrap ClassLoader cannot find class:
   └── Parent attempts to load class via findClass().
3. If all parents fail to find class:
   └── Current ClassLoader calls its own findClass(name).
   └── If still not found -> throw ClassNotFoundException!
```

### Why Parent-First Delegation?
- **Security & Consistency:** Prevents custom user code from overriding core Java classes like `java.lang.Object` or `java.lang.String`.

---

## 4. Reverse Delegation (Child-First / WebApp ClassLoader)

In application servers (Tomcat, Jetty, OSGi):

- Multiple web apps run inside the same JVM instance.
- Web App A may depend on `Jackson 2.10`, while Web App B depends on `Jackson 2.15`.
- Standard Parent-First delegation would force all web apps to share the server's version.

### How Child-First ClassLoader Works:
Overriding `loadClass(String name, boolean resolve)`:
1. Check local cache.
2. If class belongs to JDK core (`java.*`, `javax.*`), **must delegate to Parent first** (for security).
3. Try loading locally (`findClass()`) from WebApp `/WEB-INF/lib/` or `/WEB-INF/classes/`.
4. If not found locally, delegate to Parent ClassLoader.

---

## 5. Unique Class Identity in JVM

In the JVM runtime specification:

$$\text{Class Identity} = (\text{Fully Qualified Binary Name}, \text{ClassLoader Instance Reference})$$

### Consequences:
1. **ClassCastException across ClassLoaders:** If `Class A` is loaded by `ClassLoader 1` and `ClassLoader 2`, `(A1) a2` will throw `ClassCastException` even if the underlying `.class` byte array is identical!
2. **Multiple Static States:** Each ClassLoader instance creates its own isolated static variable space for the classes it loads.

---

## 6. Metaspace Memory & Class Unloading Rules

Classes and their metadata (Constant Pool, method bytecodes, field layouts) are stored in **Metaspace** (Native Memory outside Java Heap).

### Class Unloading Requirements:
A Class loaded by a custom ClassLoader can ONLY be garbage collected and removed from Metaspace if ALL 3 conditions are simultaneously true:

1. **Zero Instances:** 0 instances of any class loaded by this ClassLoader exist on the Heap.
2. **Zero Class Objects:** 0 `java.lang.Class` objects loaded by this ClassLoader are referenced anywhere on the Heap or Stack.
3. **ClassLoader Unreachable:** The `ClassLoader` instance itself is completely unreachable by any GC root.

> [!CAUTION]
> **Common Metaspace Leak Source:** Storing custom class instances inside `ThreadLocal` variables of long-lived worker threads (e.g., Tomcat HTTP thread pool threads). The thread holds a reference $\to$ Class instance $\to$ Class object $\to$ Custom ClassLoader $\to$ Metaspace Leak!
