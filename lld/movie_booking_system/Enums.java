package movie_booking_system;

public class Enums {

    public enum City {
        BANGALORE,
        MUMBAI,
        DELHI,
        HYDERABAD,
        CHENNAI
    }

    public enum Genre {
        ACTION,
        COMEDY,
        DRAMA,
        THRILLER,
        SCI_FI,
        ROMANCE,
        HORROR,
        ANIMATION
    }

    public enum MovieRating {
        G,
        PG,
        PG13,
        R,
        UA,
        A
    }

    public enum SeatType {
        REGULAR(150.0),
        PREMIUM(250.0),
        RECLINER(400.0),
        VIP(600.0);

        private final double basePrice;

        SeatType(double basePrice) {
            this.basePrice = basePrice;
        }

        public double getBasePrice() {
            return basePrice;
        }
    }

    public enum SeatStatus {
        AVAILABLE,
        TEMPORARILY_BLOCKED,
        BOOKED
    }

    public enum ShowFormat {
        FORMAT_2D(1.0),
        FORMAT_3D(1.25),
        IMAX_3D(1.60),
        FORMAT_4DX(1.80);

        private final double priceMultiplier;

        ShowFormat(double priceMultiplier) {
            this.priceMultiplier = priceMultiplier;
        }

        public double getPriceMultiplier() {
            return priceMultiplier;
        }
    }

    public enum BookingStatus {
        CREATED,
        PENDING_PAYMENT,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }

    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED,
        REFUNDED
    }

    public enum PaymentMode {
        UPI,
        CREDIT_CARD,
        DEBIT_CARD,
        NET_BANKING,
        WALLET
    }
}
