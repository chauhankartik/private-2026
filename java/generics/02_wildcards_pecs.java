package java.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Java Generics Wildcards & The PECS Principle:
 * Producer Extends, Consumer Super.
 * Demonstrates Unbounded (<?>), Upper-Bounded (<? extends T>),
 * Lower-Bounded (<? super T>), and Subtyping Covariance vs Contravariance.
 */
public class WildcardsPECSDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. Invariance in Java Generics ===");
        demoInvariance();

        System.out.println("\n=== 2. Upper-Bounded Wildcards (<? extends Number> - Producer Extends) ===");
        demoUpperBoundedProducer();

        System.out.println("\n=== 3. Lower-Bounded Wildcards (<? super Integer> - Consumer Super) ===");
        demoLowerBoundedConsumer();

        System.out.println("\n=== 4. The PECS Principle in Action (Collections.copy Simulation) ===");
        demoPECSCopy();
    }

    /**
     * Demonstrates Invariance: List<Integer> is NOT a subtype of List<Number>
     */
    private static void demoInvariance() {
        List<Integer> intList = Arrays.asList(1, 2, 3);
        // List<Number> numList = intList; // COMPILE ERROR! Invariance prevents assignment.
        
        System.out.println("Integer List: " + intList);
        System.out.println("Invariance prevents List<Integer> from being assigned to List<Number>.");
    }

    /**
     * Upper-Bounded Wildcard (<? extends T>): PRODUCER
     * Use when you only READ elements from the collection.
     * Elements are guaranteed to be of type T (or a subclass).
     * CANNOT write/add elements (except null) because exact subtype is unknown.
     */
    private static void demoUpperBoundedProducer() {
        List<Integer> intList = List.of(10, 20, 30);
        List<Double> doubleList = List.of(1.5, 2.5, 3.5);

        System.out.println("Sum of Integers: " + sumOfList(intList));
        System.out.println("Sum of Doubles: " + sumOfList(doubleList));

        List<? extends Number> numberProducer = intList;
        Number num = numberProducer.get(0); // READ is safe: Guaranteed to be at least Number
        System.out.println("Read element as Number: " + num);

        // numberProducer.add(40); // COMPILE ERROR! Cannot add to Producer Extends list!
    }

    private static double sumOfList(List<? extends Number> list) {
        double sum = 0.0;
        for (Number n : list) { // Producer: Read elements as Number
            sum += n.doubleValue();
        }
        return sum;
    }

    /**
     * Lower-Bounded Wildcard (<? super T>): CONSUMER
     * Use when you only WRITE/INSERT elements into the collection.
     * Can safely add elements of type T (or subclasses of T).
     * READ elements return Object (specific type is unknown).
     */
    private static void demoLowerBoundedConsumer() {
        List<Number> numList = new ArrayList<>();
        List<Object> objList = new ArrayList<>();

        addIntegers(numList);
        addIntegers(objList);

        System.out.println("Number List after consumer write: " + numList);
        System.out.println("Object List after consumer write: " + objList);
    }

    private static void addIntegers(List<? super Integer> list) {
        // Consumer: Can safely WRITE Integers
        list.add(100);
        list.add(200);
        list.add(300);

        // Integer item = list.get(0); // COMPILE ERROR! Read returns Object, not Integer!
        Object obj = list.get(0); // READ returns Object only
    }

    /**
     * Full PECS Demonstration: Copying elements from Source (Producer) to Destination (Consumer)
     */
    private static void demoPECSCopy() {
        List<Integer> src = List.of(1, 2, 3, 4, 5);
        List<Number> dest = new ArrayList<>(Arrays.asList(new Number[src.size()]));

        copyPECS(dest, src);
        System.out.println("Copied Destination List: " + dest);
    }

    /**
     * Canonical PECS Method Signature:
     * dest is CONSUMER (super), src is PRODUCER (extends)
     */
    public static <T> void copyPECS(List<? super T> dest, List<? extends T> src) {
        for (int i = 0; i < src.size(); i++) {
            T item = src.get(i); // Read from Producer (extends)
            dest.set(i, item);   // Write to Consumer (super)
        }
    }
}
