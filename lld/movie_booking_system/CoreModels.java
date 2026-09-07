package movie_booking_system;

import movie_booking_system.Enums.*;

import java.time.LocalDateTime;
import java.util.*;

public class CoreModels {

    public static class Movie {
        private final String movieId;
        private final String title;
        private final Genre genre;
        private final MovieRating rating;
        private final String language;
        private final int durationMinutes;

        public Movie(String movieId, String title, Genre genre, MovieRating rating, String language, int durationMinutes) {
            this.movieId = movieId;
            this.title = title;
            this.genre = genre;
            this.rating = rating;
            this.language = language;
            this.durationMinutes = durationMinutes;
        }

        public String getMovieId() { return movieId; }
        public String getTitle() { return title; }
        public Genre getGenre() { return genre; }
        public MovieRating getRating() { return rating; }
        public String getLanguage() { return language; }
        public int getDurationMinutes() { return durationMinutes; }

        @Override
        public String toString() {
            return String.format("%s (%s, %s, %d mins)", title, language, genre, durationMinutes);
        }
    }

    public static class Seat {
        private final String seatId;
        private final int row;
        private final int number;
        private final SeatType seatType;

        public Seat(String seatId, int row, int number, SeatType seatType) {
            this.seatId = seatId;
            this.row = row;
            this.number = number;
            this.seatType = seatType;
        }

        public String getSeatId() { return seatId; }
        public int getRow() { return row; }
        public int getNumber() { return number; }
        public SeatType getSeatType() { return seatType; }

        @Override
        public String toString() {
            return String.format("%s [Row %d-%d | %s]", seatId, row, number, seatType);
        }
    }

    public static class Screen {
        private final String screenId;
        private final String name;
        private final List<Seat> seats;

        public Screen(String screenId, String name, List<Seat> seats) {
            this.screenId = screenId;
            this.name = name;
            this.seats = seats;
        }

        public String getScreenId() { return screenId; }
        public String getName() { return name; }
        public List<Seat> getSeats() { return seats; }
    }

    public static class Theatre {
        private final String theatreId;
        private final String name;
        private final City city;
        private final List<Screen> screens;

        public Theatre(String theatreId, String name, City city, List<Screen> screens) {
            this.theatreId = theatreId;
            this.name = name;
            this.city = city;
            this.screens = screens;
        }

        public String getTheatreId() { return theatreId; }
        public String getName() { return name; }
        public City getCity() { return city; }
        public List<Screen> getScreens() { return screens; }
    }

    public static class ShowSeat {
        private final String showSeatId;
        private final Seat seat;
        private final String showId;
        private final double price;
        private volatile SeatStatus status;

        public ShowSeat(String showSeatId, Seat seat, String showId, double price) {
            this.showSeatId = showSeatId;
            this.seat = seat;
            this.showId = showId;
            this.price = price;
            this.status = SeatStatus.AVAILABLE;
        }

        public String getShowSeatId() { return showSeatId; }
        public Seat getSeat() { return seat; }
        public String getShowId() { return showId; }
        public double getPrice() { return price; }
        public SeatStatus getStatus() { return status; }
        public void setStatus(SeatStatus status) { this.status = status; }

        public synchronized boolean markBlocked() {
            if (this.status == SeatStatus.AVAILABLE) {
                this.status = SeatStatus.TEMPORARILY_BLOCKED;
                return true;
            }
            return false;
        }

        public synchronized boolean markBooked() {
            if (this.status == SeatStatus.TEMPORARILY_BLOCKED) {
                this.status = SeatStatus.BOOKED;
                return true;
            }
            return false;
        }

        public synchronized void markAvailable() {
            this.status = SeatStatus.AVAILABLE;
        }

        @Override
        public String toString() {
            return String.format("ShowSeat{%s, %s, %s, $%.2f}", showSeatId, seat.getSeatId(), status, price);
        }
    }

    public static class Show {
        private final String showId;
        private final Movie movie;
        private final Screen screen;
        private final Theatre theatre;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final ShowFormat showFormat;
        private final Map<String, ShowSeat> showSeats;

        public Show(String showId, Movie movie, Screen screen, Theatre theatre,
                    LocalDateTime startTime, ShowFormat showFormat, double priceMultiplier) {
            this.showId = showId;
            this.movie = movie;
            this.screen = screen;
            this.theatre = theatre;
            this.startTime = startTime;
            this.endTime = startTime.plusMinutes(movie.getDurationMinutes());
            this.showFormat = showFormat;
            this.showSeats = new HashMap<>();

            // Initialize show seats based on screen layout
            for (Seat seat : screen.getSeats()) {
                String ssId = showId + "_" + seat.getSeatId();
                double calculatedPrice = seat.getSeatType().getBasePrice() * showFormat.getPriceMultiplier() * priceMultiplier;
                showSeats.put(seat.getSeatId(), new ShowSeat(ssId, seat, showId, calculatedPrice));
            }
        }

        public String getShowId() { return showId; }
        public Movie getMovie() { return movie; }
        public Screen getScreen() { return screen; }
        public Theatre getTheatre() { return theatre; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public ShowFormat getShowFormat() { return showFormat; }
        public Map<String, ShowSeat> getShowSeats() { return showSeats; }

        public List<ShowSeat> getAvailableSeats() {
            List<ShowSeat> available = new ArrayList<>();
            for (ShowSeat ss : showSeats.values()) {
                if (ss.getStatus() == SeatStatus.AVAILABLE) {
                    available.add(ss);
                }
            }
            return available;
        }
    }
}
