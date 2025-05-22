package com.example.demo.lms.controller;

import com.example.demo.lms.dao.*;
import com.example.demo.lms.model.*;
import com.example.demo.lms.util.CsvExporter;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EnrollmentsController implements Initializable {

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private TableView<Enrollment> enrollmentsTable;

    @FXML
    private TableColumn<Enrollment, Integer> enrollmentIdColumn;

    @FXML
    private TableColumn<Enrollment, String> studentNameColumn;

    @FXML
    private TableColumn<Enrollment, String> courseNameColumn;

    @FXML
    private TableColumn<Enrollment, String> enrollmentDateColumn;

    @FXML
    private TableColumn<Enrollment, Integer> progressColumn;

    @FXML
    private TableColumn<Enrollment, Void> actionsColumn;

    @FXML
    private Pagination enrollmentsPagination;

    @FXML
    private Button exportButton;

    private EnrollmentDAO enrollmentDAO;
    private StudentDAO studentDAO;
    private CourseDAO courseDAO;

    private ObservableList<Enrollment> allEnrollments;
    private ObservableList<Enrollment> filteredEnrollments;

    private final int ROWS_PER_PAGE = 10;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing EnrollmentsController...");

        try {
            enrollmentDAO = new EnrollmentDAO();
            studentDAO = new StudentDAO();
            courseDAO = new CourseDAO();

            // Initialize filter combo box
            filterComboBox.setItems(FXCollections.observableArrayList(
                    "All Enrollments", "In Progress", "Completed"
            ));
            filterComboBox.getSelectionModel().selectFirst();
            filterComboBox.setOnAction(e -> filterEnrollments());

            // Initialize table columns
            enrollmentIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getEnrollmentId()).asObject());
            studentNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
            courseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseName()));
            enrollmentDateColumn.setCellValueFactory(data -> {
                LocalDate date = data.getValue().getEnrollmentDate();
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            });

            // Add progress bar to progress column
            progressColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getProgress()).asObject());
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

            // Add action buttons to the table
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox pane = new HBox(5, editButton, deleteButton);

                {
                    deleteButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
                    pane.setPadding(new Insets(5));

                    editButton.setOnAction(event -> {
                        Enrollment enrollment = getTableView().getItems().get(getIndex());
                        showEditProgressDialog(enrollment);
                    });

                    deleteButton.setOnAction(event -> {
                        Enrollment enrollment = getTableView().getItems().get(getIndex());
                        confirmAndDeleteEnrollment(enrollment);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            });

            // Initialize pagination
            enrollmentsPagination.setPageFactory(this::createPage);

            // Set up export button action
            exportButton.setOnAction(event -> exportToCSV());

            // Load enrollments
            loadEnrollments();

            System.out.println("EnrollmentsController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing EnrollmentsController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEnrollments() {
        System.out.println("Loading enrollments...");

        // Run database operations in a background thread
        new Thread(() -> {
            try {
                List<Enrollment> enrollments = enrollmentDAO.getAllEnrollments();
                System.out.println("Loaded " + enrollments.size() + " enrollments from database");

                allEnrollments = FXCollections.observableArrayList(enrollments);
                filteredEnrollments = FXCollections.observableArrayList(enrollments);

                Platform.runLater(() -> {
                    updatePagination();
                    System.out.println("Enrollments loaded and displayed successfully");
                });

            } catch (Exception e) {
                System.err.println("Error loading enrollments: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Error Loading Data", "Failed to load enrollments: " + e.getMessage());
                });
            }
        }).start();
    }

    private void filterEnrollments() {
        String filter = filterComboBox.getValue();

        if (filter.equals("All Enrollments")) {
            filteredEnrollments = FXCollections.observableArrayList(allEnrollments);
        } else if (filter.equals("In Progress")) {
            filteredEnrollments = FXCollections.observableArrayList(
                    allEnrollments.stream()
                            .filter(enrollment -> enrollment.getProgress() < 100)
                            .toList()
            );
        } else if (filter.equals("Completed")) {
            filteredEnrollments = FXCollections.observableArrayList(
                    allEnrollments.stream()
                            .filter(enrollment -> enrollment.getProgress() == 100)
                            .toList()
            );
        }

        updatePagination();
    }

    private void updatePagination() {
        int totalPages = (filteredEnrollments.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
        enrollmentsPagination.setPageCount(Math.max(1, totalPages));
        enrollmentsPagination.setCurrentPageIndex(0);
        enrollmentsTable.setItems((ObservableList<Enrollment>) createPage(0));

    }

    // Fixed method: Now returns a Node (the TableView) instead of an ObservableList
    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredEnrollments.size());

        if (fromIndex >= filteredEnrollments.size()) {
            enrollmentsTable.setItems(FXCollections.observableArrayList());
        } else {
            enrollmentsTable.setItems(FXCollections.observableArrayList(
                    filteredEnrollments.subList(fromIndex, toIndex)
            ));
        }

        return enrollmentsTable;
    }

    @FXML
    private void exportToCSV() {
        if (filteredEnrollments == null || filteredEnrollments.isEmpty()) {
            showErrorAlert("Export Error", "No data to export.");
            return;
        }

        Stage stage = (Stage) exportButton.getScene().getWindow();
        boolean success = CsvExporter.exportEnrollmentsToCSV(filteredEnrollments, stage);

        if (success) {
            showInfoAlert("Export Successful", "Progress data has been exported to CSV successfully.");
        } else {
            showErrorAlert("Export Error", "Failed to export progress data to CSV.");
        }
    }

    @FXML
    private void showAddEnrollmentDialog() {
        // Run database operations in a background thread to get students and courses
        new Thread(() -> {
            try {
                List<Student> students = studentDAO.getAllStudents();
                List<Course> courses = courseDAO.getAllCourses();

                Platform.runLater(() -> {
                    if (students.isEmpty() || courses.isEmpty()) {
                        showErrorAlert("Error", "No students or courses available. Please add some first.");
                        return;
                    }

                    Dialog<Enrollment> dialog = new Dialog<>();
                    dialog.setTitle("Add New Enrollment");
                    dialog.setHeaderText("Enroll a student in a course");

                    // Set the button types
                    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                    // Create the form fields
                    ComboBox<Student> studentComboBox = new ComboBox<>(FXCollections.observableArrayList(students));
                    studentComboBox.setPromptText("Select Student");
                    studentComboBox.setCellFactory(param -> new ListCell<Student>() {
                        @Override
                        protected void updateItem(Student item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty ? "" : item.getFullName());
                        }
                    });
                    studentComboBox.setButtonCell(new ListCell<Student>() {
                        @Override
                        protected void updateItem(Student item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty ? "" : item.getFullName());
                        }
                    });

                    ComboBox<Course> courseComboBox = new ComboBox<>(FXCollections.observableArrayList(courses));
                    courseComboBox.setPromptText("Select Course");
                    courseComboBox.setCellFactory(param -> new ListCell<Course>() {
                        @Override
                        protected void updateItem(Course item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty ? "" : item.getCourseName());
                        }
                    });
                    courseComboBox.setButtonCell(new ListCell<Course>() {
                        @Override
                        protected void updateItem(Course item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty ? "" : item.getCourseName());
                        }
                    });

                    DatePicker enrollmentDatePicker = new DatePicker(LocalDate.now());

                    Slider progressSlider = new Slider(0, 100, 0);
                    progressSlider.setShowTickLabels(true);
                    progressSlider.setShowTickMarks(true);
                    progressSlider.setMajorTickUnit(25);
                    progressSlider.setBlockIncrement(5);

                    Label progressLabel = new Label("0%");
                    progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                        progressLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
                    });

                    // Create layout
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20));

                    grid.add(new Label("Student:"), 0, 0);
                    grid.add(studentComboBox, 1, 0);
                    grid.add(new Label("Course:"), 0, 1);
                    grid.add(courseComboBox, 1, 1);
                    grid.add(new Label("Enrollment Date:"), 0, 2);
                    grid.add(enrollmentDatePicker, 1, 2);
                    grid.add(new Label("Initial Progress:"), 0, 3);

                    HBox progressBox = new HBox(10, progressSlider, progressLabel);
                    grid.add(progressBox, 1, 3);

                    dialog.getDialogPane().setContent(grid);

                    // Convert the result when the save button is clicked
                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == saveButtonType) {
                            Student selectedStudent = studentComboBox.getValue();
                            Course selectedCourse = courseComboBox.getValue();

                            if (selectedStudent == null || selectedCourse == null) {
                                showErrorAlert("Error", "Please select both a student and a course.");
                                return null;
                            }

                            Enrollment enrollment = new Enrollment();
                            enrollment.setStudentId(selectedStudent.getStudentId());
                            enrollment.setCourseId(selectedCourse.getCourseId());
                            enrollment.setEnrollmentDate(enrollmentDatePicker.getValue());
                            enrollment.setProgress((int) progressSlider.getValue());

                            // Set display names for UI
                            enrollment.setStudentName(selectedStudent.getFullName());
                            enrollment.setCourseName(selectedCourse.getCourseName());

                            return enrollment;
                        }
                        return null;
                    });

                    Optional<Enrollment> result = dialog.showAndWait();

                    result.ifPresent(enrollment -> {
                        // Save the new enrollment to the database
                        new Thread(() -> {
                            try {
                                boolean success = enrollmentDAO.addEnrollment(enrollment);

                                if (success) {
                                    Platform.runLater(() -> {
                                        // Reload enrollments
                                        loadEnrollments();
                                        showInfoAlert("Success", "Enrollment added successfully.");
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        showErrorAlert("Error", "Failed to add enrollment.");
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Platform.runLater(() -> {
                                    showErrorAlert("Error", "Failed to add enrollment: " + e.getMessage());
                                });
                            }
                        }).start();
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Error", "Failed to load students or courses: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showEditProgressDialog(Enrollment enrollment) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Update Progress");
        dialog.setHeaderText("Update progress for " + enrollment.getStudentName() + " in " + enrollment.getCourseName());

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        Slider progressSlider = new Slider(0, 100, enrollment.getProgress());
        progressSlider.setShowTickLabels(true);
        progressSlider.setShowTickMarks(true);
        progressSlider.setMajorTickUnit(25);
        progressSlider.setBlockIncrement(5);

        Label progressLabel = new Label(enrollment.getProgress() + "%");
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            progressLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
        });

        // Create layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Progress:"),
                new HBox(10, progressSlider, progressLabel)
        );

        dialog.getDialogPane().setContent(content);

        // Convert the result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return (int) progressSlider.getValue();
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();

        result.ifPresent(progress -> {
            // Update the enrollment progress in the database
            new Thread(() -> {
                try {
                    boolean success = enrollmentDAO.updateEnrollmentProgress(enrollment.getEnrollmentId(), progress);

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload enrollments
                            loadEnrollments();
                            showInfoAlert("Success", "Progress updated successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to update progress.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to update progress: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private void confirmAndDeleteEnrollment(Enrollment enrollment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Enrollment");
        alert.setContentText("Are you sure you want to delete the enrollment of " +
                enrollment.getStudentName() + " in " + enrollment.getCourseName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the enrollment from the database
            new Thread(() -> {
                try {
                    boolean success = enrollmentDAO.deleteEnrollment(enrollment.getEnrollmentId());

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload enrollments
                            loadEnrollments();
                            showInfoAlert("Success", "Enrollment deleted successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to delete enrollment.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to delete enrollment: " + e.getMessage());
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
