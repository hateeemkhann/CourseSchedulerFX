package com.example.courseschedulerfx.controllers;

import com.example.courseschedulerfx.DAO.CourseDAO;
import com.example.courseschedulerfx.DAO.DepartmentDAO;
import com.example.courseschedulerfx.model.Course;
import com.example.courseschedulerfx.model.Department;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditCourseController {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<Integer> creditHoursComboBox;

    @FXML
    private ComboBox<String> departmentComboBox;

    @FXML
    private Button saveBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private Label messageLabel;

    private CourseManagementController courseManagementController;
    private Course courseToEdit;

    @FXML
    public void initialize() {
        messageLabel.setText("");
        setupCreditHoursCombo();
        setupDepartmentCombo();
    }

    private void setupCreditHoursCombo() {
        ObservableList<Integer> creditHours = FXCollections.observableArrayList(
                1, 2, 3, 4, 5, 6
        );
        creditHoursComboBox.setItems(creditHours);
    }

    private void setupDepartmentCombo() {
        ObservableList<String> departments = FXCollections.observableArrayList(
                DepartmentDAO.getAllDepartmentNames()
        );
        departmentComboBox.setItems(departments);
    }

    public void setCourseToEdit(Course course) {
        this.courseToEdit = course;
        if (course != null) {
            titleField.setText(course.getCourseTitle());
            creditHoursComboBox.getSelectionModel().select((Integer) course.getCreditHours());

            // Handle department selection
            if (course.getDepartment() != null) {
                Department dept = course.getDepartment();
                departmentComboBox.getSelectionModel().select(dept.getDepartmentName());
            }
        }
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (titleField.getText().trim().isEmpty()) {
            messageLabel.setText("Please enter a course title");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (creditHoursComboBox.getSelectionModel().isEmpty()) {
            messageLabel.setText("Please select credit hours");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (departmentComboBox.getSelectionModel().isEmpty()) {
            messageLabel.setText("Please select a department");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        try {
            String courseTitle = titleField.getText().trim();
            Integer creditHours = creditHoursComboBox.getValue();
            String department = departmentComboBox.getValue();
            this.courseToEdit.setCourseTitle(courseTitle);
            this.courseToEdit.setCreditHours(creditHours);
            this.courseToEdit.setDepartment(DepartmentDAO.getDepartmentById(DepartmentDAO.getDepartmentIdByName(department)));

            if(CourseDAO.editCourse(courseToEdit)) {
                messageLabel.setText("Course updated successfully!");
                messageLabel.setStyle("-fx-text-fill: #4CAF50;");

                // Close window after successful save
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(1),
                                event -> closeWindow()
                        )
                ).play();
            }
            else{
                messageLabel.setText("Failed to update course. Please try again.");
                messageLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (Exception e) {
            messageLabel.setText("Error updating course: " + e.getMessage());
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

