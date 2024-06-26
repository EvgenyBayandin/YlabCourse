package ru.ylab.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.repository.ResourceRepository;

public class ResourceService {
    private ResourceRepository resourceRepository;
    private BookingService bookingService;

    public ResourceService(ResourceRepository resourceRepository, BookingService bookingService) {
        this.resourceRepository = resourceRepository;
        this.bookingService = bookingService;
    }

    public void addResource(Resource resource) {
        resourceRepository.addResource(resource);
    }

    public Resource getResourceById(int id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    public void updateResource(Resource resource) {
        resourceRepository.updateResource(resource);
    }

    public List<Resource> getAllResources() {
        return resourceRepository.getAllResources();
    }

    public void deleteResource(Resource resource) {
        resourceRepository.deleteResource(resource);
    }

    public List<Resource> getAvailableResources(LocalDateTime start, LocalDateTime end) {
        List<Resource> allResources = resourceRepository.getAllResources();
        return allResources.stream()
                .filter(resource -> isResourceAvailable(resource, start, end))
                .collect(Collectors.toList());
    }

    private boolean isResourceAvailable(Resource resource, LocalDateTime start, LocalDateTime end) {
        List<Booking> resourceBookings = bookingService.getBookingsByResources(resource);
        return resourceBookings.stream()
                .noneMatch(booking ->
                        (start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime())) ||
                                start.equals(booking.getStartTime()) || end.equals(booking.getEndTime())
                );
    }

    public ResourceRepository getResourceRepository() {
        return resourceRepository;
    }

    public List<ru.ylab.model.TimeSlot> getAvailableTimeSlots(LocalDate date, int durationMinutes) {
        List<Resource> allResources = resourceRepository.getAllResources();
        List<ru.ylab.model.TimeSlot> availableSlots = new ArrayList<>();

        for (Resource resource : allResources) {
            List<ru.ylab.model.TimeSlot> resourceSlots = getAvailableSlotsForResource(resource, date, durationMinutes);
            availableSlots.addAll(resourceSlots);
        }

        return availableSlots;
    }

    private List<ru.ylab.model.TimeSlot> getAvailableSlotsForResource(Resource resource, LocalDate date, int durationMinutes) {
        List<ru.ylab.model.TimeSlot> availableSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0); // Предполагаем, что рабочий день начинается в 9:00
        LocalTime endTime = LocalTime.of(18, 0);  // и заканчивается в 18:00

        List<Booking> resourceBookings = bookingService.getBookingsByResources(resource)
                .stream()
                .filter(booking -> booking.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());

        while (startTime.plusMinutes(durationMinutes).isBefore(endTime) || startTime.plusMinutes(durationMinutes).equals(endTime)) {
            LocalDateTime slotStart = LocalDateTime.of(date, startTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

            if (isSlotAvailable(slotStart, slotEnd, resourceBookings)) {
                availableSlots.add(new ru.ylab.model.TimeSlot(resource, slotStart, slotEnd));
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
