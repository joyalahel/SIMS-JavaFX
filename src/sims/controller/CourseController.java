package sims.controller;

import sims.UserSession;
import sims.dao.CourseDao;
import sims.model.Course;
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

import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public class CourseController {
    private Stage primaryStage;
    private TableView<Course> courseTable;
    private CourseDao courseDao;
    
    private Button btnAdd;
    private Button btnEdit;
    private Button btnDelete;
    private Button btnBack;
    private Button btnManageEnrollments;

    private Label lblStatus;
    
    public CourseController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.courseDao = new CourseDao();
    }
    
public void showCourseDashboard() {
    // Create a new Stage to ensure clean state
    Stage courseStage = new Stage();
    
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    lblStatus = new Label("Loading...");
    lblStatus.setStyle("-fx-text-fill: #e3f2fd; -fx-font-size: 12px; -fx-font-weight: bold;");
    
    createStyledButtons();
    setupStyledTable();
    setupEventHandlers();
    
    VBox topSection = createTopSection();
    root.setTop(topSection);
    root.setCenter(createTableContainer());
    
    loadCourseData();
    
    // Create scene without dimensions
    Scene scene = new Scene(root);
    courseStage.setTitle("Course Management - SIMS");
    courseStage.setScene(scene);
    courseStage.setMaximized(true);
    courseStage.setFullScreen(false);
    
    // Close the previous stage
    primaryStage.close();
    
    // Show the new stage
    courseStage.show();
    
    // Update the primaryStage reference
    this.primaryStage = courseStage;
}
    
