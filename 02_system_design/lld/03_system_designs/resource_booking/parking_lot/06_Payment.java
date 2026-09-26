// =============================================================================
// FILE: 06_Payment.java
// TOPIC: Parking Lot LLD — Payment Methods
// PATTERNS: Strategy (PaymentMethod interface + impls)
// SOLID: ISP (small interface), OCP (new payment = new class), DIP
// =============================================================================

package parking_lot;

// =============================================================================
// ■ PaymentMethod — Strategy interface for processing payments
//
// ISP: one method only — pay(amount). No extra lifecycle methods.
// OCP: adding UPI/Crypto/QR requires zero changes to existing code.
// DIP: ParkingLot depends on PaymentMethod, not on CashPayment specifically.
// =============================================================================
interface PaymentMethod {

    /**
     * Process a payment of the given amount.
     *
     * @param amount amount to charge in local currency (₹)
     * @return       COMPLETED on success, FAILED on failure
     */
    PaymentStatus pay(double amount);

    /** Human-readable name of this payment method (for receipts). */
    String getMethodName();
}


// =============================================================================
// ■ CashPayment — Physical currency payment at kiosk
//
// Simulates cash tendering: validates amount tendered, gives change.
// In production: integrates with cash counting machine hardware API.
// =============================================================================
class CashPayment implements PaymentMethod {

    private final double amountTendered;   // how much cash the driver gave

    CashPayment(double amountTendered) {
        this.amountTendered = amountTendered;
    }

    @Override
    public PaymentStatus pay(double amount) {
        if (amountTendered < amount) {
            System.out.printf("  [CASH] Insufficient: need ₹%.2f, got ₹%.2f%n",
                    amount, amountTendered);
            return PaymentStatus.FAILED;
        }
        double change = amountTendered - amount;
        System.out.printf("  [CASH] Paid ₹%.2f. Change: ₹%.2f%n", amount, change);
        return PaymentStatus.COMPLETED;
    }

    @Override
    public String getMethodName() { return "CASH"; }
}


// =============================================================================
// ■ CardPayment — Credit / Debit card via payment terminal
//
// Simulates card authorization: validates card number format, processes charge.
// In production: integrates with payment gateway (Razorpay, Stripe, etc.)
// =============================================================================
class CardPayment implements PaymentMethod {

    private final String cardNumber;     // last 4 digits shown on receipt
    private final String cardHolderName;

    CardPayment(String cardNumber, String cardHolderName) {
        if (cardNumber == null || cardNumber.length() < 4)
            throw new IllegalArgumentException("Invalid card number");
        this.cardNumber     = cardNumber;
        this.cardHolderName = cardHolderName;
    }

    @Override
    public PaymentStatus pay(double amount) {
        // Simulate authorization (in production: call payment gateway API)
        String maskedCard = "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
        System.out.printf("  [CARD] Charging ₹%.2f to %s (%s)%n",
                amount, maskedCard, cardHolderName);

        // Simulate ~95% success rate (in a real system, gateway returns status)
        boolean authorized = !cardNumber.endsWith("0000");   // mock decline condition
        if (authorized) {
            System.out.println("  [CARD] Authorization: APPROVED");
            return PaymentStatus.COMPLETED;
        } else {
            System.out.println("  [CARD] Authorization: DECLINED");
            return PaymentStatus.FAILED;
        }
    }

    @Override
    public String getMethodName() { return "CARD"; }
}


// =============================================================================
// ■ UPIPayment — Unified Payment Interface (QR code / VPA)
//
// India-specific fast payment. Driver scans QR or enters UPI ID.
// In production: integrates with UPI payment rails via bank API.
// =============================================================================
class UPIPayment implements PaymentMethod {

    private final String upiId;         // e.g., "driver@paytm"

    UPIPayment(String upiId) {
        if (upiId == null || !upiId.contains("@"))
            throw new IllegalArgumentException("Invalid UPI ID: " + upiId);
        this.upiId = upiId;
    }

    @Override
    public PaymentStatus pay(double amount) {
        System.out.printf("  [UPI] Sending ₹%.2f request to %s%n", amount, upiId);
        // Simulate UPI payment success (in production: poll for payment confirmation)
        System.out.printf("  [UPI] Payment of ₹%.2f received from %s%n", amount, upiId);
        return PaymentStatus.COMPLETED;
    }

    @Override
    public String getMethodName() { return "UPI(" + upiId + ")"; }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why Strategy for payment instead of an if-else block?
// A: if-else violates OCP — adding PayPal or Crypto requires modifying the
//    existing payment processing method. With Strategy, adding a new payment
//    method = writing a new class that implements PaymentMethod. ParkingLot
//    never changes. The client at the gate passes in whichever PaymentMethod
//    the driver chose — ParkingLot calls pay() without caring which type.
//
// Q: How would you add payment retry logic?
// A: Wrap the original PaymentMethod in a RetryPaymentDecorator:
//    class RetryPaymentDecorator implements PaymentMethod {
//        PaymentStatus pay(double amount) {
//            for (int i = 0; i < maxRetries; i++) {
//                PaymentStatus s = base.pay(amount);
//                if (s == COMPLETED) return s;
//            }
//            return FAILED;
//        }
//    }
//    This is the Decorator pattern on PaymentMethod — same as how
//    AsyncAppender decorated LogAppender in the Logging Framework LLD.
//
// Q: What if payment succeeds but the gate doesn't open (system crash)?
// A: This is the distributed transaction problem. Solutions:
//    1. Two-phase commit: lock spot, pay, then release gate + spot atomically.
//    2. Saga pattern: compensating transaction (refund) if gate opening fails.
//    3. Idempotency: retrying the payment is safe if each request has a unique
//       idempotency key — payment gateway rejects duplicate charges.
//    For the LLD scope, mention this as a production concern and suggest
//    storing payment status in a durable log before releasing the gate.
