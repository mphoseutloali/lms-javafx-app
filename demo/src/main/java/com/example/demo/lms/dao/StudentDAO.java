package com.example.demo.lms.dao;

import com.example.demo.lms.model.Student;
import com.example.demo.lms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getInt("student_id"));
                student.setFirstName(rs.getString("first_name"));
                student.setLastName(rs.getString("last_name"));
                student.setEmail(rs.getString("email"));
                student.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());

                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving students");
            e.printStackTrace();
        }

        return students;
    }

    public Student getStudentById(int id) {
        String sql = "SELECT * FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getInt("student_id"));
                student.setFirstName(rs.getString("first_name"));
                student.setLastName(rs.getString("last_name"));
                student.setEmail(rs.getString("email"));
                student.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());

                return student;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving student with ID: " + id);
            e.printStackTrace();
        }

        return null;
    }

    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (first_name, last_name, email, enrollment_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getLastName());
            pstmt.setString(3, student.getEmail());
            pstmt.setDate(4, Date.valueOf(student.getEnrollmentDate()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding student");
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, enrollment_date = ? WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getLastName());
            pstmt.setString(3, student.getEmail());
            pstmt.setDate(4, Date.valueOf(student.getEnrollmentDate()));
            pstmt.setInt(5, student.getStudentId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student with ID: " + student.getStudentId());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student with ID: " + id);
            e.printStackTrace();
        }

        return false;
    }
}