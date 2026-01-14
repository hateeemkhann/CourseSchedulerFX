package com.example.courseschedulerfx;

import com.example.courseschedulerfx.utils.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AddCourseTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_course.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Load CSS stylesheets
        if (getClass().getResource("/css/course_form.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/css/course_form.css").toExternalForm());
        }

        // Also add the main app.css for global styles
        if (getClass().getResource("/css/app.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        }

        primaryStage.setTitle("Add Course");
        primaryStage.setScene(scene);

        // Apply normal window behavior (not maximized, sized to fit form 520x520)
        WindowManager.applyNormalWindow(primaryStage, 560, 600);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
