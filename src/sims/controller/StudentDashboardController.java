package sims.controller;

import sims.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;

public class StudentDashboardController {
    private Stage primaryStage;
    private String studentId;
    
    public StudentDashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.studentId = UserSession.getInstance().getUsername().toUpperCase();
    }
    
  public void showStudentDashboard() {
    // Create a completely new stage to ensure clean state
    Stage studentStage = new Stage();
    
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    VBox centerContent = createCenterContent();
    root.setCenter(centerContent);
    
    // Create scene without dimensions
    Scene scene = new Scene(root);
    studentStage.setTitle("Student Dashboard - SIMS");
    studentStage.setScene(scene);
    studentStage.setMaximized(true);
    studentStage.setFullScreen(false);
    
    // Close the previous stage (login stage)
    primaryStage.close();
    
    // Show the new stage
    studentStage.show();
    
    // Update the primaryStage reference
    this.primaryStage = studentStage;
}
    
    private void showMyAttendance() {
        // Create new stage for attendance
        Stage attendanceStage = new Stage();
        StudentAttendanceViewController attendanceController = new StudentAttendanceViewController(attendanceStage, studentId);
        
        // Hide current student dashboard
        primaryStage.hide();
        
        // Show attendance view (this should handle full screen internally)
        attendanceController.showStudentAttendanceView();
    }
    
    private Background createBackground() {
        try {
            // Use the same image path or a different one for student dashboard
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
            return createFallbackBackground();
        }
    }

    private Background createFallbackBackground() {
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0d1b2a")),
            new Stop(0.3, Color.web("#1b263b")),
            new Stop(0.7, Color.web("#415a77")),
            new Stop(1, Color.web("#1b263b"))
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
    
    private VBox createCenterContent() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.85);" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(13,27,42,0.4), 20, 0, 0, 8);"
        );
        
        UserSession session = UserSession.getInstance();
        
        // Welcome message with blue theme
        Label welcomeLabel = new Label("Welcome, " + session.getFullName());
        welcomeLabel.setStyle("-fx-text-fill: #1e3c72; -fx-font-size: 28px; -fx-font-weight: bold; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(13,27,42,0.3), 4, 0, 0, 2);");
        
        Label roleLabel = new Label("Student Dashboard");
        roleLabel.setStyle("-fx-text-fill: #415a77; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label subLabel = new Label("Course Enrollment System");
        subLabel.setStyle("-fx-text-fill: #778da9; -fx-font-size: 14px; -fx-font-style: italic;");
        
        // Student actions with blue shades
        Button btnBrowseCourses = createActionButton("Browse Courses", "#1565c0");
        Button btnMyEnrollments = createActionButton("My Enrollments", "#2196f3");
        Button btnViewAttendance = createActionButton("View My Attendance", "#1976d2");
        Button btnChangePassword = createActionButton("Change Password", "#1565c0");
        Button btnLogout = createActionButton("Logout", "#2196f3");
        
        // Set button actions
        btnBrowseCourses.setOnAction(e -> openCourseEnrollment());
        btnMyEnrollments.setOnAction(e -> openMyEnrollments());
        btnViewAttendance.setOnAction(e -> showMyAttendance());
        btnChangePassword.setOnAction(e -> showChangePasswordDialog());
        btnLogout.setOnAction(e -> logout());
        
        content.getChildren().addAll(welcomeLabel, roleLabel, subLabel, 
                                   btnBrowseCourses, btnMyEnrollments, btnViewAttendance, 
                                   btnChangePassword, btnLogout);
        return content;
    }
    
    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-color: derive(" + color + ", 20%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 200px;"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + color + ", 20%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-color: derive(" + color + ", 40%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 5);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 200px;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-color: derive(" + color + ", 20%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 200px;"
        ));
        
        return button;
    }
    
    private void openCourseEnrollment() {
        // Create a new stage for course enrollment
        Stage enrollmentStage = new Stage();
        CourseEnrollmentController enrollmentController = new CourseEnrollmentController(enrollmentStage);
        
        // Hide current student dashboard
        primaryStage.hide();
        
        // Show course enrollment (this should handle full screen internally)
        enrollmentController.showCourseEnrollment();
    }
    
    private void openMyEnrollments() {
        // Create a new stage for enrollments
        Stage enrollmentStage = new Stage();
        StudentEnrollmentController enrollmentController = new StudentEnrollmentController(enrollmentStage);
        
        // Hide current student dashboard
        primaryStage.hide();
        
        // Show enrollments (this should handle full screen internally)
        enrollmentController.showMyEnrollments();
    }
    
    private void showChangePasswordDialog() {
        sims.util.PasswordUtil.showChangePasswordDialog();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert with blue theme
        DialogPane dialogPane = alert.getDialogPane();
        String color = alertType == Alert.AlertType.ERROR ? "#d32f2f" : 
                      alertType == Alert.AlertType.WARNING ? "#ff9800" : "#1565c0";
        
        dialogPane.setStyle(
            "-fx-background-color: #e3f2fd;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;"
        );
        
        alert.showAndWait();
    }
    
    private void logout() {
        UserSession.getInstance().clearSession();
        
        // Create a new stage for login
        Stage loginStage = new Stage();
        LoginController loginController = new LoginController(loginStage);
        
        // Close the current stage
        primaryStage.close();
        
        // Show login screen
        loginController.showLoginScreen();
    }
}