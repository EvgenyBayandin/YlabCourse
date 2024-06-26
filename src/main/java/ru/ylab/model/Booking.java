package ru.ylab.model;

import java.time.LocalDateTime;

public class Booking {
    private int id;
    private User user;
    private Resource resource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Booking(int id, User user, Resource resource, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.user = user;
        this.resource = resource;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}

