package elevator_system;

public class Enums {

    public enum Direction {
        UP,
        DOWN,
        IDLE
    }

    public enum ElevatorState {
        MOVING_UP,
        MOVING_DOWN,
        IDLE,
        DOOR_OPEN,
        MAINTENANCE
    }

    public enum DoorState {
        OPEN,
        CLOSED
    }

    public enum RequestType {
        INTERNAL,
        EXTERNAL
    }
}
