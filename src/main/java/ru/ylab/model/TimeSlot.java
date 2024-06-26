package ru.ylab.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeSlot {
    private Resource resource;
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlot(Resource resource, LocalDateTime start, LocalDateTime end) {
        this.resource = resource;
        this.start = start;
        this.end = end;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Resource: " + resource.getName() +
                " (ID: " + resource.getId() + ")" +
                ", Start: " + start.format(DateTimeFormatter.ofPattern("HH:mm")) +
                ", End: " + end.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
