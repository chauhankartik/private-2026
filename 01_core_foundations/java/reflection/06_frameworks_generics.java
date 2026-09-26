/**
 * ============================================================
 *  JAVA REFLECTION — PHASE 3: FRAMEWORKS & GENERICS
 * ============================================================
 *
 * Topics:
 *   1. Runtime Annotation Processing — how Spring/JUnit work
 *   2. @ExecuteMe — 5-line conditional method execution
 *   3. Type Erasure & Generics — getGenericReturnType(), ParameterizedType
 *
 * Run: javac 06_frameworks_generics.java && java FrameworksDemo
 * ============================================================
 */
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// ─────────────────────────────────────────────────────────────
// CUSTOM ANNOTATIONS (all with RUNTIME retention)
// ─────────────────────────────────────────────────────────────

/** Marks a method to be executed by our mini runner */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ExecuteMe {
    String description() default "";
    int    order()       default 0;
}

/** Simulates @Scheduled — marks a method to run on a cron schedule */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Scheduled {
    String cron();
}

/** Simulates @Transactional — marks a method to run inside a transaction */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Transactional {
    boolean readOnly() default false;
    int timeoutSeconds() default 30;
}

/** Simulates @RequestBody — marks a parameter as coming from HTTP request body */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface RequestBody {}

/** Simulates @RequestParam — marks a parameter as coming from query string */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@interface RequestParam {
    String name();
    boolean required() default true;
}

// ─────────────────────────────────────────────────────────────
// SAMPLE SERVICE — annotated like a real Spring service
// ─────────────────────────────────────────────────────────────

class ReportService {

    @ExecuteMe(description = "Generate daily summary", order = 1)
    public void generateDailySummary() {
        System.out.println("    [Report] Daily summary generated.");
    }

    @ExecuteMe(description = "Archive old records", order = 2)
    public String archiveOldRecords() {
        System.out.println("    [Report] Archived 500 records.");
        return "archived:500";
    }

    @ExecuteMe(description = "Send email digest", order = 3)
    @Transactional(readOnly = true)
    public void sendEmailDigest() {
        System.out.println("    [Report] Email digest sent.");
    }

    // NOT annotated — should NOT be executed by the runner
    public void internalCleanup() {
        System.out.println("    [Report] Internal cleanup — should NOT run.");
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void morningReport() {
        System.out.println("    [Report] Morning report dispatched.");
    }

    @Transactional(readOnly = false, timeoutSeconds = 60)
    public void writeTransactionalData(@RequestBody String payload, @RequestParam(name = "userId") int userId) {
        System.out.println("    [Report] Wrote data for userId=" + userId + ", payload=" + payload);
    }
}

// ─────────────────────────────────────────────────────────────
// SAMPLE CLASS — for demonstrating Type Erasure
// ─────────────────────────────────────────────────────────────

class DataRepository {

    // Generic method — return type visible via getGenericReturnType()
    public List<String>               findAllNames()        { return List.of("Alice", "Bob"); }
    public Map<String, List<Integer>> findScoresByStudent() { return new HashMap<>(); }
    public Optional<String>           findFirstName()       { return Optional.of("Alice"); }

    // Generic field
    private Map<String, List<Double>> priceHistory;
    private List<String>              nameCache;

    // Generic parameter
    public void saveAll(List<String> items) {}
    public void process(Map<String, Integer> config, List<Double> values) {}
}

// ─────────────────────────────────────────────────────────────
public class FrameworksDemo {

    public static void main(String[] args) throws Exception {
        demo1_executeMe_fiveLine();
        demo2_runtimeAnnotationProcessing();
        demo3_parameterAnnotations();
        demo4_typeErasure();
        demo5_genericFields();
        demo6_genericParameters();
        demo7_miniFramework();
    }

