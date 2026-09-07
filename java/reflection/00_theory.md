# Java Reflection — Complete Theory & API Guide
> **Study goal:** Understand how Reflection works at the JVM level, when to use it,
> and how it powers frameworks like Spring, Hibernate, JUnit, and Jackson.

---

## 1. What Is Reflection?

**Reflection** is the ability of a program to **inspect and modify itself at runtime** —
examining classes, methods, fields, constructors, and annotations without knowing them at compile time.

```java
// Without Reflection — compile-time knowledge required
MyClass obj = new MyClass();
obj.doSomething();

// With Reflection — type known only at runtime (e.g., from config, classpath scan)
Class<?> clazz = Class.forName("com.example.MyClass");
Object obj = clazz.getDeclaredConstructor().newInstance();
clazz.getMethod("doSomething").invoke(obj);
```

---

## 2. The JVM Class Loading Model

Before Reflection makes sense, understand how classes reach the JVM:

```
Source (.java)
    ↓ javac
Bytecode (.class)
    ↓ ClassLoader (Bootstrap → Extension → Application)
Method Area (JVM memory)
    ↓ Reflection reads FROM here at runtime
java.lang.Class object  ← the entry point for all Reflection
```

### Key Facts
- Every loaded class has **exactly one** `Class<T>` object in the JVM.
- `Class<T>` is the metadata descriptor: fields, methods, constructors, annotations, superclass.
- Reflection reads and manipulates this metadata.

---

## 3. Getting a Class Object — Three Ways

```java
// Way 1: .class literal (compile-time, no exception)
Class<String> c1 = String.class;

// Way 2: getClass() on an instance (runtime type)
String s = "hello";
Class<?> c2 = s.getClass();

// Way 3: Class.forName() (fully-qualified name, throws ClassNotFoundException)
Class<?> c3 = Class.forName("java.lang.String");  // loads class if not loaded yet
```

| Method | When to use | Type safe? | Loads class? |
|---|---|---|---|
| `Type.class` | Known at compile time | ✓ | No |
| `obj.getClass()` | Have an instance | Partial | No |
| `Class.forName(name)` | Name from config/string | ✗ | Yes |

---

## 4. Inspecting a Class

```java
Class<?> c = Employee.class;

// ─── Class Identity ────────────────────────────────────────
c.getName();               // "com.example.Employee" (fully qualified)
c.getSimpleName();         // "Employee"
c.getPackageName();        // "com.example"
c.getCanonicalName();      // "com.example.Employee" (differs for arrays/inner classes)

// ─── Type Checks ───────────────────────────────────────────
c.isInterface();           // false
c.isEnum();                // false
c.isAnnotation();          // false
c.isArray();               // false
c.isPrimitive();           // false
c.isRecord();              // false (Java 16+)

// ─── Inheritance ───────────────────────────────────────────
c.getSuperclass();         // Class object of direct superclass
c.getInterfaces();         // Class[] of directly implemented interfaces
c.isAssignableFrom(Sub.class);  // true if Sub is a subclass/implementation

// ─── Modifiers ─────────────────────────────────────────────
int mods = c.getModifiers();
Modifier.isPublic(mods);   // true/false
Modifier.isAbstract(mods);
Modifier.isFinal(mods);
```

---

## 5. Fields — Reading & Writing

```java
class Employee {
    public    String  name;
    private   int     salary;
    protected String  department;
}

Class<?> c = Employee.class;

// ─── Getting Fields ─────────────────────────────────────────
c.getFields();             // public fields ONLY (including inherited)
c.getDeclaredFields();     // ALL fields declared in THIS class (any access, no inherited)

Field salaryField = c.getDeclaredField("salary");   // by exact name

// ─── Reading Field Metadata ──────────────────────────────────
salaryField.getName();                      // "salary"
salaryField.getType();                      // int.class
salaryField.getGenericType();               // for List<String>: ParameterizedType
Modifier.isPrivate(salaryField.getModifiers()); // true

// ─── Reading Field Value ─────────────────────────────────────
Employee emp = new Employee();
emp.name = "Alice";
emp.salary = 90000;         // private — can't read directly

Field nameField = c.getField("name");
Object value = nameField.get(emp);          // "Alice"

// ─── Bypassing private access ────────────────────────────────
salaryField.setAccessible(true);            // ★★★ The key to private access
int salary = (int) salaryField.get(emp);   // 90000

// ─── Writing Field Value ─────────────────────────────────────
salaryField.set(emp, 120000);               // emp.salary is now 120000
salaryField.setAccessible(false);           // restore (good practice)
```

