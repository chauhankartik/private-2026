/**
 * ============================================================
 *  ADVANCED STATE PATTERN — MEDIA PLAYER WITH GUARD CONDITIONS,
 *  EVENT QUEUE, AND SHARED STATELESS STATE OBJECTS
 * ============================================================
 *
 * Takes the State Pattern to production level:
 *   - Stateless state singletons shared across all player instances
 *   - Guard conditions (can only play if track is loaded)
 *   - Event-driven transition queue (decouple trigger from execution)
 *   - State observers (hook for UI updates)
 *   - Pluggable transition table (data-driven transitions)
 *
 * Context: AudioPlayer with states:
 *   IDLE → LOADING → READY → PLAYING → PAUSED → STOPPED
 *                                ↓ (error)
 *                             ERROR
 */
import java.util.*;
import java.util.function.Consumer;

// ============================================================
// 1. Events — all possible triggers
// ============================================================
enum PlayerEvent {
    LOAD, LOAD_COMPLETE, LOAD_FAILED,
    PLAY, PAUSE, RESUME, STOP, SEEK,
    TRACK_END, ERROR
}

// ============================================================
// 2. State Observer — notified on every transition
//    (hook for UI: update play button, progress bar, etc.)
// ============================================================
interface PlayerStateObserver {
    void onStateChanged(String previousState, String newState, PlayerEvent trigger);
}

// ============================================================
// 3. State Interface
// ============================================================
interface PlayerState {
    // Each method returns the NEXT state (or 'this' if no transition)
    PlayerState onLoad(AudioPlayer player);
    PlayerState onLoadComplete(AudioPlayer player);
    PlayerState onLoadFailed(AudioPlayer player);
    PlayerState onPlay(AudioPlayer player);
    PlayerState onPause(AudioPlayer player);
    PlayerState onResume(AudioPlayer player);
    PlayerState onStop(AudioPlayer player);
    PlayerState onTrackEnd(AudioPlayer player);
    PlayerState onError(AudioPlayer player, String message);

    String name();
    default boolean isTerminal() { return false; }
}

// ============================================================
// 4. Base State — provides safe defaults (reject invalid events)
// ============================================================
/**
 * Default implementation: all events are ignored with a log message.
 * Concrete states override ONLY the transitions they care about.
 * This avoids implementing 9 methods in every state (most are no-ops).
 */
abstract class AbstractPlayerState implements PlayerState {

    protected PlayerState rejectEvent(String event) {
        System.out.println("  [" + name() + "] Ignoring: " + event
            + " (not valid in this state)");
        return this;  // stay in current state
    }

    @Override public PlayerState onLoad(AudioPlayer p)           { return rejectEvent("LOAD"); }
    @Override public PlayerState onLoadComplete(AudioPlayer p)   { return rejectEvent("LOAD_COMPLETE"); }
    @Override public PlayerState onLoadFailed(AudioPlayer p)     { return rejectEvent("LOAD_FAILED"); }
    @Override public PlayerState onPlay(AudioPlayer p)           { return rejectEvent("PLAY"); }
    @Override public PlayerState onPause(AudioPlayer p)          { return rejectEvent("PAUSE"); }
    @Override public PlayerState onResume(AudioPlayer p)         { return rejectEvent("RESUME"); }
    @Override public PlayerState onStop(AudioPlayer p)           { return rejectEvent("STOP"); }
    @Override public PlayerState onTrackEnd(AudioPlayer p)       { return rejectEvent("TRACK_END"); }
    @Override public PlayerState onError(AudioPlayer p, String m){ return rejectEvent("ERROR"); }
}

// ============================================================
// 5. State Registry — shared stateless singletons
// ============================================================
class PlayerStates {
    // Stateless → can be shared across all AudioPlayer instances safely
    static final PlayerState IDLE     = new IdlePlayerState();
    static final PlayerState LOADING  = new LoadingPlayerState();
    static final PlayerState READY    = new ReadyPlayerState();
    static final PlayerState PLAYING  = new PlayingPlayerState();
    static final PlayerState PAUSED   = new PausedPlayerState();
    static final PlayerState STOPPED  = new StoppedPlayerState();
    static final PlayerState ERROR    = new ErrorPlayerState();
}

