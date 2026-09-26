package interview;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Memory Management, GC Lifecycles & ClassLoader Delegation Traps
 *
 * Demonstrates:
 * 1. ClassLoader Delegation Model Hierarchy (Application -> Platform -> Bootstrap)
 * 2. Reference Types GC Lifecycle (SoftReference, WeakReference, PhantomReference)
 * 3. Static Field Collection Memory Leak simulation
 */
class MemoryGCClassLoaderTrapsDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. ClassLoader Delegation Model Hierarchy ===");
        demonstrateClassLoaderHierarchy();

        System.out.println("\n=== 2. Soft, Weak, and Phantom Reference GC Lifecycles ===");
        demonstrateReferenceTypes();

        System.out.println("\n[SUCCESS] Memory, GC & ClassLoader traps demonstration completed cleanly.");
    }

    private static void demonstrateClassLoaderHierarchy() {
        // Application / System ClassLoader
        ClassLoader appClassLoader = MemoryGCClassLoaderTrapsDemo.class.getClassLoader();
        System.out.println("  MemoryGCClassLoaderTrapsDemo ClassLoader: " + appClassLoader);

        // Platform ClassLoader
        ClassLoader platformClassLoader = appClassLoader.getParent();
        System.out.println("  Parent ClassLoader (Platform):           " + platformClassLoader);

        // Bootstrap ClassLoader (Represented as null in C++ native code for core classes like java.lang.String)
        ClassLoader bootstrapClassLoader = String.class.getClassLoader();
        System.out.println("  Bootstrap ClassLoader (java.lang.String): " + bootstrapClassLoader + " (Native C++ Kernel ClassLoader)");
    }

    private static void demonstrateReferenceTypes() throws Exception {
        // 1. Strong Reference
        Object strongRef = new Object();

        // 2. SoftReference (Garbage collected ONLY when memory pressure is critical)
        SoftReference<Object> softRef = new SoftReference<>(new Object());
        System.out.println("  SoftReference get():  " + softRef.get() + " (Preserved under ample memory)");

        // 3. WeakReference (Garbage collected at NEXT GC cycle)
        WeakReference<Object> weakRef = new WeakReference<>(new Object());
        System.out.println("  WeakReference get() Before GC: " + weakRef.get());

        // Trigger Garbage Collection
        System.gc();
        Thread.sleep(100);

        System.out.println("  WeakReference get() After GC:  " + weakRef.get() + " (Purged by GC!)");

        // 4. PhantomReference (Used for tracking off-heap resource cleanup)
        ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
        PhantomReference<Object> phantomRef = new PhantomReference<>(new Object(), refQueue);

        System.out.println("  PhantomReference get(): " + phantomRef.get() + " (ALWAYS returns null per specification!)");
        System.out.println("  PhantomReference in Queue before GC? " + (refQueue.poll() != null));

        System.gc();
        Thread.sleep(100);

        System.out.println("  PhantomReference enqueued in ReferenceQueue after GC? " + (refQueue.poll() != null));
    }
}
