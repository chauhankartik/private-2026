/**
 * ============================================================
 *  JAVA REFLECTION — PHASE 4: PERFORMANCE, METHODHANDLES & INTROSPECTION
 * ============================================================
 *
 * Topics:
 *   1. Why Reflection is slow (no JIT, security checks, boxing)
 *   2. Caching reflection objects — the first optimization
 *   3. MethodHandles (java.lang.invoke) — the modern, fast alternative
 *   4. BeanInfo & PropertyDescriptor — high-level getter/setter access
 *
 * Run: javac 07_performance.java && java PerformanceDemo
 * ============================================================
 */
import java.beans.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

// ─────────────────────────────────────────────────────────────
// Domain class for all demos
// ─────────────────────────────────────────────────────────────

class Employee {
    private String  name;
    private int     age;
    private double  salary;
    private String  department;

    public Employee() {}

    public Employee(String name, int age, double salary, String department) {
        this.name = name; this.age = age;
        this.salary = salary; this.department = department;
    }

    // Standard JavaBean getters/setters (required for BeanInfo)
    public String getName()          { return name; }
    public void   setName(String v)  { this.name = v; }
    public int    getAge()           { return age; }
    public void   setAge(int v)      { this.age = v; }
    public double getSalary()        { return salary; }
    public void   setSalary(double v){ this.salary = v; }
    public String getDepartment()    { return department; }
    public void   setDepartment(String v) { this.department = v; }

    @Override
    public String toString() {
        return "Employee{name='" + name + "', age=" + age
            + ", salary=" + salary + ", dept='" + department + "'}";
    }
}

// ─────────────────────────────────────────────────────────────
public class PerformanceDemo {

    static final int ITERATIONS = 500_000;

    public static void main(String[] args) throws Throwable {
        demo1_whyReflectionIsSlow();
        demo2_cachingReflectionObjects();
        demo3_methodHandles();
        demo4_methodHandle_vs_reflection_benchmark();
        demo5_beanInfo_propertyDescriptor();
        demo6_practical_mapper_with_beanInfo();
    }

    // =========================================================
    // Demo 1: Why Reflection is Slow
    // =========================================================
    /**
     * THREE COSTS of reflective calls:
     *
     * 1. SECURITY CHECKS per call
     *    Every Method.invoke() / Field.get() checks:
     *    - Is caller allowed to access this method/field?
     *    - Is setAccessible respected?
     *    These are NOT free — they involve stack walking (pre-Java 9)
     *    or module/access control checking.
     *
     * 2. NO JIT OPTIMIZATION
     *    The JIT compiler can inline direct method calls:
     *      employee.getName()  →  return employee.name  (inlined, zero overhead)
     *    With reflection, the JIT sees Method.invoke() — it cannot see through it
     *    to inline the actual method body. Every call goes through the interpreter
     *    or a generic stub.
     *    (Note: Java 8+ does attempt to "inflate" hot reflective calls into native
     *     stubs after ~15 calls via sun.reflect.NativeMethodAccessorImpl, but this
     *     is still slower than JIT-compiled direct calls.)
     *
     * 3. BOXING OVERHEAD
     *    Method.invoke(obj, args) takes Object...
     *    Every primitive argument (int, double) must be AUTO-BOXED:
     *      field.set(obj, 42) → new Integer(42) allocated on heap
     *    For hot paths called millions of times, this creates GC pressure.
     *
     * BENCHMARK: direct call vs reflection vs cached reflection
     */
    static void demo1_whyReflectionIsSlow() throws Exception {
        separator("Demo 1: Why Reflection Is Slow");

        Employee emp = new Employee("Alice", 30, 90000.0, "Eng");

        // ─── Method lookup overhead ────────────────────────────────
        System.out.println("Cost 1: Method.getDeclaredMethod() lookup (do NOT do this in a loop)");
        long t0 = System.nanoTime();
        for (int i = 0; i < 10_000; i++) {
            // ✗ BAD PATTERN: looking up the method EVERY iteration
            Method m = Employee.class.getDeclaredMethod("getName");
            m.invoke(emp);
        }
        long lookupCost = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        Method cachedMethod = Employee.class.getDeclaredMethod("getName");
        cachedMethod.setAccessible(true);
        for (int i = 0; i < 10_000; i++) {
            // ✓ GOOD PATTERN: method cached once, invoked many times
            cachedMethod.invoke(emp);
        }
        long cachedCost = System.nanoTime() - t1;

        System.out.printf("  With lookup per call:   %,d µs%n", lookupCost / 1000);
        System.out.printf("  With cached Method:     %,d µs%n", cachedCost / 1000);
        System.out.printf("  Speedup from caching:   %.1fx%n", (double) lookupCost / cachedCost);

        // ─── Security check overhead ───────────────────────────────
        System.out.println("\nCost 2: setAccessible(true) eliminates per-call access check");
        Method withCheck    = Employee.class.getDeclaredMethod("getName");
        // withCheck.setAccessible(true) NOT called — access check runs each invoke

        Method withoutCheck = Employee.class.getDeclaredMethod("getName");
        withoutCheck.setAccessible(true);  // done ONCE

        t0 = System.nanoTime();
        for (int i = 0; i < 10_000; i++) withCheck.invoke(emp);
        long checkCost = System.nanoTime() - t0;

        t1 = System.nanoTime();
        for (int i = 0; i < 10_000; i++) withoutCheck.invoke(emp);
        long noCheckCost = System.nanoTime() - t1;

        System.out.printf("  Without setAccessible:  %,d µs%n", checkCost / 1000);
        System.out.printf("  With setAccessible:     %,d µs%n", noCheckCost / 1000);
    }