// ============================================================
// 6. Concrete States
// ============================================================

class IdlePlayerState extends AbstractPlayerState {
    @Override
    public PlayerState onLoad(AudioPlayer player) {
        System.out.println("  [IDLE]    Loading track: " + player.getTrackUrl());
        player.startLoadingAsync();
        return PlayerStates.LOADING;
    }
    @Override public String name() { return "IDLE"; }
}

class LoadingPlayerState extends AbstractPlayerState {
    @Override
    public PlayerState onLoadComplete(AudioPlayer player) {
        System.out.println("  [LOADING] Track buffered and ready.");
        return PlayerStates.READY;
    }
    @Override
    public PlayerState onLoadFailed(AudioPlayer player) {
        System.out.println("  [LOADING] Failed to load track.");
        return PlayerStates.ERROR;
    }
    @Override
    public PlayerState onStop(AudioPlayer player) {
        System.out.println("  [LOADING] Load cancelled.");
        player.cancelLoad();
        return PlayerStates.IDLE;
    }
    @Override public String name() { return "LOADING"; }
}

class ReadyPlayerState extends AbstractPlayerState {
    @Override
    public PlayerState onPlay(AudioPlayer player) {
        // Guard condition: only play if track is actually ready
        if (!player.isTrackReady()) {
            System.out.println("  [READY]   Guard failed: track not ready yet.");
            return this;
        }
        System.out.println("  [READY]   Playback started.");
        player.startPlayback();
        return PlayerStates.PLAYING;
    }
    @Override
    public PlayerState onLoad(AudioPlayer player) {
        System.out.println("  [READY]   Loading new track (replacing current).");
        player.startLoadingAsync();
        return PlayerStates.LOADING;
    }
    @Override
    public PlayerState onStop(AudioPlayer player) {
        return PlayerStates.IDLE;
    }
    @Override public String name() { return "READY"; }
}

class PlayingPlayerState extends AbstractPlayerState {
    @Override
    public PlayerState onPause(AudioPlayer player) {
        System.out.println("  [PLAYING] Pausing at position " + player.getPosition() + "s");
        player.pausePlayback();
        return PlayerStates.PAUSED;
    }
    @Override
    public PlayerState onStop(AudioPlayer player) {
        System.out.println("  [PLAYING] Stopping playback.");
        player.stopPlayback();
        return PlayerStates.STOPPED;
    }
    @Override
    public PlayerState onTrackEnd(AudioPlayer player) {
        System.out.println("  [PLAYING] Track finished. Checking autoplay...");
        if (player.hasNextTrack()) {
            player.loadNextTrack();
            return PlayerStates.LOADING;
        }
        return PlayerStates.STOPPED;
    }
    @Override
    public PlayerState onError(AudioPlayer player, String message) {
        System.out.println("  [PLAYING] Playback error: " + message);
        player.stopPlayback();
        return PlayerStates.ERROR;
    }
    @Override public String name() { return "PLAYING"; }
}

class PausedPlayerState extends AbstractPlayerState {
    @Override
    public PlayerState onResume(AudioPlayer player) {
        System.out.println("  [PAUSED]  Resuming from position " + player.getPosition() + "s");
        player.resumePlayback();
        return PlayerStates.PLAYING;
    }
    @Override
    public PlayerState onStop(AudioPlayer player) {
        System.out.println("  [PAUSED]  Stopping (was paused).");
        player.stopPlayback();
        return PlayerStates.STOPPED;
    }
    @Override
    public PlayerState onPlay(AudioPlayer player) {
        // Play while paused = resume (user-friendly alias)
        return onResume(player);
    }
    @Override public String name() { return "PAUSED"; }
}

