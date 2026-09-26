package movie_booking_system;

import movie_booking_system.CoreModels.Show;
import movie_booking_system.CoreModels.ShowSeat;

import java.time.DayOfWeek;
import java.util.List;

public class PricingStrategy {

    public interface IPricingStrategy {
        double calculatePrice(Show show, List<ShowSeat> seats);
    }

    public interface IDiscountStrategy {
        double calculateDiscount(double originalPrice, String couponCode);
    }

    public static class DefaultPricingStrategy implements IPricingStrategy {
        private static final double TAX_RATE = 0.18; // 18% GST/Convenience Fee

        @Override
        public double calculatePrice(Show show, List<ShowSeat> seats) {
            double baseTotal = 0.0;
            for (ShowSeat ss : seats) {
                baseTotal += ss.getPrice();
            }
            double totalWithTax = baseTotal * (1.0 + TAX_RATE);
            return Math.round(totalWithTax * 100.0) / 100.0;
        }
    }

    public static class WeekendSurgePricingStrategy implements IPricingStrategy {
        private static final double WEEKEND_SURGE = 1.20; // 20% weekend surge
        private static final double TAX_RATE = 0.18;

        @Override
        public double calculatePrice(Show show, List<ShowSeat> seats) {
            double baseTotal = 0.0;
            for (ShowSeat ss : seats) {
                baseTotal += ss.getPrice();
            }

            DayOfWeek day = show.getStartTime().getDayOfWeek();
            boolean isWeekend = (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);

            if (isWeekend) {
                baseTotal *= WEEKEND_SURGE;
            }

            double totalWithTax = baseTotal * (1.0 + TAX_RATE);
            return Math.round(totalWithTax * 100.0) / 100.0;
        }
    }

    public static class CouponDiscountStrategy implements IDiscountStrategy {
        @Override
        public double calculateDiscount(double originalPrice, String couponCode) {
            if (couponCode == null || couponCode.trim().isEmpty()) {
                return 0.0;
            }

            switch (couponCode.toUpperCase()) {
                case "MOVIE50":
                    return Math.min(50.0, originalPrice * 0.50);
                case "WEEKEND20":
                    return originalPrice * 0.20;
                case "VIPFIRST":
                    return 100.0;
                default:
                    System.out.println("⚠️ Invalid or expired coupon code: " + couponCode);
                    return 0.0;
            }
        }
    }
}
