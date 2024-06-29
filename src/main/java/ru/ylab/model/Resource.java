package ru.ylab.model;

public interface Resource {
    int getId();
    String getName();
    int getCapacity();
    void setId(int id);
    void setName(String name);
    void setCapacity(int capacity);
}