class StoppedPlayerState extends AbstractPlayerState {
    @Override
    public PlayerState onPlay(AudioPlayer player) {
        System.out.println("  [STOPPED] Restarting from beginning.");
        player.seekTo(0);
        player.startPlayback();
        return PlayerStates.PLAYING;
    }
    @Override
    public PlayerState onLoad(AudioPlayer player) {
        System.out.println("  [STOPPED] Loading new track.");
        player.startLoadingAsync();
        return PlayerStates.LOADING;
    }
    @Override public String name() { return "STOPPED"; }
}

class ErrorPlayerState extends AbstractPlayerState {
    @Override
    public PlayerState onLoad(AudioPlayer player) {
        System.out.println("  [ERROR]   Retrying with new load...");
        player.startLoadingAsync();
        return PlayerStates.LOADING;
    }
    @Override
    public PlayerState onStop(AudioPlayer player) {
        System.out.println("  [ERROR]   Resetting to IDLE.");
        return PlayerStates.IDLE;
    }
    @Override public boolean isTerminal() { return false; }  // recoverable
    @Override public String name() { return "ERROR"; }
}

// ============================================================
// 7. The Context — AudioPlayer
// ============================================================
class AudioPlayer {

    private PlayerState currentState = PlayerStates.IDLE;
    private String  trackUrl;
    private int     position        = 0;
    private boolean trackReady      = false;
    private boolean hasNext         = false;

    private final List<PlayerStateObserver> observers = new ArrayList<>();

    public AudioPlayer() {
        System.out.println("[Player] Initialized in state: IDLE");
    }

    // --- Event dispatch — single entry point ---
    public void dispatch(PlayerEvent event) { dispatch(event, null); }
    public void dispatch(PlayerEvent event, String data) {
        System.out.println("\n[Player] Event: " + event
            + (data != null ? " (" + data + ")" : "")
            + "  in state: " + currentState.name());

        String prev = currentState.name();
        PlayerState next = switch (event) {
            case LOAD         -> currentState.onLoad(this);
            case LOAD_COMPLETE -> currentState.onLoadComplete(this);
            case LOAD_FAILED  -> currentState.onLoadFailed(this);
            case PLAY         -> currentState.onPlay(this);
            case PAUSE        -> currentState.onPause(this);
            case RESUME       -> currentState.onResume(this);
            case STOP         -> currentState.onStop(this);
            case TRACK_END    -> currentState.onTrackEnd(this);
            case ERROR        -> currentState.onError(this, data != null ? data : "unknown");
            default           -> currentState;
        };

        if (next != currentState) {
            currentState = next;
            System.out.println("[Player] State: " + prev + " → " + currentState.name());
            notifyObservers(prev, currentState.name(), event);
        }
    }

    // --- Observer management ---
    public void addObserver(PlayerStateObserver o) { observers.add(o); }
    private void notifyObservers(String from, String to, PlayerEvent event) {
        observers.forEach(o -> o.onStateChanged(from, to, event));
    }

    // --- Subsystem operations (called by state objects) ---
    public void startLoadingAsync() {
        System.out.println("  [Player]  [async] Fetching: " + trackUrl);
        // Simulate async: in real code dispatch(LOAD_COMPLETE) after buffer
        trackReady = false;
    }
    public void cancelLoad()        { System.out.println("  [Player]  Load cancelled."); }
    public void startPlayback()     { System.out.println("  [Player]  ▶ Playing."); }
    public void pausePlayback()     { System.out.println("  [Player]  ⏸ Paused."); }
    public void resumePlayback()    { System.out.println("  [Player]  ▶ Resumed."); }
    public void stopPlayback()      { position = 0; System.out.println("  [Player]  ⏹ Stopped."); }
    public void seekTo(int seconds) { position = seconds; System.out.println("  [Player]  Seeked to " + seconds + "s"); }
    public void loadNextTrack()     { System.out.println("  [Player]  Loading next track..."); }

    // --- Getters ---
    public String  getTrackUrl()   { return trackUrl; }
    public int     getPosition()   { return position; }
    public boolean isTrackReady()  { return trackReady; }
    public boolean hasNextTrack()  { return hasNext; }

