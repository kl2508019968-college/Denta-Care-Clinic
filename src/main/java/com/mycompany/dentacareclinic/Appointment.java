package com.mycompany.dentacareclinic;

public class Appointment {

    private int appointmentId;
    private String patientName;
    private String dentistName;
    private String appointmentDate;
    private String appointmentTime;
    private String treatmentType;
    private String status;

    public Appointment(int appointmentId, String patientName, String dentistName, String appointmentDate, String appointmentTime, String treatmentType, String status) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.dentistName = dentistName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.treatmentType = treatmentType;
        this.status = status;
    }

    public int getAppointmentId() { return appointmentId; }
    public String getPatientName() { return patientName; }
    public String getDentistName() { return dentistName; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getAppointmentTime() { return appointmentTime; }
    public String getTreatmentType() { return treatmentType; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}