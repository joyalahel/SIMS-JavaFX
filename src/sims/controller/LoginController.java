package sims.controller;

import sims.UserSession;
import sims.dao.UserDao;
import sims.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;

public class LoginController {
    private Stage primaryStage;
    private UserDao userDao;
    
    public LoginController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userDao = new UserDao();
    }
    
    public void showLoginScreen() {
        // Create main container with university image background
        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        
        // Create login card
        VBox loginCard = createLoginCard();
        
        root.setCenter(loginCard);
        
        // Create full screen scene
        Scene scene = new Scene(root);
        
        // Set stage to full screen
        primaryStage.setTitle("Student Information Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Maximize window
        primaryStage.setFullScreen(false); // Optional: set to true for true fullscreen (esc to exit)
        primaryStage.show();
    }
    
    private Background createBackground() {
        try {
            // Use the university image path
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
            // Fallback to blue gradient if image fails to load
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
    
    private VBox createLoginCard() {
        VBox loginCard = new VBox(25);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40, 50, 50, 50));
        loginCard.setMaxWidth(400);
        loginCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.92);" + // More opaque for better readability
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-border-color: #90caf9;" +
            "-fx-border-width: 2px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(13,27,42,0.6), 25, 0, 0, 10);" // Enhanced shadow
        );
        
        // Title section
        VBox titleSection = createTitleSection();
        
        // Login form
        GridPane loginForm = createLoginForm();
        
        loginCard.getChildren().addAll(titleSection, loginForm);
        return loginCard;
    }
    
    private VBox createTitleSection() {
        VBox titleSection = new VBox(10);
        titleSection.setAlignment(Pos.CENTER);
        
        // Main title
        Label mainTitle = new Label("SIMS");
        mainTitle.setStyle(
            "-fx-text-fill: #1e3c72;" +
            "-fx-font-size: 36px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(30,60,114,0.4), 6, 0, 0, 3);"
        );
        mainTitle.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 36));
        
        // Subtitle
        Label subTitle = new Label("Student Information Management System");
        subTitle.setStyle(
            "-fx-text-fill: #415a77;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: normal;"
        );
        
        // Welcome message
        Label welcomeLabel = new Label("Welcome Back!");
        welcomeLabel.setStyle(
            "-fx-text-fill: #0d47a1;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 0 0 0;"
        );
        
        titleSection.getChildren().addAll(mainTitle, subTitle, welcomeLabel);
        return titleSection;
    }
    
    private GridPane createLoginForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(20);
        form.setPadding(new Insets(20, 0, 0, 0));
        form.setAlignment(Pos.CENTER);
        
        // Username section
        Label userLabel = createStyledLabel("Username:");
        TextField usernameField = createStyledTextField("Enter your username");
        
        // Password section
        Label passLabel = createStyledLabel("Password:");
        PasswordField passwordField = createStyledPasswordField("Enter your password");
        
        // Login button
        Button loginButton = createStyledLoginButton();
        
        // Set up event handlers
        loginButton.setOnAction(e -> performLogin(usernameField.getText(), passwordField.getText()));
        passwordField.setOnAction(e -> performLogin(usernameField.getText(), passwordField.getText()));
        
        // Add components to form
        form.add(userLabel, 0, 0);
        form.add(usernameField, 0, 1);
        form.add(passLabel, 0, 2);
        form.add(passwordField, 0, 3);
        form.add(loginButton, 0, 4);
        
        // Set column constraints for proper sizing
        ColumnConstraints colConst = new ColumnConstraints();
        colConst.setPrefWidth(250);
        form.getColumnConstraints().add(colConst);
        
        return form;
    }
    
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-text-fill: #0d47a1;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;"
        );
        return label;
    }
    
    private TextField createStyledTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #64b5f6;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 12px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(100,181,246,0.2), 5, 0, 0, 2);"
        );
        
        // Add focus effects
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #1976d2;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px 15px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.3), 8, 0, 0, 3);"
                );
            } else {
                textField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #64b5f6;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px 15px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(100,181,246,0.2), 5, 0, 0, 2);"
                );
            }
        });
        
        return textField;
    }
    
    private PasswordField createStyledPasswordField(String prompt) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(prompt);
        passwordField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #64b5f6;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 12px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(100,181,246,0.2), 5, 0, 0, 2);"
        );
        
        // Add focus effects
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #1976d2;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px 15px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.3), 8, 0, 0, 3);"
                );
            } else {
                passwordField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #64b5f6;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px 15px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(100,181,246,0.2), 5, 0, 0, 2);"
                );
            }
        });
        
        return passwordField;
    }
    
    private Button createStyledLoginButton() {
        Button loginButton = new Button("🔑 Login");
        loginButton.setStyle(
            "-fx-background-color: #1565c0;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-color: derive(#1565c0, 20%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 200px;"
        );
        
        // Hover effects
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
            "-fx-background-color: derive(#1565c0, 20%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-color: derive(#1565c0, 40%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 5);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 200px;"
        ));
        
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
            "-fx-background-color: #1565c0;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-color: derive(#1565c0, 20%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 200px;"
        ));
        
        // Pressed effect
        loginButton.setOnMousePressed(e -> loginButton.setStyle(
            "-fx-background-color: #0d47a1;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-color: derive(#0d47a1, 20%);" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 200px;"
        ));
        
        return loginButton;
    }
    
    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both username and password!");
            return;
        }
        
        User user = userDao.authenticate(username, password);
        if (user != null) {
            UserSession session = UserSession.getInstance();
            session.setUser(user.getId(), user.getUsername(), user.getRole(), user.getFullName());
            
            String roleDisplay = getRoleDisplayName(user.getRole());
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", 
                     "Welcome " + user.getFullName() + "! \nRole: " + roleDisplay);
            
            // Open appropriate dashboard based on role
            openDashboard();
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", 
                     "Invalid username or password! \nPlease check your credentials and try again.");
        }
    }
    
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin": return "Administrator";
            case "instructor": return "Instructor";
            case "student": return "Student";
            default: return role;
        }
    }
    
  private void openDashboard() {
    UserSession session = UserSession.getInstance();
    
    if (session.isAdmin()) {
        AdminDashboardController adminController = new AdminDashboardController(primaryStage);
        adminController.showAdminDashboard();
    } else if (session.isInstructor()) {
        InstructorDashboardController instructorController = new InstructorDashboardController(primaryStage);
        instructorController.showInstructorDashboard();
    } else {
        // Students go to the Student Dashboard
        StudentDashboardController studentController = new StudentDashboardController(primaryStage);
        studentController.showStudentDashboard();
    }
}
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert to match the blue theme
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