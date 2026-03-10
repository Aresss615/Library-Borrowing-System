package com.library.utils;

import com.library.model.User;

/**
 * Manages the currently logged-in user session.
 * Simple singleton to track who is authenticated.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /** Set the logged-in user */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /** Get the logged-in user */
    public User getCurrentUser() {
        return currentUser;
    }

    /** Check if an admin is logged in */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    /** Log out the current user */
    public void logout() {
        this.currentUser = null;
    }

    /** Check if anyone is logged in */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
