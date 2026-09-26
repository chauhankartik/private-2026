/**
 * ============================================================
 *  INTERMEDIATE FACADE PATTERN — E-COMMERCE ORDER SYSTEM
 * ============================================================
 *
 * A realistic backend example you can talk about in any interview.
 * Shows: dependency injection, multiple facades, result objects,
 * and error handling inside the facade.
 *
 * Subsystems: InventoryService, PaymentGateway, ShippingService,
 *             NotificationService
 * Facade:     OrderService (the single entry point for placing orders)
 */
import java.util.*;

// ----------------------------------------------------
// 1. Domain Model
// ----------------------------------------------------
class Order {
    final String orderId;
    final String userId;
    final String productId;
    final int quantity;
    final double price;

    Order(String userId, String productId, int quantity, double price) {
        this.orderId   = "ORD-" + System.currentTimeMillis();
        this.userId    = userId;
        this.productId = productId;
        this.quantity  = quantity;
        this.price     = price;
    }
}

class OrderResult {
    final boolean success;
    final String orderId;
    final String message;
    final String trackingId;

    OrderResult(boolean success, String orderId, String message, String trackingId) {
        this.success    = success;
        this.orderId    = orderId;
        this.message    = message;
        this.trackingId = trackingId;
    }
}

// ----------------------------------------------------
// 2. Subsystem Classes — each has ONE job
// ----------------------------------------------------
class InventoryService {

    private final Map<String, Integer> stock = new HashMap<>(Map.of(
        "PHONE-001", 10,
        "LAPTOP-001", 3,
        "HEADSET-001", 0   // out of stock
    ));

    public boolean isAvailable(String productId, int quantity) {
        int available = stock.getOrDefault(productId, 0);
        boolean ok = available >= quantity;
        System.out.println("[Inventory]  " + productId + " — stock: " + available
            + (ok ? " ✓ available" : " ✗ insufficient"));
        return ok;
    }

    public void reserve(String productId, int quantity) {
        stock.merge(productId, -quantity, Integer::sum);
        System.out.println("[Inventory]  Reserved " + quantity + "x " + productId);
    }

    public void release(String productId, int quantity) {
        stock.merge(productId, quantity, Integer::sum);
        System.out.println("[Inventory]  Released " + quantity + "x " + productId + " (rollback)");
    }
}

class PaymentGateway {

    // Simulates a real payment processor (Stripe, Razorpay, etc.)
    public boolean charge(String userId, double amount) {
        System.out.println("[Payment]    Charging ₹" + amount + " to user " + userId);
        // Simulate failure for negative amounts
        if (amount <= 0) {
            System.out.println("[Payment]    ✗ Charge failed — invalid amount");
            return false;
        }
        System.out.println("[Payment]    ✓ Payment successful");
        return true;
    }

    public void refund(String userId, double amount) {
        System.out.println("[Payment]    Refunding ₹" + amount + " to user " + userId);
    }
}

class ShippingService {

    public String createShipment(Order order) {
        String trackingId = "TRACK-" + order.orderId;
        System.out.println("[Shipping]   Shipment created — tracking: " + trackingId);
        return trackingId;
    }
}

class NotificationService {

    public void sendOrderConfirmation(String userId, String orderId) {
        System.out.println("[Notify]     Email + SMS sent to user " + userId
            + " for order " + orderId);
    }

    public void sendFailureAlert(String userId, String reason) {
        System.out.println("[Notify]     Failure alert sent to user " + userId
            + " — reason: " + reason);
    }
}

// ----------------------------------------------------
// 3. The Facade — OrderService
// ----------------------------------------------------
/**
 * This is the ONLY class the REST controller (or client) talks to.
 *
 * Key responsibilities:
 *   1. Coordinate the correct sequence of subsystem calls.
 *   2. Handle rollback if any step fails (compensating transactions).
 *   3. Return a clean result object to the caller.
 *
 * The REST controller has zero coupling to InventoryService,
 * PaymentGateway, ShippingService, or NotificationService.
 */
class OrderService {

    private final InventoryService  inventory;
    private final PaymentGateway    payment;
    private final ShippingService   shipping;
    private final NotificationService notifications;