    // =========================================================
    // Demo 2: Caching Reflection Objects — Rule #1
    // =========================================================
    /**
     * The single most impactful optimization:
     *   NEVER look up Field/Method/Constructor inside a loop.
     *   Cache them at class load time as static final fields.
     *
     * This is EXACTLY what Spring, Hibernate, and Jackson do:
     *   - At application startup, they scan and cache all reflection metadata.
     *   - During request handling, only invoke()/get()/set() are called.
     */
    static void demo2_cachingReflectionObjects() throws Exception {
        separator("Demo 2: Caching Reflection Objects");

        // ─── Correct pattern: static cache ────────────────────────
        System.out.println("✓ Correct pattern: cache reflection objects as static fields");
        System.out.println("""
              // In your framework/utility class:
              class EmployeeMapper {
                  // Looked up ONCE at class initialization
                  private static final Method GET_NAME;
                  private static final Field  SALARY_FIELD;
                  static {
                      try {
                          GET_NAME    = Employee.class.getMethod("getName");
                          SALARY_FIELD = Employee.class.getDeclaredField("salary");
                          SALARY_FIELD.setAccessible(true);
                      } catch (ReflectiveOperationException e) {
                          throw new ExceptionInInitializerError(e);
                      }
                  }
                  // Called millions of times — zero lookup cost
                  static String name(Employee e)   throws Exception { return (String) GET_NAME.invoke(e); }
                  static double salary(Employee e) throws Exception { return SALARY_FIELD.getDouble(e); }
              }
            """);

        // ─── ConcurrentHashMap-based dynamic cache ─────────────────
        System.out.println("✓ Dynamic cache (for unknown classes at startup):");
        System.out.println("  Use ConcurrentHashMap<Class<?>, Map<String, Field>> (see 03_advanced.java ReflectionCache)");
    }

