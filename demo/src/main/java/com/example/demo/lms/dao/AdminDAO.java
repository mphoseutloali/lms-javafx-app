package com.example.demo.lms.dao;

import com.example.demo.lms.model.Admin;
import com.example.demo.lms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class AdminDAO {

    public Admin getAdminByUsername(String username) {
        String sql = "SELECT * FROM admin_users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setAdminId(rs.getInt("admin_id"));
                admin.setUsername(rs.getString("username"));
                admin.setPassword(rs.getString("password"));
                admin.setFullName(rs.getString("full_name"));
                admin.setEmail(rs.getString("email"));
                admin.setActive(rs.getBoolean("is_active"));

                if (rs.getTimestamp("last_login") != null) {
                    admin.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
                }

                if (rs.getTimestamp("created_at") != null) {
                    admin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }

                return admin;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving admin with username: " + username);
            e.printStackTrace();
        }

        return null;
    }

    public boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM admin_users WHERE username = ? AND password = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // If a record is found, login is valid
        } catch (SQLException e) {
            System.err.println("Error validating login for username: " + username);
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateLastLogin(String username) {
        String sql = "UPDATE admin_users SET last_login = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, username);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating last login for username: " + username);
            e.printStackTrace();
        }

        return false;
    }
}
