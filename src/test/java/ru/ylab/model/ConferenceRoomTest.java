package ru.ylab.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link ConferenceRoom}.
 * Проверяет работу класса.
 */

public class ConferenceRoomTest {

    @Test
    public void testConstructor() {
        int id = 123;
        String name = "Room A";
        int capacity = 50;

        ConferenceRoom conferenceRoom = new ConferenceRoom(id, name, capacity);

        assertEquals(id, conferenceRoom.getId());
        assertEquals(name, conferenceRoom.getName());
        assertEquals(capacity, conferenceRoom.getCapacity());
    }

    @Test
    public void testSetterAndGetter() {

        ConferenceRoom conferenceRoom = new ConferenceRoom(456, "Room C", 10);

        // Изменение свойств объекта
        int id = 456;
        String name = "Room B";
        int capacity = 100;
        conferenceRoom.setName(name);
        conferenceRoom.setCapacity(capacity);

        // Проверка измененных свойств объекта
        assertEquals(id, conferenceRoom.getId());
        assertEquals(name, conferenceRoom.getName());
        assertEquals(capacity, conferenceRoom.getCapacity());
    }
}
