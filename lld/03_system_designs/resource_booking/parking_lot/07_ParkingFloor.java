// =============================================================================
// FILE: 07_ParkingFloor.java
// TOPIC: Parking Lot LLD — ParkingFloor + DisplayBoard
// PATTERNS: Observer (DisplayBoard), Factory Method (SpotFactory)
// SOLID: SRP — floor manages spots; display board shows status
// THREAD-SAFETY: Per-spot ReentrantLock + AtomicInteger availability counters
// =============================================================================

package parking_lot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

// =============================================================================
// ■ DisplayBoard — Real-time availability display per floor
//
// Pattern: Observer — ParkingFloor notifies DisplayBoard every time a spot
//          changes state. DisplayBoard maintains live AtomicInteger counts.
//
// In a real system, DisplayBoard would also push to:
//   - Physical LED display boards at floor entrances
//   - Web/mobile app APIs
//   - Navigation system integrations (Google Maps parking data)
//
// AtomicInteger: lock-free counters — high-frequency reads and writes from
// multiple entry/exit threads without any blocking.
// =============================================================================
class DisplayBoard {

    private final int floorNumber;
    // SpotType → count of available spots of that type
    private final Map<SpotType, AtomicInteger> availableCounts = new EnumMap<>(SpotType.class);

    DisplayBoard(int floorNumber) {
        this.floorNumber = floorNumber;
        for (SpotType type : SpotType.values()) {
            availableCounts.put(type, new AtomicInteger(0));
        }
    }

    /** Called by ParkingFloor when a spot becomes available. */
    void onSpotReleased(SpotType type) {
        availableCounts.get(type).incrementAndGet();
    }

    /** Called by ParkingFloor when a spot is assigned / reserved. */
    void onSpotOccupied(SpotType type) {
        availableCounts.get(type).decrementAndGet();
    }

    /** Initialize counts from actual spot data (called once at startup). */
    void initialize(Map<SpotType, Integer> counts) {
        counts.forEach((type, count) -> availableCounts.get(type).set(count));
    }

    /** Get available count for a specific spot type. */
    int getAvailable(SpotType type) {
        return availableCounts.get(type).get();
    }

    /** Get total available spots across all types on this floor. */
    int getTotalAvailable() {
        return availableCounts.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    /** Display current status to console. */
    void showStatus() {
        System.out.printf("  Floor %d Display Board:%n", floorNumber);
        for (SpotType type : SpotType.values()) {
            int count = availableCounts.get(type).get();
            System.out.printf("    %-15s: %d available%n", type, count);
        }
    }
}


// =============================================================================
// ■ ParkingFloor — Manages all spots on a single floor
//
// Responsibilities:
//   1. Maintain a list of spots (all types) on this floor.
//   2. Find a suitable spot for an incoming vehicle (thread-safe).
//   3. Release a spot when a vehicle departs.
//   4. Notify the DisplayBoard on every spot state change.
//
// Thread-safety: Each spot has its own ReentrantLock.
//   findSpot() uses tryLock() to atomically claim a spot without blocking
//   other threads assigning different spots. This is the key insight that
//   avoids a floor-level lock bottleneck.
// =============================================================================
class ParkingFloor {

    private final int               floorNumber;
    private final List<ParkingSpot> spots;
    private final DisplayBoard      displayBoard;

    ParkingFloor(int floorNumber) {
        this.floorNumber  = floorNumber;
        this.spots        = new ArrayList<>();
        this.displayBoard = new DisplayBoard(floorNumber);
    }

    // ─── Setup (called during ParkingLot construction) ────────────────────────

    /**
     * Add a spot to this floor using SpotFactory.
     * Called once during ParkingLot.Builder initialization.
     */
    void addSpot(SpotType type, String spotId) {
        ParkingSpot spot = SpotFactory.create(type, spotId, floorNumber);
        spots.add(spot);
    }

