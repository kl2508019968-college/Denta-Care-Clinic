package com.mycompany.dentacareclinic;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class BookAppointmentController implements Initializable {

    @FXML private ComboBox<String> patientComboBox;
    @FXML private ComboBox<String> dentistComboBox;
    @FXML private DatePicker appointmentDatePicker;

    // Time Radio Buttons
    @FXML private RadioButton time9AM;
    @FXML private RadioButton time10AM;
    @FXML private RadioButton time11AM;
    @FXML private RadioButton time2PM;
    @FXML private RadioButton time3PM;
    @FXML private RadioButton time4PM;
    @FXML private ToggleGroup timeToggleGroup;

    // Treatment CheckBoxes
    @FXML private CheckBox checkupCheck;
    @FXML private CheckBox cleaningCheck;
    @FXML private CheckBox extractionCheck;
    @FXML private CheckBox consultationCheck;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPatients();
        loadDentists();
    }

    // 1. Fetch patient names from MySQL database
    private void loadPatients() {
        ObservableList<String> patients = FXCollections.observableArrayList();
        String sql = "SELECT full_name FROM patients";

        try (Connection connect = Database.connectDB();
             Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                patients.add(result.getString("full_name"));
            }

            if (patientComboBox != null) {
                patientComboBox.setItems(patients);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Add dentists to dropdown
    private void loadDentists() {
        ObservableList<String> dentists = FXCollections.observableArrayList(
            "Dr. Wan Maihan",
            "Dr. Nor Sharifah"
        );
        if (dentistComboBox != null) {
            dentistComboBox.setItems(dentists);
        }
    }

    // 3. Book Appointment logic
    @FXML
    public void bookAppointment(ActionEvent event) {
        String selectedPatient = patientComboBox != null ? patientComboBox.getValue() : null;
        String selectedDentist = dentistComboBox != null ? dentistComboBox.getValue() : null;
        LocalDate selectedDate = appointmentDatePicker != null ? appointmentDatePicker.getValue() : null;

        // Get single selected RadioButton time
        String selectedTime = "";
        if (timeToggleGroup != null && timeToggleGroup.getSelectedToggle() != null) {
            RadioButton selectedRadio = (RadioButton) timeToggleGroup.getSelectedToggle();
            selectedTime = selectedRadio.getText();
        }

        // Gather treatments
        StringBuilder treatmentBuilder = new StringBuilder();
        if (checkupCheck != null && checkupCheck.isSelected()) treatmentBuilder.append("Check-up, ");
        if (cleaningCheck != null && cleaningCheck.isSelected()) treatmentBuilder.append("Cleaning, ");
        if (extractionCheck != null && extractionCheck.isSelected()) treatmentBuilder.append("Extraction, ");
        if (consultationCheck != null && consultationCheck.isSelected()) treatmentBuilder.append("Consultation, ");

        String treatments = treatmentBuilder.toString();
        if (treatments.endsWith(", ")) {
            treatments = treatments.substring(0, treatments.length() - 2);
        }

        // Validation
        if (selectedPatient == null || selectedDentist == null || selectedDate == null || selectedTime.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please select Patient, Dentist, Date, and Time.");
            return;
        }

        String sql = "INSERT INTO appointments (patient_name, dentist_name, appointment_date, appointment_time, treatment_type, status) VALUES (?, ?, ?, ?, ?, 'Scheduled')";

        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, selectedPatient);
            prepare.setString(2, selectedDentist);
            prepare.setDate(3, Date.valueOf(selectedDate));
            prepare.setString(4, selectedTime);
            prepare.setString(5, treatments);

            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment successfully booked!");
            backToDashboard(event);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to book appointment. Ensure appointments table exists.");
            e.printStackTrace();
        }
    }

    @FXML
    public void clearFields(ActionEvent event) {
        if (patientComboBox != null) patientComboBox.setValue(null);
        if (dentistComboBox != null) dentistComboBox.setValue(null);
        if (appointmentDatePicker != null) appointmentDatePicker.setValue(null);
        if (timeToggleGroup != null) timeToggleGroup.selectToggle(null);
        if (checkupCheck != null) checkupCheck.setSelected(false);
        if (cleaningCheck != null) cleaningCheck.setSelected(false);
        if (extractionCheck != null) extractionCheck.setSelected(false);
        if (consultationCheck != null) consultationCheck.setSelected(false);
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