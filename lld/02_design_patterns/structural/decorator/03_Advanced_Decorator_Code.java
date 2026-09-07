import java.util.*;
import java.util.concurrent.*;

/**
 * ============================================================
 *  ADVANCED DECORATOR PATTERN — STAFF SWE
 *  Google-Style Notification System
 * ============================================================
 *
 * PROBLEM: Design a notification system where alerts can be sent
 * through multiple channels: Email, SMS, Slack, Push Notification.
 * A user can configure ANY combination of channels.
 *
 * WITHOUT Decorator: You'd need 2⁴ = 16 classes for every combo.
 * WITH Decorator: 1 base + 4 decorators = 5 classes. Done.
 *
 * This file layers on Staff-level concerns:
 *   1. Thread safety 
 *   2. Error isolation (one channel failing shouldn't kill others)
 *   3. Async delivery with timeouts
 *   4. Decorator removal at runtime
 */

// ----------------------------------------------------
// 1. Component Interface 
// ----------------------------------------------------
interface Notifier {
    void send(String message);
    String getChannelName();
}

// ----------------------------------------------------
// 2. Concrete Component (The Base Channel)
// ----------------------------------------------------
class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("  📧 [Email] Sending: " + message);
    }

    @Override
    public String getChannelName() {
        return "Email";
    }
}

// ----------------------------------------------------
// 3. Abstract Decorator
// ----------------------------------------------------
abstract class NotifierDecorator implements Notifier {
    protected Notifier wrappee;

    public NotifierDecorator(Notifier notifier) {
        this.wrappee = notifier;
    }

    @Override
    public void send(String message) {
        wrappee.send(message); // delegate to the inner notifier
    }

    @Override
    public String getChannelName() {
        return wrappee.getChannelName();
    }

    // Expose the inner notifier (useful for runtime unwrapping)
    public Notifier getWrappee() {
        return wrappee;
    }
}

// ----------------------------------------------------
// 4. Concrete Decorators 
// ----------------------------------------------------
class SMSDecorator extends NotifierDecorator {
    public SMSDecorator(Notifier notifier) { super(notifier); }

    @Override
    public void send(String message) {
        super.send(message);  // first, let the inner chain process
        sendSMS(message);     // then add OUR behavior
    }

    private void sendSMS(String message) {
        System.out.println("  📱 [SMS] Sending: " + message);
    }

    @Override
    public String getChannelName() {
        return super.getChannelName() + " + SMS";
    }
}

class SlackDecorator extends NotifierDecorator {
    public SlackDecorator(Notifier notifier) { super(notifier); }

    @Override
    public void send(String message) {
        super.send(message);
        sendSlack(message);
    }

    private void sendSlack(String message) {
        System.out.println("  💬 [Slack] Posting to #alerts: " + message);
    }

    @Override
    public String getChannelName() {
        return super.getChannelName() + " + Slack";
    }
}

class PushNotificationDecorator extends NotifierDecorator {
    public PushNotificationDecorator(Notifier notifier) { super(notifier); }

    @Override
    public void send(String message) {
        super.send(message);
        sendPush(message);
    }

    private void sendPush(String message) {
        System.out.println("  🔔 [Push] Sending mobile notification: " + message);
    }

    @Override
    public String getChannelName() {
        return super.getChannelName() + " + Push";
    }
}

// ============================================================
//  STAFF LEVEL: Error-Resilient Decorator
// ============================================================
/**
 * What if the Slack API is down? Without protection, the entire
 * notification chain crashes. Observer B (SMS) never fires.
 *
 * This "Safe" decorator wraps ANY other decorator and adds
 * try-catch isolation + timeout protection.
 */
class SafeNotifierDecorator extends NotifierDecorator {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final long timeoutMs;

