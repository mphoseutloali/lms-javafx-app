package com.example.demo.lms.util;

import com.example.demo.lms.model.Enrollment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting data to CSV files
 */
public class CsvExporter {

    /**
     * Exports enrollment progress data to a CSV file
     *
     * @param enrollments List of enrollments to export
     * @param stage The current stage (for file dialog)
     * @return true if export was successful, false otherwise
     */
    public static boolean exportEnrollmentsToCSV(List<Enrollment> enrollments, Stage stage) {
        if (enrollments == null || enrollments.isEmpty()) {
            return false;
        }

        // Configure file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Progress Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("progress_report.csv");

        // Show save dialog
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return false; // User cancelled the operation
        }

        try (FileWriter writer = new FileWriter(file)) {
            // Write CSV header
            writer.write("Enrollment ID,Student ID,Student Name,Course ID,Course Name,Enrollment Date,Progress\n");

            // Write enrollment data
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            for (Enrollment enrollment : enrollments) {
                writer.write(String.format("%d,%d,\"%s\",%d,\"%s\",%s,%d%%\n",
                        enrollment.getEnrollmentId(),
                        enrollment.getStudentId(),
                        enrollment.getStudentName(),
                        enrollment.getCourseId(),
                        enrollment.getCourseName(),
                        enrollment.getEnrollmentDate().format(dateFormatter),
                        enrollment.getProgress()));
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
