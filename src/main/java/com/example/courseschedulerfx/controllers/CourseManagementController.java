package com.example.courseschedulerfx.controllers;

import com.example.courseschedulerfx.DAO.AdminDAO;
import com.example.courseschedulerfx.DAO.CourseDAO;
import com.example.courseschedulerfx.model.Admin;
import com.example.courseschedulerfx.model.Course;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventType;
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

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CourseManagementController implements Initializable {

    // Search and Button Controls
    @FXML
    private TextField searchField;

    @FXML
    private Button addCourseBtn;

    // Table and Columns
    @FXML
    private TableView<Course> courseTable;

    @FXML
    private TableColumn<Course, Integer> courseIDColumn;

    @FXML
    private TableColumn<Course, String> courseNameColumn;

    @FXML
    private TableColumn<Course,Integer> creditHoursColumn;

    @FXML
    private TableColumn<Course, Void> actionColumn;

    @FXML
    private TableColumn<Course,String> departmentColumn;
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

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private ObservableList<Course> filteredCourseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set column resize policy to make table full-width responsive
        if (courseTable != null) {
            courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Initialize UI state
        hideLoading();
        if (countLabel != null) {
            countLabel.setText("Total: 0 courses");
        }
        if (statusLabel != null) {
            statusLabel.setText("Ready");
        }
        setupTableColumns();
        // Add table selection listener
        if (courseTable != null) {
            courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    statusLabel.setText("Selected course");
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
        loadCourseDataAsync();
    }

    private void setupTableColumns() {
        // ID Column
        courseIDColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCourseID()).asObject());

        // Name Column
        courseNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCourseTitle()));

        // Credit Hours Column
        creditHoursColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCreditHours()).asObject());

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
                    Course course = courseTable.getItems().get(getIndex());
                    handleEditCourse(course);
                });

                deleteBtn.setOnAction(event -> {
                    Course course = courseTable.getItems().get(getIndex());
                    handleDeleteCourse(course);
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

    private void handleDeleteCourse(Course course) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Course");
        confirmAlert.setContentText("Are you sure you want to delete course '" + course.getCourseTitle() + "'?\nThis action cannot be undone.");

        // Apply CSS styling to confirmation dialog
        var appCss = getClass().getResource("/css/app.css");
        var courseFormCss = getClass().getResource("/css/course_form.css");
        if (confirmAlert.getDialogPane() != null) {
            if (appCss != null) {
                confirmAlert.getDialogPane().getStylesheets().add(appCss.toExternalForm());
            }
            if (courseFormCss != null) {
                confirmAlert.getDialogPane().getStylesheets().add(courseFormCss.toExternalForm());
            }
        }

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = CourseDAO.deleteCourse(course.getCourseID());
                if (deleted) {
                    statusLabel.setText("Course deleted successfully");
                    refreshData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete course.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error deleting course: " + e.getMessage());
            }
        }
    }

    public void refreshData() {
        loadCourseDataAsync();
    }

    private void loadCourseDataAsync() {
        statusLabel.setText("Loading...");
        long startTime = System.currentTimeMillis();

        // Show loading overlay using Platform.runLater to ensure it's shown
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(true);
                loadingOverlay.setManaged(true);
            }
        });

        Task<List<Course>> task = new Task<>() {
            @Override
            protected List<Course> call() {
                return CourseDAO.getCourseList();
            }
        };
        task.setOnSucceeded(event -> {
            List<Course> courses = task.getValue();

            // Update UI immediately without artificial delays
            Platform.runLater(() -> {
                courseList.clear();
                if (courses != null) {
                    courseList.addAll(courses);
                }
                filteredCourseList.setAll(courseList);
                courseTable.setItems(filteredCourseList);
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
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load course data: " + task.getException().getMessage());
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
        countLabel.setText("Total: " + filteredCourseList.size() + " course(s)");
    }

    /**
     * Filter data based on search query - called instantly as user types
     */
    private void filterData(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Show all courses
            filteredCourseList.setAll(courseList);
            statusLabel.setText("Ready");
        } else {
            // Filter courses by title (case-insensitive)
            filteredCourseList.clear();
            for (Course course : courseList) {
                if (course.getCourseTitle().toLowerCase().contains(query.toLowerCase()) ||
                    String.valueOf(course.getCourseID()).contains(query.toLowerCase())) {
                    filteredCourseList.add(course);
                }
            }
            statusLabel.setText("Found " + filteredCourseList.size() + " course(s)");
        }
        courseTable.setItems(filteredCourseList);
        updateCount();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        statusLabel.setText("Refreshing courses...");

        // Show loading overlay
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(true);
                loadingOverlay.setManaged(true);
            }
        });

        // Background task to refresh courses
        Task<List<Course>> refreshTask = new Task<List<Course>>() {
            @Override
            protected List<Course> call() throws Exception {
                // Fetch fresh data from database
                return CourseDAO.getCourseList();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Course> refreshedCourses = getValue();
                    if (refreshedCourses != null) {
                        courseList.clear();
                        courseList.addAll(refreshedCourses);
                        filteredCourseList.setAll(courseList);
                        courseTable.setItems(filteredCourseList);
                        updateCount();
                        statusLabel.setText("Courses refreshed - " + filteredCourseList.size() + " course(s) loaded");
                    } else {
                        statusLabel.setText("No courses found");
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
        new Thread(refreshTask, "course-refresh-task").start();
    }

    @FXML
    private void handleAddCourse(ActionEvent event) {
        statusLabel.setText("Opening Add Course dialog...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_course.fxml"));
            Parent addCourseContent = loader.load();

            // Get the controller
            AddCourseController addCourseController = loader.getController();
            addCourseController.setCourseManagementController(this);

            // Create a new Stage for the dialog
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(addCourseContent);

            // Load CSS stylesheets
            var courseFormCss = getClass().getResource("/css/course_form.css");
            if (courseFormCss != null) {
                scene.getStylesheets().add(courseFormCss.toExternalForm());
            }

            var appCss = getClass().getResource("/css/app.css");
            if (appCss != null) {
                scene.getStylesheets().add(appCss.toExternalForm());
            }

            dialogStage.setTitle("Add Course");
            dialogStage.setScene(scene);
            dialogStage.setWidth(560);
            dialogStage.setHeight(600);
            dialogStage.centerOnScreen();
            dialogStage.setResizable(true);

            // Make it modal
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Show the dialog
            dialogStage.show();

            statusLabel.setText("Add Course dialog opened");
        } catch (Exception e) {
            System.err.println("Error opening Add Course dialog: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error opening Add Course dialog");
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

    private void navigateToCourseManagement() {
        try {
            // Find the mainContainer from the scene
            VBox parentContainer = (VBox) addCourseBtn.getScene().lookup("#mainContainer");

            if (parentContainer == null) {
                // Alternative: navigate up the scene graph to find the main container
                javafx.scene.Node current = addCourseBtn.getParent();
                while (current != null) {
                    if (current instanceof VBox && current.getId() != null && current.getId().equals("mainContainer")) {
                        parentContainer = (VBox) current;
                        break;
                    }
                    current = current.getParent();
                }
            }

            if (parentContainer != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_course.fxml"));
                Parent courseManagementContent = loader.load();

                // Apply stylesheet to the loaded content
                var courseCss = getClass().getResource("/css/course_form.css");
                if (courseCss != null) {
                    courseManagementContent.getStylesheets().add(courseCss.toExternalForm());
                }

                // Remove existing content (keep header at index 0)
                if (parentContainer.getChildren().size() > 1) {
                    parentContainer.getChildren().remove(1, parentContainer.getChildren().size());
                }

                // Add course management content
                parentContainer.getChildren().add(courseManagementContent);
                VBox.setVgrow(courseManagementContent, Priority.ALWAYS);
                System.out.println("Course Add page loaded successfully");
            } else {
                System.err.println("Could not find mainContainer");
                showFeatureAlert("Error", "Could not navigate to Add course.");
            }

        } catch (Exception e) {
            System.err.println("Error loading add course : " + e.getMessage());
            e.printStackTrace();
            showFeatureAlert("Error", "Failed to load Add course: " + e.getMessage());
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
        showFeatureAlert("Add Course", "Add Courses.");
        System.out.println("Add courses clicked");
    }

    private void handleEditCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_course.fxml"));
            Parent root = loader.load();

            EditCourseController controller = loader.getController();
            controller.setCourseToEdit(course);
            controller.setCourseManagementController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Course "+ course.getCourseTitle());

            Scene scene = new Scene(root, 800, 600);
            if (getClass().getResource("/css/course_form.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/course_form.css").toExternalForm());
            }

            root.applyCss();
            root.layout();

            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit course form: " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply CSS styling to alert dialog
        var appCss = getClass().getResource("/css/app.css");
        var courseFormCss = getClass().getResource("/css/course_form.css");
        if (alert.getDialogPane() != null) {
            if (appCss != null) {
                alert.getDialogPane().getStylesheets().add(appCss.toExternalForm());
            }
            if (courseFormCss != null) {
                alert.getDialogPane().getStylesheets().add(courseFormCss.toExternalForm());
            }
        }

        alert.showAndWait();
    }

}
