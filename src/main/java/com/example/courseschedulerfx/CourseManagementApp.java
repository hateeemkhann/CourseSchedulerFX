package com.example.courseschedulerfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Standalone application to run and test the Course Management view
 */
public class CourseManagementApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the course_management.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/course_management.fxml"));
        Parent root = loader.load();

        // Create the scene
        Scene scene = new Scene(root, 800, 600);

        // Load and apply CSS stylesheet if available
        String cssResource = getClass().getResource("/css/course_management.css").toExternalForm();
        scene.getStylesheets().add(cssResource);

        // Also add the main app.css for global styles
        if (getClass().getResource("/css/app.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        }

        // Force layout calculation
        root.applyCss();
        root.layout();

        // Set stage properties
        primaryStage.setTitle("Course Management");
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setResizable(true);

        // Show the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

