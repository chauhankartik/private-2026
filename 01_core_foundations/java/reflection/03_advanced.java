/**
 * ============================================================
 *  JAVA REFLECTION — ADVANCED
 *  Generics + type erasure, mini DI container, object mapper,
 *  performance caching, and proxy pattern.
 * ============================================================
 *
 * Run: javac 03_advanced.java && java AdvancedReflection
 */
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

// ─────────────────────────────────────────────────────────────
// PART 1: GENERICS & TYPE ERASURE
// ─────────────────────────────────────────────────────────────

class TypeErasureDemo {

    // Generic field whose type argument IS preserved in bytecode
    private List<String>               stringList;
    private Map<String, List<Integer>> nestedMap;
    private Optional<Double>           optionalDouble;

    static void run() throws Exception {
        System.out.println("▶ Type Erasure & ParameterizedType");
        Class<?> c = TypeErasureDemo.class;

        for (Field field : c.getDeclaredFields()) {
            System.out.println("\nField: " + field.getName());
            System.out.println("  getType():        " + field.getType().getSimpleName()); // raw
            System.out.println("  getGenericType(): " + field.getGenericType());          // full

            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType pt) {
                System.out.println("  Raw type:         " + pt.getRawType());
                System.out.println("  Type args:");
                for (Type arg : pt.getActualTypeArguments()) {
                    System.out.println("    " + arg);
                    // Recurse into nested generics
                    if (arg instanceof ParameterizedType nested) {
                        System.out.println("      (nested) " + nested);
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PART 2: MINI DI CONTAINER (Spring ApplicationContext sim)
// ─────────────────────────────────────────────────────────────

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
@interface Service {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
@interface Inject {}

// --- Service layer classes ---
@Service class EmailService {
    public void send(String to, String msg) {
        System.out.println("  [Email] Sent to " + to + ": " + msg);
    }
}

@Service class SmsService {
    public void send(String phone, String msg) {
        System.out.println("  [SMS] Sent to " + phone + ": " + msg);
    }
}

@Service class NotificationManager {
    @Inject EmailService emailService;
    @Inject SmsService   smsService;

    public void notify(String user) {
        emailService.send(user + "@example.com", "Welcome!");
        smsService.send("+91-9999999999", "Hello " + user);
    }
}

/**
 * Mini Dependency Injection Container.
 *
 * Algorithm:
 * 1. Scan registered classes for @Service.
 * 2. Instantiate each (no-arg ctor).
 * 3. For each instance, scan @Inject fields.
 * 4. Resolve the field's type from the registry and inject it.
 *
 * This is the core of what Spring's ApplicationContext does.
 */
class MiniDIContainer {

    private final Map<Class<?>, Object> beans = new HashMap<>();

    public void register(Class<?>... classes) throws Exception {
        // Pass 1: instantiate all @Service classes
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Service.class)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beans.put(clazz, instance);
                System.out.println("  [Container] Registered: " + clazz.getSimpleName());
            }
        }

        // Pass 2: inject @Inject fields
        for (Object bean : beans.values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = beans.get(field.getType());
                    if (dependency == null) {
                        throw new RuntimeException("No bean found for type: " + field.getType());
                    }
                    field.setAccessible(true);
                    field.set(bean, dependency);
                    System.out.println("  [Container] Injected " + field.getType().getSimpleName()
                        + " into " + bean.getClass().getSimpleName() + "." + field.getName());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
}

// ─────────────────────────────────────────────────────────────
// PART 3: MINI OBJECT MAPPER (Jackson-style serialization)
// ─────────────────────────────────────────────────────────────

class Product {
    public  String name;
    public  double price;
    private int    stock;       // private — but we'll access it
    public  boolean available;

    Product(String name, double price, int stock, boolean available) {
        this.name = name; this.price = price;
        this.stock = stock; this.available = available;
    }
}

/**
 * Converts any object to a JSON-like string by reading all fields via Reflection.
 * This simulates Jackson's ObjectMapper.writeValueAsString().
 */
class MiniObjectMapper {

    public static String toJson(Object obj) throws IllegalAccessException {
        if (obj == null) return "null";
        Class<?> clazz = obj.getClass();
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (!first) sb.append(", ");
            first = false;

            sb.append("\"").append(field.getName()).append("\": ");
            Object value = field.get(obj);

            if (value == null)               sb.append("null");
            else if (value instanceof String) sb.append("\"").append(value).append("\"");
            else if (value instanceof Number || value instanceof Boolean) sb.append(value);
            else                              sb.append("\"").append(value).append("\"");
        }
        return sb.append("}").toString();
    }

    /**
     * Populates an object from a Map (simulates JSON deserialization).
     * Matches map keys to field names; converts types as needed.
     */
    public static <T> T fromMap(Class<T> clazz, Map<String, Object> data) throws Exception {
        T instance = clazz.getDeclaredConstructor(
            String.class, double.class, int.class, boolean.class
        ).newInstance(
            data.get("name"), data.get("price"), data.get("stock"), data.get("available")
        );
        return instance;
    }
}

// ─────────────────────────────────────────────────────────────
// PART 4: PERFORMANCE CACHING PATTERN
// ─────────────────────────────────────────────────────────────

/**
 * Cache Field/Method objects to avoid repeated lookup overhead.
 * In real frameworks (Spring, Hibernate), reflection metadata is cached
 * at startup — never looked up per request.
 */
class ReflectionCache {

    // Cache: class → field name → Field (thread-safe)
    private static final Map<Class<?>, Map<String, Field>>  fieldCache  = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> methodCache = new ConcurrentHashMap<>();

    /** Get a cached Field — lookup happens only ONCE per class+name. */
    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Map<String, Field> fields = fieldCache.computeIfAbsent(clazz, c -> {
            Map<String, Field> map = new HashMap<>();
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true);
                map.put(f.getName(), f);
            }
            return map;
        });
        Field f = fields.get(name);
        if (f == null) throw new NoSuchFieldException(name);
        return f;
    }

    /** Get a cached Method. */
    public static Method getMethod(Class<?> clazz, String name,
                                   Class<?>... paramTypes) throws NoSuchMethodException {
        String key = name + Arrays.toString(paramTypes);
        Map<String, Method> methods = methodCache.computeIfAbsent(clazz, c -> new HashMap<>());
        return methods.computeIfAbsent(key, k -> {
            try {
                Method m = clazz.getDeclaredMethod(name, paramTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        });
    }

    static void run() throws Exception {
        System.out.println("▶ Reflection Cache Performance Test");
        Product p = new Product("Phone", 29999.0, 10, true);

        // Cold lookup (first time)
        long t1 = System.nanoTime();
        Field f1 = getField(Product.class, "name");
        System.out.println("  Cold lookup (name): " + f1.get(p) + " — "
            + (System.nanoTime() - t1) / 1000 + " µs");

        // Warm lookup (cached)
        long t2 = System.nanoTime();
        Field f2 = getField(Product.class, "name");
        System.out.println("  Warm lookup (name): " + f2.get(p) + " — "
            + (System.nanoTime() - t2) / 1000 + " µs (should be ~0 µs)");
    }
}

// ─────────────────────────────────────────────────────────────
// PART 5: DYNAMIC PROXY (Logging AOP Simulation)
// ─────────────────────────────────────────────────────────────

interface UserService {
    String findUser(int id);
    void   saveUser(String name);
}

class UserServiceImpl implements UserService {
    @Override public String findUser(int id) { return "User#" + id; }
    @Override public void   saveUser(String name) { System.out.println("  Saved: " + name); }
}

/**
 * Creates a JDK Dynamic Proxy that adds logging around every method call.
 * This is EXACTLY how Spring AOP (@Transactional, @Cacheable, @Async) works:
 *   1. Spring detects an annotation on a bean.
 *   2. It wraps the bean in a dynamic proxy.
 *   3. The proxy intercepts every method call and adds the cross-cutting concern.
 *
 * JDK Proxy requirement: the target must implement at least ONE interface.
 * For classes without interfaces, Spring uses CGLIB to generate a subclass.
 */
class LoggingProxy {

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<T> interfaceType) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            new Class<?>[]{ interfaceType },
            (proxy, method, args) -> {
                // BEFORE method
                System.out.println("[LOG] Calling: " + method.getName()
                    + "(" + Arrays.toString(args) + ")");
                long start = System.nanoTime();

                try {
                    // Delegate to the real implementation
                    Object result = method.invoke(target, args);

                    // AFTER method (success)
                    long elapsed = (System.nanoTime() - start) / 1_000;
                    System.out.println("[LOG] Completed: " + method.getName()
                        + " → " + result + " (" + elapsed + " µs)");
                    return result;

                } catch (InvocationTargetException e) {
                    // AFTER method (exception)
                    System.out.println("[LOG] Exception in: " + method.getName()
                        + " → " + e.getCause().getMessage());
                    throw e.getCause();
                }
            }
        );
    }
}

