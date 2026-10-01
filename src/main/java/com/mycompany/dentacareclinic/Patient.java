package com.mycompany.dentacareclinic;

public class Patient {
    private int patientId;
    private String fullName;
    private String icNumber;
    private String phoneNumber;
    private String gender;
    private String dentalConcern;

    public Patient(int patientId, String fullName, String icNumber, String phoneNumber, String gender, String dentalConcern) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.icNumber = icNumber;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.dentalConcern = dentalConcern;
    }

    public int getPatientId() { return patientId; }
    public String getFullName() { return fullName; }
    public String getIcNumber() { return icNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getGender() { return gender; }
    public String getDentalConcern() { return dentalConcern; }
}