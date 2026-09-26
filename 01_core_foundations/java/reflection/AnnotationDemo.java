
/**
 * ============================================================
 *  JAVA REFLECTION — ANNOTATIONS
 *  Custom annotations + runtime processing (validation, routing)
 * ============================================================
 *
 * Run: javac 02_annotations.java && java AnnotationDemo
 *
 * Shows exactly how Spring, JUnit, and Jackson use annotations
 * — by scanning classes at startup and reading them via Reflection.
 * 
 * 
 * 
 *  https://www.youtube.com/watch?v=GDUi0RwWp0U
 */
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

// ─────────────────────────────────────────────────────────────
// PART 1: CUSTOM ANNOTATIONS
// ─────────────────────────────────────────────────────────────

/**
 * @NotNull — marks a field/parameter as must-not-be-null.
 *          RUNTIME retention = readable via Reflection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@interface NotNull {
    String message() default "Field must not be null";
}

/**
 * @Pattern — validates field value against a regex.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Regex {
    String pattern();

    String message() default "Field does not match pattern";
}

/**
 * @Range — validates numeric field is within [min, max].
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Range {
    int min() default 0;

    int max() default Integer.MAX_VALUE;

    String message() default "Field out of range";
}

/**
 * @Route — maps a method to an HTTP path + verb.
 *        Simulates @RequestMapping from Spring MVC.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Route {
    String path();

    String method() default "GET";
}

/**
 * @Component — marks a class for "dependency injection" scanning.
 *            Simulates @Component / @Service from Spring.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Component {
    String name() default "";
}

// ─────────────────────────────────────────────────────────────
// PART 2: DOMAIN MODEL WITH ANNOTATIONS
// ─────────────────────────────────────────────────────────────

class UserRequest {
    @NotNull
    @Regex(pattern = "^[a-zA-Z0-9_]{3,20}$", message = "Username must be 3-20 alphanumeric chars")
    String username;

    @NotNull
    @Regex(pattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    String email;

    @Range(min = 18, max = 120, message = "Age must be between 18 and 120")
    int age;

    String optionalNickname; // no annotations — will be skipped by validator

    UserRequest(String username, String email, int age, String nickname) {
        this.username = username;
        this.email = email;
        this.age = age;
        this.optionalNickname = nickname;
    }
}

// ─────────────────────────────────────────────────────────────
// PART 3: ANNOTATION PROCESSOR — Validator Engine
// ─────────────────────────────────────────────────────────────
/**
 * A simple validation engine that:
 * 1. Scans all fields of any object via Reflection.
 * 2. Reads annotations on each field.
 * 3. Applies the corresponding validation rule.
 *
 * This is EXACTLY how Hibernate Validator (@Valid) works.
 */
class AnnotationValidator {

    public static class ValidationResult {
        final List<String> errors = new ArrayList<>();

        boolean isValid() {
            return errors.isEmpty();
        }
    }

    public static ValidationResult validate(Object obj) throws IllegalAccessException {
        ValidationResult result = new ValidationResult();
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);
            String fieldName = field.getName();

            // Check @NotNull
            if (field.isAnnotationPresent(NotNull.class)) {
                NotNull ann = field.getAnnotation(NotNull.class);
                if (value == null) {
                    result.errors.add("[" + fieldName + "] " + ann.message());
                    continue; // no point checking further if null
                }
            }

            // Check @Regex (only on non-null Strings)
            if (field.isAnnotationPresent(Regex.class) && value instanceof String str) {
                Regex ann = field.getAnnotation(Regex.class);
                if (!Pattern.matches(ann.pattern(), str)) {
                    result.errors.add("[" + fieldName + "] " + ann.message()
                            + " (value='" + str + "', pattern=" + ann.pattern() + ")");
                }
            }

            // Check @Range (only on int/Integer)
            if (field.isAnnotationPresent(Range.class)) {
                Range ann = field.getAnnotation(Range.class);
                int intValue = 0;
                if (value instanceof Integer i)
                    intValue = i;
                else if (field.getType() == int.class)
                    intValue = (int) value;
                else
                    continue;

                if (intValue < ann.min() || intValue > ann.max()) {
                    result.errors.add("[" + fieldName + "] " + ann.message()
                            + " (value=" + intValue + ", min=" + ann.min() + ", max=" + ann.max() + ")");
                }
            }
        }
        return result;
    }
}

// ─────────────────────────────────────────────────────────────
// PART 4: ANNOTATION PROCESSOR — Route Scanner (Spring MVC sim)
// ─────────────────────────────────────────────────────────────

@Component("userController")
class UserController {

    @Route(path = "/users", method = "GET")
    public String listUsers() {
        return "[Alice, Bob, Charlie]";
    }

    @Route(path = "/users/{id}", method = "GET")
    public String getUser(String id) {
        return "User#" + id;
    }

    @Route(path = "/users", method = "POST")
    public String createUser(String body) {
        return "Created: " + body;
    }

