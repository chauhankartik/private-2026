/**
 * ============================================================
 *  D - DEPENDENCY INVERSION PRINCIPLE (DIP)
 * ============================================================
 *
 * "High-level modules should not depend on low-level modules. 
 * Both should depend on abstractions (e.g., interfaces)."
 * "Abstractions should not depend on details. Details should 
 * depend on abstractions."
 *
 * 🧠 ANALOGY:
 * You bought a lamp. You don't hardwire the lamp directly into the 
 * electrical foundation of your house (tight coupling). 
 * Instead, your house provides a standard "Wall Socket" (the abstraction). 
 * The lamp has a "Plug" (implements the abstraction). 
 * Now, you can plug the lamp into ANY socket, and you can plug ANY 
 * device into that socket. Total flexibility!
 *
 * ⚠️ THE VIOLATION (Tightly Coupled Code)
 * The MacBook class (High-level) creates the WiredKeyboard (Low-level) 
 * directly using the `new` keyword. 
 * If I want to use a WirelessKeyboard, I have to rewrite the MacBook class!
 */

class WiredKeyboardViolation {
    public void type() { System.out.println("Typing via wire..."); }
}

class MacBookViolation {
    // TIGHT COUPLING: Direct dependency on a concrete class
    private WiredKeyboardViolation keyboard; 

    public MacBookViolation() {
        // High-level policy (MacBook) is dictating the low-level detail!
        this.keyboard = new WiredKeyboardViolation();
    }

    public void writeCode() {
        keyboard.type();
    }
}

/**
 * ✅ THE SOLUTION
 * Both high-level (MacBook) and low-level (Keyboard variants) depend 
 * on an ABSTRACTION (Keyboard interface).
 * We "inject" the dependency rather than instantiating it internally.
 */

// 1. The Abstraction 
interface Keyboard {
    void type();
}

// 2. Low-level detail A
class WiredKeyboard implements Keyboard {
    @Override
    public void type() { System.out.println("Typing via USB-C..."); }
}

// 3. Low-level detail B
class BluetoothKeyboard implements Keyboard {
    @Override
    public void type() { System.out.println("Typing over wireless..."); }
}

// 4. High-level module
class MacBook {
    private Keyboard keyboard;

    // INVERSION OF CONTROL / DEPENDENCY INJECTION
    // The MacBook doesn't care WHAT kind of keyboard it's given,
    // as long as it adheres to the Keyboard interface contract.
    public MacBook(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    public void writeCode() {
        keyboard.type(); // Polymorphic behavior
    }
}

/*
 * USAGE:
 * Keyboard myKeyboard = new BluetoothKeyboard();
 * MacBook myLaptop = new MacBook(myKeyboard); // injected!
 */

/**
 * 💡 INTERVIEW TAKEAWAY:
 * THIS IS THE MOST IMPORTANT PRINCIPLE FOR SYSTEM DESIGN!
 * DIP is the foundation of modern frameworks like Spring (Dependency Injection).
 * In an interview, ALWAYS pass dependencies (like Database connections, 
 * Payment Gateways, Notification Services) into your classes via Contructors.
 * NEVER initialize external services inside your business logic classes using `new`.
 */
