package movie_booking_system;

import movie_booking_system.BookingAndTicket.*;
import movie_booking_system.CoreModels.*;
import movie_booking_system.Enums.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class Demo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 🎬 MOVIE BOOKING SYSTEM — LLD INTERVIEW DEMO (BookMyShow / Fandango)");
        System.out.println("======================================================================\n");

        MovieBookingService service = MovieBookingService.getInstance();

        // -------------------------------------------------------------------------
        // SCENARIO 1: SYSTEM SETUP (Cities, Movies, Theatres, Screens, Seats, Shows)
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 1: Setting up System Data (Movies, Theatres & Shows)...");

        // Movies
        Movie movie1 = new Movie("M1", "Inception", Genre.SCI_FI, MovieRating.PG13, "English", 148);
        Movie movie2 = new Movie("M2", "RRR", Genre.ACTION, MovieRating.UA, "Telugu", 182);
        Movie movie3 = new Movie("M3", "The Dark Knight", Genre.ACTION, MovieRating.PG13, "English", 152);

        service.addMovie(movie1);
        service.addMovie(movie2);
        service.addMovie(movie3);

        // Screens & Seats Layout (Regular, Premium, Recliner, VIP)
        List<Seat> screen1Seats = new ArrayList<>();
        screen1Seats.add(new Seat("A1", 1, 1, SeatType.REGULAR));
        screen1Seats.add(new Seat("A2", 1, 2, SeatType.REGULAR));
        screen1Seats.add(new Seat("B1", 2, 1, SeatType.PREMIUM));
        screen1Seats.add(new Seat("B2", 2, 2, SeatType.PREMIUM));
        screen1Seats.add(new Seat("REC_1", 3, 1, SeatType.RECLINER));
        screen1Seats.add(new Seat("VIP_1", 4, 1, SeatType.VIP));

        Screen screen1 = new Screen("SCR1", "Screen 1 (IMAX)", screen1Seats);
        Screen screen2 = new Screen("SCR2", "Screen 2 (Dolby Atmos)", screen1Seats);

        // Theatre in Bangalore
        Theatre pvrBangalore = new Theatre("TH1", "PVR Forum Mall", City.BANGALORE, Arrays.asList(screen1, screen2));
        service.addTheatre(pvrBangalore);

        // Shows
        LocalDateTime eveningSlot = LocalDateTime.now().plusHours(3);
        Show show1 = new Show("SHOW1", movie1, screen1, pvrBangalore, eveningSlot, ShowFormat.IMAX_3D, 1.0);
        Show show2 = new Show("SHOW2", movie2, screen2, pvrBangalore, eveningSlot.plusHours(1), ShowFormat.FORMAT_2D, 1.0);

        service.addShow(show1);
        service.addShow(show2);

        System.out.println("✅ System populated with Movies, Theatres, and Shows.\n");

        // -------------------------------------------------------------------------
        // SCENARIO 2: BROWSING & SEARCHING MOVIES
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 2: Searching Movies in BANGALORE for Genre ACTION...");
        List<Movie> actionMovies = service.searchMovies(City.BANGALORE, Genre.ACTION, null);
        for (Movie m : actionMovies) {
            System.out.println("  🎥 Found Movie: " + m);
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // SCENARIO 3: GETTING SEAT LAYOUT & AVAILABILITY
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 3: Retrieving Seat Layout for SHOW1 (Inception - IMAX 3D)...");
        Map<String, ShowSeat> layout = service.getShowSeatLayout("SHOW1");
        for (ShowSeat ss : layout.values()) {
            System.out.println("  " + ss);
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // SCENARIO 4: END-TO-END SUCCESSFUL BOOKING WORKFLOW
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 4: Alice books seats A1 and A2 with Coupon 'MOVIE50'...");
        Booking aliceBooking = service.createBooking("Alice", "SHOW1", Arrays.asList("A1", "A2"), "MOVIE50");

        if (aliceBooking != null) {
            System.out.println("💳 Alice proceeding to pay via UPI...");
            Ticket aliceTicket = service.confirmBooking(aliceBooking.getBookingId(), PaymentMode.UPI);
            if (aliceTicket != null) {
                aliceTicket.printTicket();
            }
        }

        // -------------------------------------------------------------------------
        // SCENARIO 5: CONCURRENT BOOKING CONFLICT RESOLUTION
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 5: Testing Concurrency — Bob & Charlie competing for seat 'REC_1' simultaneously...");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Callable<Booking> bobTask = () -> {
            latch.await();
            System.out.println("🏃 [Thread Bob] Attempting to book seat REC_1...");
            return service.createBooking("Bob", "SHOW1", Collections.singletonList("REC_1"), null);
        };

        Callable<Booking> charlieTask = () -> {
            latch.await();
            System.out.println("🏃 [Thread Charlie] Attempting to book seat REC_1...");
            return service.createBooking("Charlie", "SHOW1", Collections.singletonList("REC_1"), null);
        };

        Future<Booking> bobFuture = executor.submit(bobTask);
        Future<Booking> charlieFuture = executor.submit(charlieTask);

        // Trigger both threads at the exact same instant
        latch.countDown();

        try {
            Booking bobBooking = bobFuture.get();
            Booking charlieBooking = charlieFuture.get();

            if (bobBooking != null && charlieBooking == null) {
                System.out.println("✅ Concurrency Control Succeeded! Bob got the lock, Charlie was safely rejected.");
                service.confirmBooking(bobBooking.getBookingId(), PaymentMode.CREDIT_CARD);
            } else if (charlieBooking != null && bobBooking == null) {
                System.out.println("✅ Concurrency Control Succeeded! Charlie got the lock, Bob was safely rejected.");
                service.confirmBooking(charlieBooking.getBookingId(), PaymentMode.CREDIT_CARD);
            } else {
                System.out.println("❌ Concurrency Failure! Both acquired locks (Double Booking!).");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        System.out.println();

        // -------------------------------------------------------------------------
        // SCENARIO 6: LOCK EXPIRATION / TTL AUTO-RELEASE
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 6: Testing TTL Auto-Release (Dave locks seat VIP_1 but abandons payment)...");
        Booking daveBooking = service.createBooking("Dave", "SHOW1", Collections.singletonList("VIP_1"), null);
        
        System.out.println("⏳ Waiting 12 seconds for TTL timer (10s lock timeout) to expire...");
        Thread.sleep(12000);

        System.out.println("🔎 Checking status of VIP_1 after TTL expiration:");
        ShowSeat vipSeat = service.getShowSeatLayout("SHOW1").get("VIP_1");
        System.out.println("  VIP_1 Status: " + vipSeat.getStatus());

        if (vipSeat.getStatus() == SeatStatus.AVAILABLE) {
            System.out.println("✅ TTL Lock Expiration Verified! Seat VIP_1 is back to AVAILABLE.\n");
        } else {
            System.out.println("❌ Lock Expiration Failed! Seat is still locked.\n");
        }

        // -------------------------------------------------------------------------
        // SCENARIO 7: BOOKING CANCELLATION & REFUND
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 7: Alice cancels her booking (" + aliceBooking.getBookingId() + ")...");
        boolean cancelled = service.cancelBooking(aliceBooking.getBookingId());

        if (cancelled) {
            System.out.println("🔎 Checking status of Alice's seats (A1, A2):");
            System.out.println("  A1 Status: " + layout.get("A1").getStatus());
            System.out.println("  A2 Status: " + layout.get("A2").getStatus());
            System.out.println("✅ Cancellation & Refund Complete.\n");
        }

        // Cleanup
        service.shutdown();
        System.out.println("======================================================================");
        System.out.println(" 🎬 MOVIE BOOKING SYSTEM DEMO COMPLETED SUCCESSFULLY");
        System.out.println("======================================================================");
    }
}
