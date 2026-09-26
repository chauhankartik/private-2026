/**
 * ============================================================
 *  I - INTERFACE SEGREGATION PRINCIPLE (ISP)
 * ============================================================
 *
 * "Clients should not be forced to depend upon interfaces that 
 * they do not use."
 *
 * 🧠 ANALOGY:
 * Imagine buying a Universal Remote to control your old CRT TV. 
 * The remote has 150 buttons (Netflix, Smart Hub, Bluetooth pairing, etc.), 
 * but you only need Volume and Channel. It's confusing, bloated, and if 
 * the Bluetooth spec changes, they might recall your remote even though 
 * you don't even use Bluetooth!
 * Interfaces should be like specialized remotes: small and specific.
 *
 * ⚠️ THE VIOLATION (The "Fat Interface")
 * Below is a bloated interface. If we have a simple basic printer, 
 * we still have to provide implementations for scan() and fax().
 */

// The FAT Interface
interface MultiFunctionMachineViolation {
    void print();
    void scan();
    void fax();
}

class BasicPrinterViolation implements MultiFunctionMachineViolation {
    @Override
    public void print() {
        System.out.println("Printing document...");
    }

    @Override
    public void scan() {
        // VIOLATION: Forced to implement a method it doesn't use!
        throw new UnsupportedOperationException("I don't know how to scan!");
    }

    @Override
    public void fax() {
        // VIOLATION
        throw new UnsupportedOperationException("I don't know how to fax!");
    }
}

/**
 * ✅ THE SOLUTION
 * Break the fat interface into smaller, logically cohesive interfaces.
 * Now, classes only implement exactly what they need.
 */

interface Printer {
    void print();
}

interface Scanner {
    void scan();
}

interface FaxMachine {
    void fax();
}

// Now the BasicPrinter is clean and lean
class BasicPrinter implements Printer {
    @Override
    public void print() {
        System.out.println("Printing document...");
    }
}

// A high-end machine can just implement multiple interfaces!
class AdvancedPhotocopier implements Printer, Scanner, FaxMachine {
    @Override
    public void print() {
        System.out.println("High quality printing...");
    }

    @Override
    public void scan() {
        System.out.println("Scanning to email...");
    }

    @Override
    public void fax() {
        System.out.println("Sending fax...");
    }
}

/**
 * 💡 INTERVIEW TAKEAWAY:
 * Keep your interfaces tight. In Java, this maps perfectly to the 
 * concept of Role Interfaces. (e.g., `Comparable`, `Runnable`, `Callable`).
 * Notice how Java doesn't have a massive `DoEverything` interface. 
 * Small interfaces make your system highly modular and decouple dependencies.
 */
