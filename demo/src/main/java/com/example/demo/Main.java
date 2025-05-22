package com.example.demo;

import com.example.demo.lms.util.DatabaseConnection;
import com.example.demo.lms.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            URL fxmlUrl = getClass().getResource("/com/example/demo/fxml/LoginView.fxml");
            if (fxmlUrl == null) {
                // Try alternative locations
                fxmlUrl = getClass().getResource("/com/example/demo/fxml/LoginView.fxml");

                if (fxmlUrl == null) {
                    System.err.println("Searching for FXML file in classpath...");
                    ClassLoader classLoader = getClass().getClassLoader();
                    URL[] urls = java.net.URLClassLoader.class.cast(classLoader).getURLs();
                    for (URL url : urls) {
                        System.err.println("Classpath entry: " + url.toString());
                    }

                    throw new IOException("Cannot find LoginView.fxml. Make sure it's in the correct location.");
                }
            }

            System.out.println("Loading FXML from: " + fxmlUrl);

            // Load the FXML file
            Parent root = FXMLLoader.load(fxmlUrl);

            // Set up the stage
            primaryStage.setTitle("LMS - Login");
            primaryStage.setScene(new Scene(root, 400, 500));
            primaryStage.setResizable(false);
            primaryStage.show();

            System.out.println("Login screen started successfully!");
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() {
        // Logout user if application is closed
        SessionManager.logout();


        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
