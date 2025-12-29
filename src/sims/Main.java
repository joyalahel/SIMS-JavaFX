package sims;

import sims.controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Student Information Management System");
        
        // Use simple class reference instead of package
        LoginController loginController = new LoginController(primaryStage);
        loginController.showLoginScreen();
        
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}