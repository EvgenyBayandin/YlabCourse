package ru.ylab.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeSlot {
    private Resource resource;
    private Timestamp start;
    private Timestamp end;

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return "Resource: " + resource.getName() +
                " (ID: " + resource.getId() + ")" +
                ", Start: " + start.toLocalDateTime().format(formatter) +
                ", End: " + end.toLocalDateTime().format(formatter);
    }
}
