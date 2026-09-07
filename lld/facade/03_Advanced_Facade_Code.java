/**
 * ============================================================
 *  ADVANCED FACADE PATTERN — MULTI-TIER LAYERED FACADES
 * ============================================================
 *
 * Real-world complexity: a Facade can itself be a subsystem
 * to a higher-level Facade. This is "layered facades".
 *
 * Also shows:
 *   - Multiple Facades for different clients (Admin vs Customer)
 *   - Facade + Builder to configure subsystems declaratively
 *   - Async-style facade (fire-and-forget notifications)
 *
 * System: A Smart Home Hub
 *   Layer 1 Subsystems: DeviceController, EnergyMonitor, SecuritySystem
 *   Layer 1 Facades:    LightingFacade, ClimateFacade, SecurityFacade
 *   Layer 2 Facade:     SmartHomeFacade (coordinates the layer-1 facades)
 */
import java.util.*;
import java.util.function.Consumer;

// ============================================================
// LAYER 0: Raw Hardware Subsystems (low-level)
// ============================================================
class DeviceController {
    public void turnOn(String deviceId) {
        System.out.println("    [Device]  " + deviceId + " → ON");
    }
    public void turnOff(String deviceId) {
        System.out.println("    [Device]  " + deviceId + " → OFF");
    }
    public void setBrightness(String deviceId, int pct) {
        System.out.println("    [Device]  " + deviceId + " brightness → " + pct + "%");
    }
    public void setTemperature(String deviceId, int celsius) {
        System.out.println("    [Device]  " + deviceId + " → " + celsius + "°C");
    }
    public void lockDoor(String doorId) {
        System.out.println("    [Device]  Door " + doorId + " → LOCKED");
    }
    public void unlockDoor(String doorId) {
        System.out.println("    [Device]  Door " + doorId + " → UNLOCKED");
    }
}

class EnergyMonitor {
    private final Map<String, Double> usage = new HashMap<>();

    public void startTracking(String deviceId) {
        usage.put(deviceId, 0.0);
        System.out.println("    [Energy]  Tracking started for " + deviceId);
    }
    public void stopTracking(String deviceId) {
        System.out.println("    [Energy]  Tracking stopped for " + deviceId
            + " — used " + usage.getOrDefault(deviceId, 0.0) + " kWh");
        usage.remove(deviceId);
    }
    public double getTotalUsage() {
        return usage.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}

class SecuritySystem {
    public void arm()            { System.out.println("    [Security] System ARMED"); }
    public void disarm()         { System.out.println("    [Security] System DISARMED"); }
    public void enableCameras()  { System.out.println("    [Security] Cameras ENABLED"); }
    public void disableCameras() { System.out.println("    [Security] Cameras DISABLED"); }
    public void setAlertLevel(String level) {
        System.out.println("    [Security] Alert level → " + level);
    }
}

class NotificationHub {
    // Consumer = async callback (simulating fire-and-forget)
    public void notify(String user, String message, Consumer<String> callback) {
        System.out.println("    [Notify]  → " + user + ": " + message);
        if (callback != null) callback.accept("delivered");
    }
}

// ============================================================
// LAYER 1: Domain Facades (coordinate subsystems)
// ============================================================

/**
 * Facade for lighting-related operations across the whole home.
 */
class LightingFacade {

    private final DeviceController devices;
    private final EnergyMonitor    energy;

    LightingFacade(DeviceController devices, EnergyMonitor energy) {
        this.devices = devices;
        this.energy  = energy;
    }

    public void movieMode() {
        System.out.println("  [Lighting] → Movie mode");
        devices.setBrightness("LIVING_ROOM_LIGHTS", 10);
        devices.turnOff("HALL_LIGHTS");
        devices.turnOff("KITCHEN_LIGHTS");
    }

    public void awayMode() {
        System.out.println("  [Lighting] → Away mode (all off)");
        for (String light : List.of("LIVING_ROOM_LIGHTS", "HALL_LIGHTS",
                                    "KITCHEN_LIGHTS", "BEDROOM_LIGHTS")) {
            devices.turnOff(light);
            energy.stopTracking(light);
        }
    }

    public void morningMode() {
        System.out.println("  [Lighting] → Morning mode");
        devices.turnOn("KITCHEN_LIGHTS");
        devices.setBrightness("BEDROOM_LIGHTS", 40);
        energy.startTracking("KITCHEN_LIGHTS");
    }
}

/**
 * Facade for climate control.
 */
class ClimateFacade {

    private final DeviceController devices;

    ClimateFacade(DeviceController devices) {
        this.devices = devices;
    }

    public void sleepMode() {
        System.out.println("  [Climate]  → Sleep mode");
        devices.setTemperature("AC_BEDROOM", 22);
        devices.turnOff("AC_LIVING_ROOM");
    }

    public void awayMode() {
        System.out.println("  [Climate]  → Away mode (eco)");
        devices.setTemperature("AC_BEDROOM", 30);    // eco temperature
        devices.setTemperature("AC_LIVING_ROOM", 30);
    }

    public void comfortMode() {
        System.out.println("  [Climate]  → Comfort mode");
        devices.setTemperature("AC_BEDROOM", 24);
        devices.setTemperature("AC_LIVING_ROOM", 24);
    }
}

/**
 * Facade for security operations.
 */
class SecurityFacade {

    private final SecuritySystem  security;
    private final DeviceController devices;

    SecurityFacade(SecuritySystem security, DeviceController devices) {
        this.security = security;
        this.devices  = devices;
    }

