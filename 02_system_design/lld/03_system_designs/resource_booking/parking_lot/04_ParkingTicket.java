// =============================================================================
// FILE: 04_ParkingTicket.java
// TOPIC: Parking Lot LLD — Ticket and Receipt
// PATTERNS: Builder (ParkingTicket), Immutable Object, Value Object (Receipt)
// =============================================================================

package parking_lot;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

// =============================================================================
// ■ ParkingTicket — Immutable receipt issued when a vehicle ENTERS
//
// Contains:
//   - A unique ticketId (UUID)
//   - Vehicle reference
//   - Spot reference (which spot was assigned)
//   - Entry timestamp
//
// Immutable after construction — safely passed across threads.
// No setters. exitTime is recorded separately in the Receipt.
//
// Pattern: Builder — clean construction without a 5-argument constructor.
// =============================================================================
final class ParkingTicket {

    private final String       ticketId;
    private final Vehicle      vehicle;
    private final ParkingSpot  spot;
    private final int          floorNumber;
    private final Instant      entryTime;

    private ParkingTicket(Builder builder) {
        this.ticketId    = builder.ticketId;
        this.vehicle     = builder.vehicle;
        this.spot        = builder.spot;
        this.floorNumber = builder.floorNumber;
        this.entryTime   = builder.entryTime;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String      getTicketId()    { return ticketId; }
    public Vehicle     getVehicle()     { return vehicle; }
    public ParkingSpot getSpot()        { return spot; }
    public int         getFloorNumber() { return floorNumber; }
    public Instant     getEntryTime()   { return entryTime; }

    /**
     * Compute how long the vehicle has been parked, relative to now.
     * Used by FeeStrategy at exit time.
     */
    public Duration getDurationSoFar() {
        return Duration.between(entryTime, Instant.now());
    }

    /**
     * Compute duration relative to a specific exit time.
     * Used by FeeStrategy when given an explicit exitTime.
     */
    public Duration getDurationUntil(Instant exitTime) {
        return Duration.between(entryTime, exitTime);
    }

    @Override
    public String toString() {
        return "Ticket{id=" + ticketId
               + ", vehicle=" + vehicle
               + ", spot=" + spot.getSpotId()
               + ", floor=" + floorNumber
               + ", entryTime=" + entryTime + "}";
    }

    // =========================================================================
    // ■ Builder — fluent construction
    // =========================================================================
    static final class Builder {

        private final String    ticketId  = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private final Instant   entryTime = Instant.now();

        private Vehicle     vehicle;
        private ParkingSpot spot;
        private int         floorNumber;

        Builder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        Builder spot(ParkingSpot spot) {
            this.spot        = spot;
            this.floorNumber = spot.getFloorNumber();
            return this;
        }

        ParkingTicket build() {
            if (vehicle == null) throw new IllegalStateException("vehicle is required");
            if (spot    == null) throw new IllegalStateException("spot is required");
            return new ParkingTicket(this);
        }
    }
}


// =============================================================================
// ■ ParkingReceipt — Immutable record of a completed parking session
//
// Created by ParkingLot.unparkVehicle() after:
//   1. Exit time is recorded
//   2. Fee is calculated
//   3. Payment is processed
//
// Value Object: identity is by ticketId, not object reference.
// No mutable state — freely shareable across threads.
// =============================================================================
final class ParkingReceipt {

    private final String        ticketId;
    private final Vehicle       vehicle;
    private final ParkingSpot   spot;
    private final Instant       entryTime;
    private final Instant       exitTime;
    private final Duration      duration;
    private final double        feeCharged;
    private final PaymentStatus paymentStatus;

    ParkingReceipt(ParkingTicket ticket, Instant exitTime,
                   double feeCharged, PaymentStatus paymentStatus) {
        this.ticketId      = ticket.getTicketId();
        this.vehicle       = ticket.getVehicle();
        this.spot          = ticket.getSpot();
        this.entryTime     = ticket.getEntryTime();
        this.exitTime      = exitTime;
        this.duration      = ticket.getDurationUntil(exitTime);
        this.feeCharged    = feeCharged;
        this.paymentStatus = paymentStatus;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String        getTicketId()      { return ticketId; }
    public Vehicle       getVehicle()       { return vehicle; }
    public Instant       getEntryTime()     { return entryTime; }
    public Instant       getExitTime()      { return exitTime; }
    public Duration      getDuration()      { return duration; }
    public double        getFeeCharged()    { return feeCharged; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }

    @Override
    public String toString() {
        long minutes = duration.toMinutes();
        return String.format(
            "Receipt{ticketId=%s, vehicle=%s, spot=%s, duration=%dm, fee=₹%.2f, payment=%s}",
            ticketId, vehicle, spot.getSpotId(), minutes, feeCharged, paymentStatus);
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why is ParkingTicket immutable?
// A: A ticket is a snapshot of the moment of entry. Nothing about the entry
//    event changes after the fact. Immutability ensures the ticket is safe to
//    pass between threads (e.g., from entry gate to exit gate to fee calculator)
//    without any synchronization. It also prevents accidental modification.
//
// Q: Why UUID for ticketId?
// A: UUID guarantees global uniqueness without a central counter or DB sequence.
//    Important for distributed systems where multiple gate machines generate
//    tickets simultaneously. No two gates can generate the same ID.
//    Truncated to 8 chars for readability in the demo ("TKT-A3B4C5D6").
//
// Q: Why separate ParkingReceipt from ParkingTicket?
// A: Different lifecycle stages. Ticket = entry event (open-ended, no fee yet).
//    Receipt = exit event (closed, fee calculated, payment recorded). Mixing
//    them into one class would require null fields (exitTime=null on entry),
//    which is a red flag. Two separate immutable classes is cleaner.
//
// Q: Why store 'spot' reference in the ticket?
// A: On exit, the system needs to release the exact spot that was assigned.
//    Storing the reference eliminates a lookup step. This is O(1) spot release
//    vs. O(n) search across all floors/spots for the vehicle.
