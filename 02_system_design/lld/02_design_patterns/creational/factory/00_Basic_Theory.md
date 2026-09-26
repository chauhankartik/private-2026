# The Factory & Abstract Factory Patterns: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Instantiating objects directly using `new ConcreteClass()` binds caller code to specific concrete classes, breaking the **Open-Closed Principle (OCP)** and **Dependency Inversion Principle (DIP)**.

The **Factory Pattern** delegates object creation to specialized factory methods or classes, hiding complex initialization logic from clients.

---

## 🏭 Three Factory Variants

### 1. Simple Factory (Idiom)
- A single factory class with a `createObject(type)` switch statement.
- *Con:* Modifying/adding new types requires editing the factory `switch` statement (Violates OCP).

### 2. Factory Method Pattern (GoF Behavioral/Creational)
- Defines an interface for creating an object, but lets subclasses decide which class to instantiate.
- Delegates object creation to concrete subclass factories.

### 3. Abstract Factory Pattern (Factory of Factories)
- Provides an interface for creating **families of related or dependent objects** without specifying their concrete classes.
- *Example:* UI Theme Factory creating matching `Button`, `Checkbox`, and `ScrollBar` components for macOS vs Windows.

---

## 📊 Comparison Matrix

| Pattern | Focus | Primary Mechanism | Use Case |
| :--- | :--- | :--- | :--- |
| **Simple Factory** | Single class object creation | Switch / If-Else router | Simple object instantiation |
| **Factory Method** | Single product subclass creation | Inheritance / Polymorphism | Dynamic plugin extension |
| **Abstract Factory** | Product Families creation | Composition / Interfaces | Cross-platform UI themes, Payment+Notification suites |
