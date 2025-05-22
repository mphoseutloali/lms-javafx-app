package com.example.demo.lms.controller;

import com.example.demo.lms.dao.CourseDAO;
import com.example.demo.lms.model.Course;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CoursesController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Course> coursesTable;

    @FXML
    private TableColumn<Course, Integer> courseIdColumn;

    @FXML
    private TableColumn<Course, String> courseNameColumn;

    @FXML
    private TableColumn<Course, String> descriptionColumn;

    @FXML
    private TableColumn<Course, Integer> creditHoursColumn;

    @FXML
    private TableColumn<Course, Void> actionsColumn;

    @FXML
    private Pagination coursesPagination;

    private CourseDAO courseDAO;
    private ObservableList<Course> allCourses;
    private ObservableList<Course> filteredCourses;

    private final int ROWS_PER_PAGE = 10;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing CoursesController...");

        try {
            courseDAO = new CourseDAO();

            // Initialize table columns
            courseIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCourseId()).asObject());
            courseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseName()));
            descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
            creditHoursColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCreditHours()).asObject());

            // Add action buttons to the table
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox pane = new HBox(5, editButton, deleteButton);

                {
                    deleteButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
                    pane.setPadding(new Insets(5));

                    editButton.setOnAction(event -> {
                        Course course = getTableView().getItems().get(getIndex());
                        showEditCourseDialog(course);
                    });

                    deleteButton.setOnAction(event -> {
                        Course course = getTableView().getItems().get(getIndex());
                        confirmAndDeleteCourse(course);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            });

            // Initialize search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCourses(newValue);
            });

            // Initialize pagination
            coursesPagination.setPageFactory(this::createPage);

            // Load courses
            loadCourses();

            System.out.println("CoursesController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing CoursesController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCourses() {
        System.out.println("Loading courses...");

        // Run database operations in a background thread
        new Thread(() -> {
            try {
                List<Course> courses = courseDAO.getAllCourses();
                System.out.println("Loaded " + courses.size() + " courses from database");

                allCourses = FXCollections.observableArrayList(courses);
                filteredCourses = FXCollections.observableArrayList(courses);

                Platform.runLater(() -> {
                    updatePagination();
                    System.out.println("Courses loaded and displayed successfully");
                });

            } catch (Exception e) {
                System.err.println("Error loading courses: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Error Loading Data", "Failed to load courses: " + e.getMessage());
                });
            }
        }).start();
    }

    private void filterCourses(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredCourses = FXCollections.observableArrayList(allCourses);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredCourses = FXCollections.observableArrayList(
                    allCourses.stream()
                            .filter(course ->
                                    course.getCourseName().toLowerCase().contains(lowerCaseSearch) ||
                                            (course.getDescription() != null &&
                                                    course.getDescription().toLowerCase().contains(lowerCaseSearch)))
                            .toList()
            );
        }

        updatePagination();
    }

    private void updatePagination() {
        int totalPages = (filteredCourses.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
        coursesPagination.setPageCount(Math.max(1, totalPages));
        coursesPagination.setCurrentPageIndex(0);
        coursesTable.setItems((ObservableList<Course>) createPage(0));
    }

    // Fixed method: Now returns a Node (the TableView) instead of an ObservableList
    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredCourses.size());

        if (fromIndex >= filteredCourses.size()) {
            coursesTable.setItems(FXCollections.observableArrayList());
        } else {
            coursesTable.setItems(FXCollections.observableArrayList(
                    filteredCourses.subList(fromIndex, toIndex)
            ));
        }

        return coursesTable;
    }

    @FXML
    private void showAddCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.setHeaderText("Enter course details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Course Name");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        Spinner<Integer> creditHoursSpinner = new Spinner<>(1, 10, 3);
        creditHoursSpinner.setEditable(true);

        // Create layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Course Name:"), courseNameField,
                new Label("Description:"), descriptionArea,
                new Label("Credit Hours:"), creditHoursSpinner
        );

        dialog.getDialogPane().setContent(content);

        // Request focus on the first field
        Platform.runLater(courseNameField::requestFocus);

        // Convert the result to a course when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Course course = new Course();
                course.setCourseName(courseNameField.getText());
                course.setDescription(descriptionArea.getText());
                course.setCreditHours(creditHoursSpinner.getValue());
                return course;
            }
            return null;
        });

        Optional<Course> result = dialog.showAndWait();

        result.ifPresent(course -> {
            // Save the new course to the database
            new Thread(() -> {
                try {
                    boolean success = courseDAO.addCourse(course);

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload courses
                            loadCourses();
                            showInfoAlert("Success", "Course added successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to add course.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to add course: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private void showEditCourseDialog(Course course) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit course details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField courseNameField = new TextField(course.getCourseName());

        TextArea descriptionArea = new TextArea(course.getDescription());
        descriptionArea.setPrefRowCount(3);

        Spinner<Integer> creditHoursSpinner = new Spinner<>(1, 10, course.getCreditHours());
        creditHoursSpinner.setEditable(true);

        // Create layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Course Name:"), courseNameField,
                new Label("Description:"), descriptionArea,
                new Label("Credit Hours:"), creditHoursSpinner
        );

        dialog.getDialogPane().setContent(content);

        // Request focus on the first field
        Platform.runLater(courseNameField::requestFocus);

        // Convert the result to a course when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                course.setCourseName(courseNameField.getText());
                course.setDescription(descriptionArea.getText());
                course.setCreditHours(creditHoursSpinner.getValue());
                return course;
            }
            return null;
        });

        Optional<Course> result = dialog.showAndWait();

        result.ifPresent(updatedCourse -> {
            // Update the course in the database
            new Thread(() -> {
                try {
                    boolean success = courseDAO.updateCourse(updatedCourse);

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload courses
                            loadCourses();
                            showInfoAlert("Success", "Course updated successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to update course.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to update course: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private void confirmAndDeleteCourse(Course course) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Course");
        alert.setContentText("Are you sure you want to delete " + course.getCourseName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the course from the database
            new Thread(() -> {
                try {
                    boolean success = courseDAO.deleteCourse(course.getCourseId());

                    if (success) {
                        Platform.runLater(() -> {
                            // Reload courses
                            loadCourses();
                            showInfoAlert("Success", "Course deleted successfully.");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorAlert("Error", "Failed to delete course.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Error", "Failed to delete course: " + e.getMessage());
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
