package ru.ylab.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Booking {
    private int id;
    private User user;
    private Resource resource;
    private Timestamp startTime;
    private Timestamp endTime;

    public int getUserId() {
        return user.getId();
    }

    public int getResourceId() {
        return resource.getId();
    }
}