    public void leaveHome() {
        System.out.println("  [Security] → Leave-home security");
        devices.lockDoor("FRONT_DOOR");
        devices.lockDoor("BACK_DOOR");
        security.arm();
        security.enableCameras();
        security.setAlertLevel("HIGH");
    }

    public void arriveHome() {
        System.out.println("  [Security] → Arrive-home security");
        security.disarm();
        security.setAlertLevel("LOW");
        devices.unlockDoor("FRONT_DOOR");
    }

    public void nightMode() {
        System.out.println("  [Security] → Night mode security");
        devices.lockDoor("FRONT_DOOR");
        devices.lockDoor("BACK_DOOR");
        security.arm();
        security.setAlertLevel("MEDIUM");
        security.disableCameras();   // privacy: indoor cameras off at night
    }
}

// ============================================================
// LAYER 2: Top-Level Facade (coordinates the Layer 1 Facades)
// ============================================================
/**
 * The highest-level entry point.
 * The user (or app) calls this. It coordinates the Layer 1 Facades.
 *
 * "Facade of Facades" — each method represents a complete home scenario.
 */
class SmartHomeFacade {

    private final LightingFacade  lighting;
    private final ClimateFacade   climate;
    private final SecurityFacade  security;
    private final NotificationHub notifications;
    private final String          owner;

    SmartHomeFacade(LightingFacade lighting,
                    ClimateFacade climate,
                    SecurityFacade security,
                    NotificationHub notifications,
                    String owner) {
        this.lighting      = lighting;
        this.climate       = climate;
        this.security      = security;
        this.notifications = notifications;
        this.owner         = owner;
    }

    /** Single button press → entire "Watch Movie" scenario */
    public void movieMode() {
        System.out.println("\n>> [SmartHome] MOVIE MODE activated");
        lighting.movieMode();
        climate.comfortMode();
        // security: stay as-is (home occupied)
        notifications.notify(owner, "Movie mode activated 🎬", null);
    }

    /** "I'm leaving home" → full away scenario */
    public void leaveHome() {
        System.out.println("\n>> [SmartHome] LEAVING HOME");
        lighting.awayMode();
        climate.awayMode();
        security.leaveHome();
        notifications.notify(owner, "Home secured — have a good day! 🏠",
            status -> System.out.println("    [Notify]  Delivery status: " + status));
    }

    /** "I'm back home" → welcome scenario */
    public void arriveHome() {
        System.out.println("\n>> [SmartHome] ARRIVING HOME");
        security.arriveHome();
        lighting.morningMode();
        climate.comfortMode();
        notifications.notify(owner, "Welcome home! 🏡", null);
    }

    /** Bedtime → full night scenario */
    public void bedtime() {
        System.out.println("\n>> [SmartHome] BEDTIME MODE");
        lighting.awayMode();
        climate.sleepMode();
        security.nightMode();
        notifications.notify(owner, "Goodnight! Sleep well 🌙", null);
    }
}

// ============================================================
// Builder for convenient SmartHomeFacade creation
// ============================================================
class SmartHomeBuilder {
    private String owner = "default-user";

    public SmartHomeBuilder owner(String owner) {
        this.owner = owner;
        return this;
    }

    public SmartHomeFacade build() {
        DeviceController devices    = new DeviceController();
        EnergyMonitor    energy     = new EnergyMonitor();
        SecuritySystem   security   = new SecuritySystem();
        NotificationHub  notify     = new NotificationHub();

        return new SmartHomeFacade(
            new LightingFacade(devices, energy),
            new ClimateFacade(devices),
            new SecurityFacade(security, devices),
            notify,
            owner
        );
    }
}

// ============================================================
// Client — the Mobile App or Voice Assistant
// ============================================================
public class AdvancedFacadeDemo {
    public static void main(String[] args) {

        // User configures their smart home via a Builder
        SmartHomeFacade home = new SmartHomeBuilder()
            .owner("Rahul")
            .build();

        // --- Simulate a day in the life ---

        home.movieMode();
        home.leaveHome();
        home.arriveHome();
        home.bedtime();
    }
}

/**
 * 🧠 KEY OBSERVATIONS
 *
 * 1. LAYERED FACADES:
 *    SmartHomeFacade → [LightingFacade, ClimateFacade, SecurityFacade]
 *    Each of those → [DeviceController, EnergyMonitor, SecuritySystem]
 *    The client only sees ONE class with FOUR simple methods.
 *
 * 2. FACADE + BUILDER:
 *    Using a Builder to construct the facade avoids a massive constructor.
 *    This is the standard Spring pattern (builder methods / @Configuration).
 *
 * 3. MULTIPLE FACADES FOR DIFFERENT CLIENTS:
 *    You could expose LightingFacade directly to a "smart lighting" app,
 *    while SmartHomeFacade serves the main mobile app.
 *    Same subsystems, different access levels.
 *
 * 4. TESTING STRATEGY:
 *    Unit test each Layer 1 Facade in isolation (mock DeviceController).
 *    Integration test SmartHomeFacade with real Layer 1 Facades.
 *    E2E test is just calling home.leaveHome() and asserting the notifications.
 *
 * 5. STAFF-LEVEL INTERVIEW POINT:
 *    "Thin facades" only coordinate. "Fat facades" add logic.
 *    Prefer thin. If you catch yourself writing IF statements in the facade
 *    about WHICH subsystem to call, you may be adding business logic that
 *    belongs in its own service. The facade should just orchestrate, not decide.
 */
