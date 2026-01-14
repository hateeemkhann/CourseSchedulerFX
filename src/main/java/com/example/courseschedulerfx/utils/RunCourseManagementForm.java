package com.example.courseschedulerfx.utils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Standalone application to run and test the Course Management view
 */
public class RunCourseManagementForm extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the course_management.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/course_management.fxml"));
        Parent root = loader.load();

        // Create the scene with dynamic sizing
        Scene scene = new Scene(root);

        // Load and apply CSS stylesheet if available
        if (getClass().getResource("/css/course_management.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/css/course_management.css").toExternalForm());
        }

        // Also add the main app.css for global styles
        if (getClass().getResource("/css/app.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        }

        // Force layout calculation
        root.applyCss();
        root.layout();

        // Set stage properties for full-screen display
        primaryStage.setTitle("Course Management");
        primaryStage.setScene(scene);

        // Apply default window (maximized, full-screen)
        WindowManager.applyDefaultWindow(primaryStage);

        // Show the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

