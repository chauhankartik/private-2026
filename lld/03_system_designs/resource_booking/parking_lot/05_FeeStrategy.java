// =============================================================================
// FILE: 05_FeeStrategy.java
// TOPIC: Parking Lot LLD — Fee Calculation Strategies
// PATTERNS: Strategy (FeeStrategy interface + concrete implementations)
// SOLID: OCP — new pricing model = new class, zero change to ParkingLot
// =============================================================================

package parking_lot;

import java.time.Duration;

// =============================================================================
// ■ FeeStrategy — Strategy interface for fee computation
//
// Receives the parking duration; returns the fee amount.
// ParkingLot delegates all fee math to this interface — it doesn't
// contain any pricing logic itself (SRP + DIP).
//
// To change pricing: inject a different FeeStrategy at construction time.
// No need to modify ParkingLot or any other class (OCP).
// =============================================================================
interface FeeStrategy {

    /**
     * Calculate the parking fee for the given duration.
     *
     * @param duration how long the vehicle was parked
     * @return         fee in local currency units (₹)
     */
    double calculate(Duration duration);

    /** Human-readable name of this pricing model (for receipts / display). */
    String getName();
}


// =============================================================================
// ■ HourlyFeeStrategy — Standard time-based pricing
//
// Fee = CEILING(hours) * ratePerHour
// Example: 2.5 hours * ₹50/hr → ceil(2.5) = 3 hours → ₹150
//
// Use case: Most common parking lot model (malls, airports, offices).
// =============================================================================
class HourlyFeeStrategy implements FeeStrategy {

    private final double ratePerHour;
    private final double minimumFee;   // minimum charge (e.g., first 30 min free)

    HourlyFeeStrategy(double ratePerHour) {
        this(ratePerHour, 0.0);
    }

    HourlyFeeStrategy(double ratePerHour, double minimumFee) {
        if (ratePerHour < 0) throw new IllegalArgumentException("rate must be >= 0");
        this.ratePerHour = ratePerHour;
        this.minimumFee  = minimumFee;
    }

    @Override
    public double calculate(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return minimumFee;
        }
        // Ceiling division: round up to next hour
        long hours = (long) Math.ceil(duration.toMinutes() / 60.0);
        double fee = hours * ratePerHour;
        return Math.max(fee, minimumFee);    // never charge less than minimum
    }

    @Override
    public String getName() { return "Hourly(₹" + ratePerHour + "/hr)"; }
}


// =============================================================================
// ■ FlatFeeStrategy — Fixed charge regardless of duration
//
// Fee = fixedAmount (always, whether parked 10 min or 10 hours)
//
// Use case: Event parking (concerts, sports), daily capped lots.
// =============================================================================
class FlatFeeStrategy implements FeeStrategy {

    private final double fixedAmount;

    FlatFeeStrategy(double fixedAmount) {
        if (fixedAmount < 0) throw new IllegalArgumentException("amount must be >= 0");
        this.fixedAmount = fixedAmount;
    }

    @Override
    public double calculate(Duration duration) {
        return fixedAmount;   // ignores duration entirely
    }

    @Override
    public String getName() { return "Flat(₹" + fixedAmount + ")"; }
}


// =============================================================================
// ■ TieredFeeStrategy — Progressive pricing with time-based tiers
//
// Tiers:
//   0  – 2  hours:  ₹firstTierRate/hr
//   2  – 6  hours:  ₹secondTierRate/hr  (discounted — incentivizes longer stays)
//   6+ hours:       ₹thirdTierRate/hr   (best rate — all-day parkers)
//   Max daily cap:  ₹dailyCap (e.g., ₹200 — you never pay more than this)
//
// Use case: Airports, city centre lots (reward short-term and long-term parkers).
// =============================================================================
class TieredFeeStrategy implements FeeStrategy {

    private final double firstTierRate;   // per hour: 0–2 hrs
    private final double secondTierRate;  // per hour: 2–6 hrs
    private final double thirdTierRate;   // per hour: 6+ hrs
    private final double dailyCap;        // maximum daily fee

    TieredFeeStrategy(double firstTierRate, double secondTierRate,
                      double thirdTierRate, double dailyCap) {
        this.firstTierRate  = firstTierRate;
        this.secondTierRate = secondTierRate;
        this.thirdTierRate  = thirdTierRate;
        this.dailyCap       = dailyCap;
    }