### `setAccessible(true)` — What It Does
- Bypasses Java access control checks (private/protected).
- Since Java 9, modules restrict this — `opens` must be declared in `module-info.java`.
- Performance: calling `setAccessible(true)` once is much cheaper than per-access.
- Frameworks like Spring, Hibernate, and Jackson use this heavily.

---

## 6. Methods — Discovery & Invocation

```java
class Calculator {
    public  int add(int a, int b)           { return a + b; }
    private int secret(String key, int val) { return key.hashCode() + val; }
    public  static String info()            { return "Calculator v1"; }
}

Class<?> c = Calculator.class;

// ─── Getting Methods ────────────────────────────────────────
c.getMethods();            // public methods + inherited (Object's toString, etc.)
c.getDeclaredMethods();    // ALL methods declared in THIS class only

// Get a specific method by name + parameter types (overload-safe)
Method addMethod    = c.getMethod("add", int.class, int.class);
Method secretMethod = c.getDeclaredMethod("secret", String.class, int.class);

// ─── Method Metadata ────────────────────────────────────────
addMethod.getName();             // "add"
addMethod.getReturnType();       // int.class
addMethod.getParameterTypes();   // [int.class, int.class]
addMethod.getParameterCount();   // 2
addMethod.getExceptionTypes();   // declared checked exceptions
Modifier.isStatic(addMethod.getModifiers()); // false

// ─── Invoking Methods ───────────────────────────────────────
Calculator calc = new Calculator();

// Instance method
Object result = addMethod.invoke(calc, 3, 4);   // returns 7 (autoboxed)
int sum = (int) result;

// Private method
secretMethod.setAccessible(true);
Object secret = secretMethod.invoke(calc, "key", 42);

// Static method — pass null as instance
Method infoMethod = c.getMethod("info");
String info = (String) infoMethod.invoke(null);  // "Calculator v1"
```

### Checked Exceptions in Reflection
```java
try {
    method.invoke(obj, args);
} catch (InvocationTargetException e) {
    // The method itself threw an exception — unwrap it
    Throwable cause = e.getCause();     // the REAL exception
    cause.printStackTrace();
} catch (IllegalAccessException e) {
    // Forgot setAccessible(true) on a private method
} catch (IllegalArgumentException e) {
    // Wrong number or type of arguments
}
```

---

## 7. Constructors — Dynamic Object Creation

```java
class Server {
    private final int port;
    private final String host;

    public  Server()                        { this.port = 8080; this.host = "localhost"; }
    public  Server(int port)                { this.port = port; this.host = "localhost"; }
    private Server(String host, int port)   { this.port = port; this.host = host; }
}

Class<?> c = Server.class;

// ─── Getting Constructors ────────────────────────────────────
c.getConstructors();           // public constructors only
c.getDeclaredConstructors();   // ALL constructors

// Get a specific constructor by parameter types
Constructor<?> noArg    = c.getConstructor();                        // Server()
Constructor<?> portCtor = c.getConstructor(int.class);               // Server(int)
Constructor<?> privCtor = c.getDeclaredConstructor(String.class, int.class); // private

// ─── Creating Instances ──────────────────────────────────────
Server s1 = (Server) noArg.newInstance();        // Server() — calls default ctor
Server s2 = (Server) portCtor.newInstance(9090); // Server(9090)

privCtor.setAccessible(true);
Server s3 = (Server) privCtor.newInstance("api.example.com", 443);

// ─── Shorthand (no-arg only) ────────────────────────────────
Object instance = c.getDeclaredConstructor().newInstance();
```

---

## 8. Annotations — Reading at Runtime

