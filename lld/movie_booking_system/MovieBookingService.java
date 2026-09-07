package movie_booking_system;

import movie_booking_system.BookingAndTicket.*;
import movie_booking_system.CoreModels.*;
import movie_booking_system.Enums.*;
import movie_booking_system.Payment.*;
import movie_booking_system.PricingStrategy.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MovieBookingService {
    private static volatile MovieBookingService instance;

    // Catalogs
    private final Map<City, List<Theatre>> cityTheatresMap = new ConcurrentHashMap<>();
    private final Map<String, Movie> moviesMap = new ConcurrentHashMap<>();
    private final Map<String, Show> showsMap = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookingsMap = new ConcurrentHashMap<>();

    // Dependencies
    private final SeatLockManager seatLockManager;
    private IPricingStrategy pricingStrategy;
    private IDiscountStrategy discountStrategy;

    private static final long SEAT_LOCK_TIMEOUT_SECONDS = 10; // Short TTL for demo/testing

    private MovieBookingService() {
        this.seatLockManager = SeatLockManager.getInstance();
        this.pricingStrategy = new DefaultPricingStrategy();
        this.discountStrategy = new CouponDiscountStrategy();
    }

    public static MovieBookingService getInstance() {
        if (instance == null) {
            synchronized (MovieBookingService.class) {
                if (instance == null) {
                    instance = new MovieBookingService();
                }
            }
        }
        return instance;
    }

    public void setPricingStrategy(IPricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public void setDiscountStrategy(IDiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    // Catalog Administration
    public void addTheatre(Theatre theatre) {
        cityTheatresMap.computeIfAbsent(theatre.getCity(), k -> new ArrayList<>()).add(theatre);
    }

    public void addMovie(Movie movie) {
        moviesMap.put(movie.getMovieId(), movie);
    }

    public void addShow(Show show) {
        showsMap.put(show.getShowId(), show);
    }

    // Browsing & Search
    public List<Movie> searchMovies(City city, Genre genre, String language) {
        List<Movie> matchingMovies = new ArrayList<>();
        List<Theatre> theatres = cityTheatresMap.getOrDefault(city, Collections.emptyList());

        Set<String> movieIdsInCity = new HashSet<>();
        for (Show show : showsMap.values()) {
            if (theatres.contains(show.getTheatre())) {
                movieIdsInCity.add(show.getMovie().getMovieId());
            }
        }

        for (String movieId : movieIdsInCity) {
            Movie m = moviesMap.get(movieId);
            if (m != null) {
                boolean matchesGenre = (genre == null || m.getGenre() == genre);
                boolean matchesLang = (language == null || m.getLanguage().equalsIgnoreCase(language));
                if (matchesGenre && matchesLang) {
                    matchingMovies.add(m);
                }
            }
        }
        return matchingMovies;
    }

    public List<Show> getShows(City city, String movieId) {
        List<Show> result = new ArrayList<>();
        List<Theatre> theatres = cityTheatresMap.getOrDefault(city, Collections.emptyList());
        for (Show show : showsMap.values()) {
            if (theatres.contains(show.getTheatre()) && show.getMovie().getMovieId().equals(movieId)) {
                result.add(show);
            }
        }
        return result;
    }

    public Map<String, ShowSeat> getShowSeatLayout(String showId) {
        Show show = showsMap.get(showId);
        if (show == null) {
            throw new IllegalArgumentException("Show not found: " + showId);
        }
        return show.getShowSeats();
    }

    // Booking Workflow
    public Booking createBooking(String userId, String showId, List<String> seatIds, String couponCode) {
        Show show = showsMap.get(showId);
        if (show == null) {
            System.out.println("❌ Invalid show ID: " + showId);
            return null;
        }

        List<ShowSeat> selectedSeats = new ArrayList<>();
        for (String seatId : seatIds) {
            ShowSeat ss = show.getShowSeats().get(seatId);
            if (ss == null) {
                System.out.println("❌ Seat " + seatId + " not found!");
                return null;
            }
            selectedSeats.add(ss);
        }

        double rawPrice = pricingStrategy.calculatePrice(show, selectedSeats);
        Booking booking = new Booking(userId, show, selectedSeats, rawPrice);

        // Attempt atomic seat lock using real bookingId
        boolean locked = seatLockManager.lockSeats(booking.getBookingId(), show, seatIds, SEAT_LOCK_TIMEOUT_SECONDS);
        if (!locked) {
            System.out.println("❌ Failed to lock seats for user: " + userId);
            return null;
        }

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            double discount = discountStrategy.calculateDiscount(rawPrice, couponCode);
            booking.applyDiscount(discount);
            System.out.printf("🎟️ Applied Coupon [%s]: Saved $%.2f%n", couponCode, discount);
        }

        bookingsMap.put(booking.getBookingId(), booking);
        
        System.out.printf("🛒 Created Booking [%s] | Total: $%.2f | Final: $%.2f%n",
                booking.getBookingId(), booking.getTotalAmount(), booking.getFinalAmount());

        return booking;
    }

    public Ticket confirmBooking(String bookingId, PaymentMode paymentMode) {
        Booking booking = bookingsMap.get(bookingId);
        if (booking == null) {
            System.out.println("❌ Booking not found: " + bookingId);
            return null;
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            System.out.println("❌ Booking is not in PENDING_PAYMENT status: " + booking.getStatus());
            return null;
        }

        // Step 1: Process Payment
        IPaymentProcessor processor = PaymentProcessorFactory.getProcessor(paymentMode);
        PaymentRecord paymentRecord = processor.processPayment(bookingId, booking.getFinalAmount(), paymentMode);

        if (paymentRecord.getStatus() != PaymentStatus.SUCCESS) {
            System.out.println("❌ Payment failed for booking: " + bookingId);
            seatLockManager.unlockSeats(bookingId);
            return null;
        }

        // Step 2: Confirm Seat Lock & update booking status
        boolean confirmed = seatLockManager.confirmLock(bookingId);
        if (!confirmed) {
            System.out.println("❌ Seat lock expired before payment completed! Initiating refund...");
            processor.processRefund(paymentRecord);
            booking.markExpired();
            return null;
        }

        booking.confirmBooking(paymentRecord);

        // Step 3: Issue Ticket & Send Notification
        Ticket ticket = new Ticket(booking);
        NotificationService.sendBookingConfirmation(booking.getUserId(), ticket);
        return ticket;
    }

    public boolean cancelBooking(String bookingId) {
        Booking booking = bookingsMap.get(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
            System.out.println("❌ Booking cannot be cancelled or not found: " + bookingId);
            return false;
        }

        // Step 1: Release seats back to AVAILABLE
        for (ShowSeat ss : booking.getSeats()) {
            ss.markAvailable();
        }

        // Step 2: Process Refund
        IPaymentProcessor processor = PaymentProcessorFactory.getProcessor(booking.getPaymentRecord().getPaymentMode());
        processor.processRefund(booking.getPaymentRecord());

        // Step 3: Update Status & Notify
        booking.cancelBooking();
        NotificationService.sendCancellationNotification(booking.getUserId(), bookingId, booking.getFinalAmount());
        return true;
    }

    public void shutdown() {
        seatLockManager.shutdown();
    }
}
