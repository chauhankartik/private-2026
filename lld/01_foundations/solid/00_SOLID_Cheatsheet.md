# SOLID Principles — Quick Interview Cheatsheet
> For Low-Level Design (LLD) / Object-Oriented Design (OOD) Rounds

A great LLD interview doesn't just solve the problem, it demonstrates that your code is maintainable, scalable, and modular. SOLID is the vocabulary you use to prove this.

---

## 🚀 The Core Principles

### S - Single Responsibility Principle (SRP)
* **Definition:** A class should have one, and only one, reason to change.
* **Analogy:** The Chef shouldn't be the Cashier. Specialized tools beat the Swiss Army knife.
* **Red Flag in code:** A class with "Manager" or "Utility" in the name that is 3,000 lines long, handling DB logic, UI logic, and business logic.
* **How to fix:** Extract logic into separate classes (Repositories, Services, Controllers).

### O - Open/Closed Principle (OCP)
* **Definition:** Open for extension, Closed for modification.
* **Analogy:** Wall sockets and plug adapters. Add functionality by adding new code, not tweaking old code.
* **Red Flag in code:** Giant `switch` or `if-else` chains checking object "types" or "flags".
* **How to fix:** Use Polymorphism. Create an interface, implement it in new classes, and use the Strategy or Factory design pattern.

### L - Liskov Substitution Principle (LSP)
* **Definition:** Subclasses must be substitutable for their base classes without breaking anything.
* **Analogy:** The Duck Test. If a toy duck needs batteries, it shouldn't inherit from real Duck.
* **Red Flag in code:** Overriding a parent method to throw `UnsupportedOperationException`, or leaving it intentionally blank.
* **How to fix:** Fix your inheritance hierarchy. Use composition instead of inheritance, or split the superclass capabilities into smaller interfaces.

### I - Interface Segregation Principle (ISP)
* **Definition:** Don't force clients to depend on interfaces they don't use.
* **Analogy:** A TV remote with 100 buttons vs a simple Apple TV remote. Keep it lean.
* **Red Flag in code:** Implementing a massive interface where 80% of the methods are left blank or throw errors.
* **How to fix:** Split "fat" interfaces into smaller, role-specific interfaces (`Printer`, `Scanner` instead of `Machine`).

### D - Dependency Inversion Principle (DIP)
* **Definition:** High-level modules should depend on abstractions, not low-level concrete specifics.
* **Analogy:** Plugging a lamp into a standard wall socket instead of hard-wiring it to the electrical grid.
* **Red Flag in code:** Using the `new` keyword inside a class to create hard dependencies (like `MySQLDatabase db = new MySQLDatabase()`).
* **How to fix:** Dependency Injection. Pass dependencies into the class constructor via interfaces (`Database interface`).

---

## 🗣️ How to Talk about SOLID in an Interview

When designing a system (e.g., "Design a Parking Lot"):

> *"I'm going to create an `IPaymentProcessor` interface rather than directly integrating Stripe in the `Ticket` class. This satisfies the **Dependency Inversion Principle** because we can swap in PayPal later, and it satisfies the **Single Responsibility Principle** since the `Ticket` shouldn't know how credit cards work."*

> *"Instead of having an `if (vehicleType == CAR)` block to calculate fees, I will create a `Vehicle` interface with a `getParkingRateModifier()` method. This way, if they add Motorcycles or Buses tomorrow, our code is **Open for extension and Closed for modification (OCP)**."*

---

## 📁 Study Materials Check
I have generated 5 `.java` files for you in this directory. 
Read them in numerical order. Each file contains:
1. The formal definition.
2. An intuitive, memorable real-world analogy.
3. Code showing the VIOLATION.
4. Code showing the FIX.
5. An "Interview Takeaway" tip.
