package com.mycompany.dentacareclinic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class PatientRegistrationController {

    @FXML private TextField fullNameField;
    @FXML private TextField icNumberField;
    @FXML private TextField phoneField;

    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private ToggleGroup genderToggleGroup;

    @FXML private CheckBox toothacheCheck;
    @FXML private CheckBox toothCleaningCheck;
    @FXML private CheckBox generalCheckupCheck;
    @FXML private CheckBox toothExtractionCheck;

    @FXML
    public void registerPatient(ActionEvent event) {
        savePatientData(event);
    }

    @FXML
    public void savePatient(ActionEvent event) {
        savePatientData(event);
    }

    private void savePatientData(ActionEvent event) {
        String fullName = (fullNameField != null) ? fullNameField.getText().trim() : "";
        String icNumber = (icNumberField != null) ? icNumberField.getText().trim() : "";
        String phone = (phoneField != null) ? phoneField.getText().trim() : "";

        // Extract selected Gender
        String gender = "";
        if (maleRadio != null && maleRadio.isSelected()) {
            gender = "Male";
        } else if (femaleRadio != null && femaleRadio.isSelected()) {
            gender = "Female";
        }

        // Extract checked Dental Concerns
        StringBuilder concernBuilder = new StringBuilder();
        if (toothacheCheck != null && toothacheCheck.isSelected()) concernBuilder.append("Toothache, ");
        if (toothCleaningCheck != null && toothCleaningCheck.isSelected()) concernBuilder.append("Tooth Cleaning, ");
        if (generalCheckupCheck != null && generalCheckupCheck.isSelected()) concernBuilder.append("General Check-up, ");
        if (toothExtractionCheck != null && toothExtractionCheck.isSelected()) concernBuilder.append("Tooth Extraction, ");

        String concern = concernBuilder.toString();
        if (concern.endsWith(", ")) {
            concern = concern.substring(0, concern.length() - 2);
        }

        // Validate required fields
        if (fullName.isEmpty() || icNumber.isEmpty() || phone.isEmpty() || gender.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please fill in all required fields (Name, IC, Phone, and Gender).");
            return;
        }

        String sql = "INSERT INTO patients (full_name, ic_number, phone_number, gender, dental_concern) VALUES (?, ?, ?, ?, ?)";

        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, fullName);
            prepare.setString(2, icNumber);
            prepare.setString(3, phone);
            prepare.setString(4, gender);
            prepare.setString(5, concern);

            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient successfully registered!");
            backToDashboard(event);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save patient to database. Check MySQL connection.");
            e.printStackTrace();
        }
    }

    @FXML
    public void clearFields(ActionEvent event) {
        if (fullNameField != null) fullNameField.clear();
        if (icNumberField != null) icNumberField.clear();
        if (phoneField != null) phoneField.clear();
        if (maleRadio != null) maleRadio.setSelected(false);
        if (femaleRadio != null) femaleRadio.setSelected(false);
        if (toothacheCheck != null) toothacheCheck.setSelected(false);
        if (toothCleaningCheck != null) toothCleaningCheck.setSelected(false);
        if (generalCheckupCheck != null) generalCheckupCheck.setSelected(false);
        if (toothExtractionCheck != null) toothExtractionCheck.setSelected(false);
    }

    @FXML
    public void backToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
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