/**
 * ============================================================
 *  BASIC STATE PATTERN — THE VENDING MACHINE
 * ============================================================
 *
 * The simplest, cleanest implementation.
 * Understand this FIRST before looking at anything advanced.
 *
 * The trick: Instead of if-else chains in every method,
 * delegate ALL behavior to the current state object.
 * Transitioning = swapping out the state reference.
 */

// ----------------------------------------------------
// 1. The State Interface
// ----------------------------------------------------
/**
 * Declares every action the VendingMachine can perform.
 * Every concrete state must implement ALL of these.
 *
 * Why pass the machine as a parameter?
 *   So the state can trigger transitions:
 *   state.pressDispense(machine) → machine.setState(machine.getIdleState())
 */
interface VendingMachineState {
    void insertCoin(VendingMachine machine);
    void pressDispense(VendingMachine machine);
    void refill(VendingMachine machine, int count);
}

// ----------------------------------------------------
// 2. The Context — VendingMachine
// ----------------------------------------------------
/**
 * Holds a reference to the current state.
 * All public method calls are delegated to the current state.
 * The context itself has NO if-else logic — just delegation.
 */
class VendingMachine {

    // All possible states — created once, reused (stateless singletons)
    private final VendingMachineState idleState;
    private final VendingMachineState hasCoinState;
    private final VendingMachineState soldOutState;

    private VendingMachineState currentState;
    private int itemCount;

    public VendingMachine(int initialItems) {
        this.idleState    = new IdleState();
        this.hasCoinState = new HasCoinState();
        this.soldOutState = new SoldOutState();

        this.itemCount    = initialItems;
        this.currentState = (initialItems > 0) ? idleState : soldOutState;

        System.out.println("[Machine] Initialized with " + initialItems + " items.");
        System.out.println("[Machine] Current state: " + currentState.getClass().getSimpleName());
    }

    // --- Delegating all actions to the current state ---
    public void insertCoin()              { currentState.insertCoin(this); }
    public void pressDispense()           { currentState.pressDispense(this); }
    public void refill(int count)         { currentState.refill(this, count); }

    // --- State transition & item management (called BY state objects) ---
    public void setState(VendingMachineState state) {
        this.currentState = state;
        System.out.println("[Machine] State → " + state.getClass().getSimpleName());
    }

    public void dispenseItem() {
        if (itemCount > 0) {
            itemCount--;
            System.out.println("[Machine] Item dispensed! Items remaining: " + itemCount);
        }
    }

    public void addItems(int count) {
        itemCount += count;
        System.out.println("[Machine] Restocked. Items now: " + itemCount);
    }

    public void returnCoin() {
        System.out.println("[Machine] Coin returned.");
    }

    // --- State access (so states can transition to each other) ---
    public VendingMachineState getIdleState()    { return idleState; }
    public VendingMachineState getHasCoinState() { return hasCoinState; }
    public VendingMachineState getSoldOutState() { return soldOutState; }
    public int getItemCount() { return itemCount; }
}

// ----------------------------------------------------
// 3. Concrete States — each handles ALL actions
// ----------------------------------------------------

/**
 * IDLE STATE: No coin inserted, items available.
 */
class IdleState implements VendingMachineState {

    @Override
    public void insertCoin(VendingMachine machine) {
        System.out.println("[Idle]     Coin accepted.");
        machine.setState(machine.getHasCoinState());
    }

    @Override
    public void pressDispense(VendingMachine machine) {
        System.out.println("[Idle]     Please insert a coin first.");
    }

    @Override
    public void refill(VendingMachine machine, int count) {
        machine.addItems(count);
        // Already in idle, stay here
    }
}

/**
 * HAS_COIN STATE: Coin inserted, waiting for dispense press.
 */
class HasCoinState implements VendingMachineState {

    @Override
    public void insertCoin(VendingMachine machine) {
        System.out.println("[HasCoin]  Coin already inserted — returning extra coin.");
        machine.returnCoin();
    }

    @Override
    public void pressDispense(VendingMachine machine) {
        System.out.println("[HasCoin]  Dispensing...");
        machine.dispenseItem();

        // Transition: go to SoldOut if no items left, else back to Idle
        if (machine.getItemCount() == 0) {
            System.out.println("[HasCoin]  Machine is now empty.");
            machine.setState(machine.getSoldOutState());
        } else {
            machine.setState(machine.getIdleState());
        }
    }

    @Override
    public void refill(VendingMachine machine, int count) {
        System.out.println("[HasCoin]  Cannot refill while transaction is in progress.");
    }
}

/**
 * SOLD_OUT STATE: No items left.
 */
class SoldOutState implements VendingMachineState {

    @Override
    public void insertCoin(VendingMachine machine) {
        System.out.println("[SoldOut]  Sorry, machine is empty. Coin returned.");
        machine.returnCoin();
    }

    @Override
    public void pressDispense(VendingMachine machine) {
        System.out.println("[SoldOut]  No items to dispense.");
    }

    @Override
    public void refill(VendingMachine machine, int count) {
        machine.addItems(count);
        machine.setState(machine.getIdleState());  // back in business!
    }
}

// ----------------------------------------------------
// 4. Let's Run It!
// ----------------------------------------------------
public class BasicStateDemo {
    public static void main(String[] args) {

        System.out.println("=== Vending Machine with 2 items ===\n");
        VendingMachine vm = new VendingMachine(2);

        System.out.println("\n--- Scenario 1: Normal purchase ---");
        vm.insertCoin();
        vm.pressDispense();    // item 1 dispensed, 1 remaining

        System.out.println("\n--- Scenario 2: Try dispensing without coin ---");
        vm.pressDispense();    // "insert a coin first"

        System.out.println("\n--- Scenario 3: Second purchase (last item) ---");
        vm.insertCoin();
        vm.pressDispense();    // item 2 dispensed → transitions to SoldOut

        System.out.println("\n--- Scenario 4: Try buying when sold out ---");
        vm.insertCoin();       // coin returned
        vm.pressDispense();    // "no items"

        System.out.println("\n--- Scenario 5: Refill and buy again ---");
        vm.refill(3);          // restocked → back to Idle
        vm.insertCoin();
        vm.pressDispense();    // works again!
    }
}

/**
 * 🧠 WHAT JUST HAPPENED?
 *
 * Notice: VendingMachine.insertCoin() always does:
 *   currentState.insertCoin(this);
 *
 * The SAME call produces COMPLETELY DIFFERENT results depending on
 * which object currentState points to. The machine "appears to change
 * its class" as it moves through states.
 *
 * State transition diagram:
 *
 *   [Idle] ──insertCoin──→ [HasCoin] ──pressDispense──→ [Idle]
 *                                              └──(last item)──→ [SoldOut]
 *   [SoldOut] ──refill──→ [Idle]
 *
 *
 * 💡 INTERVIEW TAKEAWAY:
 *   "I'd use the State pattern any time I see the phrase 'the behavior depends
 *    on the current status of the object.' Order lifecycle (PENDING → PAID →
 *    SHIPPED → DELIVERED → CANCELLED) is a perfect fit. Each state handles
 *    cancel() differently: PENDING cancels freely, SHIPPED requires a return
 *    request, DELIVERED cannot be cancelled at all."
 */
