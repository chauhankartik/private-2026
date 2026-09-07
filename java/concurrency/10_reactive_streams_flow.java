package java.concurrency;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * Java 9 Flow API (Reactive Streams Standard: JEP 266).
 * Demonstrates Publisher, Subscriber, Subscription, and Backpressure flow control.
 */
public class ReactiveStreamsFlowDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Java 9 Flow API & Reactive Backpressure Demo ===");

        // 1. Create Publisher (SubmissionPublisher handles asynchronous buffer dispatch)
        try (SubmissionPublisher<String> publisher = new SubmissionPublisher<>()) {

            // 2. Create Subscriber with Backpressure Request Management
            Flow.Subscriber<String> subscriber = new Flow.Subscriber<>() {
                private Flow.Subscription subscription;
                private int processedCount = 0;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    System.out.println("[Subscriber] Subscribed successfully. Requesting initial 2 items...");
                    // BACKPRESSURE DEMO: Request only 2 items initially
                    subscription.request(2);
                }

                @Override
                public void onNext(String item) {
                    System.out.println("[Subscriber] Received Item: " + item);
                    processedCount++;

                    // Request 1 more item whenever 2 items are processed
                    if (processedCount % 2 == 0) {
                        System.out.println("[Subscriber] Backpressure Signal: Requesting 2 more items...");
                        subscription.request(2);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("[Subscriber] Error encountered: " + throwable.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("[Subscriber] Stream completed successfully.");
                }
            };

            // 3. Register Subscriber with Publisher
            publisher.subscribe(subscriber);

            // 4. Publish Stream Items
            String[] items = {"Payload 1", "Payload 2", "Payload 3", "Payload 4", "Payload 5"};
            for (String item : items) {
                System.out.println("[Publisher] Submitting: " + item);
                publisher.submit(item);
                Thread.sleep(50);
            }

            // Allow asynchronous subscriber buffer to drain
            Thread.sleep(500);
        }
    }
}
