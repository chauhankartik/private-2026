# Chapter 12: Serialization (Items 85 – 90)

Hazards and patterns associated with Java object serialization.

---

## 📌 Item 85: Prefer alternatives to Java serialization

Java native serialization is inherently dangerous (RCE attacks, gadget chains, deserialization vulnerabilities).

**Use cross-platform structured data representations like JSON, Protocol Buffers, or Jackson instead.**

If native serialization is unavoidable, use JDK 17+ Native Serialization Filters (`ObjectInputFilter`).

---

## 📌 Item 86: Implement `Serializable` with great caution

Implementing `Serializable` decreases class flexibility (locks in internal byte stream representation) and opens security risks.

---

## 📌 Item 87: Use a custom serialized form

If a class's default serialized form ties its public API to internal private implementations, write custom `writeObject` and `readObject` methods.

Always declare an explicit `serialVersionUID`:
```java
private static final long serialVersionUID = 1L;
```

---

## 📌 Item 88: Write `readObject` methods defensively

Treat `readObject` as a public constructor. Validate class invariants and make defensive copies of mutable components during deserialization.

---

## 📌 Item 89: For instance control, prefer enum types to `readResolve`

`readResolve` allows replacing deserialized objects, but enum types guarantee singleton properties unconditionally.

---

## 📌 Item 90: Consider serialization proxies instead of serialized instances

The Serialization Proxy pattern uses a private static nested class proxy to represent the serialized state safely, avoiding class invariant vulnerabilities.

```java
private static class SerializationProxy implements Serializable {
    private final Date start;
    private final Date end;
    private static final long serialVersionUID = 1L;

    SerializationProxy(Period p) {
        this.start = p.start;
        this.end   = p.end;
    }

    private Object readResolve() {
        return new Period(start, end);
    }
}

private Object writeReplace() {
    return new SerializationProxy(this);
}
```
