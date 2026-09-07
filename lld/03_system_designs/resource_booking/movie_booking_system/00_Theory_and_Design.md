# Movie Booking System (BookMyShow / Fandango) — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** Singleton, Strategy, Factory, Facade, Observer, State, Command  
> **SOLID Coverage:** All 5 principles applied  
> **Key Technical Challenge:** Thread-safe concurrent seat locking with auto-expiration (TTL)

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design a Movie Booking System like BookMyShow or Fandango. It should support:
> - Multiple cities, theatres/cinemas per city, screens per theatre, and seats per screen.
> - Multiple seat categories (Regular, Premium, Recline, VIP).
> - Movies with genres, languages, durations, and formats (2D, 3D, IMAX).
> - Show schedules linking a movie to a screen at a specific date & time.
> - Browsing & Searching movies by city, genre, language, and date.
> - Real-time seat selection & seat availability display.
> - Temporary seat locking (5-minute window) to prevent double-booking.
> - Dynamic pricing based on seat type, show format, and weekend/time surcharges.
> - Booking flow with payment integration (Card, UPI) and ticket generation.
> - Cancellation and refund management."

---

## 2. Clarifying Questions (Ask These First!)

| Question | Why It Matters |
|---|---|
| Is **seat allocation** user-selected or system-assigned? | Dictates API contract for seat selection (`List<String> seatIds`). |
| How do we handle **concurrency** when 100 users try to book the same seat at once? | Requires fine-grained locking (`ReentrantLock` per `ShowSeat` / `SeatLockManager`). |
| What happens if a user **locks seats but abandons payment**? | Requires a TTL (Time-To-Live) seat lock with scheduled auto-release worker. |
| Is **cancellation** supported with partial/full refund? | Influences `BookingStatus` state transitions and `PaymentProcessor` refund logic. |
| Can a show have **variable pricing** based on peak hours or weekend? | Requires pluggable `PricingStrategy`. |
| Is **notification** (SMS/Email) synchronous or asynchronous? | Influences `NotificationService` (Observer pattern). |

---

## 3. Core Entities & Abstractions Identification

```
City                 → Represents a geographical location (e.g. BANGALORE, MUMBAI, DELHI).
Theatre              → Belongs to a City; contains multiple Screens.
Screen               → Belongs to a Theatre; has a total seat capacity and layout.
Seat                 → Entity with seatId, row, col, SeatType (REGULAR, PREMIUM, RECLINER).
Movie                → Metadata: title, durationMinutes, language, genre, rating.
Show                 → Represents a specific Movie screening on a Screen at startTime & endTime.
ShowSeat             → Association entity linking Show & Seat with SeatStatus (AVAILABLE, BLOCKED, BOOKED).
SeatLockManager      → Thread-safe manager handling temporary locks with TTL timers.
PricingStrategy      → Strategy interface for calculating total price based on seat types & show conditions.
DiscountStrategy     → Coupon / promotional discount calculator interface.
Booking              → Aggregate root for a reservation (bookingId, show, seats, user, status, totalAmount).
Payment              → Payment transaction details (paymentId, bookingId, amount, mode, status).
PaymentProcessor     → Interface for UPI, CreditCard payment implementations.
Ticket               → Immutable digital pass generated upon successful booking confirmation.
MovieBookingService  → Facade / Orchestrator Singleton coordinating search, locking, booking, payment.
```

---

## 4. Concurrency & Seat Locking Strategy

```
                          User A & User B select Seat A1 at the same time
                                               │
                                               ▼
                              ┌──────────────────────────────────┐
                              │     MovieBookingService          │
                              └──────────────────────────────────┘
                                               │
                                               ▼
                              ┌──────────────────────────────────┐
                              │       SeatLockManager            │
                              │  - locks: Map<ShowSeatId, Lock>  │
                              └──────────────────────────────────┘
                                      │                  │
                   User A acquires lock              User B fails to acquire lock
                                      │                  │
                                      ▼                  ▼
                         Status → TEMPORARILY_BLOCKED    Return Error: "Seat unavailable"
                         Set TTL timer (e.g. 5 mins)
                                      │
                         ┌────────────┴────────────┐
                         │                         │
                 Payment Completed           Payment Timed Out / Abandoned
                         │                         │
                         ▼                         ▼
                   Status → BOOKED           Status → AVAILABLE
                   Issue Ticket              Release Lock via TTL Scheduler
```

