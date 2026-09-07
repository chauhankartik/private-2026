package movie_booking_system;

import movie_booking_system.CoreModels.Show;
import movie_booking_system.CoreModels.ShowSeat;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class SeatLockManager {
    private static volatile SeatLockManager instance;

    // ShowSeatId -> Lock for synchronizing lock creation
    private final ConcurrentHashMap<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();
    
    // Active Seat Locks: BookingId -> SeatLockDetails
    private final ConcurrentHashMap<String, SeatLockDetails> activeBookingLocks = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private SeatLockManager() {}

    public static SeatLockManager getInstance() {
        if (instance == null) {
            synchronized (SeatLockManager.class) {
                if (instance == null) {
                    instance = new SeatLockManager();
                }
            }
        }
        return instance;
    }

    public static class SeatLockDetails {
        private final String bookingId;
        private final Show show;
        private final List<ShowSeat> lockedSeats;
        private final long expirationTimestamp;

        public SeatLockDetails(String bookingId, Show show, List<ShowSeat> lockedSeats, long timeoutSeconds) {
            this.bookingId = bookingId;
            this.show = show;
            this.lockedSeats = lockedSeats;
            this.expirationTimestamp = System.currentTimeMillis() + (timeoutSeconds * 1000);
        }

        public String getBookingId() { return bookingId; }
        public Show getShow() { return show; }
        public List<ShowSeat> getLockedSeats() { return lockedSeats; }
        public boolean isExpired() { return System.currentTimeMillis() > expirationTimestamp; }
    }

    /**
     * Atomically locks the requested seats for a booking session with a TTL timeout.
     */
    public boolean lockSeats(String bookingId, Show show, List<String> seatIds, long timeoutSeconds) {
        List<ShowSeat> seatsToLock = new ArrayList<>();
        List<ReentrantLock> acquiredLocks = new ArrayList<>();

        // Validate seats existence
        for (String seatId : seatIds) {
            ShowSeat ss = show.getShowSeats().get(seatId);
            if (ss == null) {
                System.out.println("❌ Seat " + seatId + " does not exist in show " + show.getShowId());
                return false;
            }
            seatsToLock.add(ss);
        }

        // Sort seats to prevent deadlock when multiple threads acquire locks
        seatsToLock.sort(Comparator.comparing(ShowSeat::getShowSeatId));

        try {
            // Step 1: Acquire fine-grained ReentrantLocks for each seat
            for (ShowSeat ss : seatsToLock) {
                ReentrantLock lock = seatLocks.computeIfAbsent(ss.getShowSeatId(), k -> new ReentrantLock());
                if (!lock.tryLock(2, TimeUnit.SECONDS)) {
                    System.out.println("⚠️ Could not acquire lock for seat: " + ss.getSeat().getSeatId());
                    releaseAcquiredLocks(acquiredLocks);
                    return false;
                }
                acquiredLocks.add(lock);
            }

            // Step 2: Check availability and mark TEMPORARILY_BLOCKED
            for (ShowSeat ss : seatsToLock) {
                if (!ss.markBlocked()) {
                    System.out.println("⚠️ Seat " + ss.getSeat().getSeatId() + " is already blocked or booked!");
                    // Rollback previously marked seats
                    for (ShowSeat rollbackSs : seatsToLock) {
                        if (rollbackSs == ss) break;
                        rollbackSs.markAvailable();
                    }
                    releaseAcquiredLocks(acquiredLocks);
                    return false;
                }
            }

            // Step 3: Register active lock details & schedule TTL expiry task
            SeatLockDetails lockDetails = new SeatLockDetails(bookingId, show, seatsToLock, timeoutSeconds);
            activeBookingLocks.put(bookingId, lockDetails);

            scheduler.schedule(() -> handleLockTimeout(bookingId), timeoutSeconds, TimeUnit.SECONDS);

            System.out.printf("🔒 Successfully locked %d seats for Booking [%s] (Expires in %ds)%n",
                    seatIds.size(), bookingId, timeoutSeconds);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            releaseAcquiredLocks(acquiredLocks);
            return false;
        } finally {
            releaseAcquiredLocks(acquiredLocks);
        }
    }

    /**
     * Confirms the seat lock upon payment completion, transitioning seats to BOOKED.
     */
    public boolean confirmLock(String bookingId) {
        SeatLockDetails lockDetails = activeBookingLocks.remove(bookingId);
        if (lockDetails == null || lockDetails.isExpired()) {
            System.out.println("❌ Lock expired or invalid for booking: " + bookingId);
            return false;
        }

        for (ShowSeat ss : lockDetails.getLockedSeats()) {
            ss.markBooked();
        }
        System.out.println("✅ Confirmed seat bookings for Booking ID: " + bookingId);
        return true;
    }

    /**
     * Unlocks seats manually (e.g., user cancels before payment) or on TTL expiration.
     */
    public void unlockSeats(String bookingId) {
        SeatLockDetails lockDetails = activeBookingLocks.remove(bookingId);
        if (lockDetails != null) {
            for (ShowSeat ss : lockDetails.getLockedSeats()) {
                if (ss.getStatus() == Enums.SeatStatus.TEMPORARILY_BLOCKED) {
                    ss.markAvailable();
                }
            }
            System.out.println("🔓 Released seat locks for Booking ID: " + bookingId);
        }
    }

    private void handleLockTimeout(String bookingId) {
        SeatLockDetails lockDetails = activeBookingLocks.get(bookingId);
        if (lockDetails != null) {
            System.out.println("⏰ [TTL EXPIRED] Auto-releasing seats for Booking ID: " + bookingId);
            unlockSeats(bookingId);
        }
    }

    private void releaseAcquiredLocks(List<ReentrantLock> locks) {
        for (ReentrantLock lock : locks) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
