# The State Pattern: From Basic to Advanced

## 📖 The Core Concept: Why do we need it?

Imagine a **Vending Machine**.

It behaves very differently depending on what state it's in:
- **No Coin Inserted:** Press "Dispense" → "Please insert a coin first."
- **Coin Inserted:** Press "Dispense" → Dispenses item, returns to idle.
- **Out of Stock:** Insert a coin → "Sorry, machine is empty. Coin returned."

The same action (`dispense()`) produces completely different results depending on the current **state** of the machine.

**This is the State Pattern.** It allows an object to **change its behavior when its internal state changes**. The object will appear to change its class.

---

## 🤔 Why Not Just Use If-Else or Switch?

This is the question every interviewer expects you to answer.

### The If-Else Nightmare

```java
// WITHOUT State Pattern — every method becomes a nightmare
public void insertCoin() {
    if (state == IDLE)          { /* accept coin, change to HAS_COIN */ }
    else if (state == HAS_COIN) { /* return coin, stay in HAS_COIN */ }
    else if (state == SOLD_OUT) { /* reject coin, message */ }
    else if (state == DISPENSING) { /* ignore */ }
}

public void pressDispense() {
    if (state == IDLE)          { /* say "insert coin" */ }
    else if (state == HAS_COIN) { /* dispense, change state */ }
    else if (state == SOLD_OUT) { /* say "sold out" */ }
    else if (state == DISPENSING) { /* already dispensing */ }
}
```

- **4 states × 4 actions = 16 if-else branches.**
- Add one new state? Modify EVERY method. Classic **Open/Closed Principle violation**.
- Add one new action? Add a new if-else block in every state.
- **Impossible to test** one state in isolation.

### The State Pattern Solution

```java
// WITH State Pattern — each state is its own class
class HasCoinState implements VendingMachineState {
    public void insertCoin()    { machine.returnCoin(); }
    public void pressDispense() { machine.dispenseItem(); machine.setState(idleState); }
    public void refill()        { /* not relevant in this state */ }
}
```

- Each state lives in **its own class**.
- **Adding a new state** = adding a new class. Zero changes to existing states.
- **Adding a new action** = adding a method to the interface + implementing in each state.
- Each state class is tiny, focused, and independently testable.

---

## 🧩 The 3 Main Characters

### 1. State Interface
Declares all the actions the context can perform.
Every concrete state must implement every action.
*In our analogy:* `VendingMachineState` with `insertCoin()`, `pressDispense()`, `refill()`.

### 2. Concrete States
Each class represents ONE state and defines the behavior for that state.
*In our analogy:* `IdleState`, `HasCoinState`, `DispensingState`, `SoldOutState`.

### 3. Context
The object whose behavior changes. It holds a reference to the current state.
Delegates all actions to the current state object.
*In our analogy:* `VendingMachine`.

---

## 💡 The Key Insight (Read This Twice)

The **Context** delegates behavior to its **current State** object.  
When a transition happens, the state object (or context) **swaps out** the current state reference.

```
Client calls: machine.pressDispense()
Context delegates: currentState.pressDispense(machine)
State executes logic AND may set: machine.setState(newState)
```

The client never knows which state the context is in. It always just calls the same methods on the context.

---

## 🎯 Where You See This in the Real World

| Real World | Context | States |
|---|---|---|
| **Traffic Light** | `TrafficLight` | `RedState`, `GreenState`, `YellowState` |
| **Order Lifecycle** | `Order` | `PendingState`, `PaidState`, `ShippedState`, `DeliveredState`, `CancelledState` |
| **TCP Connection** | `TCPConnection` | `ListenState`, `EstablishedState`, `ClosedState` |
| **Media Player** | `AudioPlayer` | `PlayingState`, `PausedState`, `StoppedState` |
| **Elevator** | `Elevator` | `IdleState`, `MovingUpState`, `MovingDownState`, `DoorOpenState` |
| **Thread lifecycle** | `Thread` | `NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TERMINATED` |

Java's own `Thread` class is a real-world State Pattern: a thread's `start()` behaves differently depending on whether it's in `NEW`, `RUNNABLE`, or `TERMINATED` state.

---

## 🚀 The Progression Path for Interviews

### Level 1: The Basic Implementation
Write the Vending Machine. Show `insertCoin()` and `dispense()` delegating to state objects.
*(See `01_Basic_State_Code.java`)*

### Level 2: Show You Know the Trade-offs
- **Pro:** Adding a state = adding a class (Open/Closed ✓)
- **Con:** If you have N states and M actions, you still have N×M methods total — they're just organized differently.
- **Pro:** State transitions are explicit and traceable.
- **Con:** State objects may need to reference each other → coupling between states.

### Level 3: State vs Strategy
The two patterns look identical in code. The difference is **intent**:
- **Strategy:** The algorithm is selected **by the client** and usually doesn't change during the object's lifetime.
- **State:** The state transitions happen **internally**, driven by the object's own logic or events.

### Level 4: Staff-Level Concerns
- **Who owns the transition?** State objects can call `context.setState()` directly, OR the context can decide based on the state's return value. Choose one style consistently.
- **Shared State:** If state objects are stateless, they can be shared singletons (saves memory, especially with many context objects).
- **State + Event/Command:** Large state machines often combine State pattern with an Event/Command queue.
- **Persistence:** Serializing the state enum vs. the state object — how do you restore state from a database?

---

## State vs Strategy vs Template Method — Quick Reference

| | State | Strategy | Template Method |
|---|---|---|---|
| **Problem solved** | Behavior changes with internal state | Select algorithm at runtime | Fix algorithm skeleton, vary steps |
| **Who triggers change?** | The object itself (internal) | The client (external) | Not applicable |
| **Context awareness** | State usually knows Context | Strategy usually doesn't | — |
| **Use when** | Object has distinct lifecycle states | You need interchangeable algorithms | You want to enforce a fixed flow |

---

**Next Step:** Open `01_Basic_State_Code.java` — a clean Vending Machine implementation!
