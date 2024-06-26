package ru.ylab.service;

import ru.ylab.model.User;
import ru.ylab.repository.UserRepository;

public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        User newUser = new User(username, password, false);
        userRepository.addUser(newUser);
        return newUser;
    }

    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        user.setPassword(newPassword);
        userRepository.updateUser(user);
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.deleteUser(user);
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
