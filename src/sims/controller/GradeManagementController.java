package sims.controller;

import sims.UserSession;
import sims.dao.EnrollmentDao;
import sims.model.Enrollment;
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
import javafx.util.converter.DoubleStringConverter;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;

public class GradeManagementController {
    private Stage primaryStage;
    private TableView<Enrollment> gradeTable;
    private EnrollmentDao enrollmentDao;
    private String instructorId;
    
    private Button btnSaveGrades;
    private Button btnBack;
    
    private Label lblStatus;
    
    public GradeManagementController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.enrollmentDao = new EnrollmentDao();
        this.instructorId = UserSession.getInstance().getUsername().toUpperCase();
    }
    
public void showGradeManagement() {
    // Create a new Stage to ensure clean state
    Stage gradeStage = new Stage();
    
    BorderPane root = new BorderPane();
    root.setBackground(createBackground());
    
    lblStatus = new Label("Loading student enrollments...");
    lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
    
    createStyledButtons();
    setupStyledTable();
    setupEventHandlers();
    
    VBox topSection = createTopSection();
    root.setTop(topSection);
    root.setCenter(createTableContainer());
    
    loadGradeData();
    
    // Create scene without dimensions
    Scene scene = new Scene(root);
    gradeStage.setTitle("Grade Management - SIMS");
    gradeStage.setScene(scene);
    gradeStage.setMaximized(true);
    gradeStage.setFullScreen(false);
    
    // Close the previous stage
    primaryStage.close();
    
    // Show the new stage
    gradeStage.show();
    
    // Update the primaryStage reference
    this.primaryStage = gradeStage;
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
        
        Label title = new Label("Grade Management");
        title.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 24px; -fx-font-weight: bold; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        Label welcome = new Label("Welcome, " + session.getFullName() + " - Enter grades for your students");
        welcome.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 14px;");
        
        HBox buttonPanel = new HBox(15);
        buttonPanel.setPadding(new Insets(15, 0, 5, 0));
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(btnSaveGrades, btnBack);
        
        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25));
        topSection.setBackground(createHeaderBackground());
        topSection.getChildren().addAll(title, welcome, buttonPanel, lblStatus);
        
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
        btnSaveGrades = createStyledButton("Save All Grades", "#1565c0");
        btnBack = createStyledButton("Back to Dashboard", "#1565c0");
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
        gradeTable = new TableView<>();
        gradeTable.setEditable(true);
        
        gradeTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 3);"
        );

        // Make table fill available width
        gradeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns with minimum widths
        TableColumn<Enrollment, String> courseCol = createStyledColumn("Course", "courseName", 250);
        TableColumn<Enrollment, String> studentCol = createStyledColumn("Student", "studentName", 180);
        TableColumn<Enrollment, String> studentIdCol = createStyledColumn("Student ID", "studentId", 120);
        TableColumn<Enrollment, Double> gradeCol = createGradeColumn("Grade", "grade", 120);
        
        gradeTable.getColumns().addAll(courseCol, studentCol, studentIdCol, gradeCol);

        gradeTable.setRowFactory(tv -> new TableRow<Enrollment>() {
            @Override
            protected void updateItem(Enrollment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    // Color code based on grade with blue theme variations
                    if (item.getGrade() != null) {
                        if (item.getGrade() >= 90) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #1e3c72; -fx-border-color: #e3f2fd;"); // Light green for A
                        } else if (item.getGrade() >= 80) {
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72; -fx-border-color: #bbdefb;"); // Light blue for B
                        } else if (item.getGrade() >= 70) {
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #1e3c72; -fx-border-color: #ffecb3;"); // Light orange for C
                        } else if (item.getGrade() >= 60) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #1e3c72; -fx-border-color: #ffcdd2;"); // Light red for D
                        } else {
                            setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #1e3c72; -fx-border-color: #e0e0e0;"); // Gray for F
                        }
                    } else {
                        setStyle("-fx-background-color: #fafafa; -fx-text-fill: #1e3c72; -fx-border-color: #e3f2fd;"); // Very light blue for ungraded
                    }
                }
            }
        });
    }
    
    private <T> TableColumn<Enrollment, T> createStyledColumn(String title, String property, double width) {
        TableColumn<Enrollment, T> column = new TableColumn<>(title);
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
        column.setMinWidth(80); // Set minimum width
        column.setEditable(true);
        
        column.setStyle(
            "-fx-alignment: CENTER;" +
            "-fx-background-color: linear-gradient(to bottom, #1976d2, #1565c0);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #90caf9;"
        );
        
        // Use TextFieldTableCell for easy editing
        column.setCellFactory(tc -> new TextFieldTableCell<Enrollment, Double>(new DoubleStringConverter()) {
            {
                // Style the cell
                setStyle("-fx-alignment: CENTER; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
            
            @Override
            public void updateItem(Double grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty) {
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-alignment: CENTER;");
                } else {
                    if (grade == null) {
                        setText("Not Graded");
                        setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #778da9; -fx-alignment: CENTER; -fx-font-style: italic;");
                    } else {
                        setText(String.format("%.1f", grade));
                        
                        // Color code the grade cell with blue theme variations
                        if (grade >= 90) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (grade >= 80) {
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (grade >= 70) {
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else if (grade >= 60) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #1e3c72; -fx-alignment: CENTER; -fx-font-weight: bold;");
                        }
                    }
                }
            }
            
            @Override
            public void commitEdit(Double newValue) {
                // Validate the grade before committing
                if (newValue != null && (newValue < 0 || newValue > 100)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Grade", "Grade must be between 0 and 100!");
                    cancelEdit();
                    return;
                }
                
                super.commitEdit(newValue);
                
                // Update the enrollment object
                Enrollment enrollment = getTableView().getItems().get(getIndex());
                enrollment.setGrade(newValue);
                
                // Refresh the row to update colors
                getTableView().refresh();
            }
            
            @Override
            public void startEdit() {
                super.startEdit();
                // Style the text field when editing
                TextField textField = (TextField) getGraphic();
                if (textField != null) {
                    textField.setStyle("-fx-background-color: white; -fx-border-color: #64b5f6; -fx-border-radius: 3px;");
                }
            }
        });

        return column;
    }
    
    private VBox createTableContainer() {
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(20));
        tableContainer.setBackground(Background.EMPTY);
        
        Label tableTitle = new Label("Student Grades - Double-click on grades to edit (0-100 scale)");
        tableTitle.setStyle("-fx-text-fill: #e0e1dd; -fx-font-size: 20px; -fx-font-weight: bold; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");
        
        Label instructions = new Label("Instructions: Double-click any grade cell to edit. Enter values between 0-100. Leave empty and press Enter to remove grade.");
        instructions.setStyle("-fx-text-fill: #bbdefb; -fx-font-size: 12px; -fx-font-style: italic;");
        
        // Add grade legend
        HBox legend = createGradeLegend();
        
        // Make table expand to fill available space
        VBox.setVgrow(gradeTable, Priority.ALWAYS);
        gradeTable.setMaxHeight(Double.MAX_VALUE);
        
        VBox.setMargin(tableTitle, new Insets(0, 0, 10, 0));
        VBox.setMargin(instructions, new Insets(0, 0, 10, 0));
        VBox.setMargin(legend, new Insets(0, 0, 15, 0));
        tableContainer.getChildren().addAll(tableTitle, instructions, legend, gradeTable);
        
        return tableContainer;
    }
    
    private HBox createGradeLegend() {
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        
        String legendStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 10px;";
        
        Label aGrade = new Label("A: 90-100");
        aGrade.setStyle(legendStyle + "-fx-background-color: #e8f5e8; -fx-text-fill: #1e3c72;");
        
        Label bGrade = new Label("B: 80-89");
        bGrade.setStyle(legendStyle + "-fx-background-color: #e3f2fd; -fx-text-fill: #1e3c72;");
        
        Label cGrade = new Label("C: 70-79");
        cGrade.setStyle(legendStyle + "-fx-background-color: #fff3e0; -fx-text-fill: #1e3c72;");
        
        Label dGrade = new Label("D: 60-69");
        dGrade.setStyle(legendStyle + "-fx-background-color: #ffebee; -fx-text-fill: #1e3c72;");
        
        Label fGrade = new Label("F: 0-59");
        fGrade.setStyle(legendStyle + "-fx-background-color: #f5f5f5; -fx-text-fill: #1e3c72;");
        
        legend.getChildren().addAll(aGrade, bGrade, cGrade, dGrade, fGrade);
        return legend;
    }
    
    private void setupEventHandlers() {
        btnSaveGrades.setOnAction(e -> saveAllGrades());
        btnBack.setOnAction(e -> backToDashboard());
    }
    
    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            // Color code status messages
            if (message.toLowerCase().contains("error") || message.toLowerCase().contains("failed")) {
                lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else if (message.toLowerCase().contains("success")) {
                lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else if (message.toLowerCase().contains("saving")) {
                lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else {
                lblStatus.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
        }
    }
    
    private void loadGradeData() {
        // Load only courses taught by this instructor
        gradeTable.setItems(FXCollections.observableArrayList(
            enrollmentDao.getEnrollmentsByInstructor(instructorId)
        ));
        updateStatus("Ready - " + gradeTable.getItems().size() + " student enrollments loaded");
    }
    
    private void saveAllGrades() {
        int savedCount = 0;
        int errorCount = 0;
        
        // Show progress
        updateStatus("Saving grades...");
        
        for (Enrollment enrollment : gradeTable.getItems()) {
            System.out.println("Saving grade for enrollment " + enrollment.getEnrollmentId() + 
                              ": " + enrollment.getGrade());
            
            boolean success = enrollmentDao.updateGrade(enrollment.getEnrollmentId(), enrollment.getGrade());
            if (success) {
                savedCount++;
                System.out.println("Grade saved for " + enrollment.getStudentName());
            } else {
                errorCount++;
                System.err.println("Failed to save grade for " + enrollment.getStudentName());
            }
        }
        
        if (errorCount == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     savedCount + " grades saved successfully! ✅\n\n" +
                     "All grade changes have been recorded in the system.");
            updateStatus(savedCount + " grades saved successfully");
        } else {
            showAlert(Alert.AlertType.WARNING, "Partial Success", 
                     savedCount + " grades saved, " + errorCount + " failed to save.\n\n" +
                     "Please check your connection and try again.");
            updateStatus(savedCount + " grades saved, " + errorCount + " failed");
        }
    }
    
private void backToDashboard() {
    // Close the current grade management window
    primaryStage.close();
    
    // Create and show a new Instructor Dashboard
    Stage dashboardStage = new Stage();
    InstructorDashboardController dashboardController = new InstructorDashboardController(dashboardStage);
    dashboardController.showInstructorDashboard();
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
}