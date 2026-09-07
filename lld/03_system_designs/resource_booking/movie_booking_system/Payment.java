package movie_booking_system;

import movie_booking_system.Enums.PaymentMode;
import movie_booking_system.Enums.PaymentStatus;

import java.util.UUID;

public class Payment {

    public static class PaymentRecord {
        private final String paymentId;
        private final String bookingId;
        private final double amount;
        private final PaymentMode paymentMode;
        private PaymentStatus status;

        public PaymentRecord(String bookingId, double amount, PaymentMode paymentMode) {
            this.paymentId = "PAY_" + UUID.randomUUID().toString().substring(0, 8);
            this.bookingId = bookingId;
            this.amount = amount;
            this.paymentMode = paymentMode;
            this.status = PaymentStatus.PENDING;
        }

        public String getPaymentId() { return paymentId; }
        public String getBookingId() { return bookingId; }
        public double getAmount() { return amount; }
        public PaymentMode getPaymentMode() { return paymentMode; }
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
    }

    public interface IPaymentProcessor {
        PaymentRecord processPayment(String bookingId, double amount, PaymentMode mode);
        boolean processRefund(PaymentRecord payment);
    }

    public static class UPIPaymentProcessor implements IPaymentProcessor {
        @Override
        public PaymentRecord processPayment(String bookingId, double amount, PaymentMode mode) {
            System.out.printf("💳 Processing UPI Payment of $%.2f for Booking [%s]...%n", amount, bookingId);
            PaymentRecord record = new PaymentRecord(bookingId, amount, mode);
            // Simulate gateway processing success
            record.setStatus(PaymentStatus.SUCCESS);
            System.out.printf("✅ UPI Payment Successful! Transaction ID: %s%n", record.getPaymentId());
            return record;
        }

        @Override
        public boolean processRefund(PaymentRecord payment) {
            System.out.printf("🔄 Processing UPI Refund of $%.2f for Txn [%s]...%n", payment.getAmount(), payment.getPaymentId());
            payment.setStatus(PaymentStatus.REFUNDED);
            System.out.println("✅ UPI Refund Processed Successfully.");
            return true;
        }
    }

    public static class CardPaymentProcessor implements IPaymentProcessor {
        @Override
        public PaymentRecord processPayment(String bookingId, double amount, PaymentMode mode) {
            System.out.printf("💳 Processing Card Payment of $%.2f for Booking [%s]...%n", amount, bookingId);
            PaymentRecord record = new PaymentRecord(bookingId, amount, mode);
            record.setStatus(PaymentStatus.SUCCESS);
            System.out.printf("✅ Card Payment Successful! Transaction ID: %s%n", record.getPaymentId());
            return record;
        }

        @Override
        public boolean processRefund(PaymentRecord payment) {
            System.out.printf("🔄 Processing Card Refund of $%.2f for Txn [%s]...%n", payment.getAmount(), payment.getPaymentId());
            payment.setStatus(PaymentStatus.REFUNDED);
            System.out.println("✅ Card Refund Processed Successfully.");
            return true;
        }
    }

    public static class PaymentProcessorFactory {
        public static IPaymentProcessor getProcessor(PaymentMode mode) {
            switch (mode) {
                case UPI:
                    return new UPIPaymentProcessor();
                case CREDIT_CARD:
                case DEBIT_CARD:
                    return new CardPaymentProcessor();
                default:
                    return new UPIPaymentProcessor();
            }
        }
    }
}
