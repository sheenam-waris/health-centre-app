package com.example.healthcentre.models;

public class Patient {

    private String RollNum;
    private String Address;
    private String Hostel;

    public Patient(String rollNum, String address, String hostel) {
        this.RollNum = rollNum;
        this.Address = address;
        this.Hostel = hostel;
    }

    public String getRollNum() {
        return RollNum;
    }

    public void setRollNum(String rollNum) {
        RollNum = rollNum;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getHostel() {
        return Hostel;
    }

    public void setHostel(String hostel) {
        Hostel = hostel;
    }


}