    // Dependency injection — subsystems are injected, not created inside
    // (Makes the facade testable: inject mocks in tests)
    public OrderService(InventoryService inventory,
                        PaymentGateway payment,
                        ShippingService shipping,
                        NotificationService notifications) {
        this.inventory     = inventory;
        this.payment       = payment;
        this.shipping      = shipping;
        this.notifications = notifications;
    }

    /**
     * High-level operation: places an order.
     * The client knows nothing about the steps below.
     *
     * Steps (with rollback on failure):
     *   1. Check inventory
     *   2. Reserve inventory
     *   3. Process payment     ← if fails → release inventory
     *   4. Create shipment     ← if fails → refund + release inventory
     *   5. Send notifications
     */
    public OrderResult placeOrder(String userId, String productId, int quantity, double price) {
        System.out.println("\n>> Processing order for user=" + userId + " product=" + productId);

        Order order = new Order(userId, productId, quantity, price);

        // Step 1: Check inventory
        if (!inventory.isAvailable(productId, quantity)) {
            notifications.sendFailureAlert(userId, "Out of stock");
            return new OrderResult(false, null, "Product out of stock", null);
        }

        // Step 2: Reserve inventory
        inventory.reserve(productId, quantity);

        // Step 3: Process payment (with rollback)
        boolean paid = payment.charge(userId, price * quantity);
        if (!paid) {
            inventory.release(productId, quantity);    // ROLLBACK inventory
            notifications.sendFailureAlert(userId, "Payment failed");
            return new OrderResult(false, null, "Payment failed", null);
        }

        // Step 4: Create shipment (with rollback)
        String trackingId;
        try {
            trackingId = shipping.createShipment(order);
        } catch (Exception e) {
            payment.refund(userId, price * quantity);   // ROLLBACK payment
            inventory.release(productId, quantity);     // ROLLBACK inventory
            notifications.sendFailureAlert(userId, "Shipping error");
            return new OrderResult(false, null, "Shipment creation failed", null);
        }

        // Step 5: Notify customer
        notifications.sendOrderConfirmation(userId, order.orderId);

        System.out.println(">> Order " + order.orderId + " placed successfully!\n");
        return new OrderResult(true, order.orderId, "Order placed!", trackingId);
    }
}

// ----------------------------------------------------
// 4. Client (e.g., a REST Controller)
// ----------------------------------------------------
public class IntermediateFacadeDemo {
    public static void main(String[] args) {

        // In a Spring app, these would be @Autowired
        OrderService orderService = new OrderService(
            new InventoryService(),
            new PaymentGateway(),
            new ShippingService(),
            new NotificationService()
        );

        // === Case 1: Successful Order ===
        System.out.println("=== CASE 1: Successful Order ===");
        OrderResult r1 = orderService.placeOrder("user-42", "PHONE-001", 2, 29999.0);
        System.out.println("Result: " + r1.message + " | Tracking: " + r1.trackingId);

        // === Case 2: Out of Stock ===
        System.out.println("=== CASE 2: Out of Stock ===");
        OrderResult r2 = orderService.placeOrder("user-43", "HEADSET-001", 1, 1999.0);
        System.out.println("Result: " + r2.message);

        // === Case 3: Payment Failure ===
        System.out.println("=== CASE 3: Payment Failure (negative price) ===");
        OrderResult r3 = orderService.placeOrder("user-44", "LAPTOP-001", 1, -1.0);
        System.out.println("Result: " + r3.message);
    }
}

/**
 * 🧠 KEY OBSERVATIONS
 *
 * 1. ROLLBACK COORDINATION:
 *    The facade handles the compensating transactions.
 *    The client never knows this complexity exists.
 *    Without a facade, every client would have to implement rollback logic.
 *
 * 2. TESTABILITY:
 *    Because subsystems are injected, in tests you can do:
 *      OrderService service = new OrderService(
 *          mockInventory, mockPayment, mockShipping, mockNotifications
 *      );
 *    No mocking of static calls or singletons needed.
 *
 * 3. SINGLE CHANGE POINT:
 *    If PaymentGateway changes from Stripe to Razorpay, only the
 *    PaymentGateway class changes. OrderService and the REST controller
 *    are completely unaffected.
 *
 * 4. REAL SPRING MAPPING:
 *    @RestController → calls → @Service (this Facade)
 *                              calls → @Repository / other @Services (subsystems)
 */
