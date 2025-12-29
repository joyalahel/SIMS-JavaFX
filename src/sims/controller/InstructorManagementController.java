package sims.controller;

import sims.UserSession;
import sims.dao.InstructorDao;
import sims.model.Instructor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.sql.Date;
import java.util.Optional;

public class InstructorManagementController {
    private Stage primaryStage;
    private TableView<Instructor> instructorTable;
    private InstructorDao instructorDao;
    
    private Button btnAdd;
    private Button btnEdit;
    private Button btnDelete;
    private Button btnBack;
    
    private Label lblStatus;
    
    public InstructorManagementController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.instructorDao = new InstructorDao();
    }
    
    public void showInstructorManagement() {
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    lblStatus = new Label("Loading...");
    lblStatus.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 12px; -fx-font-weight: bold;");
    
    createStyledButtons();
    setupStyledTable();
    setupEventHandlers();
    
    VBox topSection = createTopSection();
    root.setTop(topSection);
    root.setCenter(createTableContainer());
    
    loadInstructorData();
    
    // Remove dimensions and set to full screen
    Scene scene = new Scene(root);
    primaryStage.setTitle("Instructor Management - SIMS");
    primaryStage.setScene(scene); // This replaces the current scene
    primaryStage.setMaximized(true);
    primaryStage.setFullScreen(false);
    primaryStage.show();
}
    
    private Background createBackground() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0d1b2a")),
            new Stop(0.3, Color.web("#1b263b")),
            new Stop(0.7, Color.web("#415a77")),
            new Stop(1, Color.web("#1b263b"))
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
    
    private VBox createTopSection() {
        UserSession session = UserSession.getInstance();
        
        Label title = new Label("Instructor Management");
        title.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 24px; -fx-font-weight: bold; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        Label welcome = new Label("Welcome, " + session.getFullName());
        welcome.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px;");
        
        HBox buttonPanel = new HBox(15);
        buttonPanel.setPadding(new Insets(15, 0, 5, 0));
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnBack);
        
        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25));
        topSection.setBackground(createHeaderBackground());
        topSection.getChildren().addAll(title, welcome, buttonPanel, lblStatus);
        
        return topSection;
    }
    
    private Background createHeaderBackground() {
        LinearGradient headerGradient = new LinearGradient(
            0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#bbdefb")),
            new Stop(0.5, Color.web("#e3f2fd")),
            new Stop(1, Color.web("#bbdefb"))
        );
        return new Background(new BackgroundFill(headerGradient, new CornerRadii(0, 0, 20, 20, false), Insets.EMPTY));
    }
    
    private void createStyledButtons() {
        btnAdd = createStyledButton("Add Instructor", "#1565c0");
        btnEdit = createStyledButton("Edit Instructor", "#2196f3");
        btnDelete = createStyledButton("Delete Instructor", "#1565c0");
        btnBack = createStyledButton("Back to Admin", "#2196f3");
        
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 20px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: derive(" + color + ", 20%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 2);"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + color + ", 20%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 20px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: derive(" + color + ", 40%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 20px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: derive(" + color + ", 20%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 2);"
        ));
        
        return button;
    }
    
    private void setupStyledTable() {
        instructorTable = new TableView<>();
        instructorTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        // Make table fill available width
        instructorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        instructorTable.setItems(instructorDao.getInstructorData());

        // Create columns
        TableColumn<Instructor, String> idCol = createStyledColumn("Instructor ID", "instructorId", 120);
        TableColumn<Instructor, String> firstNameCol = createStyledColumn("First Name", "firstName", 150);
        TableColumn<Instructor, String> lastNameCol = createStyledColumn("Last Name", "lastName", 150);
        TableColumn<Instructor, String> emailCol = createStyledColumn("Email", "email", 250);
        TableColumn<Instructor, String> deptCol = createStyledColumn("Department", "department", 180);
        TableColumn<Instructor, Date> hireDateCol = createStyledColumn("Hire Date", "hireDate", 130);

        instructorTable.getColumns().addAll(idCol, firstNameCol, lastNameCol, emailCol, deptCol, hireDateCol);

        instructorTable.setRowFactory(tv -> new TableRow<Instructor>() {
            @Override
            protected void updateItem(Instructor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f8fbff; -fx-text-fill: #1e3c72; -fx-border-color: #e3f2fd;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-text-fill: #1e3c72; -fx-border-color: #e3f2fd;");
                    }
                }
            }
        });

        instructorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEdit.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
        });
    }
    
    private <T> TableColumn<Instructor, T> createStyledColumn(String title, String property, double width) {
        TableColumn<Instructor, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setMinWidth(80);
        
        column.setStyle(
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 0 1 0 0;"
        );
        
        column.setCellFactory(tc -> new TableCell<Instructor, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #1e3c72; -fx-font-size: 12px; -fx-padding: 8px;");
                }
            }
        });
        
        return column;
    }
    
    private VBox createTableContainer() {
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(20));
        tableContainer.setBackground(Background.EMPTY);
        
        Label tableTitle = new Label("Instructor Records");
        tableTitle.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 20px; -fx-font-weight: bold; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        // Make table expand to fill available space
        VBox.setVgrow(instructorTable, Priority.ALWAYS);
        instructorTable.setMaxHeight(Double.MAX_VALUE);
        
        VBox.setMargin(tableTitle, new Insets(0, 0, 15, 0));
        tableContainer.getChildren().addAll(tableTitle, instructorTable);
        
        return tableContainer;
    }
    
    private void setupEventHandlers() {
        btnAdd.setOnAction(e -> showAddInstructorDialog());
        btnEdit.setOnAction(e -> showEditInstructorDialog());
        btnDelete.setOnAction(e -> deleteInstructor());
        btnBack.setOnAction(e -> backToAdminDashboard());
        
        instructorTable.getItems().addListener((javafx.collections.ListChangeListener<Instructor>) change -> {
            updateStatus("Ready - " + instructorTable.getItems().size() + " instructors loaded");
        });
    }
    
    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            // Color code status messages
            if (message.toLowerCase().contains("error") || message.toLowerCase().contains("failed")) {
                lblStatus.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else if (message.toLowerCase().contains("success")) {
                lblStatus.setStyle("-fx-text-fill: #4fc3f7; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else {
                lblStatus.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
        }
    }
    
    private void showAddInstructorDialog() {
        Dialog<InstructorPasswordPair> dialog = new Dialog<>();
        dialog.setTitle("Add New Instructor");
        dialog.setHeaderText("Enter instructor details and set login password:");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        dialog.getDialogPane().setStyle("-fx-background-color: #e3f2fd;");
        
        GridPane grid = createInstructorForm(null);
        dialog.getDialogPane().setContent(grid);
        
        // Set dialog size
        dialog.getDialogPane().setMinSize(500, 450);
        
        String nextId = instructorDao.generateNextInstructorId();
        TextField instructorIdField = (TextField) grid.getChildren().get(1);
        instructorIdField.setText(nextId);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    InstructorPasswordPair pair = extractInstructorFromForm(grid);
                    if (pair != null) {
                        return pair;
                    }
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter date in YYYY-MM-DD format!");
                }
            }
            return null;
        });
        
        Optional<InstructorPasswordPair> result = dialog.showAndWait();
        
        result.ifPresent(pair -> {
            boolean success = instructorDao.insertInstructor(pair.getInstructor(), pair.getPassword());
            if (success) {
                String username = pair.getInstructor().getInstructorId().toLowerCase();
                
                String loginInfo = String.format(
                    "Instructor added successfully! 🎉\n\n" +
                    "Instructor ID: %s\n" +
                    "Name: %s %s\n" +
                    "Login Credentials:\n" +
                    "Username: %s\n" +
                    "Password: %s\n\n" +
                    "The instructor can now login to the system.",
                    pair.getInstructor().getInstructorId(),
                    pair.getInstructor().getFirstName(),
                    pair.getInstructor().getLastName(),
                    username,
                    pair.getPassword()
                );
                
                showAlert(Alert.AlertType.INFORMATION, "Success", loginInfo);
                updateStatus("Instructor added successfully");
                loadInstructorData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add instructor!");
                updateStatus("Failed to add instructor");
            }
        });
    }
    
    private void showEditInstructorDialog() {
        Instructor selectedInstructor = instructorTable.getSelectionModel().getSelectedItem();
        if (selectedInstructor == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an instructor to edit!");
            return;
        }
        
        Dialog<InstructorPasswordPair> dialog = new Dialog<>();
        dialog.setTitle("Edit Instructor");
        dialog.setHeaderText("Edit instructor details and update password (optional):");
        
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        dialog.getDialogPane().setStyle("-fx-background-color: #e3f2fd;");
        
        GridPane grid = createInstructorForm(selectedInstructor);
        dialog.getDialogPane().setContent(grid);
        
        // Set dialog size
        dialog.getDialogPane().setMinSize(500, 450);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    InstructorPasswordPair pair = extractInstructorFromForm(grid);
                    if (pair != null) {
                        pair.getInstructor().setInstructorId(selectedInstructor.getInstructorId());
                        return pair;
                    }
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter date in YYYY-MM-DD format!");
                }
            }
            return null;
        });
        
        Optional<InstructorPasswordPair> result = dialog.showAndWait();
        
        result.ifPresent(pair -> {
            boolean success = instructorDao.updateInstructor(pair.getInstructor(), pair.getPassword());
            if (success) {
                String message = "Instructor updated successfully!";
                if (pair.getPassword() != null && !pair.getPassword().isEmpty()) {
                    message += "\n\nPassword has been updated.";
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", message);
                updateStatus("Instructor updated successfully");
                loadInstructorData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update instructor!");
                updateStatus("Failed to update instructor");
            }
        });
    }
    
    private GridPane createInstructorForm(Instructor instructor) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #bbdefb; -fx-background-radius: 10px;");
        
        TextField instructorIdField = new TextField();
        instructorIdField.setEditable(false);
        instructorIdField.setStyle(createFieldStyle());
        
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField departmentField = new TextField();
        TextField hireDateField = new TextField();
        PasswordField passwordField = new PasswordField();
        
        for (TextField field : new TextField[]{firstNameField, lastNameField, emailField, departmentField, hireDateField, passwordField}) {
            field.setStyle(createFieldStyle());
        }
        
        hireDateField.setPromptText("YYYY-MM-DD");
        passwordField.setPromptText("Set login password");
        
        if (instructor != null) {
            instructorIdField.setText(instructor.getInstructorId());
            firstNameField.setText(instructor.getFirstName());
            lastNameField.setText(instructor.getLastName());
            emailField.setText(instructor.getEmail());
            departmentField.setText(instructor.getDepartment());
            hireDateField.setText(instructor.getHireDate().toString());
            passwordField.setPromptText("Leave blank to keep current password");
        }
        
        String labelStyle = "-fx-text-fill: #0d47a1; -fx-font-weight: bold; -fx-font-size: 13px;";
        
        grid.add(createStyledLabel("Instructor ID:", labelStyle), 0, 0);
        grid.add(instructorIdField, 1, 0);
        grid.add(createStyledLabel("First Name:", labelStyle), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(createStyledLabel("Last Name:", labelStyle), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(createStyledLabel("Email:", labelStyle), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(createStyledLabel("Department:", labelStyle), 0, 4);
        grid.add(departmentField, 1, 4);
        grid.add(createStyledLabel("Hire Date:", labelStyle), 0, 5);
        grid.add(hireDateField, 1, 5);
        grid.add(createStyledLabel("Password:", labelStyle), 0, 6);
        grid.add(passwordField, 1, 6);
        
        return grid;
    }
    
    private String createFieldStyle() {
        return "-fx-background-color: white; -fx-border-color: #64b5f6; -fx-border-radius: 5px; -fx-padding: 8px;";
    }
    
    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }
    
    private InstructorPasswordPair extractInstructorFromForm(GridPane grid) {
        TextField instructorIdField = (TextField) grid.getChildren().get(1);
        TextField firstNameField = (TextField) grid.getChildren().get(3);
        TextField lastNameField = (TextField) grid.getChildren().get(5);
        TextField emailField = (TextField) grid.getChildren().get(7);
        TextField departmentField = (TextField) grid.getChildren().get(9);
        TextField hireDateField = (TextField) grid.getChildren().get(11);
        PasswordField passwordField = (PasswordField) grid.getChildren().get(13);
        
        if (firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            departmentField.getText().trim().isEmpty() ||
            hireDateField.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields!");
            return null;
        }
        
        boolean isNewInstructor = instructorIdField.getText().equals(instructorDao.generateNextInstructorId());
        if (isNewInstructor && passwordField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please set a password for the new instructor!");
            return null;
        }
        
        String email = emailField.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address!");
            return null;
        }
        
        try {
            Date.valueOf(hireDateField.getText().trim());
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter date in YYYY-MM-DD format!");
            return null;
        }
        
        Instructor instructor = new Instructor();
        instructor.setInstructorId(instructorIdField.getText().trim());
        instructor.setFirstName(firstNameField.getText().trim());
        instructor.setLastName(lastNameField.getText().trim());
        instructor.setEmail(emailField.getText().trim());
        instructor.setDepartment(departmentField.getText().trim());
        instructor.setHireDate(Date.valueOf(hireDateField.getText().trim()));
        
        String password = passwordField.getText().trim();
        
        return new InstructorPasswordPair(instructor, password);
    }
    
    private void loadInstructorData() {
        instructorDao.loadAllInstructors();
        updateStatus("Ready - " + instructorTable.getItems().size() + " instructors loaded");
    }
    
    private void deleteInstructor() {
        Instructor selectedInstructor = instructorTable.getSelectionModel().getSelectedItem();
        if (selectedInstructor == null) return;
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("🗑️ Delete Instructor");
        confirmation.setContentText("Are you sure you want to delete:\n\n" +
            "• Name: " + selectedInstructor.getFirstName() + " " + selectedInstructor.getLastName() + "\n" +
            "• ID: " + selectedInstructor.getInstructorId() + "\n" +
            "• Department: " + selectedInstructor.getDepartment() + "\n\nThis action cannot be undone.");
        
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #d32f2f; -fx-border-width: 2px;");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = instructorDao.deleteInstructor(selectedInstructor.getInstructorId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor deleted successfully!");
                updateStatus("Instructor deleted successfully");
                loadInstructorData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete instructor!");
            }
        }
    }
private void backToAdminDashboard() {
    // Use the same stage to show admin dashboard
    AdminDashboardController adminController = new AdminDashboardController(primaryStage);
    adminController.showAdminDashboard();
}
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        String color = alertType == Alert.AlertType.ERROR ? "#d32f2f" : 
                      alertType == Alert.AlertType.WARNING ? "#ff9800" : "#1565c0";
        
        dialogPane.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: " + color + "; -fx-border-width: 2px;");
        
        alert.showAndWait();
    }
    
    // Helper class for instructor and password
    private class InstructorPasswordPair {
        private Instructor instructor;
        private String password;
        
        public InstructorPasswordPair(Instructor instructor, String password) {
            this.instructor = instructor;
            this.password = password;
        }
        
        public Instructor getInstructor() { return instructor; }
        public String getPassword() { return password; }
    }
}