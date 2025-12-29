package sims.controller;

import sims.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

public class InstructorDashboardController {
    private Stage primaryStage;
    private String instructorId;
    
    public InstructorDashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.instructorId = UserSession.getInstance().getUsername().toUpperCase();
    }
    
public void showInstructorDashboard() {
    // Create a new Stage to ensure clean state
    Stage instructorStage = new Stage();
    
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    VBox centerContent = createCenterContent();
    root.setCenter(centerContent);
    
    // Create scene without dimensions
    Scene scene = new Scene(root);
    instructorStage.setTitle("Instructor Dashboard - SIMS");
    instructorStage.setScene(scene);
    instructorStage.setMaximized(true);
    instructorStage.setFullScreen(false);
    
    // Close the previous stage
    primaryStage.close();
    
    // Show the new stage
    instructorStage.show();
    
    // Update the primaryStage reference
    this.primaryStage = instructorStage;
}

    private Background createBackground() {
        try {
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
        
        Label roleLabel = new Label("Instructor Dashboard");
        roleLabel.setStyle("-fx-text-fill: #415a77; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label subLabel = new Label("Course Management System");
        subLabel.setStyle("-fx-text-fill: #778da9; -fx-font-size: 14px; -fx-font-style: italic;");
        
        // Buttons for instructors with blue shades
        Button btnViewCourses = createActionButton("View My Courses", "#1565c0");
        Button btnManageGrades = createActionButton("Manage Grades", "#2196f3");
        Button btnManageAttendance = createActionButton("Manage Attendance", "#1976d2");
        Button btnChangePassword = createActionButton("Change Password", "#1565c0");
        Button btnLogout = createActionButton("Logout", "#2196f3");
        
        // Set button actions
        btnViewCourses.setOnAction(e -> openCourseManagement());
        btnManageGrades.setOnAction(e -> openGradeManagement());
        btnManageAttendance.setOnAction(e -> showManageAttendance());
        btnChangePassword.setOnAction(e -> showChangePasswordDialog());
        btnLogout.setOnAction(e -> logout());
        
        content.getChildren().addAll(
            welcomeLabel, 
            roleLabel, 
            subLabel, 
            btnViewCourses, 
            btnManageGrades, 
            btnManageAttendance,
            btnChangePassword, 
            btnLogout
        );
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
    
    private void openCourseManagement() {
        CourseController courseController = new CourseController(primaryStage);
        courseController.showCourseDashboard();
    }
    
    private void openGradeManagement() {
        GradeManagementController gradeController = new GradeManagementController(primaryStage);
        gradeController.showGradeManagement();
    }
    
private void showManageAttendance() {
    InstructorAttendanceController attendanceController = new InstructorAttendanceController(instructorId);
    VBox attendanceView = attendanceController.createAttendanceManagementView();
    
    Stage attendanceStage = new Stage();
    attendanceStage.setTitle("Manage Attendance");
    
    // Make it full screen
    Scene attendanceScene = new Scene(attendanceView);
    attendanceStage.setScene(attendanceScene);
    attendanceStage.setMaximized(true);
    attendanceStage.setFullScreen(false);
    attendanceStage.show();
}
    
    private void showChangePasswordDialog() {
        sims.util.PasswordUtil.showChangePasswordDialog();
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