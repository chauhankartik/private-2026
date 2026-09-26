# The Adapter Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Convert the interface of a class into another interface clients expect. **Adapter** lets classes work together that couldn't otherwise because of incompatible interfaces.

### Real-World Analogy:
A US power plug adapter allows a US laptop plug (3-prong flat) to connect to a European wall socket (2-prong round).

---

## 🔌 Object Adapter vs. Class Adapter

1. **Object Adapter (Composition - Recommended):**
   - The Adapter implements the Target interface and holds a reference (instance) to the Adaptee object.
2. **Class Adapter (Multiple Inheritance):**
   - The Adapter inherits from both Target and Adaptee (Supported in C++, limited in Java via interfaces).

```
Client ──► [Target Interface]
                   ▲
                   │
           [Adapter Class] ──(Wraps / Delegates)──► [Adaptee 3rd-Party API]
```

---

## 💡 When to Use?
- Integrating legacy libraries or 3rd-party APIs (e.g., Stripe API, PayPal API) into your existing application standard interface without mutating 3rd-party vendor code.
