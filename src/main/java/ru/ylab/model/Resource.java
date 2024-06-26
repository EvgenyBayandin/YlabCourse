package ru.ylab.model;

public interface Resource {
    int getId();
    String getName();
    int getCapacity();
    void setName(String name);
    void setCapacity(int capacity);
}