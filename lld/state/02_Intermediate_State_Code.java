/**
 * ============================================================
 *  INTERMEDIATE STATE PATTERN — ORDER LIFECYCLE
 * ============================================================
 *
 * A realistic backend example: an e-commerce Order moving through
 * its full lifecycle with different rules at each state.
 *
 * States:  PENDING → PAID → SHIPPED → DELIVERED
 *               ↘ CANCELLED (allowed from PENDING and PAID only)
 *
 * Key additions over Basic:
 *   - State objects hold a reference to the context directly
 *     (alternative to passing machine as parameter)
 *   - Illegal transitions throw typed exceptions
 *   - State enum for serialization-friendly identity
 *   - History log of state transitions
 */
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ----------------------------------------------------
// 1. Domain Model
// ----------------------------------------------------
class OrderItem {
    final String name;
    final double price;
    final int    quantity;

    OrderItem(String name, double price, int quantity) {
        this.name     = name;
        this.price    = price;
        this.quantity = quantity;
    }

    double total() { return price * quantity; }
}

// ----------------------------------------------------
// 2. Transition record (for audit log)
// ----------------------------------------------------
class StateTransition {
    final String         from;
    final String         to;
    final String         triggeredBy;
    final LocalDateTime  at;

    StateTransition(String from, String to, String triggeredBy) {
        this.from        = from;
        this.to          = to;
        this.triggeredBy = triggeredBy;
        this.at          = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("  %s → %s  (by: %s at %s)", from, to, triggeredBy, at);
    }
}

// ----------------------------------------------------
// 3. State Interface
// ----------------------------------------------------
interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void deliver(Order order);
    void cancel(Order order, String reason);
    void refund(Order order);
    String name();    // human-readable name for display / persistence
}

// ----------------------------------------------------
// 4. The Context — Order
// ----------------------------------------------------
class Order {

    private final String          orderId;
    private final String          customerId;
    private final List<OrderItem> items;
    private final List<StateTransition> history = new ArrayList<>();

    // All state singletons
    private final OrderState pendingState   = new PendingState();
    private final OrderState paidState      = new PaidState();
    private final OrderState shippedState   = new ShippedState();
    private final OrderState deliveredState = new DeliveredState();
    private final OrderState cancelledState = new CancelledState();

    private OrderState currentState;

    public Order(String orderId, String customerId, List<OrderItem> items) {
        this.orderId     = orderId;
        this.customerId  = customerId;
        this.items       = items;
        this.currentState = pendingState;
        System.out.println("[Order " + orderId + "] Created in state: PENDING");
    }

    // --- Delegate all lifecycle actions to current state ---
    public void pay()                          { currentState.pay(this); }
    public void ship()                         { currentState.ship(this); }
    public void deliver()                      { currentState.deliver(this); }
    public void cancel(String reason)          { currentState.cancel(this, reason); }
    public void refund()                       { currentState.refund(this); }

    // --- Called BY state objects to drive transitions ---
    public void transitionTo(OrderState newState, String action) {
        StateTransition transition = new StateTransition(
            currentState.name(), newState.name(), action
        );
        history.add(transition);
        currentState = newState;
        System.out.println("[Order " + orderId + "] " + transition.from
            + " → " + transition.to + "  (" + action + ")");
    }

    // --- State access ---
    public OrderState getPendingState()   { return pendingState; }
    public OrderState getPaidState()      { return paidState; }
    public OrderState getShippedState()   { return shippedState; }
    public OrderState getDeliveredState() { return deliveredState; }
    public OrderState getCancelledState() { return cancelledState; }

    // --- Queries ---
    public String getOrderId()     { return orderId; }
    public String getCustomerId()  { return customerId; }
    public String getStateName()   { return currentState.name(); }

    public double getTotal() {
        return items.stream().mapToDouble(OrderItem::total).sum();
    }

    public void printHistory() {
        System.out.println("\n[Order " + orderId + "] State History:");
        history.forEach(System.out::println);
    }
}

// ----------------------------------------------------
// 5. Concrete States
// ----------------------------------------------------

class PendingState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("  [Pending] Processing payment of ₹" + order.getTotal() + "...");
        // In real code: call PaymentGateway here
        System.out.println("  [Pending] Payment successful.");
        order.transitionTo(order.getPaidState(), "payment confirmed");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Cannot ship an unpaid order!");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Cannot deliver an unshipped order!");
    }

    @Override
    public void cancel(Order order, String reason) {
        System.out.println("  [Pending] Order cancelled before payment. Reason: " + reason);
        order.transitionTo(order.getCancelledState(), "cancelled: " + reason);
    }

    @Override
    public void refund(Order order) {
        System.out.println("  [Pending] No payment made — nothing to refund.");
    }

    @Override
    public String name() { return "PENDING"; }
}

