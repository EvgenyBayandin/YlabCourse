package ru.ylab.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.User;
import ru.ylab.repository.BookingRepository;
import ru.ylab.repository.ResourceRepository;
import ru.ylab.repository.UserRepository;

/**
 * Service class for managing booking-related operations.
 * This class provides CRUD methods for bookings and additional functionality
 * such as conflict checking and retrieving bookings based on various criteria.
 */
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    /**
     * Constructs a new BookingService with the necessary repositories.
     *
     * @param bookingRepository  the repository for booking operations
     * @param userRepository     the repository for user operations
     * @param resourceRepository the repository for resource operations
     */
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, ResourceRepository resourceRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }


    /**
     * Creates a new booking for a user and resource.
     *
     * @param user     the user making the booking
     * @param resource the resource being booked
     * @param start    the start time of the booking
     * @param end      the end time of the booking
     * @return the created Booking object
     * @throws SQLException             if a database access error occurs
     * @throws IllegalStateException    if there's a booking conflict
     * @throws IllegalArgumentException if the user or resource is not found
     */
    public Booking createBooking(User user, Resource resource, Timestamp start, Timestamp end) throws SQLException {
        if (hasBookingConflict(resource, start, end)) {
            throw new IllegalStateException("Booking conflict: The resource is not available for the selected time period");
        }

        Optional<User> currentUser = userRepository.findById(user.getId());
        if (currentUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Optional<Resource> currentResource = resourceRepository.findById(resource.getId());
        if (currentResource.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }

        Booking booking = new Booking(0, currentUser.get(), currentResource.get(), start, end);
        bookingRepository.addBooking(booking);
        return booking;
    }

    /**
     * Cancels an existing booking.
     *
     * @param booking the booking to be cancelled
     * @throws SQLException if a database access error occurs
     */
    public void cancelBooking(Booking booking) throws SQLException {
        bookingRepository.deleteBooking(booking);
    }

    /**
     * Retrieves all bookings for a specific user.
     *
     * @param user the user whose bookings are to be retrieved
     * @return a list of bookings for the user
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getBookingsByUser(User user) throws SQLException {
        return bookingRepository.getBookingsByUser(user);
    }

    /**
     * Retrieves all bookings for a specific resource.
     *
     * @param resource the resource whose bookings are to be retrieved
     * @return a list of bookings for the resource
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getBookingsByResource(Resource resource) throws SQLException {
        return bookingRepository.getBookingsByResources(resource);
    }

    /**
     * Retrieves all bookings for a specific date.
     *
     * @param date the date for which bookings are to be retrieved
     * @return a list of bookings for the date
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getBookingsByDate(Timestamp date) throws SQLException {
        return bookingRepository.getBookingsByDate(date);
    }

    /**
     * Checks if there's a booking conflict for a resource at a given time period.
     *
     * @param resource the resource to check
     * @param start    the start time of the period to check
     * @param end      the end time of the period to check
     * @return true if there's a conflict, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean hasBookingConflict(Resource resource, Timestamp start, Timestamp end) throws SQLException {
        List<Booking> resourceBookings = bookingRepository.getBookingsByResources(resource);
        return resourceBookings.stream()
                .anyMatch(booking -> (start.before(booking.getEndTime()) && end.after(booking.getStartTime())) ||
                        start.equals(booking.getStartTime()) || end.equals(booking.getEndTime()));
    }

    /**
     * Retrieves all bookings in the system.
     *
     * @return a list of all bookings
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getAllBookings() throws SQLException {
        return bookingRepository.getAllBookings();
    }

    /**
     * Retrieves a specific booking by its ID.
     *
     * @param id the ID of the booking to retrieve
     * @return an Optional containing the booking if found, or an empty Optional if not found
     * @throws SQLException if a database access error occurs
     */
    public Optional<Booking> getBookingById(int id) throws SQLException {
        return bookingRepository.findById(id);
    }

    /**
     * Updates an existing booking.
     *
     * @param booking the booking to be updated
     * @throws SQLException          if a database access error occurs
     * @throws IllegalStateException if there's a booking conflict after the update
     */
    public void updateBooking(Booking booking) throws SQLException {
        if (hasBookingConflict(booking.getResource(), booking.getStartTime(), booking.getEndTime())) {
            throw new IllegalStateException("Booking conflict: The resource is not available for the selected time period");
        }
        bookingRepository.updateBooking(booking);
    }

}