    // =========================================================
    // Demo 1: @ExecuteMe — The 5-line task
    // =========================================================
    static void demo1_executeMe_fiveLine() throws Exception {
        separator("Demo 1: @ExecuteMe — 5-Line Conditional Execution");

        ReportService service = new ReportService();

        // ─── THE 5 LINES ──────────────────────────────────────────
        System.out.println("  5-line version:");
        for (Method m : service.getClass().getDeclaredMethods())        // 1. get all methods
            if (m.isAnnotationPresent(ExecuteMe.class))                 // 2. check annotation
                m.invoke(service);                                      // 3. execute it
        // (3 lines of logic + 2 braces = 5 effective lines)

        // ─── Sorted version (using order attribute) ───────────────
        System.out.println("\n  Sorted by @ExecuteMe.order():");
        Arrays.stream(service.getClass().getDeclaredMethods())
              .filter(m -> m.isAnnotationPresent(ExecuteMe.class))
              .sorted(Comparator.comparingInt(m -> m.getAnnotation(ExecuteMe.class).order()))
              .forEach(m -> {
                  try {
                      ExecuteMe ann = m.getAnnotation(ExecuteMe.class);
                      System.out.println("  [order=" + ann.order() + "] " + ann.description());
                      m.invoke(service);
                  } catch (Exception e) { throw new RuntimeException(e); }
              });
    }

    // =========================================================
    // Demo 2: Runtime Annotation Processing
    // =========================================================
    static void demo2_runtimeAnnotationProcessing() throws Exception {
        separator("Demo 2: Runtime Annotation Processing");

        Class<?> clazz = ReportService.class;

        // ── isAnnotationPresent() — quick boolean check ───────────
        System.out.println("Method annotation checks:");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.printf("  %-30s | @ExecuteMe=%-5b @Scheduled=%-5b @Transactional=%-5b%n",
                m.getName(),
                m.isAnnotationPresent(ExecuteMe.class),
                m.isAnnotationPresent(Scheduled.class),
                m.isAnnotationPresent(Transactional.class)
            );
        }

