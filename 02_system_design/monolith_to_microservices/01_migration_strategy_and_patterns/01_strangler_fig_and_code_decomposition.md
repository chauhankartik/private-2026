# 01. Strangler Fig & Code Decomposition Patterns

This chapter details code-level refactoring patterns for decomposing monolithic applications: the Strangler Fig pattern, Branch by Abstraction, Parallel Run verification, and Micro Frontends.

---

## 🌿 Strangler Fig Pattern Mechanics

Named after the strangler fig tree that grows around an existing host tree, this pattern incrementally replaces monolithic features with microservices at the HTTP/API Gateway boundary.

```mermaid
sequenceDiagram
    autonumber
    actor Client as User / Browser
    participant GW as API Gateway / Strangler Proxy
    participant Monolith as Legacy Monolith
    participant MicroSvc as New Payment Microservice

    Note over GW: Strangler Proxy checks path & feature flag
    
    Client->>GW: POST /api/v1/payments (Canary 10% Traffic Split)
    
    alt Route to Legacy Monolith (90% Traffic)
        GW->>Monolith: Forward Request to Monolith
        Monolith-->>GW: 200 OK Response
    else Route to Extracted Microservice (10% Traffic)
        GW->>MicroSvc: Forward Request to Payment Microservice
        MicroSvc-->>GW: 200 OK Response
    end
    
    GW-->>Client: 200 OK Response
```

---

## 🌿 Branch by Abstraction Pattern

When decomposing code **inside** the monolith before moving it out into a separate process, use **Branch by Abstraction**:

```mermaid
flowchart TD
    subgraph Stage1 ["Stage 1: Identify Monolith Feature"]
        Client1["Caller Code"] --> LegacyImpl1["Legacy Monolith Logic"]
    end

    subgraph Stage2 ["Stage 2: Introduce Abstract Interface"]
        Client2["Caller Code"] --> Abstraction["SupplierInterface"]
        Abstraction --> LegacyImpl2["Legacy Monolith Logic"]
    end

    subgraph Stage3 ["Stage 3: Build New Implementation & Feature Flag"]
        Client3["Caller Code"] --> Abstraction2["SupplierInterface"]
        Abstraction2 -->|Toggle: FALSE| LegacyImpl3["Legacy Monolith Logic"]
        Abstraction2 -->|Toggle: TRUE| NewMicroSvcImpl["New Microservice Client Impl"]
    end
```

---

## ☕ Production Java Implementation: Branch by Abstraction with Dual-Run Verification

```java
package com.example.migration.pattern;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Branch by Abstraction implementation featuring Feature Toggles and Parallel Run diff verification.
 */
public class PaymentProcessingSupplier {

    public interface PaymentService {
        PaymentResponse processPayment(PaymentRequest request);
    }

    public record PaymentRequest(String accountId, long amountCents, String currency) {}
    public record PaymentResponse(boolean success, String transactionId, String errorMessage) {}

    private final PaymentService legacyMonolithService;
    private final PaymentService newMicroserviceClient;
    private final FeatureToggleService featureToggleService;

    public PaymentProcessingSupplier(
            PaymentService legacyMonolithService,
            PaymentService newMicroserviceClient,
            FeatureToggleService featureToggleService) {
        this.legacyMonolithService = Objects.requireNonNull(legacyMonolithService);
        this.newMicroserviceClient = Objects.requireNonNull(newMicroserviceClient);
        this.featureToggleService = Objects.requireNonNull(featureToggleService);
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        boolean useNewService = featureToggleService.isFeatureEnabled("use-extracted-payment-microservice");
        boolean runParallelVerification = featureToggleService.isFeatureEnabled("enable-parallel-run-verification");

        if (runParallelVerification) {
            // Parallel Run: Execute legacy synchronously, execute new service asynchronously & compare diff
            PaymentResponse legacyResp = legacyMonolithService.processPayment(request);
            
            CompletableFuture.runAsync(() -> {
                try {
                    PaymentResponse newResp = newMicroserviceClient.processPayment(request);
                    compareResponses(request, legacyResp, newResp);
                } catch (Exception e) {
                    System.err.println("[PARALLEL RUN ERROR] New Microservice threw exception: " + e.getMessage());
                }
            });

            return legacyResp; // Always return safe legacy response during Parallel Run phase
        }

        if (useNewService) {
            return newMicroserviceClient.processPayment(request);
        } else {
            return legacyMonolithService.processPayment(request);
        }
    }

    private void compareResponses(PaymentRequest req, PaymentResponse legacy, PaymentResponse newSvc) {
        if (legacy.success() != newSvc.success()) {
            System.err.printf("[PARALLEL RUN MISMATCH] Account %s: Legacy success=%b, New success=%b%n",
                    req.accountId(), legacy.success(), newSvc.success());
        } else {
            System.out.println("[PARALLEL RUN MATCH] Account " + req.accountId() + " verified successfully!");
        }
    }

    public interface FeatureToggleService {
        boolean isFeatureEnabled(String featureName);
    }
}
```

---

## 🎨 UI Micro Frontends Decomposition

Just as backends are split into microservices, complex monolithic single-page applications (SPAs) can be decomposed into **Micro Frontends**.

```mermaid
flowchart TD
    subgraph MicroFrontendShell ["Micro Frontend Container Shell (Module Federation)"]
        HeaderMFE["Header & Nav Micro Frontend (Team Core)"]
        OrderMFE["Order History Micro Frontend (Team Orders)"]
        PaymentMFE["Payment Checkout Micro Frontend (Team Checkout)"]
    end

    HeaderMFE --> ShellApp["App Shell Router"]
    OrderMFE --> ShellApp
    PaymentMFE --> ShellApp
```

### Composition Strategies
1. **Page-Based Composition**: The API Gateway routes entire page requests (`/orders` vs `/checkout`) to independent micro frontend web bundles.
2. **Component-Based Composition (Webpack Module Federation / Web Components)**: Multiple team-owned components co-exist on the same page, dynamically loaded at runtime.
