package sims.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import sims.dao.AttendanceDao;
import sims.dao.CourseDao;
import sims.model.Course;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;

import java.util.List;
import java.util.stream.Collectors;

public class AdminAttendanceController {
    private Stage primaryStage;
    private AttendanceDao attendanceDao;
    private CourseDao courseDao;
    private ObservableList<AttendanceDao.AttendanceSummary> attendanceData;
    private TableView<AttendanceDao.AttendanceSummary> attendanceTable;
    private ComboBox<Course> courseComboBox;
    private ComboBox<Integer> weekComboBox;
    private Label statsLabel;
    
    public AdminAttendanceController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.attendanceDao = new AttendanceDao();
        this.courseDao = new CourseDao();
        this.attendanceData = FXCollections.observableArrayList();
    }
    
    public void showAdminAttendanceView() {
        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        
        VBox attendanceContent = createAttendanceContent();
        root.setCenter(attendanceContent);
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("Attendance Overview - Admin");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(false);
        primaryStage.show();
    }
    
    private VBox createAttendanceContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-border-color: #1976d2;" +
            "-fx-border-width: 2px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.3), 25, 0, 0, 10);"
        );
        
        // Header section
        VBox headerBox = new VBox(12);
        headerBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label title = new Label("Attendance Overview - All Courses");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #0d47a1; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(13,71,161,0.2), 4, 0, 0, 2);");
        
        Label subtitle = new Label("Monitor attendance statistics across all courses and instructors");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #1976d2; -fx-font-weight: 500;");
        
        headerBox.getChildren().addAll(title, subtitle);
        
        // Controls section
        VBox controlsSection = new VBox(20);
        controlsSection.setPadding(new Insets(25));
        controlsSection.setStyle("-fx-background-color: linear-gradient(to bottom, #e3f2fd, #bbdefb); " +
                               "-fx-background-radius: 15px; " +
                               "-fx-border-color: #64b5f6; " +
                               "-fx-border-width: 1px; " +
                               "-fx-border-radius: 15px;");
        
        // Course filter
        HBox courseBox = new HBox(15);
        courseBox.setAlignment(Pos.CENTER_LEFT);
        
        Label courseLabel = new Label("Filter by Course:");
        courseLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d47a1; -fx-font-size: 14px;");
        courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("All Courses");
        courseComboBox.setPrefWidth(350);
        courseComboBox.setStyle("-fx-background-color: white; -fx-border-color: #42a5f5; -fx-border-radius: 8px; " +
                               "-fx-font-size: 13px; -fx-padding: 10px;");
        
        // Week filter
        HBox weekBox = new HBox(15);
        weekBox.setAlignment(Pos.CENTER_LEFT);
        
        Label weekLabel = new Label("Filter by Week:");
        weekLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d47a1; -fx-font-size: 14px;");
        weekComboBox = new ComboBox<>();
        weekComboBox.setPromptText("All Weeks");
        weekComboBox.setPrefWidth(150);
        weekComboBox.setStyle("-fx-background-color: white; -fx-border-color: #42a5f5; -fx-border-radius: 8px; " +
                             "-fx-font-size: 13px; -fx-padding: 10px;");
        
        weekComboBox.getItems().add(null);
        for (int i = 1; i <= 13; i++) {
            weekComboBox.getItems().add(i);
        }
        
        courseBox.getChildren().addAll(courseLabel, courseComboBox);
        weekBox.getChildren().addAll(weekLabel, weekComboBox);
        
        // Back button
        Button backButton = createBackButton();
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().add(backButton);
        
        controlsSection.getChildren().addAll(courseBox, weekBox, buttonBox);
        
        // Statistics summary
        statsLabel = new Label("Select filters to view attendance statistics");
        statsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #0d47a1; -fx-font-weight: bold; " +
                           "-fx-background-color: linear-gradient(to right, #bbdefb, #e3f2fd); " +
                           "-fx-background-radius: 10px; " +
                           "-fx-padding: 15px; -fx-alignment: center; " +
                           "-fx-border-color: #64b5f6; -fx-border-width: 1px; -fx-border-radius: 10px;");
        statsLabel.setMaxWidth(Double.MAX_VALUE);
        statsLabel.setAlignment(Pos.CENTER);
        
        // Attendance table
        attendanceTable = createAttendanceTable();
        attendanceTable.setItems(attendanceData);
        
        VBox tableContainer = new VBox();
        tableContainer.getChildren().add(attendanceTable);
        VBox.setVgrow(attendanceTable, Priority.ALWAYS);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        
        loadAllCourses(courseComboBox);
        refreshData();
        
        courseComboBox.setOnAction(e -> refreshData());
        weekComboBox.setOnAction(e -> refreshData());
        
        mainLayout.getChildren().addAll(headerBox, controlsSection, statsLabel, tableContainer);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        
        return mainLayout;
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
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#1e3c72")),
            new Stop(1, Color.web("#2a5298"))
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }
    
    private Button createBackButton() {
        Button backButton = new Button("← Back to Admin Dashboard");
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
        
        backButton.setOnAction(e -> {
            primaryStage.close();
            System.out.println("Returning to Admin Dashboard...");
        });
        
        return backButton;
    }
    
   private TableView<AttendanceDao.AttendanceSummary> createAttendanceTable() {
    TableView<AttendanceDao.AttendanceSummary> table = new TableView<>();
    table.setPlaceholder(new Label("No attendance data available. Select filters to load data."));
    
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
    TableColumn<AttendanceDao.AttendanceSummary, String> courseIdCol = createStyledColumn("COURSE ID", "courseId", 120);
    TableColumn<AttendanceDao.AttendanceSummary, String> courseNameCol = createStyledColumn("COURSE NAME", "courseName", 250);
    TableColumn<AttendanceDao.AttendanceSummary, String> instructorCol = createStyledColumn("INSTRUCTOR", "instructorName", 200);
    TableColumn<AttendanceDao.AttendanceSummary, Integer> weekCol = createStyledColumn("WEEK", "weekNumber", 80);
    TableColumn<AttendanceDao.AttendanceSummary, Integer> totalCol = createStyledColumn("TOTAL", "totalStudents", 80);
    TableColumn<AttendanceDao.AttendanceSummary, Integer> presentCol = createStyledColumn("PRESENT", "presentCount", 80);
    TableColumn<AttendanceDao.AttendanceSummary, Double> rateCol = createStyledColumn("RATE", "attendancePercentage", 80);

    // Apply custom cell factory for rate column to format percentage
    rateCol.setCellFactory(col -> new TableCell<AttendanceDao.AttendanceSummary, Double>() {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("-fx-background-color: white;");
            } else {
                setText(String.format("%.1f%%", item));
                setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 8px;");
            }
        }
    });

    table.getColumns().addAll(courseIdCol, courseNameCol, instructorCol, weekCol, totalCol, presentCol, rateCol);

    // Apply the same row factory styling
    table.setRowFactory(tv -> new TableRow<AttendanceDao.AttendanceSummary>() {
        @Override
        protected void updateItem(AttendanceDao.AttendanceSummary item, boolean empty) {
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

private <T> TableColumn<AttendanceDao.AttendanceSummary, T> createStyledColumn(String title, String property, double width) {
    TableColumn<AttendanceDao.AttendanceSummary, T> column = new TableColumn<>(title);
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
    column.setCellFactory(tc -> new TableCell<AttendanceDao.AttendanceSummary, T>() {
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
    private void loadAllCourses(ComboBox<Course> comboBox) {
        try {
            List<Course> courses = courseDao.getAllCourses();
            if (courses != null && !courses.isEmpty()) {
                comboBox.getItems().add(null);
                comboBox.getItems().addAll(courses);
            } else {
                comboBox.setPromptText("No courses available");
                comboBox.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading courses: " + e.getMessage());
            comboBox.setPromptText("Error loading courses");
            comboBox.setDisable(true);
        }
    }
    
    private void refreshData() {
        try {
            List<AttendanceDao.AttendanceSummary> summary = attendanceDao.getAttendanceSummaryForAdmin();
            attendanceData.clear();
            
            if (summary != null && !summary.isEmpty()) {
                List<AttendanceDao.AttendanceSummary> filteredSummary = summary.stream()
                    .filter(entry -> {
                        // Always filter out "All" weeks (only show specific weeks 1-13)
                        boolean weekFilter = entry.getWeekNumber() > 0;
                        
                        // Apply course filter if selected
                        Course selectedCourse = courseComboBox.getValue();
                        boolean courseFilter = selectedCourse == null || 
                                             entry.getCourseId().equals(selectedCourse.getCourseId());
                        
                        // Apply week filter if selected
                        Integer selectedWeek = weekComboBox.getValue();
                        boolean weekSelectionFilter = selectedWeek == null || 
                                                    entry.getWeekNumber() == selectedWeek;
                        
                        return weekFilter && courseFilter && weekSelectionFilter;
                    })
                    .collect(Collectors.toList());
                
                attendanceData.addAll(filteredSummary);
                updateStatisticsLabel(filteredSummary);
                
            } else {
                attendanceTable.setPlaceholder(new Label("No attendance data available for the selected filters."));
                statsLabel.setText("No attendance data available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            attendanceTable.setPlaceholder(new Label("Error loading attendance data. Please try again."));
            statsLabel.setText("Error loading attendance data");
            showAlert("Error loading attendance data: " + e.getMessage());
        }
    }
    
    private void updateStatisticsLabel(List<AttendanceDao.AttendanceSummary> filteredSummary) {
        if (filteredSummary.isEmpty()) {
            statsLabel.setText("No attendance data available for the selected filters");
            return;
        }
        
        int totalRecords = filteredSummary.size();
        int totalStudents = filteredSummary.stream().mapToInt(AttendanceDao.AttendanceSummary::getTotalStudents).sum();
        int totalPresent = filteredSummary.stream().mapToInt(AttendanceDao.AttendanceSummary::getPresentCount).sum();
        double overallRate = totalStudents > 0 ? (double) totalPresent / totalStudents * 100 : 0;
        
        Course selectedCourse = courseComboBox.getValue();
        Integer selectedWeek = weekComboBox.getValue();
        
        StringBuilder statsText = new StringBuilder();
        
        if (selectedCourse != null || selectedWeek != null) {
            statsText.append("Filtered Statistics: ");
            if (selectedCourse != null) {
                statsText.append("Course: ").append(selectedCourse.getCourseName());
            }
            if (selectedWeek != null) {
                if (selectedCourse != null) statsText.append(" • ");
                statsText.append("Week: ").append(selectedWeek);
            }
            statsText.append(" • ");
        } else {
            statsText.append("Overall Statistics: ");
        }
        
        statsText.append(String.format("%d Records • %d Total Students • %d Present • %.1f%% Attendance Rate",
            totalRecords, totalStudents, totalPresent, overallRate));
        
        statsLabel.setText(statsText.toString());
    }
    
    public void refreshAttendanceData() {
        refreshData();
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attendance Overview");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public VBox createAdminAttendanceView() {
        return createAttendanceContent();
    }
}