        // ── getAnnotation() — read attribute values ───────────────
        System.out.println("\nReading @Scheduled attribute:");
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Scheduled.class)) {
                Scheduled s = m.getAnnotation(Scheduled.class);
                System.out.println("  " + m.getName() + " → cron: '" + s.cron() + "'");
            }
        }

        System.out.println("\nReading @Transactional attributes:");
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Transactional.class)) {
                Transactional tx = m.getAnnotation(Transactional.class);
                System.out.println("  " + m.getName()
                    + " → readOnly=" + tx.readOnly()
                    + ", timeout=" + tx.timeoutSeconds() + "s");
            }
        }

        // ── getDeclaredAnnotations() — ALL annotations on a method ─
        System.out.println("\nAll annotations on sendEmailDigest():");
        Method sendEmail = clazz.getDeclaredMethod("sendEmailDigest");
        for (Annotation ann : sendEmail.getDeclaredAnnotations()) {
            System.out.println("  " + ann.annotationType().getSimpleName()
                + " → " + ann);
        }

        // ── getAnnotations() vs getDeclaredAnnotations() ──────────
        // getAnnotations()         → includes @Inherited annotations from superclass
        // getDeclaredAnnotations() → ONLY directly present on this element
        System.out.println("\ngetAnnotations() vs getDeclaredAnnotations() on method:");
        System.out.println("  getAnnotations:         " + sendEmail.getAnnotations().length);
        System.out.println("  getDeclaredAnnotations: " + sendEmail.getDeclaredAnnotations().length);
        // Same here since our annotations don't use @Inherited
    }

    // =========================================================
    // Demo 3: Parameter Annotations (@RequestBody, @RequestParam)
    // =========================================================
    static void demo3_parameterAnnotations() throws Exception {
        separator("Demo 3: Parameter Annotations");

        Method m = ReportService.class.getDeclaredMethod(
            "writeTransactionalData", String.class, int.class
        );

        // getParameterAnnotations() → Annotation[][] (array per parameter)
        Annotation[][] paramAnnotations = m.getParameterAnnotations();
        Parameter[]    parameters       = m.getParameters();

        System.out.println("Method: " + m.getName());
        for (int i = 0; i < parameters.length; i++) {
            Parameter  param  = parameters[i];
            Annotation[] anns = paramAnnotations[i];

            System.out.printf("  param[%d]: %s %s%n",
                i, param.getType().getSimpleName(), param.getName());

            for (Annotation ann : anns) {
                if (ann instanceof RequestBody rb) {
                    System.out.println("    → @RequestBody (bind from HTTP request body)");
                } else if (ann instanceof RequestParam rp) {
                    System.out.println("    → @RequestParam name='" + rp.name()
                        + "' required=" + rp.required());
                }
            }
        }
    }

    // =========================================================
    // Demo 4: Type Erasure — getGenericReturnType()
    // =========================================================
    /**
     * TYPE ERASURE: At runtime, List<String> becomes raw List.
     * getReturnType()        → erased type (just "List")
     * getGenericReturnType() → preserves type arguments ("List<String>")
     *
     * This works because generic type info IS stored in bytecode
     * for method signatures, field declarations, and class headers.
     * It is ERASED for local variable types and runtime instances.
     *
     * Example:
     *   new ArrayList<String>() → at runtime, just ArrayList (erased)
     *   List<String> field;     → in bytecode, "List<String>" preserved
     */
    static void demo4_typeErasure() throws Exception {
        separator("Demo 4: Type Erasure & getGenericReturnType()");

        Class<?> clazz = DataRepository.class;

        System.out.printf("%-30s | %-20s | %s%n", "Method", "getReturnType()", "getGenericReturnType()");
        System.out.println("-".repeat(80));

        for (Method m : clazz.getDeclaredMethods()) {
            System.out.printf("%-30s | %-20s | %s%n",
                m.getName(),
                m.getReturnType().getSimpleName(),     // erased
                m.getGenericReturnType()               // full generic signature
            );
        }

        System.out.println("\n─── Drilling into ParameterizedType ────────────────────");
        Method findScores = clazz.getDeclaredMethod("findScoresByStudent");
        Type genericReturn = findScores.getGenericReturnType();

        System.out.println("Method: findScoresByStudent()");
        System.out.println("  genericReturnType class: " + genericReturn.getClass().getSimpleName());

        if (genericReturn instanceof ParameterizedType pt) {
            System.out.println("  Raw type:   " + pt.getRawType());        // java.util.Map
            System.out.println("  Type args:  " + Arrays.toString(pt.getActualTypeArguments()));

            // Drill into Map's value type → List<Integer>
            Type valueType = pt.getActualTypeArguments()[1];
            if (valueType instanceof ParameterizedType nestedPt) {
                System.out.println("  Value is:   " + nestedPt.getRawType());
                System.out.println("  Value args: " + Arrays.toString(nestedPt.getActualTypeArguments()));
            }
        }

        System.out.println("\n─── Optional<String> ────────────────────────────────────");
        Method findFirst = clazz.getDeclaredMethod("findFirstName");
        if (findFirst.getGenericReturnType() instanceof ParameterizedType pt) {
            System.out.println("  Optional type arg: " + pt.getActualTypeArguments()[0]);
        }
    }

    // =========================================================
    // Demo 5: Generic Fields
    // =========================================================
    static void demo5_genericFields() throws Exception {
        separator("Demo 5: Generic Fields — getGenericType()");

        Class<?> clazz = DataRepository.class;

        System.out.printf("%-20s | %-15s | %s%n", "Field", "getType()", "getGenericType()");
        System.out.println("-".repeat(70));
        for (Field f : clazz.getDeclaredFields()) {
            System.out.printf("%-20s | %-15s | %s%n",
                f.getName(),
                f.getType().getSimpleName(),   // raw
                f.getGenericType()             // with type args
            );
        }

        // Extract the key type of Map<String, List<Double>>
        System.out.println("\n─── Extracting Map<String, List<Double>> type args ─────");
        Field priceHistory = clazz.getDeclaredField("priceHistory");
        Type genericType   = priceHistory.getGenericType();

        if (genericType instanceof ParameterizedType pt) {
            Type keyType   = pt.getActualTypeArguments()[0];  // String
            Type valueType = pt.getActualTypeArguments()[1];  // List<Double>
            System.out.println("  Key type:   " + keyType);
            System.out.println("  Value type: " + valueType);
            if (valueType instanceof ParameterizedType valuePt) {
                System.out.println("  List elem:  " + valuePt.getActualTypeArguments()[0]);
            }
        }
    }

    // =========================================================
    // Demo 6: Generic Parameters — getGenericParameterTypes()
    // =========================================================
    static void demo6_genericParameters() throws Exception {
        separator("Demo 6: Generic Parameters — getGenericParameterTypes()");

        Class<?> clazz = DataRepository.class;
        Method   process = clazz.getDeclaredMethod("process", Map.class, List.class);

        System.out.println("Method: process(Map<String, Integer>, List<Double>)");
        Type[] genericParams = process.getGenericParameterTypes();

        for (int i = 0; i < genericParams.length; i++) {
            System.out.println("  param[" + i + "]: " + genericParams[i]);
            if (genericParams[i] instanceof ParameterizedType pt) {
                System.out.println("    raw:  " + pt.getRawType());
                System.out.println("    args: " + Arrays.toString(pt.getActualTypeArguments()));
            }
        }

        // getGenericExceptionTypes() — same idea for throws clause
        System.out.println("\n─── TypeVariable (unresolved generic T) ─────────────────");
        // If a method has: public <T> List<T> findAll() ...
        // The type arg would be a TypeVariable, not a Class
        System.out.println("  TypeVariable: T in List<T> → instanceof TypeVariable");
        System.out.println("  ParameterizedType: List<String> → instanceof ParameterizedType");
        System.out.println("  GenericArrayType: T[] → instanceof GenericArrayType");
        System.out.println("  WildcardType: ? extends Number → instanceof WildcardType");
    }

    // =========================================================
    // Demo 7: Mini Framework — ties annotation + generics together
    // =========================================================
    /**
     * A tiny "scheduled task runner" that:
     * 1. Scans a class for @ExecuteMe methods.
     * 2. Reads the annotation attributes (order, description).
     * 3. Executes them in order.
     * 4. If the method returns a generic type, extracts and logs the type info.
     *
     * This mirrors how Spring processes @Scheduled, @EventListener, etc.
     */
    static void demo7_miniFramework() throws Exception {
        separator("Demo 7: Mini Framework — Annotation Scanner + Executor");

        ReportService service = new ReportService();
        System.out.println("Scanning " + service.getClass().getSimpleName() + " for @ExecuteMe...\n");

        List<Method> candidates = Arrays.stream(service.getClass().getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(ExecuteMe.class))
            .sorted(Comparator.comparingInt(m -> m.getAnnotation(ExecuteMe.class).order()))
            .toList();

        System.out.printf("  Found %d @ExecuteMe methods:%n", candidates.size());
        for (Method m : candidates) {
            ExecuteMe ann = m.getAnnotation(ExecuteMe.class);
            System.out.printf("  [%d] %-25s → %s%n", ann.order(), m.getName(), ann.description());

            // Also check if it's @Transactional
            if (m.isAnnotationPresent(Transactional.class)) {
                Transactional tx = m.getAnnotation(Transactional.class);
                System.out.println("      ↳ @Transactional(readOnly=" + tx.readOnly()
                    + ", timeout=" + tx.timeoutSeconds() + "s) — wrapping in transaction");
            }
        }

        System.out.println("\nExecuting in order:");
        for (Method m : candidates) {
            System.out.print("  → " + m.getName() + "() ");
            Object result = m.invoke(service);

            // If the method returns a generic type, show it
            Type genericReturn = m.getGenericReturnType();
            if (genericReturn instanceof ParameterizedType pt) {
                System.out.println("     return type args: " + Arrays.toString(pt.getActualTypeArguments()));
            } else if (!m.getReturnType().equals(void.class)) {
                System.out.println("     returned: " + result);
            }
        }
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}

