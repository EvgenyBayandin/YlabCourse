package ru.ylab.repository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.User;

public class BookingRepository {
    private List<Booking> bookings = new ArrayList<>();
    private int nextId = 100;

    public void addBooking(Booking booking)   {
        if (booking.getId() == 0) {
            try {
                Field idField = booking.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(booking, nextId++);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set booking ID", e);
            }
        }
        bookings.add(booking);
    }

    public Optional<Booking> findById(int id) {
        return bookings.stream()
                .filter(booking -> booking.getId() == id)
                .findFirst();
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    public List<Booking> getBookingsByUser(User user)   {
        return bookings.stream().filter(booking -> booking.getUser()
                        .equals(user))
                .collect(Collectors.toList());
    }

    public List<Booking> getBookingsByResources(Resource resource) {
        return bookings.stream().filter(booking -> booking.getResource()
                        .equals(resource))
                .collect(Collectors.toList());
    }

    public List<Booking> getBookingsByDate(LocalDate date)    {
        return bookings.stream().filter(booking -> booking
                        .getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public void updateBooking(Booking booking)  {
        bookings.set(bookings.indexOf(booking), booking);
    }

    public void deleteBooking(Booking booking)   {
        bookings.remove(booking);
    }

}
