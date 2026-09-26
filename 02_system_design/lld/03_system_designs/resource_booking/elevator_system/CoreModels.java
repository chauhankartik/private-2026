package elevator_system;

import elevator_system.Enums.*;

public class CoreModels {

    public static class ElevatorRequest {
        private final int sourceFloor;
        private final int targetFloor;
        private final Direction direction;
        private final RequestType requestType;

        public ElevatorRequest(int sourceFloor, int targetFloor, Direction direction, RequestType requestType) {
            this.sourceFloor = sourceFloor;
            this.targetFloor = targetFloor;
            this.direction = direction;
            this.requestType = requestType;
        }

        public int getSourceFloor() { return sourceFloor; }
        public int getTargetFloor() { return targetFloor; }
        public Direction getDirection() { return direction; }
        public RequestType getRequestType() { return requestType; }

        @Override
        public String toString() {
            return String.format("Request[Type=%s, Src=%d, Target=%d, Dir=%s]",
                    requestType, sourceFloor, targetFloor, direction);
        }
    }

    public static class Door {
        private DoorState state;

        public Door() {
            this.state = DoorState.CLOSED;
        }

        public DoorState getState() { return state; }

        public void open() {
            if (state == DoorState.CLOSED) {
                state = DoorState.OPEN;
                System.out.println("  🚪 [DOOR] Opening doors...");
            }
        }

        public void close() {
            if (state == DoorState.OPEN) {
                state = DoorState.CLOSED;
                System.out.println("  🚪 [DOOR] Closing doors.");
            }
        }
    }

    public static class Display {
        private int floor;
        private Direction direction;

        public Display(int floor, Direction direction) {
            this.floor = floor;
            this.direction = direction;
        }

        public void update(int floor, Direction direction) {
            this.floor = floor;
            this.direction = direction;
        }

        public void show(int elevatorId) {
            System.out.printf("  📟 [DISPLAY LIFT #%d] Floor: %d | Direction: %s%n", elevatorId, floor, direction);
        }
    }

    public static class Elevator {
        private final int id;
        private int currentFloor;
        private Direction direction;
        private ElevatorState state;
        private final Door door;
        private final Display display;

        public Elevator(int id, int initialFloor) {
            this.id = id;
            this.currentFloor = initialFloor;
            this.direction = Direction.IDLE;
            this.state = ElevatorState.IDLE;
            this.door = new Door();
            this.display = new Display(initialFloor, Direction.IDLE);
        }

        public int getId() { return id; }
        public synchronized int getCurrentFloor() { return currentFloor; }
        public synchronized void setCurrentFloor(int currentFloor) {
            this.currentFloor = currentFloor;
            display.update(currentFloor, direction);
        }

        public synchronized Direction getDirection() { return direction; }
        public synchronized void setDirection(Direction direction) {
            this.direction = direction;
            display.update(currentFloor, direction);
        }

        public synchronized ElevatorState getState() { return state; }
        public synchronized void setState(ElevatorState state) { this.state = state; }

        public Door getDoor() { return door; }
        public Display getDisplay() { return display; }

        @Override
        public String toString() {
            return String.format("Elevator #%d [Floor %d | %s | %s]", id, currentFloor, direction, state);
        }
    }
}