    // =========================================================
    // Demo 3: MethodHandles — The Modern Alternative
    // =========================================================
    /**
     * java.lang.invoke.MethodHandle (Java 7+)
     *
     * A MethodHandle is a "typed, directly executable reference to
     * an underlying method, constructor, field, or similar low-level operation."
     *
     * WHY FASTER THAN Method.invoke():
     * 1. The JIT CAN see through a MethodHandle and inline the target.
     *    Method.invoke() is opaque to the JIT; MethodHandle is not.
     * 2. No boxing for primitive arguments (invokeExact).
     * 3. Security check done ONCE at handle creation, not per call.
     * 4. Part of the JVM invokedynamic infrastructure (same as lambdas).
     *
     * VOCABULARY:
     *   MethodHandles.lookup()         → create a Lookup in current access context
     *   lookup.findVirtual()           → instance method
     *   lookup.findStatic()            → static method
     *   lookup.findGetter()            → field read
     *   lookup.findSetter()            → field write
     *   lookup.findSpecial()           → super method call
     *   lookup.findConstructor()       → constructor
     *   MethodType.methodType(ret, params) → describe the type signature
     */
    static void demo3_methodHandles() throws Throwable {
        separator("Demo 3: MethodHandles (java.lang.invoke)");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Employee emp = new Employee("Bob", 28, 75000.0, "HR");

        // ── 3a. Instance method handle ────────────────────────────
        System.out.println("3a. findVirtual() — instance method:");
        MethodType getNameType = MethodType.methodType(String.class);
        MethodHandle getNameHandle = lookup.findVirtual(Employee.class, "getName", getNameType);
        String name = (String) getNameHandle.invoke(emp);
        System.out.println("  getName() via handle: " + name);

        // invokeExact() — no boxing, strictest type matching
        String nameExact = (String) getNameHandle.invokeExact(emp);
        System.out.println("  getName() invokeExact: " + nameExact);

        // ── 3b. Setter method handle ──────────────────────────────
        System.out.println("\n3b. findVirtual() — setter:");
        MethodType setNameType   = MethodType.methodType(void.class, String.class);
        MethodHandle setNameHandle = lookup.findVirtual(Employee.class, "setName", setNameType);
        setNameHandle.invoke(emp, "Bobby");
        System.out.println("  After setName('Bobby'): " + emp.getName());

        // ── 3c. Field getter / setter handles ─────────────────────
        System.out.println("\n3c. findGetter() / findSetter() — field handles:");
        // Note: these only work on PUBLIC fields.
        // For private fields, use MethodHandles.privateLookupIn() (Java 9+)
        System.out.println("  (Field handles require public field or privateLookupIn in Java 9+)");
        System.out.println("  Using VarHandle for more control — see below.");

        // ── 3d. VarHandle — field access with memory semantics ────
        System.out.println("\n3d. VarHandle (Java 9+) — access private fields:");
        // privateLookupIn() requires the module to be open (or be in same module)
        try {
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(
                Employee.class, MethodHandles.lookup()
            );
            VarHandle salaryHandle = privateLookup.findVarHandle(Employee.class, "salary", double.class);
            double salary = (double) salaryHandle.get(emp);
            System.out.println("  salary via VarHandle: " + salary);

            salaryHandle.set(emp, 99000.0);
            System.out.println("  After set(99000.0): " + emp.getSalary());
        } catch (Exception e) {
            System.out.println("  (privateLookupIn restricted in this context: " + e.getClass().getSimpleName() + ")");
        }

        // ── 3e. Constructor handle ────────────────────────────────
        System.out.println("\n3e. findConstructor():");
        MethodType ctorType = MethodType.methodType(void.class,
            String.class, int.class, double.class, String.class);
        MethodHandle ctorHandle = lookup.findConstructor(Employee.class, ctorType);
        Employee emp2 = (Employee) ctorHandle.invoke("Carol", 35, 110000.0, "Eng");
        System.out.println("  Constructed: " + emp2);

        // ── 3f. Binding arguments ─────────────────────────────────
        System.out.println("\n3f. bindTo() — partially apply an instance:");
        MethodHandle boundGetName = getNameHandle.bindTo(emp2);
        // Now boundGetName is like () -> emp2.getName() — no need to pass emp2 each time
        System.out.println("  boundGetName() = " + (String) boundGetName.invoke());

        // ── 3g. asType() — adapt signatures ──────────────────────
        System.out.println("\n3g. asType() — adapt to different type:");
        // Convert the handle so we can call it with Object instead of Employee
        MethodHandle objectHandle = getNameHandle.asType(MethodType.methodType(Object.class, Object.class));
        Object result = objectHandle.invoke((Object) emp);
        System.out.println("  asType Object invoke: " + result);
    }

