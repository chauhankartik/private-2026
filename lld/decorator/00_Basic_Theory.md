# The Decorator Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Imagine you walk into a coffee shop and order a **Plain Coffee** (₹100).

Now you say: *"Add Milk"* → ₹120  
Then: *"Add Whipped Cream"* → ₹150  
Then: *"Add Caramel Syrup"* → ₹180  

You started with a plain coffee and kept **wrapping** it with extras.  
The base item never changed. Each add-on just wrapped around the previous order.

**This is the Decorator Pattern.** You take an existing object and wrap it with new behavior, without modifying the original object's code.

---

## 🤔 Why Not Just Use Inheritance?

This is the question every interviewer expects you to answer.

### The Inheritance Explosion Problem

Let's say you have these coffees: `PlainCoffee`, `Espresso`, `Decaf`  
And these add-ons: `Milk`, `WhippedCream`, `Caramel`, `Vanilla`

With inheritance, you'd need a class for EVERY combination:
```
PlainCoffeeWithMilk
PlainCoffeeWithMilkAndCream
PlainCoffeeWithCaramel
EspressoWithMilk
EspressoWithMilkAndCream
EspressoWithMilkAndCreamAndCaramelAndVanilla
DecafWithMilk
...
```

That's **3 × 2⁴ = 48 subclasses** just for 3 coffees and 4 add-ons!  
Add one more add-on? Double all your classes. This is called **class explosion**.

### The Decorator Solution

With the Decorator Pattern, you need exactly:
- 3 base classes (PlainCoffee, Espresso, Decaf)
- 4 decorator classes (Milk, WhippedCream, Caramel, Vanilla)
- **Total: 7 classes** (not 48)

And you can combine them in any order at runtime:
```java
Coffee order = new Caramel(new Milk(new PlainCoffee()));
// Reading inside out: PlainCoffee → wrapped with Milk → wrapped with Caramel
```

---

## 🧩 The 3 Main Characters

### 1. Component (The Interface / Abstract Class)
The contract that both the real object and the decorators share.  
*In our analogy:* `Coffee` interface with `getCost()` and `getDescription()`.

### 2. Concrete Component (The Base Object)
The actual real thing being decorated.  
*In our analogy:* `PlainCoffee`, `Espresso`.

### 3. Decorator (The Wrapper)
An abstract class that:
- **Implements** the same interface as the Component (IS-A relationship)
- **Holds a reference** to a Component (HAS-A relationship)
- Delegates calls to the wrapped component, adding its own behavior

*In our analogy:* `MilkDecorator`, `CaramelDecorator`.

---

## 💡 The Key Insight (Read This Twice)

A Decorator is **both** a Coffee (implements the Coffee interface) **AND** has a Coffee (holds a reference to one).

This means a Decorator can wrap another Decorator. That's how you stack: 
```
Caramel wraps → Milk wraps → PlainCoffee
```
Each layer doesn't know or care what's inside it. It just adds its own cost and delegates the rest inward.

---

## 🎯 Where You See This in the Real World

| Real World | Component | Decorator |
|---|---|---|
| Java I/O Streams | `InputStream` | `BufferedInputStream`, `DataInputStream`, `GZIPInputStream` |
| UI Frameworks | `TextView` | `ScrollableTextView`, `BorderedTextView` |
| Web Middleware | `HttpHandler` | `AuthMiddleware`, `LoggingMiddleware`, `CompressionMiddleware` |
| Pizza Ordering | `BasePizza` | `ExtraCheese`, `Mushrooms`, `Olives` |

The most famous real example is **Java I/O**:
```java
// This is the Decorator Pattern in action in the Java standard library!
InputStream raw       = new FileInputStream("data.txt");        // Concrete Component
InputStream buffered  = new BufferedInputStream(raw);            // Decorator 1
InputStream gzipped   = new GZIPInputStream(buffered);           // Decorator 2
DataInputStream data   = new DataInputStream(gzipped);           // Decorator 3
```
Each wrapper adds a capability (buffering, decompression, typed reading) without modifying the original stream.

---

## 🚀 The Progression Path for Interviews

### Level 1: The Basic Implementation
Write the Coffee Shop example. Show `getCost()` and `getDescription()` delegation.  
*(See `01_Basic_Decorator_Code.java`)*

### Level 2: Show You Understand Java I/O
Mention that Java's `InputStream` hierarchy is the textbook Decorator Pattern.  
The interviewer will be impressed that you recognize it in production code.

### Level 3: When Decorator Meets Other Patterns
- **Decorator + Strategy:** Each decorator can hold a configurable strategy for how it modifies behavior.
- **Decorator + Factory:** Use a factory to build complex decorator chains from config.

### Level 4: Staff-Level Concerns
- Order sensitivity: Does `Encrypt(Compress(data))` behave differently from `Compress(Encrypt(data))`? (Yes!)
- Performance: Deep decorator chains add method call overhead (stack depth).
- Debugging: Stack traces become hard to read with 10 layers of wrapping.
- Immutability: Should decorators be stateless? (Usually yes, for thread safety).

---

**Next Step:** Open `01_Basic_Decorator_Code.java` — the cleanest possible implementation!
