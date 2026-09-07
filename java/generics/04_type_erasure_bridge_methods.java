package java.generics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiler Type Erasure, Synthetic Bridge Methods (ACC_BRIDGE),
 * Heap Pollution (@SafeVarargs), and Generic Array Restrictions.
 */
public class TypeErasureBridgeMethodsDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Type Erasure Proof (Class Identity at Runtime) ===");
        demoTypeErasureIdentity();

        System.out.println("\n=== 2. Synthetic Bridge Methods Inspection ===");
        demoSyntheticBridgeMethods();

        System.out.println("\n=== 3. Heap Pollution & @SafeVarargs ===");
        demoHeapPollution();

        System.out.println("\n=== 4. Generic Array Creation Workaround ===");
        demoGenericArrayWorkaround();
    }

    /**
     * Type Erasure Proof: At runtime, List<String> and List<Integer> share the EXACT SAME Class instance!
     */
    private static void demoTypeErasureIdentity() {
        List<String> stringList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        System.out.println("stringList class: " + stringList.getClass().getName());
        System.out.println("intList class:    " + intList.getClass().getName());
        System.out.println("Are classes equal at runtime? " + (stringList.getClass() == intList.getClass()));
    }

    /**
     * Parent Interface with Generic Type Parameter
     */
    public interface Node<T> {
        void setData(T data);
    }

    /**
     * Concrete Class specifying T = Integer
     */
    public static class IntegerNode implements Node<Integer> {
        @Override
        public void setData(Integer data) {
            System.out.println("IntegerNode.setData(Integer): " + data);
        }
    }

    /**
     * Demonstrates Synthetic Bridge Method generation by javac.
     * The compiler generates a synthetic method: void setData(Object data)
     * which casts Object -> Integer and calls setData(Integer data)!
     */
    private static void demoSyntheticBridgeMethods() {
        IntegerNode node = new IntegerNode();
        Method[] methods = IntegerNode.class.getDeclaredMethods();

        System.out.println("Declared Methods in IntegerNode class:");
        for (Method m : methods) {
            System.out.println(" - Method Name: " + m.getName() + 
                               " | Parameter Types: " + java.util.Arrays.toString(m.getParameterTypes()) + 
                               " | Is Synthetic/Bridge? " + m.isBridge());
        }

        // Polymorphic invocation via Raw Type invoking the synthetic Bridge method!
        @SuppressWarnings({"rawtypes", "unchecked"})
        Node rawNode = node;
        rawNode.setData(100); // Calls bridge method -> casts -> calls setData(Integer)
    }

    /**
     * Heap Pollution: Occurs when a variable of a parameterized type refers to an object that is not of that type.
     */
    @SafeVarargs
    private static <T> List<T> createListFromVarargs(T... elements) {
        List<T> list = new ArrayList<>();
        for (T elem : elements) {
            list.add(elem);
        }
        return list;
    }

    private static void demoHeapPollution() {
        List<String> list = createListFromVarargs("Apple", "Banana", "Cherry");
        System.out.println("Varargs list created safely with @SafeVarargs: " + list);
    }

    /**
     * Generic Array Creation Workaround: Cannot execute `new T[10]` directly due to type erasure.
     * Must use java.lang.reflect.Array.newInstance() or Object[] cast wrapper.
     */
    @SuppressWarnings("unchecked")
    public static class GenericArrayWrapper<T> {
        private final T[] array;

        public GenericArrayWrapper(Class<T> clazz, int capacity) {
            // Correct way to instantiate generic array at runtime using Reflection!
            this.array = (T[]) java.lang.reflect.Array.newInstance(clazz, capacity);
        }

        public void set(int index, T item) {
            array[index] = item;
        }

        public T get(int index) {
            return array[index];
        }

        public T[] getArray() {
            return array;
        }
    }

    private static void demoGenericArrayWorkaround() {
        GenericArrayWrapper<String> stringArray = new GenericArrayWrapper<>(String.class, 3);
        stringArray.set(0, "Java");
        stringArray.set(1, "Generics");
        stringArray.set(2, "Mastery");

        System.out.println("Generic Array Element 1: " + stringArray.get(1));
        System.out.println("Array Runtime Class: " + stringArray.getArray().getClass().getComponentType().getName());
    }
}