```java
// Custom annotation (must have RUNTIME retention to be readable via Reflection)
@Retention(RetentionPolicy.RUNTIME)  // ← REQUIRED for reflection to see it
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@interface Validate {
    String pattern() default ".*";
    boolean required() default true;
}

@Validate(pattern = "\\d{10}", required = true)
class PhoneNumber {
    @Validate(required = false)
    String extension;

    @Validate(pattern = "[A-Z]+")
    void process() {}
}

// ─── Reading Class Annotations ───────────────────────────────
Class<?> c = PhoneNumber.class;
Validate classAnnotation = c.getAnnotation(Validate.class);  // null if not present
if (classAnnotation != null) {
    System.out.println(classAnnotation.pattern());   // "\\d{10}"
    System.out.println(classAnnotation.required());  // true
}

c.isAnnotationPresent(Validate.class);   // true
c.getAnnotations();                      // all annotations (including inherited)
c.getDeclaredAnnotations();              // only directly present

// ─── Reading Field Annotations ───────────────────────────────
Field extField = c.getDeclaredField("extension");
Validate fieldAnn = extField.getAnnotation(Validate.class);
System.out.println(fieldAnn.required()); // false

// ─── Reading Method Annotations ──────────────────────────────
Method processMethod = c.getDeclaredMethod("process");
Validate methodAnn = processMethod.getAnnotation(Validate.class);
System.out.println(methodAnn.pattern()); // "[A-Z]+"
```

### Retention Policies
| Policy | Visible in | Use case |
|---|---|---|
| `SOURCE` | Only in source code | `@Override`, `@SuppressWarnings` |
| `CLASS` | In .class file, NOT runtime | Bytecode analysis tools |
| `RUNTIME` | Available via Reflection | **Spring, Hibernate, JUnit, Jackson** |

---

## 9. Generics & Reflection (Type Erasure)

Java erases generic types at runtime. `List<String>` becomes `List` in bytecode.
But type info is preserved in **declarations** (fields, method signatures) via `ParameterizedType`.

```java
class DataStore {
    private List<String> names;
    private Map<String, List<Integer>> index;
}

Field namesField = DataStore.class.getDeclaredField("names");

// getType() loses generic info:
namesField.getType();                    // interface java.util.List (raw)

// getGenericType() preserves it:
Type genericType = namesField.getGenericType();  // java.util.List<java.lang.String>
if (genericType instanceof ParameterizedType pt) {
    Type[] typeArgs = pt.getActualTypeArguments();
    System.out.println(typeArgs[0]);     // class java.lang.String ✓
}

// Nested generics (Map<String, List<Integer>>)
Field indexField = DataStore.class.getDeclaredField("index");
ParameterizedType mapType = (ParameterizedType) indexField.getGenericType();
ParameterizedType listType = (ParameterizedType) mapType.getActualTypeArguments()[1];
System.out.println(listType.getActualTypeArguments()[0]); // class java.lang.Integer ✓
```

---

## 10. Performance & Best Practices

### Reflection Overhead
| Operation | Cost | Why |
|---|---|---|
| `Class.forName()` | High | ClassLoader invocation, JVM metadata lookup |
| `getDeclaredField/Method` | Medium | String matching on class metadata |
| `field.get(obj)` / `method.invoke()` | 2–10× slower | No JIT optimization, safety checks |
| `setAccessible(true)` | One-time cost | Security manager check |

### Best Practices
```
1. Cache reflection objects (Field, Method, Constructor) — never look them up per-call.
   WRONG: field = cls.getDeclaredField("x"); → inside a loop
   RIGHT: static final Field X_FIELD = ...; → class-level cache

2. Call setAccessible(true) once and store the reference.

3. Prefer MethodHandles (Java 7+) over Method.invoke() for repeated hot calls.
   MethodHandle is JIT-optimized; Method.invoke() is not.

4. Use Reflection only for framework-level code (DI containers, serializers, test tools).
   Never use it in business logic — it breaks encapsulation and IDE support.

5. For Java 9+ modules, add 'opens' in module-info.java:
   opens com.example.model to com.fasterxml.jackson.databind;
```

---

## 11. Where Frameworks Use Reflection

| Framework | Reflection Usage |
|---|---|
| **Spring DI** | Scan `@Component` classes, call constructors, inject `@Autowired` fields |
| **Spring MVC** | Call `@RequestMapping` methods, bind `@RequestParam` by parameter name |
| **Hibernate** | Read `@Column`/`@Entity` annotations, get/set fields for dirty checking |
| **Jackson** | Serialize/deserialize: read fields via `getDeclaredFields()`, invoke getters |
| **JUnit** | Find `@Test` methods, invoke them, read `@BeforeEach` / `@AfterEach` |
| **Mockito** | Create proxy subclasses, intercept method calls |

---

*Next: → [01_basics.java](01_basics.java) — Class loading, fields, methods*
*→ [02_annotations.java](02_annotations.java) — Custom annotations + runtime processing*
*→ [03_advanced.java](03_advanced.java) — Generic types, MethodHandles, mini DI container*
