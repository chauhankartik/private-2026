package elevator_system;

import elevator_system.CoreModels.Elevator;
import elevator_system.Enums.Direction;
import elevator_system.Enums.ElevatorState;

import java.util.Collections;
import java.util.TreeSet;

public class ElevatorController {
    private final Elevator elevator;

    // Upward target floors (ascending)
    private final TreeSet<Integer> upRequests = new TreeSet<>();
    
    // Downward target floors (descending)
    private final TreeSet<Integer> downRequests = new TreeSet<>(Collections.reverseOrder());

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
    }

    public Elevator getElevator() { return elevator; }

    public synchronized void addRequest(int floor) {
        int current = elevator.getCurrentFloor();
        if (floor == current) {
            System.out.printf("  🛗 Lift #%d is ALREADY at floor %d! Opening doors...%n", elevator.getId(), floor);
            elevator.getDoor().open();
            elevator.getDoor().close();
            return;
        }

        if (floor > current) {
            upRequests.add(floor);
        } else {
            downRequests.add(floor);
        }

        // Set direction if currently IDLE
        if (elevator.getDirection() == Direction.IDLE) {
            if (floor > current) {
                elevator.setDirection(Direction.UP);
                elevator.setState(ElevatorState.MOVING_UP);
            } else {
                elevator.setDirection(Direction.DOWN);
                elevator.setState(ElevatorState.MOVING_DOWN);
            }
        }
        System.out.printf("📌 Added Floor %d to Lift #%d queue (Up: %s | Down: %s)%n",
                floor, elevator.getId(), upRequests, downRequests);
    }

    /**
     * Executes one tick/step of movement in the elevator control loop.
     */
    public synchronized void step() {
        if (elevator.getState() == ElevatorState.MAINTENANCE) {
            return;
        }

        Direction dir = elevator.getDirection();
        int current = elevator.getCurrentFloor();

        if (dir == Direction.UP) {
            if (upRequests.contains(current)) {
                upRequests.remove(current);
                handleFloorStop(current);
            }

            if (!upRequests.isEmpty()) {
                elevator.setCurrentFloor(current + 1);
                elevator.setState(ElevatorState.MOVING_UP);
            } else if (!downRequests.isEmpty()) {
                elevator.setDirection(Direction.DOWN);
                elevator.setState(ElevatorState.MOVING_DOWN);
            } else {
                elevator.setDirection(Direction.IDLE);
                elevator.setState(ElevatorState.IDLE);
            }
        } else if (dir == Direction.DOWN) {
            if (downRequests.contains(current)) {
                downRequests.remove(current);
                handleFloorStop(current);
            }

            if (!downRequests.isEmpty()) {
                elevator.setCurrentFloor(current - 1);
                elevator.setState(ElevatorState.MOVING_DOWN);
            } else if (!upRequests.isEmpty()) {
                elevator.setDirection(Direction.UP);
                elevator.setState(ElevatorState.MOVING_UP);
            } else {
                elevator.setDirection(Direction.IDLE);
                elevator.setState(ElevatorState.IDLE);
            }
        } else { // IDLE state
            if (!upRequests.isEmpty()) {
                elevator.setDirection(Direction.UP);
                elevator.setState(ElevatorState.MOVING_UP);
            } else if (!downRequests.isEmpty()) {
                elevator.setDirection(Direction.DOWN);
                elevator.setState(ElevatorState.MOVING_DOWN);
            }
        }
    }

    private void handleFloorStop(int floor) {
        elevator.setState(ElevatorState.DOOR_OPEN);
        System.out.printf("🔔 [ARRIVED] Lift #%d STOPPED at Floor %d%n", elevator.getId(), floor);
        elevator.getDoor().open();
        elevator.getDoor().close();
    }

    public boolean hasPendingRequests() {
        return !upRequests.isEmpty() || !downRequests.isEmpty();
    }
}
