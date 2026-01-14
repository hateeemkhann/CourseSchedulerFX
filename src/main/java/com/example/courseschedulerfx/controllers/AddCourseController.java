package com.example.courseschedulerfx.controllers;

import com.example.courseschedulerfx.DAO.DepartmentDAO;
import com.example.courseschedulerfx.model.Course;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCourseController {

    @FXML
    private TextField courseTitleField;

    @FXML
    private ComboBox<Integer> creditHoursComboBox;

    @FXML
    private ComboBox<String> deptComboBoxID;

    @FXML
    private Button saveBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private Label messageLabel;

    private CourseManagementController courseManagementController;

    @FXML
    public void initialize() {
        messageLabel.setText("");
        setupCreditHoursCombo();
        setupDepartmentCombo();
    }

    private void setupCreditHoursCombo() {
        ObservableList<Integer> creditHours = FXCollections.observableArrayList(
                1, 2, 3, 4
        );
        creditHoursComboBox.setItems(creditHours);
        creditHoursComboBox.getSelectionModel().selectFirst();
    }

    private void setupDepartmentCombo() {
        // TODO: Replace with actual DAO call to fetch departments from database
        ObservableList<String> departments = FXCollections.observableArrayList(DepartmentDAO.getAllDepartmentNames());
        deptComboBoxID.setItems(departments);
        deptComboBoxID.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (courseTitleField.getText().trim().isEmpty()) {
            messageLabel.setText("Please enter a course title");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (creditHoursComboBox.getSelectionModel().isEmpty()) {
            messageLabel.setText("Please select credit hours");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (deptComboBoxID.getSelectionModel().isEmpty()) {
            messageLabel.setText("Please select a department");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        try {
            String courseTitle = courseTitleField.getText().trim();
            Integer creditHours = creditHoursComboBox.getValue();
            String department = deptComboBoxID.getValue();

            // Save to database
            boolean saved = com.example.courseschedulerfx.DAO.CourseDAO.addCourse(courseTitle, creditHours, department);

            if (saved) {
                messageLabel.setText("Course added successfully!");
                messageLabel.setStyle("-fx-text-fill: #4CAF50;");

                // Clear form
                courseTitleField.clear();
                creditHoursComboBox.getSelectionModel().selectFirst();
                deptComboBoxID.getSelectionModel().selectFirst();

                // Notify the course management controller to refresh
                if (courseManagementController != null) {
                    courseManagementController.refreshData();
                }

                // Close window after successful save
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(1),
                                event -> closeWindow()
                        )
                ).play();
            } else {
                messageLabel.setText("Failed to save course. Please try again.");
                messageLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (Exception e) {
            messageLabel.setText("Error saving course: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    public void setCourseManagementController(CourseManagementController controller) {
        this.courseManagementController = controller;
    }
}

