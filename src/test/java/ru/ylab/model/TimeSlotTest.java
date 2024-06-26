package ru.ylab.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Тестовый класс для {@link TimeSlot}.
 * Проверяет работу класса.
 */

public class TimeSlotTest {

    // Аргументы для конструктора
    Resource resource = new Resource() {
        @Override
        public int getId() {
            return 1;
        }

        @Override
        public String getName() {
            return "Room A";
        }

        @Override
        public int getCapacity() {
            return 10;
        }

        @Override
        public void setName(String name) {

        }

        @Override
        public void setCapacity(int capacity) {

        }
    };
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = LocalDateTime.now().plusHours(1);

    // Создание объекта TimeSlot
    TimeSlot timeSlot = new TimeSlot(resource, start, end);

    @Test
    public void testConstructor() {

        // Проверка свойств объекта TimeSlot
        assertEquals(resource, timeSlot.getResource());
        assertEquals(start, timeSlot.getStart());
        assertEquals(end, timeSlot.getEnd());
    }

    @Test
    public void testToString() {

        // Проверка метода toString
        String expectedString = "Resource: " + resource.getName() +
                " (ID: " + resource.getId() + ")" +
                ", Start: " + start.format(DateTimeFormatter.ofPattern("HH:mm")) +
                ", End: " + end.format(DateTimeFormatter.ofPattern("HH:mm"));
        assertEquals(expectedString, timeSlot.toString());
    }
}
