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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    // Metrics Labels
    @FXML
    private Label todayAppointmentsLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML 
    private Label completedAppointmentsLabel;

    // Search Box
    @FXML
    private TextField searchField;

    // Patient TableView
    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TableColumn<Patient, Integer> colId;
    @FXML
    private TableColumn<Patient, String> colName;
    @FXML
    private TableColumn<Patient, String> colIc;
    @FXML
    private TableColumn<Patient, String> colPhone;
    @FXML
    private TableColumn<Patient, String> colGender;

    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboardMetrics();
        loadPatientData();
    }

    // --- 1. METRICS & COUNTS ---
    public void loadDashboardMetrics() {
        // Query Total Patients
        String sqlTotal = "SELECT COUNT(*) FROM patients";
        try (Connection connect = Database.connectDB(); 
             PreparedStatement prepare = connect.prepareStatement(sqlTotal); 
             ResultSet result = prepare.executeQuery()) {

            if (result.next() && totalPatientsLabel != null) {
                totalPatientsLabel.setText(String.valueOf(result.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Query Today's Appointments
        String sqlToday = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ?";
        try (Connection connect = Database.connectDB(); 
             PreparedStatement prepare = connect.prepareStatement(sqlToday)) {

            prepare.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet result = prepare.executeQuery();

            if (result.next() && todayAppointmentsLabel != null) {
                todayAppointmentsLabel.setText(String.valueOf(result.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Query Completed Appointments
        String sqlCompleted = "SELECT COUNT(*) FROM appointments WHERE status = 'Completed'";
        try (Connection connect = Database.connectDB(); 
             PreparedStatement prepare = connect.prepareStatement(sqlCompleted); 
             ResultSet result = prepare.executeQuery()) {

            if (result.next() && completedAppointmentsLabel != null) {
                completedAppointmentsLabel.setText(String.valueOf(result.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 2. LOAD PATIENT TABLE ---
    public void loadPatientData() {
        if (patientTable == null) {
            return;
        }

        patientList.clear();

        // Bind TableView columns to Patient model getters
        colId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colIc.setCellValueFactory(new PropertyValueFactory<>("icNumber"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        String sql = "SELECT * FROM patients";

        try (Connection connect = Database.connectDB(); 
             Statement statement = connect.createStatement(); 
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                patientList.add(new Patient(
                        result.getInt("patient_id"),
                        result.getString("full_name"),
                        result.getString("ic_number"),
                        result.getString("phone_number"),
                        result.getString("gender"),
                        result.getString("dental_concern")
                ));
            }

            patientTable.setItems(patientList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 3. SEARCH FUNCTION ---
    @FXML
    public void searchPatient(ActionEvent event) {
        String keyword = searchField != null ? searchField.getText().trim() : "";

        if (keyword.isEmpty()) {
            loadPatientData(); // Reload full list if search box is empty
            return;
        }

        patientList.clear();
        String sql = "SELECT * FROM patients WHERE full_name LIKE ? OR ic_number LIKE ?";

        try (Connection connect = Database.connectDB(); 
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, "%" + keyword + "%");
            prepare.setString(2, "%" + keyword + "%");
            ResultSet result = prepare.executeQuery();

            boolean found = false;
            while (result.next()) {
                found = true;
                patientList.add(new Patient(
                        result.getInt("patient_id"),
                        result.getString("full_name"),
                        result.getString("ic_number"),
                        result.getString("phone_number"),
                        result.getString("gender"),
                        result.getString("dental_concern")
                ));
            }

            if (patientTable != null) {
                patientTable.setItems(patientList);
            }

            if (!found) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No patient found matching: " + keyword);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to execute search query.");
            e.printStackTrace();
        }
    }

    // --- 4. SIDEBAR NAVIGATION HANDLERS ---
    @FXML
    public void openPatientRegistration(ActionEvent event) throws IOException {
        switchScene(event, "PatientRegistration.fxml");
    }

    @FXML
    public void openBookAppointment(ActionEvent event) throws IOException {
        switchScene(event, "BookAppointment.fxml");
    }

    @FXML
    public void openAppointmentList(ActionEvent event) throws IOException {
        switchScene(event, "AppointmentList.fxml");
    }

    @FXML
    public void openDentalServices(ActionEvent event) throws IOException {
        switchScene(event, "DentalServices.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}