package strategy;

/**
 * Production-grade Java demonstration of the Strategy Pattern.
 * Scenario: Dynamic E-Commerce Payment Processing Engine.
 */
public class StrategyDemo {

    // --- Strategy Interface ---
    public interface PaymentStrategy {
        boolean pay(double amount);
    }

    // --- Concrete Strategies ---
    public static class CreditCardStrategy implements PaymentStrategy {
        private final String cardNumber;

        public CreditCardStrategy(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        @Override
        public boolean pay(double amount) {
            System.out.println("Paid $" + amount + " using Credit Card ending in " +
                    cardNumber.substring(cardNumber.length() - 4));
            return true;
        }
    }

    public static class UpiPaymentStrategy implements PaymentStrategy {
        private final String upiId;

        public UpiPaymentStrategy(String upiId) {
            this.upiId = upiId;
        }

        @Override
        public boolean pay(double amount) {
            System.out.println("Paid $" + amount + " using UPI ID: " + upiId);
            return true;
        }
    }

    public static class CryptoPaymentStrategy implements PaymentStrategy {
        private final String walletAddress;

        public CryptoPaymentStrategy(String walletAddress) {
            this.walletAddress = walletAddress;
        }

        @Override
        public boolean pay(double amount) {
            System.out.println("Paid $" + amount + " using Crypto Wallet: " + walletAddress);
            return true;
        }
    }

    // --- Context Class ---
    public static class ShoppingCart {
        private PaymentStrategy paymentStrategy;

        public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }

        public void checkout(double totalAmount) {
            if (paymentStrategy == null) {
                throw new IllegalStateException("Payment strategy not set!");
            }
            paymentStrategy.pay(totalAmount);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Strategy Pattern Demo ===");

        ShoppingCart cart = new ShoppingCart();

        // Pay via Credit Card
        cart.setPaymentStrategy(new CreditCardStrategy("4111-2222-3333-4444"));
        cart.checkout(199.99);

        // Pay via UPI
        cart.setPaymentStrategy(new UpiPaymentStrategy("user@upi"));
        cart.checkout(49.50);

        // Pay via Crypto
        cart.setPaymentStrategy(new CryptoPaymentStrategy("0x123...abc"));
        cart.checkout(1200.00);
    }
}
