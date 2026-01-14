package com.example.courseschedulerfx.controllers;

import com.example.courseschedulerfx.DAO.CourseDAO;
import com.example.courseschedulerfx.DAO.TeacherDAO;
import com.example.courseschedulerfx.model.Admin;
import com.example.courseschedulerfx.model.Course;
import com.example.courseschedulerfx.model.Teacher;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.traversal.TreeWalker;

import java.net.URL;
import java.util.AbstractQueue;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TeacherManagementController implements Initializable {
    // Search and Button Controls
    @FXML
    private TextField searchField;

    @FXML
    private Button addTeacherBtn;

    // Table and Columns
    @FXML
    private TableView<Teacher> teacherTable;

    @FXML
    private TableColumn<Teacher, Integer> teacherIDColumn;

    @FXML
    private TableColumn<Teacher, String> teacherNameColumn;

    @FXML
    private TableColumn<Teacher,String> teacherEmailColumn;

    @FXML
    private TableColumn<Teacher, Void> actionColumn;

    @FXML
    private TableColumn<Teacher,String> departmentColumn;
    // Loading Overlay
    @FXML
    private VBox loadingOverlay;

    @FXML
    private ProgressIndicator loadingIndicator;

    // Status and Count Labels
    @FXML
    private Label statusLabel;

    @FXML
    private Label countLabel;

    private ObservableList<Teacher> teacherList = FXCollections.observableArrayList();
    private ObservableList<Teacher> filteredTeacherList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set column resize policy to make table full-width responsive
        if (teacherTable != null) {
            teacherTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Initialize UI state
        hideLoading();
        if (countLabel != null) {
            countLabel.setText("Total: 0 teachers");
        }
        if (statusLabel != null) {
            statusLabel.setText("Ready");
        }
        setupTableColumns();
        // Add table selection listener
        if (teacherTable != null) {
            teacherTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    statusLabel.setText("Selected teacher");
                } else {
                    statusLabel.setText("Ready");
                }
            });
        }

        // Setup search functionality - auto-filter as user types
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue);
            });
        }

        // Load courses from database
        loadTeacherDataAsync();
    }

    private void setupTableColumns() {
        // ID Column
        teacherIDColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTeacherID()).asObject());

        // Name Column
        teacherNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTeacherName()));

        // Email Column
        teacherEmailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTeacherEmail()));

        // Department Column
        departmentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDepartment().getDepartmentName()));

        // Actions Column with Edit and Delete buttons
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("edit-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                container.setAlignment(Pos.CENTER);

                editBtn.setOnAction(event -> {
                    Teacher teacher = teacherTable.getItems().get(getIndex());
                    handleEditTeacher(teacher);
                });

                deleteBtn.setOnAction(event -> {
                    Teacher teacher = teacherTable.getItems().get(getIndex());
                    handleDeleteTeacher(teacher);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void handleDeleteTeacher(Teacher teacher) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Teacher");
        confirmAlert.setContentText("Are you sure you want to delete teacher '" + teacher.getTeacherName() + "'?\nThis action cannot be undone.");

        // Apply CSS styling to confirmation dialog
        var appCss = getClass().getResource("/css/app.css");
        var teacherFormCss = getClass().getResource("/css/teacher_form.css");
        if (confirmAlert.getDialogPane() != null) {
            if (appCss != null) {
                confirmAlert.getDialogPane().getStylesheets().add(appCss.toExternalForm());
            }
            if (teacherFormCss != null) {
                confirmAlert.getDialogPane().getStylesheets().add(teacherFormCss.toExternalForm());
            }
        }

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = TeacherDAO.deleteTeacher(teacher.getTeacherID());
                if (deleted) {
                    statusLabel.setText("Teacher deleted successfully");
                    refreshData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete teacher.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error deleting teacher: " + e.getMessage());
            }
        }
    }

    public void refreshData() {
        loadTeacherDataAsync();
    }

    private void loadTeacherDataAsync() {
        statusLabel.setText("Loading...");
        long startTime = System.currentTimeMillis();

        // Show loading overlay using Platform.runLater to ensure it's shown
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(true);
                loadingOverlay.setManaged(true);
            }
        });

        Task<List<Teacher>> task = new Task<>() {
            @Override
            protected List<Teacher> call() {
                return TeacherDAO.getAllTeachers();
            }
        };
        task.setOnSucceeded(event -> {
            List<Teacher> teachers = task.getValue();

            // Update UI immediately without artificial delays
            Platform.runLater(() -> {
                teacherList.clear();
                if (teachers != null) {
                    teacherList.addAll(teachers);
                }
                filteredTeacherList.setAll(teacherList);
                teacherTable.setItems(filteredTeacherList);
                updateCount();
                statusLabel.setText("Ready");
                // Hide loading overlay
                if (loadingOverlay != null) {
                    loadingOverlay.setVisible(false);
                    loadingOverlay.setManaged(false);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                statusLabel.setText("Error loading data");
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load teacher data: " + task.getException().getMessage());
                // Hide loading overlay
                if (loadingOverlay != null) {
                    loadingOverlay.setVisible(false);
                    loadingOverlay.setManaged(false);
                }
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateCount() {
        countLabel.setText("Total: " + filteredTeacherList.size() + " teacher(s)");
    }

    /**
     * Filter data based on search query - called instantly as user types
     */
    private void filterData(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Show all courses
            filteredTeacherList.setAll(teacherList);
            statusLabel.setText("Ready");
        } else {
            // Filter courses by title (case-insensitive)
            filteredTeacherList.clear();
            for (Teacher teacher : teacherList) {
                if (teacher.getTeacherName().toLowerCase().contains(query.toLowerCase()) ||
                        String.valueOf(teacher.getTeacherID()).contains(query.toLowerCase())) {
                    filteredTeacherList.add(teacher);
                }
            }
            statusLabel.setText("Found " + filteredTeacherList.size() + " teacher(s)");
        }
        teacherTable.setItems(filteredTeacherList);
        updateCount();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        statusLabel.setText("Refreshing teachers...");

        // Show loading overlay
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(true);
                loadingOverlay.setManaged(true);
            }
        });

        // Background task to refresh courses
        Task<List<Teacher>> refreshTask = new Task<List<Teacher>>() {
            @Override
            protected List<Teacher> call() throws Exception {
                // Fetch fresh data from database
                return TeacherDAO.getAllTeachers();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Teacher> refreshedTeachers = getValue();
                    if (refreshedTeachers != null) {
                        teacherList.clear();
                        teacherList.addAll(refreshedTeachers);
                        filteredTeacherList.setAll(teacherList);
                        teacherTable.setItems(filteredTeacherList);
                        updateCount();
                        statusLabel.setText("Teachers refreshed - " + filteredTeacherList.size() + " teacher(s) loaded");
                    } else {
                        statusLabel.setText("No teachers found");
                    }
                    // Hide loading overlay
                    if (loadingOverlay != null) {
                        loadingOverlay.setVisible(false);
                        loadingOverlay.setManaged(false);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusLabel.setText("Refresh failed: " + getException().getMessage());
                    // Hide loading overlay
                    if (loadingOverlay != null) {
                        loadingOverlay.setVisible(false);
                        loadingOverlay.setManaged(false);
                    }
                });
            }
        };
        new Thread(refreshTask, "teacher-refresh-task").start();
    }

    @FXML
    private void handleAddTeacher(ActionEvent event) {
        statusLabel.setText("Opening Add Teacher dialog...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_teacher.fxml"));
            Parent addTeacherContent = loader.load();

            // Get the controller
            AddTeacherController addTeacherController = loader.getController();
            addTeacherController.setTeacherManagementController(this);

            // Create a new Stage for the dialog
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(addTeacherContent);

            // Load CSS stylesheets
            var teacherFormCss = getClass().getResource("/css/teacher_form.css");
            if (teacherFormCss != null) {
                scene.getStylesheets().add(teacherFormCss.toExternalForm());
            }

            var appCss = getClass().getResource("/css/app.css");
            if (appCss != null) {
                scene.getStylesheets().add(appCss.toExternalForm());
            }

            dialogStage.setTitle("Add Teacher");
            dialogStage.setScene(scene);
            dialogStage.setWidth(560);
            dialogStage.setHeight(600);
            dialogStage.centerOnScreen();
            dialogStage.setResizable(true);

            // Make it modal
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Show the dialog
            dialogStage.show();

            statusLabel.setText("Add Teacher dialog opened");
        } catch (Exception e) {
            System.err.println("Error opening Add Teacher dialog: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error opening Add Teacher dialog");
        }
    }

    /**
     * Shows the loading overlay with progress indicator
     */
    private void showLoading() {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                loadingOverlay.setVisible(true);
                loadingOverlay.setManaged(true);
            });
        }
    }

    /**
     * Hides the loading overlay
     */
    private void hideLoading() {
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                loadingOverlay.setVisible(false);
                loadingOverlay.setManaged(false);
            });
        }
    }

    private void navigateToTeacherManagement() {
        try {
            // Find the mainContainer from the scene
            VBox parentContainer = (VBox) addTeacherBtn.getScene().lookup("#mainContainer");

            if (parentContainer == null) {
                // Alternative: navigate up the scene graph to find the main container
                javafx.scene.Node current = addTeacherBtn.getParent();
                while (current != null) {
                    if (current instanceof VBox && current.getId() != null && current.getId().equals("mainContainer")) {
                        parentContainer = (VBox) current;
                        break;
                    }
                    current = current.getParent();
                }
            }

            if (parentContainer != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher_management.fxml"));
                Parent teacherManagementContent = loader.load();

                // Apply stylesheet to the loaded content
                var teacherCss = getClass().getResource("/css/teacher_management.css");
                if (teacherCss != null) {
                    teacherManagementContent.getStylesheets().add(teacherCss.toExternalForm());
                }

                // Remove existing content (keep header at index 0)
                if (parentContainer.getChildren().size() > 1) {
                    parentContainer.getChildren().remove(1, parentContainer.getChildren().size());
                }

                // Add teacher management content
                parentContainer.getChildren().add(teacherManagementContent);
                VBox.setVgrow(teacherManagementContent, Priority.ALWAYS);
                System.out.println("Teacher Management page loaded successfully");
            } else {
                System.err.println("Could not find mainContainer");
                showFeatureAlert("Error", "Could not navigate to Teacher Management.");
            }

        } catch (Exception e) {
            System.err.println("Error loading teacher management: " + e.getMessage());
            e.printStackTrace();
            showFeatureAlert("Error", "Failed to load Teacher Management: " + e.getMessage());
        }
    }
    // Helper method to show alerts (placeholder for actual navigation)
    private void showFeatureAlert(String title, String description) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(description + "\n\nThis feature will be implemented soon!");

        var appCss = getClass().getResource("/css/app.css");
        if (appCss != null) {
            alert.getDialogPane().getStylesheets().add(appCss.toExternalForm());
        }

        alert.showAndWait();
    }

    @FXML
    public void onAddBtnClick() {
        showFeatureAlert("Add Teacher", "Add Teachers.");
        System.out.println("Add teachers clicked");
    }

    private void handleEditTeacher(Teacher teacher) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_teacher.fxml"));
            Parent root = loader.load();

            EditTeacherController controller = loader.getController();
            controller.setTeacherToEdit(teacher);
            controller.setTeacherManagementController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Teacher "+ teacher.getTeacherName());

            Scene scene = new Scene(root, 800, 600);
            if (getClass().getResource("/css/teacher_form.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/teacher_form.css").toExternalForm());
            }

            root.applyCss();
            root.layout();

            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit teacher form: " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply CSS styling to alert dialog
        var appCss = getClass().getResource("/css/app.css");
        var teacherFormCss = getClass().getResource("/css/teacher_form.css");
        if (alert.getDialogPane() != null) {
            if (appCss != null) {
                alert.getDialogPane().getStylesheets().add(appCss.toExternalForm());
            }
            if (teacherFormCss != null) {
                alert.getDialogPane().getStylesheets().add(teacherFormCss.toExternalForm());
            }
        }

        alert.showAndWait();
    }

}
