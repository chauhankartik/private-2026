package elevator_system;

import elevator_system.CoreModels.ElevatorRequest;
import elevator_system.Enums.Direction;
import elevator_system.Enums.ElevatorState;

import java.util.List;

public class DispatchStrategy {

    public interface IDispatchStrategy {
        ElevatorController selectElevator(List<ElevatorController> elevators, ElevatorRequest request);
    }

    /**
     * LOOK Algorithm Dispatcher: Evaluates elevators moving towards target floor in same direction first,
     * then idle elevators by distance, then any closest elevator.
     */
    public static class LOOKDispatchStrategy implements IDispatchStrategy {
        @Override
        public ElevatorController selectElevator(List<ElevatorController> elevators, ElevatorRequest request) {
            ElevatorController bestElevator = null;
            int minCost = Integer.MAX_VALUE;

            for (ElevatorController controller : elevators) {
                if (controller.getElevator().getState() == ElevatorState.MAINTENANCE) {
                    continue;
                }

                int currentFloor = controller.getElevator().getCurrentFloor();
                Direction dir = controller.getElevator().getDirection();
                int cost;

                // Scenario 1: Elevator is IDLE
                if (dir == Direction.IDLE) {
                    cost = Math.abs(currentFloor - request.getSourceFloor());
                }
                // Scenario 2: Elevator moving UP and request is above current floor with UP direction
                else if (dir == Direction.UP && request.getDirection() == Direction.UP && request.getSourceFloor() >= currentFloor) {
                    cost = request.getSourceFloor() - currentFloor;
                }
                // Scenario 3: Elevator moving DOWN and request is below current floor with DOWN direction
                else if (dir == Direction.DOWN && request.getDirection() == Direction.DOWN && request.getSourceFloor() <= currentFloor) {
                    cost = currentFloor - request.getSourceFloor();
                }
                // Scenario 4: Opposite direction or passed floor (higher penalty)
                else {
                    cost = Math.abs(currentFloor - request.getSourceFloor()) + 100;
                }

                if (cost < minCost) {
                    minCost = cost;
                    bestElevator = controller;
                }
            }
            return bestElevator != null ? bestElevator : elevators.get(0);
        }
    }

    /**
     * Shortest Seek Time First (SSTF): Simple distance-based dispatcher.
     */
    public static class SSTFDispatchStrategy implements IDispatchStrategy {
        @Override
        public ElevatorController selectElevator(List<ElevatorController> elevators, ElevatorRequest request) {
            ElevatorController closest = null;
            int minDistance = Integer.MAX_VALUE;

            for (ElevatorController controller : elevators) {
                if (controller.getElevator().getState() == ElevatorState.MAINTENANCE) {
                    continue;
                }
                int dist = Math.abs(controller.getElevator().getCurrentFloor() - request.getSourceFloor());
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = controller;
                }
            }
            return closest != null ? closest : elevators.get(0);
        }
    }
}
