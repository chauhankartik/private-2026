// =============================================================================
// FILE: 08_ParkingLot.java
// TOPIC: Parking Lot LLD — ParkingLot (Singleton + Builder) + Custom Exceptions
// PATTERNS: Singleton (double-checked locking), Builder, Strategy, Factory
// SOLID: SRP (orchestrates only), DIP (depends on interfaces)
// THREAD-SAFETY: ConcurrentHashMap for active tickets; per-spot locks for spots
// =============================================================================

package parking_lot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// =============================================================================
// ■ Custom Exceptions — domain-specific error signals
// =============================================================================

/** Thrown when all spots on all floors are occupied. */
class ParkingLotFullException extends RuntimeException {
    ParkingLotFullException(String message) { super(message); }
}

/** Thrown when a ticket is presented that doesn't exist or was already closed. */
class InvalidTicketException extends RuntimeException {
    InvalidTicketException(String message) { super(message); }
}

/** Thrown when a vehicle with the same license plate is already parked. */
class DuplicateVehicleException extends RuntimeException {
    DuplicateVehicleException(String message) { super(message); }
}


// =============================================================================
// ■ ParkingLot — The top-level orchestrator (Singleton)
//
// Responsibilities:
//   1. Accept vehicles at entry gates (parkVehicle).
//   2. Find the best available spot across all floors.
//   3. Issue a ParkingTicket on successful assignment.
//   4. Accept vehicle departure (unparkVehicle).
//   5. Calculate fee, process payment, release spot, return Receipt.
//   6. Track all active tickets (for duplicate vehicle detection).
//   7. Expose real-time availability via DisplayBoard.
//
// Singleton:
//   One ParkingLot instance per application (one physical parking lot).
//   Uses double-checked locking with volatile for thread-safe initialization.
//   (Same pattern as LogManager in the Logging Framework LLD.)
//
// ConcurrentHashMap for activeTickets:
//   - Key: licensePlate → prevents same car entering twice
//   - Key: ticketId → used to look up ticket at exit gate
//   - Lock-free reads; fine-grained write locking per bucket
// =============================================================================
final class ParkingLot {

    // ─── Singleton ────────────────────────────────────────────────────────────

    private static volatile ParkingLot instance;

    private ParkingLot() {}    // private: only Builder creates the instance

