import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ============================================================
 *  OBSERVER PATTERN — STAFF SWE IMPLEMENTATION
 * ============================================================
 *
 * This implementation solves the catastrophic failures of naive
 * Observer patterns:
 * 1. ConcurrentModificationException (solved via CopyOnWriteArrayList)
 * 2. Exception isolation / Poison Pill (solved via try-catch loop boundaries)
 * 3. Lapsed Listener / Memory Leaks (solved via WeakReference wrappers)
 * 4. Thread starvation / Blocking (demonstrated via ExecutorService async dispatch)
 */

// ----------------------------------------------------
// 1. Define immutable Event (Push Model)
// ----------------------------------------------------
/**
 * Good Practice: Use immutable Event classes so Observers cannot
 * accidentally modify data being passed to other Observers.
 */
class StateChangeEvent {
    private final long timestamp;
    private final String newState;

    public StateChangeEvent(String newState) {
        this.timestamp = System.currentTimeMillis();
        this.newState = newState;
    }

    public String getNewState() { return newState; }
    public long getTimestamp() { return timestamp; }
}

enum NotificationThread {
    MAIN_SYNC,
    ASYNC_BACKGROUND
}

// ----------------------------------------------------
// 2. Observer Interface
// ----------------------------------------------------
interface Observer {
    void onStateChanged(StateChangeEvent event);
}

// ----------------------------------------------------
// 3. Subject (The robust Publisher)
// ----------------------------------------------------
class EnterpriseSubject {

    // 1. Thread-safe list prevents ConcurrentModificationException if an observer
    //    deregisters itself while being notified.
    // 2. We use WeakReference to solve the "Lapsed Listener Problem" (Memory Leaks)
    private final List<WeakReference<Observer>> observers = new CopyOnWriteArrayList<>();

    // For background async processing (Backpressure & Threading control)
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public void attach(Observer observer) {
        // First check if it already exists to avoid duplicates
        for (WeakReference<Observer> ref : observers) {
            if (ref.get() == observer) return;
        }
        observers.add(new WeakReference<>(observer));
        System.out.println("Attached observer: " + observer.getClass().getSimpleName());
    }

    public void detach(Observer observer) {
        // In Java 8+, removeIf makes this elegant.
        observers.removeIf(ref -> {
            Observer target = ref.get();
            return target == null || target == observer;
        });
        System.out.println("Detached observer.");
    }

    /**
     * Update State -> Triggers broadcast
     */
    public void changeState(String newState, NotificationThread mode) {
        System.out.println("\n[Subject] State changing to: " + newState);
        StateChangeEvent event = new StateChangeEvent(newState);

        if (mode == NotificationThread.ASYNC_BACKGROUND) {
            // Hand off to backgronund threads so Subject thread is NOT blocked.
            executor.submit(() -> broadcast(event));
        } else {
            // Synchronous direct call
            broadcast(event);
        }
    }

    private void broadcast(StateChangeEvent event) {
        // We track dead references to clean them up in bulk, keeping the list lean.
        List<WeakReference<Observer>> deadReferences = new ArrayList<>();

        for (WeakReference<Observer> ref : observers) {
            Observer observer = ref.get();

            if (observer == null) {
                // The observer was garbage collected. It's a lapsed listener!
                deadReferences.add(ref);
            } else {
                // Staff Insight: ISOLATE EXCEPTIONS
                // Never let a rogue Exception in Observer A prevent Observer B from getting data.
                try {
                    observer.onStateChanged(event);
                } catch (Exception e) {
                    System.err.println("[Subject] Caught execution failure in Observer: " 
                            + observer.getClass().getSimpleName() + " -> " + e.getMessage());
                    // Log to metrics/monitoring system (e.g., Datadog, CloudWatch) here.
                }
            }
        }

        // Cleanup stale weak references
        if (!deadReferences.isEmpty()) {
            observers.removeAll(deadReferences);
            System.out.println("[Subject] Cleaned up " + deadReferences.size() + " garbage collected observers.");
        }
    }

    // Graceful shutdown of infrastructure
    public void shutdown() {
        executor.shutdown();
    }
}

// ----------------------------------------------------
// 4. Concrete Observers
// ----------------------------------------------------

// A well-behaved UI Observer
class AnalyticsService implements Observer {
    @Override
    public void onStateChanged(StateChangeEvent event) {
        System.out.println("  [Analytics] Logging state change: " + event.getNewState() + " at " + event.getTimestamp());
    }
}

// A rogue Observer that acts as a "Poison Pill"
class RogueService implements Observer {
    @Override
    public void onStateChanged(StateChangeEvent event) {
        System.out.println("  [RogueService] Attempting to process...");
        throw new RuntimeException("Simulated NullPointerException inside Observer code!");
    }
}

// An Observer that demonstrates self-deregistration
class EphemeralService implements Observer {
    private EnterpriseSubject subject;
    private int triggerCount = 0;

    public EphemeralService(EnterpriseSubject subject) {
        this.subject = subject;
    }

    @Override
    public void onStateChanged(StateChangeEvent event) {
        triggerCount++;
        System.out.println("  [Ephemeral] Trigger " + triggerCount + " processed.");
        
        if (triggerCount == 1) {
            System.out.println("  [Ephemeral] My job is done. Deregistering myself CURRENTLY during the iteration loop!");
            // This would throw ConcurrentModificationException in a normal ArrayList
            // but is perfectly safe with CopyOnWriteArrayList!
            subject.detach(this);
        }
    }
}

// ----------------------------------------------------
// 5. Runner to demonstrate functionality
// ----------------------------------------------------
public class ObserverPatternDeepDive {
    public static void main(String[] args) {
        EnterpriseSubject subject = new EnterpriseSubject();

        AnalyticsService analytics = new AnalyticsService();
        RogueService rogue = new RogueService();
        EphemeralService ephemeral = new EphemeralService(subject);

        subject.attach(analytics);
        subject.attach(rogue);
        subject.attach(ephemeral);

        // Test 1: Synchronous dispatch. Watch the Try-Catch isolate the rogue exception, 
        // and watch the Ephemeral observer detach itself safely.
        subject.changeState("INITIALIZING", NotificationThread.MAIN_SYNC);

        System.out.println("\n--- Fast Forward ---\n");

        // Test 2: Only Analytics and Rogue process this (Ephemeral detached itself)
        subject.changeState("READY", NotificationThread.MAIN_SYNC);

        System.out.println("\n--- Fast Forward (Testing Memory Leaks) ---\n");

        // Test 3: Lapsed Listener Simulation.
        // We locally scope an observer. After the block, nothing points to it.
        createLocalObserver(subject);

        // Hint: Force GC to collect the local observer. 
        // (System.gc() isn't guaranteed, but works well for local testing)
        System.gc();

        // The Subject will realize the WeakReference is dead and clean it up!
        subject.changeState("SHUTTING_DOWN", NotificationThread.ASYNC_BACKGROUND);

        // Sleep to let async threads finish
        try { Thread.sleep(500); } catch (Exception ignored) {}
        
        subject.shutdown();
    }

    // Helper method to create an observer that immediately goes out of scope
    private static void createLocalObserver(EnterpriseSubject subject) {
        Observer temporary = new Observer() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                System.out.println("  [Temporary] This shouldn't print if GC ran.");
            }
        };
        subject.attach(temporary);
        // 'temporary' reference dies immediately after this bracket.
    }
}
