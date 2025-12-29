/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sims.util;
import javafx.geometry.Insets;
import sims.dao.UserDao;
import sims.UserSession;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import java.util.Optional;

/**
 *
 * @author Joy Ahel
 */

public class PasswordUtil {
 public static void showChangePasswordDialog() {
        UserSession session = UserSession.getInstance();
        UserDao userDao = new UserDao();
        
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change Your Password");
        
        // Set button types
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        PasswordField currentPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        
        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(400, 250);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                if (newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    boolean success = userDao.changePassword(
                        session.getUserId(), 
                        newPasswordField.getText()
                    );
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
                        return true;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to change password!");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match!");
                }
            }
            return false;
        });
        
        dialog.showAndWait();
    }
    
    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}