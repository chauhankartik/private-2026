package adapter;

/**
 * Production-grade Java demonstration of the Adapter Pattern (Object Adapter).
 * Scenario: Adapting a legacy 3rd-party PayPal SDK interface to our standard PaymentGateway interface.
 */
public class AdapterDemo {

    // --- Standard Target Interface ---
    public interface PaymentGateway {
        boolean processTransaction(String customerId, double amount);
    }

    // --- Modern System Implementation ---
    public static class StandardStripeService implements PaymentGateway {
        @Override
        public boolean processTransaction(String customerId, double amount) {
            System.out.println("[Stripe] Charging customer " + customerId + " amount $" + amount);
            return true;
        }
    }

    // --- Legacy 3rd-Party Adaptee (Incompatible Interface) ---
    public static class LegacyPayPalSDK {
        public void makePayment(double amountInCents, String userEmail) {
            System.out.println("[Legacy PayPal API] Executing payment for " + userEmail +
                    " in cents: " + (long)amountInCents);
        }
    }

    // --- Object Adapter Class ---
    public static class PayPalAdapter implements PaymentGateway {
        private final LegacyPayPalSDK payPalSDK;

        public PayPalAdapter(LegacyPayPalSDK payPalSDK) {
            this.payPalSDK = payPalSDK;
        }

        @Override
        public boolean processTransaction(String customerId, double amount) {
            // Translate customerId to email format & amount to cents
            String userEmail = customerId + "@example.com";
            double amountInCents = amount * 100;

            System.out.println("[Adapter] Translating Standard Call -> Legacy PayPal SDK");
            payPalSDK.makePayment(amountInCents, userEmail);
            return true;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Adapter Pattern Demo ===");

        PaymentGateway stripeGateway = new StandardStripeService();
        stripeGateway.processTransaction("user_101", 150.00);

        System.out.println();

        // Using Legacy PayPal through Adapter
        LegacyPayPalSDK legacySDK = new LegacyPayPalSDK();
        PaymentGateway paypalGateway = new PayPalAdapter(legacySDK);
        paypalGateway.processTransaction("user_202", 299.99);
    }
}
