package singleton;

import java.io.Serializable;

/**
 * Production-grade demonstration of all Singleton Pattern variants:
 * 1. Double-Checked Locking with volatile
 * 2. Bill Pugh Holder Class
 * 3. Enum Singleton (Reflection & Serialization Safe)
 */
public class SingletonDemo {

    // 1. Double-Checked Locking Singleton
    public static class DoubleCheckedSingleton implements Serializable {
        private static final long serialVersionUID = 1L;
        private static volatile DoubleCheckedSingleton instance;

        private DoubleCheckedSingleton() {
            if (instance != null) {
                throw new RuntimeException("Reflection protection: Singleton instance already exists!");
            }
        }

        public static DoubleCheckedSingleton getInstance() {
            if (instance == null) {
                synchronized (DoubleCheckedSingleton.class) {
                    if (instance == null) {
                        instance = new DoubleCheckedSingleton();
                    }
                }
            }
            return instance;
        }

        protected Object readResolve() {
            return getInstance(); // Prevents new instance creation on deserialization
        }
    }

    // 2. Bill Pugh Holder Singleton
    public static class BillPughSingleton {
        private BillPughSingleton() {}

        private static class InstanceHolder {
            private static final BillPughSingleton INSTANCE = new BillPughSingleton();
        }

        public static BillPughSingleton getInstance() {
            return InstanceHolder.INSTANCE;
        }
    }

    // 3. Enum Singleton
    public enum EnumSingleton {
        INSTANCE;

        private int connectionCount = 0;

        public synchronized void connect() {
            connectionCount++;
            System.out.println("EnumSingleton: Connection established. Total active: " + connectionCount);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Singleton Pattern Demo ===");

        // Double-Checked Singleton Test
        DoubleCheckedSingleton d1 = DoubleCheckedSingleton.getInstance();
        DoubleCheckedSingleton d2 = DoubleCheckedSingleton.getInstance();
        System.out.println("DoubleCheckedSingleton Same Instance? " + (d1 == d2));

        // Bill Pugh Singleton Test
        BillPughSingleton b1 = BillPughSingleton.getInstance();
        BillPughSingleton b2 = BillPughSingleton.getInstance();
        System.out.println("BillPughSingleton Same Instance? " + (b1 == b2));

        // Enum Singleton Test
        EnumSingleton e1 = EnumSingleton.INSTANCE;
        EnumSingleton e2 = EnumSingleton.INSTANCE;
        System.out.println("EnumSingleton Same Instance? " + (e1 == e2));
        e1.connect();
        e2.connect();
    }
}
