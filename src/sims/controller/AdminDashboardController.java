package sims.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import sims.UserSession;
import sims.dao.StudentDao;
import sims.dao.InstructorDao;
import sims.dao.CourseDao;
import sims.dao.EnrollmentDao;
import sims.service.*;
import javafx.animation.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AdminDashboardController {
    private Stage primaryStage;
    private StudentDao studentDao;
    private InstructorDao instructorDao;
    private CourseDao courseDao;
    private AdvancedReportService advancedReportService;
    private BackupService backupService;
    private DataRefreshService refreshService;
    private ScheduledService<Void> backupScheduler;
    private ScheduledService<Void> refreshScheduler;
    private Label statsLabel;
    private Label backupStatusLabel;
    private Label refreshStatusLabel;
    
    public AdminDashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.studentDao = new StudentDao();
        this.instructorDao = new InstructorDao();
        this.courseDao = new CourseDao();
        this.advancedReportService = new AdvancedReportService();
        this.backupService = new BackupService();
        this.refreshService = new DataRefreshService();
        
        // Pre-load data for better performance
        loadInitialDataSync();
        initializeDashboard();
    }
    
    private void loadInitialDataSync() {
        System.out.println("Loading initial data synchronously...");
        
        // Load data immediately (blocking) for first display
        studentDao.loadAllStudents();
        instructorDao.loadAllInstructors();
        courseDao.loadAllCourses();
    }
    
    private void initializeDashboard() {
        startBackgroundServices();
        startBackupStatusUpdates();
        startStatisticsUpdates();
        startRefreshStatusUpdates();
    }
    
    private void startBackgroundServices() {
        // Start automatic backup service
        backupScheduler = backupService.createScheduledBackup();
        backupScheduler.start();
        
        // Start data refresh service (FASTER - every 5 seconds)
        refreshScheduler = refreshService.createRefreshService();
        refreshScheduler.start();
        
        System.out.println("Background services started");
        System.out.println("   - Data refresh: Every 5 seconds");
        System.out.println("   - Auto-backup: Every 15 minutes");
    }
    
    private void startBackupStatusUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
            if (backupStatusLabel != null) {
                backupStatusLabel.setText(getBackupStatus());
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void startStatisticsUpdates() {
        // Update statistics every 1 second to show real-time changes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (statsLabel != null) {
                statsLabel.setText(getSystemStatistics());
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void startRefreshStatusUpdates() {
        // Show last refresh time
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (refreshStatusLabel != null) {
                refreshStatusLabel.setText(getRefreshStatus());
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    public void showAdminDashboard() {
        // Create a new Stage to ensure clean state
        Stage adminStage = new Stage();
        
        // Create main layout with background
        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        
        // Create center content
        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);
        
        // Create scene
        Scene scene = new Scene(root);
        
        // Configure the new stage
        adminStage.setTitle("Admin Dashboard - Student Information Management System");
        adminStage.setScene(scene);
        adminStage.setMaximized(true);
        adminStage.setFullScreen(false);
        
        // Close the previous stage
        primaryStage.close();
        
        // Show the new stage
        adminStage.show();
        
        // Update the primaryStage reference
        this.primaryStage = adminStage;
    }
    
    private Background createBackground() {
        try {
            // Replace with your actual image path
            String imagePath = "file:/C:/Users/Administrator/Downloads/uni.jpg"; // Windows
            
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
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#1e3c72")),
            new Stop(1, Color.web("#2a5298"))
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
    
    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.65);" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        // Welcome message with statistics
        Label welcomeLabel = new Label("Welcome to Admin Dashboard");
        welcomeLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 28px; -fx-font-weight: bold;");
        
        statsLabel = new Label(getSystemStatistics());
        statsLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 14px; -fx-font-weight: normal;");
        
        // Backup status label
        backupStatusLabel = new Label(getBackupStatus());
        backupStatusLabel.setStyle("-fx-text-fill: #0288d1; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3px;");
        
        // Refresh status label
        refreshStatusLabel = new Label(getRefreshStatus());
        refreshStatusLabel.setStyle("-fx-text-fill: #0277bd; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3px;");
        
        // Create HBox for Data Management buttons
        HBox dataManagementButtons = new HBox(15);
        dataManagementButtons.setAlignment(Pos.CENTER);
        dataManagementButtons.setPadding(new Insets(10, 0, 10, 0));
                // In your createCenterContent() method, add this temporarily:

        // Data Management buttons - All in blue shades
        Button btnManageStudents = createManagementButton("Manage Students", "#2196f3");
        Button btnManageInstructors = createManagementButton("Manage Instructors", "#1565c0");
        Button btnManageCourses = createManagementButton("Manage Courses", "#2196f3");
        Button btnStudentGrades = createManagementButton("Student Grades & GPA", "#1565c0");
        dataManagementButtons.getChildren().addAll(btnManageStudents, btnManageInstructors, btnManageCourses, btnStudentGrades);

// Add it to one of your HBoxes temporarily
        // Create HBox for System Administration buttons
        HBox systemAdminButtons = new HBox(15);
        systemAdminButtons.setAlignment(Pos.CENTER);
        systemAdminButtons.setPadding(new Insets(10, 0, 10, 0));
        
        // System Administration buttons - All in blue shades
        Button btnGenerateReports = createManagementButton("Generate Reports", "#1565c0");
        Button btnBackupNow = createManagementButton("Backup Now", "#2196f3");
        Button btnLogout = createManagementButton("Logout", "#2196f3");
        Button btnAttendanceOverview = createManagementButton("View Attendances", "#1565c0");
        systemAdminButtons.getChildren().addAll(btnGenerateReports, btnBackupNow, btnAttendanceOverview, btnLogout);
        
        // Set button actions
        btnManageStudents.setOnAction(e -> openStudentManagement());
        btnManageInstructors.setOnAction(e -> openInstructorManagement());
        btnManageCourses.setOnAction(e -> openCourseManagement());
        btnGenerateReports.setOnAction(e -> showReportDialog());
        btnBackupNow.setOnAction(e -> performManualBackup());
        btnLogout.setOnAction(e -> logout());
        btnAttendanceOverview.setOnAction(e -> showAttendanceOverview());
        btnStudentGrades.setOnAction(e -> showStudentGradesManagement());
        
        // Create sections with headers
        Label managementHeader = new Label("Data Management");
        managementHeader.setStyle("-fx-text-fill: #0d47a1; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        
        Label systemHeader = new Label("System Administration");
        systemHeader.setStyle("-fx-text-fill: #0d47a1; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 20 0 5 0;");
        
        content.getChildren().addAll(
            welcomeLabel, 
            statsLabel, 
            backupStatusLabel,
            refreshStatusLabel,
            managementHeader, 
            dataManagementButtons,
            systemHeader,
            systemAdminButtons
        );
        
        return content;
    }
    
    private void showStudentGradesManagement() {
        // Close the admin dashboard completely
        primaryStage.close();
        
        // Open the student grades window
        Stage studentGradesStage = new Stage();
        AdminGradesManagement controller = new AdminGradesManagement(studentGradesStage);
        controller.showStudentGradesManagement();
    }
    
    private String getSystemStatistics() {
        int studentCount = studentDao.getStudentData().size();
        int instructorCount = instructorDao.getInstructorData().size();
        int courseCount = courseDao.getCourseData().size();
        
        return String.format("System Overview: %d Students | %d Instructors | %d Courses", 
                           studentCount, instructorCount, courseCount);
    }
    
    private String getBackupStatus() {
        return "Auto-backup: Every 15 minutes | " + backupService.getBackupStats();
    }
    
    private String getRefreshStatus() {
        return "Auto-refresh: Every 5 seconds | Last: " + new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
    
    private void showReportDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Generate Reports");
        dialog.setHeaderText("Select Report Type and Format");
        
        ButtonType excelSystemButton = new ButtonType("Excel System Report", ButtonBar.ButtonData.OK_DONE);
        ButtonType pdfSystemButton = new ButtonType("PDF System Report", ButtonBar.ButtonData.OK_DONE);
        ButtonType excelStudentButton = new ButtonType("Excel Student Report", ButtonBar.ButtonData.OK_DONE);
        ButtonType pdfStudentButton = new ButtonType("PDF Student Report", ButtonBar.ButtonData.OK_DONE);
        
        dialog.getDialogPane().getButtonTypes().addAll(
            excelSystemButton, pdfSystemButton, excelStudentButton, pdfStudentButton, ButtonType.CANCEL
        );
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Enter Student ID (for student reports)");
        studentIdField.setStyle("-fx-border-color: #64b5f6; -fx-background-color: white;");
        
        Label studentIdLabel = new Label("Student ID:");
        studentIdLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold;");
        
        Label formatInfo = new Label("Available Formats:\n• Excel (.xlsx) - Rich formatting with multiple sheets\n• PDF (.pdf) - Professional documents");
        formatInfo.setStyle("-fx-text-fill: #555; -fx-font-size: 12px; -fx-padding: 10 0 10 0;");
        
        grid.add(formatInfo, 0, 0, 2, 1);
        grid.add(studentIdLabel, 0, 1);
        grid.add(studentIdField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == excelSystemButton) {
                return "EXCEL_SYSTEM";
            } else if (dialogButton == pdfSystemButton) {
                return "PDF_SYSTEM";
            } else if (dialogButton == excelStudentButton) {
                String studentId = studentIdField.getText().trim();
                if (studentId.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Student ID for Excel student report");
                    return null;
                }
                return "EXCEL_STUDENT:" + studentId;
            } else if (dialogButton == pdfStudentButton) {
                String studentId = studentIdField.getText().trim();
                if (studentId.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Student ID for PDF student report");
                    return null;
                }
                return "PDF_STUDENT:" + studentId;
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reportType -> {
            switch (reportType) {
                case "EXCEL_SYSTEM":
                    generateExcelSystemReport();
                    break;
                case "PDF_SYSTEM":
                    generatePdfSystemReport();
                    break;
                default:
                    if (reportType.startsWith("EXCEL_STUDENT:")) {
                        String studentId = reportType.substring(14);
                        generateExcelStudentReport(studentId);
                    } else if (reportType.startsWith("PDF_STUDENT:")) {
                        String studentId = reportType.substring(12);
                        generatePdfStudentReport(studentId);
                    }
                    break;
            }
        });
    }
    
    private void generateExcelSystemReport() {
    System.out.println("DEBUG: Starting Excel system report generation");
    
    Task<Void> reportTask = advancedReportService.generateExcelSystemReport();
    
    // Add debug listeners
    reportTask.progressProperty().addListener((obs, oldVal, newVal) -> {
        System.out.println("DEBUG: Excel Progress updated: " + newVal);
    });
    
    reportTask.messageProperty().addListener((obs, oldVal, newVal) -> {
        System.out.println("DEBUG: Excel Message updated: " + newVal);
    });
    
    // Create and show progress dialog
    ProgressDialog progressDialog = new ProgressDialog(reportTask);
    progressDialog.setTitle("Generating Excel System Report");
    
    reportTask.setOnSucceeded(e -> {
        System.out.println("DEBUG: Excel system report generation succeeded");
        progressDialog.close();
        showAlert(Alert.AlertType.INFORMATION, "Success", 
                 "Excel system report generated successfully!\n\n" +
                 "File saved to: reports/system_report_*.xlsx\n" +
                 "Includes: Summary, Students, Courses, and Instructors sheets");
    });
    
    reportTask.setOnFailed(e -> {
        System.out.println("DEBUG: Excel system report generation failed: " + reportTask.getException());
        progressDialog.close();
        showAlert(Alert.AlertType.ERROR, "Error", 
                 "Failed to generate Excel system report: " + reportTask.getException().getMessage());
    });
    
    // Start the task
    new Thread(reportTask).start();
    
    // Show the dialog
    System.out.println("DEBUG: Showing Excel system progress dialog");
    progressDialog.showAndWait();
}
    
    private void generatePdfSystemReport() {
        System.out.println("DEBUG: Starting PDF system report generation");
        
        Task<Void> reportTask = advancedReportService.generatePdfSystemReport();
        
        // Add debug listeners
        reportTask.progressProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: PDF Progress updated: " + newVal);
        });
        
        reportTask.messageProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: PDF Message updated: " + newVal);
        });
        
        // Create and show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(reportTask);
        progressDialog.setTitle("Generating PDF System Report");
        
        reportTask.setOnSucceeded(e -> {
            System.out.println("DEBUG: PDF system report generation succeeded");
            progressDialog.close();
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "PDF system report generated successfully!\n\n" +
                     "File saved to: reports/system_report_*.pdf\n" +
                     "Includes: Professional summary with statistics");
        });
        
        reportTask.setOnFailed(e -> {
            System.out.println("DEBUG: PDF system report generation failed: " + reportTask.getException());
            progressDialog.close();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to generate PDF system report: " + reportTask.getException().getMessage());
        });
        
        // Start the task
        new Thread(reportTask).start();
        
        // Show the dialog
        System.out.println("DEBUG: Showing PDF system progress dialog");
        progressDialog.showAndWait();
    }
    
    private void generateExcelStudentReport(String studentId) {
        System.out.println("DEBUG: Starting Excel student report generation for: " + studentId);
        
        Task<Void> reportTask = advancedReportService.generateExcelStudentReport(studentId);
        
        // Add debug listeners
        reportTask.progressProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Excel Student Progress updated: " + newVal);
        });
        
        reportTask.messageProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Excel Student Message updated: " + newVal);
        });
        
        // Create and show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(reportTask);
        progressDialog.setTitle("Generating Excel Student Report");
        
        reportTask.setOnSucceeded(e -> {
            System.out.println("DEBUG: Excel student report generation succeeded");
            progressDialog.close();
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Excel student report generated successfully!\n\n" +
                     "File saved to: reports/student_report_" + studentId + "_*.xlsx\n" +
                     "Includes: Student details, courses, grades, and GPA");
        });
        
        reportTask.setOnFailed(e -> {
            System.out.println("DEBUG: Excel student report generation failed: " + reportTask.getException());
            progressDialog.close();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to generate Excel student report: " + reportTask.getException().getMessage());
        });
        
        // Start the task
        new Thread(reportTask).start();
        
        // Show the dialog
        System.out.println("DEBUG: Showing Excel student progress dialog");
        progressDialog.showAndWait();
    }
 
    private void generatePdfStudentReport(String studentId) {
    System.out.println("DEBUG: Starting PDF student report generation for: " + studentId);
    
    Task<Void> reportTask = advancedReportService.generatePdfStudentReport(studentId);
    
    // Add debug listeners
    reportTask.progressProperty().addListener((obs, oldVal, newVal) -> {
        System.out.println("DEBUG: PDF Student Progress updated: " + newVal);
    });
    
    reportTask.messageProperty().addListener((obs, oldVal, newVal) -> {
        System.out.println("DEBUG: PDF Student Message updated: " + newVal);
    });
    
    // Create and show progress dialog
    ProgressDialog progressDialog = new ProgressDialog(reportTask);
    progressDialog.setTitle("Generating PDF Student Report");
    
    reportTask.setOnSucceeded(e -> {
        System.out.println("DEBUG: PDF student report generation succeeded");
        progressDialog.close();
        showAlert(Alert.AlertType.INFORMATION, "Success", 
                 "PDF student report generated successfully!\n\n" +
                 "File saved to: reports/student_report_" + studentId + "_*.pdf\n" +
                 "Includes: Professional student transcript with grades");
    });
    
    reportTask.setOnFailed(e -> {
        System.out.println("DEBUG: PDF student report generation failed: " + reportTask.getException());
        progressDialog.close();
        showAlert(Alert.AlertType.ERROR, "Error", 
                 "Failed to generate PDF student report: " + reportTask.getException().getMessage());
    });
    
    // Start the task
    new Thread(reportTask).start();
    
    // Show the dialog
    System.out.println("DEBUG: Showing PDF student progress dialog");
    progressDialog.showAndWait();
}
    private void performManualBackup() {
        System.out.println("DEBUG: Starting manual backup");
        
        Task<Void> backupTask = backupService.performManualBackup();
        
        // Add debug listeners
        backupTask.progressProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Backup Progress updated: " + newVal);
        });
        
        backupTask.messageProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Backup Message updated: " + newVal);
        });
        
        // Create and show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(backupTask);
        progressDialog.setTitle("Database Backup");
        
        backupTask.setOnSucceeded(e -> {
            System.out.println("DEBUG: Backup succeeded");
            progressDialog.close();
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Database backup completed successfully!\n\n" +
                     "Backup file saved to: backups/ folder");
            // Update backup status immediately
            backupStatusLabel.setText(getBackupStatus());
        });
        
        backupTask.setOnFailed(e -> {
            System.out.println("DEBUG: Backup failed: " + backupTask.getException());
            progressDialog.close();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Backup failed: " + backupTask.getException().getMessage());
        });
        
        backupTask.setOnCancelled(e -> {
            System.out.println("DEBUG: Backup cancelled");
            progressDialog.close();
        });
        
        // Start the task
        new Thread(backupTask).start();
        
        // Show the dialog
        System.out.println("DEBUG: Showing backup progress dialog");
        progressDialog.showAndWait();
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
    
    private Button createManagementButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 25px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 180px;"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + color + ", -20%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 25px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 180px;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 25px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 180px;"
        ));
        
        return button;
    }
    
    private void openStudentManagement() {
        // Create new stage for student management
        Stage studentStage = new Stage();
        DashboardController dashboardController = new DashboardController(studentStage);
        
        // Close current admin stage
        primaryStage.close();
        
        // Show student management dashboard
        dashboardController.showDashboard();
    }

    private void openInstructorManagement() {
        // Create new stage for instructor management
        Stage instructorStage = new Stage();
        InstructorManagementController instructorController = new InstructorManagementController(instructorStage);
        
        // Close current admin stage
        primaryStage.close();
        
        // Show instructor management
        instructorController.showInstructorManagement();
    }

    private void openCourseManagement() {
        // Create new stage for course management
        Stage courseStage = new Stage();
        CourseController courseController = new CourseController(courseStage);
        
        // Close current admin stage
        primaryStage.close();
        
        // Show course management
        courseController.showCourseDashboard();
    }

    private void showAttendanceOverview() {
        // Create new stage for attendance
        Stage attendanceStage = new Stage();
        AdminAttendanceController attendanceController = new AdminAttendanceController(attendanceStage);
        
        // Show attendance view (this will handle the full screen setup)
        attendanceController.showAdminAttendanceView();
    }
    
    private void logout() {
        // Stop background services
        if (backupScheduler != null) {
            backupScheduler.cancel();
        }
        if (refreshScheduler != null) {
            refreshScheduler.cancel();
        }
        
        UserSession.getInstance().clearSession();
        
        // Create a new stage for login
        Stage loginStage = new Stage();
        LoginController loginController = new LoginController(loginStage);
        
        // Set up the stage properties for full screen
        loginStage.setMaximized(true);
        
        // Close the current stage
        primaryStage.close();
        
        // Show login screen
        loginController.showLoginScreen();
    }
    
    private class ProgressDialog extends Stage {
        private ProgressBar progressBar;
        private Label messageLabel;
        private Label percentageLabel;
        
        public ProgressDialog(Task<?> task) {
            initStyle(StageStyle.UTILITY);
            initModality(Modality.APPLICATION_MODAL);
            setResizable(false);
            setTitle("Processing");
            
            // Create a larger, more visible progress bar
            progressBar = new ProgressBar();
            progressBar.setPrefWidth(350);
            progressBar.setPrefHeight(25);
            progressBar.setStyle("-fx-accent: #1565c0; -fx-border-color: #90caf9; -fx-border-width: 2px;");
            
            // Message label
            messageLabel = new Label("Starting...");
            messageLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            // Percentage label
            percentageLabel = new Label("0%");
            percentageLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 16px; -fx-font-weight: bold;");
            
            // Cancel button
            Button cancelButton = new Button("Cancel");
            cancelButton.setStyle(
                "-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8px 16px; -fx-background-radius: 5px;"
            );
            cancelButton.setOnAction(e -> {
                if (task.isRunning()) {
                    task.cancel();
                    close();
                    showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Operation was cancelled.");
                }
            });
            
            // Layout
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setAlignment(Pos.CENTER);
            content.setStyle(
                "-fx-background-color: #e3f2fd; " +
                "-fx-border-color: #1565c0; " +
                "-fx-border-width: 3px; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px;"
            );
            
            VBox progressSection = new VBox(10, progressBar, percentageLabel);
            progressSection.setAlignment(Pos.CENTER);
            
            content.getChildren().addAll(messageLabel, progressSection, cancelButton);
            
            // Bind to task
            progressBar.progressProperty().bind(task.progressProperty());
            messageLabel.textProperty().bind(task.messageProperty());
            
            // Update percentage
            task.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    int percent = (int) (newVal.doubleValue() * 100);
                    percentageLabel.setText(percent + "%");
                    
                    // Change color based on progress
                    if (percent >= 70) {
                        percentageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 16px; -fx-font-weight: bold;");
                    } else if (percent >= 40) {
                        percentageLabel.setStyle("-fx-text-fill: #ff8f00; -fx-font-size: 16px; -fx-font-weight: bold;");
                    } else {
                        percentageLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 16px; -fx-font-weight: bold;");
                    }
                }
            });
            
            Scene scene = new Scene(content);
            setScene(scene);
            
            // Set dialog size
            setWidth(450);
            setHeight(250);
            
            // Center on screen
            centerOnScreen();
            
            // Close when task completes
            task.setOnSucceeded(e -> close());
            task.setOnFailed(e -> close());
            task.setOnCancelled(e -> close());
        }
    }
}