    @Route(path = "/users/{id}", method = "DELETE")
    public String deleteUser(String id) {
        return "Deleted User#" + id;
    }

    public String internalHelper() {
        return "not a route";
    } // no @Route
}

/**
 * Scans a class for @Route annotations and builds a routing table.
 * This simulates what Spring's DispatcherServlet does at startup.
 */
class RouteScanner {

    record RouteEntry(String path, String httpMethod, Method handler) {
    }

    public static List<RouteEntry> scan(Class<?> controllerClass) {
        List<RouteEntry> routes = new ArrayList<>();
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Route.class)) {
                Route route = method.getAnnotation(Route.class);
                routes.add(new RouteEntry(route.path(), route.method(), method));
            }
        }
        return routes;
    }
}

// ─────────────────────────────────────────────────────────────
// PART 5: COMPONENT SCANNER (Spring-style)
// ─────────────────────────────────────────────────────────────
/**
 * Scans a list of classes, finds those annotated with @Component,
 * instantiates them, and registers them by name.
 * This simulates Spring's component scan at startup.
 */
class SimpleComponentScanner {

    private final Map<String, Object> registry = new HashMap<>();

    public void scan(Class<?>... classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                Component ann = clazz.getAnnotation(Component.class);
                String beanName = ann.name().isEmpty()
                        ? clazz.getSimpleName().substring(0, 1).toLowerCase()
                                + clazz.getSimpleName().substring(1)
                        : ann.name();
                Object instance = clazz.getDeclaredConstructor().newInstance();
                registry.put(beanName, instance);
                System.out.println("  Registered bean: '" + beanName
                        + "' → " + clazz.getSimpleName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) registry.get(name);
    }
}

// ─────────────────────────────────────────────────────────────
// PART 6: Running All Demos
// ─────────────────────────────────────────────────────────────
public class AnnotationDemo {

    public static void main(String[] args) throws Exception {
        demo1_validation();
        demo2_routeScanning();
        demo3_componentScanner();
        demo4_readingAnnotationAttributes();
    }

    static void demo1_validation() throws Exception {
        separator("Demo 1: Annotation-Based Validation");

        // Valid request
        UserRequest valid = new UserRequest("alice_99", "alice@gmail.com", 25, "Ally");
        AnnotationValidator.ValidationResult r1 = AnnotationValidator.validate(valid);
        System.out.println("Valid request → " + (r1.isValid() ? "PASS ✓" : "FAIL: " + r1.errors));

        // Multiple violations
        UserRequest invalid = new UserRequest(null, "not-an-email", 15, null);
        AnnotationValidator.ValidationResult r2 = AnnotationValidator.validate(invalid);
        System.out.println("Invalid request → " + (r2.isValid() ? "PASS" : "FAIL ✗"));
        r2.errors.forEach(e -> System.out.println("  • " + e));
    }

    static void demo2_routeScanning() throws Exception {
        separator("Demo 2: Route Scanning (@Route → routing table)");

        List<RouteScanner.RouteEntry> routes = RouteScanner.scan(UserController.class);
        System.out.printf("%-8s %-25s %s%n", "METHOD", "PATH", "HANDLER");
        System.out.println("-".repeat(55));
        for (RouteScanner.RouteEntry route : routes) {
            System.out.printf("%-8s %-25s %s%n",
                    route.httpMethod(), route.path(), route.handler().getName());
        }
    }

    static void demo3_componentScanner() throws Exception {
        separator("Demo 3: Component Scanning (@Component → bean registry)");

        SimpleComponentScanner scanner = new SimpleComponentScanner();
        scanner.scan(UserController.class); // UserController is @Component("userController")

        UserController controller = scanner.getBean("userController");
        System.out.println("Retrieved bean and called listUsers(): "
                + controller.listUsers());
    }

    static void demo4_readingAnnotationAttributes() throws Exception {
        separator("Demo 4: Reading Annotation Attributes via Reflection");

        Class<?> clazz = UserRequest.class;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.getDeclaredAnnotations().length == 0)
                continue;
            System.out.println("Field: " + field.getName());

            if (field.isAnnotationPresent(NotNull.class)) {
                System.out.println("  @NotNull: message='"
                        + field.getAnnotation(NotNull.class).message() + "'");
            }
            if (field.isAnnotationPresent(Regex.class)) {
                Regex r = field.getAnnotation(Regex.class);
                System.out.println("  @Regex:   pattern='" + r.pattern()
                        + "' message='" + r.message() + "'");
            }
            if (field.isAnnotationPresent(Range.class)) {
                Range r = field.getAnnotation(Range.class);
                System.out.println("  @Range:   min=" + r.min()
                        + " max=" + r.max() + " message='" + r.message() + "'");
            }
        }
    }

    static void separator(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" " + title);
        System.out.println("══════════════════════════════════════════");
    }
}
