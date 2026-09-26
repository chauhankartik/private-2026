/**
 * ============================================================
 *  BASIC FACADE PATTERN — THE HOME THEATER ANALOGY
 * ============================================================
 *
 * The simplest, cleanest implementation.
 * Understand this FIRST before looking at anything advanced.
 *
 * The trick: One high-level method hides a sequence of low-level
 * calls across many different subsystem classes.
 */

// ----------------------------------------------------
// 1. The Subsystem Classes
// ----------------------------------------------------
/**
 * Each class does ONE thing and does it well.
 * They know NOTHING about the facade or each other.
 * They are perfectly usable on their own.
 */
class Lights {
    public void on()       { System.out.println("[Lights]    Turning ON"); }
    public void off()      { System.out.println("[Lights]    Turning OFF"); }
    public void dim(int level) {
        System.out.println("[Lights]    Dimming to " + level + "%");
    }
}

class Screen {
    public void lower()    { System.out.println("[Screen]    Lowering screen"); }
    public void raise()    { System.out.println("[Screen]    Raising screen"); }
}

class Projector {
    public void on()       { System.out.println("[Projector] Turning ON"); }
    public void off()      { System.out.println("[Projector] Turning OFF"); }
    public void setInput(String input) {
        System.out.println("[Projector] Input set to " + input);
    }
}

class SoundSystem {
    public void on()       { System.out.println("[Sound]     Turning ON"); }
    public void off()      { System.out.println("[Sound]     Turning OFF"); }
    public void setVolume(int level) {
        System.out.println("[Sound]     Volume set to " + level);
    }
    public void setMode(String mode) {
        System.out.println("[Sound]     Mode set to " + mode);
    }
}

class StreamingApp {
    public void launch()   { System.out.println("[Streaming] Launching app"); }
    public void close()    { System.out.println("[Streaming] Closing app"); }
}

// ----------------------------------------------------
// 2. The Facade
// ----------------------------------------------------
/**
 * THIS IS THE HEART OF THE PATTERN.
 *
 * The Facade:
 *   - Holds references to all subsystem objects.
 *   - Exposes simple, high-level methods.
 *   - Each method orchestrates the correct sequence of subsystem calls.
 *   - The client never needs to know what happens inside.
 */
class HomeTheaterFacade {

    // References to every subsystem
    private final Lights lights;
    private final Screen screen;
    private final Projector projector;
    private final SoundSystem sound;
    private final StreamingApp streaming;

    // Facade creates (or receives) all its dependencies
    public HomeTheaterFacade() {
        this.lights    = new Lights();
        this.screen    = new Screen();
        this.projector = new Projector();
        this.sound     = new SoundSystem();
        this.streaming = new StreamingApp();
    }

    /**
     * High-level "watch movie" operation.
     * Hides 7 subsystem calls behind 1 simple method.
     */
    public void watchMovie() {
        System.out.println("\n>> Preparing home theater for movie...");
        lights.dim(10);
        screen.lower();
        projector.on();
        projector.setInput("HDMI1");
        sound.on();
        sound.setMode("CINEMA");
        sound.setVolume(50);
        streaming.launch();
        System.out.println(">> Enjoy the movie!\n");
    }

    /**
     * High-level "end movie" operation.
     */
    public void endMovie() {
        System.out.println("\n>> Shutting down home theater...");
        streaming.close();
        sound.off();
        projector.off();
        screen.raise();
        lights.on();
        System.out.println(">> Goodnight!\n");
    }
}

// ----------------------------------------------------
// 3. Let's Run It!
// ----------------------------------------------------
public class BasicFacadeDemo {
    public static void main(String[] args) {

        // Client code — talks ONLY to the facade
        HomeTheaterFacade homeTheater = new HomeTheaterFacade();

        // One call hides 7 subsystem calls
        homeTheater.watchMovie();

        // Simulate watching...
        System.out.println("    ... [Movie Playing] ...");

        // One call to shut everything down
        homeTheater.endMovie();
    }
}

/**
 * 🧠 WHAT JUST HAPPENED?
 *
 * WITHOUT the Facade, the client would need:
 *   - Knowledge of 5 different classes
 *   - Knowledge of the correct ordering of calls
 *   - Code duplication in every class that wants to "watch a movie"
 *
 * WITH the Facade, the client:
 *   - Knows ONE class: HomeTheaterFacade
 *   - Calls ONE method: watchMovie()
 *   - Is completely decoupled from the subsystems
 *
 * If you later add a SmartThermostat subsystem that needs to
 * set the temperature for "movie mode", you only change the Facade.
 * The client code is untouched.
 *
 *
 * 💡 INTERVIEW TAKEAWAY:
 *   "The Facade pattern is something I use every time I write a Service
 *    layer in a Spring application. My OrderService.placeOrder() is a
 *    facade — the REST controller calls one method, and internally it
 *    coordinates InventoryService, PaymentGateway, ShippingService, and
 *    NotificationService. The controller has zero coupling to any of those."
 */
