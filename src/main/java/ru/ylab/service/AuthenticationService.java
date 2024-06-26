package ru.ylab.service;

import ru.ylab.model.User;
import ru.ylab.repository.UserRepository;

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

    public User authenticate(String username, String password){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(!user.getPassword().equals(password)){
            throw new IllegalArgumentException("Wrong password");
        }
        currentUser = user;
        return user;
    }

    public boolean isAuthenticated(){
        return currentUser != null;
    }

    public User getCurrentUser(){
        if (!isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return currentUser;
    }

    public void logout(){
        currentUser = null;
    }
}
