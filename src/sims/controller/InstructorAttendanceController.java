package sims.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sims.dao.AttendanceDao;
import sims.dao.CourseDao;
import sims.model.Attendance;
import sims.model.Course;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;

import java.util.List;

public class InstructorAttendanceController {
    private AttendanceDao attendanceDao;
    private CourseDao courseDao;
    private String instructorId;
    private ObservableList<Attendance> attendanceData;
    private TableView<Attendance> attendanceTable;
    private Stage primaryStage;
    
    // Constructor with stage for full screen support
    public InstructorAttendanceController(Stage primaryStage, String instructorId) {
        this.primaryStage = primaryStage;
        this.attendanceDao = new AttendanceDao();
        this.courseDao = new CourseDao();
        this.instructorId = instructorId;
        this.attendanceData = FXCollections.observableArrayList();
    }
    
    // Original constructor for backward compatibility
    public InstructorAttendanceController(String instructorId) {
        this.attendanceDao = new AttendanceDao();
        this.courseDao = new CourseDao();
        this.instructorId = instructorId;
        this.attendanceData = FXCollections.observableArrayList();
    }
    
    // New method to show full screen attendance management
    public void showAttendanceManagementView() {
        // Create main layout with background
        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        
        // Create attendance content
        VBox attendanceContent = createAttendanceContent();
        root.setCenter(attendanceContent);
        
        // Create scene and configure stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("Manage Attendance - Instructor");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(false);
        primaryStage.show();
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
            return createFallbackBackground();
        }
    }
    
    private Background createFallbackBackground() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0d1b2a")),
            new Stop(0.3, Color.web("#1b263b")),
            new Stop(0.7, Color.web("#415a77")),
            new Stop(1, Color.web("#1b263b"))
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
    
    private VBox createAttendanceContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.85);" +
            "-fx-background-radius: 15px;" +
            "-fx-border-radius: 15px;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(13,27,42,0.4), 20, 0, 0, 8);"
        );
        
        // Header section with title and back button
        HBox headerBox = new HBox();
        headerBox.setSpacing(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Manage Student Attendance");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1565c0;");
        
        // Back to Dashboard button
        Button backButton = createBackButton();
        
        // Make the header box fill the width and push title to left, button to right
        HBox.setHgrow(title, Priority.ALWAYS);
        headerBox.getChildren().addAll(title, backButton);
        
        // Controls section
        VBox controlsSection = new VBox(15);
        controlsSection.setPadding(new Insets(15));
        controlsSection.setStyle("-fx-background-color: rgba(227, 242, 253, 0.7); -fx-background-radius: 10px;");
        
        // Course selection
        HBox courseBox = new HBox(10);
        courseBox.setAlignment(Pos.CENTER_LEFT);
        
        Label courseLabel = new Label("Course:");
        courseLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d47a1;");
        ComboBox<Course> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select a course...");
        courseComboBox.setPrefWidth(300);
        courseComboBox.setStyle("-fx-background-color: white; -fx-border-color: #64b5f6;");
        
        courseBox.getChildren().addAll(courseLabel, courseComboBox);
        
        // Week selection
        HBox weekBox = new HBox(10);
        weekBox.setAlignment(Pos.CENTER_LEFT);
        
        Label weekLabel = new Label("Week:");
        weekLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d47a1;");
        ComboBox<Integer> weekComboBox = new ComboBox<>();
        weekComboBox.setPromptText("Select week...");
        weekComboBox.setPrefWidth(120);
        weekComboBox.setStyle("-fx-background-color: white; -fx-border-color: #64b5f6;");
        
        for (int i = 1; i <= 13; i++) {
            weekComboBox.getItems().add(i);
        }
        
        weekBox.getChildren().addAll(weekLabel, weekComboBox);
        
        // Save button
        Button saveButton = createStyledButton("Save Attendance", "#1976d2");
        
        controlsSection.getChildren().addAll(courseBox, weekBox, saveButton);
        
        // Attendance table with new styling
        attendanceTable = createAttendanceTable();
        attendanceTable.setItems(attendanceData);
        
        // Create a container for the table to ensure it fills available space
        VBox tableContainer = new VBox();
        tableContainer.getChildren().add(attendanceTable);
        VBox.setVgrow(attendanceTable, Priority.ALWAYS);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        
        // Load instructor's courses
        loadInstructorCourses(courseComboBox);
        
        // Auto-load when course or week is selected
        courseComboBox.setOnAction(e -> {
            if (courseComboBox.getValue() != null && weekComboBox.getValue() != null) {
                loadStudentsForAttendance(courseComboBox.getValue().getCourseId(), weekComboBox.getValue());
            }
        });
        
        weekComboBox.setOnAction(e -> {
            if (courseComboBox.getValue() != null && weekComboBox.getValue() != null) {
                loadStudentsForAttendance(courseComboBox.getValue().getCourseId(), weekComboBox.getValue());
            }
        });
        
        // Auto-select first course and week if available
        if (courseComboBox.getItems().size() > 0) {
            courseComboBox.getSelectionModel().selectFirst();
        }
        if (weekComboBox.getItems().size() > 0) {
            weekComboBox.getSelectionModel().selectFirst();
        }
        
        // Save button action
        saveButton.setOnAction(e -> {
            if (courseComboBox.getValue() != null && weekComboBox.getValue() != null) {
                saveAttendance();
            } else {
                showAlert("Please select both course and week.");
            }
        });
        
        mainLayout.getChildren().addAll(headerBox, controlsSection, tableContainer);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        
        return mainLayout;
    }
    
    private Button createBackButton() {
        Button backButton = new Button("← Back to Instructor Dashboard");
        backButton.setStyle(
            "-fx-background-color: #1976d2;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        
        backButton.setOnMouseEntered(e -> backButton.setStyle(
            "-fx-background-color: #1565c0;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;"
        ));
        
        backButton.setOnMouseExited(e -> backButton.setStyle(
            "-fx-background-color: #1976d2;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;"
        ));
        
        backButton.setOnAction(e -> backToDashboard());
        return backButton;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + color + ", -20%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;"
        ));
        
        return button;
    }
    
    private void backToDashboard() {
        if (primaryStage != null) {
            // Close current attendance management stage
            primaryStage.close();
            
            // Create and show a new Instructor Dashboard
            Stage dashboardStage = new Stage();
            InstructorDashboardController dashboardController = new InstructorDashboardController(dashboardStage);
            dashboardController.showInstructorDashboard();
        } else {
            // Fallback: close the current window if no stage reference
            Stage currentStage = (Stage) attendanceTable.getScene().getWindow();
            currentStage.close();
        }
    }
    
    // Updated table creation with CourseController styling
    private TableView<Attendance> createAttendanceTable() {
        TableView<Attendance> table = new TableView<>();
        table.setPlaceholder(new Label("Select a course and week to view students"));
        
        // Apply the same table styling as CourseController
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        // Make table fill available width
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns with the same styling
        TableColumn<Attendance, String> studentIdCol = createStyledColumn("STUDENT ID", "studentId", 150);
        TableColumn<Attendance, String> studentNameCol = createStyledColumn("STUDENT NAME", "studentName", 300);
        TableColumn<Attendance, String> statusCol = createStyledColumn("ATTENDANCE STATUS", "status", 300);
        
        // Radio buttons for attendance status with updated styling
        statusCol.setCellFactory(col -> new TableCell<Attendance, String>() {
            private final ToggleGroup toggleGroup = new ToggleGroup();
            private final RadioButton presentBtn = new RadioButton("✅ Present");
            private final RadioButton absentBtn = new RadioButton("❌ Absent");
            private final HBox hbox = new HBox(15, presentBtn, absentBtn);
            
            {
                hbox.setAlignment(javafx.geometry.Pos.CENTER);
                presentBtn.setToggleGroup(toggleGroup);
                absentBtn.setToggleGroup(toggleGroup);
                presentBtn.setUserData("present");
                absentBtn.setUserData("absent");
                
                // Style radio buttons to match the table theme
                presentBtn.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 12px;");
                absentBtn.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 12px;");
                
                toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                    if (newToggle != null && getTableRow() != null) {
                        Attendance attendance = getTableView().getItems().get(getIndex());
                        if (attendance != null) {
                            attendance.setStatus(newToggle.getUserData().toString());
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: white;");
                } else {
                    Attendance attendance = getTableView().getItems().get(getIndex());
                    if (attendance != null) {
                        if ("present".equals(attendance.getStatus())) {
                            presentBtn.setSelected(true);
                        } else {
                            absentBtn.setSelected(true);
                        }
                        setGraphic(hbox);
                        setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 8px;");
                    } else {
                        setGraphic(null);
                        setStyle("-fx-background-color: white;");
                    }
                }
            }
        });
        
        table.getColumns().addAll(studentIdCol, studentNameCol, statusCol);

        // Apply the same row factory styling
        table.setRowFactory(tv -> new TableRow<Attendance>() {
            @Override
            protected void updateItem(Attendance item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50;");
                }
            }
        });
        
        return table;
    }
    
    private <T> TableColumn<Attendance, T> createStyledColumn(String title, String property, double width) {
        TableColumn<Attendance, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setMinWidth(80); // Set minimum width to prevent columns from becoming too narrow
        
        // Apply the same column header styling
        column.setStyle(
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-background-color: #1976d2;" + // Blue header
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #bbdefb;"
        );
        
        // Apply the same cell styling
        column.setCellFactory(tc -> new TableCell<Attendance, T>() {
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
    
    private void loadInstructorCourses(ComboBox<Course> comboBox) {
        try {
            List<Course> courses = courseDao.getCoursesByInstructor(instructorId);
            if (courses != null && !courses.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList(courses));
            } else {
                comboBox.setPromptText("No courses assigned");
                comboBox.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading courses: " + e.getMessage());
            comboBox.setPromptText("Error loading courses");
            comboBox.setDisable(true);
        }
    }
    
    private void loadStudentsForAttendance(String courseId, int weekNumber) {
        try {
            List<Attendance> students = attendanceDao.getStudentsForAttendance(courseId, weekNumber, instructorId);
            attendanceData.clear();
            if (students != null && !students.isEmpty()) {
                attendanceData.addAll(students);
                attendanceTable.setPlaceholder(new Label("No students found for this course"));
            } else {
                Label placeholder = new Label("No students enrolled in this course\n\nStudents will appear here once they enroll");
                attendanceTable.setPlaceholder(placeholder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label placeholder = new Label("Error loading students\n\nPlease try again later");
            attendanceTable.setPlaceholder(placeholder);
            showAlert("Error loading students: " + e.getMessage());
        }
    }
    
    private void saveAttendance() {
        if (attendanceData == null || attendanceData.isEmpty()) {
            showAlert("No attendance data to save.");
            return;
        }
        
        try {
            boolean success = attendanceDao.markMultipleAttendance(attendanceData);
            if (success) {
                showAlert("Attendance saved successfully for " + attendanceData.size() + " students!");
            } else {
                showAlert("Error saving attendance. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error saving attendance: " + e.getMessage());
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attendance Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Keep the old method for compatibility if needed
    public VBox createAttendanceManagementView() {
        return createAttendanceContent();
    }
}