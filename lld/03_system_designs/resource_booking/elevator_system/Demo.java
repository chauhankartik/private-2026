package elevator_system;

import elevator_system.CoreModels.Elevator;
import elevator_system.DispatchStrategy.LOOKDispatchStrategy;
import elevator_system.DispatchStrategy.SSTFDispatchStrategy;
import elevator_system.Enums.Direction;

import java.util.concurrent.*;

public class Demo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 🛗 ELEVATOR CONTROL SYSTEM — LLD INTERVIEW DEMO (LOOK Algorithm)");
        System.out.println("======================================================================\n");

        int totalFloors = 10;
        ElevatorSystem system = ElevatorSystem.getInstance(totalFloors);

        // -------------------------------------------------------------------------
        // SCENARIO 1: BUILDING SETUP (3 Elevators placed at different floors)
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 1: Initializing 10-Floor Building with 3 Elevators...");
        system.addElevator(new Elevator(1, 1)); // Lift #1 at Floor 1
        system.addElevator(new Elevator(2, 5)); // Lift #2 at Floor 5
        system.addElevator(new Elevator(3, 8)); // Lift #3 at Floor 8

        // -------------------------------------------------------------------------
        // SCENARIO 2: EXTERNAL HALL REQUESTS
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 2: Receiving External Hall Requests...");
        
        // Passenger on Floor 3 wants to go UP
        system.requestElevator(3, Direction.UP);
        
        // Passenger on Floor 9 wants to go DOWN
        system.requestElevator(9, Direction.DOWN);

        // -------------------------------------------------------------------------
        // SCENARIO 3: INTERNAL BUTTON PRESSES
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 3: Passengers Pressing Internal Floor Buttons inside Lifts...");
        
        // Passenger inside Lift #1 selects Floor 7
        system.pressInternalButton(1, 7);

        // -------------------------------------------------------------------------
        // SCENARIO 4: SIMULATION TICK STEP EXECUTION (LOOK Algorithm)
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 4: Executing Movement Tick Simulation (LOOK Elevator Algorithm)...");
        system.runSimulation(12);

        // -------------------------------------------------------------------------
        // SCENARIO 5: CONCURRENT REQUEST DISPATCHING TEST
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 5: Testing Concurrent Request Dispatching (5 Passengers simultaneously)...");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            try {
                latch.await();
                system.requestElevator(2, Direction.UP);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        executor.submit(() -> {
            try {
                latch.await();
                system.requestElevator(6, Direction.DOWN);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        executor.submit(() -> {
            try {
                latch.await();
                system.requestElevator(10, Direction.DOWN);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);

        System.out.println("\n🎬 Running final simulation steps after concurrent requests...");
        system.runSimulation(10);

        System.out.println("\n======================================================================");
        System.out.println(" 🛗 ELEVATOR CONTROL SYSTEM DEMO COMPLETED SUCCESSFULLY");
        System.out.println("======================================================================");
    }
}