// ─────────────────────────────────────────────────────────────
// PART 6: Running All Demos
// ─────────────────────────────────────────────────────────────
public class AdvancedReflection {

    public static void main(String[] args) throws Exception {
        separator("Part 1: Generics & Type Erasure");
        TypeErasureDemo.run();

        separator("Part 2: Mini DI Container");
        MiniDIContainer container = new MiniDIContainer();
        container.register(EmailService.class, SmsService.class, NotificationManager.class);
        System.out.println("\n  Calling NotificationManager.notify(\"Alice\"):");
        container.getBean(NotificationManager.class).notify("Alice");

        separator("Part 3: Mini Object Mapper");
        Product p = new Product("Laptop", 89999.0, 5, true);
        String json = MiniObjectMapper.toJson(p);
        System.out.println("Serialized:   " + json);

        separator("Part 4: Reflection Cache");
        ReflectionCache.run();

        separator("Part 5: Dynamic Proxy (AOP Logging)");
        UserService real   = new UserServiceImpl();
        UserService logged = LoggingProxy.wrap(real, UserService.class);

        System.out.println("\n  Without proxy:");
        System.out.println("    " + real.findUser(42));

        System.out.println("\n  With logging proxy:");
        logged.findUser(42);
        logged.saveUser("Bob");

        System.out.println("\n  Is it a proxy? " + Proxy.isProxyClass(logged.getClass()));
        System.out.println("  InvocationHandler: "
            + Proxy.getInvocationHandler(logged).getClass().getSimpleName());
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
