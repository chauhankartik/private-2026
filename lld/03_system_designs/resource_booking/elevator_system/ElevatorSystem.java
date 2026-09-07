package elevator_system;

import elevator_system.CoreModels.Elevator;
import elevator_system.CoreModels.ElevatorRequest;
import elevator_system.DispatchStrategy.IDispatchStrategy;
import elevator_system.DispatchStrategy.LOOKDispatchStrategy;
import elevator_system.Enums.Direction;
import elevator_system.Enums.RequestType;

import java.util.ArrayList;
import java.util.List;

public class ElevatorSystem {
    private static volatile ElevatorSystem instance;

    private final int totalFloors;
    private final List<ElevatorController> controllers = new ArrayList<>();
    private IDispatchStrategy dispatchStrategy;

    private ElevatorSystem(int totalFloors) {
        this.totalFloors = totalFloors;
        this.dispatchStrategy = new LOOKDispatchStrategy();
    }

    public static ElevatorSystem getInstance(int totalFloors) {
        if (instance == null) {
            synchronized (ElevatorSystem.class) {
                if (instance == null) {
                    instance = new ElevatorSystem(totalFloors);
                }
            }
        }
        return instance;
    }

    public void setDispatchStrategy(IDispatchStrategy strategy) {
        this.dispatchStrategy = strategy;
    }

    public void addElevator(Elevator elevator) {
        controllers.add(new ElevatorController(elevator));
        System.out.printf("🏢 Added Lift #%d at Floor %d%n", elevator.getId(), elevator.getCurrentFloor());
    }

    public void requestElevator(int sourceFloor, Direction direction) {
        if (sourceFloor < 1 || sourceFloor > totalFloors) {
            System.out.println("❌ Invalid floor request: " + sourceFloor);
            return;
        }

        System.out.printf("%n🛎️ [EXTERNAL HALL REQUEST] Floor %d requests %s lift%n", sourceFloor, direction);
        ElevatorRequest request = new ElevatorRequest(sourceFloor, sourceFloor, direction, RequestType.EXTERNAL);
        
        ElevatorController bestController = dispatchStrategy.selectElevator(controllers, request);
        System.out.printf("🎯 Dispatched Lift #%d for Floor %d request%n", bestController.getElevator().getId(), sourceFloor);
        bestController.addRequest(sourceFloor);
    }

    public void pressInternalButton(int elevatorId, int destinationFloor) {
        if (destinationFloor < 1 || destinationFloor > totalFloors) {
            System.out.println("❌ Invalid destination floor: " + destinationFloor);
            return;
        }

        System.out.printf("%n🔘 [INTERNAL BUTTON] Passenger inside Lift #%d pressed Floor %d%n", elevatorId, destinationFloor);
        for (ElevatorController controller : controllers) {
            if (controller.getElevator().getId() == elevatorId) {
                controller.addRequest(destinationFloor);
                return;
            }
        }
        System.out.println("❌ Elevator ID not found: " + elevatorId);
    }

    public void stepAll() {
        for (ElevatorController controller : controllers) {
            controller.step();
        }
    }

    public void runSimulation(int maxSteps) throws InterruptedException {
        System.out.println("\n🎬 --- STARTING ELEVATOR MOVEMENT SIMULATION ---");
        for (int step = 1; step <= maxSteps; step++) {
            boolean hasPending = false;
            for (ElevatorController controller : controllers) {
                if (controller.hasPendingRequests()) {
                    hasPending = true;
                    break;
                }
            }
            if (!hasPending && step > 2) {
                System.out.println("✅ All elevator queues are idle. Simulation complete.");
                break;
            }

            System.out.printf("%n--- Simulation Tick #%d ---%n", step);
            stepAll();
            for (ElevatorController controller : controllers) {
                controller.getElevator().getDisplay().show(controller.getElevator().getId());
            }
            Thread.sleep(300);
        }
    }

    public List<ElevatorController> getControllers() {
        return controllers;
    }
}
