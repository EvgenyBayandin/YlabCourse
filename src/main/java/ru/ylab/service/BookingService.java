package ru.ylab.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.User;
import ru.ylab.repository.BookingRepository;

public class BookingService {
    private BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(User user, Resource resource, LocalDateTime start, LocalDateTime end) {
        if (hasBookingConflict(resource, start, end)) {
            throw new IllegalStateException("Booking conflict: The resource is not available for the selected time period");
        }

        Booking newBooking = new Booking(0, user, resource, start, end);
        bookingRepository.addBooking(newBooking);
        return newBooking;
    }

    public void cancelBooking(Booking booking) {
        bookingRepository.deleteBooking(booking);
    }

    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.getBookingsByUser(user);
    }

    public List<Booking> getBookingsByResources(Resource resource) {
        return bookingRepository.getBookingsByResources(resource);
    }

    public List<Booking> getBookingsByDate(LocalDate date) {
        return bookingRepository.getAllBookings().stream()
                .filter(booking -> booking.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public boolean hasBookingConflict(Resource resource, LocalDateTime start, LocalDateTime end) {
        List<Booking> resourceBookings = bookingRepository.getBookingsByResources(resource);
        return resourceBookings.stream()
                .anyMatch(booking -> (start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime())) || (start
                        .isEqual(booking.getStartTime()) || end.isEqual(booking.getEndTime())));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.getAllBookings();
    }
}