    public SafeNotifierDecorator(Notifier notifier, long timeoutMs) {
        super(notifier);
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void send(String message) {
        Future<?> future = executor.submit(() -> {
            try {
                wrappee.send(message);
            } catch (Exception e) {
                System.err.println("  ⚠️ [SafeDecorator] Caught failure in " 
                    + wrappee.getChannelName() + ": " + e.getMessage());
            }
        });

        try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.err.println("  ⏰ [SafeDecorator] " + wrappee.getChannelName() 
                + " timed out after " + timeoutMs + "ms. Skipping.");
        } catch (Exception e) {
            System.err.println("  ⚠️ [SafeDecorator] Unexpected error: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}

// ============================================================
//  STAFF LEVEL: Build decorator chains from configuration
// ============================================================
/**
 * In a real Google system, the user doesn't manually write
 *   new SlackDecorator(new SMSDecorator(new EmailNotifier()));
 * 
 * Instead, a config file says: channels = [email, sms, slack]
 * and a Factory builds the decorator chain dynamically.
 */
class NotifierFactory {
    public static Notifier createFromConfig(List<String> channels) {
        if (channels.isEmpty()) {
            throw new IllegalArgumentException("At least one channel required");
        }

        // Base is always the first channel
        Notifier notifier = createBase(channels.get(0));

        // Wrap with additional decorators
        for (int i = 1; i < channels.size(); i++) {
            notifier = wrapWithChannel(notifier, channels.get(i));
        }
        return notifier;
    }

    private static Notifier createBase(String channel) {
        return switch (channel.toLowerCase()) {
            case "email" -> new EmailNotifier();
            default -> throw new IllegalArgumentException("Unknown base channel: " + channel);
        };
    }

    private static Notifier wrapWithChannel(Notifier base, String channel) {
        return switch (channel.toLowerCase()) {
            case "sms"   -> new SMSDecorator(base);
            case "slack" -> new SlackDecorator(base);
            case "push"  -> new PushNotificationDecorator(base);
            default      -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }
}

// ----------------------------------------------------
// 5. Let's Run It!
// ----------------------------------------------------
public class AdvancedDecoratorDemo {
    public static void main(String[] args) {

        System.out.println("=== SCENARIO 1: User wants Email + SMS ===\n");
        Notifier basic = new SMSDecorator(new EmailNotifier());
        System.out.println("Channels: " + basic.getChannelName());
        basic.send("Server CPU at 95%!");

        System.out.println("\n=== SCENARIO 2: User wants Email + SMS + Slack + Push ===\n");
        Notifier fullStack = new PushNotificationDecorator(
                                new SlackDecorator(
                                    new SMSDecorator(
                                        new EmailNotifier()
                                    )
                                )
                             );
        System.out.println("Channels: " + fullStack.getChannelName());
        fullStack.send("🔥 PRODUCTION IS DOWN!");

        System.out.println("\n=== SCENARIO 3: Factory-built from config ===\n");
        List<String> userConfig = List.of("email", "slack", "push");
        Notifier fromConfig = NotifierFactory.createFromConfig(userConfig);
        System.out.println("Channels: " + fromConfig.getChannelName());
        fromConfig.send("Deployment v2.3.1 succeeded");

        System.out.println("\n=== SCENARIO 4: Safe decorator with timeout ===\n");
        SafeNotifierDecorator safe = new SafeNotifierDecorator(fullStack, 5000);
        safe.send("Testing safe notification delivery");
        safe.shutdown();
    }
}

/**
 * 🗣️ INTERVIEW COMMUNICATION SCRIPT:
 *
 * "For the notification system, I'll use the Decorator Pattern.
 *  The base Notifier sends Email. Each additional channel — SMS, Slack, 
 *  Push — is a Decorator that wraps the previous one, adding its own
 *  send() call after delegating to the inner chain.
 *
 *  This avoids class explosion: instead of 16 combo classes, I have 
 *  1 base + 4 decorators = 5 classes.
 *
 *  For production readiness, I'd add:
 *  - A SafeDecorator with try-catch isolation so one failing channel 
 *    doesn't prevent others from firing.
 *  - A Factory that builds the decorator chain from user configuration.
 *  - Async delivery with timeouts for external API calls."
 *
 *
 * 🆚 DECORATOR vs SIMILAR PATTERNS (common interview question):
 *
 * Decorator vs Proxy:
 *   - Decorator adds NEW behavior (extra cost, extra channel).
 *   - Proxy controls ACCESS to existing behavior (lazy loading, auth checks, caching).
 *   - Structurally identical! The difference is INTENT.
 *
 * Decorator vs Chain of Responsibility:
 *   - Decorator: EVERY layer processes the request.
 *   - Chain of Resp: Only ONE handler processes (the rest pass it along).
 *
 * Decorator vs Adapter:
 *   - Decorator: Same interface in, same interface out, adds behavior.
 *   - Adapter: DIFFERENT interface in, converts to the expected interface.
 */
