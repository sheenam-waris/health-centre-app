package com.example.healthcentre.models;

public class MedicalCertificate {
    private int medCert_id;
    private int appointmentId;
    private int patient_id;

    private  String purpose;
    private String duration;
    private String status;

    private String approvalDate;
    private String requestedDate;

    public MedicalCertificate(int medCert_id, int appointmentId, int patient_id, String purpose, String duration, String status, String approvalDate, String requestedDate) {
        this.medCert_id = medCert_id;
        this.appointmentId = appointmentId;
        this.patient_id = patient_id;
        this.purpose = purpose;
        this.duration = duration;
        this.status = status;
        this.approvalDate = approvalDate;
        this.requestedDate = requestedDate;
    }

    public int getMedCert_id() {
        return medCert_id;
    }

    public void setMedCert_id(int medCert_id) {
        this.medCert_id = medCert_id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(String requestedDate) {
        this.requestedDate = requestedDate;
    }
}
