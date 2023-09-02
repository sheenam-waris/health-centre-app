package com.example.healthcentre.models;

public class User {

    int UserId;
    String Name;
    private String Email;
    private String Dob;
    private String Gender;
    private String Phone;
    private Role role;

    public Patient patient;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public User(int userId, String name, String email, String dob, String gender, String phone, Role role) {
        UserId = userId;
        Name = name;
        Email = email;
        Dob = dob;
        Gender = gender;
        Phone = phone;
        this.role = role;
        patient=null;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getDob() {
        return Dob;
    }

    public void setDob(String dob) {
        Dob = dob;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


}
