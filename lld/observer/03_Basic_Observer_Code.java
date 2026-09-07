import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  BASIC OBSERVER PATTERN — THE YOUTUBE ANALOGY
 * ============================================================
 * 
 * This is the purest, simplest form of the Observer Pattern.
 * Before worrying about memory leaks or threading, understand
 * this foundation!
 */

// ----------------------------------------------------
// 1. The Observer Interface (The Subscriber)
// ----------------------------------------------------
/**
 * Anyone who wants to be notified MUST implement this interface.
 * This is the contract. The Subject doesn't care who you are,
 * as long as you have an "update" method it can call.
 */
interface Subscriber {
    // The Subject will call this method when something happens.
    void update(String videoTitle);
}

// ----------------------------------------------------
// 2. The Subject Interface (The Channel)
// ----------------------------------------------------
interface YouTubeChannel {
    void subscribe(Subscriber s);
    void unsubscribe(Subscriber s);
    void notifySubscribers();
}

// ----------------------------------------------------
// 3. The Concrete Subject (The actual YouTube Channel)
// ----------------------------------------------------
class TechChannel implements YouTubeChannel {
    
    // The channel keeps a list of everyone who clicked "Subscribe"
    private List<Subscriber> subscribers = new ArrayList<>();
    
    // The state we are tracking
    private String latestVideoTitle;

    @Override
    public void subscribe(Subscriber s) {
        subscribers.add(s);
    }

    @Override
    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }

    @Override
    public void notifySubscribers() {
        // THE CORE OF THE PATTERN: 
        // Iterate over the list and call update() on every single observer.
        for (Subscriber s : subscribers) {
            s.update(latestVideoTitle); // "PUSH" model: we push the data to them
        }
    }

    // A normal business method that triggers the notification
    public void uploadVideo(String title) {
        this.latestVideoTitle = title;
        System.out.println("\n[TechChannel] Uploaded new video: " + title);
        
        // Anytime state changes, we must inform the observers!
        notifySubscribers();
    }
}

// ----------------------------------------------------
// 4. The Concrete Observers (The actual Users)
// ----------------------------------------------------
class User implements Subscriber {
    private String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public void update(String videoTitle) {
        // The user reacts to the notification
        System.out.println("   Hey " + name + ", watch our new video: " + videoTitle);
    }
}

// Another type of observer just to show flexibility!
class PushNotificationServer implements Subscriber {
    @Override
    public void update(String videoTitle) {
        System.out.println("   [Server] Sending Apple Push Notification to all mobile devices for: " + videoTitle);
    }
}


// ----------------------------------------------------
// 5. Let's run it!
// ----------------------------------------------------
public class BasicObserverDemo {
    public static void main(String[] args) {
        
        // 1. Create the Subject
        TechChannel mkbhd = new TechChannel();

        // 2. Create the Observers
        User alice = new User("Alice");
        User bob = new User("Bob");
        PushNotificationServer server = new PushNotificationServer();

        // 3. Observers subscribe to the Subject
        mkbhd.subscribe(alice);
        mkbhd.subscribe(bob);
        mkbhd.subscribe(server);

        // 4. The Subject changes state! (Uploads a video)
        // Notice how Alice, Bob, and the Server all react automatically!
        mkbhd.uploadVideo("Reviewing the new iPhone!");

        // 5. Bob gets bored and unsubscribes
        mkbhd.unsubscribe(bob);

        // 6. Another video is uploaded. Bob will NOT be notified this time.
        mkbhd.uploadVideo("Top 5 Android Phones of 2026");
    }
}

/**
 * 💡 INTERVIEW TAKEAWAY:
 * If an interviewer asks you to code an Observer pattern on a whiteboard, 
 * THIS is what you write. 
 * - An ArrayList to hold the observers.
 * - A subscribe/unsubscribe method.
 * - A massive FOR LOOP to notify them.
 * 
 * Once you write this, the interviewer will say: "Great. Now, what happens if..."
 * THAT is when you bring out the advanced concepts from `01_ObserverPatternDeepDive.java`!
 */
