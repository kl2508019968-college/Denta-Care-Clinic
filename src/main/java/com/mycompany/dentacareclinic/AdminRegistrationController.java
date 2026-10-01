package com.mycompany.dentacareclinic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminRegistrationController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    public void registerAdmin(ActionEvent event) {
        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText().trim() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText().trim() : "";

        // Validations
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all the fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match. Please try again.");
            return;
        }

        // Matching your database structure: admin (id, username, password)
        String checkSql = "SELECT * FROM admin WHERE username = ?";
        String insertSql = "INSERT INTO admin (username, password) VALUES (?, ?)";

        try (Connection connect = Database.connectDB()) {
            if (connect == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to database.");
                return;
            }

            // 1. Check duplicate username
            try (PreparedStatement checkPrepare = connect.prepareStatement(checkSql)) {
                checkPrepare.setString(1, username);
                ResultSet result = checkPrepare.executeQuery();

                if (result.next()) {
                    showAlert(Alert.AlertType.WARNING, "Duplicate Username", "Username already exists. Please choose another.");
                    return;
                }
            }

            // 2. Insert new admin into database
            try (PreparedStatement insertPrepare = connect.prepareStatement(insertSql)) {
                insertPrepare.setString(1, username);
                insertPrepare.setString(2, password);

                insertPrepare.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "New Admin registered successfully!");
                backToLogin(event);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void backToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}