    // =========================================================
    // Demo 4: Benchmark — direct vs reflection vs MethodHandle
    // =========================================================
    static void demo4_methodHandle_vs_reflection_benchmark() throws Throwable {
        separator("Demo 4: Benchmark — Direct vs Reflection vs MethodHandle");

        Employee emp = new Employee("Dave", 40, 120000.0, "Finance");

        // Prepare handles and methods OUTSIDE the timing loop (cache them!)
        Method reflectMethod = Employee.class.getMethod("getName");
        reflectMethod.setAccessible(true);

        MethodHandles.Lookup lookup  = MethodHandles.lookup();
        MethodHandle mh              = lookup.findVirtual(Employee.class, "getName",
                                            MethodType.methodType(String.class));
        MethodHandle boundMh         = mh.bindTo(emp);  // pre-bound

        // Warm-up (JIT needs a few thousand calls to compile)
        for (int i = 0; i < 20_000; i++) {
            emp.getName();
            reflectMethod.invoke(emp);
            (String) mh.invoke(emp);
        }

        // ─── Direct call ───────────────────────────────────────────
        long t = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            String s = emp.getName();
        }
        long directTime = System.nanoTime() - t;

        // ─── Reflection (cached method, setAccessible) ────────────
        t = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            String s = (String) reflectMethod.invoke(emp);
        }
        long reflectTime = System.nanoTime() - t;

        // ─── MethodHandle.invoke() ────────────────────────────────
        t = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            String s = (String) mh.invoke(emp);
        }
        long mhTime = System.nanoTime() - t;

        // ─── MethodHandle.invokeExact() (no boxing at all) ────────
        t = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            String s = (String) mh.invokeExact(emp);
        }
        long mhExactTime = System.nanoTime() - t;

        System.out.printf("  %-30s %,8d µs%n", "Direct call:",           directTime / 1000);
        System.out.printf("  %-30s %,8d µs (%.1fx slower than direct)%n",
            "Method.invoke() (cached):", reflectTime / 1000, (double) reflectTime / directTime);
        System.out.printf("  %-30s %,8d µs (%.1fx slower than direct)%n",
            "MethodHandle.invoke():", mhTime / 1000, (double) mhTime / directTime);
        System.out.printf("  %-30s %,8d µs (%.1fx slower than direct)%n",
            "MethodHandle.invokeExact():", mhExactTime / 1000, (double) mhExactTime / directTime);

        System.out.println("\n  Rule of thumb:");
        System.out.println("  Direct ≈ MethodHandle.invokeExact ≈ MethodHandle.invoke (JIT inlined)");
        System.out.println("  Method.invoke ≈ 2-10x slower (no JIT inlining, boxing, security checks)");
    }

    // =========================================================
    // Demo 5: BeanInfo & PropertyDescriptor
    // =========================================================
    /**
     * java.beans.BeanInfo / Introspector — HIGHER LEVEL than raw Reflection.
     *
     * Instead of looking for "getName" / "setName" / "name" manually,
     * the Java Beans API does it automatically:
     *   BeanInfo info = Introspector.getBeanInfo(Employee.class);
     *   PropertyDescriptor[] pds = info.getPropertyDescriptors();
     *
     * Each PropertyDescriptor gives you:
     *   pd.getName()         → "name" (the property name, not the method name)
     *   pd.getReadMethod()   → Method object for getName()
     *   pd.getWriteMethod()  → Method object for setName()
     *   pd.getPropertyType() → String.class
     *
     * Used by: Spring BeanUtils.copyProperties(), Jackson (when using getters),
     *          JSF, old-style MVC frameworks, Spring MVC's @ModelAttribute binding.
     */
    static void demo5_beanInfo_propertyDescriptor() throws Exception {
        separator("Demo 5: BeanInfo & PropertyDescriptor");

        BeanInfo beanInfo = Introspector.getBeanInfo(Employee.class, Object.class);
        // passing Object.class as stopClass skips Object's own properties (class, etc.)

        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

        System.out.printf("%-15s %-12s %-20s %-20s%n",
            "Property", "Type", "Reader", "Writer");
        System.out.println("-".repeat(70));

        for (PropertyDescriptor pd : pds) {
            System.out.printf("%-15s %-12s %-20s %-20s%n",
                pd.getName(),
                pd.getPropertyType() != null ? pd.getPropertyType().getSimpleName() : "?",
                pd.getReadMethod()  != null ? pd.getReadMethod().getName()  : "(none)",
                pd.getWriteMethod() != null ? pd.getWriteMethod().getName() : "(none)"
            );
        }

        // ── Using PropertyDescriptor to get/set ───────────────────
        System.out.println("\nUsing PropertyDescriptor to get/set values:");
        Employee emp = new Employee("Eve", 32, 88000.0, "Design");

        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null) {
                Object value = pd.getReadMethod().invoke(emp);
                System.out.println("  " + pd.getName() + " = " + value);
            }
        }

        System.out.println("\nSetting 'name' via PropertyDescriptor:");
        PropertyDescriptor namePd = Arrays.stream(pds)
            .filter(pd -> "name".equals(pd.getName()))
            .findFirst()
            .orElseThrow();

        namePd.getWriteMethod().invoke(emp, "Eve Updated");
        System.out.println("  After set: " + emp.getName());
    }

    // =========================================================
    // Demo 6: Practical Mapper using BeanInfo
    // =========================================================
    /**
     * Simulates Spring's BeanUtils.copyProperties() — copies matching
     * property values from source to target using PropertyDescriptors.
     *
     * ADVANTAGES over raw reflection:
     *   - No need to manually match "name" field to "getName"/"setName".
     *   - Handles type conversion hooks (PropertyEditor).
     *   - Works regardless of field access modifier.
     *   - Follows JavaBean conventions automatically.
     */
    static void demo6_practical_mapper_with_beanInfo() throws Exception {
        separator("Demo 6: Practical Mapper (BeanInfo-based copyProperties)");

        Employee source = new Employee("Frank", 45, 150000.0, "CTO");
        Employee target = new Employee();

        System.out.println("Source: " + source);
        System.out.println("Target before copy: " + target);

        copyProperties(source, target);

        System.out.println("Target after copy:  " + target);

        // ── Map to a different class with matching property names ──
        System.out.println("\nExtracting to Map<String, Object>:");
        Map<String, Object> map = toMap(source);
        map.forEach((k, v) -> System.out.println("  " + k + " = " + v));
    }

    /** Copies all readable+writable properties from source to target. */
    static void copyProperties(Object source, Object target) throws Exception {
        BeanInfo sourceInfo = Introspector.getBeanInfo(source.getClass(), Object.class);
        BeanInfo targetInfo = Introspector.getBeanInfo(target.getClass(), Object.class);

        // Build map of target property name → PropertyDescriptor
        Map<String, PropertyDescriptor> targetProps = new HashMap<>();
        for (PropertyDescriptor pd : targetInfo.getPropertyDescriptors()) {
            if (pd.getWriteMethod() != null) targetProps.put(pd.getName(), pd);
        }

        for (PropertyDescriptor sourcePd : sourceInfo.getPropertyDescriptors()) {
            if (sourcePd.getReadMethod() == null) continue;

            PropertyDescriptor targetPd = targetProps.get(sourcePd.getName());
            if (targetPd == null) continue;  // no matching property in target

            // Type must match (or a converter would be needed)
            if (!sourcePd.getPropertyType().equals(targetPd.getPropertyType())) continue;

            Object value = sourcePd.getReadMethod().invoke(source);
            targetPd.getWriteMethod().invoke(target, value);
        }
    }

    /** Reads all properties of an object into a Map. */
    static Map<String, Object> toMap(Object source) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass(), Object.class);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (pd.getReadMethod() != null) {
                result.put(pd.getName(), pd.getReadMethod().invoke(source));
            }
        }
        return result;
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}

