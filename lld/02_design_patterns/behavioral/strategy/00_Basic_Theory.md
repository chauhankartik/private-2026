# The Strategy Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Define a family of algorithms, encapsulate each one, and make them interchangeable. **Strategy** lets the algorithm vary independently from clients that use it.

### Code Smell (Violating Open-Closed Principle):
```java
// Bad Code: Giant switch statement for payment processing
public void process(String type, double amount) {
    if (type.equals("CREDIT_CARD")) { ... }
    else if (type.equals("PAYPAL")) { ... }
    else if (type.equals("CRYPTO")) { ... }
}
```

---

## 🎯 The Strategy Solution

Encapsulate each algorithm variant (`CreditCardStrategy`, `PayPalStrategy`, `CryptoStrategy`) behind a common interface (`PaymentStrategy`). Pass the strategy dynamically into the context at runtime!

```
Context Class (PaymentService) ──(Uses)──► [PaymentStrategy Interface]
                                                  ▲
                                         ┌────────┴────────┐
                              CreditCardStrategy    PayPalStrategy
```
