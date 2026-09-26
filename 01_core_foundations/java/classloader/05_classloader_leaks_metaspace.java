import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  05. METASPACE MEMORY LEAKS & CLASSLOADER UNLOADING
 * ============================================================
 *
 *  Demonstrates:
 *   1. How custom ClassLoader instances leak in memory when references persist.
 *   2. ThreadLocal + ClassLoader leak vulnerability (common in application servers).
 *   3. Verifying ClassLoader garbage collection with WeakReference.
 *   4. Diagnostic principles for debugging java.lang.OutOfMemoryError: Metaspace.
 * ============================================================
 */
class ClassLoaderLeaksMetaspaceDemo {

    // ThreadLocal variable holding a custom class instance
    private static final ThreadLocal<Object> LEAK_HOLDER = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        System.out.println("=== METASPACE & CLASSLOADER LEAK DEMO ===");

        System.out.println("\n--- 1. SUCCESSFUL CLASSLOADER GARBAGE COLLECTION ---");
        testSuccessfulClassLoaderGC();

        System.out.println("\n--- 2. CLASSLOADER MEMORY LEAK VIA THREADLOCAL ---");
        testThreadLocalClassLoaderLeak();
    }

    /**
     * Demonstrates that when zero references remain to a custom ClassLoader and its classes,
     * the ClassLoader IS successfully Garbage Collected.
     */
    private static void testSuccessfulClassLoaderGC() throws Exception {
        byte[] bytecode = loadBytecode(LeakCandidate.class.getName());

        // 1. Create a custom ClassLoader
        EphemeralClassLoader loader = new EphemeralClassLoader("EphemeralLoader", LeakCandidate.class.getName(), bytecode);

        // 2. Wrap in WeakReference to monitor GC
        WeakReference<ClassLoader> loaderRef = new WeakReference<>(loader);

        // 3. Load class and instantiate
        Class<?> clazz = loader.loadClass(LeakCandidate.class.getName());
        Object obj = clazz.getDeclaredConstructor().newInstance();
        System.out.println("Loaded Class: " + clazz.getName() + " via " + clazz.getClassLoader());

        // 4. Nullify all hard references
        loader = null;
        clazz = null;
        obj = null;

        // 5. Trigger System GC and verify GC of ClassLoader
        System.gc();
        Thread.sleep(200);

        System.out.println("ClassLoader GC status (loaderRef.get() == null): " + (loaderRef.get() == null) +
            " (Expected: true - ClassLoader was garbage collected)");
    }

    /**
     * Demonstrates how ThreadLocal holding a class instance prevents ClassLoader GC,
     * eventually leading to Metaspace OOM.
     */
    private static void testThreadLocalClassLoaderLeak() throws Exception {
        byte[] bytecode = loadBytecode(LeakCandidate.class.getName());

        EphemeralClassLoader loader = new EphemeralClassLoader("LeakingLoader", LeakCandidate.class.getName(), bytecode);
        WeakReference<ClassLoader> loaderRef = new WeakReference<>(loader);

        Class<?> clazz = loader.loadClass(LeakCandidate.class.getName());
        Object obj = clazz.getDeclaredConstructor().newInstance();

        // STORE INSTANCE IN THREADLOCAL -> CREATES LEAK!
        LEAK_HOLDER.set(obj);
        System.out.println("Stored instance of " + clazz.getName() + " in ThreadLocal");

        // Nullify explicit references
        loader = null;
        clazz = null;
        obj = null;

        // Trigger System GC
        System.gc();
        Thread.sleep(200);

        boolean isCollected = (loaderRef.get() == null);
        System.out.println("ClassLoader GC status with ThreadLocal leak: " + isCollected +
            " (Expected: false - ThreadLocal prevents ClassLoader GC!)");

        // CLEAN UP LEAK TO PREVENT MEMORY POLLUTION
        LEAK_HOLDER.remove();
        System.gc();
        Thread.sleep(200);
        System.out.println("After ThreadLocal.remove(), ClassLoader GC status: " + (loaderRef.get() == null) +
            " (Expected: true - ClassLoader collected after cleanup)");
    }

    public static class LeakCandidate {
        public String status() {
            return "LeakCandidate Active";
        }
    }

    private static class EphemeralClassLoader extends ClassLoader {
        private final String name;
        private final String targetClassName;
        private final byte[] bytecode;

        public EphemeralClassLoader(String name, String targetClassName, byte[] bytecode) {
            super(ClassLoader.getSystemClassLoader());
            this.name = name;
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
            return "EphemeralClassLoader{" + name + "}";
        }
    }

    private static byte[] loadBytecode(String className) throws Exception {
        String path = className.replace('.', '/') + ".class";
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (is == null) throw new ClassNotFoundException("Resource not found: " + path);
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
            return baos.toByteArray();
        }
    }
}
