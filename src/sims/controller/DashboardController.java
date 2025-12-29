package sims.controller;

import sims.UserSession;
import sims.dao.StudentDao;
import sims.dao.UserDao;
import sims.model.Student;
import sims.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class DashboardController {
    private Stage primaryStage;
    private TableView<Student> studentTable;
    private StudentDao studentDao;
    private UserDao userDao;
    
    // Buttons
    private Button btnAdd;
    private Button btnEdit;
    private Button btnDelete;
    private Button btnChangePassword;
    private Button btnLogout;
    private Button btnBack;
    
    // Status
    private Label lblStatus;
    private Label lblWelcome;
    
    // Helper class to hold both Student and Password
    private class StudentPasswordPair {
        private Student student;
        private String password;
        
        public StudentPasswordPair(Student student, String password) {
            this.student = student;
            this.password = password;
        }
        
        public Student getStudent() { return student; }
        public String getPassword() { return password; }
    }
    
    public DashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.studentDao = new StudentDao();
        this.userDao = new UserDao();
    }
    
    public void showDashboard() {
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    // Initialize status label FIRST
    lblStatus = new Label("Loading...");
    lblStatus.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 12px; -fx-font-weight: bold;");
    
    // Welcome section
    VBox welcomeSection = createWelcomeSection();
    
    // Create buttons with blue styling
    createStyledButtons();
    
    // Button panel
    HBox buttonPanel = createButtonPanel();
    
    // Create table with white background
    setupStyledTable();
    
    // Set button actions
    setupEventHandlers();
    
    // Setup role-based access
    setupRoleBasedAccess();
    
    // Layout
    VBox topSection = new VBox(15);
    topSection.setPadding(new Insets(25));
    topSection.setBackground(createHeaderBackground());
    topSection.getChildren().addAll(welcomeSection, buttonPanel, lblStatus);
    
    root.setTop(topSection);
    root.setCenter(createTableContainer());
    
    // Load initial data AFTER UI is set up
    loadStudentData();
    
    // Remove dimensions and set to full screen
    Scene scene = new Scene(root);
    primaryStage.setTitle("Student Information Management System - Dashboard");
    primaryStage.setScene(scene); // This replaces the current scene
    primaryStage.setMaximized(true);
    primaryStage.setFullScreen(false);
    primaryStage.show();
}
    
    private Background createBackground() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0d1b2a")),  // Dark navy
            new Stop(0.3, Color.web("#1b263b")), // Navy blue
            new Stop(0.7, Color.web("#415a77")), // Steel blue
            new Stop(1, Color.web("#1b263b"))   // Navy blue
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
    
    private Background createHeaderBackground() {
        LinearGradient headerGradient = new LinearGradient(
            0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#bbdefb")),   // Pale blue
            new Stop(0.5, Color.web("#e3f2fd")), // Background blue
            new Stop(1, Color.web("#bbdefb"))    // Pale blue
        );
        return new Background(new BackgroundFill(headerGradient, new CornerRadii(0, 0, 20, 20, false), Insets.EMPTY));
    }
    
    private VBox createWelcomeSection() {
        UserSession session = UserSession.getInstance();
        String roleDisplay = getRoleDisplayName(session.getRole());
        
        lblWelcome = new Label("Welcome, " + session.getFullName() + "! ");
        lblWelcome.setStyle("-fx-text-fill: #1e3c72; -fx-font-size: 28px; -fx-font-weight: bold; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(13,27,42,0.3), 4, 0, 0, 2);");
        
        Label roleLabel = new Label("Role: " + roleDisplay);
        roleLabel.setStyle("-fx-text-fill: #415a77; -fx-font-size: 14px; -fx-font-weight: normal;");
        
        Label subTitle = new Label("Student Information Management System");
        subTitle.setStyle("-fx-text-fill: #415a77; -fx-font-size: 14px; -fx-font-weight: normal;");
        
        VBox welcomeBox = new VBox(5, lblWelcome, roleLabel, subTitle);
        welcomeBox.setAlignment(Pos.CENTER_LEFT);
        
        return welcomeBox;
    }
    
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin": return "Administrator";
            case "instructor": return "Instructor";
            case "student": return "Student";
            default: return role;
        }
    }
    
    private void createStyledButtons() {
        btnAdd = createStyledButton("Add Student", "#1565c0");        // Dark blue
        btnEdit = createStyledButton("Edit Student", "#2196f3");      // Primary blue
        btnDelete = createStyledButton("Delete Student", "#1565c0"); 
        btnChangePassword = createStyledButton("Change Password", "#2196f3"); // Medium blue
        btnLogout = createStyledButton("Logout", "#2196f3");        
        btnBack = createStyledButton("Back to Admin", "#1565c0");    
        
        // Initially disable Edit and Delete until a student is selected
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
        
        // Hover effect - lighter shade
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
    
    private HBox createButtonPanel() {
        UserSession session = UserSession.getInstance();
        HBox buttonPanel = new HBox(12);
        buttonPanel.setPadding(new Insets(15, 0, 5, 0));
        buttonPanel.setAlignment(Pos.CENTER);
        
        // Add buttons based on user role
        if (session.isAdmin()) {
            buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnChangePassword, btnBack, btnLogout);
        } else if (session.isInstructor()) {
            buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnChangePassword, btnLogout);
            btnDelete.setDisable(true);
        } else if (session.isStudent()) {
            buttonPanel.getChildren().addAll(btnChangePassword, btnLogout);
            btnAdd.setDisable(true);
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
        }
        
        return buttonPanel;
    }
    
    private void setupRoleBasedAccess() {
        UserSession session = UserSession.getInstance();
        
        if (session.isStudent()) {
            studentTable.setEditable(false);
            updateStatus("Welcome Student! You have view-only access.", "info");
        } else if (session.isInstructor()) {
            updateStatus("Welcome Instructor! You can add and edit student records.", "info");
        } else if (session.isAdmin()) {
            updateStatus("Welcome Administrator! You have full system access.", "info");
        }
    }
    
  private void backToAdminDashboard() {
    // Simply create and show admin dashboard using the same stage
    AdminDashboardController adminController = new AdminDashboardController(primaryStage);
    adminController.showAdminDashboard();
    // The current dashboard will automatically be replaced
}
    
    private void setupStyledTable() {
        studentTable = new TableView<>();
        studentTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        // Make table fill available width
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        studentTable.setItems(studentDao.getStudentData());

        TableColumn<Student, String> idCol = createStyledColumn("Student ID", "studentId", 150);
        TableColumn<Student, String> firstNameCol = createStyledColumn("First Name", "firstName", 160);
        TableColumn<Student, String> lastNameCol = createStyledColumn("Last Name", "lastName", 160);
        TableColumn<Student, String> emailCol = createStyledColumn("Email", "email", 250);
        TableColumn<Student, Date> dobCol = createStyledColumn("Date of Birth", "dateOfBirth", 150);
        TableColumn<Student, String> majorCol = createStyledColumn("Major", "major", 200);

        studentTable.getColumns().addAll(idCol, firstNameCol, lastNameCol, emailCol, dobCol, majorCol);

        // Enhanced row factory with alternating colors
        studentTable.setRowFactory(tv -> new TableRow<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
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

        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEdit.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
        });
    }
    
    private <T> TableColumn<Student, T> createStyledColumn(String title, String property, double width) {
        TableColumn<Student, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setMinWidth(80); // Set minimum width to prevent columns from becoming too narrow
        
        column.setStyle(
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 0 1 0 0;"
        );
        
        column.setCellFactory(tc -> new TableCell<Student, T>() {
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
        
        UserSession session = UserSession.getInstance();
        String tableTitleText = session.isStudent() ? "Student Records (View Only)" : "Student Records";
        
        Label tableTitle = new Label(tableTitleText);
        tableTitle.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 20px; -fx-font-weight: bold; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        // Make table expand to fill available space
        VBox.setVgrow(studentTable, Priority.ALWAYS);
        studentTable.setMaxHeight(Double.MAX_VALUE);
        
        VBox.setMargin(tableTitle, new Insets(0, 0, 15, 0));
        tableContainer.getChildren().addAll(tableTitle, studentTable);
        
        return tableContainer;
    }
    
    private void setupEventHandlers() {
        btnAdd.setOnAction(e -> showAddStudentDialog());
        btnEdit.setOnAction(e -> showEditStudentDialog());
        btnDelete.setOnAction(e -> deleteStudent());
        btnLogout.setOnAction(e -> logout());
        btnChangePassword.setOnAction(e -> showChangePasswordDialog());
        btnBack.setOnAction(e -> backToAdminDashboard());
        
        studentTable.getItems().addListener((javafx.collections.ListChangeListener<Student>) change -> {
            updateStatus("Ready - " + studentTable.getItems().size() + " students loaded", "info");
        });
    }
    
    private void updateStatus(String message, String type) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            switch (type.toLowerCase()) {
                case "error":
                    lblStatus.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px; -fx-font-weight: bold;");
                    break;
                case "success":
                    lblStatus.setStyle("-fx-text-fill: #4fc3f7; -fx-font-size: 12px; -fx-font-weight: bold;");
                    break;
                case "warning":
                    lblStatus.setStyle("-fx-text-fill: #0288d1; -fx-font-size: 12px; -fx-font-weight: bold;");
                    break;
                default:
                    lblStatus.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
        }
    }
    
    private void showAddStudentDialog() {
        UserSession session = UserSession.getInstance();
        if (session.isStudent()) {
            showAlert(Alert.AlertType.WARNING, "Access Denied", "Students cannot add new student records!");
            return;
        }
        
        Dialog<StudentPasswordPair> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student details and set login password:");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        dialog.getDialogPane().setStyle("-fx-background-color: #e3f2fd;");
        
        GridPane grid = createFormGridWithPassword(null);
        dialog.getDialogPane().setContent(grid);
        
        // Set dialog size
        dialog.getDialogPane().setMinSize(500, 450);
        
        String nextId = studentDao.generateNextStudentId();
        TextField studentIdField = (TextField) grid.getChildren().get(1);
        studentIdField.setText(nextId);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    StudentPasswordPair pair = extractStudentWithPasswordFromForm(grid);
                    if (pair != null) {
                        return pair;
                    }
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter date in YYYY-MM-DD format!");
                }
            }
            return null;
        });
        
        Optional<StudentPasswordPair> result = dialog.showAndWait();
        
        result.ifPresent(pair -> {
            System.out.println("Attempting to add student: " + pair.getStudent().getStudentId());
            
            boolean success = studentDao.insertStudent(pair.getStudent(), pair.getPassword());
            if (success) {
                String username = pair.getStudent().getStudentId().toLowerCase();
                String loginInfo = String.format(
                    "Student added successfully! 🎉\n\n" +
                    "Student ID: %s\n" +
                    "Name: %s %s\n" +
                    "Login Credentials:\n" +
                    "Username: %s\n" +
                    "Password: %s\n\n" +
                    "The student can now login to the system.",
                    pair.getStudent().getStudentId(),
                    pair.getStudent().getFirstName(),
                    pair.getStudent().getLastName(),
                    username,
                    pair.getPassword()
                );
                
                showAlert(Alert.AlertType.INFORMATION, "Success", loginInfo);
                updateStatus("Student added successfully", "success");
                loadStudentData(); // Refresh the table
            } else {
                System.err.println("Failed to add student: " + pair.getStudent().getStudentId());
                showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to add student!\n\n" +
                    "Possible reasons:\n" +
                    "• Database connection issue\n" +
                    "• Duplicate student ID or email\n" +
                    "• Invalid data format\n\n" +
                    "Please check the console for details.");
                updateStatus("Failed to add student", "error");
            }
        });
    }
    
    private void showEditStudentDialog() {
        UserSession session = UserSession.getInstance();
        if (session.isStudent()) {
            showAlert(Alert.AlertType.WARNING, "Access Denied", "Students cannot edit student records!");
            return;
        }
        
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit!");
            return;
        }
        
        Dialog<StudentPasswordPair> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Edit student details and update password (optional):");
        
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        dialog.getDialogPane().setStyle("-fx-background-color: #e3f2fd;");
        
        GridPane grid = createFormGridWithPassword(selectedStudent);
        dialog.getDialogPane().setContent(grid);
        
        // Set dialog size
        dialog.getDialogPane().setMinSize(500, 450);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    StudentPasswordPair pair = extractStudentWithPasswordFromForm(grid);
                    if (pair != null) {
                        pair.getStudent().setStudentId(selectedStudent.getStudentId());
                        return pair;
                    }
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter date in YYYY-MM-DD format!");
                }
            }
            return null;
        });
        
        Optional<StudentPasswordPair> result = dialog.showAndWait();
        
        result.ifPresent(pair -> {
            boolean success = studentDao.updateStudent(pair.getStudent(), pair.getPassword());
            if (success) {
                String message = "Student updated successfully!";
                if (pair.getPassword() != null && !pair.getPassword().isEmpty()) {
                    message += "\n\nPassword has been updated.";
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", message);
                updateStatus("Student updated successfully", "success");
                loadStudentData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update student!");
                updateStatus("Failed to update student", "error");
            }
        });
    }
   public VBox createDashboardView() {
    // Create the main layout with deep blue gradient background
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    // Initialize status label FIRST
    lblStatus = new Label("Loading...");
    lblStatus.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 12px; -fx-font-weight: bold;");
    
    // Welcome section
    VBox welcomeSection = createWelcomeSection();
    
    // Create buttons with blue styling
    createStyledButtons();
    
    // Button panel
    HBox buttonPanel = createButtonPanel();
    
    // Create table with white background
    setupStyledTable();
    
    // Set button actions
    setupEventHandlers();
    
    // Setup role-based access
    setupRoleBasedAccess();
    
    // Layout
    VBox topSection = new VBox(15);
    topSection.setPadding(new Insets(25));
    topSection.setBackground(createHeaderBackground());
    topSection.getChildren().addAll(welcomeSection, buttonPanel, lblStatus);
    
    root.setTop(topSection);
    root.setCenter(createTableContainer());
    
    // Load initial data AFTER UI is set up
    loadStudentData();
    
    // Wrap the BorderPane in a VBox to return
    VBox mainContainer = new VBox(root);
    VBox.setVgrow(root, Priority.ALWAYS);
    
    return mainContainer;
}
    private GridPane createFormGridWithPassword(Student student) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #bbdefb; -fx-background-radius: 10px;");
        
        TextField studentIdField = new TextField();
        studentIdField.setEditable(false);
        studentIdField.setStyle(createFieldStyle());
        
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField dobField = new TextField();
        TextField majorField = new TextField();
        PasswordField passwordField = new PasswordField();
        
        for (TextField field : new TextField[]{firstNameField, lastNameField, emailField, dobField, majorField, passwordField}) {
            field.setStyle(createFieldStyle());
        }
        
        dobField.setPromptText("YYYY-MM-DD");
        passwordField.setPromptText("Set login password");
        
        if (student != null) {
            studentIdField.setText(student.getStudentId());
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            emailField.setText(student.getEmail());
            dobField.setText(student.getDateOfBirth().toString());
            majorField.setText(student.getMajor());
            passwordField.setPromptText("Leave blank to keep current password");
        }
        
        String labelStyle = "-fx-text-fill: #0d47a1; -fx-font-weight: bold; -fx-font-size: 13px;";
        
        grid.add(createStyledLabel("Student ID:", labelStyle), 0, 0);
        grid.add(studentIdField, 1, 0);
        grid.add(createStyledLabel("First Name:", labelStyle), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(createStyledLabel("Last Name:", labelStyle), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(createStyledLabel("Email:", labelStyle), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(createStyledLabel("Date of Birth:", labelStyle), 0, 4);
        grid.add(dobField, 1, 4);
        grid.add(createStyledLabel("Major:", labelStyle), 0, 5);
        grid.add(majorField, 1, 5);
        grid.add(createStyledLabel("Password:", labelStyle), 0, 6);
        grid.add(passwordField, 1, 6);
        
        return grid;
    }
    
    private String createFieldStyle() {
        return "-fx-background-color: white; -fx-border-color: #64b5f6; -fx-border-radius: 5px; -fx-padding: 8px;";
    }
    
    private StudentPasswordPair extractStudentWithPasswordFromForm(GridPane grid) {
        TextField studentIdField = (TextField) grid.getChildren().get(1);
        TextField firstNameField = (TextField) grid.getChildren().get(3);
        TextField lastNameField = (TextField) grid.getChildren().get(5);
        TextField emailField = (TextField) grid.getChildren().get(7);
        TextField dobField = (TextField) grid.getChildren().get(9);
        TextField majorField = (TextField) grid.getChildren().get(11);
        PasswordField passwordField = (PasswordField) grid.getChildren().get(13);
        
        // Validate inputs
        if (firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            dobField.getText().trim().isEmpty() ||
            majorField.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields!");
            return null;
        }
        
        // For new students, password is required
        boolean isNewStudent = studentIdField.getText().equals(studentDao.generateNextStudentId());
        if (isNewStudent && passwordField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please set a password for the new student!");
            return null;
        }
        
        String email = emailField.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address!");
            return null;
        }
        
        try {
            Date.valueOf(dobField.getText().trim());
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter date in YYYY-MM-DD format!");
            return null;
        }
        
        Student student = new Student();
        student.setStudentId(studentIdField.getText().trim());
        student.setFirstName(firstNameField.getText().trim());
        student.setLastName(lastNameField.getText().trim());
        student.setEmail(emailField.getText().trim());
        student.setDateOfBirth(Date.valueOf(dobField.getText().trim()));
        student.setMajor(majorField.getText().trim());
        
        String password = passwordField.getText().trim();
        
        return new StudentPasswordPair(student, password);
    }
    
    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }
    
    private void loadStudentData() {
        studentDao.loadAllStudents();
        updateStatus("Ready - " + studentTable.getItems().size() + " students loaded", "info");
    }
    
    private void deleteStudent() {
        UserSession session = UserSession.getInstance();
        if (session.isStudent() || session.isInstructor()) {
            showAlert(Alert.AlertType.WARNING, "Access Denied", "Only administrators can delete student records!");
            return;
        }
        
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) return;
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Student");
        confirmation.setContentText("Are you sure you want to delete:\n\n" +
            "• Name: " + selectedStudent.getFirstName() + " " + selectedStudent.getLastName() + "\n" +
            "• ID: " + selectedStudent.getStudentId() + "\n" +
            "• Major: " + selectedStudent.getMajor() + "\n\nThis action cannot be undone.");
        
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #1976d2; -fx-border-width: 2px;");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = studentDao.deleteStudent(selectedStudent.getStudentId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully!");
                updateStatus("Student deleted successfully", "success");
                loadStudentData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student!");
                updateStatus("Failed to delete student", "error");
            }
        }
    }
    
    private void showChangePasswordDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change Your Password");
        
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #bbdefb; -fx-background-radius: 10px;");
        
        PasswordField currentPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        
        for (PasswordField field : new PasswordField[]{currentPasswordField, newPasswordField, confirmPasswordField}) {
            field.setStyle(createFieldStyle());
        }
        
        grid.add(createStyledLabel("Current Password:", "-fx-text-fill: #0d47a1; -fx-font-weight: bold;"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(createStyledLabel("New Password:", "-fx-text-fill: #0d47a1; -fx-font-weight: bold;"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(createStyledLabel("Confirm Password:", "-fx-text-fill: #0d47a1; -fx-font-weight: bold;"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #e3f2fd;");
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                if (newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    boolean success = userDao.changePassword(
                        UserSession.getInstance().getUserId(), 
                        newPasswordField.getText()
                    );
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
                        return true;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to change password!");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match!");
                }
            }
            return false;
        });
        
        dialog.showAndWait();
    }
    
    private void logout() {
        UserSession.getInstance().clearSession();
        LoginController loginController = new LoginController(primaryStage);
        loginController.showLoginScreen();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        String color = alertType == Alert.AlertType.ERROR ? "#d32f2f" : 
                      alertType == Alert.AlertType.WARNING ? "#ff9800" : "#1976d2";
        
        dialogPane.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: " + color + "; -fx-border-width: 2px;");
        
        alert.showAndWait();
    }
}