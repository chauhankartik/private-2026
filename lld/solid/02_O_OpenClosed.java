/**
 * ============================================================
 *  O - OPEN/CLOSED PRINCIPLE (OCP)
 * ============================================================
 *
 * "Software entities (classes, modules, functions) should be 
 * OPEN for extension, but CLOSED for modification."
 *
 * 🧠 ANALOGY:
 * Think of an electrical wall socket. It is "closed for modification" 
 * (you don't smash the wall open and rewire it when you buy a new TV). 
 * But it is "open for extension" (you can plug an adapter or a power 
 * strip into it to power new, different types of devices).
 *
 * ⚠️ THE VIOLATION (The "If-Else/Switch-Case Hell")
 * The class below violates OCP because every time we want to add a 
 * new customer type (e.g., "VIP", "Employee"), we have to MODIFY 
 * the existing code inside the `DiscountCalculator` class.
 */
class DiscountCalculatorViolation {
    
    public double calculateDiscount(String customerType, double amount) {
        // If we add "VIP", we have to modify this method!
        if (customerType.equals("Regular")) {
            return amount * 0.05;
        } else if (customerType.equals("Premium")) {
            return amount * 0.10;
        } else {
            return 0;
        }
    }
}

/**
 * ✅ THE SOLUTION
 * We use Polymorphism (Interfaces or Abstract Classes).
 * Now, the core logic relies on abstractions. If we want to add a 
 * "VIP" customer, we just CREATE A NEW CLASS. We don't touch existing code.
 */

// 1. The Abstraction (The "Wall Socket")
interface Customer {
    double getDiscountModifier();
}

// 2. The Extensions (The "Plugs")
class RegularCustomer implements Customer {
    @Override
    public double getDiscountModifier() {
        return 0.05;
    }
}

class PremiumCustomer implements Customer {
    @Override
    public double getDiscountModifier() {
        return 0.10;
    }
}

// 3. NEW FEATURE: Extending without modifying existing code!
class VIPCustomer implements Customer {
    @Override
    public double getDiscountModifier() {
        return 0.20; // 20% discount
    }
}

// 4. Closed for modification
class DiscountCalculator {
    public double calculateDiscount(Customer customer, double amount) {
        return amount * customer.getDiscountModifier();
    }
}

/**
 * 💡 INTERVIEW TAKEAWAY:
 * If you find yourself writing a switch-case statement or a long list of 
 * if-else blocks checking an object's "type", you are likely violating OCP. 
 * Use Strategy Pattern or Factory Pattern combined with interfaces to solve this.
 * Google interviewers love seeing OCP because it proves your code is maintainable.
 */
