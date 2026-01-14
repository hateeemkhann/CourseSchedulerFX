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

public class EditTeacherController {

    @FXML
    private TextField teacherIDField;

    @FXML
    private TextField teacherNameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> deptComboBoxID;

    @FXML
    private Button updateBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private Label messageLabel;

    private Teacher teacherToEdit;
    private TeacherManagementController teacherManagementController;

    @FXML
    public void initialize() {
        messageLabel.setText("");
        setupDepartmentCombo();
    }

    private void setupDepartmentCombo() {
        ObservableList<String> departments = FXCollections.observableArrayList(DepartmentDAO.getAllDepartmentNames());
        deptComboBoxID.setItems(departments);
    }

    public void setTeacherToEdit(Teacher teacher) {
        this.teacherToEdit = teacher;
        if (teacher != null) {
            teacherIDField.setText(String.valueOf(teacher.getTeacherID()));
            teacherNameField.setText(teacher.getTeacherName());
            emailField.setText(teacher.getTeacherEmail());

            // Set the department in the combobox
            if (teacher.getDepartment() != null) {
                deptComboBoxID.setValue(teacher.getDepartment().getDepartmentName());
            }
        }
    }

    @FXML
    private void handleUpdate() {
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
            int teacherID = Integer.parseInt(teacherIDField.getText());
            String teacherName = teacherNameField.getText().trim();
            String email = emailField.getText().trim();
            String department = deptComboBoxID.getValue();

            // Update in database
            boolean updated = TeacherDAO.updateTeacher(teacherID, teacherName, email, department);

            if (updated) {
                messageLabel.setText("Teacher updated successfully!");
                messageLabel.setStyle("-fx-text-fill: #4CAF50;");

                // Notify the teacher management controller to refresh
                if (teacherManagementController != null) {
                    teacherManagementController.refreshData();
                }

                // Close window after successful update
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(1),
                                event -> closeWindow()
                        )
                ).play();
            } else {
                messageLabel.setText("Failed to update teacher. Please try again.");
                messageLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (Exception e) {
            messageLabel.setText("Error updating teacher: " + e.getMessage());
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

