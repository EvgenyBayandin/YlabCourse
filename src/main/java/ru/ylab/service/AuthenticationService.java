package ru.ylab.service;

import java.sql.SQLException;
import ru.ylab.model.User;
import ru.ylab.repository.UserRepository;

/**
 * Service class for authentication and authorization.
 * This class is responsible for user authentication and session management.
 */
public class AuthenticationService {
    private UserRepository userRepository;
    private User currentUser;

    public AuthenticationService(UserRepository userRepository, User currentUser) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates user by username and password and returns user object.
     *
     * @param username the username
     * @param password the password
     * @return User object
     * @throws SQLException if a database error occurs
     */
    public User authenticate(String username, String password) throws SQLException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(!user.getPassword().equals(password)){
            throw new IllegalArgumentException("Wrong password");
        }
        currentUser = user;
        return user;
    }

    /**
     * Verification of the authentication status.
     *
     * @return true, or false
     */
    public boolean isAuthenticated(){
        return currentUser != null;
    }

    /**
     * Getting the current user.
     *
     * @return object of the current user
     * @throws IllegalStateException if the user is not authenticated
     */
    public User getCurrentUser(){
        if (!isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return currentUser;
    }

    /**
     * Exiting the session.
     *
     * @return null
     */
    public void logout(){
        currentUser = null;
    }
}
