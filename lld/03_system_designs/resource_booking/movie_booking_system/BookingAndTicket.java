package movie_booking_system;

import movie_booking_system.CoreModels.Show;
import movie_booking_system.CoreModels.ShowSeat;
import movie_booking_system.Enums.BookingStatus;
import movie_booking_system.Payment.PaymentRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BookingAndTicket {

    public static class Booking {
        private final String bookingId;
        private final String userId;
        private final Show show;
        private final List<ShowSeat> seats;
        private final double totalAmount;
        private double discountAmount;
        private double finalAmount;
        private BookingStatus status;
        private PaymentRecord paymentRecord;
        private final LocalDateTime createdAt;

        public Booking(String userId, Show show, List<ShowSeat> seats, double totalAmount) {
            this.bookingId = "BKG_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.userId = userId;
            this.show = show;
            this.seats = seats;
            this.totalAmount = totalAmount;
            this.discountAmount = 0.0;
            this.finalAmount = totalAmount;
            this.status = BookingStatus.PENDING_PAYMENT;
            this.createdAt = LocalDateTime.now();
        }

        public String getBookingId() { return bookingId; }
        public String getUserId() { return userId; }
        public Show getShow() { return show; }
        public List<ShowSeat> getSeats() { return seats; }
        public double getTotalAmount() { return totalAmount; }
        public double getDiscountAmount() { return discountAmount; }
        public double getFinalAmount() { return finalAmount; }
        public BookingStatus getStatus() { return status; }
        public PaymentRecord getPaymentRecord() { return paymentRecord; }

        public void applyDiscount(double discount) {
            this.discountAmount = discount;
            this.finalAmount = Math.max(0.0, this.totalAmount - discount);
        }

        public void confirmBooking(PaymentRecord paymentRecord) {
            this.paymentRecord = paymentRecord;
            this.status = BookingStatus.CONFIRMED;
        }

        public void cancelBooking() {
            this.status = BookingStatus.CANCELLED;
        }

        public void markExpired() {
            this.status = BookingStatus.EXPIRED;
        }
    }

    public static class Ticket {
        private final String ticketId;
        private final String bookingId;
        private final String movieTitle;
        private final String theatreName;
        private final String screenName;
        private final String showTime;
        private final List<String> seatNumbers;
        private final String qrCodeToken;
        private final double amountPaid;

        public Ticket(Booking booking) {
            this.ticketId = "TCK_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.bookingId = booking.getBookingId();
            this.movieTitle = booking.getShow().getMovie().getTitle();
            this.theatreName = booking.getShow().getTheatre().getName();
            this.screenName = booking.getShow().getScreen().getName();
            this.showTime = booking.getShow().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            this.seatNumbers = booking.getSeats().stream()
                    .map(ss -> ss.getSeat().getSeatId())
                    .collect(Collectors.toList());
            this.qrCodeToken = "QR_" + UUID.randomUUID();
            this.amountPaid = booking.getFinalAmount();
        }

        public void printTicket() {
            System.out.println("\n========================================================");
            System.out.println(" 🎟️  MOVIE TICKET  - " + ticketId);
            System.out.println("========================================================");
            System.out.println(" Movie    : " + movieTitle);
            System.out.println(" Theatre  : " + theatreName + " (" + screenName + ")");
            System.out.println(" Time     : " + showTime);
            System.out.println(" Seats    : " + String.join(", ", seatNumbers));
            System.out.printf(" Total    : $%.2f%n", amountPaid);
            System.out.println(" QR Token : " + qrCodeToken);
            System.out.println("========================================================\n");
        }
    }

    public static class NotificationService {
        public static void sendBookingConfirmation(String userId, Ticket ticket) {
            System.out.printf("📩 [NOTIFICATION to %s] Ticket Confirmed for %s! Ticket ID: %s%n",
                    userId, ticket.movieTitle, ticket.ticketId);
        }

        public static void sendCancellationNotification(String userId, String bookingId, double refundAmount) {
            System.out.printf("📩 [NOTIFICATION to %s] Booking %s Cancelled. Refund of $%.2f initiated.%n",
                    userId, bookingId, refundAmount);
        }
    }
}
