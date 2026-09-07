/**
 * ============================================================
 *  L - LISKOV SUBSTITUTION PRINCIPLE (LSP)
 * ============================================================
 *
 * "Objects of a superclass should be replaceable with objects of 
 * its subclasses without breaking the application."
 *
 * 🧠 ANALOGY:
 * The "Duck Test". If it looks like a duck, quacks like a duck, but 
 * needs batteries to work... you have the wrong abstraction!
 * A toy duck shouldn't inherit from a real duck if it can't lay eggs.
 * Another analogy: If I ask you for a "Coffee Maker", I should be able 
 * to use any brand without reading a special manual on how to trick it 
 * into brewing coffee. It must fulfill the basic contract.
 *
 * ⚠️ THE VIOLATION (The "Broken Contract")
 * The classic Rectangle-Square problem or Bird-Ostrich problem.
 * Below, an Ostrich is a Bird, but forcing it to implement `fly()` 
 * breaks the expectations of the core program.
 */

class BirdViolation {
    public void fly() {
        System.out.println("Flapping wings wildly!");
    }
}

class SparrowViolation extends BirdViolation {
    // Works fine
}

class OstrichViolation extends BirdViolation {
    @Override
    public void fly() {
        // VIOLATION: A subclass is throwing an unexpected exception!
        // Any code iterating over a List<Bird> and calling fly() will crash here.
        throw new UnsupportedOperationException("I can't fly!"); 
    }
}

/**
 * ✅ THE SOLUTION
 * Fix the inheritance tree. Don't force behaviors on classes that 
 * don't support them. Separate the capabilities.
 */

// 1. High-level pure abstraction
class Bird {
    public void eat() {
        System.out.println("Eating seeds...");
    }
}

// 2. Specialized capability interfaces
interface Flyable {
    void fly();
}

// 3. Proper implementations
class Sparrow extends Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("Sparrow flying high!");
    }
}

class Ostrich extends Bird {
    // Ostrich just inherits eat(). It doesn't have a fly() method at all.
    // We didn't implement Flyable, so we never have to throw an exception.
    public void run() {
        System.out.println("Ostrich running very fast!");
    }
}

/**
 * 💡 INTERVIEW TAKEAWAY:
 * LSP violations usually manifest as:
 * 1. Throwing Unimplemented/Unsupported exceptions in overridden methods.
 * 2. Overridden methods that do nothing (empty bodies).
 * 3. Modifying the expected behavior of a method drastically (e.g. returning 
 *    null when the base class never returned null).
 * If you see these, re-think your class hierarchy by favoring Composition 
 * over Inheritance, or creating finer-grained capabilities (interfaces).
 */
