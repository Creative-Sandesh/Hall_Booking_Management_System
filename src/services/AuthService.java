package services;

import models.User;

import java.util.List;

public class AuthService {

    public static User login(String username, String rawPassword) {
        List<User> users = FileHandler.loadUsers();

        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(rawPassword)) {
                return u;
            }
        }
        return null; // Login failed
    }
}