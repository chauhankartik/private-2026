import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * ============================================================
 *  03. CHILD-FIRST (REVERSE DELEGATION) CLASSLOADER
 * ============================================================
 *
 *  Demonstrates:
 *   1. Tomcat / Jetty / OSGi style Child-First ClassLoader.
 *   2. Overriding loadClass(String name, boolean resolve) to reverse
 *      the standard delegation hierarchy.
 *   3. Enforcing critical security policy: Core JDK packages (java.*, javax.*)
 *      MUST ALWAYS delegate to parent first to prevent SecurityException.
 *   4. Allowing web application dependencies to override server/classpath versions.
 * ============================================================
 */
class ChildFirstClassLoaderDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== CHILD-FIRST CLASSLOADER DEMO ===");

        byte[] bytecode = loadBytecode(SharedComponent.class.getName());

        // Create a Child-First ClassLoader
        ChildFirstClassLoader webAppLoader = new ChildFirstClassLoader(
            "WebAppLoader-1",
            SharedComponent.class.getName(),
            bytecode
        );

        // Load SharedComponent via WebAppLoader
        Class<?> childClass = webAppLoader.loadClass(SharedComponent.class.getName());
        System.out.println("Loaded Class              : " + childClass.getName());
        System.out.println("Loaded By ClassLoader     : " + childClass.getClassLoader());

        // Verify that Core JDK String class still delegates Parent-First!
        Class<?> stringClass = webAppLoader.loadClass("java.lang.String");
        System.out.println("String.class ClassLoader   : " + stringClass.getClassLoader() + " (Safe Parent-First Delegation)");
    }

    /**
     * Production-grade Child-First ClassLoader implementation.
     */
    public static class ChildFirstClassLoader extends ClassLoader {
        private final String name;
        private final String targetClassName;
        private final byte[] localBytecode;

        // Packages that MUST NEVER be loaded Child-First for security and JVM stability
        private static final Set<String> SYSTEM_PACKAGES = new HashSet<>();

        static {
            SYSTEM_PACKAGES.add("java.");
            SYSTEM_PACKAGES.add("javax.");
            SYSTEM_PACKAGES.add("jdk.");
            SYSTEM_PACKAGES.add("sun.");
        }

        public ChildFirstClassLoader(String name, String targetClassName, byte[] localBytecode) {
            super(ClassLoader.getSystemClassLoader());
            this.name = name;
            this.targetClassName = targetClassName;
            this.localBytecode = localBytecode;
        }

        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                // 1. Check if class has already been loaded locally
                Class<?> c = findLoadedClass(name);
                if (c != null) {
                    if (resolve) resolveClass(c);
                    return c;
                }

                // 2. Check if class is a system/JDK package -> Delegate PARENT-FIRST
                if (isSystemPackage(name)) {
                    c = super.loadClass(name, resolve);
                    return c;
                }

                // 3. Attempt CHILD-FIRST loading (try local findClass first)
                try {
                    c = findClass(name);
                    if (resolve) resolveClass(c);
                    System.out.println("[" + this.name + "] Child-First Loaded: " + name);
                    return c;
                } catch (ClassNotFoundException e) {
                    // Local loading failed -> Fallback to Parent ClassLoader
                }

                // 4. Fallback to PARENT delegation
                c = super.loadClass(name, resolve);
                if (resolve) resolveClass(c);
                return c;
            }
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals(targetClassName)) {
                return defineClass(name, localBytecode, 0, localBytecode.length);
            }
            throw new ClassNotFoundException("Class " + name + " not found in " + this.name);
        }

        private boolean isSystemPackage(String name) {
            for (String pkg : SYSTEM_PACKAGES) {
                if (name.startsWith(pkg)) return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "ChildFirstClassLoader{" + name + "}";
        }
    }

    public static class SharedComponent {
        public void printVersion() {
            System.out.println("SharedComponent Version 2.0 (WebApp Local)");
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
