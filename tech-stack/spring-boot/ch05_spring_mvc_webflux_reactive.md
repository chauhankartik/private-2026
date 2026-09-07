# Chapter 5: Web Layer: Spring MVC vs Spring WebFlux

Spring provides two distinct web stacks: the classic imperative, blocking **Spring MVC** (Servlet API) and the modern reactive, non-blocking **Spring WebFlux** (Reactive Streams).

---

## 1. Spring MVC Architecture (Imperative & Servlet-Based)

Spring MVC is built on top of the Java Servlet Specification and follows a **Thread-per-Request** execution model.

```
Client HTTP Request
       |
       v
[ Embedded Tomcat Worker Thread ] (From Thread Pool, e.g. 200 threads)
       |
       v
[ DispatcherServlet ] (Front Controller)
       |
       ├── 1. HandlerMapping  --> Finds matching @RestController method
       ├── 2. HandlerAdapter  --> Invokes method via reflection
       ├── 3. Controller Exec --> Executes business logic & DB calls (BLOCKS THREAD!)
       └── 4. MessageConverter-> Serializes Java Object to JSON via Jackson
       |
       v
HTTP Response Sent (Worker Thread returned to Pool)
```

### 1.1 Concurrency Bottleneck
Under high concurrent load, if database or downstream REST API calls take 2 seconds, 200 Tomcat threads quickly exhaust (`http-nio-8080-exec` maxed out). Subsequent incoming requests stall in the accept queue, causing high latency or `HTTP 503 Service Unavailable`.

---

## 2. Spring WebFlux Architecture (Reactive & Event-Loop)

Spring WebFlux is built on **Project Reactor** and operates on a non-blocking **Event Loop** engine powered by Netty.

```
Client HTTP Requests (10,000 Concurrent Connections)
       |
       v
[ Netty EventLoop Group ] (Fixed Thread Pool = 2 * CPU Cores)
       |
       ├── Non-Blocking I/O Selector intercepts HTTP events
       ├── Dispatches requests to WebHandler
       └── Executes Reactive Pipeline (Mono / Flux)
       |
       v DB / Remote Call (Non-blocking R2DBC / WebClient)
[ Netty Thread Instantly Released to Process Other Requests! ]
```

### 2.1 Project Reactor Types: `Mono` and `Flux`
* **`Mono<T>`:** A Reactive Streams `Publisher` emitting **0 or 1** item.
* **`Flux<T>`:** A Reactive Streams `Publisher` emitting **0 to $N$** asynchronous stream items.

```java
@RestController
@RequestMapping("/api/reactive")
public class ReactiveOrderController {

    @GetMapping("/orders/{id}")
    public Mono<Order> getOrder(@PathVariable String id) {
        return orderRepository.findById(id); // Non-blocking R2DBC query
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderEvent> streamEvents() {
        return eventService.getLiveStream(); // Server-Sent Events (SSE)
    }
}
```

---

## 3. Structural & Architectural Comparison

| Dimension | Spring MVC (Servlet Stack) | Spring WebFlux (Reactive Stack) |
| :--- | :--- | :--- |
| **Underlying Server** | Servlet Container (Tomcat, Jetty, Undertow) | Netty (Default), Undertow, Servlet 3.1+ |
| **Concurrency Model** | **Thread-per-request** (Blocking I/O) | **Event Loop** (Non-blocking I/O) |
| **Thread Count** | High (e.g., 200-500 threads) | Very Low (Fixed `2 * CPU Cores`) |
| **Data Access** | Blocking JDBC / JPA / Hibernate | Non-blocking R2DBC / Reactive Mongo / Redis |
| **HTTP Client** | `RestTemplate` / `RestClient` | `WebClient` |
| **Ideal Workload** | Standard CRUD apps, synchronous DB calls | High-concurrency streaming, API Gateways, WebSockets |

---

## 4. Staff Engineer Web Architecture Rules
1. **Do Not Mix Blocking Calls in WebFlux:** Invoking blocking JDBC/JPA calls or `Thread.sleep()` inside a WebFlux reactive pipeline blocks the entire Netty EventLoop thread, freezing all concurrent requests handled by that CPU core!
2. **Choose Spring MVC for Standard Business Applications:** Unless your system requires massive concurrency ($> 10,000$ active connections) or streaming, Spring MVC remains easier to debug, test, and profile.
3. **Use `WebClient` Over `RestTemplate`:** `RestTemplate` is in maintenance mode. Use `WebClient` (or Spring 6 `RestClient`) for modern HTTP communication in both MVC and WebFlux applications.
