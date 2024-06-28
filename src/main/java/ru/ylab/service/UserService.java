package ru.ylab.service;

import java.sql.SQLException;
import ru.ylab.model.User;
import ru.ylab.repository.UserRepository;

/**
 * Service class for managing user-related operations.
 * This class provides methods for user registration, authentication, and account management.
 */
public class UserService {
    private UserRepository userRepository;

    /**
     * Constructs a new UserService with the given UserRepository.
     *
     * @param userRepository the repository to use for user data persistence
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user with the given username and password.
     *
     * @param username the username for the new user
     * @param password the password for the new user
     * @return the newly created User object
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the username already exists
     */
    public User register(String username, String password) throws SQLException {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        User newUser = new User(username, password, false);
        userRepository.addUser(newUser);
        return newUser;
    }

    /**
     * Authenticates a user with the given username and password.
     *
     * @param username the username of the user to authenticate
     * @param password the password to check
     * @return the authenticated User object
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the username or password is invalid
     */
    public User login(String username, String password) throws SQLException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
    }

    /**
     * Changes the password for a user.
     *
     * @param username the username of the user
     * @param oldPassword the current password of the user
     * @param newPassword the new password to set
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the username or old password is invalid
     */
    public void changePassword(String username, String oldPassword, String newPassword) throws SQLException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        user.setPassword(newPassword);
        userRepository.updateUser(user);
    }

    /**
     * Deletes a user with the given username.
     *
     * @param username the username of the user to delete
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the user is not found
     */
    public void deleteUser(String username) throws SQLException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.deleteUser(user);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user to retrieve
     * @return the User object
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the user is not found
     */
    public User getUser(String username) throws SQLException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}

//package ru.ylab.service;
//
//import java.sql.SQLException;
//import ru.ylab.model.User;
//import ru.ylab.repository.UserRepository;
//
//public class UserService {
//    private UserRepository userRepository;
//
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public User register(String username, String password) throws SQLException {
//        if (userRepository.findByUsername(username).isPresent()) {
//            throw new IllegalArgumentException("User already exists");
//        }
//        User newUser = new User(username, password, false);
//        userRepository.addUser(newUser);
//        return newUser;
//    }
//
//    public User login(String username, String password) throws SQLException {
//        return userRepository.findByUsername(username)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
//    }
//
//    public void changePassword(String username, String oldPassword, String newPassword) throws SQLException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
//        if (!user.getPassword().equals(oldPassword)) {
//            throw new IllegalArgumentException("Invalid username or password");
//        }
//        user.setPassword(newPassword);
//        userRepository.updateUser(user);
//    }
//
//    public void deleteUser(String username) throws SQLException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//        userRepository.deleteUser(user);
//    }
//
//    public User getUser(String username) throws SQLException {
//        return userRepository.findByUsername(username)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//    }
//
//}