class PaidState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("  [Paid]    Order is already paid.");
    }

    @Override
    public void ship(Order order) {
        System.out.println("  [Paid]    Generating shipping label...");
        // In real code: call ShippingService here
        System.out.println("  [Paid]    Handed to courier.");
        order.transitionTo(order.getShippedState(), "shipped to courier");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Cannot deliver before shipping!");
    }

    @Override
    public void cancel(Order order, String reason) {
        System.out.println("  [Paid]    Cancelling paid order. Initiating refund...");
        order.transitionTo(order.getCancelledState(), "cancelled after payment: " + reason);
        // Trigger refund
        order.refund();
    }

    @Override
    public void refund(Order order) {
        System.out.println("  [Paid]    Refunding ₹" + order.getTotal()
            + " to customer " + order.getCustomerId());
    }

    @Override
    public String name() { return "PAID"; }
}

class ShippedState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("  [Shipped] Already paid and shipped.");
    }

    @Override
    public void ship(Order order) {
        System.out.println("  [Shipped] Order already shipped.");
    }

    @Override
    public void deliver(Order order) {
        System.out.println("  [Shipped] Delivery confirmed by courier.");
        order.transitionTo(order.getDeliveredState(), "delivery confirmed");
    }

    @Override
    public void cancel(Order order, String reason) {
        System.out.println("  [Shipped] Cannot cancel — order is already in transit.");
        System.out.println("  [Shipped] Please wait for delivery, then initiate a return.");
    }

    @Override
    public void refund(Order order) {
        System.out.println("  [Shipped] Refunds available only after delivery. Please return the item.");
    }

    @Override
    public String name() { return "SHIPPED"; }
}

class DeliveredState implements OrderState {

    @Override
    public void pay(Order order)        { System.out.println("  [Delivered] Order closed."); }
    @Override
    public void ship(Order order)       { System.out.println("  [Delivered] Order already delivered."); }
    @Override
    public void deliver(Order order)    { System.out.println("  [Delivered] Already delivered."); }

    @Override
    public void cancel(Order order, String reason) {
        System.out.println("  [Delivered] Order delivered — cannot cancel. Initiate a return request.");
    }

    @Override
    public void refund(Order order) {
        System.out.println("  [Delivered] Return approved. Refunding ₹" + order.getTotal()
            + " after item pickup.");
    }

    @Override
    public String name() { return "DELIVERED"; }
}

class CancelledState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("  [Cancelled] Cannot pay for a cancelled order.");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Order is cancelled — cannot ship!");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Order is cancelled — cannot deliver!");
    }

    @Override
    public void cancel(Order order, String reason) {
        System.out.println("  [Cancelled] Order is already cancelled.");
    }

    @Override
    public void refund(Order order) {
        // Refund is handled inline in PaidState.cancel() — this is a no-op
    }

    @Override
    public String name() { return "CANCELLED"; }
}

// ----------------------------------------------------
// 6. Let's Run It!
// ----------------------------------------------------
public class IntermediateStateDemo {
    public static void main(String[] args) {

        List<OrderItem> items = List.of(
            new OrderItem("Phone", 29999.0, 1),
            new OrderItem("Case",  499.0,   2)
        );

        // === Scenario 1: Happy path ===
        System.out.println("\n====== SCENARIO 1: Happy Path ======");
        Order order1 = new Order("ORD-001", "user-42", items);
        order1.pay();
        order1.ship();
        order1.deliver();
        order1.refund();    // return after delivery
        order1.printHistory();

        // === Scenario 2: Cancel before payment ===
        System.out.println("\n====== SCENARIO 2: Cancel Before Payment ======");
        Order order2 = new Order("ORD-002", "user-43", items);
        order2.cancel("Changed my mind");
        order2.pay();       // should not work
        order2.printHistory();

        // === Scenario 3: Cancel after payment ===
        System.out.println("\n====== SCENARIO 3: Cancel After Payment (with refund) ======");
        Order order3 = new Order("ORD-003", "user-44", items);
        order3.pay();
        order3.cancel("Price dropped elsewhere");
        order3.printHistory();

        // === Scenario 4: Try to cancel in transit ===
        System.out.println("\n====== SCENARIO 4: Too Late to Cancel (shipped) ======");
        Order order4 = new Order("ORD-004", "user-45", items);
        order4.pay();
        order4.ship();
        order4.cancel("Impulse buy");  // not allowed after shipping
        order4.printHistory();
    }
}

/**
 * 🧠 KEY OBSERVATIONS
 *
 * 1. ILLEGAL TRANSITIONS THROW EXCEPTIONS:
 *    The state object ENFORCES the rules. There's no if-else in Order.ship().
 *    Each state knows what's legal from its perspective.
 *
 * 2. TRANSITION AUDIT LOG:
 *    Order.transitionTo() always records who triggered the change and when.
 *    This is critical for production systems (GDPR, disputes, analytics).
 *
 * 3. STATES CAN CALL OTHER METHODS ON THE CONTEXT:
 *    PaidState.cancel() → transitions to Cancelled → then calls order.refund()
 *    This chained behavior is natural with the State pattern.
 *
 * 4. PERSISTENCE HOOK:
 *    currentState.name() returns a string ("PENDING", "PAID", etc.)
 *    Easy to store in a database column and restore with:
 *      switch(dbColumn) {
 *        case "PENDING" → pendingState; ...
 *      }
 */
