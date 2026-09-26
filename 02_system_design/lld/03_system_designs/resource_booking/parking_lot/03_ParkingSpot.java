// =============================================================================
// FILE: 03_ParkingSpot.java
// TOPIC: Parking Lot LLD — Parking Spot Hierarchy
// PATTERNS: Abstract Class, State (SpotState), Factory Method
// SOLID: SRP (spot manages its own state), OCP (new spot = new subclass), LSP
// THREAD-SAFETY: ReentrantLock per spot — fine-grained concurrency
// =============================================================================

package parking_lot;

import java.util.concurrent.locks.ReentrantLock;

// =============================================================================
// ■ ParkingSpot — Abstract base class for all spot types
//
// A ParkingSpot:
//   1. Has an identity: spotId, floorNumber, spotType
//   2. Has a state: AVAILABLE / OCCUPIED / RESERVED / OUT_OF_SERVICE
//   3. Has a compatibility rule: canFit(vehicle) — defined per subclass
//   4. Is thread-safe: each spot has its own ReentrantLock
//
// Thread-safety design:
//   Fine-grained locking: each spot is independently lockable.
//   Two threads assigning DIFFERENT spots never contend.
//   Only concurrent assignment to the SAME spot is serialized.
//   tryLock() is used so a thread can skip to the next available spot
//   rather than blocking, maximising throughput at the entry gate.
// =============================================================================
abstract class ParkingSpot {

    private final String   spotId;
    private final int      floorNumber;
    private final SpotType spotType;

    // Guarded by 'lock'
    private volatile SpotState state;
    private          Vehicle   parkedVehicle;   // null when not OCCUPIED

    // Fine-grained lock — one per spot for maximum concurrency
    final ReentrantLock lock = new ReentrantLock();

    ParkingSpot(String spotId, int floorNumber, SpotType spotType) {
        this.spotId      = spotId;
        this.floorNumber = floorNumber;
        this.spotType    = spotType;
        this.state       = SpotState.AVAILABLE;
    }

    // ─── Identity ─────────────────────────────────────────────────────────────

    public String   getSpotId()      { return spotId; }
    public int      getFloorNumber() { return floorNumber; }
    public SpotType getSpotType()    { return spotType; }

    // ─── State ────────────────────────────────────────────────────────────────

    public SpotState getState()          { return state; }
    public boolean   isAvailable()       { return state == SpotState.AVAILABLE; }
    public Vehicle   getParkedVehicle()  { return parkedVehicle; }

    // ─── Compatibility — Template Method ──────────────────────────────────────

    /**
     * Can this vehicle physically fit in this spot?
     * Delegates to vehicle.canFitInSpot(this.spotType).
     *
     * Subclasses may override to add extra constraints.
     * (e.g., HandicappedSpot may require HANDICAPPED_PERMIT on vehicle)
     */
    public boolean canFit(Vehicle vehicle) {
        return vehicle.canFitInSpot(this.spotType);
    }

    // ─── Lifecycle operations — all guarded by 'lock' ─────────────────────────

    /**
     * Assign this spot to a vehicle.
     * Transitions: AVAILABLE → OCCUPIED
     *
     * Thread-safe: caller must hold 'lock' before calling this method.
     * (Enforced by ParkingFloor which calls lock.tryLock() before assign)
     *
     * @throws IllegalStateException if spot is not AVAILABLE
     */
    void assign(Vehicle vehicle) {
        if (state != SpotState.AVAILABLE) {
            throw new IllegalStateException(
                "Spot " + spotId + " is not available (state=" + state + ")");
        }
        this.parkedVehicle = vehicle;
        this.state         = SpotState.OCCUPIED;
    }

