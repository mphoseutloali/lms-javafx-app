package com.example.demo.lms.controller;

import com.example.demo.lms.dao.CourseDAO;
import com.example.demo.lms.dao.EnrollmentDAO;
import com.example.demo.lms.dao.StudentDAO;
import com.example.demo.lms.model.Enrollment;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML
    private Label studentCountLabel;

    @FXML
    private Label courseCountLabel;

    @FXML
    private Label enrollmentCountLabel;

    @FXML
    private TableView<Enrollment> recentEnrollmentsTable;

    @FXML
    private TableColumn<Enrollment, String> studentNameColumn;

    @FXML
    private TableColumn<Enrollment, String> courseNameColumn;

    @FXML
    private TableColumn<Enrollment, String> enrollmentDateColumn;

    @FXML
    private TableColumn<Enrollment, Integer> progressColumn;

    @FXML
    private ProgressBar overallProgressBar;

    @FXML
    private Label overallProgressLabel;

    @FXML
    private ProgressIndicator activeStudentsIndicator;

    @FXML
    private Label activeStudentsLabel;

    private StudentDAO studentDAO;
    private CourseDAO courseDAO;
    private EnrollmentDAO enrollmentDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentDAO = new StudentDAO();
        courseDAO = new CourseDAO();
        enrollmentDAO = new EnrollmentDAO();

        // Initialize table columns
        studentNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        courseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseName()));
        enrollmentDateColumn.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getEnrollmentDate();
            return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        });
        progressColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getProgress()).asObject());

        // Add progress bar to progress column
        progressColumn.setCellFactory(column -> new TableCell<Enrollment, Integer>() {
            private final ProgressBar progressBar = new ProgressBar();

            {
                progressBar.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    progressBar.setProgress(item / 100.0);
                    setGraphic(progressBar);
                    setText(item + "%");
                }
            }
        });

        // Load data
        loadDashboardData();

        // Apply fade transition to progress indicator
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), activeStudentsIndicator);
        fadeTransition.setFromValue(0.3);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
    }

    private void loadDashboardData() {
        // Run database operations in a background thread
        new Thread(() -> {
            try {
                // Get counts
                int studentCount = studentDAO.getAllStudents().size();
                int courseCount = courseDAO.getAllCourses().size();
                List<Enrollment> allEnrollments = enrollmentDAO.getAllEnrollments();
                int enrollmentCount = allEnrollments.size();

                // Calculate overall progress
                double overallProgress = 0;
                if (!allEnrollments.isEmpty()) {
                    overallProgress = allEnrollments.stream()
                            .mapToInt(Enrollment::getProgress)
                            .average()
                            .orElse(0) / 100.0;
                }

                // Calculate active students (students with at least one enrollment)
                List<Integer> uniqueStudentIds = allEnrollments.stream()
                        .map(Enrollment::getStudentId)
                        .distinct()
                        .collect(Collectors.toList());
                double activeStudentsRatio = studentCount > 0 ? (double) uniqueStudentIds.size() / studentCount : 0;

                // Get recent enrollments (last 10)
                ObservableList<Enrollment> recentEnrollments = FXCollections.observableArrayList(
                        allEnrollments.stream()
                                .limit(10)
                                .collect(Collectors.toList())
                );

                // Update UI on JavaFX thread
                final double finalOverallProgress = overallProgress;
                final double finalActiveStudentsRatio = activeStudentsRatio;

                Platform.runLater(() -> {
                    studentCountLabel.setText(String.valueOf(studentCount));
                    courseCountLabel.setText(String.valueOf(courseCount));
                    enrollmentCountLabel.setText(String.valueOf(enrollmentCount));

                    recentEnrollmentsTable.setItems(recentEnrollments);

                    overallProgressBar.setProgress(finalOverallProgress);
                    overallProgressLabel.setText(String.format("%.1f%%", finalOverallProgress * 100));

                    activeStudentsIndicator.setProgress(finalActiveStudentsRatio);
                    activeStudentsLabel.setText(String.format("%.1f%%", finalActiveStudentsRatio * 100));
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Error Loading Data", "Failed to load dashboard data: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}