# The Composite Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Compose objects into **tree structures** to represent part-whole hierarchies. **Composite** lets clients treat individual objects (leaves) and compositions of objects (containers/composite nodes) **uniformly**.

### Real-World Examples:
- File Systems: Directories contain both Files (Leaves) and Sub-Directories (Composites).
- UI Rendering: Panel (Composite) contains Buttons (Leaves), TextFields (Leaves), and Sub-Panels (Composites).

```
                      [ Root Directory ] (Composite)
                      /                \
          [ File_A.txt ] (Leaf)    [ SubFolder ] (Composite)
                                         \
                                  [ File_B.txt ] (Leaf)
```

---

## 🧩 Key Participants:
1. **Component:** Abstract interface declaring common operations (`display()`, `getSize()`) for both simple and complex elements.
2. **Leaf:** Represents leaf objects in tree (no children).
3. **Composite:** Represents container objects holding children. Implements `add()`, `remove()`, `getChild()`.
