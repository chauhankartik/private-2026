// =============================================================================
// FILE: 02_Vehicle.java
// TOPIC: Parking Lot LLD — Vehicle Hierarchy
// PATTERNS: Abstract Class (Template Method for canFitInSpot), Factory Method
// SOLID: OCP — new vehicle = new subclass; LSP — all subtypes substitutable
// =============================================================================

package parking_lot;

// =============================================================================
// ■ Vehicle — Abstract base class
//
// Holds identity data (licensePlate) and type-level behavior (canFitInSpot).
// Each concrete subclass knows which spot types it is compatible with.
//
// Template Method: canFitInSpot() is called by ParkingSpot.canFit(vehicle).
// The base class defines the contract; subclasses implement the rule.
// =============================================================================
abstract class Vehicle {

    private final String      licensePlate;
    private final VehicleType type;

    Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.isBlank())
            throw new IllegalArgumentException("License plate must not be blank");
        if (type == null)
            throw new IllegalArgumentException("VehicleType must not be null");
        this.licensePlate = licensePlate.toUpperCase().trim();
        this.type         = type;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String      getLicensePlate() { return licensePlate; }
    public VehicleType getType()         { return type; }

    /**
     * Can this vehicle fit in a spot of the given SpotType?
     *
     * Template Method: subclasses define compatibility rules.
     * ParkingSpot.canFit(vehicle) calls this method.
     *
     * Rule (general): a vehicle can park in a spot of EQUAL or LARGER size.
     *                 Vehicle-specific rules may add preferred spot types.
     */
    public abstract boolean canFitInSpot(SpotType spotType);

    @Override
    public String toString() {
        return type + "[" + licensePlate + "]";
    }
}


// =============================================================================
// ■ Motorcycle
//
// Smallest vehicle. Can fit in MOTORCYCLE, COMPACT, or LARGE spots.
// (Motorcycles can use any spot — bikes fitting in car spots is common.)
// =============================================================================
class Motorcycle extends Vehicle {

    Motorcycle(String licensePlate) {
        super(licensePlate, VehicleType.MOTORCYCLE);
    }

    @Override
    public boolean canFitInSpot(SpotType spotType) {
        return spotType == SpotType.MOTORCYCLE
            || spotType == SpotType.COMPACT
            || spotType == SpotType.LARGE;
    }
}


// =============================================================================
// ■ Car
//
// Standard vehicle. Fits in COMPACT or LARGE spots (not MOTORCYCLE spots).
// =============================================================================
class Car extends Vehicle {

    Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }

    @Override
    public boolean canFitInSpot(SpotType spotType) {
        return spotType == SpotType.COMPACT
            || spotType == SpotType.LARGE
            || spotType == SpotType.HANDICAPPED;  // cars can use handicapped spots
    }
}


// =============================================================================
// ■ Truck
//
// Large vehicle. Only fits in LARGE spots.
// Cannot use COMPACT — would take more than one spot in real life.
// =============================================================================
class Truck extends Vehicle {

    Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    }

    @Override
    public boolean canFitInSpot(SpotType spotType) {
        return spotType == SpotType.LARGE;   // Trucks need large spots only
    }
}


// =============================================================================
// ■ ElectricCar
//
// Prefers EV spots (has charger). Falls back to COMPACT or LARGE.
// Note: Doesn't forcibly require EV spot — gracefully degrades.
// =============================================================================
class ElectricCar extends Vehicle {

    ElectricCar(String licensePlate) {
        super(licensePlate, VehicleType.ELECTRIC_CAR);
    }

    @Override
    public boolean canFitInSpot(SpotType spotType) {
        return spotType == SpotType.EV
            || spotType == SpotType.COMPACT
            || spotType == SpotType.LARGE;
    }
}


// =============================================================================
// ■ VehicleFactory — Factory Method pattern
//
// Decouples vehicle creation from client code. Client specifies type + plate;
// factory returns the correctly typed Vehicle subclass.
//
// Interview point: If VehicleType were added later (e.g., AMBULANCE), only
// VehicleFactory changes — client code is unaffected (OCP).
// =============================================================================
class VehicleFactory {

    /**
     * Create a Vehicle of the given type with the given license plate.
     *
     * @param type         the vehicle category
     * @param licensePlate vehicle registration number
     * @return             concrete Vehicle subclass
     */
    static Vehicle create(VehicleType type, String licensePlate) {
        return switch (type) {
            case MOTORCYCLE    -> new Motorcycle(licensePlate);
            case CAR           -> new Car(licensePlate);
            case TRUCK         -> new Truck(licensePlate);
            case ELECTRIC_CAR  -> new ElectricCar(licensePlate);
        };
    }
}


// =============================================================================
// ■ INTERVIEW NOTES
// =============================================================================
//
// Q: Why abstract class instead of interface for Vehicle?
// A: Vehicle holds common state (licensePlate, type) that all subclasses need.
//    An interface can't hold instance state (only constants). Abstract class is
//    the right choice when subclasses share data AND must implement a method.
//    If Vehicle had no fields, an interface would be cleaner.
//
// Q: Why is canFitInSpot() defined per-vehicle instead of per-spot?
// A: Single Responsibility. The vehicle knows what it can fit into; the spot
//    knows what it can accept. In ParkingSpot.canFit(vehicle), we delegate to
//    vehicle.canFitInSpot(this.spotType) — the vehicle makes the compatibility
//    decision. This avoids a god-class that knows about every vehicle-spot combo.
//
// Q: What if a car should NOT use handicapped spots?
// A: Remove SpotType.HANDICAPPED from Car.canFitInSpot(). Business rule changes
//    are confined to one method in one class. This is OCP working correctly.
//
// Q: How does ElectricCar get an EV spot preferentially?
// A: ParkingFloor.findSpot() searches spot types in preference order. For
//    ELECTRIC_CAR, it tries EV spots first, then COMPACT, then LARGE.
//    vehicle.canFitInSpot() returns true for all three, but the search order
//    determines preference. This keeps the preference logic in ParkingFloor
//    (operational concern), not in ElectricCar (domain concern).
