package com.mycompany.dentacareclinic;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AppointmentListController implements Initializable {

    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colDentist;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TextField searchField;

    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadAppointments();
    }

    private void setupTableColumns() {
        if (colPatient != null) colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        if (colDate != null) colDate.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        if (colTime != null) colTime.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        if (colDentist != null) colDentist.setCellValueFactory(new PropertyValueFactory<>("dentistName"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void loadAppointments() {
        appointmentList.clear();
        String query = "SELECT * FROM appointments";

        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                appointmentList.add(new Appointment(
                        rs.getInt("appointment_id"), // Fixed column name
                        rs.getString("patient_name"),
                        rs.getString("dentist_name"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("treatment_type"),
                        rs.getString("status")
                ));
            }
            appointmentTable.setItems(appointmentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. MARK APPOINTMENT AS COMPLETED
    @FXML
    public void markAsCompleted(ActionEvent event) {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an appointment from the table.");
            return;
        }

        updateStatusInDatabase(selected.getAppointmentId(), "Completed");
    }

    // 2. CANCEL APPOINTMENT
    @FXML
    public void cancelAppointment(ActionEvent event) {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an appointment from the table.");
            return;
        }

        updateStatusInDatabase(selected.getAppointmentId(), "Cancelled");
    }

    // 3. EDIT / RESCHEDULE APPOINTMENT
    @FXML
    public void editAppointment(ActionEvent event) {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an appointment to edit.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reschedule / Edit Appointment");
        dialog.setHeaderText("Editing appointment for: " + selected.getPatientName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker datePicker = new DatePicker();
        ComboBox<String> timeBox = new ComboBox<>(FXCollections.observableArrayList(
                "9:00 AM", "10:00 AM", "11:00 AM", "2:00 PM", "3:00 PM", "4:00 PM"
        ));
        timeBox.setValue(selected.getAppointmentTime());

        ComboBox<String> dentistBox = new ComboBox<>(FXCollections.observableArrayList(
                "Dr. Nor Sharifah", "Dr. Azman", "Dr. Tan"
        ));
        dentistBox.setValue(selected.getDentistName());

        grid.add(new Label("New Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("New Time:"), 0, 1);
        grid.add(timeBox, 1, 1);
        grid.add(new Label("Dentist:"), 0, 2);
        grid.add(dentistBox, 1, 2);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newDate = (datePicker.getValue() != null) ? datePicker.getValue().toString() : selected.getAppointmentDate();
            String newTime = timeBox.getValue();
            String newDentist = dentistBox.getValue();

            String sql = "UPDATE appointments SET appointment_date = ?, appointment_time = ?, dentist_name = ? WHERE appointment_id = ?";
            try (Connection conn = Database.connectDB();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, newDate);
                ps.setString(2, newTime);
                ps.setString(3, newDentist);
                ps.setInt(4, selected.getAppointmentId());

                ps.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Updated", "Appointment updated successfully!");
                loadAppointments();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 4. DELETE PERMANENTLY
    @FXML
    public void deleteAppointment(ActionEvent event) {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an appointment to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete this appointment?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM appointments WHERE appointment_id = ?";
            try (Connection conn = Database.connectDB();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, selected.getAppointmentId());
                ps.executeUpdate();
                loadAppointments();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateStatusInDatabase(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();

            loadAppointments();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void searchAppointment(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAppointments();
            return;
        }

        appointmentList.clear();
        String sql = "SELECT * FROM appointments WHERE patient_name LIKE ?";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointmentList.add(new Appointment(
                        rs.getInt("appointment_id"), // Fixed column name
                        rs.getString("patient_name"),
                        rs.getString("dentist_name"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("treatment_type"),
                        rs.getString("status")
                ));
            }
            appointmentTable.setItems(appointmentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
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