package command;

import java.util.Stack;

/**
 * Production-grade Java demonstration of the Command Pattern.
 * Scenario: Smart Home Remote Invoker with Undo / Redo Command Stack.
 */
public class CommandDemo {

    // --- Command Interface ---
    public interface Command {
        void execute();
        void undo();
    }

    // --- Receiver Class ---
    public static class Light {
        private final String location;
        private boolean isOn = false;

        public Light(String location) {
            this.location = location;
        }

        public void turnOn() {
            isOn = true;
            System.out.println("[" + location + " Light] Turned ON");
        }

        public void turnOff() {
            isOn = false;
            System.out.println("[" + location + " Light] Turned OFF");
        }

        public boolean isOn() { return isOn; }
    }

    // --- Concrete Commands ---
    public static class LightOnCommand implements Command {
        private final Light light;

        public LightOnCommand(Light light) {
            this.light = light;
        }

        @Override
        public void execute() {
            light.turnOn();
        }

        @Override
        public void undo() {
            light.turnOff();
        }
    }

    public static class LightOffCommand implements Command {
        private final Light light;

        public LightOffCommand(Light light) {
            this.light = light;
        }

        @Override
        public void execute() {
            light.turnOff();
        }

        @Override
        public void undo() {
            light.turnOn();
        }
    }

    // --- Invoker Class with Undo/Redo Stacks ---
    public static class RemoteInvoker {
        private final Stack<Command> historyStack = new Stack<>();
        private final Stack<Command> redoStack = new Stack<>();

        public void executeCommand(Command command) {
            command.execute();
            historyStack.push(command);
            redoStack.clear(); // Clear redo stack on new command
        }

        public void pressUndo() {
            if (historyStack.isEmpty()) {
                System.out.println("Nothing to UNDO!");
                return;
            }
            Command command = historyStack.pop();
            System.out.print("[UNDO] ");
            command.undo();
            redoStack.push(command);
        }

        public void pressRedo() {
            if (redoStack.isEmpty()) {
                System.out.println("Nothing to REDO!");
                return;
            }
            Command command = redoStack.pop();
            System.out.print("[REDO] ");
            command.execute();
            historyStack.push(command);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Command Pattern Demo ===");

        Light livingRoomLight = new Light("Living Room");
        Command lightOn = new LightOnCommand(livingRoomLight);
        Command lightOff = new LightOffCommand(livingRoomLight);

        RemoteInvoker remote = new RemoteInvoker();

        // Turn On
        remote.executeCommand(lightOn);

        // Turn Off
        remote.executeCommand(lightOff);

        // Perform UNDO
        System.out.println("\n--- Testing Undo ---");
        remote.pressUndo(); // Undoes Turn Off -> Turns Light back ON

        // Perform REDO
        System.out.println("\n--- Testing Redo ---");
        remote.pressRedo(); // Redoes Turn Off -> Turns Light OFF
    }
}