    @Override
    public double calculate(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) return 0;

        double totalMinutes = duration.toMinutes();
        double fee = 0;

        // Tier 1: first 2 hours = 120 minutes
        double tier1Minutes = Math.min(totalMinutes, 120);
        fee += (tier1Minutes / 60.0) * firstTierRate;
        totalMinutes -= tier1Minutes;

        // Tier 2: next 4 hours = 240 minutes
        if (totalMinutes > 0) {
            double tier2Minutes = Math.min(totalMinutes, 240);
            fee += (tier2Minutes / 60.0) * secondTierRate;
            totalMinutes -= tier2Minutes;
        }

        // Tier 3: any remaining hours
        if (totalMinutes > 0) {
            fee += (totalMinutes / 60.0) * thirdTierRate;
        }

        // Apply daily cap
        return Math.min(fee, dailyCap);
    }

    @Override
    public String getName() {
        return String.format("Tiered(₹%.0f/₹%.0f/₹%.0f, cap=₹%.0f)",
                firstTierRate, secondTierRate, thirdTierRate, dailyCap);
    }
}


// =============================================================================
// ■ WeekendFeeStrategy — Decorator-style: wraps another strategy and adds a
//                        weekend surcharge
//
// Pattern: Decorator — wraps any FeeStrategy and adds weekend pricing.
// Example: Hourly rate is ₹50 on weekdays, ₹75 on weekends.
//          WeekendFeeStrategy(hourlyStrategy, 1.5x) → ₹75/hr on weekends.
//
// Interview point: Decorator lets you compose pricing rules without
//                  creating combinatorial subclasses (WeekendHourlyStrategy,
//                  WeekendTieredStrategy, WeekendFlatStrategy, ...).
// =============================================================================
class WeekendFeeStrategy implements FeeStrategy {

    private final FeeStrategy baseStrategy;
    private final double      weekendMultiplier;  // e.g., 1.5 = 50% surcharge

    WeekendFeeStrategy(FeeStrategy baseStrategy, double weekendMultiplier) {
        this.baseStrategy       = baseStrategy;
        this.weekendMultiplier  = weekendMultiplier;
    }

    @Override
    public double calculate(Duration duration) {
        double baseFee = baseStrategy.calculate(duration);
        boolean isWeekend = isWeekend();
        return isWeekend ? baseFee * weekendMultiplier : baseFee;
    }

    private boolean isWeekend() {
        java.time.DayOfWeek day = java.time.LocalDate.now().getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }

    @Override
    public String getName() {
        return "Weekend(" + baseStrategy.getName()
               + ", " + (weekendMultiplier * 100) + "% on weekends)";
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why is FeeStrategy an interface and not an abstract class?
// A: FeeStrategy has no shared state. An interface with a single calculate()
//    method is the ideal Strategy contract. If we added common helper methods
//    (e.g., roundUp()), we could use a default method in the interface (Java 8+)
//    without needing an abstract class. Interfaces give maximum flexibility
//    (a class can implement multiple interfaces; can't extend multiple classes).
//
// Q: How do you change pricing dynamically at runtime?
// A: ParkingLot.setFeeStrategy(newStrategy) — volatile write for thread safety.
//    All subsequent exit calculations use the new strategy. No restart needed.
//    This is the Strategy pattern's primary power: runtime algorithm swapping.
//
// Q: How does WeekendFeeStrategy demonstrate the Decorator pattern?
// A: WeekendFeeStrategy wraps any FeeStrategy (the base). Its calculate()
//    delegates to baseStrategy.calculate() and applies a multiplier if weekend.
//    Client code (ParkingLot) sees WeekendFeeStrategy as just a FeeStrategy
//    — it doesn't know a Decorator is involved. You can stack decorators:
//    new WeekendFeeStrategy(new TieredFeeStrategy(...), 1.5).
//
// Q: What about tax computation on the fee?
// A: Perfect Decorator use case:
//    class TaxDecorator implements FeeStrategy {
//        double calculate(d) { return base.calculate(d) * (1 + taxRate); }
//    }
//    new TaxDecorator(new TieredFeeStrategy(...), 0.18)  // 18% GST
//    Zero changes to TieredFeeStrategy or ParkingLot.
