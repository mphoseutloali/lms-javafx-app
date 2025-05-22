package com.example.demo.lms.dao;

import com.example.demo.lms.model.Enrollment;
import com.example.demo.lms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, s.first_name, s.last_name, c.course_name " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN courses c ON e.course_id = c.course_id " +
                "ORDER BY e.enrollment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getInt("student_id"));
                enrollment.setCourseId(rs.getInt("course_id"));
                enrollment.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());
                enrollment.setProgress(rs.getInt("progress"));

                // Set display names
                enrollment.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
                enrollment.setCourseName(rs.getString("course_name"));

                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving enrollments");
            e.printStackTrace();
        }

        return enrollments;
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.course_name " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.course_id " +
                "WHERE e.student_id = ? " +
                "ORDER BY e.enrollment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getInt("student_id"));
                enrollment.setCourseId(rs.getInt("course_id"));
                enrollment.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());
                enrollment.setProgress(rs.getInt("progress"));
                enrollment.setCourseName(rs.getString("course_name"));

                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving enrollments for student ID: " + studentId);
            e.printStackTrace();
        }

        return enrollments;
    }

    public boolean addEnrollment(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date, progress) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, enrollment.getStudentId());
            pstmt.setInt(2, enrollment.getCourseId());
            pstmt.setDate(3, Date.valueOf(enrollment.getEnrollmentDate()));
            pstmt.setInt(4, enrollment.getProgress());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        enrollment.setEnrollmentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding enrollment");
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateEnrollmentProgress(int enrollmentId, int progress) {
        String sql = "UPDATE enrollments SET progress = ? WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, progress);
            pstmt.setInt(2, enrollmentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating enrollment progress for ID: " + enrollmentId);
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteEnrollment(int enrollmentId) {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting enrollment with ID: " + enrollmentId);
            e.printStackTrace();
        }

        return false;
    }
}
