package com.example.healthcentre.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import kotlinx.coroutines.channels.ActorKt;

public class Appointment implements Parcelable {
    private int appointmentId;
    private  String schedule;
    private String reason;
    private String status;

    private int user_id;

    private String reportingTime;
    private String treatmentName;


    private boolean visited;

    public Appointment(int appointmentId,String schedule, String reason, String status,boolean visited) {
        this.appointmentId = appointmentId;
        this.schedule = schedule;
        this.reason = reason;
        this.status = status;
        this.visited = visited;
    }

    protected Appointment(Parcel in) {
        appointmentId = in.readInt();
        schedule = in.readString();
        reason = in.readString();
        status = in.readString();
        user_id = in.readInt();
        reportingTime = in.readString();
        treatmentName = in.readString();
        visited = in.readByte() != 0;
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setUser_id(int user_id){this.user_id = user_id;}

    public int getUser_id() {
        return user_id;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public String getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(String reportingTime) {
        this.reportingTime = reportingTime;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }



    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(reason);
        parcel.writeString(status);
        parcel.writeString(schedule);
        parcel.writeString(treatmentName);
        parcel.writeString(reportingTime);
        parcel.writeInt(appointmentId);
        parcel.writeInt(user_id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(visited);
        }else{
            parcel.writeByte((byte) (visited ? 1 : 0));
        }
    }
}
