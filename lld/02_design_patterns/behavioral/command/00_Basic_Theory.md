# The Command Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Encapsulate a request as an object, thereby letting you parameterize clients with different requests, queue or log requests, and support **undoable operations (Undo / Redo)**.

### Real-World Use Cases:
- Text Editors (Undo/Redo history stack `Ctrl+Z` / `Ctrl+Y`).
- Smart Home Automation (Programmable Remote Controls).
- Job Queues & Asynchronous Task Processing.
- Database Transaction Logs (Replay / Undo).

---

## 🧩 Key Participants:
1. **Command Interface:** Declares `execute()` and `undo()` methods.
2. **Concrete Commands:** Implements `execute()` by invoking actions on receiver.
3. **Receiver:** The actual target class that performs the business work.
4. **Invoker:** Stores command history stack and triggers execution.
