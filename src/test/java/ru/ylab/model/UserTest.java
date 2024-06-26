package ru.ylab.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link User}.
 * Проверяет работу класса.
 */

class UserTest {

    @Test
    public void testConstructor() {

        String username = "admin";
        String password = "secret";
        boolean isAdmin = true;

        User user = new User(username, password, isAdmin);

        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertTrue(user.isAdmin());
    }

    @Test
    public void testSetterAndGetter() {
        User user = new User("u", "u", false);

        user.setUsername("NewUsername");
        user.setPassword("NewPassword");
        user.setAdmin(false);

        assertEquals("NewUsername", user.getUsername());
        assertEquals("NewPassword", user.getPassword());
        assertFalse(user.isAdmin());
    }
}