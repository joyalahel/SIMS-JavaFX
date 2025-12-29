package sims.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import sims.UserSession;
import sims.dao.EnrollmentDao;
import sims.model.Enrollment;
import sims.service.AdvancedReportService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StudentEnrollmentController {
    private Stage primaryStage;
    private TableView<Enrollment> enrollmentTable;
    private EnrollmentDao enrollmentDao;
    private String studentId;
    
    private Button btnBack;
    private Button btnExport;
    private Label lblStatus;
    private Label lblGPA;
    
    public StudentEnrollmentController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.enrollmentDao = new EnrollmentDao();
        this.studentId = UserSession.getInstance().getUsername().toUpperCase();
    }
    
    public void showMyEnrollments() {
        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        
        lblStatus = new Label("Loading your enrollments...");
        lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        lblGPA = new Label("GPA: Calculating...");
        lblGPA.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        createStyledButtons();
        setupStyledTable();
        setupEventHandlers();
        
        VBox topSection = createTopSection();
        root.setTop(topSection);
        root.setCenter(createTableContainer());
        
        loadMyEnrollments();
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("My Enrollments - SIMS");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(false);
        primaryStage.show();
    }
    
    private double calculateGPA(List<Enrollment> enrollments) {
        if (enrollments == null || enrollments.isEmpty()) {
            return 0.0;
        }
        
        double totalGradePoints = 0.0;
        int totalCredits = 0;
        int gradedCoursesCount = 0;
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getGrade() != null) {
                double gradePoints = convertGradeToPoints(enrollment.getGrade());
                int credits = enrollment.getCredits();
                
                totalGradePoints += gradePoints * credits;
                totalCredits += credits;
                gradedCoursesCount++;
            }
        }
        
        if (totalCredits == 0 || gradedCoursesCount == 0) {
            return 0.0;
        }
        
        return totalGradePoints / totalCredits;
    }
    
    private double convertGradeToPoints(double grade) {
        if (grade >= 90) return 4.0;
        else if (grade >= 85) return 3.7;
        else if (grade >= 80) return 3.3;
        else if (grade >= 75) return 3.0;
        else if (grade >= 70) return 2.7;
        else if (grade >= 65) return 2.3;
        else if (grade >= 60) return 2.0;
        else if (grade >= 55) return 1.7;
        else if (grade >= 50) return 1.3;
        else if (grade >= 45) return 1.0;
        else if (grade >= 40) return 0.7;
        else return 0.0;
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
            LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0d1b2a")),
                new Stop(0.3, Color.web("#1b263b")),
                new Stop(0.7, Color.web("#415a77")),
                new Stop(1, Color.web("#1b263b"))
            );
            return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
        }
    }
    
    private VBox createTopSection() {
        UserSession session = UserSession.getInstance();
        
        Label title = new Label("My Course Enrollments & Grades");
        title.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 24px; -fx-font-weight: bold; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        Label welcome = new Label("Welcome, " + session.getFullName() + " - Your enrolled courses and grades");
        welcome.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px;");
        
        HBox buttonPanel = new HBox(15);
        buttonPanel.setPadding(new Insets(15, 0, 5, 0));
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(btnBack, btnExport);
        
        HBox statusGpaPanel = new HBox(20);
        statusGpaPanel.setAlignment(Pos.CENTER);
        statusGpaPanel.getChildren().addAll(lblStatus, lblGPA);
        
        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25));
        topSection.setBackground(createHeaderBackground());
        topSection.getChildren().addAll(title, welcome, buttonPanel, statusGpaPanel);
        
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
        btnBack = createStyledButton("Back to Dashboard", "#1565c0");
        btnExport = createStyledButton("Export Report", "#2196f3");
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
        enrollmentTable = new TableView<>();
        enrollmentTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        enrollmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label placeholder = new Label("No course enrollments found.\n\nClick 'Browse Courses' to enroll in courses.");
        placeholder.setStyle("-fx-text-fill: #778da9; -fx-font-size: 14px; -fx-alignment: center; -fx-font-style: italic;");
        placeholder.setPadding(new Insets(20));
        enrollmentTable.setPlaceholder(placeholder);

        TableColumn<Enrollment, String> courseCol = createStyledColumn("Course", "courseName", 250);
        TableColumn<Enrollment, String> codeCol = createStyledColumn("Course Code", "courseId", 120);
        TableColumn<Enrollment, Integer> creditsCol = createCreditsColumn("Credits", "credits", 80);
        TableColumn<Enrollment, Double> gradeCol = createGradeColumn("Grade", "grade", 100);
        TableColumn<Enrollment, String> letterGradeCol = createLetterGradeColumn("Letter Grade", 100);
        TableColumn<Enrollment, String> statusCol = createStyledColumn("Status", "status", 100);

        enrollmentTable.getColumns().addAll(courseCol, codeCol, creditsCol, gradeCol, letterGradeCol, statusCol);

        enrollmentTable.setRowFactory(tv -> new TableRow<Enrollment>() {
            @Override
            protected void updateItem(Enrollment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    setText(null);
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f8fbff; -fx-text-fill: #1e3c72; -fx-border-color: #e3f2fd;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-text-fill: #1e3c72; -fx-border-color: #e3f2fd;");
                    }
                    
                    if (item.getGrade() != null) {
                        if (item.getGrade() >= 90) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #1e3c72; -fx-border-color: #c8e6c9;");
                        } else if (item.getGrade() >= 80) {
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72; -fx-border-color: #bbdefb;");
                        } else if (item.getGrade() >= 70) {
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #1e3c72; -fx-border-color: #ffecb3;");
                        } else if (item.getGrade() >= 60) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #1e3c72; -fx-border-color: #ffcdd2;");
                        } else {
                            setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #d32f2f; -fx-border-color: #e0e0e0;");
                        }
                    }
                }
            }
        });
    }
    
    private <T> TableColumn<Enrollment, T> createStyledColumn(String title, String property, double width) {
        TableColumn<Enrollment, T> column = new TableColumn<>(title);
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
        
        column.setCellFactory(tc -> new TableCell<Enrollment, T>() {
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
    
    private TableColumn<Enrollment, Integer> createCreditsColumn(String title, String property, double width) {
        TableColumn<Enrollment, Integer> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setMinWidth(60);
        
        column.setStyle(
            "-fx-alignment: CENTER;" +
            "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #90caf9;"
        );
        
        column.setCellFactory(tc -> new TableCell<Enrollment, Integer>() {
            @Override
            protected void updateItem(Integer credits, boolean empty) {
                super.updateItem(credits, empty);
                if (empty || credits == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(credits.toString());
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                }
            }
        });
        
        return column;
    }
    
    private TableColumn<Enrollment, Double> createGradeColumn(String title, String property, double width) {
        TableColumn<Enrollment, Double> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setMinWidth(80);
        
        column.setStyle(
            "-fx-alignment: CENTER;" +
            "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #90caf9;"
        );
        
        column.setCellFactory(tc -> new TableCell<Enrollment, Double>() {
            @Override
            protected void updateItem(Double grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    Enrollment enrollment = getTableRow().getItem();
                    if (enrollment.getGrade() == null) {
                        setText("Not Graded");
                        setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #778da9; -fx-alignment: CENTER; -fx-font-style: italic;");
                    } else {
                        setText(String.format("%.1f", grade));
                        
                        if (grade >= 90) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (grade >= 80) {
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (grade >= 70) {
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (grade >= 60) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #d32f2f; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });
        
        return column;
    }
    
    private TableColumn<Enrollment, String> createLetterGradeColumn(String title, double width) {
        TableColumn<Enrollment, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setMinWidth(80);
        
        column.setStyle(
            "-fx-alignment: CENTER;" +
            "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #90caf9;"
        );
        
        column.setCellFactory(tc -> new TableCell<Enrollment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    Enrollment enrollment = getTableRow().getItem();
                    String letterGrade = getLetterGrade(enrollment.getGrade());
                    setText(letterGrade);
                    
                    if (enrollment.getGrade() == null) {
                        setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #778da9; -fx-alignment: CENTER; -fx-font-style: italic;");
                    } else if (enrollment.getGrade() >= 90) {
                        setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else if (enrollment.getGrade() >= 80) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else if (enrollment.getGrade() >= 70) {
                        setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else if (enrollment.getGrade() >= 60) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #d32f2f; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        column.setCellValueFactory(cellData -> {
            Enrollment enrollment = cellData.getValue();
            String letterGrade = getLetterGrade(enrollment.getGrade());
            return new javafx.beans.property.SimpleStringProperty(letterGrade);
        });
        
        return column;
    }
    
    private String getLetterGrade(Double grade) {
        if (grade == null) {
            return "Not Graded";
        } else if (grade >= 97) return "A+";
        else if (grade >= 93) return "A";
        else if (grade >= 90) return "A-";
        else if (grade >= 87) return "B+";
        else if (grade >= 83) return "B";
        else if (grade >= 80) return "B-";
        else if (grade >= 77) return "C+";
        else if (grade >= 73) return "C";
        else if (grade >= 70) return "C-";
        else if (grade >= 67) return "D+";
        else if (grade >= 63) return "D";
        else if (grade >= 60) return "D-";
        else return "F";
    }
    
    private VBox createTableContainer() {
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(20));
        tableContainer.setBackground(Background.EMPTY);
        
        Label tableTitle = new Label("Your Course Enrollments and Grades");
        tableTitle.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 20px; -fx-font-weight: bold; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        Label gradeKey = new Label("Grade Key: A (90-100) • B (80-89) • C (70-79) • D (60-69) • F (0-59)");
        gradeKey.setStyle("-fx-text-fill: #bbdefb; -fx-font-size: 12px; -fx-font-style: italic;");
        
        VBox.setVgrow(enrollmentTable, Priority.ALWAYS);
        enrollmentTable.setMaxHeight(Double.MAX_VALUE);
        
        VBox.setMargin(tableTitle, new Insets(0, 0, 10, 0));
        VBox.setMargin(gradeKey, new Insets(0, 0, 15, 0));
        tableContainer.getChildren().addAll(tableTitle, gradeKey, enrollmentTable);
        
        return tableContainer;
    }
    
    private void setupEventHandlers() {
        btnBack.setOnAction(e -> backToDashboard());
        btnExport.setOnAction(e -> showReportGenerationDialog());
    }
    
    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            if (message.toLowerCase().contains("error")) {
                lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else if (message.toLowerCase().contains("ready")) {
                lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else {
                lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
        }
    }
    
    private void loadMyEnrollments() {
        try {
            System.out.println("=== DEBUG: LOADING ENROLLMENTS ===");
            System.out.println("Student ID: " + studentId);
            
            var enrollments = enrollmentDao.getEnrollmentsByStudent(studentId);
            System.out.println("Raw enrollments count: " + enrollments.size());
            
            if (enrollments.isEmpty()) {
                System.out.println("No enrollments found for student: " + studentId);
                showAlert(Alert.AlertType.INFORMATION, "No Enrollments", 
                         "You are not enrolled in any courses yet.\n\nPlease use 'Browse Courses' to enroll in courses.");
                lblGPA.setText("GPA: N/A - No enrolled courses");
            } else {
                for (Enrollment e : enrollments) {
                    System.out.println("Enrollment Details:");
                    System.out.println("   - Course: " + e.getCourseName());
                    System.out.println("   - Course ID: " + e.getCourseId());
                    System.out.println("   - Grade: " + e.getGrade());
                    System.out.println("   - Credits: " + e.getCredits());
                    System.out.println("   - Status: " + e.getStatus());
                    System.out.println("   - Enrollment ID: " + e.getEnrollmentId());
                }
                
                double gpa = calculateGPA(enrollments);
                updateGPALabel(gpa, enrollments);
            }
            
            enrollmentTable.setItems(FXCollections.observableArrayList(enrollments));
            
            int totalEnrollments = enrollments.size();
            int gradedEnrollments = (int) enrollments.stream()
                .filter(e -> e.getGrade() != null)
                .count();
            
            updateStatus("Ready - " + totalEnrollments + " enrollments (" + gradedEnrollments + " graded)");
            
        } catch (Exception e) {
            System.err.println("Error loading enrollments: " + e.getMessage());
            e.printStackTrace();
            updateStatus("Error loading enrollments");
            lblGPA.setText("GPA: Error calculating");
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Failed to load your enrollments. Please try again.");
        }
    }
    
    private void updateGPALabel(double gpa, List<Enrollment> enrollments) {
        int gradedCourses = (int) enrollments.stream()
            .filter(e -> e.getGrade() != null)
            .count();
        
        if (gradedCourses == 0) {
            lblGPA.setText("GPA: N/A - No graded courses");
            lblGPA.setStyle("-fx-text-fill: #778da9; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            String gpaText = String.format("GPA: %.2f (%d graded courses)", gpa, gradedCourses);
            lblGPA.setText(gpaText);
            
            if (gpa >= 3.7) {
                lblGPA.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else if (gpa >= 3.0) {
                lblGPA.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else if (gpa >= 2.0) {
                lblGPA.setStyle("-fx-text-fill: #ff8f00; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else {
                lblGPA.setStyle("-fx-text-fill: #c62828; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
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
    
    private void backToDashboard() {
        primaryStage.close();
        showStudentDashboard();
    }
    
    private void showStudentDashboard() {
        Stage dashboardStage = new Stage();
        StudentDashboardController dashboardController = new StudentDashboardController(dashboardStage);
        dashboardController.showStudentDashboard();
    }
    
  private void showReportGenerationDialog() {
    Stage progressStage = new Stage();
    progressStage.initModality(Modality.APPLICATION_MODAL);
    progressStage.initStyle(StageStyle.UTILITY);
    progressStage.setTitle("Generating Excel Report");
    progressStage.setResizable(false);

    VBox root = new VBox(20);
    root.setPadding(new Insets(25));
    root.setAlignment(Pos.CENTER);
    root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

    Label progressLabel = new Label("Generating your Excel enrollment report...");
    progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

    ProgressBar progressBar = new ProgressBar();
    progressBar.setPrefWidth(300);
    progressBar.setStyle("-fx-accent: #1565c0;");

    Label percentageLabel = new Label("0%");
    percentageLabel.setStyle("-fx-font-size: 12px;");

    Button cancelButton = createStyledButton("Cancel", "#d32f2f");
    
    root.getChildren().addAll(progressLabel, progressBar, percentageLabel, cancelButton);

    Scene scene = new Scene(root, 400, 200);
    progressStage.setScene(scene);

    // Use AdvancedReportService to generate Excel report
    AdvancedReportService reportService = new AdvancedReportService();
    Task<Void> reportTask = reportService.generateExcelStudentReport(studentId);

    progressBar.progressProperty().bind(reportTask.progressProperty());
    percentageLabel.textProperty().bind(reportTask.messageProperty());

    reportTask.setOnSucceeded(e -> {
        progressStage.close();
        showAlert(Alert.AlertType.INFORMATION, "Excel Report Generated", 
                 "Your enrollment report has been generated successfully as an Excel file!");
    });

    reportTask.setOnCancelled(e -> {
        progressStage.close();
        showAlert(Alert.AlertType.INFORMATION, "Report Cancelled", 
                 "Excel report generation was cancelled.");
    });

    reportTask.setOnFailed(e -> {
        progressStage.close();
        showAlert(Alert.AlertType.ERROR, "Report Failed", 
                 "Failed to generate Excel report: " + reportTask.getException().getMessage());
    });

    cancelButton.setOnAction(e -> reportTask.cancel());

    new Thread(reportTask).start();

    progressStage.showAndWait();
}
   
}