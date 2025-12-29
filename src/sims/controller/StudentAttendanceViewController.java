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
import sims.dao.EnrollmentDao;
import sims.model.Attendance;
import sims.model.Enrollment;
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

public class StudentAttendanceViewController {
    private AttendanceDao attendanceDao;
    private EnrollmentDao enrollmentDao;
    private String studentId;
    private ObservableList<Attendance> attendanceData;
    private TableView<Attendance> attendanceTable;
    private Label statsLabel;
    private Stage primaryStage;
    
    // Constructor for use with stage
    public StudentAttendanceViewController(Stage primaryStage, String studentId) {
        this.primaryStage = primaryStage;
        this.attendanceDao = new AttendanceDao();
        this.enrollmentDao = new EnrollmentDao();
        this.studentId = studentId;
        this.attendanceData = FXCollections.observableArrayList();
    }
    
    // Original constructor for backward compatibility
    public StudentAttendanceViewController(String studentId) {
        this.attendanceDao = new AttendanceDao();
        this.enrollmentDao = new EnrollmentDao();
        this.studentId = studentId;
        this.attendanceData = FXCollections.observableArrayList();
    }
    
    // New method to show full screen attendance view
    public void showStudentAttendanceView() {
        // Create main layout with background
        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        
        // Create attendance content
        VBox attendanceContent = createAttendanceContent();
        root.setCenter(attendanceContent);
        
        // Create scene and configure stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("My Attendance - Student");
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
        "-fx-background-color: rgba(255, 255, 255, 0.90);" +
        "-fx-background-radius: 15px;" +
        "-fx-border-radius: 15px;" +
        "-fx-border-color: #1976d2;" +
        "-fx-border-width: 2px;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.3), 20, 0, 0, 8);"
    );
    
    // Header section with title and back button
    HBox headerBox = new HBox();
    headerBox.setSpacing(20);
    headerBox.setAlignment(Pos.CENTER_LEFT);
    
    Label title = new Label("My Attendance");
    title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #0d47a1; " +
                  "-fx-effect: dropshadow(three-pass-box, rgba(13,71,161,0.3), 4, 0, 0, 2);");
    
    // Back to Dashboard button
    Button backButton = createBackButton();
    
    // Make the header box fill the width and push title to left, button to right
    HBox.setHgrow(title, Priority.ALWAYS);
    headerBox.getChildren().addAll(title, backButton);
    
    // Course selection
    HBox controlsBox = new HBox(15);
    controlsBox.setAlignment(Pos.CENTER_LEFT);
    controlsBox.setPadding(new Insets(20));
    controlsBox.setStyle("-fx-background-color: rgba(227, 242, 253, 0.9); -fx-background-radius: 12px; " +
                        "-fx-border-color: #64b5f6; -fx-border-width: 1px; -fx-border-radius: 12px;");
    
    Label courseLabel = new Label("Select Course:");
    courseLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565c0; -fx-font-size: 14px;");
    
    ComboBox<Enrollment> courseComboBox = new ComboBox<>();
    courseComboBox.setPromptText("Select Course");
    courseComboBox.setPrefWidth(350);
    courseComboBox.setStyle("-fx-background-color: white; -fx-border-color: #42a5f5; -fx-border-radius: 6px; " +
                           "-fx-font-size: 13px; -fx-padding: 8px;");
    
    controlsBox.getChildren().addAll(courseLabel, courseComboBox);
    
    // Statistics
    statsLabel = new Label("Select a course to view attendance");
    statsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1976d2; " +
                       "-fx-background-color: rgba(187, 222, 251, 0.7); -fx-background-radius: 8px; " +
                       "-fx-padding: 12px; -fx-alignment: center;");
    statsLabel.setMaxWidth(Double.MAX_VALUE);
    statsLabel.setAlignment(Pos.CENTER);
    
    // Attendance table
    attendanceTable = createAttendanceTable();
    attendanceTable.setItems(attendanceData);
    
    // Create a container for the table to ensure it fills available space
    VBox tableContainer = new VBox();
    tableContainer.getChildren().add(attendanceTable);
    VBox.setVgrow(attendanceTable, Priority.ALWAYS);
    VBox.setVgrow(tableContainer, Priority.ALWAYS);
    
    // Load student's enrolled courses
    loadStudentCourses(courseComboBox);
    
    // Auto-load when course is selected
    courseComboBox.setOnAction(e -> {
        if (courseComboBox.getValue() != null) {
            String courseId = courseComboBox.getValue().getCourseId();
            loadAttendanceData(courseId);
            showAttendanceStatistics(courseId);
        }
    });
    
    // Load first course automatically if available
    if (courseComboBox.getItems().size() > 0) {
        courseComboBox.getSelectionModel().selectFirst();
    }
    
    mainLayout.getChildren().addAll(headerBox, controlsBox, statsLabel, tableContainer);
    VBox.setVgrow(tableContainer, Priority.ALWAYS);
    
    return mainLayout;
}