    /** After all spots are added, initialize the DisplayBoard with actual counts. */
    void initializeDisplayBoard() {
        Map<SpotType, Integer> counts = new EnumMap<>(SpotType.class);
        for (SpotType type : SpotType.values()) counts.put(type, 0);
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) {
                counts.merge(spot.getSpotType(), 1, Integer::sum);
            }
        }
        displayBoard.initialize(counts);
    }

    // ─── Core operations ──────────────────────────────────────────────────────

    /**
     * Find and atomically claim a suitable spot for the given vehicle.
     *
     * Search strategy:
     *   1. Prefer smallest fitting spot (conserve large spots for large vehicles).
     *   2. For ELECTRIC_CAR: try EV spots first, then COMPACT/LARGE.
     *   3. For each candidate spot: tryLock() to claim atomically without blocking.
     *      If tryLock() fails (another thread just took it), skip to next spot.
     *
     * Time: O(n) where n = number of spots on this floor (bounded by lot size).
     *
     * @return Optional<ParkingSpot> — present = spot claimed and assigned;
     *         empty = no suitable spot available on this floor.
     */
    Optional<ParkingSpot> findAndAssignSpot(Vehicle vehicle) {
        // Build preferred spot type order for this vehicle type
        List<SpotType> preferenceOrder = getPreferenceOrder(vehicle.getType());

        for (SpotType preferred : preferenceOrder) {
            for (ParkingSpot spot : spots) {
                if (spot.getSpotType() != preferred) continue;

                // tryLock: non-blocking — skip if another thread just grabbed it
                if (spot.lock.tryLock()) {
                    try {
                        // Double-check state under lock (another thread may have changed it)
                        if (spot.isAvailable() && spot.canFit(vehicle)) {
                            spot.assign(vehicle);               // state: AVAILABLE → OCCUPIED
                            displayBoard.onSpotOccupied(spot.getSpotType());  // notify Observer
                            return Optional.of(spot);
                        }
                    } finally {
                        spot.lock.unlock();
                    }
                }
                // tryLock failed or spot not available → skip to next
            }
        }
        return Optional.empty();   // no suitable spot found on this floor
    }

    /**
     * Release a spot after vehicle departure.
     * Spot transitions: OCCUPIED → AVAILABLE.
     * DisplayBoard is notified.
     */
    void releaseSpot(ParkingSpot spot) {
        spot.release();                                    // handles its own lock internally
        displayBoard.onSpotReleased(spot.getSpotType());   // notify Observer
    }

    // ─── Getters / Queries ────────────────────────────────────────────────────

    public int           getFloorNumber()  { return floorNumber; }
    public DisplayBoard  getDisplayBoard() { return displayBoard; }
    public List<ParkingSpot> getSpots()    { return Collections.unmodifiableList(spots); }

    /** Count of available spots on this floor (across all types). */
    public int getAvailableCount() {
        return displayBoard.getTotalAvailable();
    }

    /** Count of available spots of a specific type on this floor. */
    public int getAvailableCount(SpotType type) {
        return displayBoard.getAvailable(type);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Returns spot types in order of preference for a vehicle type.
     * Smallest-first principle: conserve large spots for large vehicles.
     * EV cars prefer EV spots (charged for free or discounted).
     */
    private List<SpotType> getPreferenceOrder(VehicleType vehicleType) {
        return switch (vehicleType) {
            case MOTORCYCLE   -> List.of(SpotType.MOTORCYCLE, SpotType.COMPACT, SpotType.LARGE);
            case CAR          -> List.of(SpotType.COMPACT, SpotType.HANDICAPPED, SpotType.LARGE);
            case TRUCK        -> List.of(SpotType.LARGE);
            case ELECTRIC_CAR -> List.of(SpotType.EV, SpotType.COMPACT, SpotType.LARGE);
        };
    }

    @Override
    public String toString() {
        return "ParkingFloor[floor=" + floorNumber
               + ", totalSpots=" + spots.size()
               + ", available=" + getAvailableCount() + "]";
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: How does the Observer pattern work between ParkingFloor and DisplayBoard?
// A: ParkingFloor holds a reference to DisplayBoard. Every time a spot changes
//    state (assigned → displayBoard.onSpotOccupied; released → onSpotReleased),
//    ParkingFloor notifies the DisplayBoard. DisplayBoard updates its AtomicInteger
//    counters. In a real system, DisplayBoard could also push to a WebSocket,
//    an LED driver, or a REST endpoint. ParkingFloor doesn't know or care —
//    it just calls the notification methods (Dependency Inversion).
//
// Q: Why tryLock() instead of lock() for spot assignment?
// A: Imagine 5 cars entering simultaneously. With lock(), if 5 threads all want
//    the same spot, 4 threads block waiting. tryLock() lets them immediately
//    skip to the next spot — they all get spots without any thread waiting.
//    This gives O(n) throughput under contention instead of O(n) serial queuing.
//    It's correct because: a spot missed by tryLock() is about to be taken by
//    another thread anyway — skipping it is the right outcome.
//
// Q: Why does findAndAssignSpot() check the state again under the lock?
// A: Classic double-check idiom. Between checking isAvailable() (without lock)
//    and calling tryLock(), another thread may have assigned the spot. The
//    re-check under the lock guarantees we never double-assign a spot. Without
//    the re-check, two threads could both see "available" before either locks,
//    then both try to assign — the second assign() would throw IllegalStateException.
//
// Q: Why smallest-first preference in getPreferenceOrder()?
// A: If a motorcycle always parks in a LARGE spot, that wastes space that a Truck
//    needs. By preferring the smallest fitting spot, we pack the lot efficiently
//    and maximize total vehicle capacity. This is a greedy optimization that
//    works well in practice (though not always globally optimal).
