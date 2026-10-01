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

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Check if username or password field is empty
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please enter both username and password.");
            return;
        }

        // Query phpMyAdmin database to verify admin credentials
        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";

        try (Connection connect = Database.connectDB(); PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, username);
            prepare.setString(2, password);

            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Login Successful! Welcome to DentaCare Admin Portal.");

                // Redirect to Dashboard.fxml
                Parent root = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Username or Password.");
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Page Error", "Could not load Dashboard.fxml screen.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Connection failed! Make sure MySQL is running in XAMPP.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAdminRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Enter desired Username and Password above, then click 'Register New Admin'.");
            return;
        }

        String sql = "INSERT INTO admin (username, password) VALUES (?, ?)";

        try (Connection connect = Database.connectDB(); PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, username);
            prepare.setString(2, password);
            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Registration Success", "New Admin account registered successfully! You can now log in.");
            passwordField.clear();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username may already exist or database connection failed.");
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

    @FXML
    public void openAdminRegistration(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("AdminRegistration.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