/*
 * ─────────────────────────────────────────────────────────────
 *  TYPE ERASURE QUICK REFERENCE
 * ─────────────────────────────────────────────────────────────
 *
 *  What IS erased at runtime:
 *    new ArrayList<String>()   → just ArrayList at runtime
 *    T in a generic method     → Object at runtime
 *    instanceof check on <T>   → won't compile (can't do: obj instanceof List<String>)
 *
 *  What is NOT erased (preserved in bytecode):
 *    Method signature:  List<String> findNames()  → readable via getGenericReturnType()
 *    Field declaration: Map<String, Integer> map  → readable via getGenericType()
 *    Constructor param: Ctor(List<String> items)  → readable via getGenericParameterTypes()
 *    Class header:      class Foo extends Bar<String> → readable via getGenericSuperclass()
 *
 *  Key types in java.lang.reflect:
 *    ParameterizedType   → List<String>, Map<K,V>        → getRawType(), getActualTypeArguments()
 *    TypeVariable        → T, E, K                       → getName(), getBounds()
 *    GenericArrayType    → T[], List<String>[]            → getGenericComponentType()
 *    WildcardType        → ? extends Number, ? super T   → getUpperBounds(), getLowerBounds()
 *
 *  ANNOTATION KEY POINTS:
 *    @Retention(RUNTIME)         required — else invisible to Reflection
 *    isAnnotationPresent(X.class) → boolean check
 *    getAnnotation(X.class)       → X instance with attributes, or null
 *    getDeclaredAnnotations()     → only on this element
 *    getAnnotations()             → includes @Inherited from superclass
 *    getParameterAnnotations()    → Annotation[][] for method parameters
 */
