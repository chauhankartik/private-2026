// =============================================================================
// FILE: 01_Enums.java
// TOPIC: Parking Lot LLD — All Enumerations
// PURPOSE: Central type definitions used across the system
// =============================================================================

package parking_lot;

// =============================================================================
// ■ VehicleType — classifies the vehicle for spot-matching
// =============================================================================
enum VehicleType {
    MOTORCYCLE,
    CAR,
    TRUCK,
    ELECTRIC_CAR   // needs EV charging spot preferentially
}


// =============================================================================
// ■ SpotType — classifies the parking spot's physical size / capability
// =============================================================================
enum SpotType {
    MOTORCYCLE,    // smallest — bikes only
    COMPACT,       // standard car-sized
    LARGE,         // SUV / Truck-sized
    HANDICAPPED,   // accessible spots (near entry, may be compact or large)
    EV             // has charging station; electric vehicles preferred here
}


// =============================================================================
// ■ SpotState — the lifecycle state of a single parking spot
//
// State transitions:
//   AVAILABLE → OCCUPIED     (on vehicle assignment)
//   AVAILABLE → RESERVED     (on pre-booking)
//   RESERVED  → OCCUPIED     (on reserved vehicle arrival)
//   OCCUPIED  → AVAILABLE    (on vehicle departure)
//   AVAILABLE → OUT_OF_SERVICE (on maintenance)
//   OUT_OF_SERVICE → AVAILABLE (after maintenance)
//
// Pattern: State — spot behavior changes based on this state value.
// =============================================================================
enum SpotState {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    OUT_OF_SERVICE
}


// =============================================================================
// ■ PaymentStatus — lifecycle of a payment transaction
// =============================================================================
enum PaymentStatus {
    PENDING,      // transaction initiated but not confirmed
    COMPLETED,    // successfully charged
    FAILED,       // charge declined / error
    REFUNDED      // post-completion reversal (e.g., system error)
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why define SpotType and VehicleType as separate enums?
// A: They represent different domain concepts. A COMPACT spot is a physical
//    attribute of the parking space. A CAR is a physical attribute of the vehicle.
//    Merging them into one enum would violate SRP and create confusing comparisons
//    like "spot == vehicle". Separate enums make canFit() logic explicit and readable.
//
// Q: Why include RESERVED and OUT_OF_SERVICE in SpotState?
// A: RESERVED supports pre-booking use cases (common follow-up question).
//    OUT_OF_SERVICE models real-world maintenance scenarios where a spot is
//    temporarily unavailable without being physically occupied. Both prevent
//    the system from assigning that spot to incoming vehicles.
//
// Q: Could SpotState be replaced with boolean flags (isOccupied, isReserved)?
// A: No — boolean flags create illegal combined states (isOccupied=true AND
//    isReserved=true simultaneously). Enum guarantees exactly ONE state at a time.
//    This is the classic State pattern motivation: finite, mutually exclusive states.
