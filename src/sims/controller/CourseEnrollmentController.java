package sims.controller;

import sims.UserSession;
import sims.dao.CourseDao;
import sims.dao.EnrollmentDao;
import sims.model.Course;
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

import java.util.Optional;

public class CourseEnrollmentController {
    private Stage primaryStage;
    private TableView<Course> courseTable;
    private CourseDao courseDao;
    private EnrollmentDao enrollmentDao;
    private String studentId;
    
    private Button btnEnroll;
    private Button btnBack;
    
    private Label lblStatus;
    
    public CourseEnrollmentController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.courseDao = new CourseDao();
        this.enrollmentDao = new EnrollmentDao();
        this.studentId = UserSession.getInstance().getUsername().toUpperCase();
    }
    
 public void showCourseEnrollment() {
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    lblStatus = new Label("Loading available courses...");
    lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
    
    createStyledButtons();
    setupStyledTable();
    setupEventHandlers();
    
    VBox topSection = createTopSection();
    root.setTop(topSection);
    root.setCenter(createTableContainer());
    
    loadAvailableCourses();
    
    // Remove explicit dimensions and add full screen setup
    Scene scene = new Scene(root);
    primaryStage.setTitle("Course Enrollment - SIMS");
    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
    primaryStage.setFullScreen(false);
    primaryStage.show();
}
    
    private Background createBackground() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#1e3c72")),
            new Stop(1, Color.web("#2a5298"))
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
    
    private VBox createTopSection() {
        UserSession session = UserSession.getInstance();
        
        Label title = new Label("Course Enrollment");
        title.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label welcome = new Label("Welcome, " + session.getFullName() + " - Browse and enroll in courses");
        welcome.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px;");
        
        HBox buttonPanel = new HBox(15);
        buttonPanel.setPadding(new Insets(15, 0, 5, 0));
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(btnEnroll, btnBack);
        
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
        btnEnroll = createStyledButton("Enroll in Course", "#1976d2");     // Primary Blue
        btnBack = createStyledButton("Back to Dashboard", "#42a5f5");      // Medium Blue
        
        btnEnroll.setDisable(true);
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
                    // Check if student is already enrolled
                    boolean isEnrolled = enrollmentDao.isStudentEnrolled(studentId, item.getCourseId());
                    if (isEnrolled) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #2c3e50;"); // Light blue for enrolled
                    } else {
                        // Alternate row colors for better readability
                        if (getIndex() % 2 == 0) {
                            setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50;");
                        } else {
                            setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #2c3e50;");
                        }
                    }
                }
            }
        });

        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            if (hasSelection) {
                // Check if already enrolled
                boolean isEnrolled = enrollmentDao.isStudentEnrolled(studentId, newSel.getCourseId());
                btnEnroll.setDisable(isEnrolled);
                if (isEnrolled) {
                    updateStatus("You are already enrolled in this course");
                } else {
                    updateStatus("Ready to enroll in: " + newSel.getCourseName());
                }
            } else {
                btnEnroll.setDisable(true);
            }
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
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 8px;");
                }
            }
        });
        
        return column;
    }
    
    private VBox createTableContainer() {
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(20));
        tableContainer.setBackground(Background.EMPTY);
        
        Label tableTitle = new Label("Available Courses - Select a course to enroll");
        tableTitle.setStyle("-fx-text-fill: #e3f2fd; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Make table expand to fill available space
        VBox.setVgrow(courseTable, Priority.ALWAYS);
        courseTable.setMaxHeight(Double.MAX_VALUE);
        
        VBox.setMargin(tableTitle, new Insets(0, 0, 15, 0));
        tableContainer.getChildren().addAll(tableTitle, courseTable);
        
        return tableContainer;
    }
    
    private void setupEventHandlers() {
        btnEnroll.setOnAction(e -> enrollInCourse());
        btnBack.setOnAction(e -> backToDashboard());
    }
    
    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }
    
    private void loadAvailableCourses() {
        // Connect to the course data and load all courses
        courseTable.setItems(courseDao.getCourseData());
        courseDao.loadAllCourses();
        updateStatus("Ready - " + courseTable.getItems().size() + " courses available");
    }
    
    private void enrollInCourse() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) return;
        
        // Confirm enrollment
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Enrollment");
        confirmation.setHeaderText("🎯 Enroll in Course");
        confirmation.setContentText("Are you sure you want to enroll in:\n\n" +
            "• Course: " + selectedCourse.getCourseName() + "\n" +
            "• Code: " + selectedCourse.getCourseCode() + "\n" +
            "• Instructor: " + selectedCourse.getInstructorName() + "\n" +
            "• Credits: " + selectedCourse.getCredits() + "\n\n" +
            "This enrollment will be recorded in your academic record.");
        
        // Style the confirmation dialog
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #e3f2fd;" +
            "-fx-border-color: #1976d2;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;"
        );
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = enrollmentDao.enrollStudent(studentId, selectedCourse.getCourseId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Enrollment Successful", 
                         "You have been successfully enrolled in " + selectedCourse.getCourseName() + "! ✅\n\n" +
                         "Course: " + selectedCourse.getCourseCode() + " - " + selectedCourse.getCourseName() + "\n" +
                         "Instructor: " + selectedCourse.getInstructorName() + "\n" +
                         "You can now view this course in 'My Enrollments'.");
                
                // Refresh the table to show enrollment status
                courseTable.refresh();
                btnEnroll.setDisable(true);
                updateStatus("Enrolled in " + selectedCourse.getCourseName());
            } else {
                showAlert(Alert.AlertType.ERROR, "Enrollment Failed", 
                         "Failed to enroll in " + selectedCourse.getCourseName() + "!\n\n" +
                         "You may already be enrolled in this course or there was a system error.");
            }
        }
    }
    
    private void backToDashboard() {
        StudentDashboardController studentController = new StudentDashboardController(primaryStage);
        studentController.showStudentDashboard();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert with blue theme
        DialogPane dialogPane = alert.getDialogPane();
        String color = alertType == Alert.AlertType.ERROR ? "#d32f2f" : 
                      alertType == Alert.AlertType.WARNING ? "#ff9800" : "#1976d2";
        
        dialogPane.setStyle(
            "-fx-background-color: #e3f2fd;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;"
        );
        
        alert.showAndWait();
    }
}