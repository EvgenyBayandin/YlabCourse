package ru.ylab.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.TimeSlot;
import ru.ylab.repository.ResourceRepository;

/**
 * Service class for managing resources.
 */
public class ResourceService {
    private ResourceRepository resourceRepository;
    private BookingService bookingService;

    /**
     * Constructs a new ResourceService with the given repositories.
     *
     * @param resourceRepository the repository for managing resources
     * @param bookingService     the service for managing bookings
     */
    public ResourceService(ResourceRepository resourceRepository, BookingService bookingService) {
        this.resourceRepository = resourceRepository;
        this.bookingService = bookingService;
    }

    /**
     * Adds a new resource to the system.
     *
     * @param resource the resource to add
     * @throws SQLException if a database error occurs
     */
    public void addResource(Resource resource) throws SQLException {
        resourceRepository.addResource(resource);
    }

    /**
     * Retrieves a resource by its ID.
     *
     * @param id the ID of the resource to retrieve
     * @return the resource with the given ID
     * @throws SQLException             if a database error occurs
     * @throws IllegalArgumentException if no resource is found with the given ID
     */
    public Resource getResourceById(int id) throws SQLException {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    /**
     * Updates an existing resource in the system.
     *
     * @param resource the resource to update
     * @throws SQLException if a database error occurs
     */
    public void updateResource(Resource resource) throws SQLException {
        resourceRepository.updateResource(resource);
    }

    /**
     * Retrieves all resources in the system.
     *
     * @return a list of all resources
     * @throws SQLException if a database error occurs
     */
    public List<Resource> getAllResources() throws SQLException {
        return resourceRepository.getAllResources();
    }

    /**
     * Deletes a resource from the system.
     *
     * @param resource the resource to delete
     * @throws SQLException if a database error occurs
     */
    public void deleteResource(Resource resource) throws SQLException {
        resourceRepository.deleteResource(resource);
    }

    /**
     * Retrieves resources by their type.
     *
     * @param type the type of resources to retrieve
     * @return a list of resources of the given type
     * @throws SQLException if a database error occurs
     */
    public List<Resource> getResourcesByType(String type) throws SQLException {
        return resourceRepository.getResourcesByType(type);
    }

    /**
     * Retrieves available resources for a given time period.
     *
     * @param start the start time of the period
     * @param end   the end time of the period
     * @return a list of available resources
     * @throws SQLException if a database error occurs
     */
    public List<Resource> getAvailableResources(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<Resource> allResources = resourceRepository.getAllResources();
        return allResources.stream()
                .filter(resource -> {
                    try {
                        return isResourceAvailable(resource, start, end);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean isResourceAvailable(Resource resource, LocalDateTime start, LocalDateTime end) throws SQLException {
        List<Booking> resourceBookings = bookingService.getBookingsByResource(resource);
        return resourceBookings.stream()
                .noneMatch(booking ->
                        (start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime())) ||
                                start.equals(booking.getStartTime()) || end.equals(booking.getEndTime())
                );
    }

    /**
     * Retrieves the resource repository.
     *
     * @return the resource repository
     */
    public ResourceRepository getResourceRepository() throws SQLException {
        return resourceRepository;
    }

    /**
     * Retrieves available time slots for all resources on a given date.
     *
     * @param date            the date to check for available slots
     * @param durationMinutes the duration of each slot in minutes
     * @return a list of available time slots
     * @throws SQLException if a database error occurs
     */
    public List<TimeSlot> getAvailableTimeSlots(LocalDate date, int durationMinutes) throws SQLException {
        List<Resource> allResources = resourceRepository.getAllResources();
        List<TimeSlot> availableSlots = new ArrayList<>();

        for (Resource resource : allResources) {
            List<TimeSlot> resourceSlots = getAvailableSlotsForResource(resource, date, durationMinutes);
            availableSlots.addAll(resourceSlots);
        }

        return availableSlots;
    }

    private List<TimeSlot> getAvailableSlotsForResource(Resource resource, LocalDate date, int durationMinutes) throws SQLException {
        List<TimeSlot> availableSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0); // Предполагаем, что рабочий день начинается в 9:00
        LocalTime endTime = LocalTime.of(18, 0);  // и заканчивается в 18:00

        List<Booking> resourceBookings = bookingService.getBookingsByResource(resource)
                .stream()
                .filter(booking -> booking.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());

        while (startTime.plusMinutes(durationMinutes).isBefore(endTime) || startTime.plusMinutes(durationMinutes).equals(endTime)) {
            LocalDateTime slotStart = LocalDateTime.of(date, startTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

            if (isSlotAvailable(slotStart, slotEnd, resourceBookings)) {
                availableSlots.add(new TimeSlot(resource, slotStart, slotEnd));
            }

            startTime = startTime.plusMinutes(30); // Шаг в 30 минут
        }

        return availableSlots;
    }

    private boolean isSlotAvailable(LocalDateTime start, LocalDateTime end, List<Booking> bookings) {
        return bookings.stream()
                .noneMatch(booking ->
                        (start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime())) ||
                                start.equals(booking.getStartTime()) || end.equals(booking.getEndTime()));
    }
}
