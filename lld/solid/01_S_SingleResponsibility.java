/**
 * ============================================================
 *  S - SINGLE RESPONSIBILITY PRINCIPLE (SRP)
 * ============================================================
 *
 * "A class should have one, and only one, reason to change."
 *
 * 🧠 ANALOGY:
 * Think of a restaurant. You have a Chef, a Waiter, and a Cashier.
 * If the Chef also had to take orders and process payments, the 
 * kitchen would be chaos.
 * If the menu changes, only the Chef's behavior needs to change.
 * If the payment system changes, only the Cashier needs to change.
 * Each role has a single responsibility.
 *
 * ⚠️ THE VIOLATION (The "Swiss Army Knife" Class)
 * The class below does three completely different things:
 * 1. Business Logic (calculating pay)
 * 2. Database Operations (saving to DB)
 * 3. Presentation/Formatting (generating report)
 *
 * If the DB schema changes, this class changes.
 * If the report format changes, this class changes.
 * If taxes change, this class changes. TOO MANY REASONS TO CHANGE!
 */
class EmployeeViolation {
    private String name;
    private double salary;

    public EmployeeViolation(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    // Responsibility 1: Core Business Logic
    public double calculatePay() {
        return salary * 0.8; // after tax
    }

    // Responsibility 2: Database Storage (Infrastructure)
    public void saveToDatabase() {
        System.out.println("Connecting to DB...");
        System.out.println("Saving " + name + " to DB...");
    }

    // Responsibility 3: Formatting/Presentation
    public String generateReport() {
        return "Employee Report: " + name + " earns " + salary;
    }
}

/**
 * ✅ THE SOLUTION
 * Break the large class into smaller, specialized classes.
 * Each class has exactly ONE reason to change.
 */

// 1. Core Data / Entity
class Employee {
    private String name;
    private double salary;

    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    public String getName() { return name; }
    public double getSalary() { return salary; }
}

// 2. Business Logic Handler
class PayCalculator {
    public double calculatePay(Employee employee) {
        return employee.getSalary() * 0.8; 
    }
}

// 3. Infrastructure / Database Handler
class EmployeeRepository {
    public void saveToDatabase(Employee employee) {
        System.out.println("Connecting to DB...");
        System.out.println("Saving " + employee.getName() + " to DB...");
    }
}

// 4. Presentation Handler
class EmployeeReportFormatter {
    public String generateReport(Employee employee) {
        return "Employee Report: " + employee.getName() + " earns " + employee.getSalary();
    }
}

/**
 * 💡 INTERVIEW TAKEAWAY:
 * In a real-world Google interview, if you're designing a large system 
 * (like a Parking Lot or Elevator), make sure your core entity (e.g., `Vehicle`) 
 * doesn't contain the logic for saving itself to the database, or charging 
 * the user's credit card. 
 * Use Repositories for storage, and Services for business logic.
 */
