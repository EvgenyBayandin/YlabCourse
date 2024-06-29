package ru.ylab.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Booking {
    private int id;
    private User user;
    private Resource resource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public int getUserId() {
        return user.getId();
    }

    public int getResourceId() {
        return resource.getId();
    }
}
