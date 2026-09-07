# Chapter 1: Spring IoC Container & Bean Lifecycle

The **Inversion of Control (IoC)** container is the core engine of the Spring Framework, responsible for instantiating, configuring, assembling, and managing the lifecycle of application objects (Spring Beans).

---

## 1. `ApplicationContext` vs `BeanFactory`

Spring provides two primary container abstractions:

```
                  [ BeanFactory ] (Basic IoC, Lazy Loading)
                         ^
                         | Extends
             [ ApplicationContext ] (Enterprise Features)
            /          |           \
   AnnotationConfig   WebConfig    XmlConfig
```

* **`BeanFactory` (`org.springframework.beans.factory`):** Low-level container interface. Beans are instantiated on-demand (**Lazy Loading** when `getBean()` is invoked). Used in resource-constrained environments.
* **`ApplicationContext` (`org.springframework.context`):** High-level enterprise container extending `BeanFactory`. Instantiates all singleton beans eagerly at application startup (**Eager Loading**), failing fast on configuration errors. Supports AOP integration, i18n message source, and Event Publication (`ApplicationEventPublisher`).

---

## 2. Complete Spring Bean Lifecycle Pipeline

Understanding the exact sequence of bean instantiation, initialization, and destruction is vital for low-level Spring customization.

```
[ 1. Bean Instantiation ] ---> Constructor invoked via reflection
       |
       v
[ 2. Populate Properties ] ---> Dependency Injection (@Autowired / Setters)
       |
       v
[ 3. Aware Callbacks ] ---> BeanNameAware -> BeanFactoryAware -> ApplicationContextAware
       |
       v
[ 4. BeanPostProcessor (Before Init) ] ---> postProcessBeforeInitialization()
       |
       v
[ 5. Initialization Callbacks ] ---> 1. @PostConstruct
       |                            2. InitializingBean.afterPropertiesSet()
       |                            3. Custom initMethod (@Bean)
       v
[ 6. BeanPostProcessor (After Init) ]  ---> postProcessAfterInitialization()
       |                                     (AOP Dynamic Proxies created HERE!)
       v
[ 7. Bean Ready for Use ]
       |
       v (Application Shutdown)
[ 8. Destruction Callbacks ]  ---> 1. @PreDestroy
                                   2. DisposableBean.destroy()
                                   3. Custom destroyMethod
```

---

## 3. The 3-Level Cache: Resolving Circular Dependencies

A **Circular Dependency** occurs when Bean A depends on Bean B, and Bean B depends on Bean A ($A \to B \to A$). Spring resolves setter-based circular dependencies automatically using a **3-Level Cache** in `DefaultSingletonBeanRegistry`.

```
DefaultSingletonBeanRegistry
├── Cache 1: singletonObjects        (Map<String, Object>)           -> Fully initialized Beans
├── Cache 2: earlySingletonObjects   (Map<String, Object>)           -> Early raw / proxy references
└── Cache 3: singletonFactories      (Map<String, ObjectFactory<?>>) -> Object factories producing early references
```

### 3.1 Step-by-Step Circular Dependency Resolution ($A \leftrightarrow B$)

```
Thread requests Bean A
  │
  ├── 1. Instantiates raw object A (constructor execution).
  ├── 2. Exposes ObjectFactory for A in Cache 3 (singletonFactories).
  ├── 3. Populates properties of A -> Requires Bean B.
  │
  v
Thread requests Bean B
  │
  ├── 4. Instantiates raw object B.
  ├── 5. Exposes ObjectFactory for B in Cache 3.
  ├── 6. Populates properties of B -> Requires Bean A.
  │
  v
Lookup Bean A:
  ├── Checks Cache 1 (Miss - A not fully initialized).
  ├── Checks Cache 2 (Miss).
  ├── Checks Cache 3 (Hit!).
  ├── Invokes ObjectFactory.getObject() for A -> Returns early reference to A.
  ├── Promotes early reference of A to Cache 2 (earlySingletonObjects); removes from Cache 3.
  ├── Injects early reference of A into Bean B.
  │
  v
Bean B completes initialization:
  ├── B passes through BeanPostProcessors (AOP proxying).
  ├── B moves to Cache 1 (singletonObjects).
  │
  v
Thread returns to Bean A:
  ├── Injects fully initialized Bean B into A.
  ├── A passes through BeanPostProcessors.
  └── A moves to Cache 1 (singletonObjects).
```

### 3.2 Constructor-Based Circular Dependency Limitation
* **Why it fails:** The 3-level cache relies on instantiating the raw Java object **before** injecting dependencies. Constructor injection requires dependencies to be resolved *during* instantiation.
* **Resolution:** Change one dependency to setter injection, or annotate constructor parameter with `@Lazy` (injects a dynamic proxy instead of the real bean).

---

## 4. `BeanFactoryPostProcessor` vs `BeanPostProcessor`

| Feature | `BeanFactoryPostProcessor` | `BeanPostProcessor` |
| :--- | :--- | :--- |
| **Execution Point** | Runs **before any bean instantiation** occurs. | Runs **during bean initialization** (before and after init methods). |
| **Target Object** | Operates on `BeanDefinition` metadata. | Operates on actual **instantiated bean objects**. |
| **Common Uses** | Resolving property placeholders (`${db.url}`), modifying bean definitions dynamically. | Wrapping beans with AOP Dynamic Proxies, processing annotations (`@Autowired`). |
| **Example** | `PropertySourcesPlaceholderConfigurer`, `CustomScopeConfigurer` | `AutowiredAnnotationBeanPostProcessor`, `AnnotationAwareAspectJAutoProxyCreator` |

---

## 5. Staff Engineer Bean Lifecycle Checklist
1. **Never Call Heavy I/O in Constructors:** Constructors should only initialize internal fields. Defer network, database, or heavy file operations to `@PostConstruct` methods.
2. **Avoid Constructor Circular Dependencies:** Prefer constructor injection for immutability, but if circular references occur, resolve them using `@Lazy`.
3. **Beware AOP Proxy Creation Timing:** Remember that AOP dynamic proxies are created in `BeanPostProcessor.postProcessAfterInitialization()`. Invoking a proxyable method inside `@PostConstruct` operates on the *raw target instance*, bypassing AOP interceptors!
