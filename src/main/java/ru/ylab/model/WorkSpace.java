package ru.ylab.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkSpace implements Resource {
    private int id;
    private String name;
    private int capacity;
}
