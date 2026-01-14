package com.example.courseschedulerfx.controllers;

import com.example.courseschedulerfx.DAO.DepartmentDAO;
import com.example.courseschedulerfx.DAO.TeacherDAO;
import com.example.courseschedulerfx.model.Teacher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddTeacherController {

    @FXML
    private TextField teacherNameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> deptComboBoxID;

    @FXML
    private Button saveBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private Label messageLabel;

    private TeacherManagementController teacherManagementController;

    @FXML
    public void initialize() {
        messageLabel.setText("");
        setupDepartmentCombo();
    }

    private void setupDepartmentCombo() {
        ObservableList<String> departments = FXCollections.observableArrayList(DepartmentDAO.getAllDepartmentNames());
        deptComboBoxID.setItems(departments);
        if (!departments.isEmpty()) {
            deptComboBoxID.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (teacherNameField.getText().trim().isEmpty()) {
            messageLabel.setText("Please enter a teacher name");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (emailField.getText().trim().isEmpty()) {
            messageLabel.setText("Please enter an email address");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            messageLabel.setText("Please enter a valid email address");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (deptComboBoxID.getSelectionModel().isEmpty()) {
            messageLabel.setText("Please select a department");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        try {
            String teacherName = teacherNameField.getText().trim();
            String email = emailField.getText().trim();
            String department = deptComboBoxID.getValue();

            // Save to database
            boolean saved = TeacherDAO.addTeacher(teacherName, email, department);

            if (saved) {
                messageLabel.setText("Teacher added successfully!");
                messageLabel.setStyle("-fx-text-fill: #4CAF50;");

                // Clear form
                teacherNameField.clear();
                emailField.clear();
                deptComboBoxID.getSelectionModel().selectFirst();

                // Notify the teacher management controller to refresh
                if (teacherManagementController != null) {
                    teacherManagementController.refreshData();
                }

                // Close window after successful save
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(1),
                                event -> closeWindow()
                        )
                ).play();
            } else {
                messageLabel.setText("Failed to save teacher. Please try again.");
                messageLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (Exception e) {
            messageLabel.setText("Error saving teacher: " + e.getMessage());
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

    public void setTeacherManagementController(TeacherManagementController controller) {
        this.teacherManagementController = controller;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}

