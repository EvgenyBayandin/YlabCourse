package ru.ylab.model;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.ylab.model.Booking;

/**
 * Тестовый класс для {@link Booking}.
 * Проверяет работу класса.
 */

public class BookingTest {

    @Test
    public void testConstructor() {
        int id = 123;
        ru.ylab.model.User user = new ru.ylab.model.User("a", "a", true);
        ru.ylab.model.Resource resource = new ru.ylab.model.Resource() {
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
                return 100;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        Booking booking = new Booking(id, user, resource, startTime, endTime);

        Assertions.assertEquals(id, booking.getId());
        Assertions.assertEquals(user, booking.getUser());
        Assertions.assertEquals(resource, booking.getResource());
        Assertions.assertEquals(startTime, booking.getStartTime());
        Assertions.assertEquals(endTime, booking.getEndTime());
    }

    @Test
    public void testSettersAndGetters() {
        Booking booking = new Booking(1, new ru.ylab.model.User("a", "a", true), new ru.ylab.model.Resource() {
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
                return 200;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        }, LocalDateTime.now(), LocalDateTime.now());

        LocalDateTime newStartTime = LocalDateTime.now().plusDays(1);
        LocalDateTime newEndTime = LocalDateTime.now().plusDays(2);
        booking.setStartTime(newStartTime);
        booking.setEndTime(newEndTime);

        Assertions.assertEquals(newStartTime, booking.getStartTime());
        Assertions.assertEquals(newEndTime, booking.getEndTime());
    }
}