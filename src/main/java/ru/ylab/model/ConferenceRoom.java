package ru.ylab.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConferenceRoom implements Resource {
    private int id;
    private String name;
    private int capacity;
}