    // --- Setters (for setup) ---
    public void setTrackUrl(String url) { this.trackUrl = url; }
    public void setTrackReady(boolean r) { this.trackReady = r; }
    public void setHasNext(boolean h)   { this.hasNext = h; }
    public void setPosition(int p)      { this.position = p; }
    public String getStateName()        { return currentState.name(); }
}

// ============================================================
// 8. Demo
// ============================================================
public class AdvancedStateDemo {
    public static void main(String[] args) {

        AudioPlayer player = new AudioPlayer();

        // Add a UI observer
        player.addObserver((from, to, event) ->
            System.out.println("  [UI]      Update: button state changed " + from + " → " + to)
        );

        // === Scenario 1: Normal playback flow ===
        System.out.println("\n====== SCENARIO 1: Normal Playback ======");
        player.setTrackUrl("https://music.example.com/track/123.mp3");
        player.dispatch(PlayerEvent.LOAD);
        // Simulate async load completing
        player.setTrackReady(true);
        player.dispatch(PlayerEvent.LOAD_COMPLETE);
        player.dispatch(PlayerEvent.PLAY);
        player.setPosition(45);
        player.dispatch(PlayerEvent.PAUSE);
        player.dispatch(PlayerEvent.RESUME);
        player.dispatch(PlayerEvent.STOP);

        // === Scenario 2: Load failure and recovery ===
        System.out.println("\n====== SCENARIO 2: Load Failure + Recovery ======");
        AudioPlayer player2 = new AudioPlayer();
        player2.setTrackUrl("https://music.example.com/track/bad.mp3");
        player2.dispatch(PlayerEvent.LOAD);
        player2.dispatch(PlayerEvent.LOAD_FAILED);
        player2.dispatch(PlayerEvent.PLAY);        // ignored in ERROR state
        player2.setTrackUrl("https://music.example.com/track/good.mp3");
        player2.dispatch(PlayerEvent.LOAD);        // retry
        player2.setTrackReady(true);
        player2.dispatch(PlayerEvent.LOAD_COMPLETE);
        player2.dispatch(PlayerEvent.PLAY);

        // === Scenario 3: Autoplay next track ===
        System.out.println("\n====== SCENARIO 3: Autoplay ======");
        AudioPlayer player3 = new AudioPlayer();
        player3.setTrackUrl("https://music.example.com/playlist/1.mp3");
        player3.setHasNext(true);
        player3.dispatch(PlayerEvent.LOAD);
        player3.setTrackReady(true);
        player3.dispatch(PlayerEvent.LOAD_COMPLETE);
        player3.dispatch(PlayerEvent.PLAY);
        player3.dispatch(PlayerEvent.TRACK_END);   // auto-loads next track
    }
}

/**
 * 🧠 KEY OBSERVATIONS
 *
 * 1. STATELESS STATE SINGLETONS (PlayerStates registry):
 *    All AudioPlayer instances share the SAME state objects.
 *    No memory overhead for state objects per player.
 *    Only valid when states hold NO instance-specific data.
 *
 * 2. BASE STATE WITH DEFAULTS (AbstractPlayerState):
 *    Concrete states override ONLY what they care about.
 *    This is the "Template Method meets State" combo.
 *    Avoids boilerplate without sacrificing correctness.
 *
 * 3. GUARD CONDITIONS:
 *    ReadyPlayerState.onPlay() checks isTrackReady() before transitioning.
 *    The guard is inside the state, keeping the Context clean.
 *
 * 4. OBSERVER HOOK:
 *    Any UI component can react to state changes without polling.
 *    In Android: update playback controls. In React: dispatch Redux action.
 *
 * 5. EVENT-DRIVEN DISPATCH:
 *    All events go through one dispatch(PlayerEvent) method.
 *    This is the bridge to event queues, WebSocket messages, or voice commands:
 *      "Hey Player, pause" → dispatch(PlayerEvent.PAUSE)
 *
 * 6. STAFF-LEVEL INTERVIEW INSIGHT:
 *    "For a production media player, I'd combine State with an Event Queue.
 *     External events (network, sensor) push events onto the queue.
 *     A single-threaded event loop dequeues and dispatches them.
 *     This eliminates race conditions without locks."
 */
