package com.example.demo.lms.controller;

import com.example.demo.lms.util.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button dashboardButton;

    @FXML
    private Label adminNameLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Check if user is logged in
        if (!SessionManager.isLoggedIn()) {
            showLoginScreen();
            return;
        }

        // Display admin name
        if (SessionManager.getCurrentAdmin() != null) {
            adminNameLabel.setText("Welcome, " + SessionManager.getCurrentAdmin().getFullName());
        }

        // Apply drop shadow effect to dashboard button
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5.0);
        shadow.setOffsetX(3.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.color(0.4, 0.4, 0.4, 0.5));
        dashboardButton.setEffect(shadow);

        // Load dashboard view by default
        showDashboard(null);
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        System.out.println("Loading Dashboard view...");
        loadView("/com/example/demo/fxml/DashboardView.fxml");
    }

    @FXML
    private void showStudents(ActionEvent event) {
        System.out.println("Loading Students view...");
        loadView("/com/example/demo/fxml/StudentsView.fxml");
    }

    @FXML
    private void showCourses(ActionEvent event) {
        System.out.println("Loading Courses view...");
        loadView("/com/example/demo/fxml/CourseView.fxml");
    }

    @FXML
    private void showEnrollments(ActionEvent event) {
        System.out.println("Loading Enrollments view...");
        loadView("/com/example/demo/fxml/EnrollmentView.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        // Log out the user
        SessionManager.logout();

        // Show login screen
        showLoginScreen();
    }

    @FXML
    private void exitApplication(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void showAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Learning Management System");
        alert.setContentText("Version 1.0\nDeveloped as a JavaFX project with PostgreSQL integration.");
        alert.showAndWait();
    }

    private void loadView(String fxmlPath) {
        try {
            System.out.println("Attempting to load: " + fxmlPath);

            // Try to find the resource
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                // Try alternative locations
                System.out.println("Resource not found at " + fxmlPath + ", trying alternative locations...");
                fxmlUrl = getClass().getResource("/com/example/demo" + fxmlPath);

                if (fxmlUrl == null) {
                    fxmlUrl = getClass().getClassLoader().getResource("fxml" + fxmlPath.substring(5));

                    if (fxmlUrl == null) {
                        throw new IOException("Cannot find resource: " + fxmlPath);
                    }
                }
            }

            System.out.println("Found resource at: " + fxmlUrl);

            // Load the view
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            // Clear the content area and add the new view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println("Successfully loaded view: " + fxmlPath);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error loading view", "Could not load the requested view: " + fxmlPath + "\nError: " + e.getMessage());
        }
    }

    private void showLoginScreen() {
        try {
            URL loginViewUrl = getClass().getResource("/com/example/demo/fxml/LoginView.fxml");
            if (loginViewUrl == null) {
                throw new IOException("Cannot find LoginView.fxml");
            }

            Parent root = FXMLLoader.load(loginViewUrl);

            // Get current stage
            Stage stage = (Stage) contentArea.getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(root, 400, 500);
            stage.setScene(scene);
            stage.setTitle("LMS - Login");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not load login screen: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