private Button createBackButton() {
    Button backButton = new Button("← Back to Student Dashboard");
    backButton.setStyle(
        "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 12px 24px;" +
        "-fx-background-radius: 8px;" +
        "-fx-border-radius: 8px;" +
        "-fx-border-color: #0d47a1;" +
        "-fx-border-width: 1px;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(13,71,161,0.4), 6, 0, 0, 3);" +
        "-fx-cursor: hand;"
    );
    
    backButton.setOnMouseEntered(e -> backButton.setStyle(
        "-fx-background-color: linear-gradient(to bottom, #1565c0, #0d47a1);" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 12px 24px;" +
        "-fx-background-radius: 8px;" +
        "-fx-border-radius: 8px;" +
        "-fx-border-color: #0d47a1;" +
        "-fx-border-width: 1px;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(13,71,161,0.6), 8, 0, 0, 4);" +
        "-fx-cursor: hand;"
    ));
    
    backButton.setOnMouseExited(e -> backButton.setStyle(
        "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 12px 24px;" +
        "-fx-background-radius: 8px;" +
        "-fx-border-radius: 8px;" +
        "-fx-border-color: #0d47a1;" +
        "-fx-border-width: 1px;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(13,71,161,0.4), 6, 0, 0, 3);" +
        "-fx-cursor: hand;"
    ));
    
    backButton.setOnAction(e -> backToDashboard());
    return backButton;
}

    private void backToDashboard() {
    if (primaryStage != null) {
        // Close current attendance stage
        primaryStage.close();
        
        // Create and show a new Student Dashboard
        Stage dashboardStage = new Stage();
        StudentDashboardController dashboardController = new StudentDashboardController(dashboardStage);
        dashboardController.showStudentDashboard();
    } else {
        // Fallback: close the current window if no stage reference
        Stage currentStage = (Stage) attendanceTable.getScene().getWindow();
        currentStage.close();
        
        // Create and show a new Student Dashboard
        Stage dashboardStage = new Stage();
        StudentDashboardController dashboardController = new StudentDashboardController(dashboardStage);
        dashboardController.showStudentDashboard();
    }
}
    