/*
 * ─────────────────────────────────────────────────────────────
 *  PERFORMANCE DECISION TREE
 * ─────────────────────────────────────────────────────────────
 *
 *  Need to call a method dynamically?
 *
 *  Is it called < 1000 times total (startup/config code)?
 *    → Use Method.invoke() — simple and fine.
 *
 *  Is it on a hot path (per-request, per-message, millions of times)?
 *    → Use MethodHandle.invokeExact() — JIT-friendly, no boxing.
 *
 *  Do you need getter/setter access and the class follows JavaBean conventions?
 *    → Use BeanInfo + PropertyDescriptor — higher abstraction, less error-prone.
 *
 *  Do you need to access private fields at high speed in Java 9+?
 *    → Use MethodHandles.privateLookupIn() + VarHandle.
 *
 * ─────────────────────────────────────────────────────────────
 *  METHODHANDLE QUICK REFERENCE
 * ─────────────────────────────────────────────────────────────
 *
 *  MethodHandles.lookup()                      → Lookup in current context
 *  MethodHandles.privateLookupIn(cls, lookup)  → Private access (Java 9+)
 *
 *  lookup.findVirtual(Owner, "name", type)     → instance method
 *  lookup.findStatic(Owner, "name", type)      → static method
 *  lookup.findConstructor(Owner, type)         → constructor
 *  lookup.findGetter(Owner, "field", type)     → read public field
 *  lookup.findSetter(Owner, "field", type)     → write public field
 *  lookup.findVarHandle(Owner, "field", type)  → private field (Java 9+)
 *  lookup.findSpecial(Owner, "name", type, caller) → super call
 *
 *  mh.invoke(args...)        → polymorphic, allows Object args (some boxing)
 *  mh.invokeExact(args...)   → exact types required, zero boxing
 *  mh.invokeWithArguments()  → takes a List, useful when arg count is dynamic
 *  mh.bindTo(instance)       → pre-bind an instance (currying)
 *  mh.asType(newType)        → adapt signature (add boxing/unboxing conversions)
 *
 *  MethodType.methodType(ret, param...)  → describe the signature
 *
 * ─────────────────────────────────────────────────────────────
 *  BEANINFO / INTROSPECTOR QUICK REFERENCE
 * ─────────────────────────────────────────────────────────────
 *
 *  BeanInfo info = Introspector.getBeanInfo(cls, stopClass);
 *    stopClass = Object.class to skip Object's own properties
 *
 *  info.getPropertyDescriptors()   → PropertyDescriptor[] for all properties
 *  info.getMethodDescriptors()     → MethodDescriptor[] for all public methods
 *
 *  PropertyDescriptor pd = ...;
 *    pd.getName()           → "salary"
 *    pd.getPropertyType()   → double.class
 *    pd.getReadMethod()     → Method for getSalary()
 *    pd.getWriteMethod()    → Method for setSalary(double)
 *
 *  Convention recognized by Introspector:
 *    getSalary()   → readable property "salary"
 *    setSalary(d)  → writable property "salary"
 *    isActive()    → readable boolean property "active"
 */
