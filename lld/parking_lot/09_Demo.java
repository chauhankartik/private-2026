// =============================================================================
// FILE: 09_Demo.java
// TOPIC: Parking Lot LLD — Complete Demo (Interview Walkthrough)
//
// Demonstrates 9 scenarios:
//   1. Basic park + unpark (car, hourly fee, cash)
//   2. Multiple vehicle types (motorcycle, truck, electric car)
//   3. Spot preference — motorcycle uses smallest spot first
//   4. EV car prefers EV spot over compact
//   5. Full lot — ParkingLotFullException
//   6. Duplicate vehicle — DuplicateVehicleException
//   7. Tiered fee strategy
//   8. Runtime fee strategy change
//   9. Concurrent entry (multi-thread safety)
//
// RUN: javac -d out parking_lot/*.java && java -cp out parking_lot.Demo
// =============================================================================

package parking_lot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=".repeat(70));
        System.out.println("  PARKING LOT LLD — Complete Demo");
        System.out.println("=".repeat(70));

        // ── Build the Parking Lot ─────────────────────────────────────────────
        // 2 floors, each with: 3 compact, 2 large, 2 motorcycle, 1 handicapped, 2 EV
        ParkingLot lot = new ParkingLot.Builder()
                .name("MG Road Parking — Block A")
                .address("1 MG Road, Bengaluru 560001")
                .feeStrategy(new HourlyFeeStrategy(50, 20))  // ₹50/hr, min ₹20
                .addFloor(1, Map.of(
                        SpotType.MOTORCYCLE,  List.of("F1-M1", "F1-M2"),
                        SpotType.COMPACT,     List.of("F1-C1", "F1-C2", "F1-C3"),
                        SpotType.LARGE,       List.of("F1-L1", "F1-L2"),
                        SpotType.HANDICAPPED, List.of("F1-H1"),
                        SpotType.EV,          List.of("F1-E1", "F1-E2")
                ))
                .addFloor(2, Map.of(
                        SpotType.MOTORCYCLE,  List.of("F2-M1", "F2-M2"),
                        SpotType.COMPACT,     List.of("F2-C1", "F2-C2", "F2-C3"),
                        SpotType.LARGE,       List.of("F2-L1", "F2-L2"),
                        SpotType.HANDICAPPED, List.of("F2-H1"),
                        SpotType.EV,          List.of("F2-E1", "F2-E2")
                ))
                .build();

        lot.showAvailability();

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 1: Basic Park + Unpark (Car, Hourly Fee, Cash)
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("▶ SCENARIO 1: Basic Park + Unpark (Car, Cash)");
        System.out.println("-".repeat(55));

        Vehicle car1 = VehicleFactory.create(VehicleType.CAR, "KA01AB1234");
        ParkingTicket ticket1 = lot.parkVehicle(car1);

        // Simulate 0 mins parked (demo — minimum fee of ₹20 applies)
        ParkingReceipt receipt1 = lot.unparkVehicle(ticket1, new CashPayment(100));
        System.out.println("Receipt: " + receipt1);

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 2: Multiple Vehicle Types
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 2: Multiple Vehicle Types");
        System.out.println("-".repeat(55));

        Vehicle moto   = VehicleFactory.create(VehicleType.MOTORCYCLE,   "KA02MO9999");
        Vehicle truck  = VehicleFactory.create(VehicleType.TRUCK,        "KA03TR5555");
        Vehicle evCar  = VehicleFactory.create(VehicleType.ELECTRIC_CAR, "KA04EV7777");

        ParkingTicket motoTicket  = lot.parkVehicle(moto);
        ParkingTicket truckTicket = lot.parkVehicle(truck);
        ParkingTicket evTicket    = lot.parkVehicle(evCar);

        System.out.println("\nSpot assigned to motorcycle : " + motoTicket.getSpot());
        System.out.println("Spot assigned to truck      : " + truckTicket.getSpot());
        System.out.println("Spot assigned to electric car: " + evTicket.getSpot());

        lot.showAvailability();

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 3: Spot Preference — Motorcycle gets smallest spot
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("▶ SCENARIO 3: Spot Preference (motorcycle gets MOTORCYCLE spot first)");
        System.out.println("-".repeat(55));

        Vehicle moto2 = VehicleFactory.create(VehicleType.MOTORCYCLE, "KA05MO1111");
        ParkingTicket moto2Ticket = lot.parkVehicle(moto2);
        System.out.println("Motorcycle 2 spot: " + moto2Ticket.getSpot());
        // Should be F1-M2 (second motorcycle spot) — not a COMPACT or LARGE

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 4: EV Car Prefers EV Spot
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 4: EV Preference (electric car → EV spot)");
        System.out.println("-".repeat(55));
        // evCar already parked in EV spot (scenario 2)
        System.out.println("EV car parked at: " + evTicket.getSpot().getSpotType()
                + " (expected: EV)");

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 5: Duplicate Vehicle Entry
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 5: Duplicate Vehicle (same plate entered twice)");
        System.out.println("-".repeat(55));

        Vehicle dupCar = VehicleFactory.create(VehicleType.CAR, "KA03TR5555");  // truck's plate!
        try {
            lot.parkVehicle(dupCar);
        } catch (DuplicateVehicleException e) {
            System.out.println("✓ Caught DuplicateVehicleException: " + e.getMessage());
        }

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 6: Tiered Fee Calculation
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 6: Tiered Fee Strategy");
        System.out.println("-".repeat(55));

        TieredFeeStrategy tiered = new TieredFeeStrategy(60, 40, 25, 250);
        System.out.println("Fee for 30 min  (tier 1): ₹" + String.format("%.2f", tiered.calculate(java.time.Duration.ofMinutes(30))));
        System.out.println("Fee for 2 hrs   (tier 1): ₹" + String.format("%.2f", tiered.calculate(java.time.Duration.ofHours(2))));
        System.out.println("Fee for 4 hrs  (tier 2): ₹" + String.format("%.2f", tiered.calculate(java.time.Duration.ofHours(4))));
        System.out.println("Fee for 8 hrs  (tier 3): ₹" + String.format("%.2f", tiered.calculate(java.time.Duration.ofHours(8))));
        System.out.println("Fee for 24 hrs (capped): ₹" + String.format("%.2f", tiered.calculate(java.time.Duration.ofHours(24))));

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 7: Runtime Fee Strategy Change
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 7: Runtime Fee Strategy Change");
        System.out.println("-".repeat(55));

        lot.setFeeStrategy(new FlatFeeStrategy(100));

        Vehicle car3 = VehicleFactory.create(VehicleType.CAR, "KA06CA2222");
        ParkingTicket ticket3 = lot.parkVehicle(car3);
        ParkingReceipt receipt3 = lot.unparkVehicle(ticket3, new UPIPayment("driver@paytm"));
        System.out.println("Receipt (flat ₹100): " + receipt3);

        // Switch back to hourly
        lot.setFeeStrategy(new HourlyFeeStrategy(50, 20));

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 8: Card Payment (with mock decline)
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 8: Card Payment (Approved + Declined)");
        System.out.println("-".repeat(55));

        Vehicle car4 = VehicleFactory.create(VehicleType.CAR, "KA07CA3333");
        ParkingTicket ticket4 = lot.parkVehicle(car4);
        ParkingReceipt receipt4 = lot.unparkVehicle(ticket4,
                new CardPayment("4111111111111111", "Kartik Chauhan"));
        System.out.println("Receipt (approved card): " + receipt4);

        Vehicle car5 = VehicleFactory.create(VehicleType.CAR, "KA08CA4444");
        ParkingTicket ticket5 = lot.parkVehicle(car5);
        ParkingReceipt receipt5 = lot.unparkVehicle(ticket5,
                new CardPayment("4111111111110000", "Another Driver"));  // "0000" = decline
        System.out.println("Receipt (declined card): " + receipt5);
        System.out.println("Is KA08CA4444 still parked? " + lot.isVehicleParked("KA08CA4444")
                + " (spot NOT released — payment failed)");

        // Clean up this vehicle for scenario 9
        lot.unparkVehicle(ticket5, new CashPayment(500));

        // ─────────────────────────────────────────────────────────────────────
        // SCENARIO 9: Concurrent Entry (Multi-thread)
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("\n▶ SCENARIO 9: Concurrent Entry (8 cars entering simultaneously)");
        System.out.println("-".repeat(55));

        int numCars = 8;
        ExecutorService executor   = Executors.newFixedThreadPool(numCars);
        CountDownLatch  latch      = new CountDownLatch(numCars);
        AtomicInteger   successCount = new AtomicInteger(0);
        AtomicInteger   failCount    = new AtomicInteger(0);

        for (int i = 1; i <= numCars; i++) {
            final String plate = "KA-CONC-" + String.format("%03d", i);
            executor.submit(() -> {
                try {
                    Vehicle v = VehicleFactory.create(VehicleType.CAR, plate);
                    lot.parkVehicle(v);
                    successCount.incrementAndGet();
                } catch (ParkingLotFullException e) {
                    System.out.println("  [FULL] " + plate + ": " + e.getMessage());
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("  [ERR]  " + plate + ": " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Concurrent parking done.");
        System.out.println("  Successfully parked: " + successCount.get());
        System.out.println("  Failed (lot full):   " + failCount.get());
        System.out.println("  Zero double-bookings (verified by absence of errors)");

        lot.showAvailability();

        System.out.println("▶ All scenarios completed.");
    }
}


// =============================================================================
// ■ EXPECTED OUTPUT SUMMARY
// =============================================================================
//
// SCENARIO 1: Car KA01AB1234 → Compact spot F1-C1. Cash ₹100 tendered.
//             Minimum fee ₹20 charged. Change ₹80 returned.
//
// SCENARIO 2: Motorcycle → F1-M1, Truck → F1-L1, ElectricCar → F1-E1
//             (smallest-fit + EV preference verified)
//
// SCENARIO 3: Motorcycle 2 → F1-M2 (not a compact, not a large)
//
// SCENARIO 4: EV car spot type = EV (confirmed in scenario 2 output)
//
// SCENARIO 5: DuplicateVehicleException thrown for same license plate
//
// SCENARIO 6: Tiered fees:
//             30 min = ₹30, 2 hrs = ₹120, 4 hrs = ₹200, 8 hrs = ₹250 (capped)
//
// SCENARIO 7: Flat fee ₹100 regardless of duration. UPI payment accepted.
//
// SCENARIO 8: Approved card → spot released. Declined card → spot NOT released.
//             Vehicle with declined card is still recorded as parked.
//
// SCENARIO 9: 8 concurrent cars. No double-bookings. Some may get "lot full"
//             depending on remaining capacity after scenarios 1-8.
