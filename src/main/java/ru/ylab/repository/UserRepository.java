package ru.ylab.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.ylab.model.User;

public class UserRepository {
    private List<User> users = new ArrayList();

    public UserRepository() {
        users.add(new User("a", "a", true));
        users.add(new User("u",  "u", false));
    }

    public void addUser(User user) {
        users.add(user);
    }

    public Optional<User> findByUsername(String username) {
        return users.stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList(users);
    }

    public void updateUser(User user) {
        users.set(users.indexOf(user), user);
    }

    public void deleteUser(User user) {
        users.remove(user);
    }
}
