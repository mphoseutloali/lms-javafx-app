package com.example.demo.lms.model;

import java.time.LocalDateTime;

public class Admin {
    private int adminId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public Admin() {
    }

    public Admin(int adminId, String username, String password, String fullName, String email,
                 boolean isActive, LocalDateTime lastLogin, LocalDateTime createdAt) {
        this.adminId = adminId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