    /**
     * After the lot is built once via ParkingLot.Builder, call getInstance()
     * to retrieve the same object from anywhere in the application.
     */
    public static ParkingLot getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "ParkingLot not initialized. Call ParkingLot.Builder.build() first.");
        }
        return instance;
    }

    // ─── Instance state ───────────────────────────────────────────────────────

    private String            lotName;
    private String            lotAddress;
    private List<ParkingFloor> floors;
    private volatile FeeStrategy feeStrategy;    // volatile: swappable at runtime

    // ticketId → ParkingTicket (active sessions)
    private final ConcurrentHashMap<String, ParkingTicket> ticketMap = new ConcurrentHashMap<>();

    // licensePlate → ticketId (duplicate entry detection)
    private final ConcurrentHashMap<String, String> plateToTicket  = new ConcurrentHashMap<>();

    // ─── Core Operations ──────────────────────────────────────────────────────

    /**
     * ENTRY: Assign a spot to a vehicle and issue a ParkingTicket.
     *
     * Pipeline:
     *   [1] Duplicate check: same plate can't enter twice.
     *   [2] Search floors sequentially for a suitable spot (floor 1 → floor N).
     *   [3] If found: assign spot, create ticket, register in maps.
     *   [4] If not found: throw ParkingLotFullException.
     *
     * @param vehicle the vehicle at the entry gate
     * @return        ParkingTicket issued to the driver
     * @throws DuplicateVehicleException if the vehicle is already parked
     * @throws ParkingLotFullException   if no suitable spot is available
     */
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        // [1] Duplicate check
        if (plateToTicket.containsKey(vehicle.getLicensePlate())) {
            throw new DuplicateVehicleException(
                "Vehicle " + vehicle.getLicensePlate() + " is already parked.");
        }

        // [2] Search all floors
        for (ParkingFloor floor : floors) {
            Optional<ParkingSpot> spotOpt = floor.findAndAssignSpot(vehicle);
            if (spotOpt.isPresent()) {
                ParkingSpot spot = spotOpt.get();

                // [3] Issue ticket
                ParkingTicket ticket = new ParkingTicket.Builder()
                        .vehicle(vehicle)
                        .spot(spot)
                        .build();

                ticketMap.put(ticket.getTicketId(), ticket);
                plateToTicket.put(vehicle.getLicensePlate(), ticket.getTicketId());

                System.out.printf("[ENTRY] %s → %s (Floor %d) | Ticket: %s%n",
                        vehicle, spot.getSpotId(), floor.getFloorNumber(), ticket.getTicketId());
                return ticket;
            }
        }

        // [4] No spot found anywhere
        throw new ParkingLotFullException(
            "Parking lot is full. No spot available for " + vehicle);
    }

    /**
     * EXIT: Process departure — release spot, calculate fee, take payment.
     *
     * Pipeline:
     *   [1] Validate ticket: look up by ticketId.
     *   [2] Calculate fee using feeStrategy and parking duration.
     *   [3] Process payment via the driver's chosen PaymentMethod.
     *   [4] If payment succeeds: release spot, remove from active maps.
     *   [5] Create and return Receipt.
     *
     * @param ticket        the ticket issued at entry
     * @param paymentMethod the driver's chosen payment method
     * @return              ParkingReceipt for this session
     * @throws InvalidTicketException if ticket is unknown or already closed
     */
    public ParkingReceipt unparkVehicle(ParkingTicket ticket, PaymentMethod paymentMethod) {
        // [1] Validate
        if (!ticketMap.containsKey(ticket.getTicketId())) {
            throw new InvalidTicketException(
                "Ticket " + ticket.getTicketId() + " is invalid or already closed.");
        }

        Instant exitTime = Instant.now();

        // [2] Calculate fee
        double fee = feeStrategy.calculate(ticket.getDurationUntil(exitTime));

        System.out.printf("[EXIT]  %s | Duration: %d min | Fee: ₹%.2f | Payment: %s%n",
                ticket.getVehicle(),
                ticket.getDurationUntil(exitTime).toMinutes(),
                fee,
                paymentMethod.getMethodName());

        // [3] Process payment
        PaymentStatus paymentStatus = paymentMethod.pay(fee);

        // [4] Release spot only if payment succeeded
        if (paymentStatus == PaymentStatus.COMPLETED) {
            ParkingFloor floor = floors.get(ticket.getFloorNumber() - 1); // floors are 1-indexed
            floor.releaseSpot(ticket.getSpot());

            ticketMap.remove(ticket.getTicketId());
            plateToTicket.remove(ticket.getVehicle().getLicensePlate());

            System.out.printf("[EXIT]  Spot %s released on Floor %d.%n",
                    ticket.getSpot().getSpotId(), ticket.getFloorNumber());
        } else {
            System.out.println("[EXIT]  Payment FAILED. Vehicle must remain. Spot NOT released.");
        }

        // [5] Return receipt (regardless of payment success — for audit trail)
        return new ParkingReceipt(ticket, exitTime, fee, paymentStatus);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    /** Total available spots across all floors and all types. */
    public int getTotalAvailableSpots() {
        return floors.stream().mapToInt(ParkingFloor::getAvailableCount).sum();
    }

    /** Show full DisplayBoard for every floor. */
    public void showAvailability() {
        System.out.println("\n📋 " + lotName + " — Real-Time Availability");
        System.out.println("   " + "─".repeat(40));
        for (ParkingFloor floor : floors) {
            floor.getDisplayBoard().showStatus();
        }
        System.out.println("   Total available: " + getTotalAvailableSpots() + " spots");
        System.out.println();
    }

    /** Check if a vehicle is currently parked (by license plate). */
    public boolean isVehicleParked(String licensePlate) {
        return plateToTicket.containsKey(licensePlate);
    }

    /** Retrieve active ticket by ticket ID. */
    public Optional<ParkingTicket> findTicket(String ticketId) {
        return Optional.ofNullable(ticketMap.get(ticketId));
    }

    public String            getLotName()    { return lotName; }
    public String            getLotAddress() { return lotAddress; }
    public List<ParkingFloor> getFloors()    { return Collections.unmodifiableList(floors); }
    public int               getFloorCount() { return floors.size(); }

    /** Replace fee strategy at runtime (volatile write — thread-safe). */
    public void setFeeStrategy(FeeStrategy strategy) {
        this.feeStrategy = strategy;
        System.out.println("[CONFIG] Fee strategy updated to: " + strategy.getName());
    }

    // =========================================================================
    // ■ ParkingLot.Builder — Fluent construction + Singleton initialization
    //
    // Usage:
    //   ParkingLot lot = new ParkingLot.Builder()
    //       .name("Central Parking")
    //       .address("MG Road, Bengaluru")
    //       .feeStrategy(new HourlyFeeStrategy(50))
    //       .addFloor(1, spots -> {
    //           spots.add(SpotType.COMPACT,    "F1-C1");
    //           spots.add(SpotType.LARGE,      "F1-L1");
    //           spots.add(SpotType.MOTORCYCLE, "F1-M1");
    //           spots.add(SpotType.EV,         "F1-E1");
    //       })
    //       .build();
    // =========================================================================
    static final class Builder {

        private String            name        = "ParkingLot";
        private String            address     = "";
        private FeeStrategy       feeStrategy = new HourlyFeeStrategy(30);  // ₹30/hr default
        private final List<ParkingFloor> floors = new ArrayList<>();

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder address(String address) {
            this.address = address;
            return this;
        }

        Builder feeStrategy(FeeStrategy strategy) {
            this.feeStrategy = strategy;
            return this;
        }

        /**
         * Add a floor with a predefined list of spots.
         *
         * @param floorNumber 1-based floor number
         * @param spotDefs    SpotType-to-spotId pairs for all spots on this floor
         */
        Builder addFloor(int floorNumber, Map<SpotType, List<String>> spotDefs) {
            ParkingFloor floor = new ParkingFloor(floorNumber);
            spotDefs.forEach((type, ids) ->
                    ids.forEach(id -> floor.addSpot(type, id)));
            floor.initializeDisplayBoard();
            floors.add(floor);
            return this;
        }

        /**
         * Build and register the Singleton instance.
         * Throws if called more than once (prevents re-initialization).
         */
        ParkingLot build() {
            if (ParkingLot.instance != null) {
                throw new IllegalStateException("ParkingLot already initialized.");
            }
            synchronized (ParkingLot.class) {
                if (ParkingLot.instance != null) {
                    throw new IllegalStateException("ParkingLot already initialized.");
                }
                ParkingLot lot = new ParkingLot();
                lot.lotName     = this.name;
                lot.lotAddress  = this.address;
                lot.floors      = Collections.unmodifiableList(new ArrayList<>(this.floors));
                lot.feeStrategy = this.feeStrategy;
                ParkingLot.instance = lot;
            }
            return ParkingLot.instance;
        }

        /** Reset singleton — for testing only. */
        static void resetInstance() {
            ParkingLot.instance = null;
        }
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why Singleton for ParkingLot?
// A: There is only one physical parking lot. All entry and exit gates share the
//    same floor/spot data. If each gate had its own ParkingLot instance, they'd
//    have separate spot maps → double-booking would be possible. Singleton
//    ensures one shared source of truth for all gates.
//
// Q: How does the Builder interact with the Singleton?
// A: Builder collects all configuration (name, floors, fee strategy) and
//    atomically constructs and stores the Singleton instance. The double-checked
//    locking in build() ensures thread-safe initialization even if two threads
//    call build() simultaneously (only one creates the instance).
//
// Q: Why ConcurrentHashMap for ticketMap and plateToTicket?
// A: Multiple gate threads concurrently add and remove tickets.
//    ConcurrentHashMap guarantees linearizability of individual operations
//    (put/get/remove are atomic). This avoids the serialization overhead
//    of synchronized(this) on the entire map for every gate transaction.
//
// Q: Why is feeStrategy volatile?
// A: setFeeStrategy() can be called from a config-reload thread while exit
//    gate threads read it in unparkVehicle(). volatile guarantees the write
//    is immediately visible to all threads — no stale cached value.
//    (Same volatile rationale as Logger.level in the Logging Framework LLD.)
//
// Q: What's the Builder.resetInstance() method for?
// A: Unit testing. Tests need a fresh ParkingLot between test cases. In
//    production code, resetInstance() would be package-private or protected
//    behind a test flag. This is a common test-isolation pattern for Singletons.
