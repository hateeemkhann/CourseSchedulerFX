package com.example.courseschedulerfx.controllers;

import com.example.courseschedulerfx.DAO.DepartmentDAO;
import com.example.courseschedulerfx.datastructures.Treee;
import com.example.courseschedulerfx.model.Department;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class DepartmentManagementController {

    @FXML
    private Button addDepartmentBtn;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Department> departmentTable;

    @FXML
    private TableColumn<Department, Integer> idColumn;

    @FXML
    private TableColumn<Department, String> nameColumn;

    @FXML
    private TableColumn<Department, Void> actionsColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label countLabel;

    @FXML
    private VBox loadingOverlay;

    @FXML
    private ProgressIndicator loadingIndicator;

    private ObservableList<Department> departmentList = FXCollections.observableArrayList();
    private ObservableList<Department> filteredList = FXCollections.observableArrayList();
    private Treee departmentTree = new Treee();

    @FXML
    public void initialize() {
        // Set column resize policy
        departmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        loadDepartmentDataAsync();

        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });
    }

    private void setupTableColumns() {
        // ID Column
        idColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getDepartmentID()).asObject());

        // Name Column
        nameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDepartmentName()));

        // Actions Column with Edit and Delete buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("edit-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                container.setAlignment(Pos.CENTER);
                editBtn.setMinWidth(50);
                deleteBtn.setMinWidth(55);

                editBtn.setOnAction(event -> {
                    Department department = getTableView().getItems().get(getIndex());
                    handleEditDepartment(department);
                });

                deleteBtn.setOnAction(event -> {
                    Department department = getTableView().getItems().get(getIndex());
                    handleDeleteDepartment(department);
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

    private void loadDepartmentDataAsync() {
        statusLabel.setText("Loading...");
        // Show loading overlay
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            loadingOverlay.setManaged(true);
        }

        Task<List<Department>> task = new Task<>() {
            @Override
            protected List<Department> call() {
                return DepartmentDAO.getAllDepartments();
            }
        };

        task.setOnSucceeded(event -> {
            List<Department> departments = task.getValue();
            Platform.runLater(() -> {
                departmentList.clear();
                departmentList.addAll(departments);
                // Rebuild the tree for efficient searching
                departmentTree = new Treee();
                for (Department dept : departments) {
                    departmentTree.insert(dept);
                }
                filteredList.setAll(departmentList);
                departmentTable.setItems(filteredList);
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
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load department data: " + task.getException().getMessage());
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
        countLabel.setText("Total: " + filteredList.size() + " department(s)");
    }

    @FXML
    private void handleAddDepartment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_department.fxml"));
            Parent root = loader.load();

            AddDepartmentController controller = loader.getController();
            controller.setDepartmentManagementController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Department");

            Scene scene = new Scene(root, 800, 600);
            if (getClass().getResource("/css/department_form.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/department_form.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add department form: " + e.getMessage());
        }
    }

    private void handleEditDepartment(Department department) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_department.fxml"));
            Parent root = loader.load();

            EditDepartmentController controller = loader.getController();
            controller.setDepartment(department);
            controller.setDepartmentManagementController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Department");

            Scene scene = new Scene(root, 800, 600);
            if (getClass().getResource("/css/department_form.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/department_form.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit department form: " + e.getMessage());
        }
    }

    private void handleDeleteDepartment(Department department) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Department");
        confirmAlert.setContentText("Are you sure you want to delete department '" + department.getDepartmentName() + "'?\n\nWarning: This may affect related courses, batches, and teachers.\nThis action cannot be undone.");

        // Apply CSS styling to confirmation dialog
        var appCss = getClass().getResource("/css/app.css");
        var departmentFormCss = getClass().getResource("/css/department_form.css");
        if (confirmAlert.getDialogPane() != null) {
            if (appCss != null) {
                confirmAlert.getDialogPane().getStylesheets().add(appCss.toExternalForm());
            }
            if (departmentFormCss != null) {
                confirmAlert.getDialogPane().getStylesheets().add(departmentFormCss.toExternalForm());
            }
        }

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = DepartmentDAO.deleteDepartment(department.getDepartmentID());
                if (deleted) {
                    statusLabel.setText("Department deleted successfully");
                    refreshData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Department deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete department.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error deleting department: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        refreshData();
    }

    public void refreshData() {
        loadDepartmentDataAsync();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply CSS styling to alert dialog
        var appCss = getClass().getResource("/css/app.css");
        var departmentFormCss = getClass().getResource("/css/department_form.css");
        if (alert.getDialogPane() != null) {
            if (appCss != null) {
                alert.getDialogPane().getStylesheets().add(appCss.toExternalForm());
            }
            if (departmentFormCss != null) {
                alert.getDialogPane().getStylesheets().add(departmentFormCss.toExternalForm());
            }
        }

        alert.showAndWait();
    }

    private void filterData(String searchText) {
        if (searchText.trim().isEmpty()) {
            filteredList.setAll(departmentList);
        } else {
            try {
                int id = Integer.parseInt(searchText.trim());
                Department dept = departmentTree.search(id);
                if (dept != null) {
                    filteredList.setAll(dept);
                } else {
                    filteredList.clear();
                }
            } catch (NumberFormatException e) {
                // Filter by name
                filteredList.setAll(departmentList.filtered(department ->
                    department.getDepartmentName().toLowerCase().contains(searchText.toLowerCase())
                ));
            }
        }
        updateCount();
    }
}
