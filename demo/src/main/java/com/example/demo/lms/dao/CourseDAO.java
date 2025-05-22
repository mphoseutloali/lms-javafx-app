package com.example.demo.lms.dao;

import com.example.demo.lms.model.Course;
import com.example.demo.lms.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getInt("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setDescription(rs.getString("description"));
                course.setCreditHours(rs.getInt("credit_hours"));

                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving courses");
            e.printStackTrace();
        }

        return courses;
    }

    public Course getCourseById(int id) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getInt("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setDescription(rs.getString("description"));
                course.setCreditHours(rs.getInt("credit_hours"));

                return course;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving course with ID: " + id);
            e.printStackTrace();
        }

        return null;
    }

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (course_name, description, credit_hours) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, course.getCourseName());
            pstmt.setString(2, course.getDescription());
            pstmt.setInt(3, course.getCreditHours());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        course.setCourseId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding course");
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET course_name = ?, description = ?, credit_hours = ? WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseName());
            pstmt.setString(2, course.getDescription());
            pstmt.setInt(3, course.getCreditHours());
            pstmt.setInt(4, course.getCourseId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course with ID: " + course.getCourseId());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteCourse(int id) {
        String sql = "DELETE FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting course with ID: " + id);
            e.printStackTrace();
        }

        return false;
    }
}