### Locking Implementation Options:
1. **Optimistic Locking**: `version` field on `ShowSeat`. Fails fast if concurrent update detected.
2. **Pessimistic In-Memory Lock (Implemented here)**: `ReentrantLock` per `ShowSeat` managed by `SeatLockManager` + `ConcurrentHashMap` for active locks + `ScheduledExecutorService` for TTL cleanup.

---

## 5. Class Diagram (UML Architecture)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           MovieBookingService (Facade)                          │
│  - cityTheatresMap: Map<City, List<Theatre>>                                   │
│  - showsMap: Map<String, Show>                                                 │
│  - seatLockManager: SeatLockManager                                             │
│  - pricingStrategy: PricingStrategy                                            │
│  + searchMovies(city, genre, language): List<Movie>                            │
│  + getShowSeatLayout(showId): List<ShowSeat>                                   │
│  + createBooking(userId, showId, seatIds): Booking                             │
│  + confirmBooking(bookingId, paymentMode): Ticket                              │
│  + cancelBooking(bookingId): boolean                                           │
└─────────────────────────────────────────────────────────────────────────────────┘
         │                               │                                 │
         ▼ uses                          ▼ uses                            ▼ uses
┌───────────────────┐        ┌─────────────────────────┐       ┌────────────────────────┐
│  SeatLockManager  │        │     PricingStrategy     │       │    PaymentProcessor    │
│  - lockMap        │        │      <<interface>>      │       │     <<interface>>      │
│  - ttlScheduler   │        │ + calculatePrice(...)   │       │ + processPayment(...)  │
└───────────────────┘        └─────────────────────────┘       └────────────────────────┘
                                          ▲                                 ▲
                                ┌─────────┴─────────┐             ┌─────────┴─────────┐
                         DefaultPricing  WeekendPricing    UPIProcessor    CardProcessor
```

---

## 6. Design Patterns Applied

| Pattern | Component | Why It Was Chosen |
|---|---|---|
| **Facade** | `MovieBookingService` | Hides complex subsystem interactions (locking, pricing, payment, ticketing) behind a simple API. |
| **Strategy** | `PricingStrategy`, `DiscountStrategy`, `PaymentProcessor` | Allows pluggable price calculations and payment gateways without modifying core logic (OCP). |
| **Singleton** | `MovieBookingService`, `SeatLockManager` | Ensures a single centralized state for seat locks and catalog orchestration. |
| **State** | `BookingStatus`, `SeatStatus` | Clear state transitions (`AVAILABLE` → `TEMPORARILY_BLOCKED` → `BOOKED` or `AVAILABLE`). |
| **Observer** | `NotificationService` | Decouples ticket confirmation/cancellation notifications from the core transaction flow. |
| **Factory Method**| `PaymentProcessorFactory` | Instantiates appropriate `PaymentProcessor` based on `PaymentMode`. |

---

## 7. SOLID Principles Self-Audit

- **S (Single Responsibility)**: `SeatLockManager` handles locks only; `PricingStrategy` handles calculations only; `PaymentProcessor` handles transactions only.
- **O (Open/Closed)**: Add new payment methods or pricing rules by implementing interfaces without changing `MovieBookingService`.
- **L (Liskov Substitution)**: Any `PaymentProcessor` (UPI, Card) can be substituted seamlessly.
- **I (Interface Segregation)**: Focused, narrow interfaces (`PricingStrategy`, `PaymentProcessor`, `NotificationService`).
- **D (Dependency Inversion)**: `MovieBookingService` depends on abstractions (`PricingStrategy`, `PaymentProcessor`), injected via setters/constructors.
