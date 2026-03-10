package com.library.model;

import java.time.LocalDateTime;

/**
 * Represents a user in the library system.
 * Can be either an ADMIN or a STUDENT.
 */
public class User {

    public enum Role { ADMIN, STUDENT }

    private int id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ───────────────────────────────────────────

    public User() {}

    public User(String username, String password, String fullName,
                String email, String phone, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
        this.role     = role;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
