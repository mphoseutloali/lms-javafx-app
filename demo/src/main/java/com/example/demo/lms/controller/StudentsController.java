package com.example.demo.lms.controller;

import com.example.demo.lms.dao.StudentDAO;
import com.example.demo.lms.model.Student;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentsController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private VBox studentsContainer;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    private StudentDAO studentDAO;
    private ObservableList<Student> allStudents;
    private ObservableList<Student> filteredStudents;

    private int currentPage = 0;
    private final int ITEMS_PER_PAGE = 10;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentDAO = new StudentDAO();

        // Initialize search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStudents(newValue);
            displayCurrentPage();
        });

        // Load students
        loadStudents();
    }

    private void loadStudents() {
        // Run database operations in a background thread
        new Thread(() -> {
            try {
                List<Student> students = studentDAO.getAllStudents();
                allStudents = FXCollections.observableArrayList(students);
                filteredStudents = FXCollections.observableArrayList(students);

                Platform.runLater(() -> {
                    displayCurrentPage();
                    updatePaginationControls();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Error Loading Data", "Failed to load students: " + e.getMessage());
                });
            }
        }).start();
    }

    private void filterStudents(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredStudents = FXCollections.observableArrayList(allStudents);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredStudents = FXCollections.observableArrayList(
                    allStudents.stream()
                            .filter(student ->
                                    student.getFirstName().toLowerCase().contains(lowerCaseSearch) ||
                                            student.getLastName().toLowerCase().contains(lowerCaseSearch) ||
                                            student.getEmail().toLowerCase().contains(lowerCaseSearch))
                            .collect(Collectors.toList())
            );
        }

        currentPage = 0;
        updatePaginationControls();
    }

    private void displayCurrentPage() {
        studentsContainer.getChildren().clear();

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredStudents.size());

        for (int i = startIndex; i < endIndex; i++) {
            Student student = filteredStudents.get(i);
            studentsContainer.getChildren().add(createStudentListItem(student));
        }
    }

    private HBox createStudentListItem(Student student) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));
        item.setSpacing(10);
        item.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label nameLabel = new Label(student.getFullName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label emailLabel = new Label(student.getEmail());
        emailLabel.setStyle("-fx-text-fill: #666;");

        Label dateLabel = new Label("Enrolled: " + student.getEnrollmentDate().toString());
        dateLabel.setStyle("-fx-text-fill: #666;");

        VBox infoBox = new VBox(5, nameLabel, emailLabel, dateLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> showEditStudentDialog(student));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> confirmAndDeleteStudent(student));

        item.getChildren().addAll(infoBox, editButton, deleteButton);

        return item;
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) filteredStudents.size() / ITEMS_PER_PAGE);

        pageInfoLabel.setText(String.format("Page %d of %d", currentPage + 1, Math.max(1, totalPages)));

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1 || totalPages == 0);
    }

    @FXML
    private void previousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayCurrentPage();
            updatePaginationControls();
        }
    }

    @FXML
    private void nextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) filteredStudents.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            displayCurrentPage();
            updatePaginationControls();
        }
    }

    @FXML
    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        DatePicker enrollmentDatePicker = new DatePicker(LocalDate.now());

        // Create layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("First Name:"), firstNameField,
                new Label("Last Name:"), lastNameField,
                new Label("Email:"), emailField,
                new Label("Enrollment Date:"), enrollmentDatePicker
        );

        dialog.getDialogPane().setContent(content);

        // Request focus on the first field
        Platform.runLater(firstNameField::requestFocus);

        // Convert the result to a student when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Student student = new Student();
                student.setFirstName(firstNameField.getText());
                student.setLastName(lastNameField.getText());
                student.setEmail(emailField.getText());
                student.setEnrollmentDate(enrollmentDatePicker.getValue());
                return student;
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();

        result.ifPresent(student -> {
            // Save the new student to the database
            new Thread(() -> {
                try {
                    boolean success = studentDAO.addStudent(student);

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload students
                            loadStudents();
                            showInfoAlert("Success", "Student added successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to add student.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to add student: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private void showEditStudentDialog(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Edit student details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField firstNameField = new TextField(student.getFirstName());
        TextField lastNameField = new TextField(student.getLastName());
        TextField emailField = new TextField(student.getEmail());
        DatePicker enrollmentDatePicker = new DatePicker(student.getEnrollmentDate());

        // Create layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("First Name:"), firstNameField,
                new Label("Last Name:"), lastNameField,
                new Label("Email:"), emailField,
                new Label("Enrollment Date:"), enrollmentDatePicker
        );

        dialog.getDialogPane().setContent(content);

        // Request focus on the first field
        Platform.runLater(firstNameField::requestFocus);

        // Convert the result to a student when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                student.setFirstName(firstNameField.getText());
                student.setLastName(lastNameField.getText());
                student.setEmail(emailField.getText());
                student.setEnrollmentDate(enrollmentDatePicker.getValue());
                return student;
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();

        result.ifPresent(updatedStudent -> {
            // Update the student in the database
            new Thread(() -> {
                try {
                    boolean success = studentDAO.updateStudent(updatedStudent);

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload students
                            loadStudents();
                            showInfoAlert("Success", "Student updated successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to update student.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to update student: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private void confirmAndDeleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Student");
        alert.setContentText("Are you sure you want to delete " + student.getFullName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the student from the database
            new Thread(() -> {
                try {
                    boolean success = studentDAO.deleteStudent(student.getStudentId());

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload students
                            loadStudents();
                            showInfoAlert("Success", "Student deleted successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to delete student.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to delete student: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}