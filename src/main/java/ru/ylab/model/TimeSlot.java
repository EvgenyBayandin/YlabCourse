package ru.ylab.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeSlot {
    private Resource resource;
    private LocalDateTime start;
    private LocalDateTime end;

    @Override
    public String toString() {
        return "Resource: " + resource.getName() +
                " (ID: " + resource.getId() + ")" +
                ", Start: " + start.format(DateTimeFormatter.ofPattern("HH:mm")) +
                ", End: " + end.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
