import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * ============================================================
 *  01. CLASSLOADER DELEGATION HIERARCHY & CLASS ISOLATION
 * ============================================================
 *
 *  Demonstrates:
 *   1. Programmatic inspection of ClassLoader parent chain.
 *   2. Why Bootstrap ClassLoader returns null in Java.
 *   3. Thread Context ClassLoader (TCCL) mechanics.
 *   4. Class isolation: Two ClassLoaders loading the same class
 *      create distinct Class objects.
 * ============================================================
 */
class ClassLoaderHierarchyDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. CLASSLOADER DELEGATION CHAIN ===");
        inspectClassLoaderChain(ClassLoaderHierarchyDemo.class);

        System.out.println("\n=== 2. JDK CORE CLASSES VS APP CLASSES ===");
        inspectBuiltInClasses();

        System.out.println("\n=== 3. THREAD CONTEXT CLASSLOADER (TCCL) ===");
        inspectContextClassLoader();

        System.out.println("\n=== 4. CLASS ISOLATION DEMONSTRATION ===");
        demonstrateClassIsolation();
    }

    /**
     * Prints the parent hierarchy of ClassLoaders from current class up to Bootstrap.
     */
    private static void inspectClassLoaderChain(Class<?> clazz) {
        System.out.println("Inspecting ClassLoader hierarchy for: " + clazz.getName());
        ClassLoader cl = clazz.getClassLoader();
        int level = 0;
        while (cl != null) {
            System.out.println("  Level " + level + ": " + cl.getClass().getName() + " [" + cl + "]");
            cl = cl.getParent();
            level++;
        }
        System.out.println("  Level " + level + ": Bootstrap ClassLoader (C++ Native / null)");
    }

    /**
     * Inspects ClassLoader of core JDK class (String) vs Application class.
     */
    private static void inspectBuiltInClasses() {
        // String.class is loaded by Bootstrap ClassLoader -> returns null
        ClassLoader stringCL = String.class.getClassLoader();
        System.out.println("String.class ClassLoader        : " + stringCL + " (null = Bootstrap ClassLoader)");

        // Application class is loaded by AppClassLoader / System ClassLoader
        ClassLoader appCL = ClassLoaderHierarchyDemo.class.getClassLoader();
        System.out.println("ClassLoaderHierarchyDemo ClassLoader: " + appCL.getClass().getName());

        // Platform / Extension ClassLoader
        ClassLoader systemCL = ClassLoader.getSystemClassLoader();
        System.out.println("System ClassLoader              : " + systemCL.getClass().getName());
        System.out.println("System ClassLoader Parent       : " + systemCL.getParent().getClass().getName());
    }

    /**
     * Demonstrates Thread Context ClassLoader (used by SPI frameworks like JDBC / JNDI).
     */
    private static void inspectContextClassLoader() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        System.out.println("Thread Context ClassLoader (TCCL): " + tccl.getClass().getName());

        // Test running in a child thread
        Thread t = new Thread(() -> {
            ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
            System.out.println("Child Thread Context ClassLoader : " + threadCL.getClass().getName());
        });
        t.start();
    }

    /**
     * Demonstrates that identical bytecode loaded by two distinct ClassLoader instances
     * produces two completely distinct Class objects in the JVM.
     */
    private static void demonstrateClassIsolation() throws Exception {
        byte[] classBytes = loadBytecode(ClassLoaderHierarchyDemo.class.getName());

        // Create two independent custom ClassLoaders
        MemoryClassLoader cl1 = new MemoryClassLoader("Loader-1", ClassLoaderHierarchyDemo.class.getName(), classBytes);
        MemoryClassLoader cl2 = new MemoryClassLoader("Loader-2", ClassLoaderHierarchyDemo.class.getName(), classBytes);

        Class<?> class1 = cl1.loadClass(ClassLoaderHierarchyDemo.class.getName());
        Class<?> class2 = cl2.loadClass(ClassLoaderHierarchyDemo.class.getName());

        System.out.println("Class 1 Loaded By : " + class1.getClassLoader());
        System.out.println("Class 2 Loaded By : " + class2.getClassLoader());
        System.out.println("Class1 == Class2  : " + (class1 == class2) + " (Expected: false due to ClassLoader isolation)");
    }

    /**
     * Helper memory ClassLoader that defines a class from byte array.
     */
    private static class MemoryClassLoader extends ClassLoader {
        private final String loaderName;
        private final String targetClassName;
        private final byte[] bytecode;

        public MemoryClassLoader(String loaderName, String targetClassName, byte[] bytecode) {
            super(ClassLoader.getSystemClassLoader());
            this.loaderName = loaderName;
            this.targetClassName = targetClassName;
            this.bytecode = bytecode;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals(targetClassName)) {
                return defineClass(name, bytecode, 0, bytecode.length);
            }
            return super.findClass(name);
        }

        @Override
        public String toString() {
            return "MemoryClassLoader{" + loaderName + "}";
        }
    }

    /**
     * Reads bytecode of a loaded class from classpath input stream.
     */
    private static byte[] loadBytecode(String className) throws Exception {
        String resourcePath = className.replace('.', '/') + ".class";
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (is == null) {
                throw new ClassNotFoundException("Could not find class resource: " + resourcePath);
            }
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }
}
