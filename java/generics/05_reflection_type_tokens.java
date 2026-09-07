package java.generics;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reflection Over Generics & Super Type Tokens:
 * Demonstrates ParameterizedType runtime reflection,
 * the Super Type Token pattern (Neal Gafter / Anonymous Inner Class),
 * and Typesafe Heterogeneous Containers.
 */
public class ReflectionTypeTokensDemo {

    // Sample field containing full generic type metadata preserved in bytecode class signature
    private List<String> stringListPayload;
    private Map<String, Integer> scoreMapPayload;

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. Inspecting Field Generic Signatures via Reflection ===");
        demoFieldGenericReflection();

        System.out.println("\n=== 2. Super Type Token Pattern (TypeReference<T>) ===");
        demoSuperTypeToken();

        System.out.println("\n=== 3. Typesafe Heterogeneous Container Pattern ===");
        demoTypesafeHeterogeneousContainer();
    }

    /**
     * Demonstrates inspecting generic type parameters preserved in Class Field metadata
     */
    private static void demoFieldGenericReflection() throws NoSuchFieldException {
        Field stringListField = ReflectionTypeTokensDemo.class.getDeclaredField("stringListPayload");
        Type genericType = stringListField.getGenericType();

        System.out.println("Field Name: stringListPayload");
        System.out.println("Generic Type Class: " + genericType.getClass().getName());

        if (genericType instanceof ParameterizedType parameterizedType) {
            System.out.println("Raw Type: " + parameterizedType.getRawType());
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            System.out.println("Actual Generic Type Argument: " + actualTypeArguments[0].getTypeName());
        }

        Field scoreMapField = ReflectionTypeTokensDemo.class.getDeclaredField("scoreMapPayload");
        Type mapType = scoreMapField.getGenericType();
        if (mapType instanceof ParameterizedType paramType) {
            System.out.println("\nField Name: scoreMapPayload");
            System.out.println("Key Type:   " + paramType.getActualTypeArguments()[0].getTypeName());
            System.out.println("Value Type: " + paramType.getActualTypeArguments()[1].getTypeName());
        }
    }

    /**
     * Super Type Token Implementation (TypeReference<T>):
     * Anonymous inner subclassing captures ParameterizedType metadata at runtime.
     * (Pattern used in Spring ParameterizedTypeReference<T> & Jackson TypeReference<T>)
     */
    public abstract static class TypeReference<T> {
        private final Type type;

        protected TypeReference() {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new IllegalArgumentException("TypeReference must be instantiated as an anonymous inner class!");
            }
            ParameterizedType parameterizedType = (ParameterizedType) superclass;
            this.type = parameterizedType.getActualTypeArguments()[0];
        }

        public Type getType() {
            return this.type;
        }
    }

    private static void demoSuperTypeToken() {
        // Instantiating as Anonymous Inner Class subclass captures List<String> in bytecode!
        TypeReference<List<String>> stringListToken = new TypeReference<List<String>>() {};
        System.out.println("Captured Super Type Token for List<String>: " + stringListToken.getType());

        TypeReference<Map<Integer, List<String>>> complexToken = new TypeReference<Map<Integer, List<String>>>() {};
        System.out.println("Captured Super Type Token for Complex Map: " + complexToken.getType());
    }

    /**
     * Typesafe Heterogeneous Container (Effective Java Item 33)
     */
    public static class TypesafeContainer {
        private final Map<Class<?>, Object> values = new HashMap<>();

        public <T> void put(Class<T> type, T instance) {
            values.put(java.util.Objects.requireNonNull(type), instance);
        }

        public <T> T get(Class<T> type) {
            return type.cast(values.get(type));
        }
    }

    private static void demoTypesafeHeterogeneousContainer() {
        TypesafeContainer container = new TypesafeContainer();

        container.put(String.class, "Spring Boot Microservices");
        container.put(Integer.class, 2026);
        container.put(Double.class, 99.99);

        String title = container.get(String.class);
        Integer year = container.get(Integer.class);
        Double score = container.get(Double.class);

        System.out.println("Heterogeneous Container Retrieved:");
        System.out.println(" - Title (String):  " + title);
        System.out.println(" - Year (Integer):  " + year);
        System.out.println(" - Score (Double):  " + score);
    }
}