private Background createBackground() {
    try {
        // Use the same image path as your other controllers
        String imagePath = "file:/C:/Users/Administrator/Downloads/uni.jpg";
        
        Image backgroundImage = new Image(imagePath);
        
        BackgroundImage bgImage = new BackgroundImage(
            backgroundImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        
        return new Background(bgImage);
        
    } catch (Exception e) {
        System.err.println("Error loading background image: " + e.getMessage());
        // Fallback to gradient if image fails to load
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#1e3c72")),
            new Stop(1, Color.web("#2a5298"))
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
}
    
    private VBox createTopSection() {
        UserSession session = UserSession.getInstance();
        
        String titleText;
        if (session.isAdmin()) {
            titleText = "Course Management";
        } else if (session.isInstructor()) {
            titleText = "My Courses";
        } else {
            titleText = "Available Courses";
        }
        
        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: #2196f3; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label welcome = new Label("Welcome, " + session.getFullName());
        welcome.setStyle("-fx-text-fill: #2196f3; -fx-font-size: 14px;");
        
        HBox buttonPanel = new HBox(15);
        buttonPanel.setPadding(new Insets(15, 0, 5, 0));
        buttonPanel.setAlignment(Pos.CENTER);
        
        // Show different buttons based on role
        if (session.isAdmin()) {
            // Admin can manage courses (add, edit, delete)
            buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnBack);
        } else if (session.isInstructor()) {
            // Instructors can only view - show only back button
            buttonPanel.getChildren().addAll(btnBack);
        } else {
            // Students can only view
            buttonPanel.getChildren().addAll(btnBack);
        }
        
        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25));
        topSection.setBackground(createHeaderBackground());
        topSection.getChildren().addAll(title, welcome, buttonPanel, lblStatus);
        
        return topSection;
    }
    
    private Background createHeaderBackground() {
        return new Background(new BackgroundFill(
            Color.rgb(227, 242, 253, 0.95), // Light blue with transparency
            new CornerRadii(0, 0, 20, 20, false),
            Insets.EMPTY
        ));
    }
    
    private void createStyledButtons() {
        btnAdd = createStyledButton("Add Course", "#1565c0");      // Primary Blue
        btnEdit = createStyledButton("Edit Course", "#2196f3");    // Light Blue
        btnDelete = createStyledButton("Delete Course", "#1565c0"); // Dark Blue
        btnBack = createStyledButton("Back to Dashboard", "#2196f3"); // Medium Blue
        
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        
        // Set permissions based on role
        UserSession session = UserSession.getInstance();
        if (session.isAdmin()) {
            // Admin can add, edit, delete courses - ALL BUTTONS ENABLED
            // Edit and Delete will be enabled when a course is selected
        } else if (session.isInstructor()) {
            // Instructors can only view courses (no add/edit/delete)
            btnAdd.setDisable(true);
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
        } else {
            // Students can only view
            btnAdd.setDisable(true);
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
        }
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 2);"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + color + ", -20%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 2);"
        ));
        
        return button;
    }
    
    private void setupStyledTable() {
        courseTable = new TableView<>();
        courseTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        // Make table fill available width
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        courseTable.setItems(courseDao.getCourseData());

        // Create columns with minimum widths
        TableColumn<Course, String> codeCol = createStyledColumn("Course Code", "courseCode", 120);
        TableColumn<Course, String> nameCol = createStyledColumn("Course Name", "courseName", 200);
        TableColumn<Course, String> instructorCol = createStyledColumn("Instructor", "instructorName", 150);
        TableColumn<Course, Integer> creditsCol = createStyledColumn("Credits", "credits", 80);
        TableColumn<Course, String> deptCol = createStyledColumn("Department", "department", 150);
        TableColumn<Course, String> descCol = createStyledColumn("Description", "description", 250);

        courseTable.getColumns().addAll(codeCol, nameCol, instructorCol, creditsCol, deptCol, descCol);

        courseTable.setRowFactory(tv -> new TableRow<Course>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50;");
                }
            }
        });

        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEdit.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
        });
    }
    
    private <T> TableColumn<Course, T> createStyledColumn(String title, String property, double width) {
        TableColumn<Course, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setMinWidth(80); // Set minimum width to prevent columns from becoming too narrow
        
        column.setStyle(
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-background-color: #1976d2;" + // Blue header
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #bbdefb;"
        );
        
        column.setCellFactory(tc -> new TableCell<Course, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: white;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 8px;");
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
        String tableTitleText;
        
        if (session.isAdmin()) {
            tableTitleText = "Course Management - Add, Edit, or Delete Courses";
        } else if (session.isInstructor()) {
            tableTitleText = "Courses You Teach (View Only)";
        } else {
            tableTitleText = "Available Courses (View Only)";
        }
        
        Label tableTitle = new Label(tableTitleText);
        tableTitle.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Make table expand to fill available space
        VBox.setVgrow(courseTable, Priority.ALWAYS);
        courseTable.setMaxHeight(Double.MAX_VALUE);
        
        VBox.setMargin(tableTitle, new Insets(0, 0, 15, 0));
        tableContainer.getChildren().addAll(tableTitle, courseTable);
        
        return tableContainer;
    }
    
    private void setupEventHandlers() {
        btnAdd.setOnAction(e -> showAddCourseDialog());
        btnEdit.setOnAction(e -> showEditCourseDialog());
        btnDelete.setOnAction(e -> deleteCourse());
        btnBack.setOnAction(e -> backToDashboard());
        
        courseTable.getItems().addListener((javafx.collections.ListChangeListener<Course>) change -> {
            updateStatus("Ready - " + courseTable.getItems().size() + " courses loaded");
        });
    }
    
    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }
    
    private void showAddCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.setHeaderText("Enter course details:");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = createCourseForm(null);
        dialog.getDialogPane().setContent(grid);
        
        // Set dialog size
        dialog.getDialogPane().setMinSize(500, 450);
        
        String nextId = courseDao.generateNextCourseId();
        TextField courseIdField = (TextField) grid.getChildren().get(1);
        courseIdField.setText(nextId);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Course course = extractCourseFromForm(grid);
                if (course != null) {
                    return course;
                }
            }
            return null;
        });
        
        Optional<Course> result = dialog.showAndWait();
        
        result.ifPresent(course -> {
            boolean success = courseDao.insertCourse(course);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully! 🎉");
                updateStatus("Course added successfully");
                loadCourseData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add course!");
                updateStatus("Failed to add course");
            }
        });
    }
    
    private void showEditCourseDialog() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a course to edit!");
            return;
        }
        
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit course details:");
        
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        GridPane grid = createCourseForm(selectedCourse);
        dialog.getDialogPane().setContent(grid);
        
        // Set dialog size
        dialog.getDialogPane().setMinSize(500, 450);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                Course course = extractCourseFromForm(grid);
                if (course != null) {
                    course.setCourseId(selectedCourse.getCourseId());
                    return course;
                }
            }
            return null;
        });
        
        Optional<Course> result = dialog.showAndWait();
        
        result.ifPresent(course -> {
            boolean success = courseDao.updateCourse(course);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully!");
                updateStatus("Course updated successfully");
                loadCourseData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update course!");
                updateStatus("Failed to update course");
            }
        });
    }
    
    private GridPane createCourseForm(Course course) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 10px;"); // Light blue background
        
        TextField courseIdField = new TextField();
        courseIdField.setEditable(false);
        courseIdField.setStyle("-fx-background-color: white; -fx-border-color: #64b5f6; -fx-border-radius: 5px; -fx-padding: 8px;");
        
        TextField courseNameField = new TextField();
        TextField courseCodeField = new TextField();
        ComboBox<String> instructorComboBox = new ComboBox<>();
        TextField creditsField = new TextField();
        TextField departmentField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        
        // Style fields
        for (TextField field : new TextField[]{courseNameField, courseCodeField, creditsField, departmentField}) {
            field.setStyle("-fx-background-color: white; -fx-border-color: #64b5f6; -fx-border-radius: 5px; -fx-padding: 8px;");
        }
        descriptionArea.setStyle("-fx-background-color: white; -fx-border-color: #64b5f6; -fx-border-radius: 5px;");
        
        // Load instructors for combo box
        List<Instructor> instructors = courseDao.getAllInstructors();
        ObservableList<String> instructorOptions = FXCollections.observableArrayList();
        for (Instructor instructor : instructors) {
            instructorOptions.add(instructor.getInstructorId() + " - " + instructor.getFirstName() + " " + instructor.getLastName());
        }
        instructorComboBox.setItems(instructorOptions);
        instructorComboBox.setStyle("-fx-background-color: white; -fx-border-color: #64b5f6; -fx-border-radius: 5px;");
        
        if (course != null) {
            courseIdField.setText(course.getCourseId());
            courseNameField.setText(course.getCourseName());
            courseCodeField.setText(course.getCourseCode());
            creditsField.setText(String.valueOf(course.getCredits()));
            departmentField.setText(course.getDepartment());
            descriptionArea.setText(course.getDescription());
            
            // Set instructor in combo box
            for (String option : instructorOptions) {
                if (option.startsWith(course.getInstructorId())) {
                    instructorComboBox.setValue(option);
                    break;
                }
            }
        }
        
        String labelStyle = "-fx-text-fill: #0d47a1; -fx-font-weight: bold; -fx-font-size: 13px;"; // Dark blue text
        
        grid.add(createStyledLabel("Course ID:", labelStyle), 0, 0);
        grid.add(courseIdField, 1, 0);
        grid.add(createStyledLabel("Course Name:", labelStyle), 0, 1);
        grid.add(courseNameField, 1, 1);
        grid.add(createStyledLabel("Course Code:", labelStyle), 0, 2);
        grid.add(courseCodeField, 1, 2);
        grid.add(createStyledLabel("Instructor:", labelStyle), 0, 3);
        grid.add(instructorComboBox, 1, 3);
        grid.add(createStyledLabel("Credits:", labelStyle), 0, 4);
        grid.add(creditsField, 1, 4);
        grid.add(createStyledLabel("Department:", labelStyle), 0, 5);
        grid.add(departmentField, 1, 5);
        grid.add(createStyledLabel("Description:", labelStyle), 0, 6);
        grid.add(descriptionArea, 1, 6);
        
        return grid;
    }
    
    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }
    
    private Course extractCourseFromForm(GridPane grid) {
        TextField courseIdField = (TextField) grid.getChildren().get(1);
        TextField courseNameField = (TextField) grid.getChildren().get(3);
        TextField courseCodeField = (TextField) grid.getChildren().get(5);
        ComboBox<String> instructorComboBox = (ComboBox<String>) grid.getChildren().get(7);
        TextField creditsField = (TextField) grid.getChildren().get(9);
        TextField departmentField = (TextField) grid.getChildren().get(11);
        TextArea descriptionArea = (TextArea) grid.getChildren().get(13);
        
        if (courseNameField.getText().trim().isEmpty() ||
            courseCodeField.getText().trim().isEmpty() ||
            instructorComboBox.getValue() == null ||
            creditsField.getText().trim().isEmpty() ||
            departmentField.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields!");
            return null;
        }
        
        try {
            Integer.parseInt(creditsField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Credits", "Please enter a valid number for credits!");
            return null;
        }
        
        Course course = new Course();
        
        course.setCourseId(courseIdField.getText().trim());
        course.setCourseName(courseNameField.getText().trim());
        course.setCourseCode(courseCodeField.getText().trim().toUpperCase());
        
        // Extract instructor ID from combo box (format: "I001 - John Doe")
        String instructorValue = instructorComboBox.getValue();
        String instructorId = instructorValue.split(" - ")[0];
        course.setInstructorId(instructorId);
        
        course.setCredits(Integer.parseInt(creditsField.getText().trim()));
        course.setDepartment(departmentField.getText().trim());
        course.setDescription(descriptionArea.getText().trim());
        
        return course;
    }
    
    private void loadCourseData() {
        UserSession session = UserSession.getInstance();
        
        if (session.isInstructor()) {
            // Instructors only see courses they teach - use ObservableList directly
            String instructorId = session.getUsername().toUpperCase();
            ObservableList<Course> instructorCourses = courseDao.getCoursesByInstructor(instructorId);
            courseTable.setItems(instructorCourses);
            updateStatus("Ready - " + instructorCourses.size() + " of your courses loaded");
        } else {
            // Admin and students see all courses - connect to the shared ObservableList
            courseTable.setItems(courseDao.getCourseData());
            courseDao.loadAllCourses();
            updateStatus("Ready - " + courseDao.getCourseData().size() + " courses loaded");
        }
    }
    
    private void deleteCourse() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) return;
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Course");
        confirmation.setContentText("Are you sure you want to delete:\n\n" +
            "• Course: " + selectedCourse.getCourseName() + "\n" +
            "• Code: " + selectedCourse.getCourseCode() + "\n" +
            "• Instructor: " + selectedCourse.getInstructorName() + "\n\nThis action cannot be undone.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = courseDao.deleteCourse(selectedCourse.getCourseId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully!");
                updateStatus("Course deleted successfully");
                loadCourseData(); // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete course!");
            }
        }
    }
    
 private void backToDashboard() {
    UserSession session = UserSession.getInstance();
    
    // Close the current course management window
    primaryStage.close();
    
    if (session.isAdmin()) {
        // Create and show a new Admin Dashboard
        Stage adminStage = new Stage();
        AdminDashboardController adminController = new AdminDashboardController(adminStage);
        adminController.showAdminDashboard();
    } else if (session.isInstructor()) {
        // Create and show a new Instructor Dashboard
        Stage instructorStage = new Stage();
        InstructorDashboardController instructorController = new InstructorDashboardController(instructorStage);
        instructorController.showInstructorDashboard();
    } else {
        // Create and show a new Student Dashboard
        Stage studentStage = new Stage();
        DashboardController dashboardController = new DashboardController(studentStage);
        dashboardController.showDashboard();
    }
}
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}