    /**
     * Release this spot after vehicle departure.
     * Transitions: OCCUPIED → AVAILABLE
     *
     * @throws IllegalStateException if spot is not OCCUPIED
     */
    void release() {
        lock.lock();
        try {
            if (state != SpotState.OCCUPIED) {
                throw new IllegalStateException(
                    "Spot " + spotId + " is not occupied (state=" + state + ")");
            }
            this.parkedVehicle = null;
            this.state         = SpotState.AVAILABLE;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reserve this spot (pre-booking).
     * Transitions: AVAILABLE → RESERVED
     */
    void reserve() {
        lock.lock();
        try {
            if (state != SpotState.AVAILABLE) {
                throw new IllegalStateException(
                    "Spot " + spotId + " cannot be reserved (state=" + state + ")");
            }
            this.state = SpotState.RESERVED;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Mark spot as out of service (maintenance).
     * Transitions: AVAILABLE → OUT_OF_SERVICE
     */
    void setOutOfService() {
        lock.lock();
        try {
            this.state = SpotState.OUT_OF_SERVICE;
        } finally {
            lock.unlock();
        }
    }

    /** Restore spot to available after maintenance. */
    void restore() {
        lock.lock();
        try {
            this.state = SpotState.AVAILABLE;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return spotType + "-Spot[" + spotId + ", floor=" + floorNumber
               + ", state=" + state
               + (parkedVehicle != null ? ", vehicle=" + parkedVehicle : "") + "]";
    }
}


// =============================================================================
// ■ CompactSpot — Standard car-sized spot
// =============================================================================
class CompactSpot extends ParkingSpot {
    CompactSpot(String spotId, int floorNumber) {
        super(spotId, floorNumber, SpotType.COMPACT);
    }
}


// =============================================================================
// ■ LargeSpot — SUV / truck-sized spot
// =============================================================================
class LargeSpot extends ParkingSpot {
    LargeSpot(String spotId, int floorNumber) {
        super(spotId, floorNumber, SpotType.LARGE);
    }
}


// =============================================================================
// ■ MotorcycleSpot — Narrowest spots; bikes only (or bikes + compact vehicles)
// =============================================================================
class MotorcycleSpot extends ParkingSpot {
    MotorcycleSpot(String spotId, int floorNumber) {
        super(spotId, floorNumber, SpotType.MOTORCYCLE);
    }
}


// =============================================================================
// ■ HandicappedSpot — Accessible spot near entry/exit
//
// Same physical size as COMPACT or LARGE, but marked for accessible access.
// By default: any car can use it (enforced by Car.canFitInSpot including HANDICAPPED).
// In a stricter system: override canFit() to require a HANDICAPPED_PERMIT flag
// on the Vehicle object — mention this as a follow-up extension.
// =============================================================================
class HandicappedSpot extends ParkingSpot {
    HandicappedSpot(String spotId, int floorNumber) {
        super(spotId, floorNumber, SpotType.HANDICAPPED);
    }
}


// =============================================================================
// ■ EVSpot — Charging-capable spot; preferred for ElectricCar
//
// Extension point: add currentChargingPower, chargingStartTime fields.
// Override assign() to start charging session.
// Override release() to stop charging and compute charging fee.
// =============================================================================
class EVSpot extends ParkingSpot {
    EVSpot(String spotId, int floorNumber) {
        super(spotId, floorNumber, SpotType.EV);
    }

    // Interview extension: @Override void assign(Vehicle v) {
    //     super.assign(v);
    //     this.chargingStartTime = Instant.now();
    // }
}


// =============================================================================
// ■ SpotFactory — Factory Method for spot creation
//
// Hides subclass construction from ParkingFloor. ParkingFloor only knows
// SpotType enum values, not the concrete subclass names.
// OCP: add new spot type → add one case here; ParkingFloor unchanged.
// =============================================================================
class SpotFactory {

    static ParkingSpot create(SpotType type, String spotId, int floorNumber) {
        return switch (type) {
            case COMPACT      -> new CompactSpot(spotId, floorNumber);
            case LARGE        -> new LargeSpot(spotId, floorNumber);
            case MOTORCYCLE   -> new MotorcycleSpot(spotId, floorNumber);
            case HANDICAPPED  -> new HandicappedSpot(spotId, floorNumber);
            case EV           -> new EVSpot(spotId, floorNumber);
        };
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why does each spot have its own ReentrantLock instead of one global lock?
// A: A single global lock (or floor-level lock) would serialize ALL vehicle
//    assignments, even to completely different spots. With per-spot locks,
//    two cars parking in spots A1 and A5 on the same floor proceed
//    independently — no contention. This is the "fine-grained locking" pattern,
//    the same technique ConcurrentHashMap uses internally.
//
// Q: Why tryLock() in ParkingFloor (not lock())?
// A: tryLock() is non-blocking. If another thread just grabbed the same spot,
//    tryLock() returns false immediately and ParkingFloor moves to the next
//    available spot. Using lock() would block the entry gate thread, reducing
//    throughput. In a parking lot, there are always multiple spots — skipping
//    a briefly-contended spot is the right strategy.
//
// Q: Is SpotState volatile enough for thread-safety?
// A: 'state' being volatile guarantees visibility across threads for simple reads.
//    But transitions (e.g., AVAILABLE → OCCUPIED) require atomicity —
//    "read state, check it's AVAILABLE, set to OCCUPIED" must be atomic.
//    That's why assign() requires the caller to hold 'lock'.
//    volatile alone is not enough for compound check-then-act operations.
