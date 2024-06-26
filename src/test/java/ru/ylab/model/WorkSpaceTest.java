package ru.ylab.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link WorkSpace}.
 * Проверяет работу класса.
 */

class WorkSpaceTest {

    @Test
    public void testConstructor() {
        int id = 123;
        String name = "Room A";
        int capacity = 50;

        WorkSpace workSpace = new WorkSpace(id, name, capacity);

        assertEquals(id, workSpace.getId());
        assertEquals(name, workSpace.getName());
        assertEquals(capacity, workSpace.getCapacity());
    }

    @Test
    public void testSetterAndGetter() {

        WorkSpace workSpace = new WorkSpace(456, "Room C", 10);

        // Изменение свойств объекта
        int id = 456;
        String name = "Room B";
        int capacity = 100;
        workSpace.setName(name);
        workSpace.setCapacity(capacity);

        // Проверка измененных свойств объекта
        assertEquals(id, workSpace.getId());
        assertEquals(name, workSpace.getName());
        assertEquals(capacity, workSpace.getCapacity());
    }
}