private TableView<Attendance> createAttendanceTable() {
    TableView<Attendance> table = new TableView<>();
    table.setPlaceholder(new Label("Select a course to view attendance records"));
    
    // Remove fixed column widths and let them expand
    TableColumn<Attendance, Integer> weekCol = new TableColumn<>("Week");
    weekCol.setCellValueFactory(new PropertyValueFactory<>("weekNumber"));
    weekCol.setStyle("-fx-alignment: CENTER;");
    
    TableColumn<Attendance, String> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
    dateCol.setStyle("-fx-alignment: CENTER;");
    
    TableColumn<Attendance, String> timeCol = new TableColumn<>("Time");
    timeCol.setCellValueFactory(new PropertyValueFactory<>("attendanceTime"));
    timeCol.setStyle("-fx-alignment: CENTER;");
    
    TableColumn<Attendance, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setStyle("-fx-alignment: CENTER;");
    
    TableColumn<Attendance, String> courseCol = new TableColumn<>("Course");
    courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
    
    // Style table headers with blue theme
    String headerStyle = "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; " +
                        "-fx-alignment: CENTER; -fx-border-color: #bbdefb;";
    
    weekCol.setStyle(headerStyle);
    dateCol.setStyle(headerStyle);
    timeCol.setStyle(headerStyle);
    statusCol.setStyle(headerStyle);
    courseCol.setStyle(headerStyle);
    
    // Style status column based on value
    statusCol.setCellFactory(column -> new TableCell<Attendance, String>() {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER;");
            } else {
                setText(status.toUpperCase());
                if ("present".equalsIgnoreCase(status)) {
                    setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-background-color: #e8f5e8; " +
                            "-fx-alignment: CENTER; -fx-border-radius: 4px; -fx-background-radius: 4px;");
                } else if ("absent".equalsIgnoreCase(status)) {
                    setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-background-color: #ffebee; " +
                            "-fx-alignment: CENTER; -fx-border-radius: 4px; -fx-background-radius: 4px;");
                } else {
                    setStyle("-fx-text-fill: #757575; -fx-font-weight: bold; -fx-background-color: #f5f5f5; " +
                            "-fx-alignment: CENTER; -fx-border-radius: 4px; -fx-background-radius: 4px;");
                }
            }
        }
    });
    
    // Style other cells
    String cellStyle = "-fx-alignment: CENTER-LEFT; -fx-font-size: 12px; -fx-padding: 10px; " +
                      "-fx-background-color: transparent;";
    
    weekCol.setCellFactory(col -> createStyledTableCell());
    dateCol.setCellFactory(col -> createStyledTableCell());
    timeCol.setCellFactory(col -> createStyledTableCell());
    courseCol.setCellFactory(col -> createStyledTableCell());
    
    table.getColumns().addAll(weekCol, dateCol, timeCol, statusCol, courseCol);
    
    // Set column resize policy to make columns fill available width
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
    // Style the table
    table.setStyle(
        "-fx-background-color: white;" +
        "-fx-border-color: #64b5f6;" +
        "-fx-border-width: 2px;" +
        "-fx-border-radius: 8px;" +
        "-fx-background-radius: 8px;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
    );
    
    // Alternate row coloring
    table.setRowFactory(tv -> new TableRow<Attendance>() {
        @Override
        protected void updateItem(Attendance item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setStyle("");
            } else {
                if (getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: #f8fbff;"); // Very light blue
                } else {
                    setStyle("-fx-background-color: white;");
                }
            }
        }
    });
    
    return table;
}
    
    private void loadStudentCourses(ComboBox<Enrollment> comboBox) {
        try {
            List<Enrollment> enrollments = enrollmentDao.getEnrollmentsByStudent(studentId);
            if (enrollments != null && !enrollments.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList(enrollments));
            } else {
                comboBox.setPromptText("No enrolled courses found");
                comboBox.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading courses: " + e.getMessage());
            comboBox.setPromptText("Error loading courses");
            comboBox.setDisable(true);
        }
    }
    private <T> TableCell<Attendance, T> createStyledTableCell() {
    return new TableCell<Attendance, T>() {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                setText(item.toString());
                setStyle("-fx-background-color: transparent; -fx-text-fill: #1e3c72; -fx-font-size: 12px; " +
                        "-fx-padding: 10px; -fx-alignment: CENTER-LEFT;");
            }
        }
    };
}

    private void loadAttendanceData(String courseId) {
        try {
            List<Attendance> attendance = attendanceDao.getStudentAttendance(studentId, courseId);
            attendanceData.clear();
            if (attendance != null && !attendance.isEmpty()) {
                attendanceData.addAll(attendance);
                attendanceTable.setPlaceholder(new Label("No attendance records found for this course"));
            } else {
                attendanceTable.setPlaceholder(new Label("No attendance records found for this course"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            attendanceTable.setPlaceholder(new Label("Error loading attendance data"));
            showAlert("Error loading attendance data: " + e.getMessage());
        }
    }
    
   private void showAttendanceStatistics(String courseId) {
    try {
        AttendanceDao.AttendanceStatistics stats = attendanceDao.getAttendanceStatistics(courseId, studentId);
        if (stats.getTotalWeeks() > 0) {
            String statsText = String.format("📊 Attendance Summary: %d Present / %d Absent / %.1f%% Attendance Rate",
                stats.getPresentWeeks(), stats.getAbsentWeeks(), stats.getAttendanceRate());
            
            // Color code based on attendance rate with blue theme variations
            String backgroundColor;
            String textColor = "#0d47a1";
            
            if (stats.getAttendanceRate() >= 90) {
                backgroundColor = "#e8f5e8"; // Light green for excellent
            } else if (stats.getAttendanceRate() >= 80) {
                backgroundColor = "#e3f2fd"; // Light blue for good
            } else if (stats.getAttendanceRate() >= 70) {
                backgroundColor = "#fff3e0"; // Light orange for average
            } else if (stats.getAttendanceRate() >= 60) {
                backgroundColor = "#ffebee"; // Light red for poor
            } else {
                backgroundColor = "#f5f5f5"; // Gray for very poor
                textColor = "#d32f2f";
            }
            
            statsLabel.setStyle(String.format(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: %s; " +
                "-fx-background-color: %s; -fx-background-radius: 8px; " +
                "-fx-padding: 12px; -fx-alignment: center; -fx-border-color: #bbdefb; -fx-border-width: 1px;",
                textColor, backgroundColor
            ));
            statsLabel.setText(statsText);
        } else {
            statsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #757575; " +
                               "-fx-background-color: #f5f5f5; -fx-background-radius: 8px; " +
                               "-fx-padding: 12px; -fx-alignment: center;");
            statsLabel.setText("No attendance records available for this course");
        }
    } catch (Exception e) {
        e.printStackTrace();
        statsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d32f2f; " +
                           "-fx-background-color: #ffebee; -fx-background-radius: 8px; " +
                           "-fx-padding: 12px; -fx-alignment: center;");
        statsLabel.setText("Error loading attendance statistics");
    }
}    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attendance");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Keep the old method for compatibility if needed
    public VBox createStudentAttendanceView() {
        return createAttendanceContent();
    }
}