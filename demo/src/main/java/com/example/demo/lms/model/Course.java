package com.example.demo.lms.model;

public class Course {
    private int courseId;
    private String courseName;
    private String description;
    private int creditHours;

    public Course() {
    }

    public Course(int courseId, String courseName, String description, int creditHours) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.description = description;
        this.creditHours = creditHours;
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    @Override
    public String toString() {
        return courseName;
    }
}
