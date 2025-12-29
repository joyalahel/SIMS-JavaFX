package sims.controller;

import java.util.List;
import java.util.stream.Collectors;
import sims.dao.EnrollmentDao;
import sims.dao.StudentDao;
import sims.model.Enrollment;
import sims.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;

public class AdminGradesManagement {
    private Stage primaryStage;
    private TableView<Student> studentTable;
    private TableView<Enrollment> enrollmentTable;
    private StudentDao studentDao;
    private EnrollmentDao enrollmentDao;
    
    private Button btnBack;
    private Label lblStatus;
    private Label lblSummary;
    
    private ObservableList<Student> studentData;
    private ObservableList<Enrollment> enrollmentData;
    
    public AdminGradesManagement(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.studentDao = new StudentDao();
        this.enrollmentDao = new EnrollmentDao();
        this.studentData = FXCollections.observableArrayList();
        this.enrollmentData = FXCollections.observableArrayList();
    }
    
    public void showStudentGradesManagement() {
        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        
        lblStatus = new Label("Loading student data...");
        lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        lblSummary = new Label("");
        lblSummary.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        createStyledButtons();
        setupStudentTable();
        setupEnrollmentTable();
        setupEventHandlers();
        
        VBox topSection = createTopSection();
        root.setTop(topSection);
        root.setCenter(createMainContent());
        
        loadStudentData();
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("Student Grades & GPA Management - SIMS");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(false);
        primaryStage.show();
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
        Label title = new Label("Student Grades & GPA Management");
        title.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 24px; -fx-font-weight: bold; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        Label welcome = new Label("Admin View - Monitor all student grades and GPAs");
        welcome.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px;");
        
        HBox buttonPanel = new HBox(15);
        buttonPanel.setPadding(new Insets(15, 0, 5, 0));
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(btnBack); // Only back button, no refresh
        
        HBox statusPanel = new HBox(20);
        statusPanel.setAlignment(Pos.CENTER);
        statusPanel.getChildren().addAll(lblStatus, lblSummary);
        
        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25));
        topSection.setBackground(createHeaderBackground());
        topSection.getChildren().addAll(title, welcome, buttonPanel, statusPanel);
        
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
        // No refresh button
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
    
    private void setupStudentTable() {
        studentTable = new TableView<>();
        studentTable.setItems(studentData); // Use ObservableList
        
        studentTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label placeholder = new Label("No students found.");
        placeholder.setStyle("-fx-text-fill: #778da9; -fx-font-size: 14px; -fx-alignment: center; -fx-font-style: italic;");
        placeholder.setPadding(new Insets(20));
        studentTable.setPlaceholder(placeholder);

        TableColumn<Student, String> idCol = createStyledColumn("Student ID", "studentId", 120);
        TableColumn<Student, String> nameCol = createStyledColumn("Student Name", "fullName", 200);
        TableColumn<Student, String> emailCol = createStyledColumn("Email", "email", 180);
        TableColumn<Student, Double> gpaCol = createGPAColumn("GPA", 100);
        TableColumn<Student, Integer> coursesCol = createStyledColumn("Courses", "enrolledCourses", 80);
        TableColumn<Student, Integer> gradedCol = createStyledColumn("Graded", "gradedCourses", 80);

        studentTable.getColumns().addAll(idCol, nameCol, emailCol, gpaCol, coursesCol, gradedCol);

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
                    
                    // Highlight selected row
                    if (studentTable.getSelectionModel().getSelectedItem() == item) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72; -fx-border-color: #bbdefb;");
                    }
                }
            }
        });

        // When a student is selected, show their enrollments
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadStudentEnrollments(newSelection.getStudentId());
            }
        });
    }
    
    private <T> TableColumn<Student, T> createStyledColumn(String title, String property, double width) {
        TableColumn<Student, T> column = new TableColumn<>(title);
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
    
    private TableColumn<Student, Double> createGPAColumn(String title, double width) {
        TableColumn<Student, Double> column = new TableColumn<>(title);
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
        
        column.setCellFactory(tc -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    Student student = getTableRow().getItem();
                    Double studentGPA = student.getGpa();
                    
                    if (studentGPA == null || studentGPA == 0.0 || student.getGradedCourses() == 0) {
                        setText("N/A");
                        setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #778da9; -fx-alignment: CENTER; -fx-font-style: italic;");
                    } else {
                        setText(String.format("%.2f", studentGPA));
                        
                        // Color code the GPA
                        if (studentGPA >= 3.7) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (studentGPA >= 3.0) {
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (studentGPA >= 2.0) {
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });
        
        column.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        
        return column;
    }
    
    private void setupEnrollmentTable() {
        enrollmentTable = new TableView<>();
        enrollmentTable.setItems(enrollmentData); // Use ObservableList
        
        enrollmentTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        enrollmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label placeholder = new Label("Select a student to view their course enrollments and grades");
        placeholder.setStyle("-fx-text-fill: #778da9; -fx-font-size: 14px; -fx-alignment: center; -fx-font-style: italic;");
        placeholder.setPadding(new Insets(20));
        enrollmentTable.setPlaceholder(placeholder);

        TableColumn<Enrollment, String> courseCol = createEnrollmentColumn("Course", "courseName", 250);
        TableColumn<Enrollment, String> codeCol = createEnrollmentColumn("Course Code", "courseId", 120);
        TableColumn<Enrollment, Integer> creditsCol = createEnrollmentColumn("Credits", "credits", 80);
        TableColumn<Enrollment, Double> gradeCol = createGradeColumn("Grade", "grade", 100);
        TableColumn<Enrollment, String> letterGradeCol = createLetterGradeColumn("Letter Grade", 100);
        TableColumn<Enrollment, String> statusCol = createEnrollmentColumn("Status", "status", 100);

        enrollmentTable.getColumns().addAll(courseCol, codeCol, creditsCol, gradeCol, letterGradeCol, statusCol);
    }
    
    private <T> TableColumn<Enrollment, T> createEnrollmentColumn(String title, String property, double width) {
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
    
    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        VBox studentSection = new VBox();
        studentSection.setPadding(new Insets(10));
        studentSection.setSpacing(10);
        
        Label studentTitle = new Label("All Students");
        studentTitle.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox.setVgrow(studentTable, Priority.ALWAYS);
        studentTable.setMaxHeight(Double.MAX_VALUE);
        studentSection.getChildren().addAll(studentTitle, studentTable);
        
        VBox enrollmentSection = new VBox();
        enrollmentSection.setPadding(new Insets(10));
        enrollmentSection.setSpacing(10);
        
        Label enrollmentTitle = new Label("Selected Student's Course Enrollments & Grades");
        enrollmentTitle.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox.setVgrow(enrollmentTable, Priority.ALWAYS);
        enrollmentTable.setMaxHeight(Double.MAX_VALUE);
        enrollmentSection.getChildren().addAll(enrollmentTitle, enrollmentTable);
        
        splitPane.getItems().addAll(studentSection, enrollmentSection);
        splitPane.setDividerPositions(0.4);
        
        return splitPane;
    }
    
    private void setupEventHandlers() {
        btnBack.setOnAction(e -> backToDashboard());
        // No refresh button handler
    }
    
    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }
    
    private void updateSummary(String message) {
        if (lblSummary != null) {
            lblSummary.setText(message);
        }
    }
    
 // Alternative loadStudentData method if the above doesn't work:

private void loadStudentData() {
    try {
        // Clear the ObservableList first
        studentData.clear();
        
        // Direct database query as fallback
        String sql = "SELECT * FROM students ORDER BY student_id";
        
        try (var conn = sims.DatabaseConnection.connect();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("student_id"));
                student.setFirstName(rs.getString("first_name"));
                student.setLastName(rs.getString("last_name"));
                student.setEmail(rs.getString("email"));
                student.setDateOfBirth(rs.getDate("date_of_birth"));
                student.setMajor(rs.getString("major"));
                
                System.out.println("DEBUG: Created student: " + student.getStudentId() + " - " + student.getFullName());
                
                // Calculate GPA for this student
                calculateStudentGPA(student);
                studentData.add(student);
            }
        }
        
        // Calculate statistics
        int totalStudents = studentData.size();
        int studentsWithGPA = (int) studentData.stream()
            .filter(s -> s.getGpa() != null && s.getGpa() > 0 && s.getGradedCourses() > 0)
            .count();
        
        double averageGPA = studentData.stream()
            .filter(s -> s.getGpa() != null && s.getGpa() > 0 && s.getGradedCourses() > 0)
            .mapToDouble(Student::getGpa)
            .average()
            .orElse(0.0);
        
        updateStatus("Ready - " + totalStudents + " students loaded");
        updateSummary(String.format("Average GPA: %.2f (%d students with grades)", averageGPA, studentsWithGPA));
        
    } catch (Exception e) {
        System.err.println("Error loading student data: " + e.getMessage());
        e.printStackTrace();
        updateStatus("Error loading student data");
        showAlert(Alert.AlertType.ERROR, "Database Error", 
                 "Failed to load student data. Please try again.");
    }
}
    
    // Calculate GPA for a single student using existing EnrollmentDao
    private void calculateStudentGPA(Student student) {
        try {
            // Use existing EnrollmentDao method to get student enrollments
            List<Enrollment> enrollments = enrollmentDao.getEnrollmentsByStudent(student.getStudentId());
            
            double totalGradePoints = 0.0;
            int totalCredits = 0;
            int gradedCourses = 0;
            int totalCourses = enrollments.size();
            
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getGrade() != null) {
                    double gradePoints = convertGradeToPoints(enrollment.getGrade());
                    int credits = enrollment.getCredits();
                    
                    totalGradePoints += gradePoints * credits;
                    totalCredits += credits;
                    gradedCourses++;
                }
            }
            
            // Set the calculated values
            student.setEnrolledCourses(totalCourses);
            student.setGradedCourses(gradedCourses);
            
            if (totalCredits > 0 && gradedCourses > 0) {
                double gpa = totalGradePoints / totalCredits;
                student.setGpa(Math.round(gpa * 100.0) / 100.0); // Round to 2 decimal places
            } else {
                student.setGpa(0.0);
            }
            
        } catch (Exception e) {
            System.err.println("Error calculating GPA for student " + student.getStudentId() + ": " + e.getMessage());
            student.setEnrolledCourses(0);
            student.setGradedCourses(0);
            student.setGpa(0.0);
        }
    }
    
    // Grade to points conversion
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
    
    private void loadStudentEnrollments(String studentId) {
        try {
            // Use existing EnrollmentDao method
            List<Enrollment> enrollments = enrollmentDao.getEnrollmentsByStudent(studentId);
            
            // Clear and update ObservableList
            enrollmentData.clear();
            enrollmentData.addAll(enrollments);
            
            Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                String summary = String.format("Selected: %s (GPA: %.2f) - %d enrollments (%d graded)", 
                    selectedStudent.getFullName(),
                    selectedStudent.getGpa() != null ? selectedStudent.getGpa() : 0.0,
                    enrollments.size(),
                    enrollments.stream().filter(e -> e.getGrade() != null).count());
                updateSummary(summary);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading student enrollments: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to load enrollments for selected student.");
        }
    }
    
 private void backToDashboard() {
    // Close the student grades window
    primaryStage.close();
    
    // Create and show a new admin dashboard
    showAdminDashboard();
}

private void showAdminDashboard() {
    Stage dashboardStage = new Stage();
    AdminDashboardController dashboardController = new AdminDashboardController(dashboardStage);
    dashboardController.showAdminDashboard();
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
}