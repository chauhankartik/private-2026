package factory;

/**
 * Production-grade Java demonstration of Factory Method and Abstract Factory Patterns.
 * Scenario: Payment Processor Suite (Stripe & PayPal) with matching Receipt Generators.
 */
public class FactoryDemo {

    // --- Product Interfaces ---
    public interface PaymentProcessor {
        void processPayment(double amount);
    }

    public interface ReceiptGenerator {
        void generateReceipt(double amount);
    }

    // --- Concrete Products for Stripe ---
    public static class StripePaymentProcessor implements PaymentProcessor {
        @Override
        public void processPayment(double amount) {
            System.out.println("Processing $" + amount + " via Stripe Payment Gateway.");
        }
    }

    public static class StripeReceiptGenerator implements ReceiptGenerator {
        @Override
        public void generateReceipt(double amount) {
            System.out.println("Generating Stripe digital receipt for $" + amount);
        }
    }

    // --- Concrete Products for PayPal ---
    public static class PayPalPaymentProcessor implements PaymentProcessor {
        @Override
        public void processPayment(double amount) {
            System.out.println("Processing $" + amount + " via PayPal API.");
        }
    }

    public static class PayPalReceiptGenerator implements ReceiptGenerator {
        @Override
        public void generateReceipt(double amount) {
            System.out.println("Generating PayPal email invoice receipt for $" + amount);
        }
    }

    // --- Abstract Factory Interface ---
    public interface PaymentGatewayFactory {
        PaymentProcessor createPaymentProcessor();
        ReceiptGenerator createReceiptGenerator();
    }

    // --- Concrete Factories ---
    public static class StripeFactory implements PaymentGatewayFactory {
        @Override
        public PaymentProcessor createPaymentProcessor() {
            return new StripePaymentProcessor();
        }

        @Override
        public ReceiptGenerator createReceiptGenerator() {
            return new StripeReceiptGenerator();
        }
    }

    public static class PayPalFactory implements PaymentGatewayFactory {
        @Override
        public PaymentProcessor createPaymentProcessor() {
            return new PayPalPaymentProcessor();
        }

        @Override
        public ReceiptGenerator createReceiptGenerator() {
            return new PayPalReceiptGenerator();
        }
    }

    // --- Client Code ---
    public static class CheckoutService {
        private final PaymentProcessor processor;
        private final ReceiptGenerator receiptGenerator;

        public CheckoutService(PaymentGatewayFactory factory) {
            this.processor = factory.createPaymentProcessor();
            this.receiptGenerator = factory.createReceiptGenerator();
        }

        public void completeCheckout(double amount) {
            processor.processPayment(amount);
            receiptGenerator.generateReceipt(amount);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Abstract Factory Pattern Demo ===");

        // Checkout using Stripe Suite
        System.out.println("\n--- Stripe Checkout ---");
        CheckoutService stripeCheckout = new CheckoutService(new StripeFactory());
        stripeCheckout.completeCheckout(250.00);

        // Checkout using PayPal Suite
        System.out.println("\n--- PayPal Checkout ---");
        CheckoutService paypalCheckout = new CheckoutService(new PayPalFactory());
        paypalCheckout.completeCheckout(499.99);
    }
}
